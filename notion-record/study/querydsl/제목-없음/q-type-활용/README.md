# Q-Type 활용

태그: static import

```java
QMember m = QMember.member;

/*
generate된 코드가 static final 메서드로 있음.
*/

// 만들어진 Q클래스 내용의 일부
public class QMember extends EntityPathBase<Member> {
  
  ...
	public static final QMember member = new QMember("member1");

}
```

```java

//QMember의 member를 **static import**해서 내용을 줄인다.

public void queryDsl() {
   JPAQueryFactory queryFactory = new JPAQueryFactory(em);
   QMember m = new QMember("m");

	 Member findMember = queryFactory
				.select(member)
				.from(member)
				.where(member.username.eq("member1")) // 파라미터 바인딩 처리
				.fetchOne();
}
```
