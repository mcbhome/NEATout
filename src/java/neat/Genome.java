package neat;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Genome implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int numGenomes = 0;

    // This specifies if the brick array is taken into account
    public final boolean BRICK_INPUTS_ENABLED = false;

    private int id;
    private ArrayList<Neuron> nodeGenes;
    private BrickInputNeuron[][] brickInputs;
    private Neuron paddleInput;
    private Neuron ballInput;
    private Neuron ballSpeedInput;
    private Neuron movOutput;
    private Neuron biasNeuron;
    private ArrayList<Connection> connectionGenes;
    private transient double fitness;
    private transient double sharedFitness;
    private transient boolean fitnessDetermined;
    private int highestInnov;
    private Species species;

    private int layers;

    public Genome() {
        id = ++numGenomes;

        nodeGenes = new ArrayList<Neuron>();
        connectionGenes = new ArrayList<Connection>();
        brickInputs = new BrickInputNeuron[11][7];
        highestInnov = 0;
        fitness = 0;
        sharedFitness = 0;
        fitnessDetermined = false;
    }

    // Reset all neurons and fitness stats
    public void reset() {
        for (Neuron n : nodeGenes) {
            n.reset();
        }
    }

    private Object readResolve() throws ObjectStreamException {
        this.fitness = 0;
        this.sharedFitness = 0;
        this.fitnessDetermined = false;

        if (this.id > numGenomes) {
            numGenomes = this.id;
        }

        return this;
    }

    // Add from existing node and connections
    public Genome(Genome parent) {
        this();

        for (Neuron n : parent.getNodeGenes()) {
            this.addFromExistingNode(n);
        }

        for (Connection c : parent.connectionGenes) {
            this.addFromExistingConnection(c);
        }

        this.highestInnov = parent.highestInnov;

        this.calculateDepths();
    }

    // For initial genome generation
    public Genome(ArrayList<Neuron> inputOutputNeurons) {
        this();

        for (Neuron n : inputOutputNeurons) {
            this.addFromExistingNode(n);
        }

        this.calculateDepths();
    }

    // Automatically creates a copy of the neuron that should be added
    public Neuron addFromExistingNode(Neuron n) {
        Neuron newNode;

        if (n.getType() == Neuron.Neuron_Type.SENSOR_BRICK) {
            newNode = new BrickInputNeuron((BrickInputNeuron) n);
        } else {
            newNode = new Neuron(n);
        }

        this.addNode(newNode);

        return newNode;
    }

    // Automatically creates a copy of the connection that should be added, the connection constructor takes care of adding missing neurons
    public Connection addFromExistingConnection(Connection c) {
        Connection res = new Connection(c, this);
        res.getIn().addSuccessor(res);
        connectionGenes.add(res);
        refreshConnectionStats(c.getInnov());

        return res;
    }

    // Sort connection genes by innovation number
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

    public boolean isFitnessDetermined() {
        return fitnessDetermined;
    }

    public void setFitnessDetermined(boolean fitnessDetermined) {
        this.fitnessDetermined = fitnessDetermined;
    }

    public ArrayList<Connection> getConnectionGenes() {
        return connectionGenes;
    }

    public double hasConnection(Connection conn) {
        for (Connection c : connectionGenes) {
            if (c.getInnov() == conn.getInnov())
                return c.getWeight();
        }

        return 0;
    }


    public Connection addConnection(Neuron in, Neuron out, int innov, double weight) {
        Connection result = new Connection(in, out, innov, weight, this);
        connectionGenes.add(new Connection(in, out, innov, weight, this));
        result.getIn().addSuccessor(result);
        refreshConnectionStats(innov);

        return result;
    }

    public void refreshConnectionStats(int innov) {
        sortConnectionGenes();

        if (innov > highestInnov)
            highestInnov = innov;

        calculateDepths();
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

    public int getGenomeCount() {
        return connectionGenes.size() + nodeGenes.size();
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

        // Else: It has to be excess! Return false for safety
        return false;
    }

    public void setSharedFitness(double sharedFitness) {
        this.sharedFitness = sharedFitness;
    }

    public ArrayList<Connection> getEnabledConnectionGenes() {
        ArrayList<Connection> res = new ArrayList<Connection>();

        for (Connection c : connectionGenes) {
            if (c.isEnabled()) {
                res.add(c);
            }
        }

        return res;
    }

    public ArrayList<Neuron> getNodeGenes() {
        return this.nodeGenes;
    }



    public ArrayList<Neuron> getValidSourceNodeGenes() {
        ArrayList<Neuron> result = new ArrayList<>(this.nodeGenes);
        result.remove(movOutput);

        return result;
    }

    public ArrayList<Neuron> getValidDestNodesMinDepth(int minDepth) {
        ArrayList<Neuron> res = new ArrayList<Neuron>();

        for (Neuron n : getNodeGenes()) {
            if (n.getDepth() >= minDepth && !n.isInputNeuron())
                res.add(n);
        }

        return res;
    }

    public void addNode(Neuron n) {
        if (!(n.getType() == Neuron.Neuron_Type.HIDDEN)) {
            if (n.getType() == Neuron.Neuron_Type.SENSOR_BRICK && BRICK_INPUTS_ENABLED) {
                BrickInputNeuron brick = (BrickInputNeuron) n;
                this.brickInputs[brick.getI()][brick.getJ()] = brick;
                this.nodeGenes.add(n);
            } else if (n.getType() == Neuron.Neuron_Type.BIAS) {
                this.biasNeuron = n;
                this.nodeGenes.add(n);
            } else if (n.getType() == Neuron.Neuron_Type.SENSOR_BALL_SPEED) {
                this.ballSpeedInput = n;
                this.nodeGenes.add(n);
            } else if (n.getType() == Neuron.Neuron_Type.SENSOR_PADDLE) {
                this.paddleInput = n;
                this.nodeGenes.add(n);
            } else if (n.getType() == Neuron.Neuron_Type.SENSOR_BALL) {
                this.ballInput = n;
                this.nodeGenes.add(n);
            } else if (n.getType() == Neuron.Neuron_Type.OUTPUT_MOV) {
                this.movOutput = n;
                this.nodeGenes.add(n);
            }
        } else {
            this.nodeGenes.add(n);
        }
    }

    public void addAllNodes(Collection<? extends Neuron> neurons) {
        for (Neuron n : neurons) {
            if (n.getType() == Neuron.Neuron_Type.SENSOR_BRICK) {
                this.addNode(new BrickInputNeuron((BrickInputNeuron) n));
            } else {
                this.addNode(new Neuron(n));
            }
        }
    }

    public Neuron getNodeById(int id) {
        for (Neuron n : nodeGenes) {
            if (n.getId() == id) {
                return n;
            }
        }

        return null;
    }

    public int getId() {
        return id;
    }

    public BrickInputNeuron[][] getBrickInputNeurons() {
        return brickInputs;
    }

    public Neuron getPaddleInputNeuron() {
        return paddleInput;
    }

    public Neuron getBallInputNeuron() {
        return ballInput;
    }

    public Neuron getMovOutputNeuron() {
        return movOutput;
    }

    public ArrayList<Neuron> getMandatoryNodes() {
        ArrayList<Neuron> res = new ArrayList<Neuron>();

        if (BRICK_INPUTS_ENABLED) {
            for (int i = 0; i < brickInputs.length; i++) {
                for (int j = 0; j < brickInputs[i].length; j++) {
                    res.add(brickInputs[i][j]);
                }
            }
        }

        res.add(paddleInput);
        res.add(ballInput);
        res.add(ballSpeedInput);
        res.add(movOutput);
        res.add(biasNeuron);

        return res;
    }

    public String toString() {
        return "ID: " + this.getId();
    }

    public int getActiveConnectionCount() {
        int res = 0;

        for (Connection c : connectionGenes) {
            if (c.isEnabled()) {
                res++;
            }
        }

        return res;
    }

    public void calculateDepths() {
        LinkedList<Neuron> curQueue = new LinkedList<Neuron>();
        LinkedList<Neuron> nextQueue = new LinkedList<Neuron>();

        if (BRICK_INPUTS_ENABLED) {
            for (int i = 0; i < brickInputs.length; i++) {
                for (int j = 0; j < brickInputs[i].length; j++)
                    curQueue.add(brickInputs[i][j]);
            }
        }

        curQueue.add(paddleInput);
        curQueue.add(ballInput);
        curQueue.add(ballSpeedInput);
        curQueue.add(biasNeuron);

        int curDepth = 0;

        while (!curQueue.isEmpty()) {
            curDepth++;

            while (!curQueue.isEmpty()) {
                Neuron n = curQueue.poll();

                ArrayList<Connection> successors = n.getSuccessors();

                for (Connection c : successors) {
                    Neuron cur = c.getOut();
                    nextQueue.add(cur);
                    if (cur.getDepth() < curDepth || cur.getDepth() == Integer.MAX_VALUE || (cur.getDepth() == 0 && !cur.isInputNeuron())) {
                        cur.setDepth(curDepth);
                    }
                }
            }

            curQueue = nextQueue;
            nextQueue = new LinkedList<Neuron>();
        }

        if (movOutput.getDepth() == 0) {
            movOutput.setDepth(Integer.MAX_VALUE);
        }

        if (movOutput.getDepth() == Integer.MAX_VALUE)
            layers = curDepth + 1;
        else {
            layers = curDepth;
        }
    }

    public int getLayers() {
        return layers;
    }

    public int getGenomeCountWithoutBrickNeurons() {
        int brickInputSize = 0;

        if (BRICK_INPUTS_ENABLED) {
            brickInputSize = brickInputs.length * brickInputs[0].length;
        }
        return nodeGenes.size() - brickInputSize + connectionGenes.size();
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public Neuron getBiasNeuron() {
        return biasNeuron;
    }

    public Neuron getBallSpeedInputNeuron() {
        return ballSpeedInput;
    }

    public static void resetGenomeCount() {
        numGenomes = 0;
    }
}
