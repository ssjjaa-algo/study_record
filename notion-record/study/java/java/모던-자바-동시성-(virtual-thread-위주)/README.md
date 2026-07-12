# 모던 자바 동시성 (virtual thread 위주)

# 자바의 Thread

- 자바의 스레드 모델은 스레드 스케줄링 및 실행과 관련하여 **운영체제**에 의존
- 운영체제는 실행 효율을 보장하기 위해 가 스레드에 CPU 시간을할당하고, 스레드 사이의 상태 전환도 관리
    - 스레드를 다수의 CPU에분배하면 병렬성을 높일 수 있음
- 자바에서 더 많은 스레드를 사용 → 더 많은 실행 환경 → 여러 연산 동시 수행

## 디버거와 스레드

- 자바 디버거가 애플리케이션에서 멈추는 지점을 확인하는 것 역시 스레드에서 함
- 디버거는 자기 자신을 애플리케이션의 여러 스레드에 연결
    - 이를 통해 어떤 스레드를 조사할지 선택할 수 있고 스레드의 상태를 변경할 수도 있다.
- 스텝 인투, 스텝 아웃 등은 모두 하나의 스레드 수준에서 동작하는 기능

## 스레드 비용

- 힙 외부에서 약 2Mib에 달하는 메모리 공간을 사용함 → 수천 개의 동시요청에 발생하는 비용은 상당히 크다
- 자바 스레드는 운영체제가 제공하는 네이티브 스레드를 얇게 감싼 스레드
    - 따라서 애플리케이션에서 생성할 수 있는 최대 스레드 개수는 운영체제의 네이트브 스레드 최대 개수에 의해 제한된다
        - Linux 기준 /proc/sys/kernel/threads-max에서 설정된 값이 최대값
- 컨텍스트 스위치 비용 → CPU 사이클 소모

### 생성 가능한 스레드 개수

```java
public class ThreadLimitTest {
	public static void main(String[] args) {
		var threadCount = new AtomicInteger(0);
		try {
			while(true) {
				var thread = new Thread(() -> {
					threadCount.incrementsAndGet();
					LockSupport.park();
				});
				thread.start();
				}
		} catch (OutOfMemoryError error) {
			System.out.println("limit: " + threadCount);
			error.printStackTrace();
		}
	}
}
```

## AtomicReference

- 스레드사이에서 안전하게 객체 참조를 공유하기 위함
- CAS 알고리즘을 이용해 혀냊 스레드가 알고 있는 이전 값과 비교하여 일치할 때만 새로운 값으로 변경하고, 일치하지 않으면 재시도하여 안정성 유지한다

## InterruptedException 처리하기

- 자바에서 InterruptedException을잡을 때에는 스레드의 상태를 인터럽트된 상태로 유지해야 함
- 따라서 Catch후 Thread.currentThread().interrupt()를 수행해야 함
- 만약 그냥 catch만 하면 인터럽트 플래그가 지워져서 호출 스택의 상부로 예외가 전달되지 않아서 문제가 될 수도 있음

## Executor framework

```java
try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
	var person = getPerson(personId);
	
	var assetsFuture = executor.submit(() -> getAssets(person));
	var liabilitiesFuture = executor.submit(() -> getLiabilities(person));
	executor.submit(() -> importantWork());
	
	// 필수 데이터를 가져오는 태스크 완료 대기 후 계산
	return calculateCredits(assetsFuture.get(), liabilitiesFuture.get());
	
}
```

- 스레드 무분별 생성을 피함
- 스레드 생애주기를 관리해서 효율적으로 사용하도록 해줌
- 자세한거 공식문서 참조

### 제약 사항

- Future.get()
    - 결국 블로
