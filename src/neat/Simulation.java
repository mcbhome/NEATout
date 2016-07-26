package neat;
import Game.*;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by qfi_2 on 26.07.2016.
 */
public class Simulation implements Observer {
    public class ObservableArg {
        private boolean brickStateChanged;
        private int i;
        private int j;
        private boolean brickState;
        private int paddleX;
        private int ballX;
        private boolean gameWon;
        private boolean gameLost;
        private int lives;

        public ObservableArg(int i, int j, boolean brickState, int paddleX, int ballX, boolean gameWon, boolean gameLost, int lives) {
            this.brickStateChanged = true;
            this.i = i;
            this.j = j;
            this.brickState = brickState;
            this.paddleX = paddleX;
            this.ballX = ballX;
            this.gameLost = gameLost;
            this.gameWon = gameWon;
            this.lives = lives;
        }

        public ObservableArg(int paddleX, int ballX, boolean gameWon, boolean gameLost, int lives) {
            this.brickStateChanged = false;
            this.paddleX = paddleX;
            this.ballX = ballX;
            this.gameLost = gameLost;
            this.gameWon = gameWon;
            this.lives = lives;
        }

        public boolean isBrickStateChanged() {
            return brickStateChanged;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }

        public boolean isBrickState() {
            return brickState;
        }

        public int getPaddleX() {
            return paddleX;
        }

        public int getBallX() {
            return ballX;
        }

        public boolean isGameWon() {
            return gameWon;
        }

        public boolean isGameLost() {
            return gameLost;
        }

        public int getLives() {
            return lives;
        }
    }


    Population p;
    NeuralNetwork current;
    GameStats gameStats;

    public Simulation(GameStats gameStats) {
        this.gameStats = gameStats;
        p = new Population();

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
    }

    public void simulate() {

    }


    @Override
    public void update(Observable o, Object arg) {
        ObservableArg observableArg = (ObservableArg) arg;

        if ((observableArg.isBrickStateChanged())) {
            current.setBrickInput(observableArg.getI(), observableArg.getJ(), observableArg.isBrickState());
        }

        current.setPaddlePosition(observableArg.getPaddleX());
        current.setBallPosition(observableArg.getBallX());
    }
}
