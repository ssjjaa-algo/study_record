# String

```bash
package string.test;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StringBuffer s1 = new StringBuffer("java");
		StringBuffer s2 = new StringBuffer("java");

		s1.append(" study");
		StringBuffer s3 = s2.append(" study");

		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);

	}

}
/*
	StringBuffer는 무조건 메소드로만 변경
  	처음에 16자리로 만든다. 처음에 16자리로 시작하기 때문에, 만약 16자리 범위안에 있다면 바로 채운다.
  	그 이후로 32, 64 ... 이런 식의 증가
  	가공이 많이 할거면, StringBuffer.

  	StringBuffer는 lock 시스템이 있음.
  	StringBuilder는 lock 시스템 제거한 것.

  	동작은 똑같으나 둘의 차이는 '동시성' 문제.
  	동시성이 필요없다면 Builder, 우려된다면 Buffer

  	그래서..
  	String 쓰면 "  "

  	String 가공 =>
  	1) 멀티쓰레드 : StringBuffer
  	2) 단일쓰레드 : StringBuilder

*/
```

```bash
package string.test;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s1 = new String("java");
		String s2 = new String("java");
		String s3 = "java"; // 객체와 바로 선언하는 것이 저장되는 곳이 다르다.
		String s4 = "java";

		System.out.println(s1);
		System.out.println(s2);
		System.out.println(s3);
		System.out.println(s4);

		System.out.println(s1 == s2);
		System.out.println(s3 == s4);

		String s5 = s3+" study";
		System.out.println(s5);
		System.out.println(s4);
		System.out.println(s3);

		// s3 = s3+" study"; 이렇게 말고
		s3 = s3.concat(" study"); // 이렇게 하면
		System.out.println(s4);
		System.out.println(s3);

		s2 = s1.concat(" study");
	}

}
/*
load -> (main x) static -> 상속 -> main

java Test 전,은,수 같이 했을 경우
JVM은 메인을 수행할 때 String을 보고서 String을 load한 적이 있냐고 물어본다.
없으므로 로드하고 static 영역에 String을 올려둔다. (static void main)
String의 상속을 파악하고

상자는 객체이므로 String[] args가 instance 영역에 생김.
처음에 null로 3칸이 default 저장

지금은 그냥 java Test이므로, 3칸이 지정이 아니라 몇 칸인지 모르는 상태.
String s1 =new String("java")의 경우 String 배열 4칸짜리가 Default, Object까지 묶임.
그러면 java가 들어가고 s1이라는 로컬 변수가 주소를 가리킴.

String s2 = new String("java")의 경우 똑같은 과정 생성.. 다만 다른 객체이기 때문에 s1 == s2는 다르다. (주소가 다름)

new를 하면 계속 메모리 생성..
그래서 더블 쿼테이션. String s3 = "java"같은 것임.

new를 안하고 더블 쿼테이션을 하면 String literal Pool 공간으로 저장된다
String literal Pool은 Final 영역이므로 '불변'

SlP에는 c(char)-value comparator가 있는데 이를 이용해 문자가 있는지 확인하고
없으면 char 개수만큼 공간 할당 (String)
그리고 method와 링크, Object도.
java 값 임력 후, 주소 할당.

이 떄 String s4 ="java"를 하면 이미 s3이 Slp에 들어가있고, c-value comparator가 이미 있다고 판단했기 때문에
s3과 s4는 같은 것이 된다.

즉, 중복 방지.

한 편 s3과 s4는 지금 같은 값을 가지고 있는데,
s3 = s3 + "study"를 할 경우 기존에 가리키던 주소를 끊고 새로운 것을 가리킴.
계속 생성한다는 소리.

*Garbage Collector는 instance영역만 쳐다본다.

s1과 s2도. 기존에 가리키던 객체를 변경하고 싶은 경우 원본 변경 불가하고 새로 생성해야 함.
주소를 또 가르킨다는 것임.

가공을 해야한다면 concat 형태로. 왜냐면, Garbage Collector의 범위에 들어와있기 때문에.

concat 역시 객체. 인스턴스 영역에 새로 만든다는 것. 기존에 누군가 가리키던 객체는 Garbage
SLP 영역은 Garbage이나 collect 되지 않음

garbage collector를 이용하려면 모든 변수에 null 주면 된다.

*/
```

load - (main x) - static 초기화 -> 상속관계파악 -> main 수행 반복한다.

생성자가 하는 일  : non-static 멤버 초기화

클래스 멤버 변수는 할당 연산자 왼쪽부터 시행.

생성자의 첫 수행은 super()

그래서 Object 만나고 Object를 가져온다.

non-static과 method를 가져오고, method는 m/a에서 링크걸어서 연결.

new를 instance영역에

로컬데이터는 초기화하지 않으면 에러가 나고

멤버데이터는 괜찮. 얘들은 타입과 이름만 먼저 보고

로컬은 값부터 보기 때문에.

method 영역과 instance 영역 합쳐서 힙 영역

가비지 컬렉터는 인스턴스 영역만 쳐다본다.
