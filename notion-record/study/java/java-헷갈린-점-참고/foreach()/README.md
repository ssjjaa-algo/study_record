# forEach()

## 개선된 for문

```java
for (String s : empNames) {
		System.out.println(s));
	}
```

## 자바 8

- @FunctionalInterface
    - 추상 메소드가 하나인 인터페이스

```java
public interface Consumer<T> {
		void accept(T t)
}

void forEach(Consumer<T> action)

empNames.forEach((s) -> System.out.println(s));
```

## 코드정리

```java
package Baekjoon;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

class OjcConsumer implements Consumer<String> {

    @Override
    public void accept(String s) {
        System.out.println("Consumer Impl :: " + s);
    }
}
public class Main {
    public static void main(String[] args) {

        String[] myArr = {"1","2","3"};
        List<String> list = (List) Arrays.asList(myArr);

        // Consumer는 자바에서 제공하는 추상메소드가 하나 있는 함수형 인터페이스
        list.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("forEach :: " + s);
            }
        });

        // Consumer Interface를 구현한 클래스를 new 후 forEach에 사용
        list.forEach(new OjcConsumer());

        /**
         * 메서드를 람다식으로 표현하면 메서드의 이름과 반환값이 필요없다
         * 람다식을 '익명 함수'라고 부른다.
         * forEach 안에는 Consumer Interface 타입이 들어와야 하는데
         * accept 추상 메서드 하나만 있으면 되기 때문에
         * 람다식으로 구현한 아래 괄호 안의 메서드는
         * accept 추상 메서드의 구현체인 익명 함수
         */
        list.forEach(s -> System.out.println("람다식 :: " + s));

        /**
         * 더블 콜론 연산자
         * 이름만으로 특정 메서드 호출
         * 타겟 레퍼런스는 :: 앞에 놓고 메소드명은 :: 뒤에 놓는다.
         */
        list.forEach(System.out::println);

    }
}
```
