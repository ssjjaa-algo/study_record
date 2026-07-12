# 0418 유라

# AOP(Aspect Oriented Programming)

- 핵심 관심 사항과 공통 관심 사항
- 기존 OOP에서 공통관심사항을 여러 모듈에서 적용하는데 중복된 코드를 양상하는 한계가 존재.
- application에서 관심사의 분리(기능의 분리), **핵심에서 부가기능 분리**

Spring AOP 용어

1. Aspect(어스펙트): 애플리케이션에서 공통적으로 발생하는 관심사를 모듈화한 것을 말합니다. 예를 들어 로깅, 보안, 트랜잭션 처리 등이 Aspect가 될 수 있습니다.
2. Join point(조인 포인트): Aspect가 적용될 수 있는 위치를 말합니다. 메소드 호출, 예외 발생 등이 조인 포인트가 될 수 있습니다.
3. Advice(어드바이스): Aspect에서 조인 포인트에 적용되는 행동을 말합니다. Before, After, Around 등의 Advice가 있습니다.
4. Pointcut(포인트컷): 어떤 Join point에 어떤 Advice를 적용할지를 결정하는 것을 말합니다. 포인트컷은 일반적으로 특정 메소드나 패키지, 클래스 등을 지정하여 사용됩니다.
5. Weaving(위빙): AOP가 적용될 때 Aspect와 대상 객체를 결합하는 것을 말합니다. 즉, 컴파일 시간, 로드 시간, 런타임 시간에 Aspect를 적용하는 방법이 있습니다.

Spring AOP에서는 대상 객체가 인터페이스를 구현하고 있을 때, 프록시 객체를 생성하여 Aspect를 적용합니다. 이러한 방식을 프록시 기반 AOP라고 합니다. Spring AOP는 컴파일 시간이 아닌 런타임 시간에 Weaving을 수행하므로, 대상 객체를 수정하지 않고도 Aspect를 적용할 수 있습니다. 이러한 방식은 유연성과 편의성을 제공하기 때문에 Spring 프레임워크에서 많이 사용됩니다.

```java
execution(public *(리턴) *(어디) (..)) : public으로 시작하는 모든 method
execution(* set*(..)) : set으로 시작하는 모든 method

```

- 프록시 기반 AOP 지원
    - 프록시가 호출을 가로챈다(Intercept)
        - Target 객체에 대한 호출을 가로챈 다음 Advice의 부가기능 로직을 수행하고 난 후에 Target의 핵심 기능 로직을 호출(전처리)
        - Target의 핵심 기능 로직을 호출한 후에 부가 기능을 수행(후처리)
        

- POJO기반 AOP 구현
    
    ```java
    <bean id="logging" class="com.test.aop.LoggingTest"/>
    
    <aop:config>
    	<aop:pointcut id="Logmethod" expression="execution
    (public * com.test.aop..*(..))"/>
    <aop:aspect id="logAspect" ref="logging">
    	<aop:around pointcut-ref="logmethod" method:printlog"/>
    	</aop:asepect>
    </aop:config>
    
    ```
    
- Database Connection Pooling (datasource)
    - 어디다 만들어 놓고 찾아다 쓸거야.
    -
