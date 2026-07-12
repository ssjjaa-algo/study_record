# 문자열 클래스

# String

- 저장 영역
    - Heap
    - Constant String Pool
- 불변
    - **변하지 않는 문자열을 자주 읽어들이는 경우** 사용하면 유리
    - 추가, 수정, 삭제 빈번 → Heap memory 부족 유도
        - **`성능에 악영향`**
    
    ```java
    String str = "first"
    str = str + "second"
    
    // str은 최초에 불변성으로 first의 값을 가지고 있음
    // 이후 str + "second" 작업을 통해 str은 first second로 변경
    // String은 불변이기 때문에 문자열을 수정하는 시점에
    // 새로운 인스턴스가 생성된다.
    // first는 GC의 대상이 되어 사라지게 된다.
    ```
    
- Thread-Safe
    - **`불변`**이기 때문에 **동기화를 신경 쓸 필요 없다.**

# StringBuffer & StringBuilder

## 공통점

- 저장영역
    - Heap
- 가변
    - **문자열 변경이 자주 일어나는 경우** 유리

## 차이점

- Thread-Safe(StringBuffer)
- Not Thread-Safe(StringBuilder)

<aside>
💡 멀티쓰레드 환경에서 StringBuffer
싱글쓰레드 환경에서 StringBuilder

</aside>
