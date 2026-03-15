import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Section {

    private final Lock lock = new ReentrantLock(true);
    private final Condition boxAvailable    = lock.newCondition();
    private final Condition spaceAvailable  = lock.newCondition();
    private final Condition stockerFinished = lock.newCondition();

    private volatile boolean stockerActive = false;
    private int boxCount  = 0;
    private final int capacity;

    // Tracks pickers currently waiting for stock.
    private final AtomicInteger waitingPickers = new AtomicInteger(0);

    public Section(int capacity) {
        this.capacity = capacity;
    }

    /** Pre-populate with initial stock without tick delays (used at simulation start). */
    public void initializeBoxes(int count) {
        lock.lock();
        try {
            boxCount = Math.min(count, capacity);
        } finally {
            lock.unlock();
        }
    }

    /* Blocks until no other stocker is active */
    public void acquireForStocking() throws InterruptedException {
        lock.lock();
        try {
            while (stockerActive) {
                stockerFinished.await();
            }
            stockerActive = true;
        } finally {
            lock.unlock();
        }
    }

    /* Releases exclusive stocking access. */
    public void releaseFromStocking() {
        lock.lock();
        try {
            stockerActive = false;
            stockerFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    
    public void stockOneBox() throws InterruptedException {
        lock.lock();
        try {
            if (isFull()) {
                // Should never happen: stockSection() checks !isFull() before calling here.
                throw new IllegalStateException("stockOneBox called on a full section");
            }
            boxCount++;
            boxAvailable.signal();
        } finally {
            lock.unlock();
        }
        simulator_clock.getInstance().waitOneTick();
    }

    /**
     * Waits for space to be available.
     * Returns true if space is available before the timeout.
     */
    public boolean waitForSpaceWithTimeout(int ticks) throws InterruptedException {
        long nanos = ticks * simulator_clock.getInstance().get_tick_duration() * 1_000_000L;
        lock.lock();
        try {
            while (isFull()) {
                nanos = spaceAvailable.awaitNanos(nanos);
                if (nanos <= 0) return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    // Picking side
    /* waits for no active stocker and boxes available, returns ticks waited for stock */
    public long pickBox() throws InterruptedException {
        waitingPickers.incrementAndGet();
        long startTick = simulator_clock.getInstance().getCurrentTick();
        try {
            lock.lock();
            try {
                while (stockerActive || boxCount == 0) {
                    if (stockerActive) {
                        stockerFinished.await();
                    } else {
                        boxAvailable.await();
                    }
                }
                boxCount--;
                spaceAvailable.signal();
            } finally {
                lock.unlock();
            }
        } finally {
            waitingPickers.decrementAndGet();
        }
        long waitedTicks = simulator_clock.getInstance().getCurrentTick() - startTick;
        simulator_clock.getInstance().waitOneTick();
        return waitedTicks;
    }

    /** Wakes all blocked threads so they can detect shutdown */
    public void shutdown() {
        lock.lock();
        try {
            boxAvailable.signalAll();
            spaceAvailable.signalAll();
            stockerFinished.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Query methods
    public int  getBoxCount()            { return boxCount; }
    public int  getCapacity()            { return capacity; }
    public int  getAvailableSpace()      { return capacity - boxCount; }
    public boolean isEmpty()             { return boxCount == 0; }
    public boolean isFull()              { return boxCount >= capacity; }
    public boolean isStockerActive()     { return stockerActive; }
    public int  getWaitingPickersCount() { return waitingPickers.get(); }
}
