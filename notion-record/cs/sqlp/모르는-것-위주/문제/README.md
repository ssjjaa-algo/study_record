# 문제

표준 SQL(SQL1999)에서테이블 생성시참조관계를정의하기 위해 외래키 (Foreign Key)를 선언한다. 관계형 데이터베이스에서 Child Table의 FK 데이터 생성시 Parent Table에 PK가 없는 경우, Child Table 데이터 입력을 허용하지 않는 참조동작(Referential Action)인 것은?

- Dependent

SUM의 경우 ORDER BY 사용 시 ORDER BY 컬럼 순서대로 누적 합을 계산하는데, 이때 범위는 RANGE가 기본이다. RANGE란 ORDER BY 절에 명시된 컬럼의 값이 같을 경우 하나의 그룹으로 묶어서 누적 합을 계산하는 범위를 말한다. 따라서 2024.01.02 값이 두 개이므로 **`각각 1000과 2000이 먼저 3000으로 결합`**되어 두 번째와 세 번째 행의 누적 합이 둘 다 4000이 된다. DENSE_RANK의 경우 동순위 발생 뒤 순위가 연속적으로 출력되므로 주문번호가 3인 행의 순위는 2위가 된다.

NATURAL JOIN은 컬럼명이 같은 컬럼끼리 값이 같은 조인을 완성한다. 따라서 양쪽 테이블의 COL2의 값이 같은 대상끼리 연결되는데, TAB1의 COL2가 10인 경우 1개, 20인 경우 2개, 30인 경우는 생략되며, NULL은 같다고 볼 수 없으므로 생략된다. 

30/24/60 은 30*1/24/60 인데 1/24/60 는 1 분을 의미한다. (1(하루)를 24 로 나누면 1 시간, 다시 60 으로 나누면 1 분이 된다.)

연산자는 NOT > AND > OR 순서대로의 우선순위를 가지고 있다.

CREATE, TRUNCATE 같은 DDL은 묵시적으로 COMMIT을 수행하므로 ROLLBACK 명령어를 수행하면 마지막 COMMIT을 수행한 6번째 행으로 이동한다.

 SELECT TEAM_ID, NVL(SUM(CASE POSITION WHEN 'FW' THEN 1 ELSE 1 END), 0) FW, NVL(SUM(CASE POSITION WHEN 'MF' THEN 1 ELSE 1 END), 0) MF, NVL(SUM(CASE POSITION WHEN 'DF' THEN 1 ELSE 1 END), 0) DF, NVL(SUM(CASE POSITION WHEN 'GK' THEN 1 ELSE 1 END), 0) GK, COUNT(*) SUM FROM PLAYER GROUP BY TEAM_ID；

 SELECT TEAM_ID, NVL(SUM(CASE WHEN POSITION = 'FW' THEN 1 END), 0) FW, NVL(SUM(CASE WHEN POSITION = 'MF' THEN 1 END), 0) MF, NVL(SUM(CASE WHEN POSITION = 'DF' THEN 1 END), 0) DF, NVL(SUM(CASE WHEN POSITION = 'GK' THEN 1 END), 0) GK, COUNT(*) SUM FROM PLAYER GROUP BY TEAM_ID；

 SELECT TEAM_ID, NVL(SUM(CASE POSITION WHEN 'FW' THEN 1 END), 0) FW, NVL(SUM(CASE POSITION WHEN 'MF' THEN 1 END), 0) MF, NVL(SUM(CASE POSITION WHEN 'DF' THEN 1 END), 0) DF, NVL(SUM(CASE POSITION WHEN 'GK' THEN 1 END), 0) GK, COUNT(*) SUM FROM PLAYER GROUP BY TEAM_ID；

 SELECT TEAM_ID, ISNULL(SUM(CASE WHEN POSITION = 'FW' THEN 1 END), 0) FW, ISNULL(SUM(CASE WHEN POSITION = 'MF' THEN 1 END), 0) MF, ISNULL(SUM(CASE WHEN POSITION = 'DF' THEN 1 END), 0) DF, ISNULL(SUM(CASE WHEN POSITION = 'GK' THEN 1 END), 0) GK, COUNT(*) SUM FROM PLAYER GROUP BY TEAM_ID；

SAVEPOINT가 중복될 경우 ROLLBACK TO SAVEPOINT을 수행하면, 중복된 SAVEPOINT 중 맨 뒤에 있는 SAVEPOINT 지점으로 ROLLBACK 된다.

NTILE(3)는 데이터 3등분 한다. 그리고 각 등분에 대해서 COUNT를 계산하므로 3,2가 된다.

ORDER BY 절을 사용하지 않으면 기본적으로 테이블에 입력된 행 순서대로 출력된다.

NULL IF는 두 값이 같으면 null

ORDER BY절에는 GROUP BY에 사용하지 않은 컬럼을 명시할 수 없다.

서브쿼리가 메인쿼리 컬럼을 가지고 있을 경우 연관 서브쿼리라고 하며, 주로 메인쿼리가 먼저 수행된 후에 서브쿼 리에서 조건이 맞는지 확인할 때 사용한다.

메인쿼리에 값을 제공하기 위한 목적으로 사용하는 쿼리는 비연관 서브쿼리이다.

단일 행 서브쿼리는 단일 행 비교연산자인 =, <>, >, >=, <, <=의 연산자를 주로 사용한다.

GROUPING SETS(PRODUCT_NO, GOGAK_NO, ())에서 PRODUCT_NO별 SUM(QTY) 결과, GOGAK_NO별 SUM(QTY)연산 결과가 출력된 것과 ()으로 인해 SUM(QTY)의 전체 총 합이 출력된 것을 찾는 문제이다.

V1 NOT IN ('A', NULL, 'B', 'C') => NOT (V1 = 'A' OR V1 = NULL OR V1 = 'C') => V1 != 'A' AND V1 != NULL AND V1 != 'C' V1 != NULL은 거짓이므로 이 조건으로 인해 전체 조건이 거짓이 된다. 

NULL은 IN 연산자에 의해 출력되지 않는다

DELETE 문을 사용할 때는 FROM 키워드를 생략할 수 있다 → 테이블명을 생략하는게 아니라 from만 생략 가능

여러 사용자 관점으로 구성하는 것이 외부스키마

파생속성 : 데이터를 조회할 때 성능을 빠르게 하기 위해 원래 속성의 값을 계산하여 저장할 수 있도록 만든 속성

관계명, 관계차수, 선택사양

엔티티 내에서 스스로 생성되었는지에 따라 내부/외부식별자로 구분

정규화는 논리 데이터 모델의 일관성을 확보하는 것이다 (개념 데이터 모델 x)

GROUP BY 절에는 ALIAS 명을 사용할 수 없다

NULLIF(MGR,7698) --> NULL이다 MGR이 7698이라면
