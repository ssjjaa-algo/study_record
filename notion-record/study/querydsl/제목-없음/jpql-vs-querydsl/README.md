# JPQL vs QueryDsl

태그: 간단 문법 비교

```java
// EntityManager 주입이 완료되었다고 가정

@Autowired
EntityManager em;

@Test
public void jpql() {
		Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
         .setParameter("username", "member1")
         .getStringResult();
}

@Test
public void queryDsl() {
   JPAQueryFactory queryFactory = new JPAQueryFactory(em);
   QMember m = new QMember("m");

	 Member member = queryFactory
				.select(m)
				.from(m)
				.where(m.username.eq("member1")) // 파라미터 바인딩 처리
				.fetchOne();
}
```

- jpql에서는 파라미터 바인딩 (setParameter)
- querydsl 실행 시 preparedStatement로 파라미터 바인딩이 되기 때문에 sql injection 우려 x

- querydsl이 컴파일 타임에 오류를 잡을 수 있기 때문에 jpql에 비해 유리
