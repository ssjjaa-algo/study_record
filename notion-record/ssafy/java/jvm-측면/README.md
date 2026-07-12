# JVM 측면

태그: 메모리

- Primitive : 기본형 (char, short, byte, int, long, float, double, boolean)
- Reference : 기본형으로 나타낼 수 없는 모든 것. (Class, Interface .... )

- Class loader가 클래스 파일을 로드
- Bytecode Verifier가 로드된 클래스 파일의 바이트 코드를 검사하여 유효성을 확인
- Machine Code Generator가 검증된 바이트 코드를 네이티브 기계 코드로 변환
- 최종적으로 JVM에 준다

CL -> BV -> MG -> JVM -> load -> static 멤버 초기화 -> 상속관계 파악 -> MAIN 수행 -> 클래스만나면 다시 load

데이터 연산의 오른쪽 부터 수행 -> 이를 JVM이 기억한다 = 레지스터에 기록되는 것임.

4byte에 38을 기억해두고, 이를 저장해야하는데?

이 때 저장하는 곳이 메모리이고, 이 Type이 method 안에 선언되어있고(Stack 영역)

int형이므로 4byte를 메모리에서 점유.

(오른쪽부터 수행한다!)

Name myName = new Name();

생성자 : non-static 멤버 초기화 --> 멤버 데이터들은 타입과 이름부터 본다 = 즉, 왼쪽부터 본다. = Default 초기화

그러므로.. 에덴 영역에 Name('성' '이름1' '이름2')

그렇게 class가 끝난 것 같은데? 사실.

myName에 슈퍼 생성자 코드가 생략되있으므로 Obejct가 에덴에 같이 붙고..

myName의 이름을 가진 객체를 저장하는데, 이는 stack 영역에 저장된다. 지역변수이기 때문에.

- 어떤 객체도 주소부터 할당받지 않음.

```bash
import entity.type.MyProfile;

public class Test {

	public static void main(String []args) {
		MyProfile m = new MyProfile();
		MyProfile m2 = new MyProfile();
		// default Package에서는 Myprofile을 못찾음.
		// 그래서 MyProfile을 import해준다.

		System.out.println(m);
		// 오류임. Package가 다르기 때문. System.out.println(m.myName);
		// public으로 값을 선언하면 가능.
		System.out.println(m.myName);
		System.out.println(m2.myName);
		System.out.println(m.myName==m2.myName);
		System.out.println(m2);
		System.out.println(m==m2);
	}
}
```

```bash
package entity.type;

public class Name {
	public char 성 = '전';
	public char 이름1 = '은';
	public char 이름2 = '수';

}
```

```bash
package entity.type;

public class MyProfile {
		// 타입 이름 할당연산자 값
		public int age = 38;
		public double tall = 160.5;
		public boolean isPretty=true;
		public char gender = '여';

		public Name myName = new Name(); // 호출(), Reference Type
		// 자바는 모든 데이터 타입을 본인이 직접 정의하는 것임.
		// 타입 이름 할당연산자 값 = Object
		// 즉, primitive Type은 값이 Object가 될 수 없다.
}
```

[배열]

(앞과정 생략) load -> static 멤버 초기화 -> 상속관계 파악 -> Main 수행

```bash
package array.test;

public class Test {

	public static void main(String[] args) {
		int i=10;
		int arr[] = new int[5]; // 얘의 타입은 int가 아니라 배열 타입이다.
		// arr안에 들어가는 원소의 타입이 int임

		// int arr[][] = new int[5][2] --> arr자체의 주소는 맨 마지막 할당임.

		System.out.println(arr); // 출력시 [I : 1차원 배열 & int]
		System.out.println(arr instanceof Object);
		for (int j=0; j<5; j++)
		{
			System.out.println(arr[j]);
		}

	}

}
```

자바에서의 메모리 관리는 전적으로 JVM이 담당하고 있기 때문에 프로그래머가 메모리 관리를 할 수 없으며,JVM에서 메모리가 부족하거나 유휴하다고 판단될 때 참조 횟수 기반으로 garbage collector가 동작해서 메모리를 관리해줍니다.+ young 영역과 eden 영역이 분리되어 있기도 합니다.

자바에서는 C언어와 같이 힙메모리에 동적할당된 메모리를 해제해주는 메서드가 존재하지 않습니다. 따라서 제한된 힙 메모리를 Out of memory가 발생하지 않도록 VM이 판단하여 GC를 실행하는 과정이 필요합니다
