package Game.neat;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Population implements Serializable {
    private int gen_count = 0;
    private int conn_count = 0;

    private static final int POPULATION_SIZE = 6;
    private static final int POPULATION_DENOMINATOR = 3;

    private static final int MAXIMUM_STALENESS = 20;

    private static final double COMPATIBILITY_THRESHOLD = 3;
    private static final double C1 = 1;
    private static final double C2 = 1;
    private static final double C3 = 1;

    private static final double MUTATION_WEIGHT_BOUND = 5;
    private static final double PERTURBATION_FACTOR_BOUND = 2;

    private static final double NODE_MUTATE_CHANCE = 0.03;
    private static final double EDGE_MUTATE_CHANCE = 0.05;
    private static final double EDGE_PERTURB_CHANCE = 0.9;

    private static final double MATE_CHANCE = 0.75;
    private static final double REACTIVATE_CONNECTION_CHANCE = 0.25;
    private static final double INTERSPECIES_MATING_CHANCE = 0.001;

    private static final boolean MUTATE_FIRST_GEN = true;

    private transient double top_fitness = 0;
    private transient int staleness = 0;

    private int gen_id;
    private ArrayList<Genome> genomes;
    private ArrayList<Species> species;

    private transient Random random;

    private HashMap<Integer, HashMap<Integer, Integer>> innovationNumbers;

    public Population() {
        gen_id = gen_count++;
        genomes = new ArrayList<Genome>();
        species = new ArrayList<Species>();
        innovationNumbers = new HashMap<Integer, HashMap<Integer, Integer>>();
        this.random = new Random();

        Neuron.resetNeuronCount();
        Genome.resetGenomeCount();
        Species.resetSpeciesCount();
    }

    public void initializePopulation(ArrayList<Neuron> inputOutputNeurons) {
        while (genomes.size() < POPULATION_SIZE) {
            Genome g = new Genome(inputOutputNeurons);

            if (MUTATE_FIRST_GEN) {
                mutate(g);
            }

            genomes.add(g);
        }

        for (Genome g : genomes) {
            sortIntoSpecies(g);
        }
    }

    public void newGeneration() {
        gen_id = gen_count++;
        cullSpecies();

        ArrayList<Genome> offspring = new ArrayList<Genome>();
        int genomeSize = genomes.size();

        if (staleness >= MAXIMUM_STALENESS) {
            stalenessCleanUp();
        }

        int offspringSize = POPULATION_SIZE - genomeSize;

        double totalAverageFitness = 0;

        for (Species s : species) {
            totalAverageFitness += s.getAverageFitness();
        }

        for (Species s : species) {
            int numOffspring;

            if (totalAverageFitness == 0) {
                numOffspring = (int) ((double) offspringSize / species.size() + 0.5);
            } else {
                numOffspring = (int) ((s.getAverageFitness() / totalAverageFitness) * offspringSize + 0.5);
            }

            for (int i = 0; i < numOffspring; i++) {
                offspring.add(produceOffspring(s));
            }
        }

        for (Species s : species) {
            Genome rnd = s.getGenomes().get(random.nextInt(s.getGenomes().size()));
            s.setRepresentativeAndResetGenomes(rnd);
        }

        genomes.addAll(offspring);


        for (Genome g : genomes) {
            sortIntoSpecies(g);
        }

        for (Genome g: genomes) {
            g.setFitness(0);
            g.setSharedFitness(0);
        }

        this.top_fitness = 0;
    }

    private void cullSpecies() {
        for (Species s : species) {
            int numGenomesToRemove = s.getGenomes().size() / POPULATION_DENOMINATOR;
            s.sortBySharedFitness();
            Genome toRemove;

            for (int i = 0; i < numGenomesToRemove; i++) {
                if (s.getGenomes().size() > 1) {
                    toRemove = s.getGenomes().get(0);
                    s.getGenomes().remove(0);
                    genomes.remove(toRemove);
                }
            }
        }
    }

    // Remove all species but the top two
    private void stalenessCleanUp() {
        Collections.sort(species, new Comparator<Species>() {
            @Override
            public int compare(Species o1, Species o2) {
                double f1 = o1.getAverageFitness();
                double f2 = o2.getAverageFitness();

                if (f1 < f2)
                    return -1;
                if (f2 > f1)
                    return 1;
                else
                    return 0;
            }
        });

        while (species.size() > 2) {
            Species toRemove = species.get(0);
            genomes.removeAll(toRemove.getGenomes());
            species.remove(0);
        }
    }

    public void sortIntoSpecies(Genome g) {
        boolean foundSpecies = false;

        for (Species s : species) {
            Genome r = s.getRepresentative();
            double dist = calculateCompatibilityDistance(g, r);

            if (dist < COMPATIBILITY_THRESHOLD) {
                s.addGenome(g);
                foundSpecies = true;
            }
        }

        if (!foundSpecies)
            species.add(new Species(g));
    }

    public void updateFitness(Genome g, double fitness) {
        g.setFitness(fitness);
        Species s = null;

        for (Species sp : species) {
            if (sp.hasGenome(g)) {
                s = sp;
            }
        }

        if (s == null)
            System.out.println();
        g.setSharedFitness(fitness / s.getGenomes().size());

        if (fitness > top_fitness) {
            top_fitness = fitness;
            staleness = 0;
        }
    }

    private double calculateCompatibilityDistance(Genome a, Genome b) {
        int n = getHigherGenomeCount(a, b);

        if (n == 0)
            return 0;

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
        return Math.max(a.getGenomeCountWithoutBrickNeurons(), b.getGenomeCountWithoutBrickNeurons());
    }

    private Genome produceOffspring(Species s) {
        double rnd = Math.random();
        ArrayList<Genome> genomes = s.getGenomes();

        if (rnd < MATE_CHANCE) {
            Genome child;
            Genome parentA = genomes.get(random.nextInt(genomes.size()));
            Genome parentB;

            rnd = Math.random();

            if (rnd < INTERSPECIES_MATING_CHANCE) {
                Species s2 = species.get(random.nextInt(species.size()));
                ArrayList<Genome> genomes2 = s2.getGenomes();

                parentB = genomes2.get(random.nextInt(genomes2.size()));
            } else {
                parentB = genomes.get((random.nextInt(genomes.size())));
            }

            child = mate(parentA, parentB);

            mutate(child);

            return child;
        } else {
            Genome g = genomes.get(random.nextInt(genomes.size()));
            Genome offspring = new Genome(g);
            mutate(offspring);
            return offspring;
        }
    }

    private Genome mate(Genome a, Genome b) {
        Genome child;
        Genome fitter;
        Genome lessFit;

        boolean equallyfit = a.getFitness() == b.getFitness();

        if (a.equals(b))
            child = new Genome(a);
        else {
            child = new Genome(a.getMandatoryNodes());

            if (equallyfit) {
                int indexA = 0;
                int indexB = 0;

                ArrayList<Connection> connectionsA = a.getConnectionGenes();
                ArrayList<Connection> connectionsB = b.getConnectionGenes();

                while (indexA < connectionsA.size() || indexB < connectionsB.size()) {
                    Connection first = null;
                    int firstInnov = -1;

                    if (indexA < connectionsA.size()) {
                        first = connectionsA.get(indexA);
                        firstInnov = first.getInnov();
                    }

                    Connection second = null;
                    int secondInnov = -1;

                    if (indexB < connectionsB.size()) {
                        second = connectionsB.get(indexB);
                        secondInnov = second.getInnov();
                    }

                    Double rnd = Math.random();

                    if (first != null && second != null && first.getInnov() == second.getInnov()) {
                        Connection c;
                        if (rnd < 0.5) {
                            c = child.addFromExistingConnection(first);
                        } else {
                            c = child.addFromExistingConnection(second);
                        }

                        if (!first.isEnabled() && !second.isEnabled() && Math.random() < REACTIVATE_CONNECTION_CHANCE) {
                            c.setEnabled(true);
                        }

                        indexA++;
                        indexB++;
                    } else {
                        if (firstInnov == -1) {
                            child.addFromExistingConnection(second);
                            indexB++;
                        } else if (secondInnov == -1) {
                            child.addFromExistingConnection(first);
                            indexA++;
                        } else {
                            if (firstInnov < secondInnov) {
                                if (rnd < 0.5) {
                                    child.addFromExistingConnection(first);
                                }
                                indexA++;
                            } else {
                                if (rnd < 0.5) {
                                    child.addFromExistingConnection(second);
                                }
                                indexB++;
                            }
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
                    Connection lesserConnection = null;

                    if (indexLesser < lesserConnectionGenes.size()) {
                        lesserConnection = lesserConnectionGenes.get(indexLesser);
                    }

                    if (lesserConnection == null) {
                        child.addFromExistingConnection(fitterConnection);
                        indexFitter++;
                    } else {
                        if (fitterConnection.getInnov() == lesserConnection.getInnov()) {
                            if (Math.random() < 0.5) {
                                child.addFromExistingConnection(fitterConnection);
                            } else {
                                child.addFromExistingConnection(lesserConnection);
                            }
                            indexFitter++;
                            indexLesser++;

                        } else if (lessFit.isDisjoint(fitterConnection) || lessFit.isExcess(fitterConnection)) {
                            child.addFromExistingConnection(fitterConnection);
                            indexFitter++;
                        } else if (fitter.isDisjoint(lesserConnection) || fitter.isExcess(lesserConnection)) {
                            indexLesser++;
                        }
                    }
                }
            }
        }

        return child;
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
            //perturbEdges(g);
        }
    }

    private void nodeMutation(Genome g) {
        ArrayList<Connection> connections = new ArrayList<Connection>(g.getEnabledConnectionGenes());

        if (!connections.isEmpty()) {
            Connection c = connections.get(random.nextInt(connections.size()));

            Neuron n = new Neuron(Neuron.Neuron_Type.HIDDEN);
            Neuron in = c.getIn();
            Neuron out = c.getOut();

            c.setEnabled(false);

            Connection c1 = new Connection(in, n, getNextInnov(in.getId(), out.getId()), 1, g);
            Connection c2 = new Connection(n, out, getNextInnov(in.getId(), out.getId()), c.getWeight(), g);

            g.addFromExistingConnection(c1);
            g.addFromExistingConnection(c2);
        }
    }

    private void edgeMutation(Genome g) {
        ArrayList<Neuron> validSourceNodes = g.getNodeGenes();
        boolean addedConnection = false;
        Neuron in;
        Neuron out;

        do {
            in = validSourceNodes.get(random.nextInt(validSourceNodes.size()));
        } while (in == null || in.isOutputNeuron());

        ArrayList<Neuron> validDestNodes = g.getNodesMinDepth(in.getDepth() + 1);

        while (!validDestNodes.isEmpty() && !addedConnection) {
            out = validDestNodes.get(random.nextInt(validDestNodes.size()));
            if (!in.hasSuccessor(out)) {
                double connectionWeight = Math.random() * MUTATION_WEIGHT_BOUND - MUTATION_WEIGHT_BOUND;
                Connection c = new Connection(in, out, getNextInnov(in.getId(), out.getId()), connectionWeight, g);
                g.addFromExistingConnection(c);
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

    public int getGenerationId() {
        return gen_id;
    }

    public ArrayList<Genome> getGenomes() {
        return this.genomes;
    }

    public Genome getGenomeById(int id) {
        for (Genome g : genomes) {
            if (g.getId() == id) {
                return g;
            }
        }

        return null;
    }

    public double getTopFitness() {
        return top_fitness;
    }

    public ArrayList<Species> getSpecies() {
        return species;
    }

    private int getNextInnov(int inId, int outId) {
        HashMap<Integer, Integer> innovNumbersForInId = innovationNumbers.get(inId);
        int resultInnov = -1;

        if (innovNumbersForInId != null) {
            for (Integer out : innovNumbersForInId.keySet()) {
                if (out == outId)
                    return innovNumbersForInId.get(out);
            }

            resultInnov = conn_count++;
            innovNumbersForInId.put(outId, resultInnov);
        } else {
            innovationNumbers.put(inId, new HashMap<Integer, Integer>());
            resultInnov = conn_count++;
            innovationNumbers.get(inId).put(outId, resultInnov);
        }

        return resultInnov;
    }

    public Object readResolve() throws ObjectStreamException {
        this.staleness = 0;
        this.top_fitness = 0;
        this.random = new Random();

        if (this.gen_id >= gen_count) {
            gen_count = gen_id + 1;
        }

        for (Genome g : genomes) {
            for (Connection c : g.getConnectionGenes()) {
                if (c.getInnov() >= conn_count) {
                    conn_count = c.getInnov() + 1;
                }
            }
        }

        return this;
    }
}
