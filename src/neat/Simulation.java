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
        if (arg != null) {
            ObservableArg observableArg = (ObservableArg) arg;
            current.setBrickInput(observableArg.getI(), observableArg.getJ(), observableArg.isBrickState());
        }

        current.setPaddlePosition(gameStats.getPaddle().getXLeft());
        current.setBallPosition(gameStats.getBall().getX());
    }

    private void finishSimulationForCurrentNetwork() {

    }
}
