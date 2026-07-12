# Reactor Netty

https://godekdls.github.io/Reactor%20Netty/tcpserver/

- 리액터 네티는 쉬운 TcpServer 생성
    - Netty 기능을 대부분 숨겨주고 리액티브 스트림 backpressure를 추가해준다

# TCP 서버 시작 (HTTP도 만들 수 있고 그건 공식문서의 HTTP 부분을 보면 된다)

- TcpServer 인스턴스 만들기
- bind 연산은 시스템에서 임의의 포트를 선택 (아래 예시는 port 설정)

```java
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

public class Application {

	public static void main(String[] args) {
		DisposableServer server =
				TcpServer.create()
				         .host("localhost") // (1)
				         .port(8080)        // (2)
				         .bindNow();

		server.onDispose()
		      .block();
	}
}
```

# Writing Data

- 연결된 클라이언트에 데이터 전송 → I/O 핸들러가 필요하다
- I/O 핸들러는 **`NettyOutbound`**에 접근하여 데이터를 write할 수 있다

```java
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

public class Application {

	public static void main(String[] args) {
		DisposableServer server =
				TcpServer.create()
				         .handle((inbound, outbound) -> 
				          outbound.sendString(Mono.just("hello"))) 
				          // (1) 클라이언트에 hello 전송
				         .bindNow();

		server.onDispose()
		      .block();
	}
}
```

# Consuming Data

- 연결된 클라이언트로부터 데이터 받기 → I/O 핸들러가 필요하다
- I/O 핸들러는 **`NettyInbound`**에 접근하여 데이터를 read할 수 있다

```java
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

public class Application {

	public static void main(String[] args) {
		DisposableServer server =
				TcpServer.create()
				         .handle((inbound, outbound) -> inbound.receive().then()) 
				         // (1) 연결된 클라이언트로부터 데이터를 받는다
				         .bindNow();

		server.onDispose()
		      .block();
	}
}
```

# WebFlux와의 관계

- HTTP 요청이 들어온다
- Reactor Netty가 NettyInbound를 통해 요청 본문을 Flux<DataBuffer> (WebfFlux의 ByteBuf 래퍼)로 변환
- 이 Flux가 Spring WebFlux의 디코더를 거쳐 @RequestBody에 바인딩
- 컨트롤러가 Mono나 Flux를 반환하면 WebFlux가 NettyOutbound의 send() 메서드에 전달하여 응답 본문에 클라이언트로 전송
- **TcpServer를 직접 생성할 때는 NettyInbound와 NettyOutbound를 직접 구현해야 한다**

# Lifecycle Callbacks

- 라이프사이클 콜백을 이용한 TCP 서버 확장 → 일종의 기능 확장 느낌이며 이거는 Spring Filter Interceptor랑 느낌이 매우 비슷한거 같은데?
    - doOnBind : 서버 채널이 바운드되려고 할 때 호출 (서버가 포트를 점유하지 않은 시점)
        - 바인딩 전에 필요한 설정값을 검증
    - doOnBound : 서버 채널이 바운드 될 때 호출한다
        - 서버 시작 완료 후 알림을 전송하거나, 실제 할당된 포트 번호를 확인하거나… 뭐 용도는 알맞게
    - doOnConnection : 리모트 클라이언트가 연결될 때 호출한다
        - 새로운 클라이언트가 서버에 연결될 때 호출
    - doOnUnbound : 서버 채널이 언바운드 될 때 호출한다
        - 서버 종료 및 자원 회수 등

 

```java
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import java.util.concurrent.TimeUnit;

public class Application {

	public static void main(String[] args) {
		DisposableServer server =
				TcpServer.create()
				         .doOnConnection(conn ->
				             conn.addHandler(new ReadTimeoutHandler(10, TimeUnit.SECONDS))) 
				             // (1) 리모트 클라이언트가 연결될 때 설정한 핸들러로 
				             // 네티 파이프라인 확장
				         .bindNow();

		server.onDispose()
		      .block();
	}
}
```

# Setting Channel Options

- TCP 서버의 Defatult 옵션

```java
TcpServerBind() {
	Map<ChannelOption<?>, Boolean> childOptions = new HashMap<>(2);
	childOptions.put(ChannelOption.AUTO_READ, false);
	childOptions.put(ChannelOption.TCP_NODELAY, true);
	this.config = new TcpServerConfig(
			Collections.singletonMap(ChannelOption.SO_REUSEADDR, true),
			childOptions,
			() -> new InetSocketAddress(DEFAULT_PORT));
}
```

- ChannelOption.SO_REUSEADDR
    - 서버를 종료했다가 즉시 재시작할 때 OS커널이 이전에 사용된 포트를 TIME_WAIT로 잡고 있을 수 있음 (무기한 잡고 있는 것인지는 아직 모름)
    - 이 옵션이 true이면 해당 포트가 TIME_WAIT라도 오류 없이 즉시 바인딩된다
- ChannelOption.AUTO_READ
    - true라면 클라이언트로부터 데이터가 도착하는 즉시 Netty가 InbounBuffer에 쌓아둠
    - 그런데 Reactor Netty는 **반응형 백프레셔** 지원
        - 데이터를 처리할 수 있을 때만 데이터를 읽는 것
    - 따라서 이 옵션을 false로 설정하여 데이터 소비자가 요청할 때만 데이터를 읽도록 제어
- ChannelOption.TCP_NODELAY
    - Nagle 알고리즘 비활성화
        - 작은 데이터 조각들을 버퍼에 모았다가 일정 크기나 특정 시간이 지나면 flush하는 것
    - 모으는 과정에서 약간의 지연 시간이 발생
    - 즉각적인 응답속도를 위해 low latency를 보장하기 위해 이를 false로 설정한다

### 약간 헷갈렸던 것

- **TCP_NODELY**는 데이터가 1바이트라도 생기면 모으지 말고 즉시 전송해라
    - Sender 동작의 제어
- **AUTO_READ**는 애플리케이션의 안정성
    - Receiver의 동작 제어
    - 애플리케이션이 읽겠다고 요청하기 전까지 읽지 마라는 것.
    - 이를 수신 버퍼에 담아두어 메모리 폭주 방지
