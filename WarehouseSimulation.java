public class WarehouseSimulation {

    public static void main(String[] args) throws Exception {
        String configPath = (args.length > 0) ? args[0] : "warehouse.properties";

        Config config = Config.loadFromFile(configPath);
        config.printSummary();

       
        simulator_clock.initialize(config.getTickDurationMs());
        System.out.println("Clock: " + config.getTickDurationMs() + " ms/tick.");
    }
}
