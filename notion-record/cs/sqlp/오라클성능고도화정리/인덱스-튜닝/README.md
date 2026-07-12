# 인덱스 튜닝

## 14

- IN 조건이 액세스 조건일 때는 Index Skip Scan 불가

## 15

- Index Sparse는 인덱스 전반에 거쳐 블록 밀도가 떨어지는 현상

## 17

- 인덱스에도 함수 적용 가능

```sql
CREATE INDEX 고객_X01 ON 고객( REPLACE(전화번호, '-', '') );
```

## 18

- **Reverse Key 인덱스**
- 데이터의 저장 방식을 변환하여 인덱스 키를 역순으로 저장하는 방법

## 22

```sql
# VAL가 없으면 '대한'으로 인덱스를 탄다
WHERE 업체명 = NVL(:VAL, '대한')

# 아래는 옵션 조건, 따라서 OR Expansion 여부에 따라 인덱스 Range Scan 가능 여부가 결정됨
WHERE 업체명 = NVL(:VAL, 업체명)
```

## 23

- 단일 컬럼 인덱스의 경우에서 컬럼이 NULL 허용인 경우, NULL로 입력된 컬럼은 인덱스에 저장되지 않음
- 결합 인덱스일 때도 모두 NULL 허용 컬럼인 경우 결과집합 누락
- Range Scan은 단 하나라도 인덱스 컬럼이 NULL이 아니면 조회가 가능하다.

## 25

- 인덱스 선두 컬럼이 OR 조건이 걸려있는 경우 인덱스 Range Scan 불가
- UNION ALL 분기가 되는 경우는 따져봐야 함

## 29

```sql
UPDATE 월별계좌상태 SET 상태구분코드 ='07'
WHERE 상태구분코드 <> '01'
AND 기준년월 = :BASE_DT
AND (계좌번호, 계좌일련번호) IN
 (SELECT 계좌번호, 계좌일련번호
  FROM 계좌원장
  WHERE 개설일자 LIKE :STD_YM || '%')
```

## 30

```sql
/*+ USE_CONCAT */
(지수구분코드, 지수업종코드) IN ((1, 001), (2,003))
```

## 31

```sql
NVL(MAX(주문번호) + 1, 1)

# INDEX (RANGE SCAN (MIN/MAX)) OF '주문_PK'
# FIRST ROW
```

## 34

- 테이블 스캔 비용은 선형적 증가 (인덱스가 기하급수적 증가)

## 37

- 인덱스를 재생성하는 것은 스캔 비율은 좋아지나 테이블 액세스에는 영향이 없다
- **이거 애매하다.**

## 41

- 인덱스를 스캔한 양에 비해 얻은 결과 건수가 적다면 비효율적이다.
- 테이블을 액세스한 양에 비해 얻은 결과 건수가 적으면 비효율적이다.

```sql
# 8459개의 블록을 읽어 10개의 결과를 반환
Rows    Row Source Operation
------  ------------------------------------------------------------10  TABLE ACCESS FULL 상품 (cr=8459 pr=759 pw=0 time=11258 us)

# 645개의 블록을 읽고 테이블 액세스는 100000번하였음
# 이후 (39248 - 645)개의 블록을 읽고 100개의 결과를 반환
Rows    Row Source Operation
------  ------------------------------------------------------------100  TABLE ACCESS BY INDEX ROWID 주문 (cr=39248 pr=48 pw=0 time=2872 us)
100000    INDEX RANGE SCAN 주문_IDX (cr=645 pr=42 pw=0 time=278386 us)

# 3647개의 블록을 읽고 테이블 액세스는 10번
# 이후 (3657 - 3647)개의 블록을 읽어 10개의 결과를 반환
Rows    Row Source Operation
------  ------------------------------------------------------------10  TABLE ACCESS BY INDEX ROWID 고객 (cr=3657 pr=698 pw=0 time=3249 us)
    10    INDEX RANGE SCAN 고객_IDX (cr=3647 pr=698 pw=0 time=3248us)

# 428개의 블록을 읽고 100000번 테이블 액세스
# 이후 (34522 - 428)개의 블록을 읽고 100000개 반환
Rows    Row Source Operation
------  ------------------------------------------------------------100000  TABLE ACCESS BY INDEX ROWID 배송 (cr=34522 pr=2159 pw=0 time=1378753 us)
100000    INDEX RANGE SCAN 배송_IDX (cr=428 pr=38 pw=0 time=278429 us)
```

## 43

- 선두 컬럼의 NDV가 낮고 후행 컬럼의 NDV가 높은 경우 IN 조건을 푸는 것이 효율적일 수 있다.

## 46

- 주문당 평균 20개 상품을 구매한다
- 한 고객이 주문을 1번 하면, 주문 상세는 20개가 생성된다는 것
- 따라서 상품 ID를 필터링하는 절차는 별로 오래 걸리지 않을 것이다.

## 47

- 인덱스 : 증서번호 + 투입인출구분코드 + 이체사유발생일자 + 거래코드

```sql
SELECT NVL( (G기본이체금액+G정산이자) - (S기본이체금액+S정산이자), 0)
FROM
  ( SELECT NVL(SUM(CASE WHEN 투입인출구분코드='G' THEN 기본이체금액 END), 0) G기본이체금액,
    SELECT NVL(SUM(CASE WHEN 투입인출구분코드='G' THEN 정산이자 END), 0) G정산이자,
    SELECT NVL(SUM(CASE WHEN 투입인출구분코드='S' THEN 기본이체금액 END), 0) S기본이체금액,
    SELECT NVL(SUM(CASE WHEN 투입인출구분코드='S' THEN 정산이자 END), 0) S기본이체금액
    FROM 거래
    WHERE 증서번호 = :증서번호
    AND 이체사유발생일자 <= :일자
    AND 거래코드 NOT IN ('7411', '7412', '7503', '7504)
    AND 투입인출구분코드 IN ('G', 'S')
  )
```

## 49

- 하나의 상품이 평균 100개
- 상품 코드는 짧다 (5자)
- 상품코드는 A16, K03, Z386으로 시작한다.

```sql
SELECT/*+ use_concat */ SUM(주문수량), SUM(주문금액)
FROM 주문상품
WHERE 상품코드 LIKE 'A16%'
OR 상품코드 LIKE 'K03%'
OR 상품코드 LIKE 'Z386'
and 주문일자 = to_char(sysdate, 'yyyymmdd')
group by 상품코드

(주문일자 + 상품코드)
```

## 52

- 1. OR 방식을 사용하면 고객ID가 인덱스 선두 컬럼이어도 Range Scan할 수 없다.
- 2. WHERE 고객ID LIKE #CUST_ID# || '%' -> CUST_ID에 NULL이 들어가면 인덱스에서 모든 거래 데이터 스캔
- 4. **옵션조건에 CASE문을 쓰면 UNION ALL 형태로 변환이 일어나지 않음**

## 63

**(선분 이력 조회 패턴, 아직 제대로 이해하지는 못했음)**

- 오래된 과거 이력을 조회할 때는 시작일자가 인덱스 선두컬럼,
- 최근 이력을 조회할 때는 종료일자가 선두컬럼.

## 64

- 인덱스 선두컬럼에 LIKE 연산자 -> 값을 입력하지 않았을 때 전체 스캔
- 전체 스캔이 **NULL 허용 컬럼에 NULL을 입력하는 것하고는 다르다**

## 67

- **NOT NULL 일 때는 nvl 이용할 수 있지만 NULL 허용일 때는 직접 분기 필요**
- IS NOT NULL의 조건문에는 해당 컬럼에 대한 조건문 써주는 것도 잊지 말 것

## 68

- 서비스 이용 테이블의 데이터가 하루에 10만건 씩 누적된다는 것이 힌트
- 주민등록 / 서비스일자에 따라 Driving / Driven을 무엇으로 할 것인가에 대한 판단
