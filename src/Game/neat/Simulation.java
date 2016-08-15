package Game.neat;
import Game.Breakout.Ball;
import Game.Breakout.Commons;
import Game.Breakout.GameStats;
import Game.Breakout.Paddle;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by qfi_2 on 26.07.2016.
 */
public class Simulation extends Observable implements Observer, Serializable {
    private static final long serialVersionUID = 1L;

    public enum Update_Args {PLAYER_DIED, BRICK_CHANGE, SCORE_CHANGED, MOVEMENT, MISC, NEW_GAME, BRICKS_RANDOMIZED, NEW_GENERATION, GAME_WON, BALL_HIT}
    public static final String TOP_FITNESS_KEY = "TOP_FITNESS";
    public static final String AVERAGE_FITNESS_KEY = "AVERAGE_FITNESS";

    private static boolean trainingMode = true;

    private static final double SCORE_FACTOR = 0.01;
    private static final double SHOTS_FACTOR = 100.0;
    private static final int SHOTS_BONUS_CEILING = 25;
    private static final int SHOTS_TIMEOUT = 250;

    private Population p;
    private NeuralNetwork current;

    private ArrayList<Neuron> mandatoryNeurons;

    private TreeMap<Integer, HashMap<String, Double>> historyMap;

    private transient GameStats gameStats;
    private transient LinkedList<NeuralNetwork> allNetsInGeneration;
    private transient LinkedList<NeuralNetwork> remainingNetsInGeneration;
    private transient HashMap<NeuralNetwork, Double> calculatedFitnesses;

    public Simulation(GameStats gameStats) {
        this.gameStats = gameStats;
        p = new Population();
        remainingNetsInGeneration = new LinkedList<NeuralNetwork>();
        allNetsInGeneration = new LinkedList<>();
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
        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.SENSOR_BALL_SPEED));

        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.OUTPUT_MOV));

        mandatoryNeurons.add(new Neuron(Neuron.Neuron_Type.BIAS));


        p.initializePopulation(mandatoryNeurons);

        //enterDebugData();

        putGenerationIntoQueue();

        gameStats.addObserver(this);
    }

    private void enterDebugData() {
        for (Genome g : p.getGenomes()) {

            g.addFromExistingConnection(new Connection(g.getPaddleInputNeuron(), g.getMovOutputNeuron(), 1, -1, g));
            g.addFromExistingConnection(new Connection(g.getBallInputNeuron(), g.getMovOutputNeuron(), 2, 1, g));
        }
    }

    public Simulation() {
        super();
    }

    @Override
    public synchronized void update(Observable o, Object arg) {
        ObservableArg observableArg = (ObservableArg) arg;
        Update_Args type = observableArg.getType();

        if (type == Update_Args.PLAYER_DIED) {
            if (gameStats.getLives() == 0) {
                if (trainingMode) {
                    finishSimulationForCurrentNetwork();
                    properlyNotify(Update_Args.NEW_GENERATION);
                }
            }
        } else if (current != null) {
            if (type == Update_Args.BRICK_CHANGE) {
                current.setBrickInput(observableArg.getI(), observableArg.getJ(), observableArg.isBrickState());
            } else if (type == Update_Args.NEW_GAME) {
                initBrickInputsForCurrentNetwork();
            } else if (type == Update_Args.MOVEMENT) {
                current.setPaddlePosition(getNormalizedPaddlePos());
                current.setBallPosition(getNormalizedBallPos());
                current.setBallSpeed(getNormalizedBallSpeed());
                calculateOutputAndMovePaddle();
            } else if (type == Update_Args.BALL_HIT) {
                checkForTimeout();
            } else if(type == Update_Args.GAME_WON) {
                if (trainingMode) {
                    finishSimulationForCurrentNetwork();
                }
                properlyNotify(Update_Args.GAME_WON);
            }
        }
    }

    private double getNormalizedBallSpeed() {
        Ball ball = gameStats.getBall();
        double speed = gameStats.MAX_BALL_SPEED;

        return (ball.getHorizontalSpeed() + speed) / (speed) - 1;
    }

    private double getNormalizedPaddlePos() {
        Paddle paddle = gameStats.getPaddle();
        int width = paddle.getPWidth();
        return (paddle.getXLeft() / (Commons.width - width)) * 2 - 1;
    }

    private double getNormalizedBallPos() {
        Ball ball = gameStats.getBall();
        int radius = ball.getRadius();
        double res = ((ball.getX() - radius) / (Commons.width - 2 * radius)) * 2 - 1;

        return res;
    }

    private void checkForTimeout() {
        if (gameStats.getShots() > SHOTS_TIMEOUT && trainingMode) {
            finishSimulationForCurrentNetwork();
            gameStats.gameInit();
            gameStats.newGame();
        }
    }

    private void getNewCurrentNetwork() {
        current = null;

        while (current == null && !remainingNetsInGeneration.isEmpty()) {
            current = remainingNetsInGeneration.poll();

            if (current != null && current.getGenome().isFitnessDetermined()) {
                current = null;
            }
        }

        if (current == null) {
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
        return gameStats.getShots() == 0 ? gameStats.getScore()  :  gameStats.getScore() / gameStats.getShots() + Math.min(gameStats.getShots(), SHOTS_BONUS_CEILING) * SHOTS_FACTOR;
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

        double dir = current.getMovOutput();

        controlPaddle(dir);

        current.reset();
    }

    public void controlPaddle(double dir) {
        gameStats.getPaddle().changeDir(dir * gameStats.MAX_PADDLE_SPEED);
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
        allNetsInGeneration.clear();
        calculatedFitnesses.clear();

        for (Genome g : p.getGenomes()) {
            remainingNetsInGeneration.add(new NeuralNetwork(g));
        }

        allNetsInGeneration.addAll(remainingNetsInGeneration);

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
        this.allNetsInGeneration = new LinkedList<>();
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

    public void destroy() {
        this.deleteObservers();
        gameStats.deleteObserver(this);
        gameStats.deleteObserver(this);
    }

    public static boolean isTrainingMode() {
        return trainingMode;
    }

    public static void setTrainingMode(boolean trainingMode) {
        Simulation.trainingMode = trainingMode;
    }

    public void setCurrent(NeuralNetwork current) {
        this.current = current;
    }

    public LinkedList<NeuralNetwork> getNetsInCurrentGeneration() {
        return allNetsInGeneration;
    }

    public TreeMap<Integer, HashMap<String, Double>> getHistoryMap() {
        return historyMap;
    }
}
