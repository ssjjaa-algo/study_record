# 프린터

태그: Interrupt

```java
package thread.start.controll.printer;

import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import static util.MyLogger.log;
import static util.ThreadUtils.sleep;

public class MyPrinterV1 {

    public static void main(String[] args) {

        Printer printer = new Printer();
        Thread thread = new Thread(printer, "printer");
        thread.start();

        Scanner scanner = new Scanner(System.in);
        while(true) {
            log("작업을 입력하세요");
            String input = scanner.nextLine();
            if (input.equals("q")) {
                printer.work = false;
                thread.interrupt();
                break;
            }
            printer.addJob(input);
        }

    }

    static class Printer implements Runnable {

        volatile boolean work = true;
        Queue<String> jobQueue = new ConcurrentLinkedQueue<>();

        @Override
        public void run() {

            while (!Thread.interrupted()) { // work 대신 사용가능

                if (jobQueue.isEmpty()) continue;

                try {
                    String job = jobQueue.poll();
                    log("출력 시작 : " + job + ", 대기 문서 : " + jobQueue);
                    sleep(3000);
                    log("출력 완료");
                } catch (Exception e) {
                    log("인터럽트 발생");
                    break;
                }
            }

            log("프린터 종료");
        }

        public void addJob(String job) {
            jobQueue.add(job);
        }
    }
}

```
