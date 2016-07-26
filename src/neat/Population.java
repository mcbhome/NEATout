package neat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Population {
    private static int gen_count = 0;
    private static int conn_count = 0;

    private static final double COMPATIBILITY_THRESHOLD = 5;
    private static final double C1 = 1;
    private static final double C2 = 1;
    private static final double C3 = 1;

    private static final double MUTATION_WEIGHT_BOUND = 5;
    private static final double PERTURBATION_FACTOR_BOUND = 2;

    private static final double NODE_MUTATE_CHANCE = 0.03;
    private static final double EDGE_MUTATE_CHANCE = 0.05;
    private static final double EDGE_PERTURB_CHANCE = 0.9;

    private static final double MATE_CHANCE = 0.7;
    private static final double REACTIVATE_CONNECTION_CHANCE = 0.25;

    private int gen_id;
    private ArrayList<Genome> genomes;
    private ArrayList<Species> species;

    public Population() {
        gen_id = gen_count++;
        genomes = new ArrayList<Genome>();
        species = new ArrayList<Species>();
    }

    public Population(Population p) {
        // TODO: Create offspring, recombine, natural selection

        for (Genome g : genomes) {
            sortIntoSpecies(g);
        }
    }

    public void newGeneration() {

    }

    public void sortIntoSpecies(Genome g) {
        boolean done = false;

        while (!done) {
            for (Species s : species) {
                Genome r = s.getRepresentative();

                if (calculateCompatibilityDistance(g, r) < COMPATIBILITY_THRESHOLD) {
                    s.addGenome(g);
                    done = true;
                }
            }

            species.add(new Species(g));
        }
    }

    private double calculateCompatibilityDistance(Genome a, Genome b) {
        int n = getHigherGenomeCount(a, b);
        double e = calculateExcessGenes(a, b);
        double d = calculateDisjointGenes(a, b);
        double w = calculateAvgWeightDifference(a, b);

        return (C1 * e) / n + (C2 * d) / n + C3 * w;
    }

    private double calculateExcessGenes(Genome a, Genome b) {
        return Math.abs(a.getHighestInnov() - b.getHighestInnov());
    }

    private double calculateDisjointGenes(Genome a, Genome b) {
        HashMap<Integer, Boolean> checkedGenes = new HashMap<Integer, Boolean>();
        int numDisjointGenes = 0;
        int aInnov = a.getHighestInnov();
        int bInnov = b.getHighestInnov();

        for (Connection c : a.getConnectionGenes()) {
            if (c.getInnov() < bInnov) {
                if (b.isDisjoint(c)) {
                    checkedGenes.put(c.getInnov(), true);
                    numDisjointGenes++;
                }
            }
        }

        for (Connection c : b.getConnectionGenes()) {
            if (c.getInnov() < aInnov) {
                if (a.isDisjoint(c)) {
                    checkedGenes.put(c.getInnov(), true);
                    numDisjointGenes++;
                }
            }
        }

        return numDisjointGenes;
    }

    private double calculateAvgWeightDifference(Genome a, Genome b) {
        int numSharedWeights = 0;
        double sharedSum = 0;

        for (Connection c : a.getConnectionGenes()) {
            double d = b.hasConnection(c.getIn(), c.getOut());

            if (d != 0) {
                numSharedWeights++;
                sharedSum += d;
            }
        }

        if (numSharedWeights > 0)
            return sharedSum / numSharedWeights;
        else
            return 0;
    }

    private int getHigherGenomeCount(Genome a, Genome b) {
        return Math.max(a.getGenomeCount(), b.getGenomeCount());
    }

    private void mate(Genome a, Genome b) {
        Genome child;
        Genome fitter;
        Genome lessFit;

        boolean equallyfit = a.getFitness() == b.getFitness();

        if (a.equals(b))
            child = new Genome(a);
        else {
            child = new Genome();

            if (equallyfit) {
                int indexA = 0;
                int indexB = 0;

                ArrayList<Connection> connectionsA = a.getConnectionGenes();
                ArrayList<Connection> connectionsB = b.getConnectionGenes();

                while (indexA < connectionsA.size() || indexB < connectionsB.size()) {
                    Connection first = connectionsA.get(indexA);
                    Connection second = connectionsB.get(indexB);
                    int firstInnov = first.getInnov();
                    int secondInnov = second.getInnov();
                    Double rnd = Math.random();

                    if (first.getInnov() == second.getInnov()) {
                        Connection c;
                        if (rnd < 0.5) {
                            c = new Connection(first);
                            child.addConnectionGene(c);
                        } else {
                            c = new Connection(second);
                            child.addConnectionGene(c);
                        }

                        if (!first.isEnabled() && !second.isEnabled() && Math.random() < REACTIVATE_CONNECTION_CHANCE) {
                            c.setEnabled(true);
                        }

                        indexA++;
                        indexB++;
                    } else {
                        if (firstInnov < secondInnov) {
                            if (rnd < 0.5) {
                                child.addConnectionGene(new Connection(first));
                            }
                            indexA++;
                        } else {
                            if (rnd < 0.5) {
                                child.addConnectionGene(new Connection(second));
                            }
                            indexB++;
                        }
                    }
                }
            } else {
                if (a.getFitness() > b.getFitness()) {
                    fitter = a;
                    lessFit = b;
                } else {
                    fitter = b;
                    lessFit = a;
                }

                int indexFitter = 0;
                int indexLesser = 0;
                ArrayList<Connection> fitterConnectionGenes = fitter.getConnectionGenes();
                ArrayList<Connection> lesserConnectionGenes = lessFit.getConnectionGenes();

                while (indexFitter < fitterConnectionGenes.size()) {
                    Connection fitterConnection = fitterConnectionGenes.get(indexFitter);
                    Connection lesserConnection = lesserConnectionGenes.get(indexLesser);

                    if (fitterConnection.getInnov() == lesserConnection.getInnov()) {
                        if (Math.random() < 0.5) {
                            child.addConnectionGene(new Connection(fitterConnection));
                        } else {
                            child.addConnectionGene(new Connection(lesserConnection));
                        }
                        indexFitter++;
                        indexLesser++;

                    } else if (lessFit.isDisjoint(fitterConnection) || lessFit.isExcess(fitterConnection)) {
                        child.addConnectionGene(new Connection(fitterConnection));
                        indexFitter++;
                    } else if (fitter.isDisjoint(lesserConnection) || fitter.isExcess(lesserConnection)) {
                        indexLesser++;
                    }
                }
            }
        }

        genomes.add(child);
        mutate(child);
    }



    private void mutate(Genome g) {
        double rnd = Math.random();

        if (rnd < NODE_MUTATE_CHANCE) {
            nodeMutation(g);
        }

        rnd = Math.random();

        if (rnd < EDGE_MUTATE_CHANCE) {
            edgeMutation(g);
        }

        rnd = Math.random();

        if (rnd < EDGE_PERTURB_CHANCE) {
            perturbEdges(g);
        }
    }

    private void nodeMutation(Genome g) {
        ArrayList<Connection> connections = new ArrayList<Connection>(g.getEnabledConnectionGenes());

        Connection c = connections.get((int) (Math.random() * connections.size()));

        Neuron n = new Neuron(Neuron.Neuron_Type.HIDDEN);
        Neuron in = c.getIn();
        Neuron out = c.getOut();

        c.setEnabled(false);

        in.addSuccessor(new Connection(in, n, getNextInnov(), 1));
        n.addSuccessor(new Connection(n, out, getNextInnov(), c.getWeight()));
    }

    private void edgeMutation(Genome g) {
        ArrayList<Neuron> validSourceNodes = g.getNodeGenes();
        boolean addedConnection = false;
        Neuron in;
        Neuron out;

        do {
            in = validSourceNodes.get((int) (Math.random() * validSourceNodes.size()));
        } while (in == null || in.getType() == Neuron.Neuron_Type.OUTPUT);

        ArrayList<Neuron> validDestNodes = g.getNodesMinDepth(in.getDepth() + 1);

        while (!validDestNodes.isEmpty() && addedConnection) {
            out = validDestNodes.get((int) (Math.random() * validDestNodes.size()));
            if (!in.hasSuccessor(out)) {
                in.addSuccessor(new Connection(in, out, getNextInnov(), Math.random() * MUTATION_WEIGHT_BOUND - MUTATION_WEIGHT_BOUND));
                addedConnection = true;
            }

            validDestNodes.remove(out);
        }
    }

    private void perturbEdges(Genome g) {
        for (Connection c : g.getConnectionGenes()) {
            c.setWeight(c.getWeight() + c.getWeight() * (Math.random() * PERTURBATION_FACTOR_BOUND - PERTURBATION_FACTOR_BOUND));
        }
    }

    // TODO: Check if exists
    private int getNextInnov() {
        return conn_count++;
    }
}
