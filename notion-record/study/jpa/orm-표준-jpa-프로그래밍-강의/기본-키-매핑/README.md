# 기본 키 매핑

# 어노테이션

- 직접 할당
    - @Id만 사요
- 자동 생성(@GeneratedValue)
    - IDENETITY
        - 기본 키 생성을 데이터베이스에 위임 (MySQL의 AUTO_INCREMENT)
        - db에 insert를 해야 다음 값을 확인할 수 있음
        - em.persit()시 무조건 insert가 나간다.
    - SEQUENCE
        - IDENTITY와 마찬가지로 next_val을 sequence에서 가져와야함 (쿼리가 나감)
        - **`allocationSize`**를 이용할 수 있다. (기본값 50), 쉽게 얘기해 미리 50개를 땡겨오는 것)
            - 동시성 이슈 없이 다양한 문제를 해결해준다고 한다… 오?
        
        ```java
        @Entity
        @SequenceGenrator (
        	name = "MEMBER_SEQ_GENERATOR",
        	sequenceName = "MEMBER_SEQ", // 매핑할 데이터베이스 시퀀스 이름
        	initialValue = 1, allocationSize = 1)
        public class Member {
        	
        	@Id
        	@GeneratedValue(strategy = GenerationType.SEQUENCE,
        				generator = "MEMBER_SEQ_GENERATOR")
        	private Long id;
        }
        
        ```
        
    - TABLE
        - 키 생성 전용 테이블을 만들어서 데이터베이스 시퀀스를 흉내낸다.
        
        ```java
        @Entity
        @TableGenerator(
        	name = "MEMBER_SEQ_GENERATOR",
        	sequenceName = "MY_SEQUENCES", // 매핑할 데이터베이스 시퀀스 이름
        	initialValue = 1, allocationSize = 1)
        public class Member {
        	
        	@Id
        	@GeneratedValue(strategy = GenerationType.TABLE,
        				generator = "MEMBER_SEQ_GENERATOR")
        	private Long id;
        }
        ```
        
    - AUTO
