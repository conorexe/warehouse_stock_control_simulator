import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.EnumMap;

public class SimulationGUI extends JFrame {

    private final JSpinner tickDurationMs   = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 10));
    private final JSpinner simDurationTicks = new JSpinner(new SpinnerNumberModel(480, 1, 1000000, 10));
    private final JSpinner deliveryProb     = new JSpinner(new SpinnerNumberModel(0.01, 0.0, 1.0, 0.01));
    private final JSpinner sectionCapacity  = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
    private final JSpinner numStockers      = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
    private final JSpinner numPickers       = new JSpinner(new SpinnerNumberModel(4, 1, 100, 1));
    private final JSpinner trolleyCapacity  = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
    private final JSpinner numTrolleys      = new JSpinner(new SpinnerNumberModel(-1, -1, 1000, 1));
    private final JSpinner breakInterval    = new JSpinner(new SpinnerNumberModel(120, 1, 100000, 10));
    private final JSpinner targetPicksDay   = new JSpinner(new SpinnerNumberModel(100, 1, 1000000, 10));
    private final JSpinner randomSeed       = new JSpinner(new SpinnerNumberModel(42L, Long.MIN_VALUE, Long.MAX_VALUE, 1L));

    private final JButton startBtn = new JButton("Start");
    private final JButton stopBtn  = new JButton("Stop");

    private final JLabel  tickLabel       = bigLabel("tick: 0");
    private final JLabel  picksLabel      = bigLabel("picks: 0");
    private final JLabel  stockedLabel    = bigLabel("stocked: 0");
    private final JLabel  breaksLabel     = bigLabel("breaks: 0");
    private final JLabel  avgWaitLabel    = bigLabel("avg pick wait: 0.0");
    private final JLabel  stagingLabel    = bigLabel("staging boxes: 0");

    private final EnumMap<BoxType, SectionBar> sectionBars = new EnumMap<>(BoxType.class);

    private final JTextArea logArea = new JTextArea();
    private final Timer refreshTimer;

    private WarehouseSimulation sim;
    private Thread runnerThread;

    public SimulationGUI() {
        super("Warehouse Stock Control Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        add(buildConfigPanel(),  BorderLayout.WEST);
        add(buildCenterPanel(),  BorderLayout.CENTER);
        add(buildLogPanel(),     BorderLayout.SOUTH);

        redirectStdOut();

        startBtn.addActionListener(this::onStart);
        stopBtn.addActionListener(this::onStop);
        stopBtn.setEnabled(false);

        refreshTimer = new Timer(200, e -> refresh());
        refreshTimer.start();

        setSize(1100, 800);
        setLocationRelativeTo(null);
    }

    private JPanel buildConfigPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Configuration"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        addRow(p, c, row++, "tickDurationMs",          tickDurationMs);
        addRow(p, c, row++, "simulationDurationTicks", simDurationTicks);
        addRow(p, c, row++, "deliveryProbability",     deliveryProb);
        addRow(p, c, row++, "sectionCapacity",         sectionCapacity);
        addRow(p, c, row++, "numStockers",             numStockers);
        addRow(p, c, row++, "numPickers",              numPickers);
        addRow(p, c, row++, "trolleyCapacity",         trolleyCapacity);
        addRow(p, c, row++, "numTrolleys (-1=auto)",   numTrolleys);
        addRow(p, c, row++, "breakInterval",           breakInterval);
        addRow(p, c, row++, "targetPicksPerDay",       targetPicksDay);
        addRow(p, c, row++, "randomSeed",              randomSeed);

        JButton loadBtn = new JButton("Load .properties...");
        loadBtn.addActionListener(e -> onLoadProperties());

        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(loadBtn, c);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 6, 0));
        btnRow.add(startBtn);
        btnRow.add(stopBtn);
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        p.add(btnRow, c);

        c.gridy = row++; c.weighty = 1;
        p.add(Box.createGlue(), c);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(p, BorderLayout.NORTH);
        wrap.setBorder(new EmptyBorder(8, 8, 8, 8));
        return wrap;
    }

    private static void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1;
        Dimension d = field.getPreferredSize();
        field.setPreferredSize(new Dimension(Math.max(120, d.width), d.height));
        p.add(field, c);
    }

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBorder(new EmptyBorder(8, 0, 0, 8));

        JPanel stats = new JPanel(new GridLayout(2, 3, 8, 4));
        stats.setBorder(new TitledBorder("Live statistics"));
        stats.add(tickLabel);
        stats.add(picksLabel);
        stats.add(stockedLabel);
        stats.add(breaksLabel);
        stats.add(avgWaitLabel);
        stats.add(stagingLabel);
        center.add(stats, BorderLayout.NORTH);

        JPanel sectionsPanel = new JPanel(new GridLayout(BoxType.values().length, 1, 0, 6));
        sectionsPanel.setBorder(new TitledBorder("Sections (boxes / capacity)"));
        for (BoxType t : BoxType.values()) {
            SectionBar bar = new SectionBar(t);
            sectionBars.put(t, bar);
            sectionsPanel.add(bar);
        }
        center.add(sectionsPanel, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildLogPanel() {
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane sp = new JScrollPane(logArea);
        sp.setPreferredSize(new Dimension(0, 220));
        sp.setBorder(new TitledBorder("Log"));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(0, 8, 8, 8));
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private static JLabel bigLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        return l;
    }

    private void onLoadProperties() {
        JFileChooser fc = new JFileChooser(".");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Config cfg = Config.loadFromFile(fc.getSelectedFile().getAbsolutePath());
            tickDurationMs.setValue((int) cfg.getTickDurationMs());
            simDurationTicks.setValue(cfg.getSimulationDurationTicks());
            deliveryProb.setValue(cfg.getDeliveryProbability());
            sectionCapacity.setValue(cfg.getSectionCapacity());
            numStockers.setValue(cfg.getNumStockers());
            numPickers.setValue(cfg.getNumPickers());
            trolleyCapacity.setValue(cfg.getTrolleyCapacity());
            numTrolleys.setValue(cfg.getNumTrolleysOverride());
            breakInterval.setValue(cfg.getBreakInterval());
            targetPicksDay.setValue(cfg.getTargetPicksPerDay());
            randomSeed.setValue(cfg.getRandomSeed());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onStart(ActionEvent e) {
        Config cfg = new Config();
        cfg.setTickDurationMs(((Number) tickDurationMs.getValue()).longValue());
        cfg.setSimulationDurationTicks(((Number) simDurationTicks.getValue()).intValue());
        cfg.setDeliveryProbability(((Number) deliveryProb.getValue()).doubleValue());
        cfg.setSectionCapacity(((Number) sectionCapacity.getValue()).intValue());
        cfg.setNumStockers(((Number) numStockers.getValue()).intValue());
        cfg.setNumPickers(((Number) numPickers.getValue()).intValue());
        cfg.setTrolleyCapacity(((Number) trolleyCapacity.getValue()).intValue());
        cfg.setNumTrolleys(((Number) numTrolleys.getValue()).intValue());
        cfg.setBreakInterval(((Number) breakInterval.getValue()).intValue());
        cfg.setTargetPicksPerDay(((Number) targetPicksDay.getValue()).intValue());
        cfg.setRandomSeed(((Number) randomSeed.getValue()).longValue());

        logArea.setText("");
        System.out.println("=== Starting simulation ===");
        cfg.printSummary();

        sim = new WarehouseSimulation(cfg);
        sim.start();

        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        setConfigEditable(false);

        runnerThread = new Thread(() -> {
            try {
                sim.waitForCompletion();
                sim.printStatistics();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    startBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                    setConfigEditable(true);
                });
            }
        }, "sim-runner");
        runnerThread.start();
    }

    private void onStop(ActionEvent e) {
        if (sim != null) sim.stopNow();
    }

    private void setConfigEditable(boolean enabled) {
        for (JSpinner s : new JSpinner[]{tickDurationMs, simDurationTicks, deliveryProb,
                sectionCapacity, numStockers, numPickers, trolleyCapacity, numTrolleys,
                breakInterval, targetPicksDay, randomSeed}) {
            s.setEnabled(enabled);
        }
    }

    private void refresh() {
        if (sim == null) return;
        EnumMap<BoxType, Section> sections = sim.getSections();
        if (sections == null) return;

        tickLabel.setText("tick: " + sim.getCurrentTick());
        Statistics st = sim.getStatistics();
        long picks = st.getTotalPicks();
        long waitSum = st.getTotalPickWaitTicks();
        double avg = picks > 0 ? (double) waitSum / picks : 0.0;
        picksLabel.setText("picks: " + picks);
        stockedLabel.setText("stocked: " + st.getTotalBoxesStocked());
        breaksLabel.setText("breaks: " + st.getTotalBreaks());
        avgWaitLabel.setText(String.format("avg pick wait: %.2f", avg));
        StagingArea sa = sim.getStagingArea();
        stagingLabel.setText("staging boxes: " + (sa != null ? sa.getTotalBoxes() : 0));

        for (BoxType t : BoxType.values()) {
            Section s = sections.get(t);
            if (s != null) sectionBars.get(t).update(s, st.getPicksFor(t), st.getStockedFor(t));
        }
    }

    private void redirectStdOut() {
        PrintStream ps = new PrintStream(new OutputStream() {
            private final StringBuilder buf = new StringBuilder();
            @Override public void write(int b) {
                buf.append((char) b);
                if (b == '\n') {
                    final String s = buf.toString();
                    buf.setLength(0);
                    SwingUtilities.invokeLater(() -> {
                        logArea.append(s);
                        int max = 200_000;
                        if (logArea.getDocument().getLength() > max) {
                            try {
                                logArea.getDocument().remove(0,
                                        logArea.getDocument().getLength() - max);
                            } catch (Exception ignore) {}
                        }
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    });
                }
            }
        }, true);
        System.setOut(ps);
    }

    private static class SectionBar extends JPanel {
        private final JLabel  name = new JLabel();
        private final JProgressBar bar = new JProgressBar(0, 10);
        private final JLabel stats = new JLabel();

        SectionBar(BoxType t) {
            setLayout(new BorderLayout(6, 0));
            name.setPreferredSize(new Dimension(110, 20));
            name.setText(t.getName());
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            bar.setStringPainted(true);
            stats.setPreferredSize(new Dimension(180, 20));
            add(name, BorderLayout.WEST);
            add(bar,  BorderLayout.CENTER);
            add(stats, BorderLayout.EAST);
        }

        void update(Section s, long picks, long stocked) {
            int cap = s.getCapacity();
            int cnt = s.getBoxCount();
            bar.setMaximum(cap);
            bar.setValue(Math.min(cnt, cap));
            bar.setString(cnt + " / " + cap);
            bar.setForeground(colorFor(cnt, cap));
            stats.setText(String.format(
                    " picks=%d stocked=%d waiting=%d %s",
                    picks, stocked, s.getWaitingPickersCount(),
                    s.isStockerActive() ? "[stocker]" : ""));
        }

        private static Color colorFor(int cnt, int cap) {
            if (cap == 0) return Color.GRAY;
            double r = (double) cnt / cap;
            if (r < 0.2)  return new Color(220, 70, 70);
            if (r < 0.5)  return new Color(220, 160, 60);
            return new Color(70, 160, 80);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignore) {}
        SwingUtilities.invokeLater(() -> new SimulationGUI().setVisible(true));
    }
}
