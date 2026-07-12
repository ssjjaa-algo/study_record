# SQL 변화 시리즈

# Transitivity

```sql
SELECT 사원.사원번호, 사원.사원이름, 부서.부서번호, 부서.부서이름
  FROM 사원, 부서
 WHERE 사원.부서번호 = 부서.부서번호
   AND 부서.부서번호 = '10';
```

- 상수 조건의 전이 (삼단논법과 같은 논리임)

```sql
SELECT 사원.사원번호, 사원.사원이름, 부서.부서번호, 부서.부서이름 
  FROM 사원, 부서, 급여
 WHERE 사원.부서번호 = 부서.부서번호 
   AND 사원.부서번호 = 급여.부서번호;
```

- **join 조건에 대한 전이는 일어나지 않는다**

# View Merging

- 두 가지 존재
    - 주 쿼리의 조건이 인라인 뷰 안으로
    - 인라인 뷰 또는 뷰의 SQL이 주 쿼리로 합쳐지는 경우

### 주 쿼리의 조건이 안으로

```sql
SELECT A.사원번호, A.사원이름, B.부서번호, B.부서이름 
  FROM
     ( SELECT 부서번호, 사원번호, 사원이름, SUM(급여) 
         FROM 급여 
        WHERE 부서번호 = 10 
        GROUP BY 부서번호, 사원번호, 사원이름 
     ) A, 부서 B 
 WHERE A.부서번호 = B.부서번호 
   AND A.사원번호 = '100';
```

```sql
SELECT A.사원번호, A.사원이름, B.부서번호, B.부서이름 
  FROM
     ( SELECT 부서번호, 사원번호, 사원이름, SUM(급여) 
         FROM 급여 
        WHERE 부서번호 = 10 
          AND 사원번호 = '100'
        GROUP BY 부서번호, 사원번호, 사원이름 
     ) A, 부서 B 
 WHERE A.부서번호 = B.부서번호;
```

- 인라인 뷰 안에서 처리 범위를 감소
    - 추가된 사원 번호가 인덱스를 이용해야만 함

### 인라인 뷰가 주 쿼리로

```sql
SELECT a.department_name, c.employee_name, c.address 
  FROM department a, 
     ( SELECT department_id, grade, employee_name, address 
         FROM employees b 
        WHERE sal > 200 
     ) c 
 WHERE a.department_id = b.department_id 
   AND c.grade= 's';
```

```sql
SELECT a.department_name, b.employee_name, b.address 
  FROM department a, employees b 
 WHERE a.department_id = b.department_id 
   AND b.grade = 'S'
   AND b.sal > 200;
```

- 앞의 SQL만 보면 인덱스 구성이 sal만 들어갈 수도 있다
- 뒤의 변환된 SQL을 보면 인덱스 구성이 (grade + sal)
- **따라서 변경되는 SQL을 고려한 인덱스 설계가 필요**
- 뷰 머징은 옵티마이저의 선택이기 때문에 항상 하지 않을 수도 있음 (책에선 거의 항상 한다고 하던데)
    - 힌트 유도 필요
    
    ```sql
    SELECT  /*+ MERGE(C) */ a.department_name, c.employee_name, c.address 
      FROM department a, 
         ( SELECT department_id, grade, employee_name, address 
             FROM employees b 
            WHERE sal > 200 
         ) c 
     WHERE a.department_id = b.department_id 
       AND c.grade= 's';
    ```
    
    - 이 또한 제한사항에 의해 항상 수행되지 않을 수 있음.

### Mergeable 인라인 뷰

- 뷰가 해체되거나 뷰 안으로 조건이 삽입되는 인라인 뷰

### Non-Mergeable 인라인 뷰

- 별도로 수행되는 인라인 뷰

# **Non-Mergeable 인라인 뷰 확인하기**

- SQL 형태만 보고서 뷰 Merging이 발생하는지를 파악할 수는 없다
- 다음과 같은 문법은 **Non-Mergeable이 되거나 Merge가 되더라도 주 쿼리의 조건이 인라인 뷰**로 들어간다
    - UNION ALL
    - UNION
    - DISTINCT
    - GROUP BY
    - ROWNUM
    - 집합 함수
- 인라인 뷰가 주 쿼리와 합쳐지는 현상은 일어나지 않음

## 그럼 어떻게 확인하는가

- **`실행계획`**을 보고 확인한다

```sql
SELECT STATEMENT
  NESTED LOOPS
    TABLE ACCESS (FULL) OF 'DEPARTMENT'
    VIEW
      UNION-ALL
        TABLE ACCESS (FULL) OF 'EMPLOYEES'
        TABLE ACCESS (FULL) OF 'EMPLOYEES'
```

- **`VIEW`** 실행계획이 나타났다
    - 인라인 뷰가 별도로 생성되어 메모리에 해당 데이터를 생성했다는 것
    - 인라인 뷰가 별도로 수행되었다 = Non-Mergeable 뷰로 작동했다
    - Mergable 뷰라면 VIEW가 실행계획에 나타나지 않는다
        - 만약 그렇다면 인라인 뷰가 주 쿼리와 합쳐지는 Mergeable 인라인 뷰로 수행된 것
- 주 쿼리의 조건이 인라인 뷰로 들어온 경우
    - **`VIEW PREDICATE`** 실행계획
