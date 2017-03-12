package UserInterface;

import Breakout.Board;
import Breakout.GameStats;
import neat.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by qfi_2 on 27.07.2016.
 */
public class NEATDiagnostics extends JFrame implements Observer {

    private static final String TO_BE_DETERMINED = "TBD";
    private JTabbedPane networkPopulationTabbedPane;
    private JPanel populationPane;
    private JPanel networkDetailPane;
//    private JLabel networkCountTextLabel;
//    private JLabel generationCountTextLabel;
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
    private JLabel networkDetailIDLabel;
    private JLabel networkDetailConnectionCountLabel;
    private JLabel networkDetailHiddenNodeCount;
    private JLabel networkDetailLayerCount;
    private JLabel networkDetailBallInput;
    private JLabel networkDetailPaddleInput;
    private JComboBox<String> networkDetailComboBox;
    private JButton networkDetaiLoadCurrent;
    private JButton networkDetailLoadID;
    private JToolBar toolBar;
    private JButton playButton;
    private JButton pauseButton;
    private JButton newPopulationButton;
    private JButton restartGenerationButton;
    private JPanel statisticsPane;
    private JTable statisticsTable;
    private JCheckBox trainingCheckBox;
    private JComboBox<String> networkSelectionComboBox;
    private JLabel networkDetailBallSpeedInput;
    private JButton loadNetworkButton;
    private JLabel stalenessLabel;
    private JComboBox<String> simSpeedComboBox;
    private JFileChooser fileChooser;
    private DefaultComboBoxModel<String> networkDetailComboModel;
    private DefaultComboBoxModel loadNetworkComboModel;
    private DefaultTableModel currentPopulationTableModel;
    private DefaultTableModel statisticsTableModel;
    private DefaultComboBoxModel<String> simSpeedModel;
    private Board board;
    private Simulation simulation;
    private Population population;
    private GameStats gameStats;
    private Genome curDetailGenome;
    private boolean showCurrent;
    private int generationSaveMod = 0;
    private int lastSavedGeneration;
    private String[] networkTableColumnNames = {"ID", "SpeciesID", "# Nodes", "#Connections (#Active)", "Fitness", "SharedFitness"};
    private String[] historyTableColumnNames = {"Gen. ID", "Average Fitness", "Top Fitness"};

    public NEATDiagnostics() {
        super("NEAT Diagnostics");

        simulation = new Simulation(GameStats.getInstance());
        population = simulation.getPopulation();
        gameStats = gameStats.getInstance();
        board = Board.getInstance();

        showCurrent = true;

        fileChooser = new JFileChooser();

        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu("File");

        JMenuItem item = new JMenuItem("Save");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serializeSim();
                } catch (IOException exception) {
                    System.out.println("Error during sim serialization");
                    exception.printStackTrace();
                }
            }
        });

        file.add(item);

        item = new JMenuItem("Save as...");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showSaveDialog(NEATDiagnostics.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        serializeSim(file);
                    } catch (IOException exception) {
                        System.out.println("Error during sim serialization");
                        exception.printStackTrace();
                    }
                }
            }
        });

        file.add(item);

        item = new JMenu("Autosave every...");

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem rbItem = new JRadioButtonMenuItem("Off");
        rbItem.setSelected(true);
        rbItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generationSaveMod = 0;
            }
        });
        group.add(rbItem);
        item.add(rbItem);
        rbItem = new JRadioButtonMenuItem("5 Generations");
        rbItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generationSaveMod = 5;
            }
        });
        group.add(rbItem);
        item.add(rbItem);
        rbItem = new JRadioButtonMenuItem("10 Generations");
        rbItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generationSaveMod = 10;
            }
        });
        group.add(rbItem);
        item.add(rbItem);
        rbItem = new JRadioButtonMenuItem("20 Generations");
        rbItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generationSaveMod = 20;
            }
        });
        group.add(rbItem);
        item.add(rbItem);
        rbItem = new JRadioButtonMenuItem("50 Generations");
        rbItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generationSaveMod = 50;
            }
        });
        group.add(rbItem);
        item.add(rbItem);
        rbItem = new JRadioButtonMenuItem("100 Generations");
        rbItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generationSaveMod = 100;
            }
        });
        group.add(rbItem);
        item.add(rbItem);
        file.add(item);

        item = new JMenuItem("Load population from file...");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.showOpenDialog(NEATDiagnostics.this);
                try {
                    Simulation sim = loadSim(fileChooser.getSelectedFile());
                    setSimulation(sim);
                    updateUIComponents();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
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
        setupUI();

        networkDetailLoadID.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = Integer.parseInt((String) networkDetailComboModel.getSelectedItem());
                Genome g = simulation.getPopulation().getGenomeById(id);

                if (g != null) {
                    curDetailGenome = g;
                    updateLabels();
                    showCurrent = false;
                }
            }
        });


        trainingCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            	if (e.getSource() == trainingCheckBox) {
                    setTrainingMode(trainingCheckBox.isSelected());
            	}
                gameStats.resetGame();
                pauseGame();
            }
        });

        loadNetworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = Integer.parseInt((String) networkSelectionComboBox.getSelectedItem());
                for (NeuralNetwork n : simulation.getNetsInCurrentGeneration()) {
                    if (id == n.getGenome().getId()) {
                        simulation.setCurrent(n);
                        gameStats.resetGame();
                        pauseGame();
                    }
                }
            }
        });

        addButtonActionListeners();

        trainingCheckBox.setSelected(true);

        pauseGame();

        setContentPane(mainPane);
        mainPane.setPreferredSize(new Dimension(960, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();


        fillTables();
        updateLabels();

        gameStats.addObserver(this);
        simulation.addObserver(this);

        validate();
        setVisible(true);
    }

    private void pauseGame() {
        gameStats.pauseGame();
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
    }

    private void unPauseGame() {
        gameStats.unPauseGame();
        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
    }

    private void addButtonActionListeners() {
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStats.isGamePaused()) {
                    unPauseGame();
                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameStats.isGamePaused()) {
                    pauseGame();
                }
            }
        });

        restartGenerationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStats.resetGame();
                pauseGame();

                simulation.resetGeneration();

                updateUIComponents();
            }
        });

        newPopulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStats.resetGame();
                pauseGame();
                simulation.resetPopulation();
                population = simulation.getPopulation();
                updateUIComponents();
            }
        });
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
        currentPopulationTableModel = new DefaultTableModel(0, networkTableColumnNames.length);
        currentPopulationTableModel.setColumnIdentifiers(networkTableColumnNames);

        networkTable = new JTable(currentPopulationTableModel);
        networkTable.setAutoCreateRowSorter(true);
        //networkTable.setVisible(true);

        statisticsTableModel = new DefaultTableModel(0, historyTableColumnNames.length);
        statisticsTableModel.setColumnIdentifiers(historyTableColumnNames);

        statisticsTable = new JTable(statisticsTableModel);
        statisticsTable.setAutoCreateRowSorter(true);
        DefaultRowSorter sorter = (DefaultRowSorter) statisticsTable.getRowSorter();
        ArrayList<RowSorter.SortKey> keys = new ArrayList<RowSorter.SortKey>();
        keys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        sorter.setSortKeys(keys);

        Vector<String> vec = new Vector<String>();

        for (Genome g : simulation.getPopulation().getGenomes()) {
            vec.add("" + g.getId());
        }

        networkDetailComboModel = new DefaultComboBoxModel<String>(vec);
        networkDetailComboBox = new JComboBox<String>(networkDetailComboModel);

        networkSelectionComboBox = new JComboBox<String>(networkDetailComboModel);

        simSpeedModel = new DefaultComboBoxModel<String>();
        simSpeedModel.addElement("x1");
        simSpeedModel.addElement("x2");
        simSpeedModel.addElement("x4");
        simSpeedModel.addElement("x8");

        simSpeedComboBox = new JComboBox<String>(simSpeedModel);

        simSpeedComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String item = (String) e.getItem();

                switch (item) {
                    case "x1":
                        board.setSimSpeed(8);
                        break;
                    case "x2":
                        board.setSimSpeed(4);
                        break;
                    case "x4":
                        board.setSimSpeed(2);
                        break;
                    case "x8":
                        board.setSimSpeed(1);
                        break;
                    default:
                        board.setSimSpeed(8);
                        break;
                }
            }
        });

        mainPane = new JPanel();

        toolBar = new JToolBar();
        mainPane.add(toolBar);

    }

    @Override
    public synchronized void update(Observable o, Object arg) {
        if (arg != null && o instanceof Simulation) {
            Simulation.Update_Args argtype = (Simulation.Update_Args) arg;

            if (argtype == Simulation.Update_Args.NEW_GENERATION) {
                fillTables();
                updateComboBox();
                if (showCurrent) {
                    curDetailGenome = simulation.getCurrent().getGenome();
                }

                if (generationSaveMod != 0 && simulation.getPopulation().getGenerationId() % generationSaveMod == 0 && simulation.getPopulation().getGenerationId() != lastSavedGeneration) {
                    try {
                        serializeSim();
                    } catch (IOException e) {
                        System.out.println("IOException during serialization occured");
                    }
                }
            } else if (argtype == Simulation.Update_Args.PLAYER_DIED) {
                if (showCurrent) {
                    curDetailGenome = simulation.getCurrent().getGenome();
                }
            }
        }

        updateLabels();
    }

    private void updateUIComponents() {
        this.updateLabels();
        this.updateComboBox();
        this.fillTables();
    }

    private void setTrainingMode(boolean trainingMode) {
        networkSelectionComboBox.setEnabled(!trainingMode);
        loadNetworkButton.setEnabled(!trainingMode);
        restartGenerationButton.setEnabled(trainingMode);
        newPopulationButton.setEnabled(trainingMode);
        simulation.setTrainingMode(trainingMode);
    }

    private void updateComboBox() {
        networkDetailComboModel.removeAllElements();

        for (Genome g : population.getGenomes()) {
            networkDetailComboModel.addElement("" + g.getId());
        }
    }

    public void updateLabels() {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        if (population != null && simulation.getCurrent() != null) {
            generationLabel.setText("" + population.getGenerationId());
            maxFitnessLabel.setText("" + nf.format(population.getTopFitness()));
            networkCountLabel.setText("" + population.getGenomes().size());
            currentNetLabelId.setText("" + simulation.getCurrent().getGenome().getId());
            stalenessLabel.setText("" + population.getStaleness());
        } else {
            generationLabel.setText(TO_BE_DETERMINED);
            maxFitnessLabel.setText(TO_BE_DETERMINED);
            networkCountLabel.setText(TO_BE_DETERMINED);
            currentNetLabelId.setText(TO_BE_DETERMINED);
        }

        if (curDetailGenome == null) {
            curDetailGenome = simulation.getCurrent().getGenome();
        }

        if (curDetailGenome != null) {
            networkDetailIDLabel.setText("" + curDetailGenome.getId());
            networkDetailConnectionCountLabel.setText("" + curDetailGenome.getConnectionGenes().size());
            networkDetailHiddenNodeCount.setText("" + (curDetailGenome.getNodeGenes().size() - curDetailGenome.getMandatoryNodes().size()));
            networkDetailLayerCount.setText("" + curDetailGenome.getLayers());
            networkDetailBallInput.setText("" + nf.format(curDetailGenome.getBallInputNeuron().getInput()));
            networkDetailPaddleInput.setText("" + nf.format(curDetailGenome.getPaddleInputNeuron().getInput()));
            //networkDetailBallSpeedInput.setText("" + nf.format(curDetailGenome.getBallSpeedInputNeuron().getInput()));
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

    public void fillTables() {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);

        for (int i = currentPopulationTableModel.getRowCount(); i > 0; i--) {
            currentPopulationTableModel.removeRow(0);
        }

        for (Species s : population.getSpecies()) {
            for (Genome g : s.getGenomes()) {

                currentPopulationTableModel.addRow(new String[]{"" + g.getId(), "" + s.getId(), "" + g.getNodeGenes().size(), "" + g.getConnectionGenes().size() + " (" + g.getActiveConnectionCount() + ")", "" + nf.format(g.getFitness()), "" + nf.format(g.getSharedFitness())});
            }
        }

        for (int i = statisticsTableModel.getRowCount(); i > 0; i--) {
            statisticsTableModel.removeRow(0);
        }

        TreeMap<Integer, HashMap<String, Double>> history = simulation.getHistoryMap();

        DefaultRowSorter sorter = (DefaultRowSorter) statisticsTable.getRowSorter();

        for (Integer i : history.keySet()) {
            HashMap<String, Double> values = history.get(i);

            statisticsTableModel.addRow(new String[]{"" + i, nf.format(values.get(Simulation.AVERAGE_FITNESS_KEY)), nf.format(values.get(Simulation.TOP_FITNESS_KEY))});
            sorter.sort();
        }
    }

    private void serializeSim() throws IOException {
        GregorianCalendar cal = new GregorianCalendar();
        String fileName = "simulation_gen" + simulation.getPopulation().getGenerationId() +
                "_" + String.format("%02d", cal.get(GregorianCalendar.DAY_OF_MONTH)) +
                "-" + String.format("%02d", cal.get(GregorianCalendar.MONTH)) +
                "-" + String.format("%04d", cal.get(GregorianCalendar.YEAR)) +
                "_" + String.format("%02d", cal.get(GregorianCalendar.HOUR_OF_DAY)) +
                "-" + String.format("%02d", cal.get(GregorianCalendar.MINUTE)) +
                ".ser";

        File f = new File("./" + fileName);

        if (!f.exists()) {
            f.createNewFile();
        }

        serializeSim(f);
    }

    private Simulation loadSim(File f) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream ois = new ObjectInputStream(fis);

        Simulation res = (Simulation) ois.readObject();

        fis.close();
        ois.close();


        return res;
    }

    private void serializeSim(File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(simulation);

        fos.close();
        oos.close();

        lastSavedGeneration = simulation.getPopulation().getGenerationId();
    }

    private void setSimulation(Simulation s) {
        this.simulation.destroy();
        this.simulation = s;
        this.population = s.getPopulation();
        this.curDetailGenome = s.getCurrent().getGenome();
        simulation.addObserver(this);
    }
    
    private JLabel addLabeledTextToPanel(String labelText, JPanel parentPanel) {
    	JLabel labelComponent = new JLabel();
    	labelComponent.setText(labelText);
    	parentPanel.add(labelComponent);
    	JLabel variableText = new JLabel();
    	variableText.setText("Label");
    	parentPanel.add(variableText);
    	return variableText;
    }

    /**
     * Setup the UI for NEAT Diagnostics window
     */
    private void setupUI() {
        createUIComponents();
        mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        networkPopulationTabbedPane = new JTabbedPane();
        populationPane = new JPanel();
        populationPane.setLayout(new GridLayout(2, 2, 0, 0));
        networkPopulationTabbedPane.addTab("Current Generation", populationPane);

        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayout(5, 2, 5, 5));
        populationPane.add(panel1);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "General Info"));
        networkCountLabel = addLabeledTextToPanel("Network Count:", panel1);
        generationLabel = addLabeledTextToPanel("Generation #: ", panel1);
        maxFitnessLabel = addLabeledTextToPanel("Maximum Fitness:", panel1);
        currentNetLabelId = addLabeledTextToPanel("Current Network:", panel1);
        stalenessLabel = addLabeledTextToPanel("Staleness:", panel1);

        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(22);
        populationPane.add(scrollPane1);
        scrollPane1.setViewportView(networkTable);

        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(4, 2, 0, 0));
        populationPane.add(panel2);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Current Game Stats"));
        scoreLabel = addLabeledTextToPanel("Score", panel2);
        livesLabel = addLabeledTextToPanel("Lives:", panel2);
        levelLabel = addLabeledTextToPanel("Level:", panel2);
        shotsLabel = addLabeledTextToPanel("Shots:", panel2);

        networkDetailPane = new JPanel();
        networkDetailPane.setLayout(new GridLayout(2, 4, 0, 10));
        networkPopulationTabbedPane.addTab("Individual Detail", networkDetailPane);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayout(4, 2, 0, 10));
        networkDetailPane.add(panel3);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "General"));
        networkDetailIDLabel = addLabeledTextToPanel("ID:", panel3);
        networkDetailHiddenNodeCount = addLabeledTextToPanel("# Nodes (Hidden):", panel3);
        networkDetailConnectionCountLabel = addLabeledTextToPanel("# Connections (Active)", panel3);
        networkDetailLayerCount = addLabeledTextToPanel("# Layers:", panel3);

        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayout(1, 2, 0, 0));
        networkDetailPane.add(panel4);

        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayout(3, 2, 0, 10));
        panel4.add(panel5);
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Inputs"));
        networkDetailBallInput = addLabeledTextToPanel("BallX:", panel5);
        networkDetailPaddleInput = addLabeledTextToPanel("PaddleX:", panel5);
        networkDetailBallSpeedInput = addLabeledTextToPanel("BallSpeed", panel5);

        final JLabel label14 = new JLabel();
        label14.setText("Network");
        networkDetailPane.add(label14);
        networkDetailPane.add(networkDetailComboBox);
        networkDetaiLoadCurrent = new JButton();
        networkDetaiLoadCurrent.setText("Load Current");
        networkDetailPane.add(networkDetaiLoadCurrent);
        networkDetailLoadID = new JButton();
        networkDetailLoadID.setText("Load");
        networkDetailPane.add(networkDetailLoadID);

        statisticsPane = new JPanel();
        statisticsPane.setLayout(new GridLayout(1, 1, 0, 0));
        networkPopulationTabbedPane.addTab("Ancestors Statistics", statisticsPane);
        final JScrollPane scrollPane2 = new JScrollPane();
        statisticsPane.add(scrollPane2);
        scrollPane2.setViewportView(statisticsTable);
        
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        playButton = new JButton();
        playButton.setIcon(new ImageIcon(getClass().getResource("/UserInterface/play24x24.png")));
        playButton.setText("");
        playButton.setToolTipText("Start/Resume simulation");
        toolBar.add(playButton);
        pauseButton = new JButton();
        pauseButton.setIcon(new ImageIcon(getClass().getResource("/UserInterface/pause24x24.png")));
        pauseButton.setText("");
        pauseButton.setToolTipText("Pause simulation");
        toolBar.add(pauseButton);
        restartGenerationButton = new JButton();
        restartGenerationButton.setIcon(new ImageIcon(getClass().getResource("/UserInterface/restart24x24.png")));
        restartGenerationButton.setText("");
        restartGenerationButton.setToolTipText("Restart simulation for current generation");
        toolBar.add(restartGenerationButton);
        newPopulationButton = new JButton();
        newPopulationButton.setIcon(new ImageIcon(getClass().getResource("/UserInterface/new24x24.png")));
        newPopulationButton.setText("");
        newPopulationButton.setToolTipText("Start with new population");
        toolBar.add(newPopulationButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar.add(toolBar$Separator1);
        trainingCheckBox = new JCheckBox();
        trainingCheckBox.setText("Training Mode");
        toolBar.add(trainingCheckBox);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        toolBar.add(toolBar$Separator2);
        final JLabel labelNetSel = new JLabel();
        labelNetSel.setText("Network Selection:");
        toolBar.add(labelNetSel);
        toolBar.add(networkSelectionComboBox);
        loadNetworkButton = new JButton();
        loadNetworkButton.setText("Load Network");

        JPanel gameSpeedPanel = new JPanel(new FlowLayout());
        final JLabel label15 = new JLabel();
        label15.setText("Game Speed:");
        gameSpeedPanel.add(label15);
        gameSpeedPanel.add(simSpeedComboBox);
        
        mainPane.add(toolBar);
        mainPane.add(gameSpeedPanel);
        mainPane.add(networkPopulationTabbedPane);
        mainPane.add(loadNetworkButton);

    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPane;
    }
}
