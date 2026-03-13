import java.util.EnumMap;
import java.util.Random;

public class DeliverySystem implements Runnable {

    private final StagingArea stagingArea;
    private final Logger      logger;
    private final double      deliveryProbability;
    private final int         boxesPerDelivery;
    private final Random      random;

    public DeliverySystem(StagingArea stagingArea, Logger logger,
                          double deliveryProbability, int boxesPerDelivery, long seed) {
        this.stagingArea = stagingArea;
        this.logger  = logger;
        this.deliveryProbability = deliveryProbability;
        this.boxesPerDelivery = boxesPerDelivery;
        this.random = new Random(seed);
    }

    @Override
    public void run() {
        simulator_clock clock = simulator_clock.getInstance();
        try {
            while (clock.mclock_status()) {
                clock.waitOneTick();
                if (random.nextDouble() < deliveryProbability) {
                    generateDelivery(clock);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generateDelivery(simulator_clock clock) {
        EnumMap<BoxType, Integer> delivery = new EnumMap<>(BoxType.class);
        for (BoxType type : BoxType.values()) delivery.put(type, 0);

        BoxType[] types = BoxType.values();
        for (int i = 0; i < boxesPerDelivery; i++) {
            BoxType type = types[random.nextInt(types.length)];
            delivery.merge(type, 1, Integer::sum);
        }

        stagingArea.addDelivery(delivery);

        int tick = (int) clock.getCurrentTick();
        logger.logDelivery(tick,
            delivery.get(BoxType.ELECTRONICS),
            delivery.get(BoxType.BOOKS),
            delivery.get(BoxType.MEDICINES),
            delivery.get(BoxType.CLOTHES),
            delivery.get(BoxType.TOOLS)
        );
    }
}
