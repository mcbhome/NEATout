package neat;

import java.io.Serializable;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Connection implements Serializable {
    private static final long serialVersionUID = 1L;

    private Neuron in;
    private Neuron out;
    private double weight;
    private boolean enabled;
    private int innov;

    public Connection(Neuron in, Neuron out, int innov, double weight, Genome g) {
        Neuron genomeIn = g.getNodeById(in.getId());
        Neuron genomeOut = g.getNodeById(out.getId());

        if (genomeIn  == null) {
            genomeIn = g.addFromExistingNode(in);
        }

        if (genomeOut == null) {
            genomeOut = g.addFromExistingNode(out);
        }

        this.in = genomeIn;
        this.out = genomeOut;
        this.innov = innov;
        this.weight = weight;
        enabled = true;
    }

    public Connection(Connection c, Genome g) {
        this(c.getIn(), c.getOut(), c.getInnov(), c.getWeight(), g);
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

    public String toString() {
        return ("From " + in.getId() + " to " + out.getId() + ": " + this.getWeight());
    }
}
