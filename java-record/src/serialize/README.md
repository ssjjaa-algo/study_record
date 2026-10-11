# Java 직렬화(Serialization) 정리

Java에서 객체를 파일로 저장하거나 다른 시스템으로 전달하려면 객체의 데이터를 외부에서 처리할 수 있는 형태로 변환해야 한다. 이때 사용하는 개념이 직렬화(Serialization)다.

Java는 `Serializable` 인터페이스와 `ObjectOutputStream`, `ObjectInputStream`을 통해 객체를 바이트 스트림으로 변환하고 복원하는 기능을 제공한다.

## 1. 직렬화란?

직렬화(Serialization)는 **객체의 상태를 저장하거나 전송할 수 있는 형태로 변환하는 과정**이다. 반대로 직렬화된 데이터를 다시 객체로 복원하는 과정을 역직렬화(Deserialization)라고 한다.

Oracle의 Java Object Serialization Specification에서는 직렬화를 객체의 상태를 저장하고, 이후 해당 객체를 재구성할 수 있도록 하는 메커니즘으로 설명한다. Java의 기본 객체 직렬화는 객체의 상태를 바이트 스트림으로 변환한다.

```
[직렬화]
Java Object → Byte Stream

[역직렬화]
Byte Stream → Java Object
```

예를 들어 `User` 객체를 프로그램이 종료된 이후에도 다시 사용해야 한다면 객체의 데이터를 파일에 저장할 수 있다. 파일에 저장된 데이터를 읽어 객체를 복원하는 것이 역직렬화다.

직렬화라는 개념은 Java에만 존재하는 것은 아니다. 객체를 JSON이나 XML로 변환하는 것도 넓은 의미에서는 직렬화에 해당한다. 다만 Java에서 `Serializable`을 이용하는 방식은 Java가 제공하는 고유한 바이너리 직렬화 방식이다.

## 2. Java 직렬화의 동작 방식

`java.io.Serializable` 인터페이스를 구현한다.

```java
public class User implements Serializable {

    private Long id;
    private String name;
}
```

`Serializable`은 메서드가 없는 **마커 인터페이스(Marker Interface)** 다. 따라서 구현해야 하는 메서드는 없으며, 해당 클래스가 Java의 객체 직렬화를 지원한다는 사실을 나타내는 역할을 한다.

직렬화와 역직렬화에는 다음 클래스들이 사용된다.

| 클래스 | 역할 |
| --- | --- |
| `Serializable` | 객체 직렬화 가능 여부 표시 |
| `ObjectOutputStream` | 객체를 바이트 스트림으로 직렬화 |
| `ObjectInputStream` | 바이트 스트림을 객체로 역직렬화 |
| `FileOutputStream` | 바이트 데이터를 파일에 기록 |
| `FileInputStream` | 파일에서 바이트 데이터 읽기 |

### 직렬화 대상이 되는 필드

Java의 기본 직렬화 방식에서는 객체의 인스턴스 필드 중 `static`과 `transient`가 아닌 필드가 직렬화 대상이 된다.

```java
public class User implements Serializable {

    private Long id;              // 직렬화 대상
    private String name;          // 직렬화 대상
    private transient String token; // 직렬화 제외

    private static int count;     // 직렬화 제외
}
```

`static` 필드는 인스턴스가 아닌 클래스에 속하는 값이기 때문에 기본 직렬화 대상에서 제외된다.

`transient`는 특정 필드를 직렬화 대상에서 제외하기 위한 키워드다. 역직렬화 시 해당 필드가 별도로 복원되지 않으면 참조 타입은 `null`, 숫자형은 `0`, `boolean`은 `false` 등의 기본값을 갖는다.

또한 직렬화는 객체 하나의 필드만 처리하는 것이 아니다. 객체가 다른 객체를 참조하고 있다면 해당 객체도 함께 직렬화될 수 있다. 이때 참조된 객체가 직렬화를 지원하지 않으면 `NotSerializableException`이 발생할 수 있다.

## 3. 직렬화 및 역직렬화 예시

```java
import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String name;
    private transient String token;

    public User(Long id, String name, String token) {
        this.id = id;
        this.name = name;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }
}
```

### 객체 직렬화

```java
User user = new User(1L, "Kim", "token123");

try (ObjectOutputStream oos =
         new ObjectOutputStream(
             new FileOutputStream("user.ser"))) {

    oos.writeObject(user);
}
```

`ObjectOutputStream`의 `writeObject()`를 호출하면 객체가 직렬화되어 `user.ser` 파일에 기록된다.

### 객체 역직렬화

```java
try (ObjectInputStream ois =
         new ObjectInputStream(
             new FileInputStream("user.ser"))) {

    User user = (User) ois.readObject();

    System.out.println(user.getName()); // Kim
    System.out.println(user.getToken()); // null
}
```

`name`은 정상적으로 복원되지만 `token`은 `transient`로 선언되어 직렬화되지 않았으므로 `null`이 반환된다. 이 과정에서 `readObject()`는 스트림에 기록된 객체의 정보를 이용해 인스턴스를 복원한다.

### 3.4. serialVersionUID의 역할

예시에서 사용한 다음 필드는 직렬화 버전을 식별하는 데 사용된다.

```java
private static final long serialVersionUID = 1L;
```

`serialVersionUID`는 직렬화된 데이터와 현재 클래스가 직렬화 관점에서 호환되는지 확인하기 위한 버전 식별자다. 일반적인 `Serializable` 클래스에서 직렬화 당시와 역직렬화 당시의 `serialVersionUID`가 다르면 `InvalidClassException`이 발생한다.

명시적으로 선언하지 않으면 JVM이 클래스 구조를 바탕으로 값을 계산한다. 그러나 **클래스의 변경이나 컴파일러 구현 차이에 따라 계산 결과가 달라질 수 있어** Oracle에서는 직접 선언할 것을 권장한다.

### 3.5. Record

```java
public record User(
    Long id,
    String name
) implements Serializable {
}
```

다만 Record는 일반 클래스와 역직렬화 방식에 차이가 있다. 일반적인 `Serializable` 객체는 역직렬화 과정에서 해당 클래스의 생성자를 호출하지 않고 직렬화된 필드 값을 직접 복원한다. 반면 Record는 **Canonical Constructor를 호출하여 객체를 생성하고, 직렬화된 데이터를 각 Record Component에 전달한다.**

또한 `serialVersionUID`를 처리하는 방식에도 차이가 있다. 일반 클래스는 직렬화된 데이터와 현재 클래스의 `serialVersionUID`가 일치해야 하지만, Record는 이러한 일치 여부 검사가 면제된다. Record의 기본 `serialVersionUID`는 `0L`이며, 명시적으로 다른 값을 선언할 수도 있다.

이는 Record가 역직렬화 과정에서 **`Class Descriptor`**(클래스 이름, 필드 이름과 타입, 직렬화 버전 등의 정보)를 참고하여 저장된 필드와 현재 Record Component를 대응시키고, Canonical Constructor를 통해 객체를 복원하기 때문이다.

예를 들어 기존 Record에 새로운 Component가 추가되었다면, 이전 직렬화 데이터에 존재하지 않는 Component에는 `null`이나 `0`과 같은 기본값이 전달된다. 따라서 일부 구조 변경이 발생하더라도 기존 데이터를 복원할 수 있다.

다만 `serialVersionUID` 검사가 면제된다고 해서 모든 구조 변경이 호환되는 것은 아니다. Component의 타입이 호환되지 않게 변경되거나 Canonical Constructor의 유효성 검증에 실패하면 역직렬화가 실패할 수 있다.

# 4. Java 직렬화와 JSON 직렬화 비교

| 구분 | Java 기본 직렬화 | JSON 직렬화 |
| --- | --- | --- |
| 데이터 형식 | Java 전용 바이너리 형식 | 텍스트 형식 |
| 기본 구현 | `Serializable` | Jackson 등 라이브러리 활용 |
| 가독성 | 낮음 | 높음 |
| 다른 언어와 호환성 | 낮음 | 높음 |
| 클래스 구조와의 결합 | 강함 | 상대적으로 낮음 |
| 주요 활용 | Java 객체 저장 및 복원 | REST API, 시스템 간 데이터 교환 |

예를 들어 Spring Boot에서 API 응답을 JSON으로 변환하는 것은 일반적으로 Jackson과 같은 라이브러리가 담당한다. 이 경우 DTO가 반드시 `Serializable` 인터페이스를 구현할 필요는 없다. 따라서 단순히 데이터를 외부로 전달하려는 목적이라면 Java 기본 직렬화보다는 JSON 같은 데이터 교환 형식이 더 적합할 수 있다.

## 정리

직렬화는 객체의 상태를 저장하거나 전송할 수 있는 형태로 변환하는 과정이며, 역직렬화는 변환된 데이터를 다시 객체로 복원하는 과정이다.

Java는 `Serializable` 인터페이스와 `ObjectOutputStream`, `ObjectInputStream`을 이용한 객체 직렬화 기능을 제공한다. 이를 통해 객체 상태를 파일에 저장하거나 객체 간 참조 관계를 유지하면서 복원할 수 있다.

다만 Java의 기본 직렬화 방식은 클래스 구조와 밀접하게 연결되어 있고, 역직렬화 과정에서 보안 문제가 발생할 수 있다는 점에 주의해야 한다.

결국 **직렬화 자체는 데이터를 저장하고 전달하기 위한 일반적인 개념이며, Java의 `Serializable`은 이를 구현하는 여러 방식 중 하나**다.

---

## 참고 문서

- Oracle Java SE 27 — Serializable API
- Oracle — Java Object Serialization Specification
- Oracle — ObjectOutputStream API
- Oracle — ObjectInputStream API
- Oracle — Java Serialization Filters