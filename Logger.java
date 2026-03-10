import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {

    private static final Lock lock = new ReentrantLock();
    private static boolean consoleOutput = true;

    public void setConsoleOutput(boolean bool) {
        consoleOutput = bool;
    }

    public void logDelivery(int tick, int electronics, int books, int medicines, int clothes, int tools) {
        try {
            lock.lock();
            String str = "tick=%d tid=DEL event=delivery_arrived electronics=%d books=%d medicines=%d clothes=%d tools=%d";
            String result = String.format(str, tick, electronics, books, medicines, clothes, tools);
            if(consoleOutput) {
                System.out.println(result);
            }
        } finally {
            lock.unlock();
        }
    }
}