import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {

    private final AtomicLong totalPicks         = new AtomicLong(0);
    private final AtomicLong totalPickWaitTicks  = new AtomicLong(0);

    private final EnumMap<BoxType, AtomicLong> picksPerSection     = new EnumMap<>(BoxType.class);
    private final EnumMap<BoxType, AtomicLong> waitTicksPerSection = new EnumMap<>(BoxType.class);

    private final AtomicLong totalBoxesStocked = new AtomicLong(0);
    private final EnumMap<BoxType, AtomicLong> boxesStockedPerSection = new EnumMap<>(BoxType.class);

    private final AtomicLong totalBreaks = new AtomicLong(0);

    public Statistics() {
        for (BoxType type : BoxType.values()) {
            picksPerSection    .put(type, new AtomicLong(0));
            waitTicksPerSection.put(type, new AtomicLong(0));
            boxesStockedPerSection.put(type, new AtomicLong(0));
        }
    }

    public void recordPick(BoxType type, long waitedTicks) {
        totalPicks.incrementAndGet();
        totalPickWaitTicks.addAndGet(waitedTicks);
        picksPerSection    .get(type).incrementAndGet();
        waitTicksPerSection.get(type).addAndGet(waitedTicks);
    }

    public void recordStocked(BoxType type, int count) {
        totalBoxesStocked.addAndGet(count);
        boxesStockedPerSection.get(type).addAndGet(count);
    }

    public void recordBreak() {
        totalBreaks.incrementAndGet();
    }

    public void print(long simulationTicks) {
        long   picks    = totalPicks.get();
        long   waitSum  = totalPickWaitTicks.get();
        double avgWait  = picks > 0 ? (double) waitSum / picks : 0.0;
        double picksPerDay   = simulationTicks > 0 ? picks * 1000.0 / simulationTicks : 0.0;
        double stockedPerDay = simulationTicks > 0 ? totalBoxesStocked.get() * 1000.0 / simulationTicks : 0.0;

        System.out.println("\n=== Simulation Statistics ===");
        System.out.printf("  simulation_ticks        : %d%n",    simulationTicks);
        System.out.printf("  total_picks             : %d  (%.1f / day)%n", picks, picksPerDay);
        System.out.printf("  total_pick_wait_ticks   : %d%n",    waitSum);
        System.out.printf("  avg_pick_wait_ticks     : %.2f%n",  avgWait);
        System.out.printf("  total_boxes_stocked     : %d  (%.1f / day)%n",
                          totalBoxesStocked.get(), stockedPerDay);
        System.out.printf("  total_stocker_breaks    : %d%n",    totalBreaks.get());

        System.out.println();
        System.out.printf("  %-12s %8s %12s %12s %12s%n",
                          "section", "picks", "avg_wait", "wait_ticks", "stocked");
        System.out.println("  " + "-".repeat(60));
        for (BoxType type : BoxType.values()) {
            long sectionPicks   = picksPerSection    .get(type).get();
            long sectionWait    = waitTicksPerSection.get(type).get();
            long sectionStocked = boxesStockedPerSection.get(type).get();
            double sectionAvg   = sectionPicks > 0 ? (double) sectionWait / sectionPicks : 0.0;
            System.out.printf("  %-12s %8d %12.2f %12d %12d%n",
                              type.getName(), sectionPicks, sectionAvg, sectionWait, sectionStocked);
        }
    }
}
