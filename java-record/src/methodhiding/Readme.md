- 메서드 하이딩은 정적 메서드에 한정된다
- 슈퍼/서브 클래스 관계에서 같은 시그니처와 이름을 가진 2개의 static 메서드를 선언한다면 두 메서드를 숨긴다
- 실제 참조하는 클래스를 찾는 것이 아니라 `컴파일에 결정된 클래스의 메서드`를 호출한다
- 슈퍼 클래스에서 메서드를 호출하면 슈퍼클래스의 static이 호출되고 서브에서는 서브의 static이 호출된다
    - static 메서드는 **다형이 될 수 없기 때문에** **`하이딩과 오버라이딩은 다른 개념이다`**

```java
package methodhiding;

public class Main {

    public static void main(String[] args) {

        Vehicle v= new Car();
        v.overrideMethod();
        v.staticMethod();
    }
}

/*
결과
Car's override method
Vehicle's method
*/
```

- 위의 코드는 문법상은 가능하다
- 실제로는 참조변수 car의 `컴파일타입인 Vehicle 기준`으로 처리되어 Vehicle.staticMethod() 호출
- JVM은 런타임에 객체가 Car인지 보고 고르는 것이 아니라, 이미 바이트코드에 있는 Vehicle.staticMethod 심볼을 따라간다

## Vehicle 바이트코드

```java
public static void staticMethod();
  descriptor: ()V
  flags: (0x0009) ACC_PUBLIC, ACC_STATIC
  Code:
    stack=2, locals=0, args_size=0
       0: getstatic     #7   // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #13  // String Vehicle's method
       5: invokevirtual #15  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

```java
public void overrideMethod();
  descriptor: ()V
  flags: (0x0001) ACC_PUBLIC
  Code:
    stack=2, locals=1, args_size=1
       0: getstatic     #7   // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #21  // String Vehicle's override method
       5: invokevirtual #15  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

## Car 바이트코드

```java
public static void staticMethod();
  descriptor: ()V
  flags: (0x0009) ACC_PUBLIC, ACC_STATIC
  Code:
    stack=2, locals=0, args_size=0
       0: getstatic     #7   // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #13  // String Car's method
       5: invokevirtual #15  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

```java
public void overrideMethod();
  descriptor: ()V
  flags: (0x0001) ACC_PUBLIC
  Code:
    stack=2, locals=1, args_size=1
       0: getstatic     #7   // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #21  // String Car's override method
       5: invokevirtual #15  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: return
```

## main 바이트코드

```java
public static void main(java.lang.String[]);
  descriptor: ([Ljava/lang/String;)V
  flags: (0x0009) ACC_PUBLIC, ACC_STATIC
  Code:
     0: new           #7   // class methodhiding/Car
     3: dup
     4: invokespecial #9   // Method methodhiding/Car."<init>":()V
     7: astore_1
     8: aload_1
     9: pop
    10: invokestatic  #10  // Method methodhiding/Vehicle.staticMethod:()V
    13: aload_1
    14: invokevirtual #15  // Method methodhiding/Vehicle.overrideMethod:()V
    17: return
    
// 실제 코드
package methodhiding;

public class Main {

    public static void main(String[] args) {

        Vehicle v= new Car();
        v.staticMethod();
        v.overrideMethod();
    }
}

/*
결과
Car's override method
Vehicle's method
*/
```

- v.staticMethod()를 호출할 때는 invokestatic이 나온다
    - 컴파일타임 타입 Vehicle 기준
- v.overrideMethod()를 호출할 때는 invokevirtual이 나온다
    - 런타임 객체 타입 Car 기준으로 디스패치
    - `invokevirtual`은 JVM에서 인스턴스 메서드를 호출하는 가장 일반적인 바이트코드 명령어로, 런타임 시 객체의 실제 타입에 기반하여 다형성을 지원
- 따라서 v.overrideMethod()는 car타입의 다형성을 받아 car.overrideMethod가 실행이 된다
- v.staticeMethod()는 컴파일타임 기준으로 Vehicle.staticMethod가 실행된다

## 참고

- 자바 코딩 인터뷰 완벽 가이드
- JVM 밑바닥까지 파헤치기