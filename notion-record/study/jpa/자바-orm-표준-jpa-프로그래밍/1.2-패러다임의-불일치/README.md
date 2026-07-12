# 1.2 패러다임의 불일치

## 객체를 데이터베이스에 저장

- 개발자가 직접 SQL 작성이 아닌 **`JPA가 제공하는 API`**사용

## 아주 간단한 예시

- 저장
    - jpa.persist(member);
- 조회
    - String memberId = “example”;
    - Member member = jpa.find(Member.class,memberId);
- 수정
    - Member member = jpa.find(Member.class,memberId);
    - member.setName(”수정”);
- 연관된 객체 조회
    - Member member = jpa.find(Member.class,memberId);
    - Team team = member.getTeam();

# 1.2 - 패러다임의 불일치

- 애플리케이션의 발전과 내부의 복잡성 커짐은 일치
    - **`복잡성을 제어`**하는 것이 핵심
- 비즈니스 요구사항을 정의한 모델도 객체로 모델링
    - 객체지향 언어가 가진 장점을 활용할 수 있음

- 관계형 데이터베이스에는 데이터 중심으로 구조화
    - 집합적인 사고 요구
    - 객체지향의 개념이 없음

<aside>
💡 객체와 관계형 데이터베이스의 지향점이 다르므로 둘의 기능과 표현방법이 다름
→
**`패러다임의 불일치`**

</aside>

## 1.2.1 JPA와 상속

- 자바 컬렉션에 객체를 저장하듯이 JPA에 객체를 저장한다.
- Item을 상속한 Album 객체 저장
    - Item, Album에 각각 저장
    - album을 찾을라고 하면 JPA는 두 테이블을 조인해서 알아서 알려준다.

## 1.2.2 연관관계

- 객체는 **`참조`**를 사용해서 다른 객체와 연관관계
    - 참조에 접근해서 연관된 객체를 조회
- 테이블은 **`외래 키`**를 사용해서 다른 테이블과 연관관계
    - 조인을 사용해서 연관된 테이블을 조회
    

### 객체를 테이블에 맞추어 모델링 → **`객체지향 모델링`**

- 특정 회원이 소속된 팀을 조회하는 객제지향적인 방법?
    - 참조를 사용하는것
        - Member.teamId 필드가 아닌,
        - **`Team team이 Member 안에 있어야 한다.`**
        
        ```java
        class Member {
        	String id;
        	Team team; // **참조로 연관관계**를 맺는 것.
        	String username;
        
        	Team getTeam() {
        		return team;
        	}
        }
        
        class Team {
        	Long id; // TEAM_ID PK
        	String name;
        }
        ```
        

- 객체를 데이터베이스에 저장하려면 team 필드를 **`TEAM_ID 외래 키 값으로 변환해야 한다`**
    - member.getTeam().getId();
    - 패러다임 불일치를 해결하려고 소모하는 비용들임
    
    <aside>
    💡 **`자바 컬렉션에 회원 객체를 저장`**한다면 비용이 들지 않음
    
    </aside>
    

## 1.2.3 객체 그래프 탐색

- 객체는 마음껏 객체 그래프를 탐색할 수 있어야 한다.
- 근데 어떻게 알까?
    - MemberService가 DAO를 통해서 member를 조회했다고 하자.
        - 이 객체와 연관된 Team, Order, Delivery…다른 것들
        - 탐색할 수 있을지 없을지는 모른다.
            - 결국 DAO를 열어서 SQL을 확인해야 한다.
                - SQL에 의존적인 개발
                - 엔티티가 SQL에 논리적으로 종속되어서 발생한다.

### JPA와 객체 그래프 탐색

- JPA는 연관된 객체를 사용하는 시점에 SELECT SQL을 실행
    - 실제 객체를 사용하는 시점까지 데이터베이스 조회를 미룬다
        - **`지연 로딩`**

## 1.2.4 비교

- 객체
    - 동일성 비교(identity) , 동등성 비교(equals)
    - 주소, 값 비교 차이
- 그래서 테이블의 로우를 구분하는 방법과 객체를 구분하는 방법에는 차이가 존재
    - Member member1 = memberDAO.getMember(memberId);
    - Member member2 = memberDAO.getMember(memberId);
        - member1 == member2 —> false
    - Member member1 = list.get(0);
    - Member member2 = list.get(0);
        - member == member2 —> true

### JPA와 비교

- Member member1 = jpa.find(Member.class, memberId);
- Member member2 = jpa.find(Member.class, memberId);
    - member1 == member2 —> true
- JPA는 같은 트랜잭션일 때 같은 객체가 조회되는 것을 보장
