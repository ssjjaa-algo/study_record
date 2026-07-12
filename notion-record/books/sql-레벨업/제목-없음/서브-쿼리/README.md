# 서브 쿼리

태그: 문제점, 상황 비교

# 서브 쿼리의 문제점

- 연산 비용의 추가
    - 실체적인 데이터를 저장하고 있지 않다 == **접근할 때마다 SELECT 실행**
- 데이터 I / O
    - temp 탈락 현상의 일종
- **`최적화 제한`**
    - 옵티마이저가 쿼리를 해석하기 위해 필요한 정보를 서브 쿼리에서 못얻는다

# SQL 튜닝에서 가장 중요한 부분은 I/O를 줄이기

- 윈도우 함수가 하나의 방법이 될 수 있다.

# 서브쿼리 사용이 더 나은 경우

- 결합 대상 레코드 수를 사전에 압축해서 낮출 수 있는 경우

- 상황 : 회사마다 주요 사업소에서 main_flg = Y의 직원 수를 구해 결과를 얻는다.
- district는 다른 테이블에 위치하기 때문에 join이 필요한 상황
1. 결합하고 집약하는 경우

```java
SELECT C.co_cd, MAX(C.district), SUM(emp,nbr) AS sum_enp
	FROM Companies C
		INNER JOIN
			Shops S
		ON C.co_cd = S.co_cd
	WHERE main_flg = 'Y'
	GROUP BY C.co_cd
```

1. 집약하고 결합하는 경우

```java
SELECT C.co_cd, C.district, sum_emp
	FROM Companies C
		INNER JOIN
			(SELECT co_cd,SUM(emp_nbr) AS sum_emp
				FROM Shops
				WHERE main_flg = 'Y'
				GROUP BY co_cd) CSUM
	ON C.co_cd = CSUM.co_cd
```

- 예를 들어 회사 테이블이 레코드가 4개, 사업소 테이블이 레코드가 10개라고 한다면
    - 1번 방법에 비해 2번 방법이 결합 비용을 낮출 수 있다.
        - 왜? 2번 방법은 CSUM 뷰가 집약되어 4개로 압축되기 때문에.
