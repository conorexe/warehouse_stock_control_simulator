import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class WarehouseSimulation {

    private final Config         config;
    private final Logger         logger;
    private final simulator_clock clock;

    private StagingArea               stagingArea;
    private TrolleyPool               trolleyPool;
    private EnumMap<BoxType, Section> sections;
    private Statistics                statistics;

    private Thread       clockThread;
    private Thread       deliveryThread;
    private final List<Thread> stockerThreads = new ArrayList<>();
    private final List<Thread> pickerThreads  = new ArrayList<>();

    public WarehouseSimulation(Config config) {
        this.config = config;
        this.logger = new Logger();
        simulator_clock.reset();
        simulator_clock.initialize(config.getTickDurationMs());
        this.clock = simulator_clock.getInstance();
    }

    public void setConsoleOutput(boolean enabled) {
        logger.setConsoleOutput(enabled);
    }

    public void start() {
        Random seeder = new Random(config.getRandomSeed());

        // Shared resources — spec: each section begins with 5 boxes
        sections = new EnumMap<>(BoxType.class);
        for (BoxType type : BoxType.values()) {
            Section s = new Section(config.getSectionCapacity());
            s.initializeBoxes(5);
            sections.put(type, s);
        }
        stagingArea = new StagingArea();
        trolleyPool = new TrolleyPool(config.getNumTrolleys(), config.getTrolleyCapacity());
        statistics  = new Statistics();

        // Clock thread: sleeps for full simulation duration then stops the clock
        long durationMs = (long) config.getSimulationDurationTicks() * config.getTickDurationMs();
        clockThread = new Thread(() -> {
            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            clock.stop();
        }, "clock");
        clockThread.setDaemon(true);
        clockThread.start();

        // Delivery thread
        deliveryThread = new Thread(
            new DeliverySystem(stagingArea, logger,
                config.getDeliveryProbability(), 10, seeder.nextLong()),
            "DEL");
        deliveryThread.start();

        // Stocker threads
        for (int i = 0; i < config.getNumStockers(); i++) {
            String tid = "S" + (i + 1);
            Thread t = new Thread(
                new Stocker(tid, stagingArea, sections, trolleyPool, logger,
                    statistics, config.getBreakInterval(), seeder.nextLong()), tid);
            stockerThreads.add(t);
            t.start();
        }

        // Picker threads
        Picker.resetPickIdCounter();
        for (int i = 0; i < config.getNumPickers(); i++) {
            String tid = "P" + (i + 1);
            Thread t = new Thread(
                new Picker(tid, sections, trolleyPool, logger,
                    statistics, config.getPickRatePerTick(), seeder.nextLong()), tid);
            pickerThreads.add(t);
            t.start();
        }
    }

    public void waitForCompletion() throws InterruptedException {
        clockThread.join();
        shutdown();
    }

    private void shutdown() throws InterruptedException {
        clock.stop();

        // Wake all threads blocked on shared resources
        stagingArea.shutdown();
        for (Section s : sections.values()) s.shutdown();

        // Interrupt all worker threads
        deliveryThread.interrupt();
        for (Thread t : stockerThreads) t.interrupt();
        for (Thread t : pickerThreads)  t.interrupt();

        // Join with 1-second timeout each
        deliveryThread.join(1000);
        for (Thread t : stockerThreads) t.join(1000);
        for (Thread t : pickerThreads)  t.join(1000);
    }

    public void printStatistics() {
        statistics.print(config.getSimulationDurationTicks());

        // Final snapshot of section and staging state.
        System.out.println("\n=== End-of-run Snapshot ===");
        for (BoxType type : BoxType.values()) {
            Section s = sections.get(type);
            System.out.printf("  section=%-12s boxes=%d/%d waiting_pickers=%d%n",
                type.getName(), s.getBoxCount(), s.getCapacity(), s.getWaitingPickersCount());
        }
        System.out.printf("  staging total_boxes=%d%n", stagingArea.getTotalBoxes());
    }

    public static void main(String[] args) throws Exception {
        String  configPath = "warehouse.properties";
        boolean noConsole  = false;

        for (String arg : args) {
            if      (arg.equals("--no-console")) noConsole = true;
            else if (!arg.startsWith("--"))      configPath = arg;
        }

        Config config = Config.loadFromFile(configPath);
        config.printSummary();

        WarehouseSimulation sim = new WarehouseSimulation(config);
        if (noConsole) sim.setConsoleOutput(false);

        sim.start();
        sim.waitForCompletion();
        sim.printStatistics();
    }
}
