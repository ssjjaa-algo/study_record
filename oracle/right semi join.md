### 1. SEMI JOIN

- `SEMI JOIN`은 **매칭 존재 여부만 확인**하는 조인
    - EXISTS에서 일치하는 것만 보고 넘어가는 거랑 동일한 원리
- 보통 `EXISTS`, `IN` 서브쿼리가 변환되면서 나옴
    - 직접 힌트로 제어할 수 있다 → unnest hash_sj, unnest nl_sj 등
- **`Semi Join은 항상 메인쿼리가 수행된 후 서브쿼리의 데이터를 체크`**
    - 이 이유로 서브쿼리는 항상 후행집합이 된다
    - 이 비효율을 해결하는 것이 **`RIGHT SEMI JOIN`  → `build input을 제어할 수 있는 hash join 방식에서만 가능`**
    - 후행 집합을 Build Input을 이용하는 것이 유리함에도 이용하지 못했던 것이 RIGHT SEMI JOIN을 통해서 가능하게 되었음

### 2. `RIGHT SEMI JOIN`이 나오는 흐름

- 서브쿼리가 **unnest** 되어 조인으로 풀리고
- 그 조인이 **semi join** 방식으로 선택되고
- Build Input(왼쪽)이 아닌 Probe Input(오른쪽)을 반환하는 경우

### 3. SEMI JOIN은 SORT UNIQUE가 발생하지 않음

- 이미 SEMI JOIN이 선택되었다는 것은 SORT UNIQUE 연산을 하지 않아도 된다는 것

### 4. NL SEMI JOIN은 구조 상 RIGHT SEMI JOIN이 불가능

### 5. 헷갈렸던 포인트

- swap_join_inputs
    - Build Input을 사용자가 직접 선택하고자 할 때는 swap_join_inputs를 선택한다
    - **`2개 테이블을 해시 조인할 때`**는 ordered나 leading 힌트를 사용해도 된다
        - 내가 주로 사용하는 방식  (Oracle 성능 고도화 2권 251p)
        - 내가 헷갈렸던 것은 2개의 테이블 SEMI JOIN 에서 LEADING(A)와 SWAP_JOIN_INPUTS(B)를 써서 RIGHT SEMI JOIN을 유도한다 → unnest만 하고 나면 조인 순서를 제어할 수 있으니 leading(b)만으로도 가능하지 않을까 생각했음
            - 그러나 이건 **`SEMI JOIN을`** 이해해야 함 → SEMI JOIN은 본질 자체가 메인쿼리를 기준으로 필터링하는 방식임
            - 옵티마이저가 세미 조인 방식을 선택하지 않는다면 Unnesting한 다음에 **`메인 쿼리 쪽 테이블이 드라이빙 집합`**으로 선택되게 해야 한다
                - (Oracle 성능 고도화 2권 251p)
        - (19c 기준) 아래 실험 결과를 통해 RIGHT SEMI JOIN을 유도할 때는 qb_name을 이용해서 제어하고 leading이 아닌  swap_join_inputs를 명시해 주는 것이 맞다고 결론
            - **오라클 성능 고도화에서는 qb_name을 지정하여 leading하는 방식을 알려줌**
            - 참고로 Oracle 버전에 따라 다를 수도 있으니 실행계획을 필히 확인할 것

```sql
SELECT /*+ leading(a) swap_join_inputs(@sub b) */
       a.empno, a.sal
FROM   big_emp a
WHERE  EXISTS (
         SELECT /*+ qb_name(sub) unnest hash_sj */
                b.deptno
         FROM   dept b
         WHERE  b.deptno = a.deptno
       );
-----------------------------------------------------------------------------------------
| Id  | Operation            | Name    | E-Rows | Cost (%CPU)|  OMem |  1Mem | Used-Mem |
-----------------------------------------------------------------------------------------
|   0 | SELECT STATEMENT     |         |        |     4 (100)|       |       |          |
|*  1 |  HASH JOIN RIGHT SEMI|         |      1 |     4   (0)|  1506K|  1506K|  432K (0)|
|   2 |   TABLE ACCESS FULL  | DEPT    |      1 |     2   (0)|       |       |          |
|   3 |   TABLE ACCESS FULL  | BIG_EMP |      1 |     2   (0)|       |       |          |
-----------------------------------------------------------------------------------------       

# leading힌트가 동작하지 않음  -> semi join과 의미가 상충된다                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           버전 차이가 있거나. 따로 알아봐야 한다.
SELECT /*+ leading(@sub) */
       a.empno, a.sal
FROM   big_emp a
WHERE  EXISTS (
         SELECT /*+ unnest hash_sj qb_name(sub)*/
                b.deptno
         FROM   dept b
         WHERE  b.deptno = a.deptno
       );
       
| Id  | Operation          | Name    | E-Rows | Cost (%CPU)|  OMem |  1Mem | Used-Mem |
---------------------------------------------------------------------------------------
|   0 | SELECT STATEMENT   |         |        |     4 (100)|       |       |          |
|*  1 |  HASH JOIN SEMI    |         |      1 |     4   (0)|  1068K|  1068K|  186K (0)|
|   2 |   TABLE ACCESS FULL| BIG_EMP |      1 |     2   (0)|       |       |          |
|   3 |   TABLE ACCESS FULL| DEPT    |      1 |     2   (0)|       |       |          |
---------------------------------------------------------------------------------------       

#semi join이 아니기 때문에 힌트가 작동한다
SELECT /*+ leading(@sub) */
       a.empno, a.sal
FROM   big_emp a
WHERE  EXISTS (
         SELECT /*+ unnest qb_name(sub)*/
                b.deptno
         FROM   dept b
         WHERE  b.deptno = a.deptno
       );
---------------------------------------------------------------------------------
| Id  | Operation                    | Name               | E-Rows | Cost (%CPU)|
---------------------------------------------------------------------------------
|   0 | SELECT STATEMENT             |                    |        |     2 (100)|
|   1 |  NESTED LOOPS                |                    |      1 |     2   (0)|
|   2 |   NESTED LOOPS               |                    |      1 |     2   (0)|
|   3 |    TABLE ACCESS FULL         | DEPT               |      1 |     2   (0)|
|*  4 |    INDEX RANGE SCAN          | IDX_BIG_EMP_DEPTNO |      1 |     0   (0)|
|   5 |   TABLE ACCESS BY INDEX ROWID| BIG_EMP            |      1 |     0   (0)|
---------------------------------------------------------------------------------

SELECT /*+ swap_join_inputs(@sub b) */
       a.empno, a.sal
FROM   big_emp a
WHERE  EXISTS (
         SELECT /*+ qb_name(sub) unnest hash_sj */
                b.deptno
         FROM   dept b
         WHERE  b.deptno = a.deptno
       );
       
-----------------------------------------------------------------------------------------
| Id  | Operation            | Name    | E-Rows | Cost (%CPU)|  OMem |  1Mem | Used-Mem |
-----------------------------------------------------------------------------------------
|   0 | SELECT STATEMENT     |         |        |     4 (100)|       |       |          |
|*  1 |  HASH JOIN RIGHT SEMI|         |      1 |     4   (0)|  1506K|  1506K|  390K (0)|
|   2 |   TABLE ACCESS FULL  | DEPT    |      1 |     2   (0)|       |       |          |
|   3 |   TABLE ACCESS FULL  | BIG_EMP |      1 |     2   (0)|       |       |          |
-----------------------------------------------------------------------------------------       

 
 # UNNEST + NL_SJ에서는 메인 쿼리 쪽이 driving이어야 하므로 
 # 서브쿼리 쪽을 선행시키는 leading은 동작하지 않음
SELECT /*+ leading(b) */
       a.empno, a.sal
FROM   big_emp a
WHERE  EXISTS (
         SELECT /*+ unnest nl_sj */
                b.deptno
         FROM   dept b
         WHERE  b.deptno = a.deptno
       );
  
------------------------------------------------------------
| Id  | Operation          | Name    | E-Rows | Cost (%CPU)|
------------------------------------------------------------
|   0 | SELECT STATEMENT   |         |        |     2 (100)|
|   1 |  NESTED LOOPS SEMI |         |      1 |     2   (0)|
|   2 |   TABLE ACCESS FULL| BIG_EMP |      1 |     2   (0)|
|*  3 |   INDEX UNIQUE SCAN| PK_DEPT |      1 |     0   (0)|
------------------------------------------------------------  

```

# 결론

- SEMI JOIN은 본질 자체가 **main 쿼리**에서 시작하여 FILTER 방식으로 처리하는 것과 다름이 없기에 leading으로 힌트를 제어하는 것이 먹지 않음
- 다만 hash join의 경우 right가 가능하게 됨으로써 build input을 지정할 수 있기에 RIGHT SEMI JOIN이 있다