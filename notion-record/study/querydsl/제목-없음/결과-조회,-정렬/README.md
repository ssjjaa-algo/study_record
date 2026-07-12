# 결과 조회, 정렬

- fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
- fetchOne() : 단 건 조회
    - 결과가 없으면 null
    - 결과가 둘 이상이면 NonUniqueResultException
- fetchFirst() : limi(1).fetchOne()
- fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
- fetchCount() : count 쿼리로 변경해서 count 수 조회

```java

public void resultFetch() {
	List<Member> fetch = queryFactory
		.selectFrom(member)
		.fetch();

	Member fetchOne = queryFactory
		.selectFrom(member)
		.fetchOne();

	Member fetchFirst = queryFactory
		.selectFrom(member)
		.fetchOne();	

	// 페이징용 쿼리 가능. count를 위해 쿼리가 2번 나간다.
	QueryResult<Member> results = queryFactory
		.selectFrom(member)
		.fetchResults();

	results.getTotal(); // 개수
	List<Member> content = result.getResults(); // 실제 내용 조회

	long total = queryFactory
		.selectFrom(member)
		.fetchCount();
}
```

```java
/**
1. 나이 내림차순
2. 이름 올림차순
이름이 없으면 맨 뒤로
*/

queryFactory
		.selectFrom(member)
		.where(member.age.eq(100))
		.orderBy(member.age(desc(), member.username.asc().nullsLast())
		.fetch();
```
