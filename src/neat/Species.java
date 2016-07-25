package neat;

import java.util.ArrayList;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Species {
    private double sharedFitness;
    private ArrayList<Genome> genomes;
    private Genome representative;

    public Species(Genome representative) {
        this.representative = representative;
        genomes.add(representative);
    }

    public void calculateSharedFitness() {
        for (Genome g : genomes) {
            g.setSharedFitness(g.getFitness() / genomes.size());
        }
    }

    public Genome getRepresentative() {
        return representative;
    }

    public void addGenome(Genome g) {
        genomes.add(g);
    }
}
