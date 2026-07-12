# 레코드에 순번 붙이기

# 기본 키가 한 개의 필드일 경우

```sql
SELECT student_id,
	ROW_NUMBER() OVER(ORDER BY student_id) AS seq
	FROM 테이블
```

- ROW_NUMBER를 사용하지 못하는 경우

```sql
SELECT student_id,
	(SELECT count(*)
		FROM Weights W2
	 WHERE W2.student_id <= W1.student_id) AS seq
	FROM Weights W1
```

# 기본 키가 여러 개의 필드

```sql
SELECT student_id,
	ROW_NUMBER() OVER(ORDER BY class, student_id) AS seq
	FROM 테이블
```

- 상관 서브쿼리

```sql
SELECT student_id,
	(SELECT count(*)
		FROM Weights W2
	 WHERE (W2.class, W2.student_id) <= (W1.class, W1.student_id) AS seq
	FROM Weights W1
```

# 그룹마다 순번

```sql
SELECT class, student_id,
	ROW_NUMBER() OVER(PARTITION BY class ORDER BY class, student_id) AS seq
	FROM 테이블
```

- 상관 서브쿼리

```sql
SELECT student_id,
	(SELECT count(*)
		FROM Weights W2
	 WHERE W2.class = W1.class
		 AND W2.student_id <= W1.student_id) AS seq
	FROM Weights W1
```

# 순번과 갱신
