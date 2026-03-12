public class WarehouseSimulation {

    public static void main(String[] args) throws Exception {
        String configPath = (args.length > 0) ? args[0] : "warehouse.properties";

        Config config = Config.loadFromFile(configPath);
        config.printSummary();

       
        simulator_clock.initialize(config.getTickDurationMs());
        System.out.println("Clock: " + config.getTickDurationMs() + " ms/tick.");

        /*
        Logger logger = new Logger();
        logger.logDelivery(1, 3, 2, 0, 4, 1);
        logger.setConsoleOutput(false);

        logger.logDelivery(2, 5, 1, 3, 0, 2);
        logger.setConsoleOutput(true);
        logger.logDelivery(3, 0, 0, 0, 0, 0);
        */

        /*
        Trolley trolley = new Trolley(1, 10);

        trolley.addBoxes(BoxType.ELECTRONICS, 3);
        System.out.println(trolley.getTotalLoad());
        trolley.removeOneBox(BoxType.ELECTRONICS);
        System.out.println(trolley.getTotalLoad());
        System.out.println(trolley.isEmpty());
        */

        TrolleyPool trolleyPool = new TrolleyPool(3, 10);
        Trolley trolley = trolleyPool.getTrolley();
        System.out.println("Acquired: " + trolley.id);

        trolley.addBoxes(BoxType.ELECTRONICS, 3);
        try {
            trolleyPool.releaseTrolley(trolley);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }

        trolley.removeOneBox(BoxType.ELECTRONICS);
        trolley.removeOneBox(BoxType.ELECTRONICS);
        trolley.removeOneBox(BoxType.ELECTRONICS);
        trolleyPool.releaseTrolley(trolley);
        System.out.println("Trolley released successfully");

        Trolley ta = trolleyPool.getTrolley();
        Trolley tb = trolleyPool.getTrolley();
        Trolley tc = trolleyPool.getTrolley();
        System.out.println("Acquired all 3 trolleys");
        trolleyPool.releaseTrolley(ta);
        trolleyPool.releaseTrolley(tb);
        trolleyPool.releaseTrolley(tc);
        System.out.println("released all 3 trolleys");

        Section section = new Section(10);
        System.out.println(section.getBoxCount());
        System.out.println(section.isEmpty());


        section.startStocking();
        section.stockMultiple(5);
        section.endStocking();

        System.out.println(section.getBoxCount());

        System.out.println(section.isEmpty());
        System.out.println(section.getAvailableSpace());

        Thread picker1 = new Thread(() -> {
            try {
                int waited = section.pick();
                System.out.println(waited + " ticks");
                int waited2 = section.pick();
                System.out.println(waited2 + " ticks");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread picker2 = new Thread(() -> {
            try {
                int waited = section.pick();
                System.out.println(waited + " ticks");
                int waited2 = section.pick();
                System.out.println(waited2 + " ticks");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        picker1.start();
        picker2.start();

        picker1.join();
        picker2.join();

        System.out.println(section.getWaitingPickersCount());
    }
}
