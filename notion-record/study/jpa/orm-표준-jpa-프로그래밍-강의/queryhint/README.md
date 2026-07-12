# QueryHint

- readOnly로 사용하는 것.
    - jpa는 find를 통해 어떤 엔티티를 가져올 때 스냅샷을 기록한다.
    - 스냅샷과, 원본이 둘 다 위치해있는 상태이므로 수정할 것이 아니라면 스냅샷 기록 필요 없다.
        - 스냅샷이 메모리 어딘가에 또 상주된다..
- 따라서 아래와 같이 처리하면 스냅샷을 안딴다.
    
    ```java
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);
    ```
    
    - 그리고 변경감지가 일어나지 않아서 setUsername(”member2”)같이 변경해도
        - update 쿼리가 나가지 않는다.

- 최적화가 되긴 하는데, 많이 되진 않는다.
    - 트래픽이 엄청나게 많으면 고려하는 정도..?
    - 이점이 있다고 판단하면 설정하는 정도.
