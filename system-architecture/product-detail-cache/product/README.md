# Product Cache 구현 설명

핵심은 인기 상품 조회를 Redis에서 빠르게 응답하면서, 상품 수정 이후 오래된 데이터가 다시 캐시에 저장되는 race condition을 막는 것이다.

## 요구사항별 처리

### 1. 상품 상세 API 응답 시간 P95 200ms 이하

- Redis cache-aside 방식

```text
GET /products/{productId}
-> Redis product:detail:{productId} 조회
-> cache hit이면 DB 조회 없이 응답
-> cache miss이면 DB 조회
-> Redis에 상품 상세 저장
-> 응답 반환
```

인기 상품처럼 같은 상품을 반복 조회하는 요청은 대부분 Redis에서 처리되므로 DB 접근 횟수를 줄일 수 있다. Redis 장애나 저장 실패가 발생하면 캐시를 포기하고 DB 조회 결과를 반환한다.

### 2. 상품 수정은 적게 발생한다

- 읽기 최적화를 우선하고, 수정 시에는 캐시 refresh 이벤트를 남긴다.

읽기와 쓰기 비율을 `99:1`로 가정하므로 조회 요청은 캐시에서 빠르게 처리하고, 수정 요청은 DB 업데이트를 우선한다.

```text
PUT /products/{productId}
-> DB 상품 수정
-> 상품 version 증가
-> outbox event 저장
-> 응답 반환
```

수정 요청에서 Redis까지 반드시 즉시 성공시켜야 한다고 강제하면 Redis 장애가 상품 수정 API 실패로 전파될 수 있다. 그래서 DB 변경 사실은 outbox event로 남기고, 별도 worker가 Redis를 최신 값으로 갱신한다.

### 3. 상품 수정 이후 캐시와 DB 간 정합성을 해결해야 한다

- DB 최신 값을 기준으로 Redis를 refresh하고, version key로 stale write를 막는다.

상품이 수정되면 단순히 캐시를 삭제하는 대신 worker가 DB의 최신 상품을 다시 읽어 Redis를 최신 값으로 갱신한다.

```text
scheduler/worker
-> PENDING outbox event 조회
-> DB에서 최신 상품 조회
-> Redis product:version:{productId} 저장
-> Redis product:detail:{productId} 최신 값으로 refresh
-> event 상태를 PROCESSED로 변경
```

조회 요청이 늦게 끝나면서 과거 DB 조회 결과를 Redis에 다시 저장하는 상황은 `product:version:{productId}`로 막는다.

```text
저장하려는 상품 version < Redis의 최신 version
-> Redis product:detail:{productId}에 저장하지 않음
```

Redis refresh가 실패하면 outbox event는 즉시 사라지지 않고 재시도 상태로 남는다.

```text
Redis refresh 실패
-> retryCount 증가
-> nextRetryAt 갱신
-> status=PENDING 유지

재시도 횟수 초과
-> status=FAILED
```

스케줄러는 n초(여기선 5라 가정)마다 처리 가능한 `PENDING` 이벤트를 다시 확인한다.

```properties
product.cache.invalidation-fixed-delay-ms=5000
product.cache.max-retry-count=20
```

### 4. 평상시 2,000RPS, 피크 30,000RPS 처리

- 운영 확장은 Redis Cluster와 local cache로 확장한다.

현재 코드는 단일 Redis로 동작한다. 하지만 key가 상품 ID 기반으로 구성되어 있어 Redis Cluster로 확장할 때도 같은 key 전략을 사용할 수 있다.

```text
product:detail:{productId}
product:version:{productId}
```

운영 환경에서 피크 트래픽이 커지면 다음 방식으로 확장한다.

```text
Client
-> Product API
-> Local Cache
-> Redis Cluster
-> DB
```

local cache는 초인기 상품 hot key 요청이 Redis에 매번 도달하지 않도록 짧은 TTL로 둔다. Redis TTL에는 jitter를 추가해 여러 상품의 캐시가 동시에 만료되는 상황을 줄인다.

## Local Cache 적용 시 주의 사항

상품 수정이 적고 인기 상품 목록을 고정하거나 예측할 수 있다면, 인기 상품을 local cache에 미리 적재해 DB 부하를 줄일 수 있다.

```text
Application start
-> 인기 상품 조회
-> local cache warm-up
-> 인기 상품 요청은 local cache에서 응답
```

다만 캐싱해야 하는 상품 수가 많거나, 상품 변경이 자주 발생하거나, 인기 상품 목록이 자주 바뀐다면 local cache만으로 관리하기 어렵다. 이 경우 Redis 같은 글로벌 캐시를 두는 편이 캐시 관리와 확장 측면에서 더 적합하다.

멀티 인스턴스 환경에서 local cache를 적용하면 각 서버 인스턴스가 자기 메모리에 상품 데이터를 따로 저장한다.

```text
Server A local cache
Server B local cache
Server C local cache
```

이 상태에서 상품이 수정되면 Redis는 최신 값으로 refresh할 수 있지만, 각 서버의 local cache에는 짧은 시간 동안 이전 값이 남을 수 있다.

```text
상품 수정
-> DB 업데이트
-> Redis 최신 값 refresh
-> Server A local cache에는 이전 값 존재 가능
-> Server B local cache에는 이전 값 존재 가능
```

따라서 멀티 인스턴스에서 local cache를 사용하려면 다음 전략이 필요하다.

```text
1. local cache TTL을 짧게 둔다.
2. TTL에 jitter를 적용해 만료 시점을 분산한다.
3. 상품 변경 이벤트를 모든 인스턴스에 전파한다.
4. 이벤트를 받은 인스턴스는 자기 local cache에서 해당 상품을 제거한다.
```

운영 환경에서는 Redis Pub/Sub, Kafka, Redis Stream 같은 외부 이벤트 전파 수단을 사용해야 한다.

```text
상품 수정 API
-> DB 업데이트
-> outbox event 저장
-> commit

cache worker
-> DB 최신 상품 조회
-> Redis detail/version refresh
-> ProductChangedEvent 발행

각 API 인스턴스
-> ProductChangedEvent 수신
-> 자기 local cache에서 productId 제거
```


local cache를 추가할 경우 정합성 전략은 다음과 같다.

```text
Local cache 정합성
-> 짧은 TTL + jitter + 변경 이벤트 전파 + invalidate
```

## Cache Key

```text
product:detail:{productId}   상품 상세 응답 캐시
product:version:{productId}  현재 유효한 최신 상품 version
```

## 실행 API

```text
POST /products/seed
GET  /products/{productId}
PUT  /products/{productId}
```
