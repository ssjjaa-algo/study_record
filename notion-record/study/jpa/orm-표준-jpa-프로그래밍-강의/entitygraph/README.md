# EntityGraph

태그: spring data jpa

- JPQL 없이 사용
    - 내부적으로는 어차피 fetch join이라 다를거 없음.
    
    ```java
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();1
    ```
