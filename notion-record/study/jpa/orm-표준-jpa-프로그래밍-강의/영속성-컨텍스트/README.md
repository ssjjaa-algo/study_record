# 영속성 컨텍스트

# 영속성 컨테스트

- **`엔티티를 영구 저장하는 환경`**
    - EntityManger.persist(entity);
    - db에 저장하는게 아니라 영속성 컨텍스트에 저장해둔다

## 엔티티 생명 주기

- 비영속(new/transient)
    - 영속성 컨텍스트와 **전혀 관계가 없는 상태**
    
    ```java
    Memeber member = new Member();
    meber.setId("member1");
    ```
    
- 영속(managed)
    - 영속성 컨텍스트에 **관리되는 상태**
    
    ```java
    EntityhManger em = emf.createEntityManager();
    em.persist(member); // 영속 상태가 된다.
    // 이때 db에 저장되지 않는다.
    
    tx.commit(); // 이 때 insert 쿼리가 날라간다.
    ```
    
- 준영속(detached)
    - 영속성 컨텍스트에 저장되었다가 분리된 상태
    
    ```java
    em.detach(member); // 영속성 컨텍스트에서 분리, 준영속 상태
    // commit이 일어나더라도 해당 member는 쿼리가 나가지 않음
    
    em.clear(); // 영속성 컨텍스트 초기화
    
    em.close(); // 영속성 컨텍스트 종료
    ```
    
    - 
- 삭제(removed)
    - 영속성 컨텍스트에서 삭제
    
    ```java
    em.remove(member);
    ```
    

## 영속성 컨텍스트 이점

<aside>
💡 버퍼링, 캐시 등의 이점
한 트랜잭션 안에서만 이용하기 때문에 큰 이점을 얻을 수는 없다.
entityManager 생성 → 삭제가 요청 하나에서 이루어짐

</aside>

- **1차 캐시**
    
    ```java
    em.persist(member);
    // 1차 캐시에서 조회
    Member findMember = em.find(Member.class, "member1");
    
    // 영속성 컨텍스트에 member2가 없는 경우, db에서 조회하고 1차 캐시(영속성 컨텍스트)에 저장
    // 이후 반환
    MEmber findMember2 = em.find(Member.class, "member2");
    ```
    
- **동일성(identity)** 보장
    - **`REPEATABLE READ 수준의 격리 수준`**을 애플리케이션 차원에서 제공
    
    ```java
    Member a = em.find(Member.class, "member1");
    Member b = em.find(Member.class, "member1");
    
    System.out.println(a == b);
    // a와 b는 같은 상태임.
    ```
    
- 트랜잭션을 지원하는 **쓰기 지연**
    - 1차 캐시와 쓰기 지연 SQL 저장소가 존재
    - jpa는 우선 쓰기 지연 SQL 저장소에 넣어둔다.
    
    ```java
    em.persist(memberA);
    em.persist(memberB);
    // INSERT SQL을 데이터베이스에 보내지 않는다.
    
    tx.commit(); // 트랜잭션 커밋 순간에 INSERT SQL 보낸다.
    // 이 순간에 jpa의 flush()가 동작
    ```
    
    - 팁 : hibernate_batch_size를 통해 size만큼 쿼리가 쌓이기 전까지 commit하지 않게 가능
- 변경 감지(Dirty Chceking)
    - commit을 할 때
        - 엔티티와 스냅샷을 비교한다.
        - 스냅샷은 값을 읽어온 시점의 값
        - 변경이 있다면 쓰기 지연 SQL 저장소에 반영하고 커밋한다.
    
    ```java
    Member member = em.find(member, 150L);
    memeber.setName("변경");
    
    tx.commit();
    
    // update 쿼리를 작성하지 않아도 Dirty Checking때문에 update가 나중에 알아서 나감
    ```
    
- 지연 로딩(Lazy Loading)
