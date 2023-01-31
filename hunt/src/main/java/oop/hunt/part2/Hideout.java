package oop.hunt.part2;

import javafx.scene.media.AudioClip;
import java.util.List;

/**
 * The Hideout class that contains the reproduction method for the houses
 */
public class Hideout extends Spot {
    private float reproduction_chance = (float) (Math.random() * 0.4);
    private List<Prey> preys;

    /**
     * Constructor for Hideout class
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param name name of hideout
     * @param max_animals max number of animals hideout can hold
     * @param preys list of preys
     */
    public Hideout(int x, int y, String name, int max_animals, List<Prey> preys) {
        super(x, y, name, max_animals);
        this.preys = preys;
    }

    /**
     * Reproduce a new prey
     * @param prey the prey to reproduce
     */
    public void reproduce(Prey prey) {
        if (Math.random() < reproduction_chance) {
            System.out.println(prey.getName() + " Jr. was born");
            Prey child = new Prey(prey.getX(), prey.getY(), prey.getName() + " Jr.", prey.getPaths(), prey.getWaters(), prey.getPlants(), prey.getHideouts(), prey.getPredators());
            child.setSpeed(prey.getSpeed());
            child.setStrength(prey.getStrength());
            preys.add(child);
            new AudioClip(getClass().getResource("/audio/prey.mp3").toExternalForm()).play();
            new Thread(child).start();
        }
    }

    /**
     * @return reproduction_chance
     */
    public float getReproduction_chance() {
        return reproduction_chance;
    }
}