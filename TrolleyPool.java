import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class TrolleyPool {
    private final LinkedList<Trolley> trolleyPool = new LinkedList<Trolley>();
    private final Lock lock = new ReentrantLock();
    private final Condition trolleyAvailable = lock.newCondition();

    public Trolley getTrolley() throws InterruptedException {
        lock.lock();

        try {
            while (trolleyPool.size() == 0) {
                trolleyAvailable.await();
            }

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
            trolleyAvailable.signal();
        }
        finally {
            lock.unlock();
        }
    }

    public TrolleyPool(int num, int capacity) {
        for (int i = 0; i < num; i++) {
            Trolley trolley = new Trolley(i, capacity);
            trolleyPool.add(trolley);
        }
    }
}