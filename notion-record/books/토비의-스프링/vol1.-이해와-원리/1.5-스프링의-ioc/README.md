# 1.5 스프링의 IoC

## 오브젝트 팩토리를 이용한 스프링 IoC

- **`빈(Bean)`**
    - 스프링이 제어권을 가지고 직접 만들고 관계를 부여하는 오브젝트
    - 오브젝트 단위의 애플리케이션 컴포넌트
    - 스프링 컨테이너가 생성과 관계 설정, 사용 등을 제어
- **`빈 팩토리(bean Factory)`**
    - 빈의 생성과 관계 설정같은 제어를 담당하는 IoC 오브젝트
    - 주로 이를 더 확장한 **애플리케이션 컨텍스트(application context)** 사용
    - 설계도의 역할을 한다고 보자
    - 애플리케이션 로직을 담지는 않으나 **생성, 관계를 맺는 책임**

### 애플리케이션 컨텍스트

```java
@Configuration
public class DaoFactory {
		@Bean
		public UserDao userDao() {
			return new UserDao(connectionMaker());
		}

		@Bean
		public ConnectionMaker connectionMaker() { 
			...
		}
}
```

```java
public class UserDaoTest {
		public static void main(String[] args) throws ClassNotFoundException, SQLException {
			ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
			UserDao dao = context.getBean("userDao", UserDao,class);
		}
}
```

- userDao에 Bean을 붙여서 이름이 userDao
- getBean()은 기본적으로 **Object 타입**
    - 캐스팅을 피하기 위해 두 번째 파라미터에 리턴 타입 명시

### 애플리케이션 컨텍스트 동작방식

- ApplicationContext 인터페이스를 구현했고, BeanFactory 인터페이스를 상속했기 때문에 일종의 빈 팩토리임.
- 생성정보와 연관관계 정보를 별도의 설정정보를 통해 얻음

- 장점
    - 클라이언트가 구체적인 팩토리 클래스를 알 필요가 없다.
    - 종합 IoC 서비스 제공
        - 오브젝트 만들어지는 방식, 시점과 전략도 다르게 가져갈 수 있다
        - 자동생성, 오브젝트 후처리 등
    - 빈을 검색하는 다양한 방법 제공
        - getBean() : 빈의 이름을 이용해 빈을 찾아줌
        - 타입만으로 빈을 검색하거나, 특별한 애노테이션 설정이 되어 있는 빈을 찾는 것도 가능

## 스프링 IoC의 용어 정리

### 빈(bean)

- 스프링이 IoC 방식으로 관리하는 오브젝트
    - 스프링이 **직접 그 생성과 제어를 담당하는 오브젝트만** 빈임

### 빈 팩토리(bean factory)

- 스프링 IoC를 담당하는 핵심 컨테이너
- 빈을 등록, 생성, 조회하고 돌려주고, 부가적인 빈을 관리하는 기능
- 보통 애플리케이션 컨텍스트를 이용

### 애플리케이션 컨텍스트

- 빈 팩토리를 확장한 IoC 컨테이너
- 빈 팩토리에 스프링이 제공하는 각종 부가 서비스를 추가로 제공
- **BeanFactory를 상속**

### 설정 정보/설정 메타 정보(configuration metadata)

- 애플리케이션 컨텍스트 또는 빈 팩토리가 IoC를 적용하기 위해 사용하는 메타정보
    - configuration
- IoC 컨테이너에 의해 관리되는 애플리케이션 오브젝트를 생성하고 구성할 때 사용

### 컨테이너 또는 IoC 컨테이너

- IoC 방식으로 빈을 관리하는 의미

### 스프링 프레임워크

- IoC 컨테이너, 애플리케이션 컨텍스트를 포함해서 스프링이 제공하는 모든 기능을 통틀어 말함
