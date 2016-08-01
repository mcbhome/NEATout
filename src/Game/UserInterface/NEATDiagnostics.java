package Game.UserInterface;

import Game.Breakout.GameStats;
import Game.neat.Genome;
import Game.neat.Population;
import Game.neat.Simulation;
import Game.neat.Species;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * Created by qfi_2 on 27.07.2016.
 */
public class NEATDiagnostics extends JFrame implements Observer {

    private static final String TO_BE_DETERMINED = "TBD";
    private JTabbedPane NetworkPopulationTabbedPane;
    private JPanel PopulationPane;
    private JPanel NetworkPane;
    private JLabel networkCountTextLabel;
    private JLabel generationCountTextLabel;
    private JLabel maxFitnessTextLabel;
    private JTable networkTable;
    private JPanel mainPane;
    private JLabel generationLabel;
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel levelLabel;
    private JLabel maxFitnessLabel;
    private JLabel networkCountLabel;
    private JLabel shotsLabel;
    private JLabel currentNetLabelId;
    private MyTableModel tableModel;
    private Simulation simulation;
    private Population population;
    private GameStats gameStats;
    private String[] networkTableColumnNames = {"ID", "SpeciesID", "# Nodes", "#Connections (#Active)", "Fitness", "SharedFitness"};

    public NEATDiagnostics() {
        super("NEAT Diagnostics");

        simulation = new Simulation(GameStats.getInstance());
        population = simulation.getPopulation();
        gameStats = gameStats.getInstance();

        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem item = new JMenuItem("Save current population to file...");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Object serialization
            }
        });

        file.add(item);

        item = new JMenuItem("Load population from file...");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Object loading
            }
        });

        file.add(item);

        item = new JMenuItem("Exit");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: Exit & cleanup
            }
        });

        file.add(item);
        menubar.add(file);
        setJMenuBar(menubar);

        setResizable(false);
        $$$setupUI$$$();
        setContentPane(mainPane);
        mainPane.setPreferredSize(new Dimension(960, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        fillTable();

        updateLabels();

        gameStats.addObserver(this);
        simulation.addObserver(this);

        setVisible(true);
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
        tableModel = new MyTableModel(0, networkTableColumnNames.length);
        tableModel.setColumnIdentifiers(networkTableColumnNames);

        networkTable = new JTable(tableModel);
        networkTable.setVisible(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && o instanceof Simulation) {
            Simulation.Update_Args argtype = (Simulation.Update_Args) arg;

            if (argtype == Simulation.Update_Args.NEW_GENERATION) {
                fillTable();
            }
        }

        updateLabels();
    }

    public void updateLabels() {
        if (population != null) {
            generationLabel.setText("" + population.getGenerationId());
            maxFitnessLabel.setText("" + population.getTopFitness());
            networkCountLabel.setText("" + population.getGenomes().size());
            currentNetLabelId.setText("" + simulation.getCurrent().getGenome().getId());
        } else {
            generationLabel.setText(TO_BE_DETERMINED);
            maxFitnessLabel.setText(TO_BE_DETERMINED);
            networkCountLabel.setText(TO_BE_DETERMINED);
            currentNetLabelId.setText(TO_BE_DETERMINED);
        }

        if (gameStats.isGameStarted()) {
            scoreLabel.setText("" + gameStats.getScore());
            livesLabel.setText("" + gameStats.getLives());
            levelLabel.setText("" + gameStats.getLevel());
            shotsLabel.setText("" + gameStats.getShots());
        } else {
            scoreLabel.setText(TO_BE_DETERMINED);
            livesLabel.setText(TO_BE_DETERMINED);
            levelLabel.setText(TO_BE_DETERMINED);
            shotsLabel.setText(TO_BE_DETERMINED);
        }
    }

    public void fillTable() {
        for (int i = tableModel.getRowCount(); i > 0; i--) {
            tableModel.removeRow(0);
        }

        for (Species s : population.getSpecies()) {
            for (Genome g : s.getGenomes()) {
                tableModel.addRow(new String[]{"" + g.getId(), "" + s.getId(), "" + g.getNodeGenes().size(), "" + g.getConnectionGenes().size() + " (" + g.getActiveConnectionCount() + ")", "" + g.getFitness(), "" + g.getSharedFitness()});
            }
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPane = new JPanel();
        mainPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        NetworkPopulationTabbedPane = new JTabbedPane();
        mainPane.add(NetworkPopulationTabbedPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), null, 0, false));
        PopulationPane = new JPanel();
        PopulationPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), 5, 5, true, false));
        NetworkPopulationTabbedPane.addTab("Population", PopulationPane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(5, 5, 5, 5), -1, -1));
        PopulationPane.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "General Info"));
        networkCountLabel = new JLabel();
        networkCountLabel.setText("Label");
        panel1.add(networkCountLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generationLabel = new JLabel();
        generationLabel.setText("Label");
        panel1.add(generationLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxFitnessLabel = new JLabel();
        maxFitnessLabel.setText("Label");
        panel1.add(maxFitnessLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxFitnessTextLabel = new JLabel();
        maxFitnessTextLabel.setText("Maximum Fitness:");
        panel1.add(maxFitnessTextLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(312, 16), null, 0, false));
        generationCountTextLabel = new JLabel();
        generationCountTextLabel.setText("Generation #: ");
        panel1.add(generationCountTextLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(312, 16), null, 0, false));
        networkCountTextLabel = new JLabel();
        networkCountTextLabel.setText("Network Count:");
        panel1.add(networkCountTextLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(312, 16), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Current Network:");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        currentNetLabelId = new JLabel();
        currentNetLabelId.setText("Label");
        panel1.add(currentNetLabelId, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(22);
        PopulationPane.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(376, 419), null, 0, false));
        scrollPane1.setViewportView(networkTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        PopulationPane.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current Game Stats"));
        final JLabel label2 = new JLabel();
        label2.setText("Score");
        panel2.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Lives:");
        panel2.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Level:");
        panel2.add(label4, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scoreLabel = new JLabel();
        scoreLabel.setText("Label");
        panel2.add(scoreLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        livesLabel = new JLabel();
        livesLabel.setText("Label");
        panel2.add(livesLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        levelLabel = new JLabel();
        levelLabel.setText("Label");
        panel2.add(levelLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Shots:");
        panel2.add(label5, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        shotsLabel = new JLabel();
        shotsLabel.setText("Label");
        panel2.add(shotsLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        NetworkPane = new JPanel();
        NetworkPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        NetworkPopulationTabbedPane.addTab("Network Detail", NetworkPane);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPane;
    }

    private class MyTableModel extends DefaultTableModel {
        public MyTableModel(int rowCount, int columnCount) {
            super(rowCount, columnCount);
        }

        public void removeRowWithNetworkID(int id) {
            for (int i = 0; i < this.dataVector.size(); i++) {
                Vector cur = (Vector) this.dataVector.get(i);

                if (Integer.parseInt((String) cur.get(0)) == id) {
                    this.removeRow(i);
                }
            }
        }
    }

}
