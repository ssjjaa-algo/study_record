# Inheritance Encapsulation Polymorphism

### Encapsulation : **data는 무조건 private, method는 4가지 고려 사용**, data를 public으로 사용할 때는 읽기만 할 때

- 데이터가 잘못 입력될 것을 고려해야한다
- 그래서 금고에 넣는다 == private
- 그러면 어떻게 특정 변수에 접근할것이냐 ? -> set을 이용해서.. 즉, set이 '유효성 검사'인 것이다.
- 조건문 (if-else)의 경우 else도 꼭 포함시키는 것이 보안, Secure code이다
- break도 권장
- 서버 -> 클라이언트 결과 넘겨줄 시 return값 필요하고 알려줄 필요가 있다.

### Modifier [private, public, protected, default]

- private : 자기 클래스 내에서만 접근 가능
- default : 같은 패키지 내에서만 접근 가능
- protected : 다른 패키지라고 '상속' 관계에 있으면 접근 가능
- public : 어디서나 접근 가능

- 사용 지정자
    - static : "객체 생성 없이" 사용하자.
    - final : 변경 없이 사용하자.
        - class 앞에 final 붙이면 상속 불가
        - 변수 앞에 붙이면 변경 불가
        - method 앞에 붙이면 Overriding 불가
    - abstract : 구체적이지 않으니 메모리에 올리지 마세요!로 생각하자.추상적이므로 구체화해야하는데? 그러므로 상속을 해서 구체화한 것을 써야한다.
        - 상속을 쓰는 것은 단일상속이기 때문에 잘 고려해야 하는것.
        - 인터페이스는 다중이 가능해서 되도록이면.. 인터페이스 ..

### inheritance(상속)

- 상속받은 클래스와 똑같은 이름의 변수 : 가능.
- 상속이란 단순히 물려받는 것이 아니라, 일상 생활에 있는 것을 프로그래밍에 녹여낸 것
- 데이터나 메소드가 중복되있는 형태의 객체가 Super 타입으로 될 때는 자기 것이 가려지고
- 그것을 Shadow Effect라고 한다.
- 상속은 확장이지만 제한의 의미도 가진다. Classification : 분류
- Class는 **`이름만으로도 가치가 있을 수 있다`**. (Shape -> 원, 직사각형, 삼각형 생각)
- super 클래스를 상속했는데, super클래스를 매개로 가지고 Down Class해서 보고싶다?
    - **if ( 부모 instanceof 아들 )**
    - **근데 이렇게 하면 할수록 확장성은 높아지나 성능(Perfomance)가 너무 낮아지더라..**
    - 상속을 지향하다보면 무조건적으로 확장성과 성능문제의 딜레마에 빠지게 된다.
    - 그래서 JVM에 예외 상황을 만들었는데, 그것이 **`Override`**
- Super에 method가 미리 있고 DownClass의 메소드를 호출하고 싶다면
- 어떤 객체가 들어와도 컴파일 시 DownClass의 Type을 찾아가서 자동으로 해준다.
- **Method는 Method Area에 남아있고 아래서부터 찾는다..!**

<aside>
💡 Overriding을 왜하냐? Shadow Effect의 예외 상황. 확장성과 성능을 다 잡기 위해

- **상속을 지향하는 프로그래밍에서는 확장성과 성능의 필연적 고민이 필요한데**
- **이것을 위해서 Shadow Effect에 안걸리게 하는 예외 상황을 만든 것**
</aside>

### Polymorphism : 하나의 객체가 많은 형(타입)을 가질 수 있는 성질

- : Poly(많다) / morphism(Map으로 생각하자)
- : 많을 다 / 형태 형 = 많은 타입.
- 황금잉어빵은 붕어빵인가? 그렇다. 황금잉어빵 is a 붕어빵 : 상속 관계.
    - **상속 관계**에 있을 때 조상 클래스의 타입으로 자식 클래스 객체를 레퍼런스 할 수 있다.
- 다형성으로 다른 타입의 데이터를 하나도 묶는다.
- 업캐스팅 시에는 묵시적 형변환 (is a)의 개념으로 조상 타입으로 자식 객체 참조
- 기본형은 Wrapper클래스를 이용해 받을 수 있다. : autoboxing한다.
- 큰 집(super)에서 작은 집(child)로는 **명시적 캐스팅 필요**
- **자식이 부모가 되려면 특별한 과정이 거쳐야 될 수 있는 것임.**
    - **근데 형변환을 통해서 자식 -> 부모 SpiderMan sman = (SpiderMan) person**
    - **실제로 자식 영역 메소드 사용 불가.**

Person person = new SpiderMan();

- autobox
    - **`Wrapper 클래스로 직접 변경하는 것이 낫다`.** 되도록이면 사용하지 말자.
    - 왜냐면, autobox는 훨씬 더 많은 퍼포먼스를 요구한다.
        - 기본이 Primitive -> Reference는 불가. 근데, 사용자 요구에 의해 '컴파일러'를 고친 형태라서..
        - 컴파일러가 다시 잡아주는 것이.. 상당히 퍼포먼스에 악영향을 끼친다.

### **[abstract class] : 추상 클래스**

- 상속 전용의 클래스, 객체를 생성할 수 없음
- 하지만 상위클래스 타입으로써 자식을 참조할 수는 있다.
- 클래스들의 공통 분모를 뽑아서 상속 구조를 만든다.
- 전기차 / 디젤차 : 둘다 "연료를 넣는다"를 가짐 -> 중복 요소
- 그래서 공통적인 것은 묶어서 '상속'할 것이다.
- 상속하여 '다형성'으로 구현하면 된다.
- 근데 '공통'의 메소드에 있어 조상의 **구현은 무의미**. 그러니까, **메소드에서 '선언'만 해놓으면 된다!**
- **구현부가 없다는 의미로 abstract를 붙여주어야 함. 객체 생성이 불가.**
    - **public abstract class Vehicle{    }**
    - **public abstract void addfuel();**
- **조상클래스에서 상속받은 abstract 메서드를 재정의 하지 않은 경우**
    - **클래스 내부에 abstract 메서드가 있는 상황이므로 자식 클래스는 abstract 클래스로 선언되어야 함**
    - **abstract 클래스는 구현의 강제를 통해 프로그램의 안정성 향상**
    - **`이거 왜이렇게 했냐고? 개발자들 실수하지 말라고.. 즉 개발자 편의성..`**

### **Interface**

- 인터페이스 : 서로 다른 두 시스템 장치, 소프트웨어 따위를 서로 이어 주는 부분. 또는 그런 접속 장치.
- GUI (Graphic User Interface)
- 인터페이스를 사이에 두고 한 쪽은 사용에 관심, 한 쪽은 구현에 관심
- 최고 수준의 추상화 단계 : 일반 메서드는 모두 abstract 형태로 작성되있음
- 클래스와 유사하게 선언 public interface myinterface
- 모든 멤버변수는 public static 이며 생략 가능
- 모든 메서드는 public abstract 이며 생략 가능
- 메서드를 재정의하려면 public으로 생성해야된다
- 인터페이스는 다중 상속이 가능하다
- 구현부가 없기 때문에 헷갈릴 메서드가 없다.
- 인터페이스는 object와 관련이 없다. 즉 object를 상속받지도 않음
- 클래스에서 implements 키워드를 사용해서 interface를 구현
- 구현의 강제로 표준화 처리 -> 손쉬운 모듈 교체
- 서로 상속의 관계가 없는 것에 다형성 부여
- 독립적인 프로그래밍으로 개발 기간 단축

**Default method**

- 인터페이스에 선언 된 구현부가 있는 일반 메서드
- 메서드 선언부에 default modifier 추가 후 메서드 구현부 작성
- 필요성 : interface 기반으로 동작하는 라이브러리의 interface에 '추가'해야 하는 기능 발생?
    - 기존 클래스에 Override를 전체 다 해야하는 불편함이 존재
    - Default method는 abstract가 아니므로 반드시 구현해야 할 필요는 없어짐.
- method의 우선순위
    - super class의 method 우선 : super가 구체적인 메서드를 갖는 경우 default
