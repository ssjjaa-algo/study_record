# 템플릿 메서드 패턴

GOF에서 말하는 템플릿 메서드 패턴은 아래와 같다.

**"작업에서 알고리즘의 골격을 정의하고 일부 단계를 하위 클래스로 연기합니다. 템플릿 메서드를 사용하면 하위 클래스가 알고리즘의 구조를 변경하지 않고도 알고리즘의 특정 단계를 재정의할 수 있습니다."**

일반적으로 **상속**을 이용해서 사용한다. 즉, 상속을 통해서 기능을 확장하는데, **변하지 않는 부분과 변하는 부분을 분리**해두어 변하지 않는 부분을 부모(슈퍼)클래스에 두고, 변하는 부분은 추상(abstract) 메서드로 정의해서 자식 클래스에서 Override로 구현하는 것이다.

(템플릿이 변하지 않는 부분을 뜻하며, 변하는 부분은 메서드 Override로 재정의하는 것을 의미)

예를 들어서 메서드를 실행할 때마다 걸리는 시간을 측정하고 싶다고 하자. 그렇다면 모든 메서드에 대해서 시간을 측정하는 로직은 동일하게 작성할 수 있다. 아래 코드에서는 endTime - startTime이다.

이런 공통적인 요소를 뽑아 아래와 같은 AbstractTemplate을 만들 수 있다.

```java
@Slf4j
public abstract class AbstractTemplate {

    public void execute() {

        long startTime = System.currentTimeMillis();
//비즈니스 로직 실행
        call();
//비즈니스 로직 종료long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;
        log.info("resultTime={}",resultTime);

    }

    protected abstract void call();
}
```

위와 같은 형태를 만들어두고, 서브클래스에서 AbstaractTemplate을  extends한 클래스를 하나 정의한다.

```java
@Slf4j
public class SubClassLogic1 extends AbstractTemplate{

    @Override
    protected void call() {
        log.info("비즈니스 로직1 실행");
    }
}
```

이후 아래의 테스트 코드를 통해 실행하면 execute()를 실행함과 동시에, SubClassLogic1에서 정의한 call 메서드를 실행시킨다. call메서드는 부모 클래스에서 abstract 형태로 되어있기 때문에 자식 클래스에서 반드시 정의해야 하며, 부모의 골격을 건드리지 않고 자식 클래스에서 마음대로 해당 부분을 작성하여 이용할 수 있다.

```java
    @Test
    void templateMethod() {
        AbstractTemplate template1 = new SubClassLogic1();
        template1.execute();

}
```

그림으로 보면 아래와 같은 관계를 가지게 된다.

![](https://blog.kakaocdn.net/dn/bDhV9D/btsCN8nzpMx/e4r9UmgEcQPHam1wpeBfvK/img.png)

템플릿 메서드 패턴을 통해 공통 로직을 정의하고, 상속으로 구현하게 함으로써 확장 가능성을 열어둘 수 있다. 하지만, **상속**을 이용하여 구현하기 때문에 **새로운 클래스**를 매번 만들어야 하며 상속으로 얻는 단점을 그대로 가진다.
