# 웹 애플리케이션과 싱글톤

### 

### **싱글톤 패턴을 적용하지 않는 경우**

![](https://blog.kakaocdn.net/dn/boTCec/btsBUKAxuwb/rT26BwmEpOEkyLS3p6TlV1/img.png)

동일한 요청에 대해 같은 객체를 계속 생성

- 동일한 요청에 대해 계속 객체를 new 해서 생성 -> 낭비.

### **싱글톤 패턴 적용 예시**

```java
package hello.core.singleton;

public class SingletonService {

 private static final SingletonService instance;

 public static SingletonService getInstance() {
		 if (instance == null) {
			instance = new SingletonService();
		}
		 return instance;
 }

 private SingletonService() {

 }

 public void logic() {
 System.out.println("싱글톤 객체 로직 호출");
 }

```

1. static 영역에 객체 instance를 미리 하나 생성해서 올려둔다.
2. 이 객체 인스턴스가 필요하면 오직 getInstance() 메서드를 통해서만 조회할 수 있다. 이 메서드를 호출하면 항상 같은 인스턴스를 반환한다.
3. 딱 1개의 객체 인스턴스만 존재해야 하므로, 생성자를 private으로 막아서 혹시라도 외부에서 new 키워드 로 객체 인스턴스가 생성되는 것을 막는다.

계속해서 만드는 것이 아닌 **이미 만들어진 객체를 호출**해서 사용 → 효율적 사용 가능

### **싱글톤 패턴 문제점**

1. 싱글톤 패턴을 구현하는 코드 자체가 많이 들어간다.
    - 일일이 싱글톤 패턴을 적용할 객체마다 위 코드를 작성해야 한다.
2. 의존관계상 클라이언트가 구체 클래스에 의존한다. DIP를 위반한다.
    - 클래스명.getInstance() —> 구체 클래스에 의존하는 방식
3. 클라이언트가 구체 클래스에 의존해서 OCP 원칙을 위반할 가능성이 높다.
4. 테스트하기 어렵다.
    - 생성 방식이 제한적이고, 동적으로 객체를 주입하기가 어려움
5. 내부 속성을 변경하거나 초기화 하기 어렵다.
6. private 생성자로 자식 클래스를 만들기 어렵다.
    - private → **상속 불가 :** 객체지향 이점 이용 불가
7. 결론적으로 유연성이 떨어진다.
8. 안티패턴으로 불리기도 한다

## **싱글톤 컨테이너**

- 스프링 컨테이너가 싱글톤 패턴의 문제점을 해결하면서, 객체 인스턴스를 싱글톤(기본)으로 관리
    - 사용자가 빈 스코프를 조작해서 싱글톤으로 관리하지 않을 수도 있다.
- 스프링 컨테이너는 싱글톤 객체를 생성하고 관리하는 기능을 싱글톤 레지스트리
- 싱글톤 패턴을 위한 지저분한 코드가 들어가지 않아도 된다. DIP, OCP, 테스트, private 생성자로 부터 자유롭게 싱글톤을 사용할 수 있다

### **사용 시 주의점**

- 객체를 하나만 생성해서 공유하기 때문에, **상태 유지(stateful)로 설계하면 안된다.**
- 특정 클라이언트에 의존적인 필드나, 모두가 공유하는데 값을 임의로 변경하면 예상하지 못한 결과값 발생
- 필드에서 공유되지 않는 지역변수, 파라미터, ThreadLocal 등으로 사용하자.

## **@Configuration과 바이트 코드**

다음과 같은 AppConfig 코드가 있다.

스프링은 기본적으로 싱글톤 레지스트리이기 때문에 AppConfig의 생성자를 통해 출력문을 찍어서 확인해보자.

싱글톤이라면 예상하는 값은 memberService 호출 1번, memeberRepository 호출 한 번, orderService 호출 한 번이다.

```java
@Configuration
public class AppConfig {

    @Bean
    public MemberService memberService() {
        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    @Bean
    public OrderService orderService() {
        System.out.println("call AppConfig.orderService");
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    @Bean
    public DiscountPolicy discountPolicy() {
// return new FixDiscountPolicy();return new RateDiscountPolicy();
    }
}
```

```csharp
@Test
    void configurationDeep() {

        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        AppConfig bean = ac.getBean(AppConfig.class);

        System.out.println("bean = " + bean.getClass());

    }
```

그리고 테스트 했을 때의 결과값은

```java
call AppConfig.memberService
call AppConfig.memberRepository
call AppConfig.orderService
bean = class hello.core.AppConfig$$SpringCGLIB$$0
```

(참고로 AppConfig도 당연히 bean으로 등록된다)

그런데 Java의 new 키워드를 어떻게 무시하고 싱글톤으로 만들어내는가?

또한 순수한 Java 클래스라면 class.hello.core.AppConfig로 나와야 하는데,

bean의 출력 정보를 보면 뒤에 **SpringCGLIB**가 붙어있다.

**결론은 스프링이 @Congifuration을 적용한 곳에 CGLIB라는 바이트코드 조작 라이브러리를 사용**

**AppConfig 클래스를 상속받는 임의의 다른 클래스(프록시 객체)를 만들고 스프링 빈으로 등록한다.**

![](https://blog.kakaocdn.net/dn/breKoe/btsBZE7PTqk/9YVL6KvhUdAKQ16bBOuF71/img.png)

@Congifuration에 Bean으로 등록된 AppConfig 원본이 아닌 Spring이 새로 만들어낸 객체가 빈이다.

스프링은 @Bean과 함께 @Configuration을 활용하도록 하며, @Configuration에 프록시 패턴을 적용하여 싱글톤을 보장시킨다. 하지만 Bean이 단독적으로 쓰인다면 프록시 패턴이 적용되어 있지 않기 때문에, **싱글톤을 보장하지 못한다.**

그래서 단순히 Configuration 어노테이션을 사용하지 않고 **Bean만 사용한다면 생성되는 객체는 모두 다른 객체**일 것이다.

또한 **@Configuration(proxyBeanMethod=false)**를 통해 싱글톤이 아닌 다른 객체를 반환하도록 설정할 수도 있다.

해당 관련 테스트는 아래에서 테스트 코드로 진행해보자.

### **@Configuration을 적용하지 않고 @Bean만 적용**

위에서 작성한 AppConfig.Class에서 Configuration을 빼고 테스트코드를 돌린 경우 아래와 같은 결과값이 나온다.

```java
call AppConfig.memberService
call AppConfig.memberRepository
call AppConfig.orderService
call AppConfig.memberRepository
call AppConfig.memberRepository
bean = class hello.core.AppConfig
```

```java
memberService -> memberRepository =
hello.core.member.MemoryMemberRepository@6239aba6
orderService -> memberRepository =
hello.core.member.MemoryMemberRepository@3e6104fc
memberRepository = hello.core.member.MemoryMemberRepository@12359a82
```

서로 다른 객체가 생겼기 때문에 기본적으로 memberRepository가 @Configuration이 붙었을 때랑 다르게 3번 호출되었으며, memberRepository 객체들은 **모두 다른 인스턴스**이다.

### **정리**

- @Bean만 사용하면 스프링 빈이 등록은 되나 싱글톤을 보장하지 않는다
- 스프링 설정 정보에 @Configuration을 붙여주자.
