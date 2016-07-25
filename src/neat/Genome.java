package neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by qfi_2 on 25.07.2016.
 */
public class Genome {
    ArrayList<Neuron> nodeGenes;
    ArrayList<Connection> connectionGenes;
    private double fitness;
    private double sharedFitness;
    private int highestInnov;

    public Genome() {
        nodeGenes = new ArrayList<Neuron>();
        connectionGenes = new ArrayList<Connection>();
        highestInnov = 0;
        fitness = 0;
        sharedFitness = 0;
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
        connectionGenes.add(c);
        sortConnectionGenes();

        int innov = c.getInnov();

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

}
