package neat;

import java.io.Serializable;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Connection implements Serializable {
    private Neuron in;
    private Neuron out;
    private double weight;
    private boolean enabled;
    private int innov;

    public Connection(Neuron in, Neuron out, int innov, double weight) {
        this.in = in;
        this.out = out;
        this.innov = innov;
        this.weight = weight;
        enabled = true;
    }

    public Connection(Connection c) {
        this.in = c.in;
        this.out = c.out;
        this.weight = c.weight;
        this.enabled = c.enabled;
        this.innov = c.innov;
    }

    public Neuron getIn() {
        return in;
    }

    public Neuron getOut() {
        return out;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getInnov() {
        return innov;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean sameStructure(Connection c) {
        return this.in.equals(c.in) && this.out.equals(c.out);
    }
}
