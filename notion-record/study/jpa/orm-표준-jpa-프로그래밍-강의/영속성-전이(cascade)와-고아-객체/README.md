# 영속성 전이(CASCADE)와 고아 객체

# CASCADE

```java
@OneToMany(mappedBy = "abcd", cascade = CascadeType.ALL)
// cascade는 여러 조건을 설정 가능. 문서 참고
```

# orphanRemoval

- 참조하는 곳이 하나일 때 사용
- **`특정 엔티티가 개인 소유할 때 사용`**
    - 예를 들어 특정 카테고리에서 상품을 삭제하면 상품도 삭제되는 거.
- 부모 객체 삭제 → 자식 객체 삭제.

```java
@OneToMany(mappedBy = "abcd", cascade = CascadeType.ALL, orphanRemoval = true)
// 자동 삭제
```

## 영속성 전이 + 고아객체의 생명주기

- DDD Aggregate Root 개념이랑 잘 맞아떨어진다.
