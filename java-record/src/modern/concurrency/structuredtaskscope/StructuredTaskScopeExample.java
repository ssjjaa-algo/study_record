package modern.concurrency.structuredtaskscope;

import java.util.concurrent.StructuredTaskScope;

public class StructuredTaskScopeExample {

    public static void main(String[] args) {
        try (var scope = StructuredTaskScope.open()) {

            StructuredTaskScope.Subtask<Integer> a = scope.fork(() -> aMethod());
            StructuredTaskScope.Subtask<Integer> b = scope.fork(() -> bMethod());

            scope.join();

            int result = add(a.get(), b.get());
            System.out.println(result);

        } catch (Exception e) {
            System.out.println("예외 발생: " + e);
        }
    }

    static int aMethod() throws InterruptedException {
        try {
            System.out.println("aMethod 시작: " + Thread.currentThread());
            Thread.sleep(5000);
            System.out.println("aMethod 종료");
            return 1;
        } catch (InterruptedException e) {
            System.out.println("aMethod 취소됨");
            throw e;
        }
    }

    static int bMethod() {
        System.out.println("bMethod 예외 발생: " + Thread.currentThread());
        throw new RuntimeException("bMethod 실패");
    }

    static int add(int a, int b) {
        return a + b;
    }
}
