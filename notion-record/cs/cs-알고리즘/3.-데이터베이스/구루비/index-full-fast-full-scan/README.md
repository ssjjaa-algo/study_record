# INDEX FULL / FAST FULL SCAN

http://www.gurubee.net/lecture/2255

http://www.gurubee.net/lecture/2256

# FULL SCAN

```sql
SELECT COL1, COL2
  FROM TAB1
 WHERE COL3 = 'AAA'
 ORDER BY COL4
```

- 인덱스는 (COL4 + COL3)
- 처음부터 끝까지 보도록 인덱스를 이용할 수 있다

```sql
SELECT STATEMENT
SORT(ORDER BY)
TABLE ACCESS (BY INDEX ROWID) OF 'TAB1'
  INDEX (FULL SCAN) OF 'COL4_COL3_IDX'
```

## FULL SCAN 장점

- TAB1 테이블이 100,000건이며 조건을 만족하는 데이터가 90,000건이라고 가정하자. 또한, 인덱스는 COL4+COL3+COL2+COL1으로 구성되어 있다.
- 인덱스 Full Scan 하지만 데이터 순서를 유지할 수 있다
- 인덱스에 모든 컬럼이 존재한다
    - 테이블보다 작은 인덱스를 전부 엑세스하므로 I/O 양을 감소시킴
    - 인덱스의 첫 번째 컬럼으로 정렬된 데이터를 자동으로 추출

# FAST FULL SCAN

- 인덱스 순서를 유지하지 않는다는 것을 염두에 두고 언제 유리한지 판별한다

## 유리할 때

- 인덱스로만 원하는 데이터를 모두 추출하는 경우
- 해당 테이블의 데이터 중 대부분을 추출하는 경우
- **`정렬이 불필요한 경우`**

```sql
SELECT COUNT(*)
FROM TAB1
```

- PK의 건수를 확인하는 것만으로 결과 확인 가능
- 정렬 별도 미수행 : Fast Full Scan 유도 가능
    - **근데 이거 Table Full Scan이 낫지 않나..**
