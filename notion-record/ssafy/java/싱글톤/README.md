# 싱글톤

```bash
package test3.singleton;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
			A o1= A.getInstance();
			A o2= A.getInstance();
			System.out.println(o1==o2); // false
			//객체의 사용이 남발되면?

	}

}

class A extends Object // 싱글톤 코드
{
	private static A a; // 1) 여기에서 new A()를 하는 경우 클래스 내에서 null이 생기는 경우가 발생할 수 있다. 싱글톤은 무조건 1개를 생성해야 되는데
	private A()
	{
		super();
	}

	public static A getInstance() // 객체 생성 없이도 사용할 수 있도록.
	{
		if (a ==null) a= new A(); // 2) 그래서 여기에 있어야 객체를 무조건 1개는 생성할 수 있게 하는 것임.
		return a;
	}
}
```

### [Singleton 디자인 패턴]

- 여러 개의 객체가 필요 없는 경우
    - 객체를 구별할 필요가 없는 경우 = 수정 가능한 멤버 변수가 없고 기능만 있는 경우 (statelsess한 객체)
    - 외부에서 생성자에 접근 금지 -> 생성자의 접근 제한자를 **`private`**로 설정
    - 내부에서는 private에 접근 가능하므로 '직접' 객체 생성 -> 멤버 변수이므로 private 설정
    - 외부에서 private member에 접근 가능한 getter 생성
    - **객체 없이 외부에서 접근할 수 있도록 getter와 변수에 static 추가**
    - **외부에서는 절대 객체를 볼 수 없기 때문에 미리 static으로 올리는 것임**
    - 외부에서는 **`언제나 getter를 통해서 객체를 참조`**하므로 **`하나의 객체 재사용`**
