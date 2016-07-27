package Game.neat;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class NeuralNetwork {
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
        calculateDepths();
    }

    private void initInputDepths() {
        for (int i = 0; i < brickInputNeurons.length; i++) {
            for (int j = 0; j < brickInputNeurons[i].length; j++)
                brickInputNeurons[i][j].setDepth(0);
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

    public void calculateDepths() {
        LinkedList<Neuron> curQueue = new LinkedList<Neuron>();
        LinkedList<Neuron> nextQueue = new LinkedList<Neuron>();

        for (int i = 0; i < brickInputNeurons.length; i++) {
            for (int j = 0; j < brickInputNeurons[i].length; j++)
                curQueue.add(brickInputNeurons[i][j]);
        }

        curQueue.add(paddleInputNeuron);
        curQueue.add(ballInputNeuron);

        int curDepth = 1;

        while (!curQueue.isEmpty()) {
            while (!curQueue.isEmpty()) {
                ArrayList<Connection> successors = curQueue.poll().getSuccessors();

                for (Connection c : successors) {
                    Neuron cur = c.getOut();
                    if (cur.getDepth() < curDepth) {
                        nextQueue.add(cur);
                        cur.setDepth(curDepth);
                    }
                }
            }
            curDepth++;
            curQueue = nextQueue;
            nextQueue = new LinkedList<Neuron>();
        }

        if (rightOutputNeuron.getDepth() > leftOutputNeuron.getDepth()) {
            leftOutputNeuron.setDepth(rightOutputNeuron.getDepth());
        } else if (leftOutputNeuron.getDepth() > rightOutputNeuron.getDepth()) {
            rightOutputNeuron.setDepth(leftOutputNeuron.getDepth());
        } else if (leftOutputNeuron.getDepth() == 0 && rightOutputNeuron.getDepth() == 0) {
            leftOutputNeuron.setDepth(Integer.MAX_VALUE);
            rightOutputNeuron.setDepth(Integer.MAX_VALUE);
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
