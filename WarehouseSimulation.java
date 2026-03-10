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
    }
}
