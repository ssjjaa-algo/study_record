# JPQL

```java
List<Member> result = em.createQuery {
		"select m From Member m where m.username like '%kim%'"
	}.getResultList();
```

- JPQL은 테이블이 아닌 **`엔티티를 대상`**으로 쿼리를 날림
    - 따라서 Member는 테이블이 아닌 엔티티로 설정한다

## Criteria

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);

Root<Member> m = query.from(Member.class);

query.select(m).where(cb.equals(m.get("username"), "kim"));

List<Member> resultList = em.createQuery(cq).getResultList();
```

- 복잡하다.
- Querydsl로 대체하자.

## TypeQuery, Query

- TypedQuery는 반환 타입이 명확할 때.
- Query는 반환 타입이 명확하지 않을 때

## 결과 조회

```java
TypedQuery<Member> query = em.createQuery("select m from Member m", Member.class);

List<Member> result = query.getResultList();

Member result = query.getSingleResult(); // 하나일 때
// 결과가 없으면 NoResultException, NonUniqueResultExcpetion
```

```java
TypedQuery<Member> query = 
em.createQuery("select m from Member m where m.username = ":username", Member.class)
.setParameter("username", "member1");
```
