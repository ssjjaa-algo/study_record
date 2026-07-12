# 직렬화

[자바 직렬화, 그것이 알고싶다. 훑어보기편 | 우아한형제들 기술블로그](https://techblog.woowahan.com/2550/)

# 개념

- 자바 시스템 내부에서 사용되는 객체 또는 데이터를 외부의 자바 시스템에서도 사용할 수 있도록 바이트(byte) 형태로 데이터 변환하는 기술
- 바이트로 변환된 데이터를 다시 객체로 변환하는 기술(역직렬화)
- 시스템적 이야기
    - JVM의 메모리에 상주하고 있는 객체 데이터를 바이트 형태로 변환하는 기술
    - 직렬화된 바이트 형태의 데이터를 객체로 변환해서 JVM으로 상주시키는 형태

# 자바 직렬화 대상

- Primitive type
- **`java.io.Serializable`** 인터페이스를 상속받은 객체
    - 상속받지 않으면 직렬화 불가.

# 직렬화 방법

- **`java.io.ObjectOutputStream`** 객체 이용

```java
Obj obj = new Obj("직렬화","하겠다");
byte[] serializeObj;
try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
		oos.writeObject(obj);
		// 직렬화된 obj 객체
		serializeObj = baos.toByteArray();
		}
}
System.out.println(Base64.getEncoder().encodeToString(serializeObj));
```

- 객체를 직렬화하여 바이트 배열(byte [])형태로 변환

# 역직렬화

- 직렬화 대상이 된 객체의 클래스가 클래스 패스에 존재하며 import 되어있어야 한다.
    - 직렬화와 역직렬화를 진행하는 시스템이 서로 다를 수 있다는 것을 고려.

```java
String base64Obj = "...";
byte[] serializeObj = Base64.getDecoder().decode(base64Obj);
try (ByteArrayInputStream bais = new ByteArrayInputStream(serializeObj)) {
	try (ObjectInputStream ois = new ObjectInputStream(bais)) {
		// 역직렬화된 Obj 객체 읽어오기
		Object objectObj = ois.readObject();
		Obj obj = (Obj) objectObj;
	}
}
```

<aside>
💡 "자바 직렬화 형태의 데이터 교환은 **`자바 시스템 간의`** 데이터 교환을 위해서 존재한다."

</aside>

# 자바 직렬화는 언제 어디서 사용?

- JVM의 메모리에서만 상주되어있는 객체 데이터를 그대로 영속화가 필요할 때
- 서블릿 세션
    - 파일 저장, 세션 클러스터링, DB를 저장하는 옵션 등 선택 시 세션 자체가 직렬화되어 저장되어 전달
- 캐시
    - 동일 요청이 들어올 시 저장된 객체를 찾아서 응답하게 하는 형태
    - 이 부분을 자바 직렬화된 데이터를 저장해서 사용.
- 자바 RMI
