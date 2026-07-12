# 함수형 인터페이스

## 정의

- 오직 하나의 추상 메서드를 지정
- 함수형 인터페이스의 추상 메서드는 람다 표현식의 시그니처를 묘사.
    - 추상 메서드 시그니처 = **함수 디스크립터**

## 종류

- java.util.function.Predicate<T>
    - boolean test
- java.util.function.Consumer<T>
    - void accept
- java.util.function.Function<T,R>
    - 제네릭 형식 T를 인수로 받아서 제네릭 형식 R 객체를 반환한다
    - R apply

## 형식 검사, 형식 추론, 제약

- 람다 표현식을 이해하기 위해 람다의 실제 형식을 파악한다.

### 형식 검사

- 람다가 사용되는 **`context`**를 이용해서 람다의 type을 추론.
    - 어떤 context에서 기대되는 람다 표현식의 형식 = 대상 형식
    
    ```java
    List<Apple> heavierThan150g = 
    filter(inventory, (Apple apple) -> apple.getWeight() > 150);
    ```
    
    - filter 메서드의 선언을 확인한다
    - 두 번째 파라미터로 Predicate<Apple> 형식(대상 형식)을 기대
    - Predicate<Apple>은 test 추상 메서드를 정의하는 함수형 인터페이스
    - test 메서드가 Apple을 받아 boolean을 반환하는 함수 디스크립터를 묘사
    - filter 메서드로 전달된 인수는 이와 같은 요구사항을 만족해야 한다.

### 같은 람다, 다른 함수형 인터페이스

- 대상 형식 특징 때문에 같은 람다 표현식이더라도 호환되는 추상 메서드를 가진 다른 함수형 인터페이스로 사용될 수 있다.
    - 아래 두 인터페이스는 인수를 받지 않고 제네릭 형식 T를 반환하는 함수를 정의
    
    ```java
    Callable<Integer> c = () -> 42;
    PrivilegedAction<Integer> p = () -> 42;
    ```
    
    - Callable<Integer>, PrevilegedAction<Integer> 각각 다른 대상 형식

### 형식 추론

- 람다 표현식이 사용된 context(대상 형식)을 이용해서 람다 표현식과 관련된 함수형 인터페이스를 추론
    - 대상 형식을 이용해서 함수 디스크립터를 알 수 있다.
        - 즉 컴파일러는 람다의 시그니처도 추론할 수 있다.
        
        ```java
        Comparator<Apple> c = 
        (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()); // 추론하지 않음
        
        Comparator<Apple> c = 
        (a1, a2) -> a1.getWeight().compareTo(a2.getWeight()); // 형식을 추론함
        ```
