import java.util.concurrent.atomic.AtomicInteger;


public class SectionSynchronised {

    private volatile boolean stockerActive = false;
    private int boxCount  = 0;
    private final int capacity;

    // Tracks pickers currently waiting for stock.
    private final AtomicInteger waitingPickers = new AtomicInteger(0);

    public SectionSynchronised(int capacity) {
        this.capacity = capacity;
    }

    /** Pre-populate with initial stock without tick delays (used at simulation start). */
    public void initializeBoxes(int count) {
        boxCount = Math.min(count, capacity);
    }

    /* Blocks until no other stocker is active */
    public synchronized void acquireForStocking() throws InterruptedException {

        while (stockerActive) {
            wait();
        }
        stockerActive = true;
    }

    /* Releases exclusive stocking access. */
    public synchronized void releaseFromStocking() {

        stockerActive = false;
        notifyAll();
    }

    
    public synchronized void stockOneBox() throws InterruptedException {
        if (isFull()) {
            // Should never happen: stockSection() checks !isFull() before calling here.
            throw new IllegalStateException("stockOneBox called on a full section");
        }
        boxCount++;
        notifyAll();
    }

    /**
     * Waits for space to be available.
     * Returns true if space is available before the timeout.
     */
    public synchronized boolean waitForSpaceWithTimeout(int ticks) throws InterruptedException {
        long nanos = ticks * simulator_clock.getInstance().get_tick_duration();
        long deadline = System.currentTimeMillis() + nanos;
        while (isFull()) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) return false;
            wait(remaining);
            }
            return true;
    }

    // Picking side
    /* waits for no active stocker and boxes available, returns ticks waited for stock */
    public synchronized long pickBox() throws InterruptedException {
        waitingPickers.incrementAndGet();
        long startTick = simulator_clock.getInstance().getCurrentTick();
        try {
                while (stockerActive || boxCount == 0) {
                    if (stockerActive) {
                        wait();
                    } else {
                        wait();
                    }
                }
                boxCount--;
                notifyAll();
        } finally {
            waitingPickers.decrementAndGet();
        }
        long waitedTicks = simulator_clock.getInstance().getCurrentTick() - startTick;
        simulator_clock.getInstance().waitOneTick();
        return waitedTicks;
    }

    /** Wakes all blocked threads so they can detect shutdown */
    public synchronized void shutdown() {
        notifyAll();
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
