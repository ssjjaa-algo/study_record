# 오라클 성능 고도화 원리와 해법 2

텍스트: 소트 튜닝, 파티션

- Optimal Sort : Sort Area 내에서 정렬을 완료
- 양이 많을 때는 정렬된 중간결과 집합을 Temp 테이블스페이스의 Temp 세그먼트에 저장해둠

- Sort Area가 찰 때마다 Temp 영역에 저장해둔 중간 집합이 Sort Run

- Disk Sort는 순서 상 Sort Area -> Sort Runs(Temp tablespace) -> Sort Area

- Onepass 소트 : 정렬 대상 집합이 디스크에 한 번만 쓰임

- Multipass 소트 : 정렬 대상 집합이 디스크에 여러 번 쓰임

- **소트 과정에서 발생하는 디스크 I/O는 Direct Path I/O 방식을 사용하므로 버퍼 캐시를 경유하는 일반적인 디스크 I/O에 비해 가볍다.**
- **Sort Area 크기를 조정한 튜닝의 핵심은 디스크 소트를 방지하며, 불가피할 경우 Onepass소트라도 하도록.**

## Sort Area

- 데이터 정렬을 위해 사용되는 Sort Area는 소트 오퍼레이션이 진행되는 동안 공간이 부족해질 때마다 청크 단위로 조금씩 할당

### UGA

- 전용 서버 방식으로 연결할 때는 프로세스와 세션이 1:1
- 공유 서버 방식으로 연결할 때는 1: M
- 세션이 프로세스보다 많아질 수 있는 구조.
- 각 세션을 위한 독립적인 메모리 공간이 필요 = UGA(User Global Area)
- 전용서버 방식으로 연결할 때는 UGA in PGA, 공유 서버 방식일 때는 UGA in SGA
- 기억해야 할 점
    - 하나의 프로세스는 하나의 PGA를 갖는다.
    - 하나의 세션은 하나의 UGA를 갖는다.
    - PGA에는 세션과 독립적인 프로세스만의 정보를 관리한다.
    - UGA에는 프로세스와 독립적인 세션만의 정보를 관리한다.
    - 거의 대부분이 전용 서버 방식을 사용하므로 세션과 프로세스는 1:1 관계, 따라서 UGA도 PGA 내에 할당된다고 이해

### CGA

- 하나의 데이터베이스 Call을 넘어서 다음 Call에도 참조되어야 하는 정보는 UGA에 담는다
- Call이 진행되는 동안에만 필요한 데이터는 CGA에 담는다.
- CGA는 Parse, Execute, Fetch Call마다 매번 할당받는다.
    - Recursive Call이 그 안에서 일어나도 추가로 할당됨

(우선 이해하기 쉽게)

- DML은 하나의 Execute Call 내에서 모든 데이터 처리 완료 -> sort Area를 CGA에 할당
- SELECT 문장이 수행되는 가장 마지막 단계에서 정렬된 데이터는 Fetch Call에서 사용 -> UGA 할당
    - SELECT는 경우에 따라 다름

## 소트를 발생시키는 오퍼레이션

### Sort Unique

- Unnesting된 서브쿼리가 M쪽 집합이거나 Unique 인덱스가 없다면, 그리고 세미조인으로 수행되지도 않는다면 발생
- 실행계획에 SORT UNIQUE 등장
- union, minus, intersect같은 집합 연산자에도 나타남

## 소트가 발생하지 않도록 SQL 작성

- 불필요한 소트를 말하는 것(PK가 걸려있는데 union 연산을 쓴다)

## 인덱스를 이용한 소트 연산 대체

- 근데 이거 왜 Sort Order By를 대체할 수 있는지는 정확히 잘 모르겠다.
- region = 'A' order by custId

### Sort Group By

- group by 컬럼이 선두 컬럼인 인덱스 사용 -> sort group by nosort
- 인덱스를 스캔하다가 'A'가 아닌 레코드를 만나는 순간 그 때까지 집계한 값을 Oracle Net으로 보냄
- 이후 SDU(Session Data Unit - 1권 5장 5절)에 버퍼링됨.
- 이런 과정을 거치다 Fetch Call에서 요청한 레코드 개수(Array Size)에 도달하면 전송 명령을 내려 보내고 다음 Fetch Call 기다림
- 부분범위처리가 가능해져서 성능 개선.

## 소트 연산을 대체하지 못하는 경우

- 인덱스를 이용하지 않는 편이 더 낫다고 판단하는 경우

- 옵티마이저 모드 all_rows

- 단일 컬럼 인덱스일 때 값이 null -> 인덱스 레코드에 미포함 -> 결과 집합에 오류
- 결합 인덱스에 null 값을 가진 레코드가 있을 때, **order by 컬럼 NULLS FIRST** 이런 식으로 사용하는 경우

- 결합 인덱스일 때는 nul 값을 가진 레코드가 맨 뒤쪽에 저장되기 때문에.

## Sort Area를 적게 사용하도록 SQL 작성

### 소트를 완료하고 나서 데이터 가공하기

### TOP-N 쿼리

```sql
select * from (
	select 거래일시, 체결건수, 체결수량, 거래대금
    from 시간대별종목거래
    where 종목코드 = 'KR123456'
    and 거래일시 >= '20201010'
    order by 거래일시
)
where rownum <= 10
```

- 종목코드 + 거래일시로 구성된 인덱스 -> 인덱스를 이용해 order by 연산 대체 가능
- rownum 조건을 사용해 N건에서 멈추도록 했음 -> 실행계획에 COUNT (STOPKEY)

### TOP-N 쿼리의 소트 부하 경감 원리

- 인덱스가 없는 경우에는 정렬작업이 불가피
- 그러나 rownum 조건의 수만큼 '공간을 할당하여 부하를 줄일 수 있다. 예를 들어 rownum <= 10이라면 10개 배열을 할당하고 조건에 맞는 것들만 바꿔치기 한다는 소리.
- Top-N 쿼리가 작동하고 안하고는 physical read의 차이가 있음 (메모리 정렬 -> disk 안읽음, 읽음)

### 분석함수에서의 Top-N 쿼리

- window sort 시에도 rank()나 row_number()를 사용하면 Top-N 작동, max()등의 함수를 쓸 때보다 소트 부하 경감

## 테이블 파티셔닝

- 파티셔닝도 클러스터, IOT와 마찬가지로 관련 있는 데이터를 물리적으로 인접하는 클러스터링 기술
- **다만 클러스터는 블록 단위 모아서 저장하고, 파티션은 세그먼트 단위로 모아서 저장함**
- IOT와 파티셔닝을 조합(Partitioned IOT)하여 놀라운 성능 효과를 얻을 수 있다.
- 조건절에 부합하틑 테이블만 읽는다 = **파티션 Pruning**
- 파티션되지 않은 일반 테이블 = 테이블 : 세그먼트 1:1
- 파티션된 테이블 = 테이블 : 세그먼트 1 : M (인덱스 파티셔닝도 마찬가지)

```
create table partition_table

# partition by 절은 테이블의 체크 제약# 세 개의 세그먼트 생성partition by range(deptno) (
    partition p1 values less than(20)
  , partition p2 values less than(30)
  , partition p3 values less than(40)
)
as
select * from emp;

create index ptable_empno_idx on partition_table(empno) LOCAL;
```

- Range 파티셔닝

### 해시 파티셔닝

```
partition by hash(고객id) partitions 4;
```

- 데이터 분포를 신중히 고려해야함
- 데이터가 고르게 분포되어 있다면 병렬 I/O 성능 증가, 반대는 반감
- DML 경합 분산 (입력할 블록을 할당받기 위한 Freelist 조회에서 세그먼트 헤더 블록에 대한 경합을 줄일 수 있다)

### 리스트 파티셔닝

- 미리 정해진 그룹핑 기준에 따라 데이터를 분할 저장하는 방식

```
create table 인터넷매물( ~ varchar2(5), ....)
partition by list(지역분류) (
   partition p_지역1 values('서울')
 , partition p_지역2 values('경기', '인천')
 ...
);
```

### 결합 파티셔닝

- 서브 파티션마다 세그먼트를 하나씩 할당하고, 서브 파티션 단위로 데이터를 저장
- 주 파티션 키에 따라 1차적으로 데이터를 분배, 서브 파티션 키에 따라 최종적으로 저장할 세그먼트를 결정

### Range + 해시 결합 파티셔닝

```
create table 주문(주문번호 number, 주문일자 varchae2(8), ...)
partition by range(주문일자)
subpartition by hash(고객id) subpartitions 8
( partition p2009_q1 values less than('20090401')
, partition p2009_q2 values less than('20090701')
....
);
```

- 주문일자로 조회하면 특정 파티션 테이블에 속한 8개의 서브 파티션 탐색
- 고객id로만 조회하면 각 range 파티션당 1개씩 총 6개 서브파티션을 탐색한다.

### Range + 리스트 결합 파티셔닝

### Reference / Interval

# 파티션 Pruning

- **하드파싱이나 실행 시점에 SQL 조건절을 분석**하여 읽지 않아도 되는 파티션 세그먼트를 액세스 대상에서 제외시키는 기능
- 파티션 키 컬럼도 인덱스와 마찬가지로 가공, 형변환하면 이용 못함
    - Predicate Information으로 충분히 확인할 수 있음.

 

### 기본 파티션 Pruning

- 정적 파티션 Pruning
    - 파티션 키 컬럼을 상수 조건으로 조회하는 경우, 쿼리 최적화 시점에 미리 결정됨
        - 실행계획의 Pstart, Pstrop 컬럼에 엑세스할 파티션 번호가 출력
        - IN-List 조건의 경우 상수값이더라고 ‘KEY(I)’라고 출력
- 동적 파티션 Pruning
    - 파티션 키 컬럼을 바인드 변수로 조회하는 경우, 실행 시점에 결정
        - 실행계획의 Pstart, Pstrop 컬럼에 ‘KEY’라고 출력

### 서브쿼리 Pruning

```sql
select d.분기, o.주문일자, o.고객ID. .....
from 일자 d, 주문 o
where o.주문일자 = d.일자
and d.분기 >= 'Q20071'

# 내부적으로 아래와 같은 서브쿼리가 실행된다

select distinct TBL$OR$IDX$PART$NUM(주문, 0, 1, 0, a.일자)
from (select 일자 from 일자 wehre 분기 >= 'Q20071') a
order by 1
```

- 대용량 주문 테이블에 Random Access 위주의 NL 조인 → 성능 기대 어려움
- 소트 / 해시 → 어려움
    - 특정 일자 (2007년 1분기) 이후의 주문 데이터만 필요하나 주문 테이블로부터 모든 파티션을 읽어 조인하고, 나중에 조인 조건을 필터링하는 비효율
- 이럴 때 **Recursive 서브쿼리를 이용한** 동적 파티션 Pruning 이용
- 서브쿼리 Pruning이 작동할 때 **`PARTITIION RANGE SUBQUERY`** 실행계획
- p.647 파라미터 참고

### 조인 필터 Pruning

- 서브쿼리 Pruning은 드라이빙 테이블을 한 번 더 액세스하는 추가 비용 발생
- 11g부터는 블룸 필터 알고리즘을 기반으로 한 조인 필터 방식 도입
    1. n 비트 Array를 할당하고 각 비트를 0으로 설정
    2. n개의 값(1~n)을 리턴하는 m개의 해시 함수를 정의하며, 서로 다른 해시 알고리즘을 사용
        1. m개의 해시 함수는 다른 입력 값에 대해 같은 값을 출력할 수도 있다.
        2. 집합 A의 각 원소마다 차례로 m개의 해시함수를 모두 적용. 그리고 각 해시함수에 리턴된 값에 해당하는 비트를 모두 1로 설정
        3. 집합 B의 원소마다 차례로 m개의 해시함수를 모두 적용. 그리고 원소별로 해시함수에서 리턴된 값에 해당하는 비트를 모두 확인
    - 하나라도 0이면 그 원소는 집합 A에 없는 값.
    - 모두 1로 설정돼 있으면 그 원소는 집합 A에 포함될 가능성이 있는 값이므로 이 때만 A를 찾아가 실제 값을 가진 원소인지 찾아본다.(모두 1이어도 잘못된 양수일 가능성이 있다고 한다)

- 실행계획에 PART JOIN FILTER CREATE , PARTITION RANGE JOIN-FILTER
- 가급적 범위는 between을 명확하게 (LIKE 보다) 이용할 것

# 인덱스 파티셔닝

- 비파티션 테이블
    - 비파티션 인덱스
        - 파티셔닝하지 않은 인덱스 (하나의 인덱스 세그먼트가 여러 테이블 파티션 세그먼트와 관계를 가짐) → 글로벌 비파티션 인덱스라 부르기도 한다.
    - 글로벌 파티션 인덱스
- 파티션 인덱스
    - 비파티션 인덱스, 글로벌 파티션 인덱스, 로컬 파티션 인덱스

### 테이블 파티션과의 관계

- 오라클이 자동으로 관리하는 1:1 관계가 아닌 파티션 인덱스는 모두 **글로벌 파티션 인덱스**

### 글로벌 해시 파티션 인덱스

- 인덱스만 해시 키 값에 따라 ㅍ티셔닝 할 수 있다.
- Right Growing 인덱스처럼 Hot 블록이 발생하는 인덱스의 경합을 분산할 목적으로.

### Prefixed vs Nonprefixed

- 파티션 키 컬럼이 인덱스 키 컬럼 왼쪽에 있냐, 없냐에 따라.
- **`글로벌 파티션 인덱스는 Nonprefixed 지원 안한다. 왜?`**
