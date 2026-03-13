import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StagingArea {

    private final EnumMap<BoxType, Integer> boxes = new EnumMap<>(BoxType.class);

    private final Lock      lock           = new ReentrantLock(true);
    private final Condition boxesAvailable = lock.newCondition();
    private final Condition stockerDone    = lock.newCondition();

    private volatile boolean stockerTaking = false;
    private volatile boolean shutdownFlag  = false;

    public StagingArea() {
        for (BoxType type : BoxType.values()) {
            boxes.put(type, 0);
        }
    }

    /* Blocks until stocker has exclusive access and boxes are available. */
    public void acquireForTaking() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                if (shutdownFlag) throw new InterruptedException("StagingArea shut down");

                if (!stockerTaking && hasBoxes()) {
                    stockerTaking = true;
                    return;
                }

                if (stockerTaking) {
                    stockerDone.await();
                } else {
                    boxesAvailable.await();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void tryAcquireExclusive() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                if (shutdownFlag) throw new InterruptedException("StagingArea shut down");
                if (!stockerTaking) {
                    stockerTaking = true;
                    return;
                }
                stockerDone.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /** Waits until at least one box is present. */
    public void waitForBoxes() throws InterruptedException {
        lock.lock();
        try {
            while (!hasBoxes() && !shutdownFlag) {
                boxesAvailable.await();
            }
            if (shutdownFlag) throw new InterruptedException("StagingArea shut down");
        } finally {
            lock.unlock();
        }
    }

    /* Adds boxes and wakes waiting stockers. */
    public void addDelivery(EnumMap<BoxType, Integer> delivery) {
        lock.lock();
        try {
            for (var entry : delivery.entrySet()) {
                if (entry.getValue() > 0) {
                    boxes.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            }
            boxesAvailable.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /* Releases exclusive stocker access and wakes waiting threads. */
    public void releaseFromTaking() {
        lock.lock();
        try {
            stockerTaking = false;
            stockerDone.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public EnumMap<BoxType, Integer> takeBoxes(
            Trolley trolley,
            int maxBoxes,
            EnumMap<BoxType, Section> sections) throws InterruptedException {

        EnumMap<BoxType, Integer> taken = new EnumMap<>(BoxType.class);
        for (BoxType type : BoxType.values()) taken.put(type, 0);

        int remaining = Math.min(maxBoxes, trolley.getAvailableSpace());
        BoxType[] order = getPriorityOrder(sections);

        lock.lock();
        try {
            for (BoxType type : order) {
                if (remaining <= 0) break;

                int available = boxes.get(type);
                if (available <= 0) continue;

                // load boxes the trolley can use.
                Section section = sections.get(type);
                if (section != null && section.isFull()) continue;

                int toTake = Math.min(available, remaining);
                boxes.put(type, available - toTake);
                trolley.addBoxes(type, toTake);
                taken.put(type, toTake);
                remaining -= toTake;
            }
        } finally {
            lock.unlock();
        }

        simulator_clock.getInstance().waitOneTick();
        return taken;
    }

    /* Returns the five box types sorted by stocking priority */
    BoxType[] getPriorityOrder(EnumMap<BoxType, Section> sections) {
        BoxType[] types = BoxType.values();
        Arrays.sort(types, Comparator.comparingInt(
                (BoxType t) -> {
                    Section s = sections.get(t);
                    if (s == null) return 0;
                    return s.getWaitingPickersCount();
                }).reversed()
            .thenComparing(t -> {
                Section s = sections.get(t);
                return (s == null || s.isEmpty()) ? 0 : 1;
            })
            .thenComparingInt(t -> {
                Section s = sections.get(t);
                return s == null ? 0 : s.getBoxCount();
            })
        );
        return types;
    }

    /** Returns true if any box type has at least one box. */
    public boolean hasBoxes() {
        for (int n : boxes.values()) {
            if (n > 0) return true;
        }
        return false;
    }

    /** Returns the total number of boxes across all types. */
    public int getTotalBoxes() {
        int total = 0;
        for (int n : boxes.values()) total += n;
        return total;
    }

    public EnumMap<BoxType, Integer> getBoxSnapshot() {
        lock.lock();
        try {
            return new EnumMap<>(boxes);
        } finally {
            lock.unlock();
        }
    }

    /* Signals all waiting threads to wake up and check for shutdown. */
    public void shutdown() {
        lock.lock();
        try {
            shutdownFlag = true;
            boxesAvailable.signalAll();
            stockerDone.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
