# 4. 유스케이스 구현하기

# 도메인 모델 구현하기

```java
package bukpal.domain;

public class Account {

	private Account id;
	private Money baselineBalance;
	private ActivityWindow activityWindow;

 // 생성자, getter 생략

	public Money calculateBalance() {
		return Money.add(
		this.baselineBalance,
		this.activityWindow.calculateBalance(this.id));
	}

	public boolean withdraw(Money money, AccountId targetAccountId) {
		if (!mayWithdraw(money)) {
			return false;
	}
	
	Activity withdrawal = new Activity (
	this.id,
	this.id,
	targetAccountId,
	LocalDateTime.now(),money);
	
	this.activityWindow.addActivity(withdrawal);
	return true;
	}

	private boolean mayWithDraw(Money money) {
		return Money.add(
			this.calculateBalance(),
			money.negate())
		.isPositive();
	}

	public boolean deposit(Money money, AccountId sourceAccountId) {
		Activity deposit = new Activity (
			this.id,
			sourceAccountId,
			this.id,
			LocalDateTime.now(),
			money);
		this.activityWindow.addActivity(deposit);
		return true;
	}

}
```

- 위 엔티티를 기준으로 유스케이스를 구현, 바깥 방향으로 나아간다.

# 유스케이스 둘러보기

1. 입력을 받는다
2. 비즈니스 규칙을 검증한다
3. 모델 상태를 조작한다
4. 출력을 반환한다

```
- 인커밍 어댑터로부터 입력을 받는다.
- 비즈니스 규칙을 충족하면 입력을 기반으로 모델의 상태를 변경
- 아웃고잉 어댑터에서 온 출력값을 유스케이스를 호출한 어댑터로 반환할 출력 객체로 변환
```

```java
package buckpal.application.service;

@RequiredArgsConstructor
@Transcational
public class SendMoneyService implements SendMoneyUseCase {
	private final LoadAccountPort loadAccountPort;
	private final AccountLock accountLock;
	private final UpdateAccountStatePort updateAccountStatePort;

	@Override
	public boolean sendMoney(SendMoneyCommand command) {
		// 비즈니스 규칙 검증
		// 모델 상태 조작

		// 출력 값 반환
	}
}

```

# 입력 유효성 검증

- 애플리케이션 계층에서 입력 유효성을 검증해야 하는 이유
    - 애플리케이션 코어의 바깥쪽으로부터 유효하지 않은 입력값을 받게 됨
    - 모델의 상태를 해칠 수 있음

<aside>
💡 어디에서 이 유효성을 검증하느냐?

</aside>

- 입력 모델(input model)에서 해보자.
    - 생성자 내에서 입력 유효성을 검증해보자.
    
    ```java
    package bukpal.application.port.in;
    
    @Getter
    public class SendMoneyCommand {
    
    	private final AccountId sourceAccountId;
    	private final AccountId targetAccountId;
    	private final Money money;
    
    	public SendMoneyCommand(
    		AcountId sourceAccountId,
    		AccountId targetAccountId,
    		Money money) {
    	this.sourceAccountId = sourceAccountId;
    	...
    	requireNonNull(sourceAccountId0;
    	...
    	}
    }
    ```
    
    - final을 지정해 불변 필드
        - 생성에 성공하고 나면 상태는 유효하고 이후에 잘못된 상태로 변경할 수 없다
    - SendMoneyCommand는 유스케이스 API의 일부, 인커밍 포트 패키지에 위치
    - 유효성 검증이 애플리케이션 코어(육각형 아키텍처의 육각형 내부)에 위치하지만 유스케이스 코드를 오염시키지 않는다.
    
    <aside>
    💡 자바의 Bean Validation API 표준 라이브러리를 사용해서 간단하게 변경하자.
    **`@NotNull`** 등
    
    </aside>
    
    ```java
    package bukpal.application.port.in;
    
    @Getter
    public class SendMoneyCommand extends SelfValidating<SendMoneyCommand> {
    	@NotNull
    	private final Account.AccountId sourceAccountId;
    	...
    
    	public SendMoneyCommand(
    		Account.AccountId sourceAccountId,
    		...
    	) {
    		this.sourceAccountId = sourceAccountId;
    		...
    		this.validateSelf();
    	}
    }
    ```
    
    ```java
    package shared;
    
    public abstract class SelfValidating<T> {
    	private Validator;
    
    	public SelfValidating() {
    		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    		validator = factory.getValidator();
    	}
    
    	protected void validateSelf() {
    		Set<ConstraintViolation<T>> violations = validator.validate((T) this);
    		if(!violations.isEmpty()) {
    			throw new ConstraintViolationException(violations);
    		}
    	}
    }
    ```
    
    - 유스케이스 구현체 주위에 오류 방지 계층(anti corruption layer)
    - 잘못된 입력을 호출자에게 돌려주는 유스케이스 보호막

# 생성자의 힘

- 책에서 말하는 의도는 빌더보다 생성자가 더 좋다는 의도인 듯 하다.
    - SendMoneyComandBuilder에 필드를 새로 추가했을 때
    - **`무언가 추가해야 할 때`** **잠깐 한 눈 팔다가 새로운 필드를 추가하는 것을 잊는다는 것**이다.
    - 생성자를 직접 사용한다면, **컴파일 에러를 이용할 수 있다.**

# 유스케이스마다 다른 입력 모델

- 각기 다른 유스케이스에 동일한 입력 모델을 사용하고 싶을 때가 있다.
    - 계좌 등록하기, 계좌 정보 업데이트하기
    - 둘 모두 거의 똑같은 계좌 상세 정보가 필요하다
    - 차이점
        - 업데이트할 계좌를 특정하기 위해 계좌 ID 정보 필요
        - 계좌를 귀속시킬 소유자의 ID 정보 필요
- 문제점
    - 불변 커맨드 객체의 필드에 null을 유효한 상태로 받아들인다 → **`code smell`**
    - 유효성 검증은 어떻게 해야하냐?

# 비즈니스 규칙 검증하기

- 언제 입력 유효성을 검증하고 언제 비즈니스 규칙을 검증해야 하나?
- 차이점
    - 비즈니스 규칙 검증
        - 도메인 모델의 현재 상태에 접근
        - 조금 더 맥락이 필요하다.
            - **`의미적인(semantical)`**
    - 입력 유효성 검증
        - @NotNull 처럼 선언적으로 구현 가능.
        - **`구문 상(syntactical)`** 유효성을 검증하는 것이라고도 할 수 있음.

- 출금 계좌는 초과 출금되어서는 안된다
    - 출금 계좌와 입금 계좌가 존재하는지 확인하기 위해 모델의 현재 상태에 접근해야 함
        - **비즈니스 규칙**

- 송금되는 금액은 0보다 커야 한다
    - 모델에 접근하지 않고도 접근할 수 있다.
        - **입력 유효성 검증**

- 어떻게 구분?

<aside>
💡 유효성 검증 로직이 현재 모델의 상태에 접근해야 하는지 여부만 확인하면 된다.

</aside>

- 비즈니스 규칙
    - 가장 좋은 방법은 도메인 엔티티 안에 넣는 것.
    
    ```java
    package buckpal.domain;
    
    public class Account {
    
    // ...
    
    	public boolean withdraw(Money money, ...) {
    	
    	if(!mayWithdraw(money)) return false;
    
    	// ...
    	}
    }
    ```
    
    - 규칙을 지켜야하는 비즈니스 로직 바로 옆에 규칙이 위치 → 추론이 쉽다.
    - 의도하는 바
        - 도메인 모델을 로드해야 한다면, 도메인 엔티티 내에 비즈니스 규칙 구현

# 풍부한 도메인 모델 vs 빈약한 도메인 모델

<aside>
💡 각자의 필요에 맞는 스타일을 선택해서 사용한다

</aside>

### DDD를 따르는 풍부한 도메인 모델(rich domain model)

- 애플리케이션의 코어에 있는 엔티티에서 가능한 많은 도메인 로직이 구현
- 엔티티들은 상태를 변경하는 메서드 제공, 비즈니스 규칙에 맞는 유효한 변경만을 허용

<aside>
💡 비즈니스 규칙이 유스케이스 구현체 대신 **엔티티에 위치**

</aside>

### 빈약한 도메인 모델(anemic domain model)

- 상태를 표현하는 필드와 이 값을 읽고 바꾸기 위한 getter, setter만 포함
- 어떤 도메인 로직도 가지고 있지 않음.

<aside>
💡 도메인 로직이 **유스케이스 클래스에 구현**돼있는 것.

</aside>

- 아웃고잉 포트에 엔티티를 전달할 책임 역시 유스케이스 클래스에 있다.

# 유스케이스마다 다른 출력 모델

- 유스케이스들 간에 **같은 출력 모델을 공유하게 되면 유스케이스들도 강하게 결합된다**
    - 한 유스케이스에서 새로운 필드가 필요해진다?
        - 다른 유스케이스 필드에서도 처리해야 한다.

<aside>
💡 **`단일 책임 원칙으로 모델 분리`** → 유스케이스 결합을 제거하는데 도움

</aside>

- 같은 이유로, 도메인 엔티티를 출력 모델로 사용하고 싶은 유혹 역시 피하자.

# 읽기 전용 유스케이스

- 프로젝트 맥락에서 유스케이스로 간주되지 않는다면?
    - 실제 유스케이스와 구분하기 위해 **쿼리**로 구현할 수 있다.
- 책에서 소개하는 방법
    - 인커밍 전용 포트를 만들고 쿼리 서비스에 구현하는 것

# 유지보수 가능한 소프트웨어 만드는데 어떤 도움?

- 입출력 모델을 독립적으로 모델링하면 원치 않는 부수효과를  피할 수 있다.
- 유스케이스별로 모델 만들기
    - 유스케이스의 명확한 이해
    - 장기적으로 유지보수 도움
    - 여러 명의 개발자가 다른 유스케이스를 건드리지 않고 동시 작업 가능.
