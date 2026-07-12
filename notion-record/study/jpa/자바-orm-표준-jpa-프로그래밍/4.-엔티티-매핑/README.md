# 4. 엔티티 매핑

<aside>
💡 **`엔티티와 테이블을 정확히 매핑하는 것`**

</aside>

- 객체와 테이블 매핑
    - @Entity, @Table
- 기본 키 매핑
    - @Id
- 필드와 컬럼 매핑
    - @Column
- 연관관계 매핑
    - @ManyToOne, @JoinColumn

## `@Entity`

- @Entity가 붙은 클래스는 JPA가 관리하는 것
- 특징
    - 기본 생성자는 필수다(파라미터가 없는 public 또는 protected 생성자)
        - JPA가 엔티티 객체를 생성할 때 기본 생성자를 사용한다.
    - final 클래스, enum, interface, inner 클래스에 사용 불가
    - 저장할 필드에 **`final`**을 사용하면 안 된다.

## `@Table`

- 엔티티와 매핑할 테이블 지정
    - 생략하면 매핑한 엔티티 이름을 테이블 이름으로 지정

## `데이터베이스 스키마 자동 생성`

- value = “create”
    - 애플리케이션 실행 시점에 데이터베이스 테이블을 자동으로 생성
    - CafeProject 예제
        
        ```bash
        spring.main.hibernate.hbm2ddl.auto=update
        spring.main.hibernate.hbm2ddl.auto=create
        ```
        
- 운영 환경에서 사용할 만큼 완벽하지 않음
    - 개발 환경에서 사용하거나
    - 매핑을 어떻게 해야 하는지 참고하는 정도로만
- **`이 기능을 사용해서 생성된 DDL → 좋은 학습도구`**
- [hibernate.hbm2ddl.auto](http://hibernate.hbm2ddl.auto) 속성
    - create
    - create-drop
    - update
    - validate
        - 데이터베이스 테이블과 엔티티 매핑정보를 비교해서 차이가 있으면 경고
    - none
        
        <aside>
        💡 **위의 옵션들은 무조건 `개발 서버나 개발 단계에서만` 사용해라.**
        
        </aside>
        
    - 추가 공부
        - HBM2DDL
- hibernate.ejb.naming_strategy
    - 카멜 표기법 → 테이블의 언더스코어 매핑

## `기본 키 매핑`

- 기본 키 할당 방식
    - 직접 할당
        - 기본 키를 애플리케이션에서 직접 할당
            - @Id에 @GeneratedValue를 추가
    - 자동 생성
        - IDENTITY : 기본 키 생성을 데이터베이스에 위임
            - 데이터베이스에 값을 저장하고 나서야 기본 키 값을 구할 수 있음
            - 엔티티는 영속상태가 되려면 식별자가 반드시 필요.
            - 즉 이 방식을 쓰면 **`쓰기 지연이 동작하지 않음`**
        - SEQUENCE : 데이터베이스 시퀀스를 사용해서 기본 키를 할당한다.
        - TABLE : 키 생성 테이블을 사용한다.
    - 자동 생성 전략은 데이터베이스 벤더마다 지원하는 방식이 달라서 다양함

### @Id

- 자바 기본형
- 자바 Wrapper형
- String, util.Date, sql.Date, math.BigDecimal, math.BigInteger
- em.persist()로 엔티티를 저장하기 전에 애플리케이션에서 기본 키를 직접 할당??

### IDENTITY 전략

- 기본 키 생성을 데이터베이스에 위임하는 전략
    - 데이터를 데이터베이스에 insert한 후 기본 키 값을 조회할 수 있음
        - 추가로 데이터베이스를 조회해야한다는 것임.
        - 엔티티의 영속 상태가 되려면 **`식별자가 반드시 필요`**
        - 그러므로 em.persist()를 호출하는 즉시 INSERT SQL이 동작
            - 쓰기 지연 지원하지 않음

### SEQUENCE 전략

- 오라클, PostgreSQL, DB@, H2 DB에서 사용 가능 (우선 skip)

### TABLE 전략

- 키 생성 전용 테이블을 만들고 이름과 값으로 사용할 컬럼을 만들어 DB 시퀀스를 흉내내는 전략
    - 우선 skip

### AUTO 전략

- @GeneratedValue
- 데이터베이스를 변경해도 코드를 수정할 필요가 없다
    - 키 생성 전략이 확정되지 않은 개발 초기 단계나 프로토타입 개발 시.

## `의문이었던 점 (책에서 설명하는 대리 키 추천 이유)`

### 자연 키 vs 대리 키에서 대리 키를 권장하는 이유

- 자연키
    - 의미가 있는 키 → 주민등록번호, 아이디, 이메일 …
- 대리키
    - 의미가 없는 키 → 시퀀스, auto_increment …

- 비즈니스적인 측면에서 생각하면
    - 자연키를 기본 키로 선택한다면 유일할 수 있지만, 기본 키로 적당하지 않다.
    - **`기본 키는 항상 변하지 않아야 하는데,`**
    - 주민등록번호처럼 변하지 않을 것 같은 값도 여러가지 이유로 변경이 될 수 있다.

### 비즈니스 환경의 변화

- 대리 키는 비즈니스와 무관하기 때문에 요구사항이 변경되어도 괜찮다.
- 기본 키가 자연 키로 잡혀있을 경우 변경될 때 많은 비즈니스 로직의 변경 필요

## **`필드와 컬럼 매핑`**

- @Column → 컬럼 매핑
    - nullable (기본값 true)
    - unique
- @Enumerated → 자바 enum 타입 매핑
- @Temporal → 날짜 타입 매핑
    
    ```
    TemporalType.DATE // 날짜 --> date date
    TemporalType.TIME // 시간 --> time time
    TemporalType.TIMESTAMP // 날짜와 시간 --> timestamp timestamp
    
    datetime : MySQL
    timestamp : H2, 오라클, PostgreSQL
    ```
    
- @Lob → BLOB, CLOB 타입 매핑 → 이게 뭐지?
- @Transient → 특정 필드를 데이터베이스에 매핑하지 않음
    - 객체에 임시로 어떤 값을 보관하고 싶을 때 사용
- @Access → JPA가 엔티티에 접근하는 방식 지정
    - @Id가 필드에 있는 경우 필드에 직접 접근 가능
    - @Id가 프로퍼티에 있는 경우 접근자 Getter에 사용.
