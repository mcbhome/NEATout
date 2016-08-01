package Game.neat;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Genome {
    private static int numGenomes = 0;

    private int id;
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

    private int layers;

    public Genome() {
        id = ++numGenomes;

        nodeGenes = new ArrayList<Neuron>();
        connectionGenes = new ArrayList<Connection>();
        brickInputs = new BrickInputNeuron[11][7];
        highestInnov = 0;
        fitness = 0;
        sharedFitness = 0;

    }

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

    public Genome(ArrayList<Neuron> inputOutputNeurons) {
        this();

        for (Neuron n : inputOutputNeurons) {
            this.addFromExistingNode(n);
        }

        this.calculateDepths();
    }

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

    public Connection addFromExistingConnection(Connection c) {
        Connection res = new Connection(c, this);
        res.getIn().addSuccessor(res);
        connectionGenes.add(res);
        refreshConnectionStats(c.getInnov());

        return res;
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

    public Neuron getLeftOutputNeuron() {
        return leftOutput;
    }

    public Neuron getRightOutputNeuron() {
        return rightOutput;
    }

    public ArrayList<Neuron> getMandatoryNodes() {
        ArrayList<Neuron> res = new ArrayList<Neuron>();

        for (int i = 0; i < brickInputs.length; i++) {
            for (int j = 0; j < brickInputs[i].length; j++) {
                res.add(brickInputs[i][j]);
            }
        }

        res.add(paddleInput);
        res.add(ballInput);
        res.add(leftOutput);
        res.add(rightOutput);

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

        for (int i = 0; i < brickInputs.length; i++) {
            for (int j = 0; j < brickInputs[i].length; j++)
                curQueue.add(brickInputs[i][j]);
        }

        curQueue.add(paddleInput);
        curQueue.add(ballInput);

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

        if (rightOutput.getDepth() > leftOutput.getDepth()) {
            leftOutput.setDepth(rightOutput.getDepth());
        } else if (leftOutput.getDepth() > rightOutput.getDepth()) {
            rightOutput.setDepth(leftOutput.getDepth());
        } else if (leftOutput.getDepth() == 0 && rightOutput.getDepth() == 0) {
            leftOutput.setDepth(Integer.MAX_VALUE);
            rightOutput.setDepth(Integer.MAX_VALUE);
        }

        layers = curDepth;
    }

    public int getLayers() {
        return layers;
    }

    public int getGenomeCountWithoutBrickNeurons() {
        return nodeGenes.size() - brickInputs.length * brickInputs[0].length + connectionGenes.size();
    }
}
