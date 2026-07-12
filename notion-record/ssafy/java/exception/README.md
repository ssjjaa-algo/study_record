# Exception

### [에러와 예외] :  어떤 원인에 의해 오동작 하거나 비정상적 종료

- Error : 메모리 부족, stack overflow와 같이 발생하면 복구할 수 없는 상황
    - 프로그램의 비정상적 종료를 막을 수 없으므로 디버깅을 잘 해야한다.
- Exception : 읽으려는 파일이 없거나, 네트워크 연결이 안되는 등. 수습될 수 있는 비교적 상태가 약한 것 들
    - 프로그램 코드에 의해 수습될 수 있는 상황
    - checked exception : 예외에 대한 대처코드가 없으면 컴파일이 진행되지 않음
    - unchecked exception : 예외에 대한 대처코드가 없더라도 컴파일은 진행
- try catch 코드

```java
try {
// 예외가 발생할 수 있는 코드
} catch(XXException e) {// 던진 예외를 받음// 예외가 발생했을 때 처리할 코드
}
```

- Throwable의 주요 메서드
    - public String getMessage() : 발생된 예외에 대한 구체적인 메시지를 반환
    - public void printStackTrace() : 예외가 발생된 메서드가 호출되기까지의 메서드 호출 스택을 출력한다.
    - try블록에서 예외가 발생하면 jvm이 해당 exception 클래스의 객체 생성 후 던짐(throw)
    - throw new XXException()
        - 던져진 exception을 처리할 수 있는 catch 블록에서 받은 후 처리
        - 정상적으로 처리되면 try-catch 블록을 벗어나 다음 문장 진행
- 다중 exception handling
- try 블록에서 여러 종류의 예외가 발생할 경우 하나의 try 블록에 여러 개의 catch 블록 추가 가능
    - 상위 타입의 예외가 먼저 선언되는 경우 뒤에 등장하는 catch 블록은 동작할 기회가 없음
    - 상속 관계가 없는 경우는 무관
    - **`작은 범위에서 큰 범위로 작성하자.`**
- **`예외 발생 여부가 중요한 것이 아닌 예외가 발생 시 어떻게 할 것인가? 가 중요하다.`**
- finally : 예외 발생 여부와 상관 없이 언제나 실행
    - 중간에 return을 만나도 finally 블록을 수행 후 리턴
    - try 선언문에 선언된 객체들이 AutoCloseable interface를 구현되 있다면 finally 역할

### Exception 처리 법

- try - catch
- catch에서 복구문장이 한 개도 없어도, JVM은 살아나게 설계되어있다.
- getMessager() : 시스템 정보 노출, 공백 : 에러상황 대응부재, printStackTrace : 에러 정보 노출
- 즉, catch 문에서는 에러 코드 테이블 준비와, 복구 코드가 있어야함.
- throws
    - main method에서 throws를 하면 안된다..?
    - 서버와 클라이언트는 각각 JVM이 있는데 서버쪽에서 발생한 에러에 대해서 try, catch를 처리를 하고 클라이언트에 넘겨주면 클라이언트쪽 JVM은 오류가 난 사실에 대해 전혀 모름
    - **`그래서 서버 -> 클라이언트로 throws 해주어야 한다.`**

```java
public class Test {

	public static void main(String[] args) {
// TODO Auto-generated method stubint y;
		int x = 100;
		y= Integer.parseInt(args[0]);
		try {
			Divider.divide(x, y);
		} catch (MyException e) {
// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}

		System.out.println("더 중요한 일 시작...");
	}

}

class Divider {
	public static void divide(int x,int y) throws MyException{
		if (y == 0) {// 비상사태 전에 일부로 비상사태를 만들었다.throw new MyException("ERROR-09");
		}
		System.out.println(x/y);
	}
}

class MyException extends Exception
{

	public MyException(String message) {
		super(message);
// TODO Auto-generated constructor stub
	}

}
```

**finally는 try 안에서 반드시 해야할 것들을 해주는 역할. 자원 낭비가 되지 않도록**
