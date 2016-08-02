package Game.neat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class NeuralNetwork implements Serializable {
    private Neuron[][] brickInputNeurons;
    private Neuron paddleInputNeuron;
    private Neuron ballInputNeuron;
    private Neuron rightOutputNeuron;
    private Neuron leftOutputNeuron;
    private Genome genome;

    public NeuralNetwork(Genome g) {
        this.genome = g;

        this.brickInputNeurons = g.getBrickInputNeurons();
        this.paddleInputNeuron = g.getPaddleInputNeuron();
        this.ballInputNeuron = g.getBallInputNeuron();
        this.rightOutputNeuron = g.getRightOutputNeuron();
        this.leftOutputNeuron = g.getLeftOutputNeuron();

        initInputDepths();
    }

    private void initInputDepths() {
        for (int i = 0; i < brickInputNeurons.length; i++) {
            for (int j = 0; j < brickInputNeurons[i].length; j++) {
                if (brickInputNeurons[i][j] == null)
                    System.out.println();

                brickInputNeurons[i][j].setDepth(0);
            }
        }

        paddleInputNeuron.setDepth(0);
        ballInputNeuron.setDepth(0);
    }

    public void setBrickInput(int i, int j, boolean state) {
        brickInputNeurons[i][j].setInput(state ? 1.0 : 0.0);
    }

    public void setPaddlePosition(int x) {
        paddleInputNeuron.setInput(x);
    }

    public void setBallPosition(int x) {
        ballInputNeuron.setInput(x);
    }

    public double getRightOutput() {
        return rightOutputNeuron.getOutput();
    }

    public double getLeftOutput() {
        return leftOutputNeuron.getOutput();
    }

    public void propagateInputs() {
        LinkedList<Neuron> neuronQueue = new LinkedList<Neuron>();

        for (int i = 0; i < brickInputNeurons.length; i++) {
            for (int j = 0; j < brickInputNeurons[i].length; j++)
                neuronQueue.add(brickInputNeurons[i][j]);
        }

        neuronQueue.add(paddleInputNeuron);
        neuronQueue.add(ballInputNeuron);

        while (!neuronQueue.isEmpty()) {
            Neuron cur = neuronQueue.poll();
            cur.calculateOutput();
            cur.propagateOutputToSuccessors();
            for (Connection c : cur.getSuccessors()) {
                if (!neuronQueue.contains(c.getOut())) {
                    neuronQueue.add(c.getOut());
                }
            }
        }
    }

    public void reset() {
        for (Neuron n : genome.getNodeGenes()) {
            n.reset();
        }
    }

    public Genome getGenome() {
        return genome;
    }
}
