package Game.neat;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Neuron implements Serializable {

    public enum Neuron_Type { BIAS, SENSOR_BRICK, SENSOR_PADDLE, SENSOR_BALL, HIDDEN, OUTPUT_LEFT, OUTPUT_RIGHT }

    protected final int id;
    protected static int num_neurons = 0;
    protected transient double output;
    protected transient double input;
    protected static final double SIGMOID_STEEPNESS = 5.0;
    protected ArrayList<Connection> successors;
    protected Neuron_Type type;

    protected int depth;

    public Neuron (Neuron_Type type) {
        this.id = num_neurons++;
        this.type = type;
        successors = new ArrayList<Connection>();

        if (this.type == Neuron_Type.BIAS) {
            this.depth = -1;
        }
    }

    public Neuron(Neuron n) {
        this.id = n.getId();
        this.type = n.getType();
        this.successors = new ArrayList<Connection>();

        if (this.type == Neuron_Type.BIAS) {
            this.depth = -1;
        }
    }

    public void setInput(double in) {
        if (this.isInputNeuron())
            this.input = in;
    }

    public boolean isInputNeuron() {
        return this.type == Neuron_Type.SENSOR_BRICK || this.type == Neuron_Type.SENSOR_PADDLE || this.type == Neuron_Type.SENSOR_BALL;
    }

    public boolean isOutputNeuron() {
        return this.type == Neuron_Type.OUTPUT_LEFT || this.type == Neuron_Type.OUTPUT_RIGHT;
    }

    public void calculateOutput() {
        if (this.isInputNeuron())
            output = input;
        else if(this.getType() == Neuron_Type.BIAS)
            output = 1;
        else
            output = calculateSigmoid(input);
    }

    public double calculateSigmoid(double x) {
        return (1.0 / (1 + Math.exp(-1 * SIGMOID_STEEPNESS * x))) - 0.5;
    }

    public double getOutput() {
        return (this.type == Neuron_Type.BIAS) ? 1 : output;
    }

    public void propagateOutputToSuccessors() {
        for (Connection c : successors) {
            if (c.isEnabled())
                c.getOut().addToInput(this.output * c.getWeight());
        }
    }

    private void addToInput(double d) {
        input += d;
    }

    public void setDepth(int i) {
        depth = i;
    }

    public int getDepth() {
        return depth;
    }

    public Neuron_Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public boolean hasSuccessor(Neuron n) {
        for (Connection c : successors) {
            if (c.getOut().equals(n))
                return true;
        }

        return false;
    }

    public void addSuccessor(Connection c) {
        for (Connection s : successors) {
            if (s.getInnov() == c.getInnov())
                return;
        }

        successors.add(c);
    }

    public ArrayList<Connection> getSuccessors() {
        return this.successors;
    }

    public void reset() {
        if (!this.isInputNeuron()) {
            this.input = 0;
        }

        this.output = 0;
    }

    public double getInput() {
        return input;
    }

    public String toString() {
        return "ID: " + this.getId() + ", Input: " + this.input + ", Output: " + this.output;
    }

    private Object readResolve() throws ObjectStreamException {
        this.input = 0;
        this.output = 0;

        if (this.id >= num_neurons) {
            num_neurons = this.id + 1;
        }

        return this;
    }

    public static void resetNeuronCount() {
        num_neurons = 0;
    }
}
