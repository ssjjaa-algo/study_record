# 상품 상세 API 캐시 설계

## 시나리오

인기 상품의 조회 속도를 빠르게 하기 위해 상품 상세 API에 캐싱을 적용한다.

캐싱 적용 시에는 단순히 조회 결과를 저장하는 것뿐 아니라, 다음 요소들을 함께 고려해야 한다.

```text
- 반복 조회되는 인기 상품의 응답 속도 개선
- DB 조회 부하 감소
- 캐시 만료 및 무효화 전략
- 상품 수정 이후 DB와 캐시 간 정합성 보장
- 트래픽 폭증 상황에서의 캐시 계층 확장
```

## 요구사항

1. 상품 상세 API 응답 시간은 `P95 200ms 이하`여야 한다.
2. 상품 정보는 읽기 요청이 대부분이며, 수정 요청은 적게 발생한다. 예를 들어 읽기와 쓰기 비율은 `99:1`로 가정한다.
3. 상품 정보 수정 이후 캐시와 DB 간 정합성 문제를 해결해야 한다.
4. 전체 상품은 `10,000개`, 인기 상품은 `200개`로 가정한다.
5. 평상시 조회 트래픽은 `2,000RPS`, 이벤트/프로모션 피크 트래픽은 `30,000RPS`로 가정한다.

## 해결책

### 1. 상품 상세 API 응답 시간 P95 200ms 이하

- **Redis 기반 캐시를 도입한다.**

상품 상세 조회 시 DB를 먼저 조회하지 않고 Redis 캐시를 먼저 조회한다.

```text
1. Redis에서 상품 상세 조회
2. 캐시에 있으면 즉시 응답
3. 캐시에 없으면 DB 조회
4. DB 조회 결과를 Redis에 저장
5. 응답 반환
```

Redis는 메모리 기반 저장소이므로 DB보다 빠르게 응답할 수 있다. 반복 조회되는 상품 상세 데이터를 Redis에서 반환하면 DB 조회 비율이 줄어들고, API 응답 시간도 안정화된다.

### 2. 상품 수정 요청이 적게 발생한다

- **Cache-Aside 패턴을 사용하고, 수정 시 캐시를 무효화한다.**

읽기 요청이 대부분이고 쓰기 요청은 적기 때문에, 조회 요청 중심으로 캐시를 최적화한다.

조회 시에는 cache-aside 방식으로 캐시를 채운다.

```text
조회 요청
1. Redis 조회
2. cache miss면 DB 조회
3. DB 조회 결과를 Redis에 저장
```

상품 수정 시에는 DB를 먼저 업데이트하고, commit 이후 캐시를 삭제한다.

```text
수정 요청
1. DB 상품 정보 업데이트
2. DB commit
3. Redis 캐시 삭제
4. local cache 무효화 이벤트 발행
```

수정 요청이 적기 때문에 캐시 무효화 비용은 충분히 감당 가능하다.

### 3. 캐시와 DB 간 정합성 문제를 해결해야 한다

- **DB commit 이후 캐시 무효화 + version/updatedAt 기반 stale put 방지를 사용한다.**

상품 수정 후에는 Redis 캐시를 삭제한다. 이후 다음 조회 요청이 들어오면 DB에서 최신 데이터를 읽고 다시 캐시에 저장한다.

다만 cache-aside 구조에서는 이미 진행 중이던 조회 요청이 DB의 이전 값을 읽고, 수정 이후 늦게 캐시에 저장하는 문제가 생길 수 있다.

```text
1. 조회 요청 A가 cache miss
2. A가 DB에서 old version 조회
3. 수정 요청 B가 DB 업데이트 commit
4. B가 Redis 캐시 삭제
5. A가 old version 데이터를 Redis에 저장
```

이를 방지하기 위해 상품 데이터에 `version` 또는 `updatedAt`을 포함한다.

```text
product:detail:{productId}
{
  productId: 1,
  name: "상품명",
  price: 10000,
  version: 10,
  updatedAt: "..."
}
```

캐시에 저장할 때 현재 유효한 version보다 오래된 데이터는 저장하지 않는다.

```text
저장하려는 데이터 version < 현재 상품 version
=> 캐시에 저장하지 않음
```

이를 통해 오래된 데이터가 캐시에 다시 들어가는 것을 막는다.

### 4. 전체 상품 10,000개 중 인기 상품 200개

- **인기 상품은 사전 캐싱하고, 일반 상품은 조회 시 캐싱한다.**

인기 상품 200개는 전체 조회 요청의 대부분을 차지한다고 가정하므로, 이 상품들은 미리 Redis에 적재한다.

```text
1. 최근 조회 로그를 기준으로 인기 상품 집계
2. 상위 200개 상품 선정
3. Redis에 상품 상세 데이터 warm-up
```

일반 상품은 모든 데이터를 미리 캐싱하지 않고, 조회가 발생했을 때 cache-aside 방식으로 캐시에 저장한다.

```text
- 인기 상품: 사전 캐싱
- 일반 상품: 조회 시 캐싱
- 일정 시간 조회되지 않은 상품: TTL 만료로 제거
```

### 5. 평상시 2,000RPS, 피크 30,000RPS 처리

- **Redis Cluster와 local cache를 사용해 캐시 계층을 확장한다.**

피크 트래픽을 모두 DB로 보내면 DB 부하가 급격히 증가하므로, Redis Cluster를 사용해 캐시 요청을 여러 노드로 분산한다.

```text
product:detail:{productId}
```

상품 ID 기반 key를 사용해 Redis Cluster에 분산 저장한다.

다만 Redis Cluster만으로는 특정 인기 상품 하나에 요청이 몰리는 hot key 문제를 완전히 해결할 수 없다. 따라서 애플리케이션 local cache를 짧게 둔다.

```text
Local Cache TTL: 1~5초
Redis TTL: 5~30분 + random jitter
```

local cache는 초인기 상품 요청이 Redis까지 매번 도달하지 않도록 막아준다. TTL은 짧게 두어 정합성 영향을 최소화한다.

또한 TTL에 랜덤 값을 추가해 여러 인기 상품 캐시가 동시에 만료되는 것을 방지한다.

```text
TTL = 기본 10분 + 랜덤 0~3분
```

이를 통해 cache stampede와 Redis hot key 부하를 줄일 수 있다.

## 최종 구조

```text
Client
  -> Product Detail API
      -> Local Cache
          -> Redis Cluster
              -> DB
```

조회 흐름:

```text
1. Local Cache 조회
2. 없으면 Redis 조회
3. 없으면 DB 조회
4. version 확인 후 Redis 저장
5. Local Cache 저장
6. 응답 반환
```

수정 흐름:

```text
1. 상품 정보 DB 업데이트
2. version 또는 updatedAt 갱신
3. DB commit
4. Redis 캐시 삭제
5. local cache 무효화 이벤트 발행
```

이 설계는 인기 상품 조회 요청을 캐시에서 처리하여 `P95 200ms 이하`를 달성하고, 이벤트 기간의 폭증 트래픽은 Redis Cluster와 local cache로 흡수한다. 또한 상품 수정 이후에는 캐시 무효화와 version 기반 검증을 통해 오래된 데이터가 다시 캐시에 저장되는 문제를 방지한다.
