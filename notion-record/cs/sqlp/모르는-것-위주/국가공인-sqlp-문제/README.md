# 국가공인 SQLP 문제

# 1권

# 183p 47

## 근거

- 할인유형코드(20%) + 주문일시(100만건) → 20만건 = 인덱스 액세스 조건으로 사용하자
    - 상품 1개당 10개의 주문상품
- 상품이 2만개인데 모두 고르게 주문한다
    - 상품 코드로 GROUP BY 해도 2만개인데 조인 횟수 20만 → 2만개로 줄임 (1 / 10)

```sql
from (select /*+ index 할인유형코드 주문일시 no_merge */ 상품코드 상품명 상품가격
		  from 주문상품
			where 할인유형코드 =
			and   주문일시 >= ADD_MONTHS(SYSDATE, -1)
			GROUP BY 상품코드) O, 상품 P
WHERE P.상품코드 = O.상품코드
ORDER BY 총주문금액 desc, 상품코드
```

# 187p 51

## 근거

- 정렬을 생각해야 한다 (부분범위 처리)
- 주문상품 10만건 만족에 비파티션이므로 할인유형코드 + 주문일시 필요하다
- 등록일시로 desc → 만족하는 상품을 **`역순으로 스캔해서 멈출 수 있도록 join 조건 pushdown 해준다`**

# 188p 52

## **`근거(이건 다시 봐도 모르겠네..)`**

- 정렬을 생각해야 한다(부분범위 처리)
- 주문상품 만족 10만 건에 할인유형코드 만족 상품 5000개, 파티션테이블 100만 중 10만 → full

# 189p 53

## 근거

- 작업자ID ‘Z123456’
- 최근 한달간 방문 (실제 방문일자 ≥ trunc(add_months(sysdate, -1))
- 배타적 관계 파악해야함 그래프 보고

```sql
select *
from (
	select a.작업일련번호, a.실제방문일자, '개통' as 접수구분, b.고객번호, b.주소
	-- nvl2처리 하라
	from 작업지시 a, 개통접수 b, 장애접수 c
	where a.작업자ID = 'Z'
	and a.실제방문일자 >= trunc(add_months(sysdate, -1))
	and b.개통접수번호(+) = a.개통접수번호
	and c.장애접수번호(+) = a.개통접수번호
	order by a.실제방문일자 desc
)
where rownum <= 10
```

# 258p 33

## 근거

- 상품(1000건) → 주문(6만건) → 60000건의 nl
- 이후 상품분류 (상위분류코드 = ‘AK’)에 해당하는 건이 1개인데
- **`60000건 중 결과를 만족하는 집합이 3000건` (FILTER 방식으로 작용)**

## 쿼리 분석

- 주문일시 일주일 내에
- 주문된 상품에 대해서
- 상위분류코드가 ak인 놈

## 판단

- 상품분류 테이블을 먼저 읽었으면 좋겠다는 감은 있는데
    - 2가지 방법이 존재
        - unnest nl_sj
        - push_subq
    - 둘 중 뭐가 이득인지 정확히 결론을 못내리겠다
- push의 경우
    - leading(p t) use_nl(t)
    - /*+ no_unnset push_subq */
- nl_sj의 경우
    - leading(e@subq p t) use_nl(p) use_nl(t) nl_sj(e@subq)
    - qb_name(subq)
- **서브쿼리 조건이 인덱스를 잘 활용할 수 있다면** → `nl_sj`
- **서브쿼리 조건으로 메인 테이블을 효과적으로 줄일 수 있다면** → `push_subq`

# 2권

## 21p 10

### 근거

- OTBND 만족 50% → 10000건
- 상담결과코드 RS01 RV13 합해서 3%
- 10000건의 3% → 결과는 **`300건`**

### 문제 쿼리

- ROWNUM ≤ 100 = 100개만 뽑는다
- **`case 1`**
    - 접촉구분코드 + 상담일자 + 상담순번
        - 전체 건수에서 50%줄이고 → 상담일자로 줄이고까지 10000건
        - 상담순번은 그냥 SORT 제거용
        - 상담결과코드 필터링 → 10000개를 보지만 결과는 300건 (9700건 필터링, **`테이블 필터`**)
        
        <aside>
        💡
        
        핵심  
        
        **`선택도가 3%다 = 100번 액세스해야 3번을 얻는다 = 300개를 얻어야 하기에.`**
        
        </aside>
        
- case 2
    - 접촉구분코드 + 상담결과코드 + 상담일자
    - 상담결과코드가 IN절인 상태에서 액세스 조건으로 사용된다 → 2번 액세스한다
        - 브랜치를 2번 탐색하는 비효율은 있다
        - 그렇지만 300건을 빠르게 가져올 수 있다
        - 인덱스 구성으로 인해 ORDER BY 생략 불가
- case 3
    - 상담결과코드 + 접촉구분코드 + 상담일자
    - 상담결과코드가 IN절인 상태에서 액세스 조건으로 사용된다 → 2번 액세스한다
        - 그 다음에 접촉구분코드까찌 액세스
        - 여기까지만 해도 300건을 빠르게 가져온다
        - 인덱스 구성으로 인해 ORDER BY 생략 불가
- **`case 4`**
    - 접촉구분코드 + 상담일자 + 상덤결과코드
        - 접촉구분코드로 n개를 n/2개로 줄이고
        - 상담일자로 10000개까지 줄이고
        - 상담결과코드로 필터링한다 **`(인덱스 필터 조건)`**
        - 10000개 → 300건 필터링한다

## 24p 13

### 근거

- 상품 100개
- 주문 1억개
- 주문상품 2억개
- 한달 주문건수 100만 건
- 모든 상품 골고루 주문
- 위의 근거에 따라
    - 상품 1개가 100만 건의 주문을 발생시킨다
    - 1건의 주문이 2개의 주문상품을 발생시킨다

### 문제 쿼리

- 주문일자가 1년 이내인 주문상품들에 대해서
    - 1억 개 중에 1200만건
        - **FULL TABLE SCAN**
- 상품코드 PRD_CD를 만족하는 녀석을 가져와라.
    - 즉 상품 1개가 1년동안 얼마나 주문되었냐
- (병선)
    - 올 해동안 상품코드에 해당하는 주문을 모두 가져와라
- case 1
    - leading 주문
        - full table scan
        - 1억에서 8800만건의 필터링
        - outer join 횟수 1200만건
- case 2
    - **leading 주문상품이 맞는거 같다**
        - 상품코드의 NDV는 1/100
        - 2억건의 1/100이므로 200만건으로 줄인다
        - PK : 주문번호 + 상품번호
            - 조건절에 쓰인 것이 주문번호 상품코드인데
            - **`중복이 없다`**
        - (병선) join 횟수가 많아서 양측 어디로 하든 hash join 필요
            - DISTINCT 불필요 : 주문번호에 대해서 상품코드를 만족하는 녀석은 중복이 없다
- 답안
    - LEADING(P) use_hash(o) full(o) index_ffs(p 주문상품_PK)
    - 인덱스 (상품코드 + 주문번호)
    - 주문 테이블 주문번호 1
        - 주문상품 테이블 **`(1, a) (1, b)`** → 걸러지네
        - **`distinct 필요없다` → 왜 안지웠지?**

<aside>
💡

카페에 올림 distinct 이유

</aside>

## 27p 16

### 근거

- 작업목록 10만 건
- 작업실행로그
    - 하루 5000개
    - 총 500만 건
    - 작업번호 not null
    - 데이터 분포
        - 0.02 / 99.88(FAIL) / 0.1
- FAIL 입력했을 때 가장 빠르게 하고 싶다
- 작업목록_PK 작업번호
- 작업실행로그_PK 작업실행ID

### 문제 쿼리

- 작업상태코드 FAIL을 만족하고
- 작업시작일시가 오늘 시작하고 작업종료일시가 오늘 종료된 놈들에 대해서
- 작업실행ID 순으로 정렬해서
- 50개만 가져와라

- case 1
    - 조인 순서 변경 + (작업상태코드 + 작업실행ID idx)
    - 500만 * 0.1 = 5천건
        - 그러나 조인 순서를 변경하는 경우 무용지물
- case 2
    - 작업목록조인 인라인 뷰 바깥 처리 + (작업상태코드 + 작업실행ID idx)
    - 인라인 뷰 바깥에서 조인하려면 근거가 있어야 한다
        - (병선) Group by 같은 것이 있었다면 ok지만 없음
- case 3
    - (작업상태코드 + 작업시작일시 idx)
    - 이 경우 5개
    - best
    - (파악 못하고 있음)**`이렇게 했을 때 rownum ≤ 50은 소용이 있나`**
        - 애초에 효율성을 따지기 의미없을 정도.
- case 4
    - 의미 없음

## 31p 18

### 근거

- 작업목록 10만 건
- 작업실행로그
    - 하루 5000개
    - 총 500만 건
    - 작업번호 not null
    - 데이터 분포
        - 0.02 / 99.88 / 0.1
- 3개의 데이터 분포를 모두 골고루 입력하고 싶다
- 작업목록_PK 작업번호
- 작업실행로그_PK 작업실행ID

### 문제 쿼리

- 놓친 것
    - 0.01 / 0.2의 경우 **만족하는 데이터 자체가 너무 적어서 스캔량 자체가 많지 않다**
        - **`비효율은 존재하지만 비효율로 보기는 어렵다는 것`**
- case 1
    - 작업상태코드 + 작업실행 ID + 작업시작일시
    - **`작업실행ID는 인덱스 액세스 조건으로 별 의미가 없다`**
        - 근데 rownum ≤ 50이라 5000개 중 50개를 뽑는 경우이므로
        - **`SORT 생략 가능해야 유리하다`**
- case 2
    - 작업상태코드 + 작업시작일시
        - SORT 생략 안되지만 5000개로 딱 줄임

## 35p 20

### 근거

- 최근 1년 내에
- 상품번호 ZE367
- 변경구분코드 C2
- 최종 변경일시를 찾아라
- 상품번호 + 변경일시 + 변경구분코드

## 36p 22

### 근거

- 상품번호 + 변경일시

### 문제 쿼리

- 2021년 3월에 변경된 상품 중
- 3월의 최종 상품변경이력의 변경구분코드가 C2 = 최종 상품변경이력의 변경구분코드가 C2가 아닌놈은 안본다
- 3월의 맨끝에 해당하는 모든 상품을 봐야한다

```sql
select 상품번호, 변경일시
from (
	select 상품번호, 변경일시
	row_number() over (partition by 상품번호 order by 변경일시 desc) as rn
	from 상품변경이력
)
where rn <= 1
and 변경구분코드 = 'c2'
```

```sql

SELECT 상품번호, 변경일시
FROM (
  SELECT
    상품번호,
    변경일시,
    변경구분코드,
    MAX(변경일시) OVER (PARTITION BY 상품번호) AS max_변경일시
  FROM 상품변경이력
) v
WHERE v.변경일시 = v.max_변경일시   -- 상품별 전체에서 최신일시인 행만
  AND v.변경구분코드 = 'c2';        -- 그중 코드가 c2인 행만

```

## 37p 26번

- ‘A001’ 장비의 최종 상태 코드, 변경일자, 변경순번 출력
- 장비_PK : 장비번호
- 상태변경이력_PK : 장비번호 + 변경일자 + 변경순번
    - INDEX를 사용하면 최종 변경일자, 변경순번 뽑기가 쉬워보임

```sql
SELECT 
FROM 장비 A, 상태변경이력 B
WHERE A.장비구분코드 = 'A001'
AND B.장비번호 = A.장비번호
AND (B.변경일자, B.변경순번) 
		= (SELECT 변경일자, 변경순번
			 FROM (
				 SELECT 변경일자, 변경순번
				 FROM 상태변경이력
				 WHERE 장비번호 = A.장비번호
				 ORDER BY 변경일자 DESC, 변경순번 DESC
			 )
			 WHERE ROWNUM<=1
			);			
```

## 41p 35번문제

- 온라인 트랜잭션 없음
- 대량 INSERT
- PK index만 존재

```sql
1) DELETE -> TRUNCATE

ALTER TABLE TARGET_T NOLOGGING;

**ALTER TABLE TARGET_T MODIFY CONSTRAINT TARGET_T_PK DISABLE DROP INDEX;**

2) INSERT -> /*+ PARALLEL(T1 4) */

ALTER TABLE TARGET_T LOGGING;

**ALTER TABLE TARGET_T MODIFY CONSTRAINT TARGET_T_PK ENABLE NOVALIDATE;**
```

## 41p 36번문제

- C1 조건절 만족 90%이상
- MYTAB 테이블 PK → DT + ID
- **병렬 처리 활용 불가**

```sql
ALTER TABLE MYTAB_TEMP NOLOGGING;

INSERT INTO MYTAB_TEMP
SELECT C0, C1, C2, C3
, CASE WHEN C1 < TRUNC(SYSDATE) THEN C4+1 ELSE C4 END AS C4
FROM YOURTAB@RDS
WHERE C0 IS NOT NULL
AND C5>0;

--답안
1) PK constraint 제거 + NOLOGGING 추가 후 CTAS
CREATE TABLE MYTAB_TEMP
NOLOGGING
AS SELECT
...
CAS
;

2) ID 중복값 여부 확인
SELECT count(*) INTO V_CNT
FROM (SELECT
			FROM MYTAB_TEMP
			GROUP BY ID
			HAVING COUNT(*) > 1);

3) 이후 중복값이 있는지 없는지에 따라 분기(핵심)

IF V_CNT > 0 THEN
  -- 오류
ELSE
  -- 기존 동작
```

## 43P 37번문제

# 쿼리 분석

- 성인여부가 = ‘N’인 법정대리인 연락처를 세팅해라
    - 법정대리인_고객번호를 등록하지 않은 사람은 → C.법정대리인_연락처
    - 법정대리인_고객번호를 등록해둔 사람은 → 현재 법정대리인_고객번호에 해당되는 사람이 등록한 연락처로 update
- case 1 : 연락처가 업데이트되지 않은 사람들의 연락처 갱신
- case 2 : 연락처가 이미 갱신된 사람도 다시 갱신
    - 여기가 문제
- 무엇을 matched로 볼거고 무엇을 not matched로 볼거냐
    - (뇌피셜)case 2가 matched인데..
    - 얘는 할 필요가 없는 놈.
    - 얘는 merge문으로 쓴다면 아예 작성되지 말아야 할 놈
- case 1
    - join에 성공했을 때 → (이전 번호로 되어있는 사람)
    - UPDATE SET 법정대리인_연락처 = 고객.연락처
    - WHERE 법정대리인_고객번호 IS NOT NULL —> 어디에다 쓰느냐에 따라.
        - merge문의 join할 때 써버리면 문제가 생기나? 안생기나?

```sql
merge /*+ ORDERED USE_NL(C2) INDEX(C2 고객_PK) **INDEX(C1 고객_X3)***/
into 고객 C1 -- 강제
using 고객 C2 -- 강제
on (
	C2.고객번호 = C1.법정대리인_고객번호 and
	C1.법정대리인_고객번호 IS NOT NULL and -- 알아서 넣어줄 가능성이 있다
  C1.성인여부 = 'N'
	-- 고객_X3, 고객_PK
)
WHEN MATCHED THEN -- 여기에 C1 INDEX를 강제할 수 있나?
update -- 따로 인덱스를 쓰게 할 수 있나
	set C1.법정대리인_연락처 = C2.연락처
	-- WHERE C1.성인여부 = 'N' -- 여기서는 인덱스 사용 가능(고객_X2)
	WHERE C2.연락처 <> C1.법정대리인_연락처 -- 맞네..
;
```

```sql
--법정대리인_고객번호로 등록된 고객의 연락처 갱신이 필요한 상황에서 쓰는 쿼리
	-- 연락처가 NULL일때 UPDATE 쿼리
	-- 연락처가 NULL인 경우는 두가지가 있다.
	-- 1) 고객 C가 법정대리인을 등록하지 않은 경우(Join 실패)
	-- 2) 고객 C의 법정대리인인 고객이 연락처를 등록하지 않은 경우(Join 성공. SELECT 값이 null)
	-- 결과적으로 값이 있긴 해야 UPDATE가 수행이 된다.
	-- NULLABLE -> 특정 고객의 연락처 NULL인 경우가 있을 수 있음. 하지만 NVL함수에서 배제된다.
UPDATE 고객 C
SET 법정대리인 연락처 = 법정대리인_연락처
WHERE 성인여부 = 'N';

--> 연락처의 최신 여부를 파악할 수 없다. (병선) : **이전의 연락처일 수 있다**.
--> 조인에 성공했으나 연락처가 있을 수도 없을 수도 있다.
```

## 44P 38번 문제

- 상품재고 10만개
- 상품재고이력: 8천 8백만개
- 1시간 주기 배치
- 평균 업데이트 = 8만여건
- 문제점 - row lock이 많이 발생한다.

### 쿼리 분석

1. 업체코드=’Z’이며 상품재고가 가용재고량, 가상재고수량 모두 0(즉 품절)
2. 상품재고이력이 있어야 함(JOIN 성공해야한다)

### 튜닝 요소 분석

1. WHERE절에 상품재고이력 여부는 확인하지 않는다
2. 품절유지”일” → 1시간 주기로 수행하는 배치 프로그램이다. 오늘 업데이트된 내역은 다시 변경할 필요가 없다.
    1. `품절유지일 <> TRUNC(SYSDATE) - TO_DATE(MAX(A.변경일자))`
3. join 조건
    - A.상품번호 = B.상품번호
    - 변경일자가 뭐를 의미?? 재고의 변경??

`STOPKEY OR MERGE INTO`

# 132p 62번

## 근거

- 장비 100만 개

## 쿼리

- 상태변경이력 테이블과 조인해서
- 모든 장비의
    - 장비번호, 장비명, 장비구분코드, 최종상태코드, 최종변경일자, 최종변경순번

```sql
select a.정보, b.정보
from 장비 a,
(select 장비번호, 정보,
				row_number() over(partition by 장비번호 order by 변 desc 변 desc) as rn
				from 상태변경이력) b
where b.장비번호 = a.장비번호
and b.rn <= 1;

```

```sql

SELECT /*+ leading (a b) index(a tt1_idx) index(b tt1_idx) use_nl(b) */ *
FROM (
    SELECT *
    FROM (
        SELECT '1' AS tp, '2', '3', '4', a.*
        FROM tt1 a, tt2 b
        WHERE a.dt = 1
          AND b.cd = a.cd
        ORDER BY 1, 2, a.idn -- 이거 정렬 생략 된다.. 테스트겸 여러개로 해봤는데도 된다.
    )
    WHERE ROWNUM <= 10

    UNION ALL

    SELECT *
    FROM (
        SELECT '1' AS tp, '2', '3', '4', a.*
        FROM tt1 a, tt2 b
        WHERE a.dt = 1
          AND b.cd = a.cd
        ORDER BY 1, 2, a.idn
    )
    WHERE ROWNUM <= 10
) a,
tt1 b
WHERE b.idn = a.idn
  AND ROWNUM <= 10;
```

|   0 | SELECT STATEMENT                    |         |      1 |        |    11 (100)|     10 |00:00:00.01 |       8 |
|*  1 |  COUNT STOPKEY                      |         |      1 |        |            |     10 |00:00:00.01 |       8 |
|   2 |   NESTED LOOPS                      |         |      1 |     10 |    11   (0)|     10 |00:00:00.01 |       8 |
|   3 |    NESTED LOOPS                     |         |      1 |     10 |    11   (0)|     10 |00:00:00.01 |       7 |
|   4 |     VIEW                            |         |      1 |     10 |     2  (50)|     10 |00:00:00.01 |       4 |
|   5 |      UNION-ALL                      |         |      1 |        |            |     10 |00:00:00.01 |       4 |
|*  6 |       COUNT STOPKEY                 |         |      1 |        |            |     10 |00:00:00.01 |       4 |
|   7 |        VIEW                         |         |      1 |     33 |     1   (0)|     10 |00:00:00.01 |       4 |
|   8 |         NESTED LOOPS                |         |      1 |     33 |     1   (0)|     10 |00:00:00.01 |       4 |
|   9 |          TABLE ACCESS BY INDEX ROWID| TT1     |      1 |     10 |     1   (0)|      4 |00:00:00.01 |       2 |
|* 10 |           INDEX RANGE SCAN          | TT1_IDX |      1 |     10 |     1   (0)|      4 |00:00:00.01 |       1 |
|* 11 |          INDEX RANGE SCAN           | TT2_IDX |      4 |      3 |     0   (0)|     10 |00:00:00.01 |       2 |
|* 12 |       COUNT STOPKEY                 |         |      0 |        |            |      0 |00:00:00.01 |       0 |
|  13 |        VIEW                         |         |      0 |     33 |     1   (0)|      0 |00:00:00.01 |       0 |
|  14 |         NESTED LOOPS                |         |      0 |     33 |     1   (0)|      0 |00:00:00.01 |       0 |
|  15 |          TABLE ACCESS BY INDEX ROWID| TT1     |      0 |     10 |     1   (0)|      0 |00:00:00.01 |       0 |
|* 16 |           INDEX RANGE SCAN          | TT1_IDX |      0 |     10 |     1   (0)|      0 |00:00:00.01 |       0 |
|* 17 |          INDEX RANGE SCAN           | TT2_IDX |      0 |      3 |     0   (0)|      0 |00:00:00.01 |       0 |
|* 18 |     INDEX FULL SCAN                 | TT1_IDX |     10 |      1 |     1   (0)|     10 |00:00:00.01 |       3 |
|  19 |    TABLE ACCESS BY INDEX ROWID      | TT1     |     10 |      1 |     1   (0)|     10 |00:00:00.01 |       1 |
