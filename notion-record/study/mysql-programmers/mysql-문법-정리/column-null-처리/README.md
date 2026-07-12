# column null 처리

**`IFNULL`, `CASE`, `COALESCE`**과 같은 함수들이 있다.

# IFNULL

- 해당 **Column의 값이 NULL일 때 대체값으로 변경**
    
    ```sql
    SELECT IFNULL(Column명, "대체값") FROM TABLE
    ```
    

# CASE

```sql
SELECT CASE
WHEN Column명 IS NULL THEN "대체값"
ELSE Column명
END AS Column명
FROM TABLE
```

# COALESCE

**`COALESCE`**는 **지정한 표현식들 중에 NULL이 아닌 첫 번째 값을 반환**한다.

***모든 DBMS에서 사용가능***

표현식은 여러 항목 지정이 가능하고, 처음으로 만나는 NULL이 아닌 값을 출력한다.

*표현식이 모두 NULL일 경우엔 결과도 NULL 반환*

**`COALESCE`**는 배타적 OR 관계 열에서 활용도가 높다.

*엔터티(테이블)에서 두 개 이상의 속성(열) 중 하나의 값만 가지는 데이터 일 경우*

- 기본 구조

```
// NULL 처리 상황
SELECT COALESCE(Column명1, Column명1이 NULL인 경우 대체할 값)
FROM 테이블명

// 배타적 OR 관계 열
// Column1 ~ 4 중 NULL이 아닌 첫 번째 Column을 출력
SELECT COALESCE(Column명1, Column명2, Column명3, Column명4)
FROM 테이블명
```

- Example

```
// NAME Column의 값이 NULL인 경우 다음 표현식으로 넘어간다.
// 다음 표현식인 "No name"이 Null이 아니므로 "No name"을 출력.
SELECT COALESCE(NAME, "No name")
FROM ANIMAL_INS
```
