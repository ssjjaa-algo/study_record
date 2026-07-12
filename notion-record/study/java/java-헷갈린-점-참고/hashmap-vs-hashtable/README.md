# HashMap vs Hashtable

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {
}
```

- HashMap에 대한 문법이 궁금하다면 아래 블로그를 참고

[[Collection] 이것만 알면 해시맵(HashMap) 정복 가능 - HashMap의 특징, 사용법 예제](https://reakwon.tistory.com/151)

# Hashtable

- 컬렉션 프레임워크가 만들어지기 이전부터 존재했다.
- 설계를 변경해서 남겨둔 것
    - Vector도 동일한 의미임.
    - ArrayList와 HashMap을 사용하는 것이 좋다.
- 일반적으로 HashMap과 사용법이 거의 동일하다.

# HashMap vs Hashtable

- Thread-safe
    - Hashtable이 Thread-safe,
    - HashMap은 그렇지 않음.
- Null값 허용 여부
    - Hashtable은 key값에 null 허용하지 않는다.
    - HashMap은 허용한다.
- HashMap은 보조해시를 사용한다.
    - 해시 충돌이 상대적으로 덜 발생해 성능상의 이점이 있다는 것.
