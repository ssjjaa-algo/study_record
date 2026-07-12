# 프록시, 데코레이터 패턴

컴퓨터 네트워크에서 클라이언트 / 서버를 말하면 보통 클라이언트를 웹 브라우저, 요청을 처리하는 서버를 웹 서버라고 한다. 해당 개념을 객체에 도입하면 요청을 하는 객체가 클라이언트가 되고, 요청을 처리하는 객체가 서버가 된다.

![](https://blog.kakaocdn.net/dn/YBxVX/btsC4CNpKDb/J5gaUjjJf2icYccgdKgEik/img.png)

클라이언트가 서버를 바로 호출하고 있으므로 이를 직접 호출이라 한다.

하지만 클라이언트가 서버에 직접적으로 요청하지 않고 **중간(대리)자**를 이용해서 요청할 수도 있다. 중간에 대리자를 둘 경우, 대리자에게 여러가지 목적을 위임할 수도 있다. 프록시를 중간에 두면 아래와 같은 그림이 된다.

![](https://blog.kakaocdn.net/dn/cNy1X5/btsCU0WUOFR/4S8jLFUOFQXNJjsURkgcD1/img.png)

Proxy(대리자)를 두어 서버를 호출한다.

위의 그림에서 Proxy를 하나만 두었지만, Proxy를 여러가지를 두어 최종적으로 서버에 도달하게 할 수도 있다. 프록시를 여러가지 두면 **프록시 체인**이 된다.

객체에서 프록시가 되려면, **클라이언트가 서버에 요청을 직접적으로 한지 프록시에게 요청을 한지 몰라야 한다.**

객체를 DI하는 것과 유사하다. 클라이언트는 인터페이스만을 바라보고 있고, Interface의 구현체 중에 Proxy와 Server가 있는 것이다. 즉, 클라이언트와 서버가 같은 인터페이스를 사용해야 한다. 클라이언트가 인터페이스를 바라봄으로써, 어느 구현체를 사용하고 있는지는 전혀 모르는 상황을 만들 수 있다.

또한 중요한 것은 클라이언트 코드를 변경하지 않아도 동작할 수 있어야 한다.

![](https://blog.kakaocdn.net/dn/HptOk/btsC39dDcc6/cPcngAhWJjJA50PpaPaZck/img.png)

클라이언트와 서버가 같은 인터페이스를 바라본다.

프록시 패턴은 보통 접근 제어(권한, 캐싱, 지연 로딩 등)에 사용한다.

예시로 코드를 작성해보자.

List를 반환하는 메서드 하나만을 가지고 있는 인터페이스를 설정하고, 실제 구현체(target, 위에서의 그림에 의하면 Server)를 Desktop 인터페이스를 구현하도록 한다. 서버도 Desktop을 반드시 구현해야 한다.

```java
public interface Desktop {
	List<Integer> info();
}
```

아래와 같이 Desktop을 구현한 클래스 Mac을 작성한다.

```java
public class Mac implements Desktop {
    @Override
    public List<Integer> info() {

    	List<Integer> infoOfMac = new ArrayList<>();

        for (int i = 0; i < 1000000000; i++) {
        	infoOfMac.add(i);
        }
    	return infoOfMac;
    }
}
```

info()의 로직 안을 보면, List에 0부터 10억까지 반복문을 돌려 반복하고 해당 값을 반환해준다. 항상 똑같은 값을 유지한다. 해서 프록시 패턴의 접근 제어(캐시)를 이용해서 한 번 작업이 완료된 결과를 계속해서 반환하고자 한다. 예시를 들기 위해 위와 같은 설정을 했다.

아래와 같이 프록시 패턴으로 캐시 로직을 추가한다.

```java
@Slf4j
public class CacheProxy implements Desktop {

    private Desktop target;
    private List<Integer> cacheValue;

    public CacheProxy(Desktop target) {
        this.target=target;
    }

    @Override
    public List<Integer> info() {

        if (cacheValue.isEmpty()) {
            log.info("타겟 호출");
            cacheValue = target.info();
        }
    	return cacheValue;
    }
}
```

CacheProxy는 cacheValue에서 이미 설정한 값이 있다면 반환하도록 설정했다. 이를 통해 타겟이 한 번 호출되고 나면, 다음 요청부터는 타겟(실제 서버)를 가지 않고 바로 값을 return시켜준다.

이제 Client 코드를 작성한다. Client 입장에서는 Desktop을 바라보게 한다. 이를 통해 Desktop을 구현한 클래스라면 어떤 것이든 의존받을 수 있다.

```java
public class ProxyPatternClient {

   private Desktop desktop;

   public ProxyPatternClient(Desktop desktop) {
       this.desktop = desktop;
   }

   public List<Integer> getInfo() {
       return desktop.info();
   }
}
```

테스트 코드를 아래와 같이 작성하면, 결과는 타겟 호출 한 번만 찍히게 된다. Client가 Proxy객체를 생성자로 넘겨받고, Proxy객체는 Target인 desktop을 바라보는데 desktop에서 로직을 한 번만 수행하고 나면, Proxy는 더이상 서버에 결과를 전달하지 않는다.

```java
@Test
void cacheProxyTest() {
   Desktop desktop = new Mac();
   Desktop Proxy = new Proxy(desktop);
   ProxyPatternClient client = new ProxyPatternClient(Proxy);

   client.getInfo();
   client.getInfo();
   client.getInfo();

}

// 결과 : 타겟 호출// 한 번의 타겟만 호출되고 그 다음은 프록시가 캐싱처리한 결과값을 바로 클라이언트에 준다.
```

### **정리**

위의 예시 코드에서 Mac(실제 서버) 코드와 클라이언트 코드는 전혀 변경하지 않고, CacheProxy라는 대리자를 두어 같은 행위가 반복되지 않도록 접근을 제어하였다. 또한 프록시를 뺀다고 하더라도, 비즈니스 로직에 영향이 가지 않는다.
