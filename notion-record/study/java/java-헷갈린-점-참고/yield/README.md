# yield

태그: Thread

```java
package thread.start.controll.yield;

public class YieldMain {

    static final int THREAD_COUNT = 1000;
    public static void main(String[] args) {

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(new MyRunnalbe());
            thread.start();
        }

    }

    static class MyRunnalbe implements Runnable {

        @Override
        public void run() {

            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + ":" + i);

                /**
                Empty : 운영체제의 스케줄링을 따른다.
                sleep(1) : 특정 스레드 잠시 쉬게 한다. --> RUNNABLE TIMED_WAITING / 양보할 상황이 아닌데 이 상태를 왔다갔다 한다.
                yield() : 다른 스레드에 양보한다. / 운영체제에게 힌트만 제공한다? ->
                RUNNABLE 상태를 유지하기 때문에 양보할 스레드가 없다면 자신 스레드를 계속 실행
                 */
            }
        }
    }
}

```

## 아래 코드의 문제점

- 무한 실행
    - 따라서 yield를 추가하여 낭비 제거

```java
while(!Thread.interrupted()) {
	if (jobQueue.isEmpty()) {
		Therad.yield(); // 양보
		continue;
	}
}
```
