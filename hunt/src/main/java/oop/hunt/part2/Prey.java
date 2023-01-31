package oop.hunt.part2;

import javafx.scene.media.AudioClip;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

/**
 * The Prey class that contains the logic for the villager
 */
public class Prey extends Creatura implements Runnable {
    private boolean hiding = false;
    private boolean reroute = false;
    private Thingamajig target = new Thingamajig(0, 0);

    private int water_level;
    private int food_level;
    private List<Path> paths;
    private List<Water> waters;
    private List<Plant> plants;
    private List<Hideout> hideouts;
    private List<Predator> predators;
    private Hideout current_hideout = null;
    private Semaphore semaphore;

    /**
     * Constructor for Prey class
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param preyName name of prey
     * @param paths list of path objects
     * @param waters list of water objects
     * @param plants list of plant objects
     * @param hideouts list of hideout objects
     * @param predators list of predator objects
     */
    public Prey(int x, int y, String preyName, List<Path> paths, List<Water> waters, List<Plant> plants, List<Hideout> hideouts, List<Predator> predators) {
        super(x, y, preyName, 20, App.randInt(5, 8), App.randInt(1, 5), "Villager");
        this.water_level = App.randInt(45,49);
        this.food_level = App.randInt(45,49);
        this.paths = paths;
        this.waters = waters;
        this.plants = plants;
        this.hideouts = hideouts;
        this.predators = predators;
        this.semaphore = new Semaphore(1);
    }

    /**
     * Run method for prey thread
     * if health is 0, prey dies
     * if reroute is true, prey will move towards target
     * if water level is low, prey will find nearest water and replenish water level
     * if food level is low, prey will find nearest plant and replenish food level
     * if hiding is false, prey will find nearest hideout and hide if possible
     */
    @Override
    public void run() {
        while (this.getHealth() > 0) {
            // dying
            if (water_level <= 0 || food_level <= 0) {
                this.setHealth(0);
                System.out.println(getName() + " died");
            }
            // check if rerouted
            else if (reroute) {
                unhide();
                pathfindTo(target.getX(), target.getY());
                if (getX() == target.getX() && getY() == target.getY()) {
                    reroute = false;
                }
            }
            // if water level is low, find the nearest water
            else if (water_level < 50) {
                unhide();
                pathfindToSpot(waters);
                // check if there is any water within range
                for (Water water : waters) {
                    if (water.getX() == this.getX() && water.getY() == this.getY()) {
                        if (water.join()) {
                            while (water_level < 100 && this.getHealth() > 0) {
                                water_level += 1;
                                try {
                                    Thread.sleep(100 / water.getReplenish_speed());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            water.leave();
                            break;
                        }
                    }
                }
            }
            // if food level is low, find the nearest plant
            else if (food_level < 50) {
                unhide();
                pathfindToSpot(plants);
                // check if there is any food within range
                for (Plant plant : plants) {
                    if (plant.getX() == this.getX() && plant.getY() == this.getY()) {
                        if (plant.join()) {
                            while (food_level < 100 && this.getHealth() > 0) {
                                food_level += 1;
                                try {
                                    Thread.sleep(100 / plant.getReplenish_speed());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            plant.leave();
                            break;
                        }
                    }
                }
            }
            else if (!hiding) {
                // find the nearest hideout
                pathfindToSpot(hideouts);
                // check if there is any hideout within range
                for (Hideout hideout : hideouts) {
                    if (hideout.getX() == this.getX() && hideout.getY() == this.getY()) {
                        if (hide(hideout)) {
                            break;
                        }
                    }
                }
            }
            else {
                // heal up while hiding
                int health = this.getHealth();
                if (health < 20) {
                    this.setHealth(health + 1);
                }
            }

            this.water_level -= 1;
            this.food_level -= 1;

            // Sleep for a short period of time
            try {
                Thread.sleep(1000/this.getSpeed());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // interrupt the thread
        Thread.currentThread().interrupt();
    }

    /**
     * Hide in the hideout
     * @param hideout
     * @return true if hideout is available, false otherwise
     */
    private boolean hide(Hideout hideout) {
        if (!hiding) {
            current_hideout = hideout;
            if (current_hideout.join()) {
                hiding = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Stop hiding in the current hideout
     */
    private boolean unhide() {
        if (hiding) {
            if (current_hideout.leave()) {
                hiding = false;
                current_hideout.reproduce(this);
                current_hideout = null;
                return true;
            }
        }
        return false;
    }

    /**
     * The prey pathfinding algorithm (it sucks)
     * @param x
     * @param y
     */
    public void pathfindTo(int x, int y) {
        // ps: ain't no way I'm implementing a* for this

        // get neighbouring paths
        List<Path> neighbours = new ArrayList<>();
        for (Path p : paths) {
            int dx = Math.abs(p.getX() - this.getX());
            int dy = Math.abs(p.getY() - this.getY());
            if (dx <= 1 && dy <= 1) {
                neighbours.add(p);
            }
        }

        // sort paths by distance to (x, y)
        Thingamajig target = new Thingamajig(x, y);
        neighbours.sort(Comparator.comparingInt(p -> {
            int dx = p.getX() - target.getX();
            int dy = p.getY() - target.getY();
            return dx * dx + dy * dy;
        }));

        for (Path p : neighbours) {
            if (p.getX() != this.getX() || p.getY() != this.getY()) {
                // move onto the nearest path
                int dx = p.getX() - this.getX();
                int dy = p.getY() - this.getY();
                if (dx != 0) {
                    this.setX(this.getX() + dx / Math.abs(dx));
                }
                if (dy != 0) {
                    this.setY(this.getY() + dy / Math.abs(dy));
                }
                break;
            }
        }
    }

    /**
     * Pathfinding to the nearest spot
     * @param spots the list of spots
     */
    private void pathfindToSpot(List<? extends Spot> spots) {
        Spot target = null;
        int minDistance = Integer.MAX_VALUE;
        for (Spot spot : spots) {
            if (spot.isAtCapacity()) {
                continue;
            }
            int dx = spot.getX() - this.getX();
            int dy = spot.getY() - this.getY();
            int distance = dx * dx + dy * dy;
            if (distance < minDistance) {
                target = spot;
                minDistance = distance;
            }
        }

        if (target != null) {
            // semaphore so they don't trample each other
            try {
                semaphore.acquire();
                pathfindTo(target.getX(), target.getY());
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Dueling with the predator
     * @param p the predator in question
     * @return true if the predator wins, false otherwise
     */
    public synchronized boolean getAttacked(Predator p) {
        new AudioClip(getClass().getResource("/audio/damage.mp3").toExternalForm()).play();
        if ((p.getStrength() - this.getStrength()) > 0) {
            this.setHealth(this.getHealth() - (p.getStrength() - this.getStrength()));
        }
        else if (p.getStrength() == this.getStrength()) {
            this.setStrength(this.getStrength() - 1);
        }
        else {
            p.setHealth(p.getHealth() - (this.getStrength() - p.getStrength()));
        }
        return this.getHealth() <= 0;
    }

    /**
     * Turning the prey into a predator
     * @param p the predator that will be copied
     */
    public void getInfected(Predator p) {
        if (Math.random() < p.getInfection_chance()) {
            System.out.println(getName() + " turned into a zombie");
            Predator zombie = new Predator(this.getX(), this.getY(), this.getName() + " Zombie", p.getPreys());
            zombie.setSpeed(p.getSpeed());
            zombie.setStrength(p.getStrength());
            predators.add(zombie);
            new AudioClip(getClass().getResource("/audio/predator.mp3").toExternalForm()).play();
            new Thread(zombie).start();
        }
    }

    /**
     *
     * @param x
     * @param y
     */
    public void reroute(int x, int y) {
        reroute = true;
        target.setX(x);
        target.setY(y);
    }

    /**
     * @return water_level
     */
    public int getWater_level() {
        return water_level;
    }

    /**
     * @param water_level
     */
    public void setWater_level(int water_level) {
        this.water_level = water_level;
    }

    /**
     * @return food_level
     */
    public int getFood_level() {
        return food_level;
    }

    /**
     * @param food_level
     */
    public void setFood_level(int food_level) {
        this.food_level = food_level;
    }

    /**
     * @return hiding
     */
    public boolean getHiding() {
        return hiding;
    }

    /**
     * @param hiding
     */
    public void setHiding(boolean hiding) {
        this.hiding = hiding;
    }

    /**
     * @return plants
     */
    public List<Plant> getPlants() {
        return plants;
    }

    /**
     * @return waters
     */
    public List<Water> getWaters() {
        return waters;
    }

    /**
     * @return hideouts
     */
    public List<Hideout> getHideouts() {
        return hideouts;
    }

    /**
     * @return paths
     */
    public List<Path> getPaths() {
        return paths;
    }

    /**
     * @return predators
     */
    public List<Predator> getPredators() {
        return predators;
    }
}