# Java Record 타입 정리

Java의 `record`는 Java 14에서 처음 Preview 기능으로 도입되었고, Java 16부터 정식 기능으로 제공되었다. 데이터를 저장하고 전달하기 위한 클래스를 간결하게 정의할 수 있도록 설계되었다.

기존에는 단순한 데이터 전달 객체를 만들더라도 필드, 생성자, getter, `equals()`, `hashCode()`, `toString()` 등을 직접 구현하거나 Lombok과 같은 라이브러리를 사용해야 했다. Record를 사용하면 이러한 코드를 컴파일러가 자동으로 생성해 준다.

## 1. Record 타입이란?

Oracle의 Java Language Specification에서는 Record를 다음과 같이 정의한다.

> A record class is a restricted kind of class that defines a simple aggregate of values.
>

Record는 여러 값을 하나로 묶어 표현하기 위한 특별한 형태의 클래스다. 일반 클래스와 달리 객체가 가질 데이터를 Record 선언부에서 명시적으로 정의하며, 해당 데이터를 관리하는 데 필요한 기본 메서드를 자동으로 제공한다.

예를 들어 사용자 정보를 담는 Record는 다음과 같이 선언할 수 있다.

```java
public record User(
    Long id,
    String name,
    String email
) {
}
```

괄호 안에 선언된 id, name, email을 **Record Component**라고 한다. 위 코드를 컴파일하면 다음 요소들이 자동으로 생성된다.

- 각 Component에 대응하는 `private final` 필드
- 모든 Component를 초기화하는 생성자(Canonical Constructor)
- 각 Component의 값을 반환하는 접근 메서드(Accessor Method)
- `equals()`, `hashCode()`, `toString()`

따라서 별도의 생성자나 getter 없이도 객체를 생성하고 데이터를 조회할 수 있다.

```java
User user = new User(1L, "Kim", "kim@example.com");

System.out.println(user.id());
System.out.println(user.name());
System.out.println(user.email());
System.out.println(user);
```

일반 클래스에서는 보통 `getName()`과 같은 getter를 사용하지만, Record가 자동으로 생성하는 접근 메서드는 Component 이름과 동일한 `name()` 형태다.

Oracle의 `java.lang.Record` API 문서에서는 Record를 다음과 같이 설명한다.

> A record class is a shallowly immutable, transparent carrier for a fixed set of values.
>

즉, Record는 **정해진 값들을 저장하고 전달**하며, 선언된 Component의 참조를 변경할 수 없는 구조다. 단, 참조하는 객체의 내부 상태까지 변경할 수 없는 것은 아니므로 **완전한 불변 객체와는 구분**해야 한다.

## 2. 일반 Class와의 차이

Record는 Class와 별개의 타입이 아니라, 몇 가지 제약이 추가된 특수한 클래스다.

| 구분 | Class | Record |
| --- | --- | --- |
| 주요 목적 | 상태와 행동을 가진 객체 정의 | 데이터 저장 및 전달 |
| 필드 선언 | 자유롭게 선언 가능 | Record Component로 정의 |
| 필드 변경 | 가능 | Component 필드는 `final` |
| 생성자 | 직접 작성 | 자동 생성 |
| Getter | 직접 작성 | 자동 생성 |
| equals / hashCode | 직접 구현 필요 | 자동 구현 |
| toString | 직접 구현 필요 | 자동 구현 |
| 클래스 상속 | 가능 | 불가능 |
| 인터페이스 구현 | 가능 | 가능 |
| 추가 인스턴스 필드 | 가능 | 불가능 |
| 인스턴스 메서드 | 선언 가능 | 선언 가능 |

### 일반 Class로 구현한 경우

```java
import java.util.Objects;

public final class User {

    private final Long id;
    private final String name;

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        return Objects.equals(id, user.id)
            && Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", name=" + name + "]";
    }
}
```

### Record로 구현한 경우

```java
public record User(Long id, String name) {
}
```

Record의 상태가 선언부에 정의된 Component로만 구성되도록 제한하기 때문에, 객체의 데이터 구조가 보다 명확하게 드러난다. 또한 Record는 암묵적으로 `final` 클래스이며 `java.lang.Record`를 상속한다. 따라서 다른 클래스를 상속하거나, 다른 클래스가 해당 Record를 **`상속하도록 만드는 것은 불가능`**하다. 반면 인터페이스 구현은 가능하다.

## 3. Record 사용 예시

### 3.1. DTO로 사용

DTO는 계층 사이에서 데이터를 전달하는 역할을 하므로, 별도의 상태 변경 기능이 필요하지 않은 경우가 많다.

```
public record UserResponse(
    Long id,
    String name,
    String email
) {
}
```

### 3.2. Compact Constructor를 이용한 유효성 검증

Record에서도 생성자 내부에 검증 로직을 작성할 수 있다. 이를 위해 **Compact Constructor**를 사용할 수 있다.

```
public record User(Long id, String name) {

    public User {
        if (id == null) {
            throw new IllegalArgumentException("id는 필수입니다.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 필수입니다.");
        }
    }
}
```

Compact Constructor에서는 **생성자의 매개변수 목록을 다시 작성하지 않아도 된다.** 검증 로직이 실행된 후 각 Component에 값을 할당하는 과정은 컴파일러가 처리한다.

### 3.3. 메서드 추가

Record도 클래스이므로 필요한 메서드를 선언할 수 있다.

```
public record Rectangle(double width, double height) {

    public double area() {
        return width * height;
    }
}
```

다만 Component 외에 **`별도의 인스턴스 필드를 추가할 수 없으며, 생성된 Component 필드의 값을 변경할 수 없다는 제약이 있다. 다만 static 필드는 가능하다.`**

```java
public record User(Long id, String name) {

    private int loginCount; // 컴파일 오류
    
    private static final String TYPE = "USER" // 가능
}
```

## 4. Record의 장단점

### 장점

1. 반복적인 코드 감소

2. 데이터 구조가 명확함

3. 값 기반 비교 지원

- 일반 클래스에서 `equals()`를 재정의하지 않으면 기본적으로 객체의 동일성을 비교한다. 반면 Record는 Component 값을 기준으로 비교하는 `equals()`를 자동으로 제공한다.

```java
User user1 = new User(1L, "Kim");
User user2 = new User(1L, "Kim");

System.out.println(user1.equals(user2)); // true
```

4. Component 재할당 방지

Record의 Component 필드는 `private final`로 선언된다. 생성 이후 필드에 새로운 값을 할당할 수 없으므로, 생성 시점의 데이터를 유지하는 객체를 정의하기에 적합하다.

### 단점

1. 클래스 상속이 불가능함

Record는 암묵적으로 `final`이므로 상속을 이용해 클래스를 확장할 수 없다. 상속 구조가 필요한 도메인 모델에는 적합하지 않다.

2. 별도의 인스턴스 필드를 추가할 수 없음

Record의 인스턴스 상태는 선언부의 Component로 결정된다. 객체 내부에서 추가적인 상태를 관리해야 한다면 일반 클래스를 사용하는 편이 적절하다.

3. 완전한 불변 객체는 아님

Record의 Component 필드는 `final`이지만, 해당 필드가 참조하는 객체까지 불변인 것은 아니다.

```java
public record User(
    String name,
    List<String> roles
) {
}

List<String> roles = new ArrayList<>();
roles.add("USER");

User user = new User("Kim", roles);
user.roles().add("ADMIN"); // 정상 실행
```

`roles` 필드가 참조하는 List 자체를 다른 List로 교체할 수는 없지만, List 내부의 요소는 변경할 수 있기 때문에, 변경 가능한 객체를 Component로 사용하는 경우에는 주의해야 한다. 아래와 같이 방어적 복사를 적용할 수 있다.

```java
public record User(
    String name,
    List<String> roles
) {

    public User {
        roles = List.copyOf(roles);
    }
}
```

## 정리

Record는 데이터 저장과 전달을 목적으로 도입된 Java의 특수한 클래스다.

일반 클래스와 달리 Component를 선언하면 생성자, 접근 메서드, `equals()`, `hashCode()`, `toString()`이 자동으로 생성된다. 또한 Component 필드가 `final`로 선언되고 추가 인스턴스 필드를 허용하지 않으므로, 데이터의 구성을 명확하게 표현할 수 있다.

다만 Record가 모든 클래스를 대체하는 것은 아니다. 데이터 전달이 주목적인 DTO나 값 객체에는 적합하지만, 상태 변경이나 상속이 필요한 객체에는 일반 클래스를 사용하는 것이 좋다.

결국 Record와 일반 Class를 구분하는 기준은 코드의 길이가 아니라 객체의 역할이라고 볼 수 있다.

## 참고 문서

- https://docs.oracle.com/javase/specs/jls/se27/html/jls-8.html#jls-8.10
- https://docs.oracle.com/en/java/javase/27/docs/api/java.base/java/lang/Record.html
- https://docs.oracle.com/en/java/javase/25/language/records.html