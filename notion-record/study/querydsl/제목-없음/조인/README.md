# 조인

```java
List<Member> result = queryFactory
		.selectFrom(member)
		.join(member.team, team) // left, right 다 가능
		.where(team.name.eq("teamA"))
		.fetch();
```

### 세타 조인

- 연관관계가 없는 필드로 조인이 가능하다.
    - on을 이용하면 외부 조인도 가능하다.

### on절 조인

- 필터링 조인

```java
// 회원과 팀을 조회하면서 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회

queryFactory
		.selectFrom(member,team)
		.from(member)
		.leftJoin(memeber.team, team).on(team.name.eq("teamA"))
		.fetch();
```

### 연관관계 없는 외부 엔티티 조인

```java
List<Tuple> result = queryFactory
		.selectFrom(member, team)
		.from(member)
		.leftJoin(team).on(member.username.eq(team.name))
		.fetch();
```

## 페치 조인

- SQL 조인을 활용해서 연관된 엔티티를 SQL 한번에 조회하는 기능.

```java

Member findMember = queryFactory
		.selectFrom(member, team)
		.from(member)
		.join(member.team, team).fetchJoin()
		.where(member.username.eq("member1))
		.fetch();

// fetchJoin()만 적으면 된다. 연관된 엔티티를 즉시 조회한다.
```
