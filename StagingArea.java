import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class StagingArea {
    EnumMap<BoxType, Integer> boxes = new EnumMap<>(BoxType.class);
    private final Lock lock = new ReentrantLock();
    private final Condition boxesAvailable = lock.newCondition();
    private final Condition stockerFinished = lock.newCondition();

    private volatile boolean stocker = false;

    public StagingArea() {
        boxes.put(BoxType.ELECTRONICS, 0);
        boxes.put(BoxType.BOOKS, 0);
        boxes.put(BoxType.MEDICINES, 0);
        boxes.put(BoxType.CLOTHES, 0);
        boxes.put(BoxType.TOOLS, 0);
    }

    public void addDelivery(EnumMap<BoxType, Integer> delivery) {
        lock.lock();

        try {
            for (Entry<BoxType, Integer> entry : delivery.entrySet()) {
                boxes.put(entry.getKey(), boxes.get(entry.getKey()) + entry.getValue());
            }

            boxesAvailable.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public void acquire() throws InterruptedException {
        lock.lock();

        try {
            while(stocker) {
                stockerFinished.await();
            }
            while(!hasBoxes()) {
                boxesAvailable.await();
            }
            stocker = true;
        }
        finally {
            lock.unlock();
        }
    }

    public void realease() {
        lock.lock();

        try {
            stocker = false;

            stockerFinished.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public boolean hasBoxes() {
        for (int num : boxes.values()) {
            if (num > 0) {
                return true;
            }
        }
        return false;
    }
}