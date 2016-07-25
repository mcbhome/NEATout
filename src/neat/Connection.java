package neat;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Connection {
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
}
