# 개요

가상 스레드는 작업을 수행할 때 일시적으로 캐리어 스레드(플랫폼 스레드)에 **mount** 되어 실행된다. 그리고 소켓, 파일, 네트워크 호출 같은 블로킹 I/O를 만나면, 가능한 경우 캐리어 스레드에서 **unmount** 되어 대기 상태로 전환된다. 이때 비워진 캐리어 스레드는 다른 가상 스레드의 실행에 재사용될 수 있으므로, 적은 수의 플랫폼 스레드로도 매우 많은 동시 작업을 처리할 수 있다. 즉, 가상 스레드의 핵심 장점은 스레드가 블로킹되더라도 플랫폼 스레드를 계속 점유하지 않고 반납할 수 있다는 점이다. 이 덕분에 서버처럼 I/O 대기가 많은 환경에서 훨씬 많은 요청을 동시에 감당할 수 있다.

## 고정 문제

모든 상황에서 가상 스레드가 자유롭게 unmount되는 것은 아니다. JDK 21 기준으로, 가상 스레드가 **`synchronized` 블록 또는 `synchronized` 메서드 내부**에 있는 동안 블로킹 연산을 수행하면, 캐리어 스레드에서 분리되지 못하고 그대로 묶이는 **pinning(고정)** 현상이 발생할 수 있다. `synchronized` 때문에 캐리어 스레드까지 함께 점유하게 되고, 그러면 다른 가상 스레드가 해당 캐리어 스레드를 사용하지 못하고, 가상 스레드가 동시성이 약화될 수 있다.

```java
// 예시 : 아래와 같이 사용하는 경우 synchronized 때문에 unmount될 수 없다.
synchronized (lock) { 
    callRemoteApi();   // I/O 
}
```

## 해결책 - ReentrantLock

- `synchronized`는 JVM이 제공하는 내장 모니터(monitor)를 사용
- `ReentrantLock`은 가상 스레드를 인지할 수 있는 park/unpark 메커니즘을 사용한다

`ReentrantLock`은 락 획득 실패 스레드를 `LockSupport.park()`로 대기시키고 해제 시 `unpark()`로 깨운다. JDK 21에서 `LockSupport`는 가상 스레드가 park될 때 carrier thread를 반납할 수 있도록 지원하므로, monitor 기반의 `synchronized`보다 virtual thread pinning 문제를 완화하는 데 유리하다. 따라서 빈번하게 실행되며 I/O를 포함하는 임계구역에서는 `synchronized`보다 `ReentrantLock`이 더 적절한 선택이 될 수 있다.

- Reentrantlock 설명: [Readme.md](../reentrantlock/Readme.md)