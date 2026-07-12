# 페이징

```java

queryFactory
		.selectFrom(member)
		.orderBy(member.username.desc())
		.offset(1)
		.limit(2)
		.fetch();

queryFactory
		.selectFrom(member)
		.orderBy(member.username.desc())
		.offset(1)
		.limit(2)
		.fetchResults();
```

- count쿼리의 분리는 실무에서 요구된다.
- where로 제약조건이 붙는다면 join 시 양쪽에 다 붙기 때문이다.
