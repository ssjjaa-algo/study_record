# Virtual Thread 병렬 작업에서 예외 처리 방식 비교

- `aMethod()` : Thread.sleep(5000) 후 `1` 반환
- `bMethod()` : 즉시 예외 발생
- `add(a, b)` : 두 결과를 더함

```java
    static int aMethod() throws InterruptedException {
        try {
            System.out.println("aMethod 시작: " + Thread.currentThread());
            Thread.sleep(5000);
            System.out.println("aMethod 종료");
        } catch (InterruptedException e) {
            throw e;
        }
        return 1;
    }

    static int bMethod() {
        System.out.println("bMethod 예외 발생: " + Thread.currentThread());
        throw new RuntimeException("bMethod 실패");
    }

    static int add(int a, int b) {
        return a + b;
    }
```

---

# 1. 비구조적 동시성에서 예외를 처리하지 못하는 경우

```java
Future<Integer> a = executor.submit(() -> aMethod());
Future<Integer> b = executor.submit(() -> bMethod());

try {
    int result = add(a.get(), b.get());
    System.out.println(result);
} catch (Exception e) {
    System.out.println("예외 발생: " + e.getCause());
}
```

## 동작

- bMethod에서 즉시 예외를 발생 시켜도 a 메서드는 종료되지 않고 계속 실행됨
  - 하나의 결과가 끝났다고 하더라도 메인 스레드에서 블로킹되어 있어서 다른 결과를 리턴 못하는 상황
- 즉 하나가 실패해도 다른 작업을 즉시 중단하지 않기 때문에 `리소스 낭비`가 발생
- `작업 실패 전파가 자동으로 일어나지 않음`

```java
bMethod 예외 발생: VirtualThread[#28]/runnable@ForkJoinPool-1-worker-2
aMethod 시작: VirtualThread[#26]/runnable@ForkJoinPool-1-worker-1
aMethod 종료
예외 발생: java.lang.RuntimeException: bMethod 실패
```

---

# 2. 비구조적 동시성에서 수동으로 예외를 처리

```java
CompletionService<Integer> cs = new ExecutorCompletionService<>(executor);

Future<Integer> aFuture = cs.submit(() -> aMethod());
Future<Integer> bFuture = cs.submit(() -> bMethod());

List<Future<Integer>> futures = List.of(aFuture, bFuture);

try {
    for (int i = 0; i < futures.size(); i++) {
        results.add(cs.take().get());
    }

    int result = add(results.get(0), results.get(1));
    System.out.println("result = " + result);

} catch (ExecutionException e) {
    futures.forEach(f -> f.cancel(true));
    System.out.println("예외 즉시 감지: " + e.getCause());
}
```

```java
aMethod 진입
bMethod 예외 발생
예외 즉시 감지: java.lang.RuntimeException: bMethod 실패
```

## 동작

- `CompletionService`는 작업 완료 순으로 꺼낼 수 있음
- 따라서 `bMethod()`가 먼저 실패하면 `cs.take().get()`에서 즉시 `ExecutionException`을 감지할 수 있다.
- 그 후 직접 나머지 작업을 취소
    - cancle을 통해 다른 thread에 interrupt 시킨다

```java
futures.forEach(f -> f.cancel(true));
```

---

# 3. StructuredTaskScope

```java
try (var scope = StructuredTaskScope.open()) {

    StructuredTaskScope.Subtask<Integer> a = scope.fork(() -> aMethod());
    StructuredTaskScope.Subtask<Integer> b = scope.fork(() -> bMethod());

    scope.join();

    int result = add(a.get(), b.get());
    System.out.println(result);

} catch (Exception e) {
    System.out.println("예외 발생: " + e);
}
```

```java
aMethod 시작: VirtualThread[#26]/runnable@ForkJoinPool-1-worker-1
bMethod 예외 발생: VirtualThread[#28]/runnable@ForkJoinPool-1-worker-2
aMethod 취소됨
예외 발생: java.util.concurrent.StructuredTaskScope$FailedException: java.lang.RuntimeException: bMethod 실패
```

## 동작

```java
/*
Opens a new StructuredTaskScope that can be used to fork subtasks 
that return results of any type. 
The scope's join() method waits for all subtasks to succeed or any subtask to fail.
*/    
    
static <T> StructuredTaskScope<T, Void> open() {
    return open(Joiner.awaitAllSuccessfulOrThrow(), Function.identity());
}
```

- StructuredScope의 open()을 이용하면 내부적으로 `Joiner` 의 여러 방식 중 `awaitAllSuccessfulOrThrow`를 이용한다
    - 이름에서 유추할 수 있듯이 하나의 예외라도 발생하면 즉시 종료한다
- `scope.fork()`로 실행한 작업들은 하나의 scope 안에 묶인다
- `bMethod()`에서 예외가 발생하면 해당 subtask는 실패 상태가 되고, `scope.join()` 시점에 실패가 감지된다
- scope는 아직 실행 중인 다른 subtask를 취소한다.
- 따라서 `aMethod()`는 `Thread.sleep(5000)` 중 interrupt를 받고 중단된다.
- 즉, **StructuredTaskScope는 여러 병렬 작업을 하나의 작업 단위로 묶고, 실패 시 전체 작업을 구조적으로 종료시킨다.**