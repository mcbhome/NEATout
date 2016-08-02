package Game.neat;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by qfi_2 on 27.07.2016.
 */
public class BrickInputNeuron extends Neuron implements Serializable {
    private int i;
    private int j;

    public BrickInputNeuron(int i, int j) {
        super(Neuron_Type.SENSOR_BRICK);
        this.i = i;
        this. j = j;
    }

    public BrickInputNeuron(BrickInputNeuron n) {
        super(n);
        this.i = n.i;
        this.j = n.j;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    private Object readResolve() throws ObjectStreamException {
        this.input = 0;
        this.output = 0;

        return this;
    }
}
