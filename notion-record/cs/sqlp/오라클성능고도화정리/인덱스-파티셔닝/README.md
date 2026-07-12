# 인덱스 파티셔닝

# 인덱스 파티션 유형

- 비파티션 인덱스
- 글로벌 파티션 인덱스
- 로컬 파티션 인덱스
- 오라클이 자동으로 관리하는 1:1 관계가 아닌 파티션 인덱스는 모두 **`글로벌 파티션 인덱스`**

### 테이블로 구분

- 비파티션 테이블
    - 비파티션 인덱스와 글로벌 파티션 인덱스를 가질 수 있다
- 파티션 테이블
    - 비파티션 인덱스, 글로벌 파티션 인덱스, 로컬 파티션 인덱스를 가질 수 있다

## 로컬 파티션 인덱스

- 인덱스 파티션이 테이블 파티션과 1:1 대응
- 오라클이 알아서 관리해준다 → 신경 x
- 테이블이 결합 파티셔닝 → 인덱스도 같은 단위로 파티셔닝

## 비파티션 인덱스

- 파티셔닝하지 않은 인덱스
- 글로벌 비파티션 인덱스라 부르기도 한다

## 글로벌 파티션 인덱스

- 테이블 파티션과 독립적인 구성을 가진다
- 기존 테이블의 파티션 구성에 변경(drop, exchange, split 등)이 생길 때마다 인덱스가 unusable 상태로 바뀌고 그 때마다 인덱스를 재생성해야 한다
    - **비파티션 인덱스도 동일**
    - update global indexes 옵션을 주어 파티션 DDL 작업에 영향받는 인덱스 레코드를 자동으로 갱신해주면 인덱스가 unusable 상태로 빠지지 않는다
    
    ```sql
    alter table
    split partition ...
    update global indexes;
    ```
    
    - 파티션 DDL로 인해 영향 받는 레코드가 전체의 5% 미만일 때만 유용
        - 그니까 이상이면 재생성하는게 낫다는 것

## 글로벌 해시 파티션 인덱스(잘 모른채로 넘어갈 것.)

- 테이블과 독립적으로 인덱스만 해시 키 값에 따라 파티셔닝 할 수 있다
- Hot 블록이 발생하는 인덱스의 경합을 분산할 목적으로 사용된다

## Prefixed VS Nonprefixed

- Prefixed
    - 파티션 키 컬럼을 인덱스 키 컬럼 왼쪽 선두에 두는 것
- Nonprefixed
    - 파티션 키 컬름을 인덱스 키 컬럼 왼쪽 선두에 두지 않는 것
    - **파티션 키가 인덱스에 아예 속하지 않을 때도 여기에 속함**
- 글로벌 파티션 인덱스는 Prefixed만 지원

## 인덱스 파티셔닝 예제

```sql
create table t (gubun
   , seq, seq_name, seq_cls
   , seq2, seq2_name, seq2_cls
)
partition by range(seq) (
  partition p1 values less than (100)
, partition p2 values less than (200)
, partition p3 values less than (300)
, partition p4 values less than (maxvalue)
)
as
select 1
     , rownum, dbms_random.string('u', 10), 'A'
     , rownum, dbms_random.string('1', 10), 'B'
from dual
connect by level <= 400;
```

### 로컬 파티션 인덱스

```sql

create unique index t_idx1 on t(gubun, seq2) LOCAL;

# ERROR at line 1;
# ORA-14039: partition columns must form a subset of key columns of a UNIQUE index
```

- Unique 파티션 인덱스를 만들 때는 파티션 키 컬럼이 인덱스에 포함돼 있어야 한다
- **현재 만들려고 하는 것이 LOCAL 파티션 인덱스**
    - 테이블 파티션 키 컬럼을 상속받아 seq가 파티션 키 컬럼인데, 이 컬럼이 인덱스 컬럼에 없는 상태
        - 근데 인덱스 컬럼에 포함되어 있지 않아 에러가 발생
- 인덱스 컬럼(seq)를 추가해주면 만들어진다
    - Nonprefixed 형태임
    
    ```sql
    create unique index_t idx2 on t(gubun, seq) LOCAL;
    ```
    
- 비파티션 인덱스를 만든다면? 상관없이 만들어진다
    
    ```sql
    create unique index_t idxq on t(gubun, seq2);
    ```
    

### 글로벌 파티션 인덱스

```sql
create index t_idx5 on t(seq_cls, seq) GLOBAL
partition by range(seq) (
	partition p1 values less than(100)
, partition p2 values less than(200)
, partition p3 values less than (maxValue)
)
;

#ORA-14038: GLOBAL partitioned index must be prefixed

create index t_idx5 on t(seq, seq_cls) GLOBAL
partition by range(seq) (
	partition p1 values less than(100)
, partition p2 values less than(200)
, partition p3 values less than (maxValue)
)
# success

create index t_idx6 on t(seq, seq_name) GLOBAL
partition by range(seq) (
  partition p2 values less than(200)
, partition p3 values less than (maxValue)
)

# 키 값 구간 정의가 다르므로 글로벌 파티션 인덱스이면서 
# 각 인덱스 파티션이 두 개 테이블 파티션과 매칭

```
