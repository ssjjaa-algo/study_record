# 스레드

[https://github.com/gyoogle/tech-interview-for-developer/blob/master/Language/[java] Java에서의 Thread.md](https://github.com/gyoogle/tech-interview-for-developer/blob/master/Language/%5Bjava%5D%20Java%EC%97%90%EC%84%9C%EC%9D%98%20Thread.md)

# 자바에서 스레드 구현 : 2가지

## Runnable 인터페이스

- run()메서드 오버라이딩

```jsx
public class MyThread implements Runnable {
    @Override
    public void run() {
        // 수행 코드
    }
}

public static void main(String[] args) {
		Runnable r = new MyThread();
		Thread t = new Thread(r, "mythread");
}
```

```jsx
public class MyThread implements Runnable {
    @Override
    public void run() {
        // 수행 코드
    }
}

public static void main(String[] args) {
		Runnable r = new MyThread();
		Thread t = new Thread(r, "mythread");
}
```

- 해당 클래스를 인스턴스화 → Thread 생성자에 인자로 넘겨준다.
- 이후 run() 호출 시 Runnable 인터페이스에서 구현한 run() 호출, 따로 오버라이딩 하지 않아도 된다.
- currentThread() —> Thread 클래스의 static 메서드
    - 위를 통해 현재 스레드에 대한 참조를 얻어와야 호출이 가능.

## Thread 클래스 상속

- run()메서드 오버라이딩

```jsx
public class MyThread extends Thread {
    @Override
    public void run() {
        // 수행 코드
    }
}
```

- 상속받은 클래스 자체를 스레드로 사용 가능.
- 스레드 클래스의 메소드 (getName()) 바로 사용 가능.

## 스레드 상태

- NEW : 스레드가 생성되고 아직 start()가 호출되지 않은 상태.
- RUNNABLE : 실행 중 또는 실행 가능 상태
- BLOCKED : 동기화 블럭에 의해 일시정지
- WAITING, TIME_WAITING : 실행가능하지 않은 일시정지 상태
- TERMINATED : 작업 종료

## 주의

- 스레드의 실행은 run() 호출이 아닌, start() 호출로 해야 한다.
- **run()은 스레드를 사용하는 것이 아니다.**

<aside>
💡 **`Java 콜 스택(call stack)`**

</aside>

- 동시에 두 가지 작업을 하는 경우 두 개 이상의 콜스택 필요.
- 스레드를 이용한다?
    - JVM이 다수의 콜 스택을 번갈아가며 일처리를 한다.
- run() 메서드는 main()의 콜 스택 하나만을 이용하는 것이다.
- start()메서드는 JVM이 알아서 새롭게 콜 스택을 만들고 context switching으로 스레드답게 동작하게 한다.
