import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Section {
    private final Lock lock = new ReentrantLock();
    private final Condition boxAvailable = lock.newCondition();
    private final Condition spaceAvailable = lock.newCondition();
    private final Condition stockerFinished = lock.newCondition();
    private volatile boolean stockerActive = false;
    private int count = 0;
    private int capacity = 0;

    public void startStocking() throws InterruptedException {
        lock.lock();

        try {
            while(stockerActive) {
                stockerFinished.await();
            }
            stockerActive = true;
        }
        finally {
            lock.unlock();
        }
    }

    public void endStocking() {
        lock.lock();
        try {
            stockerActive = false;
            stockerFinished.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public void stock() throws InterruptedException {
        lock.lock();

        try {
            count++;
            boxAvailable.signal();
        }
        finally {
            lock.unlock();
        }
        //sleepTime = generator.nextInt()
        simulator_clock.getInstance().waitOneTick();
    }

    public void stockMultiple(int num) throws InterruptedException {
        for(int i = 0; i < num; i++) {
            stock();
        }
    }

    public int getBoxCount() {
        return count;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isEmpty() {
        if(count == 0) {
            return true;
        } else {
            return false;
        }
    }

    public int getAvailableSpace() {
        int available = capacity - count;
        return available;
    }

    public Section(int capacity) {
        this.capacity = capacity;
    }

    public boolean isFull() {
        if(count >= capacity) {
            return true;
        } else {
            return false;
        }
    }
}