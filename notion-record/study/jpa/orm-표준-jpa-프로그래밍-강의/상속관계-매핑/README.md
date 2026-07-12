# 상속관계 매핑

# @Inheritance(strategy = “”)

<aside>
💡 JPA 기본 전략 : 싱글 테이블

</aside>

## 조인

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn // DTYPE이 생기는데, 상속하는 클래스들의 type이 들어간다.
```

## 싱글테이블

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TYPE)
```

## 구현 클래스마다 테이블 전략

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLS_PER_CLASS)
```

- 권장 x

# @MappedSuperclass

- 공통 매핑 정보가 필요할 때 사용
- BaseEntity를 상속해서 사용하는거
    
    ```java
    @MappedSuperclass
    public class BaseEntity {
    
    }
    ```
    
- 엔티티가 아니라서 조회 x
    - 이러면 무조건 abstract class로.
