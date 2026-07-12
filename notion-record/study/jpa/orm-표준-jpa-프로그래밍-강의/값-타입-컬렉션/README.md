# 값 타입 컬렉션

# 값 타입을 하나 이상 저장할 때

- 컬렉션을 저장하기 위해 별도의 테이블 설정
- 영속성 전이 + 고아 객체 제거 기능을 필수로 가진다.
    
    ```java
    @ElementCollection
    @CollectionTable(name = "table_name", joinColumns = 
    		@JoinColumn(name = "PK")
    )
    @Column(name = "지정")
    private Set<String> examins = new HashSet<>();
    ```
    

- 값 타입 지정은 new 키워드로 갈아끼우기.

## 부모 변경시

- 부모를 변경하면 Collection에 있는 것들이 다 지워지고, 다시 써진다..?

<aside>
💡 일대 다로 풀자.

</aside>
