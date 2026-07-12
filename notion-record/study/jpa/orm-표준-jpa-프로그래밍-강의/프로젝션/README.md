# 프로젝션

- SELECT 절에 조회할 대상 지정
- 대상
    - 엔티티, 임베디드, 스칼라 ..

```java

List<Member> resultList = em.createQuery("select m from Member m", Member.class)
							.getResultList();
							
```
