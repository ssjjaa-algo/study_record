# stream 사용법

# Part 1. Stream 기본 사용법

### 1. Stream은 이렇게 쓴다

```java
컬렉션.stream()
     .중간연산()
     .중간연산()
     .최종연산();
```

공식문서 기준으로 `Stream`은 요소들의 시퀀스에 대해 순차 또는 병렬 aggregate operation을 지원하는 인터페이스다. Stream 연산은 보통 **source → intermediate operation → terminal operation** 형태의 pipeline으로 구성된다. ([Oracle Docs](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html?utm_source=chatgpt.com))

---

## 2. 기존 방식과 비교

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

List<Integer> result = new ArrayList<>();

for (int number : numbers) {
    if (number >= 3) {
        result.add(number * 10);
    }
}

System.out.println(result);
```

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

List<Integer> result = numbers.stream()
        .filter(number -> number >= 3)
        .map(number -> number * 10)
        .toList();

System.out.println(result);
```

결과:

```
[30, 40, 50]
```

---

# 3. Stream 사용 구조

| 단계 | 코드 | 의미 |
| --- | --- | --- |
| Source | `numbers.stream()` | 데이터 원본에서 Stream 생성 |
| Intermediate Operation | `.filter(number -> number >= 3)` | 조건에 맞는 값만 통과 |
| Intermediate Operation | `.map(number -> number * 10)` | 값을 다른 값으로 변환 |
| Terminal Operation | `.toList()` | 최종 결과를 리스트로 반환 |

---

# 4. Stream에서 가장 먼저 익혀야 하는 메서드

| 메서드 | 역할 | 예시 |
| --- | --- | --- |
| `stream()` | 컬렉션을 Stream으로 변환 | `list.stream()` |
| `filter()` | 조건에 맞는 값만 남김 | `.filter(x -> x > 10)` |
| `map()` | 값을 다른 값으로 변환 | `.map(x -> x * 2)` |
| `toList()` | 결과를 List로 수집 | `.toList()` |
| `forEach()` | 각 요소를 처리 | `.forEach(System.out::println)` |
| `count()` | 개수 계산 | `.count()` |
| `anyMatch()` | 하나라도 조건 만족 여부 | `.anyMatch(x -> x > 10)` |
| `allMatch()` | 모두 조건 만족 여부 | `.allMatch(x -> x > 0)` |
| `findFirst()` | 첫 번째 요소 찾기 | `.findFirst()` |

---

# 5. 가장 기본 패턴 5개

## 패턴 1. 조건 필터링

```java
List<String> names = List.of("Kim", "Lee", "Park", "Choi");

List<String> result = names.stream()
        .filter(name -> name.length() >= 4)
        .toList();

System.out.println(result);
```

결과:

```
[Park, Choi]
```

---

## 패턴 2. 값 변환

```java
List<String> names = List.of("Kim", "Lee", "Park");

List<Integer> result = names.stream()
        .map(name -> name.length())
        .toList();

System.out.println(result);
```

결과:

```
[3, 3, 4]
```

---

## 패턴 3. 필터링 후 변환

```java
List<Integer> prices = List.of(5000, 12000, 30000, 7000);

List<Integer> result = prices.stream()
        .filter(price -> price >= 10000)
        .map(price -> price / 1000)
        .toList();

System.out.println(result);
```

결과:

```
[12, 30]
```

---

## 패턴 4. 개수 세기

```java
List<Integer> prices = List.of(5000, 12000, 30000, 7000);

long count = prices.stream()
        .filter(price -> price >= 10000)
        .count();

System.out.println(count);
```

결과:

```
2
```

---

## 패턴 5. 하나라도 만족하는지 확인

```java
List<Integer> prices = List.of(5000, 12000, 30000, 7000);

boolean exists = prices.stream()
        .anyMatch(price -> price >= 30000);

System.out.println(exists);
```

결과:

```
true
```

---

# 6. 첫 번째 실습 문제

## 문제 1

아래 코드에서 `scores` 중 **80점 이상인 점수만 골라서 List로 출력**하라.

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> scores = List.of(70, 85, 90, 60, 100, 75);

        // 여기에 Stream 코드 작성
    }
}
```

## 기대 결과

```
[85, 90, 100]
```

## 사용해야 할 메서드

| 메서드 | 이유 |
| --- | --- |
| `stream()` | List를 Stream으로 변환 |
| `filter()` | 80점 이상만 통과 |
| `toList()` | 결과를 List로 변환 |

---

# 7. 답안

```java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Integer> scores = List.of(70, 85, 90, 60, 100, 75);

        List<Integer> result = scores.stream()
                .filter(score -> score >= 80)
                .toList();

        System.out.println(result);
    }
}
```

결과:

```
[85, 90, 100]
```

---

# 8. 지금 기준으로 기억할 것

```java
scores.stream()
      .filter(score -> score >= 80)
      .toList();
```

이 코드는 이렇게 읽으면 된다.

```
scores에서 Stream을 만들고
→ 80점 이상인 score만 남기고
→ 그 결과를 List로 만든다
```

즉, Stream의 기본 사용법은 이것이다.

```java
데이터.stream()
    .걸러내고()
    .바꾸고()
    .최종결과();
```
