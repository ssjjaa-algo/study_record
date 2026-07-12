# 플러시

# 영속성 컨텍스트의 변경 내용을 db에 반영

- 변경 감지
- 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
- 쓰키 지연 SQL 저장소의 쿼리를 db에 전송
- 영속성 컨텍스트의 변경내용을 데이터베이스에 동기화

## 플러시 방법

- em.flush()
    
    ```java
    Member member = new Member(200L, "member1");
    em.persist(member);
    
    em.flush(); // 이 때 쿼리 나간다.
    // 쓰기 지연 저장소에 있는 것만 db에 반영한다. 1차 캐시는 그대로 존재하는 상태임
    
    tx.commit();
    ```
    
- tx.commit()
- JPQL 쿼리 실행
    
    ```java
    em.persist(memberA);
    em.persist(memberB);
    em.persist(memberC);
    
    // 중간에 JPQL 실행
    query = em.createQuery("select m from Member m", Member.class);
    List<Member> member = query.getResultList();
    ```
    

## 플러시 모드 옵션

- [FlushModeType.AUTO](http://FlushModeType.AUTO) (기본값)
    - 커밋이나 쿼리를 실행할 때 플러시
- FlushmodeType.COMMIT
    - 커밋할 때만 플러시
