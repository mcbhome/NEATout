package neat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Population {
    private static int gen_count = 0;
    private static final double COMPATIBILITY_THRESHOLD = 5;
    private static final double C1 = 1;
    private static final double C2 = 1;
    private static final double C3 = 1;

    private int gen_id;
    private ArrayList<Genome> genomes;
    private ArrayList<Species> species;

    public Population() {
        gen_id = gen_count++;
        genomes = new ArrayList<Genome>();
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
}
