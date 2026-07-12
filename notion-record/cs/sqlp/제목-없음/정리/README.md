# 정리

## 4 - 43

- 조인 조건 Pushdown은 **NL 조인을 전제**로 한다
- 조인 조건 Pushdown은 말 그대로 조인 조건절을 뷰 쿼리 블록 안으로 밀어 넣는 것으로서,

NL 조인 수행 중에 드라이빙 테이블에서 읽은 조인 컬럼 값을 Inner 쪽 뷰 쿼리 블록내에서

참조할 수 있도록 하는 기능이다.

- 지금까지 보았던 조인문에서 조건절 Pushdown은 상수 조건이 조인 조건을 타고 전이된 것을

Pushing하는 기능이었던 반면, 조인 조건 Pushdown은 조인을 수행하는 중에

드라이빙 집합에서 얻은 값을 뷰 쿼리 블록 안에 실시간으로 Pushing하는 기능이다.

예시 ) 조인 조건은 deptno 이다. 그런데 다른 조건은 loc= 'CHICAGO'라고 한다면

loc가  'CHICAGO'인 deptno 값들이 들어가는 것이다.

## 4 - 50

- order by가 인라인 뷰 안쪽에 기술되어 있고 rownum이 밖에 있는 쿼리는 뷰 머징 x
- 따라서 no_merge 힌트는 불필요하다
- 다만 정렬된 결과를 유지하기 위해 인라인 뷰 안에 있는 **order by 문을 바깥에도 적어줘야 한다**

- 이 것은 **no_nlj_batching** 힌트로 제어할 수도 있음

## 4 - 53

- 작업자 ID가 Z123456인 사람이
- 최근 한달간 방문해서 처리한 작업지시 중
- 실제방문일자 역순으로 최근 10건만 출력하라
- 최근 10건만 출력하는 최적의 인덱스 구성 -> 부분범위 처리를 이용하라는 것
- 부분범위 처리를 위해 NL조인 방식을 유도할 필요가 있음
- 개통접수번호와 장애접수번호 양쪽 모두에 값을 입력할 수는 없기에 NVL2 함수를 이용할 수 있다

```sql
select */*+ ordered use_nl(b) use_nl(c) */from
 ( select a.작업일련번호, a.실제방문일자,
          nvl2(b.개통접수번호, '개통', '장애') as 접수구분,
          nvl2(b.개통접수번호, b.고객번호, c.고객번호) as 고객번호,
          nvl2(b.개통접수번호, b.주소, c.주소) as 주소
   FROM 작업지시 a, 개통접수 b, 장애접수 c
   WHERE a.작업자ID = 'Z123456'
   and a.실제방문일자 >= trunc(add_months(sysdate, -1))
   and b.개통접수번호(+) = a.개통접수번호
   and c.개통접수번호(+) = a.개통접수번호
   order by a.실제방문일자 desc
 )
where rownum <= 100;

# 인덱스 : (작업자ID + 실제방문일자)
```

## 4 - 55

- 작업구분코드가 'A'일 때만 개통접수 테이블과 조인을 시도. 작업구분코드가 'A'가 아닐 때는 DECODE 함수가 NULL을 반환하므로 조인을 시도하지 않는다
- **decode(작업구분, 'A', '개통', 'B', '장애')**

- 작업구분이 'A'라면 '개통'을 출력하고, 'B'라면 '장애'를 출력하라

```sql
select a.작업일련번호, a.작업자ID,
       decode(a.작업구분코드, 'A', '개통', 'B', '장애') AS 작업구분,
       decode(a.작업구분코드, 'A', b.고객번호, 'B', c.고객번호) AS 고객번호,
       decode(a.작업구분코드, 'A', b.주소, 'B', c.주소) AS 주소
FROM 작업지시 a, 개통접수 b, 장애접수 c
where a.방문예정일자 = to_char(sysdate, 'YYYYMMDD')
and b.개통접수번호(+) = decode(a.작업구분코드, 'A', a.접수번호)
and c.장애접수번호(+) = decode(a.작업구분코드, 'B', a.접수번호)
```

# SQLP 2권

## 7 - 14

- 오라클에서 TX Lock은 **`트랜잭션 당 1개씩`**
- TM Lock은 **`DML을 수행하는 테이블 별`**로 하나씩 설정

## 7- 17

- Dirty Read
    - 다른 트랜잭션이 변경 중인 데이터를 읽었는데 그 트랜잭션이 롤백함으로써 비일관적인 상태가 된 것
- Non-Repeatable-Read
    - 한 번 읽은 상태에서 다른 트랜잭션이 그 데이터를 수정 또는 삭제하여 다른 데이터를 읽게 되는 현상
- Phantom Read
    - 한 번 읽은 상태에서 다른 트랜잭션이 Insert하여 다시 읽었을 때 다른 것도 보이는 것

## 7 - 23

- **`SELECT`** 문에서는 Oracle은 어떤 Lock도 사용하지 않는다

## 7 - 24

- 오라클 트랜잭션 격리수준 문법

```sql
SET TRANSACTION ISOLATION LEVEL READ COMMITTED -- 지정한 레벨
```

## 7 - 28

- SQL 서버는 update 하는 순간에 읽은 값을 읽으므로 TX2 실패
    - TX1이 끝날 때 까지 기다린다 → 3100인데 여기서 실패
- Oraacle은 update문이  시작되는 시점을 기준으로 읽음
    - TX2에서 sal 값이 변경된 사실을 안 TX2는 다시 읽고보니 조건을 만족하지 않아 실패

## 7 - 32

- WAIT, NOWAIT는 select for update에만 사용 가능

## 7 - 36

- select for update 사용 시 양쪽 조인 테이블에 모두 lock 걸림
- 방지하려면 for update of 주문수량 wait 10 이런 식으로 사용

# 실전모의고사

## 1 - 9

- **`index에 없는 컬럼을 읽는 경우`** index fast full scan은 불가하다

## 1 - 17

- 해시 조인에서는 **`하나만 (=)`** 조건이어도 해시 조인 가능

## 1 - 24

- 테이블을 해시 파티셔닝한다고 INSERT가 빨라지진 않는다. 오히려 더 느려질 수 있다

## 1 - 25

- Exclusive 모드 TM Lock와 RX 모드 TM Lock을 헷갈리지 말 것

## 1 - 27

- 버퍼캐시 히트율 = 1 - (disk / (query + current))

# 참고

https://www.youtube.com/watch?v=Ltb28QbVAHg

- **`가장 상단에서 만나는 자식이 없는 오퍼레이션`**이 먼저 수행된다
