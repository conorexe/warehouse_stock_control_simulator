import java.util.EnumMap;

public class Stocker implements Runnable {

    private final String                    tid;
    private final StagingArea               stagingArea;
    private final EnumMap<BoxType, Section> sections;
    private final TrolleyPool               trolleyPool;
    private final Logger                    logger;

    // Tracks where the stocker is.
    private String currentLocation = "staging";

    public Stocker(String tid, StagingArea stagingArea, EnumMap<BoxType, Section> sections,
                   TrolleyPool trolleyPool, Logger logger) {
        this.tid          = tid;
        this.stagingArea  = stagingArea;
        this.sections     = sections;
        this.trolleyPool  = trolleyPool;
        this.logger       = logger;
    }

    @Override
    public void run() {
        simulator_clock clock = simulator_clock.getInstance();
        Trolley trolley = null;
        try {
            while (clock.mclock_status()) {
                // get trolley
                long startTick   = clock.getCurrentTick();
                trolley          = trolleyPool.getTrolley();
                long waitedTicks = clock.getCurrentTick() - startTick;
                logger.logAcquireTrolley(clock.getCurrentTick(), tid, trolley.id, waitedTicks);

                // Load boxes, stock all sections, then return
                loadFromStaging(trolley, clock);
                stockAllBoxes(trolley, clock);
                moveToStaging(trolley, clock);

                // Release trolley 
                logger.logReleaseTrolley(clock.getCurrentTick(), tid, trolley.id, trolley.getTotalLoad());
                trolleyPool.releaseTrolley(trolley);
                trolley = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // return trolley even on interrupt
            if (trolley != null) {
                trolley.clear();
                trolleyPool.releaseTrolley(trolley);
            }
        }
    }

    private void loadFromStaging(Trolley trolley, simulator_clock clock) throws InterruptedException {
        stagingArea.acquireForTaking();
        try {
            stagingArea.takeBoxes(trolley, trolley.getAvailableSpace(), sections);
            logger.logStockerLoad(clock.getCurrentTick(), tid, trolley.getBoxes(), trolley.getTotalLoad());
        } finally {
            stagingArea.releaseFromTaking();
        }
    }

    /* Iterates box types to stock each section with boxes waiting on the trolley. */
    private void stockAllBoxes(Trolley trolley, simulator_clock clock) throws InterruptedException {
        for (BoxType type : BoxType.values()) {
            if (trolley.getBoxCount(type) > 0) {
                moveToSection(type.getName(), trolley, clock);
                stockSection(type, trolley, clock);
            }
        }
    }

    /* Travel time */
    private void moveToSection(String dest, Trolley trolley, simulator_clock clock) throws InterruptedException {
        int travelTicks = 10 + trolley.getTotalLoad();
        logger.logMove(clock.getCurrentTick(), tid, currentLocation, dest, trolley.getTotalLoad(), trolley.id);
        clock.waitTicks(travelTicks);
        currentLocation = dest;
    }

    private void moveToStaging(Trolley trolley, simulator_clock clock) throws InterruptedException {
        int travelTicks = 10 + trolley.getTotalLoad();
        logger.logMove(clock.getCurrentTick(), tid, currentLocation, "staging", trolley.getTotalLoad(), trolley.id);
        clock.waitTicks(travelTicks);
        currentLocation = "staging";
    }

    private void stockSection(BoxType type, Trolley trolley, simulator_clock clock) throws InterruptedException {
        Section section = sections.get(type);
        section.acquireForStocking();
        try {
            int toStock = trolley.getBoxCount(type);
            logger.logStockBegin(clock.getCurrentTick(), tid, type.getName(), toStock, trolley.id);

            int stocked = 0;
            while (trolley.getBoxCount(type) > 0 && !section.isFull()) {
                section.stockOneBox();
                trolley.removeOneBox(type);
                stocked++;
            }

            logger.logStockEnd(clock.getCurrentTick(), tid, type.getName(), stocked, trolley.getTotalLoad(), trolley.id);
        } finally {
            section.releaseFromStocking();
        }
    }
}
