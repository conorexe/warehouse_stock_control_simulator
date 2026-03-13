import java.util.EnumMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {

    private static final Lock lock = new ReentrantLock();
    private static boolean consoleOutput = true;

    public void setConsoleOutput(boolean bool) {
        consoleOutput = bool;
    }

    public void logDelivery(int tick, int electronics, int books, int medicines, int clothes, int tools) {
        lock.lock();
        try {
            String str = "tick=%d tid=DEL event=delivery_arrived electronics=%d books=%d medicines=%d clothes=%d tools=%d";
            if (consoleOutput) System.out.println(String.format(str, tick, electronics, books, medicines, clothes, tools));
        } finally {
            lock.unlock();
        }
    }

    public void logAcquireTrolley(long tick, String tid, int trolleyId, long waitedTicks) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=acquire_trolley trolley_id=%d waited_ticks=%d",
                tick, tid, trolleyId, waitedTicks));
        } finally {
            lock.unlock();
        }
    }

    public void logReleaseTrolley(long tick, String tid, int trolleyId, int remainingLoad) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=release_trolley trolley_id=%d remaining_load=%d",
                tick, tid, trolleyId, remainingLoad));
        } finally {
            lock.unlock();
        }
    }

    public void logStockerLoad(long tick, String tid, EnumMap<BoxType, Integer> boxes, int totalLoad) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=stocker_load electronics=%d books=%d medicines=%d clothes=%d tools=%d total_load=%d",
                tick, tid,
                boxes.get(BoxType.ELECTRONICS), boxes.get(BoxType.BOOKS),
                boxes.get(BoxType.MEDICINES),   boxes.get(BoxType.CLOTHES),
                boxes.get(BoxType.TOOLS),        totalLoad));
        } finally {
            lock.unlock();
        }
    }

    public void logMove(long tick, String tid, String from, String to, int load, int trolleyId) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=move from=%s to=%s load=%d trolley_id=%d",
                tick, tid, from, to, load, trolleyId));
        } finally {
            lock.unlock();
        }
    }

    public void logStockBegin(long tick, String tid, String section, int amount, int trolleyId) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=stock_begin section=%s amount=%d trolley_id=%d",
                tick, tid, section, amount, trolleyId));
        } finally {
            lock.unlock();
        }
    }

    public void logStockEnd(long tick, String tid, String section, int stocked, int remainingLoad, int trolleyId) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=stock_end section=%s stocked=%d remaining_load=%d trolley_id=%d",
                tick, tid, section, stocked, remainingLoad, trolleyId));
        } finally {
            lock.unlock();
        }
    }

    public void logPickStart(long tick, String tid, int pickId, String section, int trolleyId) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=pick_start pick_id=%d section=%s trolley_id=%d",
                tick, tid, pickId, section, trolleyId));
        } finally {
            lock.unlock();
        }
    }

    public void logPickDone(long tick, String tid, int pickId, String section, long waitedTicks, int trolleyId) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=pick_done pick_id=%d section=%s waited_ticks=%d trolley_id=%d",
                tick, tid, pickId, section, waitedTicks, trolleyId));
        } finally {
            lock.unlock();
        }
    }

    public void logBreakStart(long tick, String tid, int duration) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=break_start duration=%d",
                tick, tid, duration));
        } finally {
            lock.unlock();
        }
    }

    public void logBreakEnd(long tick, String tid) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=break_end",
                tick, tid));
        } finally {
            lock.unlock();
        }
    }

    public void logWaitingForSpace(long tick, String tid, String section) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=waiting_for_space section=%s",
                tick, tid, section));
        } finally {
            lock.unlock();
        }
    }

    public void logWaitingAtStaging(long tick, String tid) {
        lock.lock();
        try {
            if (consoleOutput) System.out.println(String.format(
                "tick=%d tid=%s event=waiting_at_staging",
                tick, tid));
        } finally {
            lock.unlock();
        }
    }
}