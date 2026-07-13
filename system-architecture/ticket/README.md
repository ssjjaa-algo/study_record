# Coupon 발급 시나리오

## 상황

선착순 쿠폰 발급 이벤트를 가정한다.

- 쿠폰 재고는 정해져 있다.
- 사용자는 쿠폰을 1개만 발급받을 수 있다.
- 이벤트 시작 직후 많은 사용자가 동시에 발급 API를 호출한다.
- API는 Redis에서 먼저 발급 가능 여부를 판단한다.
- Redis에서 성공한 요청만 Redis Stream에 기록된다.
- Consumer는 Redis Stream을 읽어 DB에 최종 발급 내역을 저장한다.

구조는 다음과 같다.

```text
ticket/
  api/        // 발급 요청 API, Redis Lua 실행
  consumer/   // Redis Stream 소비, DB 반영
```

## Redis 선택 이유

동시에 많은 요청이 들어올 때 DB에서 직접 재고를 확인하고 차감하면 병목이 커진다.

현재 구현은 Redis Lua Script로 발급 판정을 원자적으로 처리한다.

```text
중복 사용자 확인
품절 여부 확인
재고 확인
재고 차감
발급 사용자 기록
Redis Stream 기록
```

이 작업은 하나의 Lua Script 안에서 실행된다.

```text
SISMEMBER
EXISTS sold-out flag
GET stock
DECR stock
SADD issued-users
XADD issue stream
```

따라서 같은 시점에 여러 요청이 들어와도 Redis 기준으로 재고보다 많은 사용자가 성공할 수 없다.

Redis Stream은 Redis에서 성공한 발급 요청을 Consumer에게 전달하기 위해 사용한다.

현재 로직에서는 재고 차감과 Stream 기록이 같은 Lua Script 안에서 실행된다.

```text
DECR stock
SADD issued-users
XADD ticket:issue:stream
```

따라서 Redis에서 성공한 요청만 Stream에 남는다.

```text
SUCCESS    -> Stream 기록
DUPLICATED -> Stream 기록 안 함
SOLD_OUT   -> Stream 기록 안 함
NOT_READY  -> Stream 기록 안 함
```

Consumer는 Redis Stream을 consumer group으로 읽고 DB에 반영한다.

```text
XREADGROUP
DB 저장
XACK
```

처리 중 장애가 나서 ACK 하지 못한 메시지는 pending 상태에 남고, 이후 다시 회수해서 처리한다.

## Redis Stream 선택 이유

- Redis에서 발급 성공 처리와 Stream 기록을 같은 Lua Script 실행 안에서 처리한다.
- 성공한 요청만 비동기 DB 저장 대상으로 넘길 수 있다.
- Consumer group으로 여러 Consumer가 나누어 처리할 수 있다.
- ACK 되지 않은 메시지를 pending 상태로 추적할 수 있다.

## 성공 시나리오

1. 사용자가 쿠폰 발급 API를 호출한다.

```http
POST /api/ticket-events/{eventId}/issues
```

2. Redis Lua Script를 실행한다.

```text
issued-users에 userId가 없음
sold-out flag가 없음
stock이 존재함
stock > 0
```

3. 조건을 만족하면 Redis에서 다음 작업을 수행한다.

```text
DECR stock
SADD issued-users userId
XADD ticket:issue:stream eventId userId
return SUCCESS
```

4. 사용자에게 응답한다.

```text
HTTP 202
status = SUCCESS
```

5. `consumer`는 Redis Stream을 읽는다.

```text
pending 메시지 회수
새 Stream 메시지 읽기
```

6. `consumer`는 DB에 발급 내역을 저장한다.

```text
ticket_issue insert
ticket_event.remaining_quantity 조건부 차감
```

7. DB 반영이 끝나면 Redis Stream 메시지를 ACK 한다.

```text
DB 저장 성공
-> XACK
```

## 실패 시나리오

### 중복 요청

이미 발급에 성공한 사용자가 다시 요청한 경우

```text
SISMEMBER issued-users userId == 1
-> DUPLICATED
```

응답:

```text
HTTP 409
status = DUPLICATED
```

이 경우에는 재고를 차감하지 않고 Redis Stream에도 기록하지 않는다.

### 품절

재고가 없거나 sold-out flag가 있는 경우다.

```text
sold-out flag 존재
또는 stock <= 0
-> SOLD_OUT
```

응답:

```text
HTTP 410
status = SOLD_OUT
```

이 경우에도 Redis Stream에 기록하지 않고 DB까지 가지 않는다.

### DB 반영 실패

Redis에서는 성공했지만 Consumer가 DB에 저장하는 과정에서 실패할 수 있다.
일시적인 DB 오류라면 ACK 하지 않는다.

```text
DB timeout
-> ACK 안 함
-> pending 상태 유지
-> 이후 재처리
```

반복 실패하거나 DB가 거절한 메시지는 failure stream으로 이동한다.

```text
ticket:issue:stream:failed
```

이후 원본 메시지는 ACK 한다.
