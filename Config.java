import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private long   tickDurationMs         = 100;
    private int    simulationDurationTicks = 480;
    private double deliveryProbability    = 0.01;
    private int    sectionCapacity        = 10;
    private int    numStockers            = 2;
    private int    numPickers             = 4;
    private int    trolleyCapacity        = 10;
    // -1 means "use default formula: floor((numStockers + numPickers) / 2)"
    private int    numTrolleys            = -1;
    private int    breakInterval          = 120;
    private int    targetPicksPerDay      = 100;
    private long   randomSeed             = 42;

    public static Config loadFromFile(String path) throws IOException {
        Config cfg = new Config();
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        }

        if (props.containsKey("tickDurationMs"))
            cfg.tickDurationMs = Long.parseLong(props.getProperty("tickDurationMs").trim());
        if (props.containsKey("simulationDurationTicks"))
            cfg.simulationDurationTicks = Integer.parseInt(props.getProperty("simulationDurationTicks").trim());
        if (props.containsKey("deliveryProbability"))
            cfg.deliveryProbability = Double.parseDouble(props.getProperty("deliveryProbability").trim());
        if (props.containsKey("sectionCapacity"))
            cfg.sectionCapacity = Integer.parseInt(props.getProperty("sectionCapacity").trim());
        if (props.containsKey("numStockers"))
            cfg.numStockers = Integer.parseInt(props.getProperty("numStockers").trim());
        if (props.containsKey("numPickers"))
            cfg.numPickers = Integer.parseInt(props.getProperty("numPickers").trim());
        if (props.containsKey("trolleyCapacity"))
            cfg.trolleyCapacity = Integer.parseInt(props.getProperty("trolleyCapacity").trim());
        if (props.containsKey("numTrolleys"))
            cfg.numTrolleys = Integer.parseInt(props.getProperty("numTrolleys").trim());
        if (props.containsKey("breakInterval"))
            cfg.breakInterval = Integer.parseInt(props.getProperty("breakInterval").trim());
        if (props.containsKey("targetPicksPerDay"))
            cfg.targetPicksPerDay = Integer.parseInt(props.getProperty("targetPicksPerDay").trim());
        if (props.containsKey("randomSeed"))
            cfg.randomSeed = Long.parseLong(props.getProperty("randomSeed").trim());

        return cfg;
    }

    public double getPickRatePerTick() {
        return (double) targetPicksPerDay / (1000.0 * numPickers);
    }

    /** Returns K (trolley count). Uses the explicit numTrolleys value if set in config,
     *  otherwise falls back to the spec default: floor((numStockers + numPickers) / 2). */
    public int getNumTrolleys() {
        return (numTrolleys > 0) ? numTrolleys : (numStockers + numPickers) / 2;
    }

    public void printSummary() {
        boolean usingDefault = (numTrolleys <= 0);
        System.out.printf("  tickDurationMs         : %d ms%n",    tickDurationMs);
        System.out.printf("  simulationDurationTicks: %d%n",       simulationDurationTicks);
        System.out.printf("  deliveryProbability    : %.2f%n",     deliveryProbability);
        System.out.printf("  sectionCapacity        : %d%n",       sectionCapacity);
        System.out.printf("  numStockers (S)        : %d%n",       numStockers);
        System.out.printf("  numPickers  (P)        : %d%n",       numPickers);
        System.out.printf("  trolleyCapacity        : %d%n",       trolleyCapacity);
        System.out.printf("  numTrolleys            : %d%s%n",     getNumTrolleys(),
                          usingDefault ? " (default: floor((S+P)/2))" : " (explicit)");
        System.out.printf("  breakInterval          : %d ticks%n", breakInterval);
        System.out.printf("  targetPicksPerDay      : %d%n",       targetPicksPerDay);
        System.out.printf("  pickRatePerTick        : %.6f%n",     getPickRatePerTick());
        System.out.printf("  randomSeed             : %d%n",       randomSeed);
    }

    public long   getTickDurationMs()                { return tickDurationMs; }
    public void   setTickDurationMs(long v)          { tickDurationMs = v; }

    public int    getSimulationDurationTicks()       { return simulationDurationTicks; }
    public void   setSimulationDurationTicks(int v)  { simulationDurationTicks = v; }

    public double getDeliveryProbability()           { return deliveryProbability; }
    public void   setDeliveryProbability(double v)   { deliveryProbability = v; }

    public int    getSectionCapacity()               { return sectionCapacity; }
    public void   setSectionCapacity(int v)          { sectionCapacity = v; }

    public int    getNumStockers()                   { return numStockers; }
    public void   setNumStockers(int v)              { numStockers = v; }

    public int    getNumPickers()                    { return numPickers; }
    public void   setNumPickers(int v)               { numPickers = v; }

    public int    getTrolleyCapacity()               { return trolleyCapacity; }
    public void   setTrolleyCapacity(int v)          { trolleyCapacity = v; }

    /** Explicitly override the trolley count. Pass -1 to restore the default formula. */
    public int    getNumTrolleysOverride()           { return numTrolleys; }
    public void   setNumTrolleys(int v)              { numTrolleys = v; }

    public int    getBreakInterval()                 { return breakInterval; }
    public void   setBreakInterval(int v)            { breakInterval = v; }

    public int    getTargetPicksPerDay()             { return targetPicksPerDay; }
    public void   setTargetPicksPerDay(int v)        { targetPicksPerDay = v; }

    public long   getRandomSeed()                    { return randomSeed; }
    public void   setRandomSeed(long v)              { randomSeed = v; }
}
