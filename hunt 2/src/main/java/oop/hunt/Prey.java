package oop.hunt;

import javafx.scene.media.AudioClip;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import static oop.hunt.Main.randInt;

public class Prey extends Creatura implements Runnable {
    private boolean hiding;
    private int water_level;
    private int food_level;
    private List<Path> paths;
    private List<Water> waters;
    private List<Plant> plants;
    private List<Hideout> hideouts;
    private List<Predator> predators;
    private Hideout current_hideout = null;

    public Prey(int x, int y, String preyName, List<Path> paths, List<Water> waters, List<Plant> plants, List<Hideout> hideouts, List<Predator> predators) {
        super(x, y, preyName, 20, randInt(4, 7), randInt(4, 8), "Villager");
        this.water_level = randInt(45,49);
        this.food_level = randInt(45,49);
        this.paths = paths;
        this.waters = waters;
        this.plants = plants;
        this.hideouts = hideouts;
        this.predators = predators;
    }

    @Override
    public void run() {
        while (this.getHealth() > 0) {
            if (water_level <= 0 || food_level <= 0) {
                this.setHealth(0);
                System.out.println(getName() + " died");
                return;
            }

            // if water level is low, find the nearest water
            if (water_level < 50) {
                unhide();
                pathfindToWater();

                // check if there is any water within range
                for (Water water : waters) {
                    if (water.getX() == this.getX() && water.getY() == this.getY()) {
                        water.join();
                        while (water_level < 100) {
                            water_level += water.getReplenish_speed();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        water.leave();
                        break;
                    }
                }
            }
            else if (food_level < 50) {
                unhide();
                pathfindToFood();

                // check if there is any food within range
                for (Plant plant : plants) {
                    if (plant.getX() == this.getX() && plant.getY() == this.getY()) {
                        plant.join();
                        while (food_level < 100) {
                            food_level += plant.getReplenish_speed();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        plant.leave();
                        break;
                    }
                }
            }
            else if (!hiding) {
                pathfindToHideout();

                // check if there is any hideout within range
                for (Hideout hideout : hideouts) {
                    if (hideout.getX() == this.getX() && hideout.getY() == this.getY()) {
                        hide(hideout);
                        break;
                    }
                }
            }
            else if (hiding) {
                int health = this.getHealth();
                if (health < 20) {
                    this.setHealth(health + 1);
                }
            }

            this.food_level -= 1;
            this.water_level -= 1;

            // Sleep for a short period of time
            try {
                Thread.sleep(1000/this.getSpeed());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Hiding
    private void hide(Hideout hideout) {
        if (!hiding) {
            hideout.join();
            hiding = true;
            current_hideout = hideout;
        }
    }

    private void unhide() {
        if (hiding) {
            current_hideout.leave();
            current_hideout.reproduce(this);
            current_hideout = null;
            hiding = false;
        }
    }

    // pathfind to (x, y)
    protected void pathfindTo(int x, int y) {
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

        // sort paths by distance to water
        Thingamajig target = new Thingamajig(x, y);
        neighbours.sort(Comparator.comparingInt(p -> {
            int dx = p.getX() - target.getX();
            int dy = p.getY() - target.getY();
            return dx * dx + dy * dy;
        }));

        for (Path p : neighbours) {
            if (p.getX() != this.getX() || p.getY() != this.getY()) {
                // move towards the nearest water
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

    private void pathfindToWater() {
        // find the nearest water
        Water target = null;
        int minDistance = Integer.MAX_VALUE;
        for (Water w : waters) {
            if (w.isAtCapacity()) {
                continue;
            }
            int dx = w.getX() - this.getX();
            int dy = w.getY() - this.getY();
            int distance = dx * dx + dy * dy;
            if (distance < minDistance) {
                target = w;
                minDistance = distance;
            }
        }

        if (target != null) {
            pathfindTo(target.getX(), target.getY());
        }
    }

    private void pathfindToFood() {
        // find the nearest food
        Plant target = null;
        int minDistance = Integer.MAX_VALUE;
        for (Plant p : plants) {
            if (p.isAtCapacity()) {
                continue;
            }
            int dx = p.getX() - this.getX();
            int dy = p.getY() - this.getY();
            int distance = dx * dx + dy * dy;
            if (distance < minDistance) {
                target = p;
                minDistance = distance;
            }
        }

        if (target != null) {
            pathfindTo(target.getX(), target.getY());
        }
    }
    private void pathfindToHideout() {
        // find the nearest hideout
        Hideout target = null;
        int minDistance = Integer.MAX_VALUE;
        for (Hideout h : hideouts) {
            if (h.isAtCapacity()) {
                continue;
            }
            int dx = h.getX() - this.getX();
            int dy = h.getY() - this.getY();
            int distance = dx * dx + dy * dy;
            if (distance < minDistance) {
                target = h;
                minDistance = distance;
            }
        }

        if (target != null) {
            pathfindTo(target.getX(), target.getY());
        }
    }

    public boolean getAttacked(Predator p) {
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
        if (this.getHealth() <= 0) {
            return true;
        }
        return false;
    }

    public void getInfected(Predator p) {
        if (Math.random() < p.getInfection_chance()) {
            System.out.println(getName() + " was infected by a predator");
            Predator zombie = new Predator(this.getX(), this.getY(), this.getName() + " Zombie", p.getPreys());
            zombie.setSpeed(p.getSpeed());
            zombie.setStrength(p.getStrength());
            predators.add(zombie);
            new AudioClip(getClass().getResource("/audio/predator.mp3").toExternalForm()).play();
            new Thread(zombie).start();
        }
    }
    public int getWater_level() {
        return water_level;
    }

    public void setWater_level(int water_level) {
        this.water_level = water_level;
    }

    public int getFood_level() {
        return food_level;
    }

    public void setFood_level(int food_level) {
        this.food_level = food_level;
    }

    public boolean getHiding() {
        return hiding;
    }

    public void setHiding(boolean hiding) {
        this.hiding = hiding;
    }

    // getters for lists
    public List<Plant> getPlants() {
        return plants;
    }

    public List<Water> getWaters() {
        return waters;
    }

    public List<Hideout> getHideouts() {
        return hideouts;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public List<Predator> getPredators() {
        return predators;
    }
}