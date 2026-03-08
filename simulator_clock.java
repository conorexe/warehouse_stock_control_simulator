public class simulator_clock {

    private volatile boolean clock_status;
    private long start_time;
    private final long tick_duration;

    private static volatile simulator_clock instance;

    private simulator_clock(long tick_duration) {
        this.tick_duration = tick_duration;
        this.start_time = System.currentTimeMillis();
        this.clock_status = true;
    }

    public static synchronized void initialize(long tick_duration) {
        if (instance == null) {
            instance = new simulator_clock(tick_duration);
        }
    }

    public static simulator_clock getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "simulation clock is off");
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    public long getCurrentTick() {
        return (System.currentTimeMillis() - start_time) / tick_duration;
    }

    public boolean mclock_status() {
        return clock_status;
    }

    public void stop() {
        clock_status = false;
    }

    public long get_tick_duration() {
        return tick_duration;
    }

    public void waitTicks(int n) throws InterruptedException {
        Thread.sleep((long) n * tick_duration);
    }

    public void waitOneTick() throws InterruptedException {
        Thread.sleep(tick_duration);
    }

    public static void main(String[] args) throws InterruptedException {
        final long TICK_MS = 200;

        simulator_clock.initialize(TICK_MS);
        simulator_clock clock = simulator_clock.getInstance();

        System.out.println("Clock running:        " + clock.mclock_status());
        System.out.println("Tick duration:   " + clock.get_tick_duration());
        System.out.println("Tick before sleep:    " + clock.getCurrentTick());

        clock.waitTicks(5);

        System.out.println("Tick after 5-tick sleep: " + clock.getCurrentTick());

        clock.stop();
        System.out.println("Clock running after stop: " + clock.mclock_status());

        simulator_clock.reset();
        System.out.println("reset()");
    }
}
