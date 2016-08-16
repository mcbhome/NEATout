package neat;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Species implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int MAXIMUM_STALENESS = 20;
    private static int species_count = 0;
    private int id;
    private ArrayList<Genome> genomes;
    private Genome representative;
    private double totalSharedFitness;
    private double topFitness;
    private int staleness;

    public Species(Genome representative) {
        genomes = new ArrayList<Genome>();
        this.representative = representative;
        genomes.add(representative);
        this.id = ++species_count;
        this.staleness = 0;
        representative.setSpecies(this);
    }

    public void calculateSharedFitness() {
        for (Genome g : genomes) {
            if (g.isFitnessDetermined()) {
                g.setSharedFitness(g.getFitness() / genomes.size());
            }
        }
    }

    public void sortBySharedFitness() {
        calculateSharedFitness();

        Collections.sort(genomes, new Comparator<Genome>() {
            @Override
            public int compare(Genome o1, Genome o2) {
                double sf1 = o1.getSharedFitness();
                double sf2 = o2.getSharedFitness();

                if (sf1 < sf2)
                    return -1;
                if (sf2 > sf1)
                    return 1;
                else
                    return 0;
            }
        });
    }

    public void setFitness(Genome g, double d) {
        if (d > this.topFitness) {
            topFitness = d;
        }

        g.setFitness(d);
        g.setSharedFitness(d / genomes.size());
        g.setFitnessDetermined(true);
    }


    public void calculateTotalFitness() {
        calculateSharedFitness();
        double res = 0;

        for (Genome g : genomes) {
            res += g.getSharedFitness();
        }

        totalSharedFitness = res;
    }

    public double getAverageFitness() {
        calculateTotalFitness();
        return totalSharedFitness / genomes.size();
    }

    public double getTotalSharedFitness() {
        return totalSharedFitness;
    }

    public Genome getRepresentative() {
        return representative;
    }

    public void addGenome(Genome g) {
        if (!genomes.contains(g)) {
            genomes.add(g);
        }

        g.setSpecies(this);
    }

    public void setRepresentativeAndResetGenomes(Genome g) {
        genomes.clear();
        genomes.add(g);
        this.representative = g;
    }

    public boolean hasGenome(Genome g) {
        return this.genomes.contains(g);
    }

    public ArrayList<Genome> getGenomes() {
        return genomes;
    }

    public int getId() {
        return id;
    }

    public boolean isStale() {
        for (Genome g : genomes) {
            if (g.getFitness() > this.topFitness) {
                staleness = 0;
                topFitness = g.getFitness();
                return false;
            }
        }

        staleness++;

        if (staleness > MAXIMUM_STALENESS) {
            return true;
        } else {
            return false;
        }
    }

    public double getTopFitness() {
        return topFitness;
    }

    public static void resetSpeciesCount() {
        species_count = 0;
    }

    private Object readResolve() throws ObjectStreamException {
        if (this.getId() > species_count) {
            species_count = this.getId();
        }

        return this;
    }

}
