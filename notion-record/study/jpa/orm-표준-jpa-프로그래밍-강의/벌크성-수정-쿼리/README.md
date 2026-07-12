# 벌크성 수정 쿼리

태그: spring data jpa

- 순수 JPA 버전

```java
public int bulkAgePlus(int age) {
	return em.createQuery{
		"update Member m set m.age = m.age + 1" +
		" where m.age >= :age"
		.setParameter("age", age)
		.executeUptdate();
	}

}
```

- spring data jpa

```java
@Modifying(clearAutomatically = true)
@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);

// @Modifying을 빼면 안된다. clear는 영속성 컨텍스트를 비워주는 작업.
```

## 벌크 연산시 주의

```java
memberRepository.save(new Member("member1", 10));
memberRepository.save(new Member("member2", 20));
memberRepository.save(new Member("member3", 30));
memberRepository.save(new Member("member4", 40));
memberRepository.save(new Member("member5", 50));

// em.flush();
// em.clear();

int resultCount = memeberRepository.bulkAgePlus(20);

--> 이 때 member5의 나이는 여전히 50이다.
--> save는 db에 flush하지 않은 상태.
--> 따라서 중간에 flush 해줘야 올바른 결과 반영

```

- 이거는 그냥 참고글
    - save와 saveAll의 성능 차이에 관한 내용
    
    [Jpa save vs saveAndFlush vs saveAll](https://dncjf64.tistory.com/458)
