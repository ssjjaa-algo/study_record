# 프록시와 연관관계

# 프록시 기초

## em.find() vs em.getReference()

- em.find()
    - 데이터베이스를 통해 실제 엔티티 객체 조회
- em.getReference()
    - 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회
    
    ```java
    // class명을 호출하면 hibernateProxy 확인 가능
    
    Member member = em.getReference(Member.class, member.getId());
    System.out.println("정보 : " member.getUsername()); // 필요한 순간에 select 쿼리 발생
    ```
    
    - 이미 어떤 member를 find를 통해 가져왔다면, reference 호출해도 같은 클래스
        - 영속성 컨텍스트에 이미 존재하니까.
