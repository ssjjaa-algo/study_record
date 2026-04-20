- 기본 생성자인 `new ReentrantLock()`은 **비공정(nonfair)** 락을 사용
- `ReentrantLock.lock()`은 내부의 `sync.lock()`으로 위임되고, `sync`는 기본적으로 `NonfairSync` 인스턴스
- **비공정 락 위주로 설명**
- 비공정 락 : 먼저 기다리는 스레드가 있어도, **새로 온 스레드가 먼저 락을 가져갈 수 있다.**
    - 큐를 먼저 보지 않고 CAS로 락을 시도할 수 있다

---

## 시작점: `ReentrantLock.lock()`

```java
public void lock() {
    sync.lock();                 // 실제 동기화 로직은 Sync/AQS 쪽으로 위임
}
```

- 기본 생성자는 `sync = new NonfairSync();` 이므로 기본 `lock()`은 비공정 락 경로를 탄다

```jsx
// ReentrantLock lock = new ReentrantLock()의 생성자 내부에서 비공정 락을 선언
private final Sync sync;         // 실제 락 메커니즘 담당 객체로 내부 필드

public ReentrantLock() {
    sync = new NonfairSync();    // 기본은 nonfair
}

public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

- `Sync`는 `AbstractQueuedSynchronizer`(AQS)를 상속
    - `ReentrantLock`의 본질은 **AQS 기반 exclusive lock 구현체**

---

## 1. `Sync.lock()`

```java
@ReservedStackAccess
final void lock() {
    if (!initialTryLock())       // 먼저 빠른 경로(fast path)로 즉시 획득 시도
        acquire(1);              // 실패하면 AQS acquire 경로 진입
}
```

- `initialTryLock()`은 지금 당장 락을 잡을 수 있는가?를 체크한다
    - **비공정 락의 특성**으로 락이 풀린 순간에 다른 스레드가 바로 잡을 수 있으면(대기하고 있는 스레드와 상관없이)
        - 별도의 작업 (큐 확인, 문맥 전환) 없이 처리할 수 있어 **성능 향상 (공정은 항상 대기 큐를 확인함)**
- 실패하면 `AQS.acquire(1)`로 내려가서 대기 큐 기반 경쟁 절차를 진행한다
    - `1`의 의미: exclusive lock을 **1단위 획득**하겠다는 뜻
- `ReentrantLock`에서는 AQS의 `state`를 **hold count(재진입 횟수)**로 사용

---

# 2. `NonfairSync.initialTryLock()`

```java
final boolean initialTryLock() {
    Thread current = Thread.currentThread();   // 현재 스레드 확보

    if (compareAndSetState(0, 1)) {            // state가 0이면 1로 CAS 시도
        setExclusiveOwnerThread(current);      // 락 소유자를 현재 스레드로 기록
        return true;                           // 획득 성공
    } else if (getExclusiveOwnerThread() == current) { // 이미 내가 소유 중인지
        int c = getState() + 1;                // 재진입이므로 hold count 증가
        if (c < 0)                             // int overflow 검사
            throw new Error("Maximum lock count exceeded");
        setState(c);                           // 증가된 hold count 저장
        return true;                           // 재진입 성공
    } else
        return false;                          // 다른 스레드가 가지고 있으므로 실패
}
```

1. 현재 스레드를 owner로 기록하기 위해 호출한다.
2. compareAndSetState를 통해 state를 1로 바꿀 수 있는 상태라면 바로 락을 점유한다.
3. 해당 락이 이미 내가 소유자일 수도 있으므로 검사하여 재진입 count를 증가시킨다
- 이상의 경우에도 불구하고 내가 소유하지 못한다면 **`초기에 락을 잡는데 실패`**

---

# 3. `initialTryLock()` 실패 시: `AQS.acquire(1)`

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg))                // 다시 한 번 subclass의 tryAcquire 시도
        acquire(null, arg, false, false, false, 0L); // 실패하면 본격적인 큐 대기
}
```

- 여기서도 먼저 `tryAcquire(1)`를 다시 호출
- 그래도 실패하면 내부 `acquire(...)` 루프로 들어가 큐에 들어가고 필요하면 `park()`

---

# 4. `tryAcquire(1)`

```java
protected final boolean tryAcquire(int acquires) {
    if (getState() == 0 && compareAndSetState(0, acquires)) {
        setExclusiveOwnerThread(Thread.currentThread()); // owner 설정
        return true;                                     // 획득 성공
    }
    return false;                                        // 실패
}
```

- 로직을 살펴보면 initialTryLock에서 수행하는 메서드를 다시 확인하는 것임. 왜 이것을 또 하나?
    - 첫 실패 직후에도 순간적으로 락이 풀렸을 수 있기 때문
        - 비공정 락의 acquire는 성능 향상을 위해 락이 풀린 순간에 다른 스레드가 점유를 요청하는 경우 대기열에 있는 스레드까지 가서 확인하는 작업을 안하고 즉시 획득할 수 있다 (위에서 설명한 성능 향상 때문에)
            - 여기서 대기열은 CHL Node로 구성된 큐를 의미
        - 그래서 이 잠깐의 짧은 사이에도 똑같이 확인하는 것 같다.

### 흐름

1. `initialTryLock()`
    - 비어 있으면 CAS
    - 내가 owner면 재진입 증가
2. 실패 시 `acquire(1)`
3. 내부에서 `tryAcquire(1)`
    - 오직 `state == 0`인 경우만 재도전
    - 성공하면 owner 설정 후 끝

---

# 5. AQS.acquire(node, arg, shared, interruptible, timed, time)

```java
    final int acquire(Node node, int arg, boolean shared,
                      boolean interruptible, boolean timed, long time) {
        Thread current = Thread.currentThread();
        byte spins = 0, postSpins = 0;   // retries upon unpark of first thread
        boolean interrupted = false, first = false;
        Node pred = null;               // predecessor of node when enqueued

        /*
         * Repeatedly:
         *  Check if node now first
         *    if so, ensure head stable, else ensure valid predecessor
         *  if node is first or not yet enqueued, try acquiring
         *  else if queue is not initialized, do so by attaching new header node
         *     resort to spinwait on OOME trying to create node
         *  else if node not yet created, create it
         *     resort to spinwait on OOME trying to create node
         *  else if not yet enqueued, try once to enqueue
         *  else if woken from park, retry (up to postSpins times)
         *  else if WAITING status not set, set and retry
         *  else park and clear WAITING status, and check cancellation
         */

        for (;;) {
            if (!first && (pred = (node == null) ? null : node.prev) != null &&
                !(first = (head == pred))) {
                if (pred.status < 0) {
                    cleanQueue();           // predecessor cancelled
                    continue;
                } else if (pred.prev == null) {
                    Thread.onSpinWait();    // ensure serialization
                    continue;
                }
            }
            if (first || pred == null) {
                boolean acquired;
                try {
                    if (shared)
                        acquired = (tryAcquireShared(arg) >= 0);
                    else
                        acquired = tryAcquire(arg);
                } catch (Throwable ex) {
                    cancelAcquire(node, interrupted, false);
                    throw ex;
                }
                if (acquired) {
                    if (first) {
                        node.prev = null;
                        head = node;
                        pred.next = null;
                        node.waiter = null;
                        if (shared)
                            signalNextIfShared(node);
                        if (interrupted)
                            current.interrupt();
                    }
                    return 1;
                }
            }
            Node t;
            if ((t = tail) == null) {           // initialize queue
                if (tryInitializeHead() == null)
                    return acquireOnOOME(shared, arg);
            } else if (node == null) {          // allocate; retry before enqueue
                try {
                    node = (shared) ? new SharedNode() : new ExclusiveNode();
                } catch (OutOfMemoryError oome) {
                    return acquireOnOOME(shared, arg);
                }
            } else if (pred == null) {          // try to enqueue
                node.waiter = current;
                node.setPrevRelaxed(t);         // avoid unnecessary fence
                if (!casTail(t, node))
                    node.setPrevRelaxed(null);  // back out
                else
                    t.next = node;
            } else if (first && spins != 0) {
                --spins;                        // reduce unfairness on rewaits
                Thread.onSpinWait();
            } else if (node.status == 0) {
                node.status = WAITING;          // enable signal and recheck
            } else {
                long nanos;
                spins = postSpins = (byte)((postSpins << 1) | 1);
                if (!timed)
                    LockSupport.park(this);
                else if ((nanos = time - System.nanoTime()) > 0L)
                    LockSupport.parkNanos(this, nanos);
                else
                    break;
                node.clearStatus();
                if ((interrupted |= Thread.interrupted()) && interruptible)
                    break;
            }
        }
        return cancelAcquire(node, interrupted, interruptible);
    }
```

1. 내가 맨 앞 차례인지 확인 → 대기 큐에서 **가장 맨 앞에 있어야** 락을 시도할 수 있는 자격이 있는 상태임
    - 비공정 락이라고 하지 않았나요?
        - 여기서의 비공정은 대기큐 없이 순서 무작위로 락을 잡는다는 것이 아님
        - **`기본적으로 대기 큐는 존재`**하지만 락을 점유한 스레드가 락을 해제했을 때, 대기 큐에 있는 스레드가 아닌 외부의 스레드가 락 소유를 요청하는 경우에 해당 스레드가 먼저 점유할 수 있다는 뜻
            - 이 타이밍이 맞지 않으면 비공정으로 돌아가지 않음
        - 왜 이렇게 설계 했을까?
            - 대기 큐로 들어가서 **`실제로 스레드를 park 시키는 과정을 최소화하기 위함`**→ 비용이 비싼 작업이므로
2. 내가 맨 앞 차례이거나 아직 큐에 안들어갔다면 락 획득 시도
    - 여기서 락 획득이 가능하다면 return한다
    - 여기서도 최대한 큐에서 park 하지 않도록 하려는 것처럼 보인다.
3. 큐 초기화 (AQS 큐를 만드는 것)

    ```java
                Node t;
                if ((t = tail) == null) {           // initialize queue
                    if (tryInitializeHead() == null)
                        return acquireOnOOME(shared, arg);
                } else if (node == null) {          // allocate; retry before enqueue
                    try {
                        node = (shared) ? new SharedNode() : new ExclusiveNode();
                    } catch (OutOfMemoryError oome) {
                        return acquireOnOOME(shared, arg);
                    }
     
     abstract static class Node {
         volatile Node prev;       // initially attached via casTail
         volatile Node next;       // visibly nonnull when signallable
         Thread waiter;            // visibly nonnull when enqueued
         volatile int status;
         
    
    // AQS 내부 안에 큐
    
        /**
         * Head of the wait queue, lazily initialized.
         */
        private transient volatile Node head;
    
        /**
         * Tail of the wait queue. After initialization, modified only via casTail.
         */
        private transient volatile Node tail;  
    ```

    - 이렇게 되어있는 Node를 생성하여 붙인다, **`CLH Node`**라고 부르며 메서드 형태를 통해 이중 연결 리스트 형태로 관리됨을 파악할 수 있다.
    - node는 acquire 메서드를 호출할 때 null로 들어온다. 또한 shared 변수 역시 false로 들어오므로 exclusive mode의 노드가 만들어진다.
4. 현재 노드를 tail에 cas로 연결한다
5. spinwait를 통해 깨워진 스레드가 비공정 락에 의해 항상 무시되는 일이 없도록 spin을 돌린다
6. WAITING 상태로 기록 후 대기자라는 표시를 남긴다
7. 더 할 수 있는 게 없으면 여기서부터 실제 **`park`**
    - **최대한 park를 delay 하려는 것처럼 보인다**

# 6. 락 해제

```java
public void unlock() {
    sync.release(1);                     // AQS release 호출
}
```

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {               // subclass가 state 감소/완전 해제 판단
        signalNext(head);                // 완전 해제면 다음 대기자 깨움
        return true;
    }
    return false;
}
```

- `tryRelease(1)`가 `true`를 반환할 때만 다음 스레드를 깨운다
- 재진입 상태라면 hold count만 줄이고 아직 완전히 풀지 않을 수 있다

```java
protected final boolean tryRelease(int releases) {
    int c = getState() - releases;                   // hold count 감소
    if (getExclusiveOwnerThread() != Thread.currentThread())
        throw new IllegalMonitorStateException();    // owner 아닌데 unlock 금지

    boolean free = (c == 0);                         // 0이면 완전 해제
    if (free)
        setExclusiveOwnerThread(null);               // owner 제거
    setState(c);                                     // 감소된 state 저장
    return free;                                     // true일 때만 후속 대기자 깨움
}
```

---

# 7. 전체 호출 체인 요약

## 7-1. 경쟁이 없을 때

```java
ReentrantLock.lock()
 -> Sync.lock()
    -> NonfairSync.initialTryLock()
       -> compareAndSetState(0, 1)
       -> setExclusiveOwnerThread(current)
       -> return
```

## 7-2. 현재 스레드가 이미 락을 들고 있을 때

```java
ReentrantLock.lock()
 -> Sync.lock()
    -> NonfairSync.initialTryLock()
       -> getExclusiveOwnerThread() == current
       -> getState() + 1
       -> setState(c)
       -> return
```

## 7-3. 다른 스레드가 락을 들고 있을 때

```java
ReentrantLock.lock()
 -> Sync.lock()
    -> NonfairSync.initialTryLock()   // 실패
    -> AQS.acquire(1)
       -> tryAcquire(1)               // 재시도
       -> AQS.acquire(null, 1, false, false, false, 0L)
          -> 큐 초기화
          -> ExclusiveNode 생성
          -> tail에 enqueue
          -> WAITING 설정
          -> LockSupport.park(this)
          -> unlock 쪽 signalNext(head) 후 깨어남
          -> tryAcquire(1) 성공
          -> head 승격
          -> return
```

# 왜 가상스레드 친화적인가

```java
             else {
                long nanos;
                spins = postSpins = (byte)((postSpins << 1) | 1);
                if (!timed)
                    LockSupport.park(this);
                else if ((nanos = time - System.nanoTime()) > 0L)
                    LockSupport.parkNanos(this, nanos);
                else
                    break;
                node.clearStatus();
                if ((interrupted |= Thread.interrupted()) && interruptible)
                    break;
            }
```

- acquire 메서드의 끝 중 실제로 park까지 가는 부분의 메서드
- `ReentrantLock`은 경쟁 시 AQS 내부에서 결국 `LockSupport.park(this)`를 호출해 대기

```java
    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        try {
            if (t.isVirtual()) {
                VirtualThreads.park();
            } else {
                U.park(false, 0L);
            }
        } finally {
            setBlocker(t, null);
        }
    }
```

- ReentrantLock이 대기 시 `LockSupport.park()`를 사용하고, JDK 21의 `LockSupport.park()`는 현재 스레드가 virtual thread이면 `VirtualThreads.park()`로 분기하기 때문에, 경쟁으로 기다리는 virtual thread를 carrier thread에 고정하지 않고 unmount시킬 수 있다. → pinning 문제 해결