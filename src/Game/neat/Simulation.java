package Game.neat;
import Game.Breakout.GameStats;
import Game.Breakout.Paddle;

import java.util.*;

/**
 * Created by qfi_2 on 26.07.2016.
 */
public class Simulation extends Observable implements Observer {
    private static final double SCORE_FACTOR = 1.0;
    private static final double SHOTS_FACTOR = 100.0;

    private static final double MAXIMUM_SPEED_FACTOR = 3;

    private Population p;
    private NeuralNetwork current;
    private GameStats gameStats;
    private LinkedList<NeuralNetwork> remainingNetsInGeneration;
    private HashMap<NeuralNetwork, Double> calculatedFitnesses;

    public Simulation(GameStats gameStats) {
        this.gameStats = gameStats;
        gameStats.addObserver(this);
        p = new Population();
        remainingNetsInGeneration = new LinkedList<NeuralNetwork>();
        calculatedFitnesses = new HashMap<NeuralNetwork, Double>();

        boolean[][] bricks = gameStats.getBricks();

        ArrayList<Neuron> inputNeurons = new ArrayList<Neuron>();
        ArrayList<Neuron> outputNeurons = new ArrayList<Neuron>();

        for (int i = 0; i < bricks.length; i++) {
            for (int j = 0; j < bricks[i].length; j++) {
                inputNeurons.add(new BrickInputNeuron(i, j));
            }
        }

        inputNeurons.add(new Neuron(Neuron.Neuron_Type.SENSOR_PADDLE));
        inputNeurons.add(new Neuron(Neuron.Neuron_Type.SENSOR_BALL));

        outputNeurons.add(new Neuron(Neuron.Neuron_Type.OUTPUT_LEFT));
        outputNeurons.add(new Neuron(Neuron.Neuron_Type.OUTPUT_RIGHT));


        p.initializePopulation(inputNeurons, outputNeurons);

        putNewGenerationsIntoQueue();
    }


    @Override
    public void update(Observable o, Object arg) {
        if (!gameStats.isPlayerDead()) {
            if (arg != null) {
                ObservableArg observableArg = (ObservableArg) arg;
                current.setBrickInput(observableArg.getI(), observableArg.getJ(), observableArg.isBrickState());
            }
            current.setPaddlePosition(gameStats.getPaddle().getXLeft());
            current.setBallPosition(gameStats.getBall().getX());

            calculateOutputAndMovePaddle();
        } else {
            finishSimulationForCurrentNetwork();
        }
    }

    public void calculateCurrentFitness() {
        double fitness = gameStats.getScore() * SCORE_FACTOR - gameStats.getShots() * SHOTS_FACTOR;
        calculatedFitnesses.put(current, fitness);
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
        } else {
            gameStats.getPaddle().changeDir(Paddle.Direction.RIGHT, dirRight * MAXIMUM_SPEED_FACTOR);
        }
    }

    private void finishSimulationForCurrentNetwork() {
        calculateCurrentFitness();

        if (!remainingNetsInGeneration.isEmpty()) {
            current = remainingNetsInGeneration.poll();
        } else {
            current = null;
            calculateFinalFitnessValues();
            p.newGeneration();
            putNewGenerationsIntoQueue();
        }
    }

    private void putNewGenerationsIntoQueue() {
        for (Genome g : p.getGenomes()) {
            remainingNetsInGeneration.add(new NeuralNetwork(g));
        }

        current = remainingNetsInGeneration.poll();
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

    public class ObservableArg {
        private int i;
        private int j;
        private boolean brickState;

        public ObservableArg(int i, int j, boolean brickState) {
            this.i = i;
            this.j = j;
            this.brickState = brickState;
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
    }
}
