package Game.neat;
import Game.Breakout.GameStats;
import Game.Breakout.Paddle;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by qfi_2 on 26.07.2016.
 */
public class Simulation extends Observable implements Observer, Serializable {
    public enum Update_Args {PLAYER_DIED, BRICK_CHANGE, SCORE_CHANGED, MOVEMENT, MISC, NEW_GAME, NEW_GENERATION}
    public static final String TOP_FITNESS_KEY = "TOP_FITNESS";
    public static final String AVERAGE_FITNESS_KEY = "AVERAGE_FITNESS";
    private static final double SCORE_FACTOR = 0.01;
    private static final double SHOTS_FACTOR = 10.0;

    private static final double MAXIMUM_SPEED_FACTOR = 3;

    private Population p;
    private NeuralNetwork current;

    private ArrayList<Neuron> mandatoryNeurons;

    private TreeMap<Integer, HashMap<String, Double>> historyMap;

    private transient GameStats gameStats;
    private transient LinkedList<NeuralNetwork> remainingNetsInGeneration;
    private transient HashMap<NeuralNetwork, Double> calculatedFitnesses;

    public Simulation(GameStats gameStats) {
        this.gameStats = gameStats;
        p = new Population();
        remainingNetsInGeneration = new LinkedList<NeuralNetwork>();
        calculatedFitnesses = new HashMap<NeuralNetwork, Double>();
        historyMap = new TreeMap<Integer, HashMap<String, Double>>();

        boolean[][] bricks = gameStats.getBricks();

        mandatoryNeurons = new ArrayList<Neuron>();

        for (int i = 0; i < bricks.length; i++) {
            for (int j = 0; j < bricks[i].length; j++) {
                mandatoryNeurons.add(new BrickInputNeuron(i, j));
            }
        }

        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.SENSOR_PADDLE));
        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.SENSOR_BALL));

        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.OUTPUT_LEFT));
        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.OUTPUT_RIGHT));

        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.BIAS));


        p.initializePopulation(mandatoryNeurons);
        //enterDebugData();

        putGenerationIntoQueue();

        gameStats.addObserver(this);
    }

    private void enterDebugData() {
        for (Genome g : p.getGenomes()) {
            Neuron n = new Neuron(Neuron.Neuron_Type.HIDDEN);
            Neuron m = new Neuron(Neuron.Neuron_Type.HIDDEN);

            g.addFromExistingConnection(new Connection(g.getBrickInputNeurons()[0][0], g.getLeftOutputNeuron(), 1, 1, g));
        }
    }

    public Simulation() {
        super();
    }

    @Override
    public void update(Observable o, Object arg) {
        ObservableArg observableArg = (ObservableArg) arg;
        Update_Args type = observableArg.getType();

        if (type != Update_Args.MOVEMENT){
            System.out.println();
        }

        if (type == Update_Args.PLAYER_DIED) {
            if (gameStats.getLives() == 0) {
                finishSimulationForCurrentNetwork();
                properlyNotify(Update_Args.NEW_GENERATION);
            }
        } else if (type == Update_Args.BRICK_CHANGE) {
            current.setBrickInput(observableArg.getI(), observableArg.getJ(), observableArg.isBrickState());
        } else if (type == Update_Args.NEW_GAME) {
            initBrickInputsForCurrentNetwork();
        } else if (type == Update_Args.MOVEMENT) {
            current.setPaddlePosition(gameStats.getPaddle().getXLeft());
            current.setBallPosition(gameStats.getBall().getX());
            current.setBallPosition(gameStats.getBall().getX());

            calculateOutputAndMovePaddle();
        }
    }

    private void getNewCurrentNetwork() {
        current = remainingNetsInGeneration.poll();

        if (current == null) {
            current = null;
            HashMap<String, Double> currentStatistics = historyMap.get(p.getGenerationId());

            if (currentStatistics == null) {
                currentStatistics = new HashMap<String, Double>();
                historyMap.put(p.getGenerationId(), currentStatistics);
            }

            currentStatistics.put(TOP_FITNESS_KEY, p.getTopFitness());

            double totalFitness = 0;

            for (Genome g : p.getGenomes()) {
                totalFitness += g.getFitness();
            }

            double averageFitness = totalFitness / p.getGenomes().size();

            currentStatistics.put(AVERAGE_FITNESS_KEY, averageFitness);

            calculateFinalFitnessValues();

            p.newGeneration();
            putGenerationIntoQueue();
        }

        initBrickInputsForCurrentNetwork();
    }

    public double calculateCurrentFitness() {
        return gameStats.getScore() * SCORE_FACTOR + gameStats.getShots() * SHOTS_FACTOR;
    }

    public void initBrickInputsForCurrentNetwork() {
        boolean[][] bricks = gameStats.getBricks();

        for (int i = 0; i < bricks.length; i++) {
            for (int j = 0; j < bricks[i].length; j++) {
                current.setBrickInput(i, j, bricks[i][j]);
            }
        }
    }

    public void calculateOutputAndMovePaddle() {
        current.propagateInputs();

        double dirLeft = current.getLeftOutput();
        double dirRight = current.getRightOutput();

        controlPaddle(dirLeft, dirRight);

        current.reset();
    }

    public void controlPaddle(double dirLeft, double dirRight) {
        if (dirRight < dirLeft) {
            gameStats.getPaddle().changeDir(Paddle.Direction.LEFT, dirLeft * MAXIMUM_SPEED_FACTOR);
        } else if (dirRight > dirLeft){
            gameStats.getPaddle().changeDir(Paddle.Direction.RIGHT, dirRight * MAXIMUM_SPEED_FACTOR);
        }
    }

    private void finishSimulationForCurrentNetwork() {
        double fitness = calculateCurrentFitness();
        calculatedFitnesses.put(current, fitness);
        p.updateFitness(current.getGenome(), fitness);

        getNewCurrentNetwork();
    }

    public void resetGeneration() {
        resetGenomes();
        putGenerationIntoQueue();
    }

    public void resetPopulation() {
        p = new Population();
        p.initializePopulation(mandatoryNeurons);
        putGenerationIntoQueue();
    }

    private void resetGenomes() {
        for (Genome g : p.getGenomes()) {
            g.reset();
        }
    }

    private void putGenerationIntoQueue() {
        remainingNetsInGeneration.clear();
        calculatedFitnesses.clear();

        for (Genome g : p.getGenomes()) {
            remainingNetsInGeneration.add(new NeuralNetwork(g));
        }

        getNewCurrentNetwork();

        properlyNotify(Update_Args.NEW_GENERATION);
    }

    private void calculateFinalFitnessValues() {
        double minFitness = Double.POSITIVE_INFINITY;

        for (NeuralNetwork n : calculatedFitnesses.keySet()) {
            double fitness = calculatedFitnesses.get(n);
            if (fitness < minFitness) {
                minFitness = fitness;
            }
        }

        for (NeuralNetwork n : calculatedFitnesses.keySet()) {
            double fitness = calculatedFitnesses.get(n);
            Genome g = n.getGenome();
            double fitnessDistance =  fitness - minFitness;
            p.updateFitness(g, fitnessDistance);
        }
    }

    public NeuralNetwork getCurrent() {
        return current;
    }

    public Population getPopulation() {
        return p;
    }

    private Object readResolve() throws ObjectStreamException {
        this.gameStats = GameStats.getInstance();
        this.remainingNetsInGeneration = new LinkedList<NeuralNetwork>();
        this.calculatedFitnesses = new HashMap<NeuralNetwork, Double>();

        putGenerationIntoQueue();

        initBrickInputsForCurrentNetwork();

        gameStats.addObserver(this);

        return this;
    }

    public static class ObservableArg {
        private Update_Args type;
        private int i;
        private int j;
        private boolean brickState;
        private boolean[][] bricks;

        public ObservableArg(Update_Args type, boolean[][] bricks) {
            this.type = type;
            this.bricks = bricks;
        }

        public ObservableArg(Update_Args type, int i, int j, boolean brickState) {
            this.type = type;
            this.i = i;
            this.j = j;
            this.brickState = brickState;
        }

        public ObservableArg(Update_Args type) {
            this.type = type;
        }

        public int getI() {
            return i;
        }

        public boolean isBrickState() {
            return brickState;
        }

        public int getJ() {
            return j;
        }

        public Update_Args getType() {
            return type;
        }

        public boolean[][] getBricks() {
            return bricks;
        }
    }

    private void properlyNotify(Update_Args type) {
        setChanged();
        notifyObservers(type);
    }

    public TreeMap<Integer, HashMap<String, Double>> getHistoryMap() {
        return historyMap;
    }
}
