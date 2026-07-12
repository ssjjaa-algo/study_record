# ChannelHandler

<aside>
💡

I/O 이벤트를 처리하거나 I/O 작업을 가로채서, **`ChannelPipeline`**의 다음 핸들러로 전달

</aside>

```java
/**
 * 1. ChannelHandler의 기본 개념
 * ================================
 * 
 * ChannelHandler = Pipeline의 한 단계
 * 
 * [Client] → [Channel] → [Pipeline] → [Handler1] → [Handler2] → [Handler3]
 *                                          ↓           ↓           ↓
 *                                       Decode      Business    Encode
 */
```

- 보통 다음 하위 타입 중 하나를 구현:
    - **ChannelInboundHandler** : inbound I/O 이벤트를 처리
    - **ChannelOutboundHandler** : outbound I/O 작업을 처리
    
    ```java
    public class ChannelHandlerBasics {
        
        /**
         * ChannelHandler는 두 가지 타입이 있음:
         * 
         * 1. ChannelInboundHandler: 들어오는(Inbound) 데이터 처리
         *    - 클라이언트 → 서버 방향
         *    - 예: 데이터 읽기, 연결 수립, 예외 처리
         * 
         * 2. ChannelOutboundHandler: 나가는(Outbound) 데이터 처리
         *    - 서버 → 클라이언트 방향
         *    - 예: 데이터 쓰기, 연결 종료, flush
         */
        
        public void setupPipeline(ChannelPipeline pipeline) {
            // Inbound: 위에서 아래로 실행
            pipeline.addLast("decoder", new MyDecoder());        // 1번째
            pipeline.addLast("handler", new MyBusinessHandler()); // 2번째
            
            // Outbound: 아래에서 위로 실행
            pipeline.addLast("encoder", new MyEncoder());        // 응답시 실행
        }
    }
    ```
    
- 편의를 위해 다음 어댑터 클래스들이 제공
    - **ChannelInboundHandlerAdapter** : inbound I/O 이벤트를 처리
    - **ChannelOutboundHandlerAdapter** : outbound I/O 작업을 처리
    - **ChannelDuplexHandler :** inbound와 outbound 이벤트를 모두 처리
    
    ```java
    public class HandlerAdapters {
        
        // 방법 1: ChannelInboundHandlerAdapter - Inbound만 처리
        static class InboundOnlyHandler extends ChannelInboundHandlerAdapter {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                System.out.println("데이터 수신: " + msg);
                ctx.fireChannelRead(msg); // 다음 핸들러로 전달
            }
        }
        
        // 방법 2: ChannelOutboundHandlerAdapter - Outbound만 처리
        static class OutboundOnlyHandler extends ChannelOutboundHandlerAdapter {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                System.out.println("데이터 송신: " + msg);
                ctx.write(msg, promise); // 다음 핸들러로 전달
            }
        }
        
        // 방법 3: ChannelDuplexHandler - 양방향 처리
        static class BothDirectionHandler extends ChannelDuplexHandler {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                System.out.println("Inbound: " + msg);
                ctx.fireChannelRead(msg);
            }
            
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                System.out.println("Outbound: " + msg);
                ctx.write(msg, promise);
            }
        }
    }
    ```
    

## Context 객체

- ChannelHandler는 ChannelHandlerContext 객체와 함께 제공
- ChannelHandler는 context 객체를 통해 자신이 속한 ChannelPipeline과 상호작용해야 한다
- context 객체를 사용하여, ChannelHandler는 이벤트를 upstream이나 downstream으로 전달하거나, pipeline을 동적으로 수정하거나, 핸들러에 특정한 정보를 (AttributeKey를 사용하여) 저장할 수 있다

```java
/**
 * 
 * Context = Handler와 Pipeline 사이의 연결 고리
 * 
 * Context를 통해:
 * - 다음 핸들러로 이벤트 전달
 * - Pipeline 동적 수정
 * - Channel별 상태 저장
 * - EventLoop 접근
 */
public class ContextUsage {
    
    static class ContextExampleHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 1. 이벤트를 다음 핸들러로 전달
            ctx.fireChannelRead(msg);  // Pipeline을 따라 전파
            
            // 2. 직접 응답 보내기
            ctx.writeAndFlush("Response");  // Outbound 방향으로 전송
            
            // 3. Channel 정보 접근
            Channel channel = ctx.channel();
            System.out.println("Channel ID: " + channel.id());
            
            // 4. EventLoop 접근 (중요!)
            EventLoop eventLoop = ctx.channel().eventLoop();
            System.out.println("처리 스레드: " + eventLoop);
            
            // 5. Pipeline 동적 수정
            ctx.pipeline().addLast("newHandler", new AnotherHandler());
            
            // 6. Attribute로 상태 저장 (Channel별)
            AttributeKey<Integer> key = AttributeKey.valueOf("requestCount");
            ctx.channel().attr(key).set(10);
        }
    }
}
```

# 상태 관리

- Q. 핸들러가 상태를 가져야 할 때가 언제일까? 공식문서에서는 이런 경우도 있나보다.
- 상태 관리를 위해 주의해야할 점
- 공식문서 내용
    - Because the handler instance has a state variable which is dedicated to one connection, **you `have to create a new handler instance` for each new channel to avoid a race condition where an unauthenticated client can get the confidential information**:

```java
/**
 * 4. 상태 관리 (State Management) - 핵심 개념!
 * ===========================================
 * 
 * 문제: Handler가 상태(state)를 가져야 한다면?
 * 해결: 두 가지 방법
 */
public class StateManagement {
    
    /**
     * 방법 1: 멤버 변수 사용 (권장)
     * =============================
     * 
     * 각 연결마다 새로운 Handler 인스턴스 생성
     */
    static class StatefulHandler extends SimpleChannelInboundHandler<String> {
        
        // 이 변수는 이 Handler 인스턴스에만 속함
        private boolean loggedIn = false;
        private int requestCount = 0;
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            requestCount++;
            
            if (msg.startsWith("LOGIN:")) {
                loggedIn = true;
                ctx.writeAndFlush("로그인 성공");
            } else if (msg.startsWith("DATA:")) {
                if (loggedIn) {
                    ctx.writeAndFlush("데이터: " + fetchData());
                } else {
                    ctx.writeAndFlush("로그인 필요");
                }
            }
            
            System.out.println("요청 횟수: " + requestCount);
        }
        
        private String fetchData() {
            return "비밀 데이터";
        }
    }
    
    /**
     * 주의: 각 Channel마다 새 인스턴스를 생성해야 함!
     */
    static class ServerInitializer extends ChannelInitializer<Channel> {
        @Override
        protected void initChannel(Channel ch) {
            // ✅ 올바른 방법: 매번 새 인스턴스 생성
            ch.pipeline().addLast(new StatefulHandler());
            
            // ❌ 잘못된 방법: 인스턴스 재사용 (race condition 발생!)
            // ch.pipeline().addLast(sharedInstance);
        }
    }
}
```

## Sharable하게 Handler를 사용하는 경우

```java
/**
 * 5. @Sharable 어노테이션 - 인스턴스 공유
 * =======================================
 * 
 * 상태가 없는 Handler는 @Sharable로 표시하여
 * 하나의 인스턴스를 여러 Pipeline에서 재사용 가능
 */
public class SharableHandlers {
    
    /**
     * 방법 2: AttributeKey 사용 + @Sharable
     * ======================================
     * 
     * 상태를 ChannelHandlerContext에 저장
     * Handler 인스턴스는 stateless하게 유지
     */
    @ChannelHandler.Sharable  // 이 어노테이션이 핵심!
    static class SharedStatefulHandler extends SimpleChannelInboundHandler<String> {
        
        // AttributeKey는 상수로 선언 (모든 Channel이 공유)
        private static final AttributeKey<Boolean> AUTH = 
                AttributeKey.valueOf("authenticated");
        private static final AttributeKey<Integer> COUNT = 
                AttributeKey.valueOf("count");
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            // Channel별로 독립적인 상태를 AttributeKey로 관리
            Boolean authenticated = ctx.channel().attr(AUTH).get();
            Integer count = ctx.channel().attr(COUNT).get();
            
            if (count == null) count = 0;
            count++;
            ctx.channel().attr(COUNT).set(count);
            
            if (msg.startsWith("LOGIN:")) {
                ctx.channel().attr(AUTH).set(true);
                ctx.writeAndFlush("로그인 성공");
            } else if (msg.startsWith("DATA:")) {
                if (Boolean.TRUE.equals(authenticated)) {
                    ctx.writeAndFlush("데이터: " + fetchData());
                } else {
                    ctx.writeAndFlush("로그인 필요");
                }
            }
            
            System.out.println("Channel별 요청 횟수: " + count);
        }
        
        private String fetchData() {
            return "비밀 데이터";
        }
    }
    
    /**
     * @Sharable 사용 시 장점
     * 이것은 일종의 싱글톤처럼 작동하는 듯
     */
    static class SharedHandlerInitializer extends ChannelInitializer<Channel> {
        
        // ✅ 하나의 인스턴스만 생성 (메모리 절약)
        private static final SharedStatefulHandler SHARED = 
                new SharedStatefulHandler();
        
        @Override
        protected void initChannel(Channel ch) {
            // 같은 인스턴스를 모든 Channel에서 재사용
            ch.pipeline().addLast(SHARED);
            
            // 100만 개 연결이 와도 Handler 인스턴스는 1개!
        }
    }
    
    /**
     * @Sharable 없이 stateless Handler
     */
    @ChannelHandler.Sharable
    static class LoggingHandler extends ChannelInboundHandlerAdapter {
        
        // 멤버 변수 없음 = stateless = 공유 가능
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("수신: " + msg);
            ctx.fireChannelRead(msg);
        }
    }
}
```

```java
/**
 * 6. 실제 사용 예제: HTTP 서버
 * ============================
 */
public class HttpServerExample {
    
    static class HttpServerInitializer extends ChannelInitializer<Channel> {
        
        // Stateless handlers - @Sharable로 공유
        private static final HttpServerCodec CODEC = new HttpServerCodec();
        private static final LoggingHandler LOGGER = new LoggingHandler();
        
        @Override
        protected void initChannel(Channel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            
            // 1. HTTP 코덱 (Sharable)
            pipeline.addLast(CODEC);
            
            // 2. 로깅 (Sharable)
            pipeline.addLast(LOGGER);
            
            // 3. 비즈니스 로직 (Stateful, 매번 새 인스턴스)
            pipeline.addLast(new HttpBusinessHandler());
        }
    }
    
    static class HttpBusinessHandler extends SimpleChannelInboundHandler<HttpRequest> {
        
        // 이 Handler는 상태를 가질 수 있음
        private long requestStartTime;
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) {
            requestStartTime = System.currentTimeMillis();
            
            // 비즈니스 로직 처리
            String response = processRequest(request);
            
            // 응답 전송
            ctx.writeAndFlush(createResponse(response));
            
            long elapsed = System.currentTimeMillis() - requestStartTime;
            System.out.println("처리 시간: " + elapsed + "ms");
        }
        
        private String processRequest(HttpRequest request) {
            return "Hello World";
        }
        
        private HttpResponse createResponse(String content) {
            return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK
            );
        }
    }
}
```

# ChannelHandler 주요 메서드

```java
public class HandlerLifecycle {
    
    static class LifecycleHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            // Handler가 Pipeline에 추가될 때 호출
            System.out.println("Handler 추가됨");
        }
        
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) {
            // Channel이 EventLoop에 등록될 때
            System.out.println("Channel 등록됨");
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // Channel이 활성화될 때 (연결 수립)
            System.out.println("Channel 활성화");
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // 데이터가 읽힐 때마다
            System.out.println("데이터 수신");
            ctx.fireChannelRead(msg);
        }
        
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            // 현재 읽기 작업 완료
            System.out.println("읽기 완료");
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            // Channel이 비활성화될 때 (연결 종료)
            System.out.println("Channel 비활성화");
        }
        
        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) {
            // Channel이 EventLoop에서 해제될 때
            System.out.println("Channel 등록 해제");
        }
        
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            // Handler가 Pipeline에서 제거될 때
            System.out.println("Handler 제거됨");
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // 예외 발생 시
            System.err.println("예외 발생: " + cause.getMessage());
            ctx.close();
        }
    }
}

```
