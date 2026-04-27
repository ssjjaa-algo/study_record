package modern.concurrency.structuredtaskscope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UnstructuredlManualCancelExample {

    public static void main(String[] args) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            CompletionService<Integer> cs = new ExecutorCompletionService<>(executor);

            Future<Integer> aFuture = cs.submit(() -> aMethod());
            Future<Integer> bFuture = cs.submit(() -> bMethod());

            List<Future<Integer>> futures = List.of(aFuture, bFuture);
            List<Integer> results = new ArrayList<>();

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
        }
    }

    static int aMethod() throws InterruptedException {
        try {
            System.out.println("aMethod 진입");
            Thread.sleep(5000);

            return 1;
        } catch (InterruptedException e) {
            throw e;
        }
    }

    static int bMethod() {
        System.out.println("bMethod 예외 발생");
        throw new RuntimeException("bMethod 실패");
    }

    static int add(int a, int b) {
        return a + b;
    }
}