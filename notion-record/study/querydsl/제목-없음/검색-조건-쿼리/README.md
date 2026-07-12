# 검색 조건 쿼리

태그: 기본 문법

```java
@Test
public void search() {

		// 둘 다 동일한 쿼리
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1")
						.and(member.age.eq(10))
			.fetchOne();

		Member findMember = queryFactory
			.selectFrom(member)
			.where(
						member.username.eq("member1"),
						member.age.eq(10)
			.fetchOne();

}
```

- eq (equal)
- ne (not equal)
- isNotNull()
- in
- notIn
- between
- goe(30) (age ≥ 30)
- gt(30) ( age > 30)
- loe(30)(age ≤ 30)
- lt(30) (age < 30)
- like
- contains(”member”)  %member%
- startsWith(”member”) member%
