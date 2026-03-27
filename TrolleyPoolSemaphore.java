import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class TrolleyPoolSemaphore {
    private final LinkedList<Trolley> trolleyPool = new LinkedList<Trolley>();
    private final Lock lock = new ReentrantLock(true);
    //private final Condition trolleyAvailable = lock.newCondition();
    private final Semaphore semaphore;

    public Trolley getTrolley() throws InterruptedException {
        semaphore.acquire();
        lock.lock();

        try {

            return trolleyPool.removeFirst();
        }
        finally {
            lock.unlock();
        }
    }

    public void releaseTrolley(Trolley trolley) {
        lock.lock();

        try{
            if(!trolley.isEmpty()) {
                throw new IllegalStateException("Trolley must be empty before release");
            }

            trolleyPool.addLast(trolley);
        }
        finally {
            lock.unlock();
        }
        semaphore.release();
    }

    public TrolleyPoolSemaphore(int num, int capacity) {
        semaphore = new Semaphore(num, true);
        for (int i = 0; i < num; i++) {
            Trolley trolley = new Trolley(i, capacity);
            trolleyPool.add(trolley);
        }
    }
}
