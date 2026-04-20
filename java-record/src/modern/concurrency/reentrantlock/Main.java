package modern.concurrency.reentrantlock;

import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) {

        ReentrantLock reentrantLock = new ReentrantLock();

        reentrantLock.lock();
    }
}
