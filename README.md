# Warehouse Stock Control Simulator

Tick-based concurrency sim. Delivery thread drops boxes at a staging area, stocker threads
trolley them out to 5 sections (electronics, books, medicines, clothes, tools), picker
threads pull them off. Includes a Swing GUI.

## Run

Compile:

```
javac *.java
```

CLI:

```
java WarehouseSimulation warehouse.properties
```

GUI:

```
java SimulationGUI
```

Add `--no-console` to the CLI to suppress event logs.

## Tech stack

- Java 25 (older JDKs will reject the bundled `.class` files - delete them and rebuild)
- `java.util.concurrent.locks` (`ReentrantLock`, `Condition`)
- `java.util.concurrent.atomic` (`AtomicLong`, `AtomicInteger`)
- `javax.swing` for the GUI
- No third-party dependencies, no build tool

## Config

Edit any `.properties` file:

| key | meaning |
| --- | --- |
| `tickDurationMs` | ms per tick |
| `simulationDurationTicks` | run length |
| `deliveryProbability` | per-tick delivery chance |
| `sectionCapacity` | max boxes per section |
| `numStockers` / `numPickers` | thread counts |
| `trolleyCapacity` | boxes per trolley |
| `numTrolleys` | -1 = floor((S+P)/2) |
| `breakInterval` | ticks between stocker breaks |
| `targetPicksPerDay` | picker rate target |
| `randomSeed` | reproducibility |

## Layout

```
BoxType.java                enum of the 5 section types
simulator_clock.java        tick clock singleton
Config.java                 properties loader
Logger.java                 thread-safe event log
Trolley.java                box container
TrolleyPool.java            fair bounded pool (ReentrantLock + Condition)
TrolleyPoolSemaphore.java   semaphore variant
Section.java                shelf, exclusive stocker / concurrent pickers
SectionSynchronised.java    synchronized variant
StagingArea.java            delivery dropoff
DeliverySystem.java         delivery thread
Stocker.java                stocker thread
Picker.java                 picker thread (absolute-time scheduling)
PickerRelative.java         relative-time variant
Statistics.java             atomic counters
WarehouseSimulation.java    CLI entry point, wires everything up
SimulationGUI.java          Swing GUI
*.properties                example configs
```
