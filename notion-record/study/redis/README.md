# Redis

[1. Redis 개념](1.-redis-개념/README.md)

[RedisTemplate 이용](redistemplate-이용/README.md)

https://github.com/ssjjaa-algo/springboot-redis

[Redis](redis/README.md)

## @EnableCaching

- 해당 어노테이션을 붙여줌으로써 캐싱 기능을 활성화
- 이제 DB에서 데이터를 읽어오는 부분이 필요할 때, @Cacheable 등의 어노테이션 등을 이용해 캐싱 기능을 이용할 수 있음.

### public RedisConnectionFactory redisConnectionFactory()

- Springboot 2.0 이상은 **기본적으로 Lettuce를 사용**
- 생략해도 무방하나, 명확하게 의미를 알려주기 위해 사용합니다.
    - LettuceConnectionFactory
- 생성한 LettuceConnectionFactory에서 사용할 Redis의 HOST, PORT, PW를 설정
    - **PW는 테스트 환경이므로 생략했으나, 반드시 설정해 주는게 좋습니다.**

### public RedisTemplate

<aside>
💡 추후 업데이트를 위해 선언해둔 것, 나중에 내용 추가하겠습니다.

</aside>

### public CacheManager cacheManager()

- mainRedisFactory에서 생성한 connectionFactory를 주입해줍니다.
- Value Serialize를 객체로 하기 때문에, **`GenericJackson2JsonRedisSerializer`**를 사용합니다.
    - ObjectMapper를 안에 **추가로 선언하고 안하고의 차이가 존재**합니다.
- entryTtl에 Cache를 유지할 시간을 설정합니다.

<aside>
💡 **`GenericJackson2JsonRedisSerializer` 에 대해 짚고 가겠습니다.**

</aside>

### GenericJackson2JsonRedisSerializer

- ObjectMapper를 선언하여 생성자의 변수로 넣어주지 않을 경우, class 타입까지 같이 저장됩니다. MSA 환경이라면, 모든 패키지나 클래스를 똑같이 해주지 않는 이상 실패하는 지점이 분명 발생합니다.
- 선언하지 않았을 경우,
    
    ```json
    "java.util.ArrayList",
    [{"@class":"com.example.redis.test.domain.Board","id":12,"userId":"강병선","userName":"1"},
    {"@class":"com.example.redis.test.domain.Board","id":13,"userId":"강병선","userName":"12"}
    ```
    
- ObjectMapper를 선언해줬을 경우
    
    ```json
    [{"id":12,"userId":"강병선","userName":"1"},
    {"id":13,"userId":"강병선","userName":"12"}
    ```
    
- 선언해주는 것이 더 관리가 용이하다고 판단하여 선언해주었습니다.
- **GenericJackson2JsonRedisSerializer의 생성자 형식을 참고하시면 도움이 됩니다.**

[[Spring] 캐시(Cache) 추상화와 사용법(@Cacheable, @CachePut, @CacheEvict)](https://mangkyu.tistory.com/179)

# Spring이 제공하는 캐시(Cache) 추상화

- AOP 방식으로 메소드에 서비스 적용 기능
    - 캐시 관련 로직을 비즈니스 로직으로부터 분리
    - 손쉬운 캐시 기능 방식

# 어노테이션 설명

- @EnableCaching
    - 캐시 기능을 사용하기 위해 선언해줍니다.
- @Cacheable
    - **자주 변하지 않고, 많은 조회가 필요한 곳에 캐싱 처리는 적합합니다.**
    
    ```java
    @Cacheable(value="boardList",key="#id")
        public List<Board> getAll(String id) {
            return testRepository.findAllByUserId(id);
        }
    ```
    
    - 캐시에 데이터가 없을 경우 testRepository를 실행해서 캐시에 데이터를 추가
    - 캐시에 데이터가 있다면 그대로 반환
    - 최초 호출 시는 testRepository를 호출하여 값을 넣어줄 것이고
    - 그 이후에는 Cache에 있다면 찾아낼 것입니다.
    - 여기서 key값은 id이고, value는 boardList인데 redis-cli에서 명령어로 확인 가능합니다.
        - GET boardList::id

- @CacheEvict
    - 캐시의 제거를 위해 사용
        - 값의 변화가 생긴다면 기존 캐시를 제거해줘야 할 때, 사용할 수 있습니다.
    - 메서드의 키에 해당하는 캐시를 제거합니다.
    
    ```java
    @CacheEvict(value="boardList",key="#id")
        public void regist(Board board, String id) {
            testRepository.save(board);
    
        }
    ```
    

- @CachePut
    - 캐시에 값을 저장하는 용도로만 사용합니다.
    - **항상 메서드의 로직을 실행합니다.**
        - 이를 통해 DB와의 일관성을 유지할 수 있습니다.
