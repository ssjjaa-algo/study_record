# 친절한 SQL 튜닝

텍스트: 헷갈린 것 정리

https://m.blog.naver.com/comekej/222092197726

# 배치 I/O

- 읽는 블록마다 건건이 I/O Call을 발생시키는 비효율을 줄이기 위해
- 테이블 블록에 대한 디스크 I/O Call을 미뤘다가 **읽을 블록이 일정량 쌓이면 한꺼번에 처리**
- **`데이터 정렬 순서가 매번 다를 수 있음`**
- 힌트 : batch_table_access_by_rowid
    - 억제 : no_nlj_batching(테이블)
        - 또는 맨 바깥쪽에 ORDER BY 연산 추가
- 인덱스로 소트 연산을 생략할 수 없거나 SQL에 ORDER BY가 없는 경우 좋은 기능이 될 수 있다
    - 반대로 말해 옵티마이저가 배치 I/O를 선택하는 경우 결과집합의 정렬을 유지하지 못한다는 것을 염두에 두어야 한다.

# NUM_INDEX_KEYS

- num_index_keys(a 고객별가입상품_X1 **`1`**)
    - 인덱스 첫 번째 컬럼까지만 액세스 조건으로 사용하라

# LIKE/BETWEEN 주의

- 인덱스 선두 컬럼에 대한 옵션 조건이 LIKE/BETWEEN
- NULL 허용 컬럼에 대한 옵션 조건을 LIKE/BETWEEN 연산자로 처리
    - 결과 집합에 오류가 생긴다.
    
    ```sql
    select * from 거래
    where 고객ID like '%'
    and 거래일자 between :dt1 and :dt2
    ```
    
    - 실제 NULL 값이 입력돼 있다면 그 데이터는 결과집합에서 누락

# UNION ALL 활용

```sql
select * from 거래
where :cust_id is null
union all
select * from 거래
where :cust_id is not null
```

- 변수의 입력값에 따라 위 / 아래 브랜치 분기
- 유일한 단점은 SQL 코딩량 느는 것 뿐

# NVL/DECODE

```sql
select * from 거래
where 고객ID = nvl(:cust_id, 고객ID)
-- where 고객ID = decode(:cst_id, null, 고객ID, :cust_id)
and 거래일자 between :dt1 and :dt2
```

- NULL 허용 컬럼에는 사용할 수 없다.
- 옵션 조건 처리용 NVL/DECODE를 여러 개 사용하면 그 중 **`변별력이 가장 좋은 컬럼 기준으로 딱 한 번만 OR Expansion이 일어난다.`**
    - 모든 옵션 조건을 이 방식으로 처리할 수 없는 이유임

# NL 조인 확장 메커니즘

- 테이블 Prefetch, 배치 I/O기능
    - 인덱스를 이용해 테이블을 액세스하다 디스크 I/O가 필요해지면 곧 읽게 될 블록까지 미리 읽어서 버퍼캐시에 적재
- 책에 따르면
    - TABLE ACCESS BY INDEX ROWID OF 테이블 실행계획이 위에 뜨면 테이블 Prefetch
    - 아래에 뜨면 배치I/O

# 해시 테이블에 담기는 정보

- **해시 테이블에 조인 키값만 저장하지 않는다.**
    - 만약 그렇다면 조인에 성공한 키에 대한 **`나머지 정보를 다시 ROWID로 테이블 블록을 액세스해야 함`**

# 해시 조인 실행계획 제어

- use_hash 힌트만 사용하면 Build Input을 옵티마이저가 선택
    - 일반적으로 둘 중 **카디널리티가 작은 테이블을 선택**한다.

```sql
select /*+ leading(T1,T2,T3) swap_join_inputs(T2) */

HASH JOIN
├── HASH JOIN
│   ├── T2 (Build Input)
│   └── T1 (Probe Input)
└── T3 (Probe Input)

select /*+ leading(T1, T2, T3) swap_join_inputs(T2) swap_join_input(T3) */
HASH JOIN
├── T3 (Build Input)
└── HASH JOIN
    ├── T2 (Build Input)
    └── T1 (Probe Input)
```

- 패턴 2를 패턴 1로 바꾸려면 T3를 no_swap_join_inputs(T3)로 설정

# 스칼라 서브쿼리

- 메인 쿼리 집합이 매우 작은 경우 도움 x
    - 스칼라 서브쿼리 캐싱은 **`쿼리 단위`**로 이루어진다 **`(재사용성)`**
    - 따라서 메인 쿼리 집합이 클수록 **`재사용성이 높아`** 효과가 좋다.

### 스칼라 서브쿼리 Unnesting

- 스칼라 서브쿼리 또한 NL 방식으로 조인하기에 캐싱 효과가 크지 않으면 랜덤 I/O 부하 존재
- 따라서 다른 조인 방식을 선택하기 위한 Unnesting이 가능하다고 말하는 것 같음.

# Union vs Union All

- Union → 중복 제거 : **`소트 작업을 수행`**한다는 것
- 따라서 Union을 쓸지, Union All을 쓸지에 대한 판단이 정확히 필요
    - 데이터 모델링을 어떻게 했냐에 따른 정확한 이해

# TOP N 쿼리

```sql
select * from
	(select 거래일시, 체견구룻
	from 종목거래
	where 종목코드 = 'a'
	and 거래일시 >= '20000101'
	order by 거래일시
)
where rownum <= 10
```

- 기본적으로 소트 연산 생략 불가(전체범위처리)
- 종목코드 + 거래일시의 경우만 생략 가능
    - 부분범위처리 가능하다는 것
    - 실행계획에 Sort Order By가 나타나지 않고 **`COUNT(STOPKEY)`**

### 부분범위 처리 가능하도록 SQL을 작성한다는 것

- 인덱스 사용 가능하도록
- 조인은 NL조인 위주의 처리
- Order By 절이 있어도 소트 연산을 생략할 수 있도록 인덱스를 구성한다.

# 분석함수에서의 Top N 소트

- 윈도우 함수 중 rank나  row_number 함수는 max 함수보다 소트 부하가 적다. Top N 소트 알고리즘이 작동하기 때문.

# 서브쿼리와 조인

### 필터 오퍼레이션

- 서브쿼리를 의도적으로 필터 방식으로 처리한다 = no_unnest
- 기본적으로 NL 조인과 처리 루틴이 같으며
    - 실행계획 상 FILTER = NESTED LOOPS로 치환하고 해석해도 된다.
    - NL과 차이
        - 메인쿼리의 한 로우가 서브쿼리의 한 로우와 조인에 성공하는 순간 진행을 멈추고 메인 쿼리의 다음 로우를 계속 처리한다.
        - 캐싱 기능을 갖는다.
            - 캐싱은 쿼리 단위이며 쿼리를 시작할 때 PGA에 공간 할당, 쿼리를 마치며 공간 반환
        - **`항상 메인쿼리가 드라이빙 집합 (필터 서브쿼리는 메인쿼리에 종속되기 때문에)`**

### 서브쿼리 Unnesting → NL 세미조인과 필터의 차이

- 서브쿼리를 Unnesting하면 **다양한 방식으로 조인**이 실행될 수 있다
- Unnesting된 서브쿼리는 **`메인 쿼리 집합보다 먼저 실행될 수 있다.`**

```sql
# Unnesting된 서브쿼리가 드라이빙되도록 leading 힌트를 사용했다.
# 서브쿼리를 그대로 풀어서 조인하면 메인쿼리 결과집합(고객)이 서브쿼리 M쪽 집합(거래)
# 수준으로 확장될 수 있다. 따라서 실행계획에 SORT UNIQUE 오퍼레이션이 나타남
# 친절한 SQL 튜닝 305

select /*+ leading(거래@subq) use_nl(c) */ c.고객번호, c.고객명
from 고객 c
where c.가입일시 >= trunc(add_months(sysdate, -1), 'mm')
and exists (
				select /*+ qb_name(subq) unnest */ 'x'
				from 거래
				where 고객번호 = c.고객번호
				and 거래일시시 >= trunc(sysdate, 'mm') )
```

# ROWNUM (친절한 SQL튜닝 307)

- Exists 서브쿼리와 rownum 조건의 의미 중복

```sql
select *
from 게시판 b
where 게시판구분 = '공지'
and exists (select 'x'
						from 수신대상자
						where 글번호 = b.글번호
						and 수신자 = :memb_no
						and rownum <= 1)
```

- **`서브쿼리에 rownum을 쓰면 Unnesting하지 못함`**
    - 옵티마이저에게 이 블록을 건드리지 말라고 명령하는 것

# Exists 활용

- Distinct 연산을 활용하면 조건에 해당하는 데이터를 모두 읽어서 중복을 제거해야한다.
    - 부분 범위 처리 x, I/O 다수 발생
- Exists로 변경해보자

```sql
select DISTINCT p.*
from 상품 p, 계약 c
where p.상품유형코드 = :pclsd
and c.상품번호 = p.상품번호
and c.계약일자 between :dt1 and :dt2
and c.계약구분코드 = :ctpcd

# ----------------------------

select p.*
from 상품 p, 계약 c
where p.상품유형코드 = :pclsd
and exists ( select 'x' from 계약 c
						 where c.상품번호 = p.상품번호
						 and c.계약일자 between :dt1 and :dt2
						 and c.계약구분코드 = :ctpcd)
						 
# 상품유형코드에 해당하는 상품에 대해
# 계약일자 조건기간에 발생한 계약 중 계약 구분코드를 조건절을 만족하는 데이터가 
# 한 건이라도 존재하는지 확인 
# DISTINCT가 아니어서 부분범위처리도 가능
```

# 조인 방식

- 해시 조인이기 때문에 Sort 연산을 생략하지 못하는 경우
    - use_nl 힌트를 통한 명확한 제어 (필요한 상황에)
- 정렬 기준이 조인 키 컬럼이면 소트 머지 조인도 Sort Order By 연산 생략 가능

# **수정 가능 조인 뷰(도대체 이건 뭔가.)**

# MERGE 문 활용

- 고객(customer) 테이블에 발생한 변경분 데이터를 DW에 반영하는 프로세스
    - 전일 발생한 변경 데이터를 기간계 시스템으로부터 추출(Extraction)
    
    ```sql
    create table customer_delta
    as
    select * from customer
    where mod_dt >= trunc(sysdate) -1
    and   mod_dt < trunc(sysdate);
    ```
    
    - customer_delta 테이블을 dw 시스템으로 전송(Transportation)
    - DW 시스템으로 적재(Loading)
    
    ```sql
    merge into customer t using customer_delta s on (t.cust_id = s.cust_id)
    where matched then update
      set t.cust_nm = s.cust_nm, t.email = s.email, ...
      # where 조건문이 들어올 수도 있다.
    when not matched then insert
      (cust_id, cust_nm, email...) values
      (s.cust_id, s.cust_nm, s.email...);
    ```
    
- Source(customer_delta) 테이블을 기준으로 Target(custmoer) 테이블과 Left Outer Join하여 조인에 성공하면 UPDATE, 실패하면 INSERT한다
    - MERGE 문을 **`(UPDATE + INSERT)`**라고 부르는 이유

## DELETE Clause

- 이미 저장된 데이터를 조건에 따라 지우는 기능

```sql
merge into customer t using customer_delta s on (t.cust_id = s.cust_id)
where matched then update
  set t.cust_nm = s.cust_nm, t.email = s.email, ...
  delete where t.withdraw is not null -- 탈퇴일시가 null이 아닌 레코드 삭제
when not matched then insert
  (cust_id, cust_nm, email...) values
  (s.cust_id, s.cust_nm, s.email...);
```

- UPDATE가 이루어진 결과로서 탈퇴일시가 Null이 아닌 레코드만 삭제
- 조인에 성공한 레코드만 삭제
- 쉽게 말해 순서가 아래와 같음
    1. 조인에 성공한(Matched) 데이터를 일단 모두 UPDATE한다
    2. 그 결과값이 DELETE WHERE 조건절을 만족하면 삭제한다.
