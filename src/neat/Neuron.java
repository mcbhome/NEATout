package neat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qfi_2 on 25.07.2016.
 */
class Neuron {

    public enum Neuron_Type { SENSOR, HIDDEN, OUTPUT }
    private final int id;
    private double output;
    private double input;
    private static final double SIGMOID_STEEPNESS = 2.0;
    private ArrayList<Connection> successors;
    private Neuron_Type type;

    private int depth;

    public Neuron (Neuron_Type type, int id) {
        this.id = id;
        this.type = type;
        successors = new ArrayList<Connection>();
    }

    public double calculateOutput() {
        return 1.0 / (1 - Math.exp(-1 * SIGMOID_STEEPNESS * input));
    }

    public double getOutput() {
        return output;
    }

    private void propagateOutputToSuccessors() {
        for (Connection c : successors) {
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
}
