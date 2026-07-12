# 자바 컨테이너

[[JAVA] 컨테이너 - 기초](http://asuraiv.blogspot.com/2015/05/java-container.html)

# 기존 배열(array)

- 크기가 정해진다
    - 정해지면 바꿀 수 없다.
- 필요로 하는 객체가 얼마인지 예상이 되면 배열을 선언할 수 있겠으나, 예상이 매우 힘들다.

# Java.util

- 컨테이너
    - List, Set, Queue, Map

- Generics
    - ArrayList list = new ArrayList<>();
        - 아무 Type이나 담을 수 있다.
        - 아무거나 담기 때문에, list에서 뽑아 어떤 메서드를 호출할 때
        - 해당 Type의 객체가 메서드를 가지고 있지 않은 경우 Exception
    - ArrayList<Node> list = new ArrayList<Node>();
        - Type의 제한
        - 별도로 다른 것을 확인할 작업이 필요 없다.
    - 다른 컨테이너도 마찬가지
