package oop.hunt;

import javafx.scene.media.AudioClip;

import java.util.List;

import static oop.hunt.Main.randInt;

public class Hideout extends Spot {
    private float reproduction_chance = (float) (Math.random() * 0.4);
    private List<Prey> preys;
    public Hideout(int x, int y, String name, int max_animals, List<Prey> preys) {
        super(x, y, name, max_animals);
        this.preys = preys;
    }

    protected void reproduce(Prey prey) {
        if (Math.random() < reproduction_chance) {
            System.out.println("Reproduced " + prey.getName() + " Jr.");
            Prey child = new Prey(prey.getX(), prey.getY(), prey.getName() + " Jr.", prey.getPaths(), prey.getWaters(), prey.getPlants(), prey.getHideouts(), prey.getPredators());
            child.setSpeed(prey.getSpeed());
            child.setStrength(prey.getStrength());
            preys.add(child);
            new AudioClip(getClass().getResource("/audio/prey.mp3").toExternalForm()).play();
            new Thread(child).start();
        }
    }

    public float getReproduction_chance() {
        return reproduction_chance;
    }
}