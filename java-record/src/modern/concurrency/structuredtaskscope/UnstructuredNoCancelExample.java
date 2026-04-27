package modern.concurrency.structuredtaskscope;

import java.util.concurrent.*;

public class UnstructuredNoCancelExample {
    public static void main(String[] args) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Future<Integer> a = executor.submit(() -> aMethod());
            Future<Integer> b = executor.submit(() -> bMethod());

            try {
                int result = add(a.get(), b.get());
                System.out.println(result);
            } catch (Exception e) {
                System.out.println("예외 발생: " + e.getCause());
            }
        }
    }

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
}
