import java.util.EnumMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class PickerRelative {
     private final String                    tid;
    private final EnumMap<BoxType, Section> sections;
    private final TrolleyPool               trolleyPool;
    private final Logger                    logger;
    private final Statistics                stats;
    private final double                    pickRatePerTick;
    private final Random                    random;

    private static final AtomicInteger pickIdCounter = new AtomicInteger(0);

    public PickerRelative(String tid, EnumMap<BoxType, Section> sections, TrolleyPool trolleyPool,
                  Logger logger, Statistics stats, double pickRatePerTick, long seed) {
        this.tid             = tid;
        this.sections        = sections;
        this.trolleyPool     = trolleyPool;
        this.logger          = logger;
        this.stats           = stats;
        this.pickRatePerTick = pickRatePerTick;
        this.random          = new Random(seed);
    }

    public static void resetPickIdCounter() {
        pickIdCounter.set(0);
    }

    /*
    @Override
    public void run() {
        simulator_clock clock = simulator_clock.getInstance();
        //tracks when the next attempt is needed.
        //long nextAttemptTick = clock.getCurrentTick();
        try {
            while (clock.mclock_status()) {
                // Sleep remaining ticks

                // Schedule next attempt.
          
                long interArrival = (long) Math.ceil(-Math.log(random.nextDouble()) / pickRatePerTick);
                //nextAttemptTick += interArrival;
                clock.waitTicks((int) interArrival);
                performPickAttempt(clock);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    */

    private void performPickAttempt(simulator_clock clock) throws InterruptedException {
        Trolley trolley = trolleyPool.getTrolley();
        try {
            int pickId = pickIdCounter.incrementAndGet();

            BoxType[] types = BoxType.values();
            BoxType   type  = types[random.nextInt(types.length)];
            Section   section = sections.get(type);

            logger.logPickStart(clock.getCurrentTick(), tid, pickId, type.getName(), trolley.id);

            long waitedTicks;
            try {
                waitedTicks = section.pickBox();
            } catch (InterruptedException e) {
                // Ensure theres a  pick_start for every pick_done.
                logger.logPickDone(clock.getCurrentTick(), tid, pickId, type.getName(), 0, trolley.id);
                throw e;
            }

            stats.recordPick(type, waitedTicks);
            logger.logPickDone(clock.getCurrentTick(), tid, pickId, type.getName(), waitedTicks, trolley.id);
        } finally {

            trolleyPool.releaseTrolley(trolley);
        }
    }   
}
