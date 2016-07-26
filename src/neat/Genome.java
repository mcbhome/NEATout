package neat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Genome {
    private ArrayList<Neuron> nodeGenes;
    private BrickInputNeuron[][] brickInputs;
    private Neuron paddleInput;
    private Neuron ballInput;
    private Neuron leftOutput;
    private Neuron rightOutput;
    private ArrayList<Connection> connectionGenes;
    private double fitness;
    private double sharedFitness;
    private int highestInnov;

    public Genome() {
        nodeGenes = new ArrayList<Neuron>();
        connectionGenes = new ArrayList<Connection>();
        brickInputs = new BrickInputNeuron[11][7];
        highestInnov = 0;
        fitness = 0;
        sharedFitness = 0;
    }

    public Genome(Genome parent) {
        brickInputs = new BrickInputNeuron[11][7];

        this.nodeGenes = new ArrayList<Neuron>();

        for (Neuron n : parent.getNodeGenes()) {
            this.addNode(new Neuron(n));
        }

        this.connectionGenes = new ArrayList<Connection>();

        for (Connection c : parent.connectionGenes) {
            this.connectionGenes.add(new Connection(c));
        }

        this.highestInnov = parent.highestInnov;
    }


    private void sortConnectionGenes() {
        Collections.sort(connectionGenes, new Comparator<Connection>() {
            @Override
            public int compare(Connection o1, Connection o2) {
                return o1.getInnov() - o2.getInnov();
            }
        });
    }

    public double getFitness() {
        return fitness;
    }

    public ArrayList<Connection> getConnectionGenes() {
        return connectionGenes;
    }

    public double hasConnection(Neuron a, Neuron b) {
        for (Connection c : connectionGenes) {
            if (c.getIn().equals(a) && c.getOut().equals(b))
                return c.getWeight();
        }

        return 0;
    }

    public void addConnectionGene(Connection c) {
        connectionGenes.add(new Connection(c));
        addNodeFromConnectionGeneIfDoesntExist(c);
        refreshConnectionStats(c.getInnov());
    }

    private void addNodeFromConnectionGeneIfDoesntExist(Connection c) {
            Neuron in = c.getIn();
            Neuron out = c.getOut();

            if (!nodeGenes.contains(in)) {
                this.addNode(new Neuron(in));
            }
            if (!nodeGenes.contains(out)) {
                this.addNode(new Neuron(out));
            }
    }

    public void addConnection(Neuron in, Neuron out, int innov, double weight) {
        connectionGenes.add(new Connection(in, out, innov, weight));
        refreshConnectionStats(innov);
    }

    public void refreshConnectionStats(int innov) {
        sortConnectionGenes();

        if (innov > highestInnov)
            highestInnov = innov;
    }

    public int getHighestInnov() {
        return highestInnov;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getSharedFitness() {
        return sharedFitness;
    }

    // TODO: Node genes aswell?
    public int getGenomeCount() {
        return connectionGenes.size();
    }

    public boolean isExcess(Connection c) {
        return c.getInnov() > highestInnov;
    }

    public boolean isDisjoint(Connection c) {
        int innov = c.getInnov();

        for (int i = 0; i < connectionGenes.size(); i++) {
            int curInnov = connectionGenes.get(i).getInnov();

            if (curInnov == innov)
                return false;
            // We went past c's innov number
            else if (curInnov > innov)
                return true;
        }

        // Else: It has to be excess! Return for safety
        return false;
    }

    public void setSharedFitness(double sharedFitness) {
        this.sharedFitness = sharedFitness;
    }

    public ArrayList<Connection> getEnabledConnectionGenes() {
        ArrayList<Connection> res = new ArrayList<Connection>();

        for (Connection c : connectionGenes)
            if (c.isEnabled())
                res.add(c);

        return res;
    }

    public ArrayList<Neuron> getNodeGenes() {
        return this.nodeGenes;
    }

    public ArrayList<Neuron> getNodesMinDepth(int minDepth) {
        ArrayList<Neuron> res = new ArrayList<Neuron>();

        for (Neuron n : getNodeGenes()) {
            if (n.getDepth() >= minDepth)
                res.add(n);
        }

        return res;
    }

    public void addNode(Neuron n) {
        this.nodeGenes.add(n);

        if (!(n.getType() == Neuron.Neuron_Type.HIDDEN)) {
            if (n.getType() == Neuron.Neuron_Type.SENSOR_BRICK) {
                BrickInputNeuron brick = (BrickInputNeuron) n;
                this.brickInputs[brick.getI()][brick.getJ()] = brick;
            } else if (n.getType() == Neuron.Neuron_Type.SENSOR_PADDLE) {
                this.paddleInput = n;
            } else if (n.getType() == Neuron.Neuron_Type.SENSOR_BALL) {
                this.ballInput = n;
            } else if (n.getType() == Neuron.Neuron_Type.OUTPUT_LEFT) {
                this.leftOutput = n;
            } else if (n.getType() == Neuron.Neuron_Type.OUTPUT_RIGHT) {
                this.rightOutput = n;
            }
        }
    }

    public void addAllNodes(Collection<? extends Neuron> neurons) {
        for (Neuron n : neurons) {
            this.nodeGenes.add(new Neuron(n));
        }
    }

    public BrickInputNeuron[][] getBrickInputNeurons() {
        return brickInputs;
    }

    public Neuron getPaddleInputNeurons() {
        return paddleInput;
    }

    public Neuron getBallInputNeurons() {
        return ballInput;
    }

    public Neuron getLeftOutputNeurons() {
        return leftOutput;
    }

    public Neuron getRightOutputNeurons() {
        return rightOutput;
    }
}
