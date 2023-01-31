package oop.hunt.part2;

import java.util.List;

/**
 * The Predator class that contains the logic for the zombie
 */
public class Predator extends Creatura implements Runnable {
    private boolean hunting = false;
    private float infection_chance = (float) (Math.random() * 0.4);
    private List<Prey> preys;

    /**
     * Constructor for Predator class
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param predatorName name of predator
     * @param preys list of prey objects
     */
    public Predator(int x, int y, String predatorName, List<Prey> preys) {
        super(x, y, predatorName, 20, App.randInt(4, 6), App.randInt(6, 10), "Zombie");
        this.preys = preys;
    }

    /**
     * Run method for predator thread
     * predator will hunt for prey if they are within a certain range
     * if predator successfully kills prey, it might infect the prey and relax
     * if no prey is found, predator will move randomly
     */
    @Override
    public void run() {
        while (this.getHealth() > 0) {
            int x = getX();
            int y = getY();

            if (preys.size() > 0) {
                for (Prey prey : preys) {
                    if (prey.getHiding()) {
                        hunting = false;
                        continue;
                    }
                    if (Math.abs(prey.getX() - x) <= App.GRID_SIZE / 4 && Math.abs(prey.getY() - y) <= App.GRID_SIZE / 4) {
                        hunting = true;
                        break;
                    } else {
                        hunting = false;
                    }
                }
            }
            else {
                hunting = false;
            }

            if (hunting) {
                // find the nearest prey
                Prey target = null;
                int minDistance = Integer.MAX_VALUE;
                for (Prey prey : preys) {
                    if (prey.getHiding()) {
                        continue;
                    }
                    int distance = (prey.getX()-x)*(prey.getX()-x) + (prey.getY()-y)*(prey.getY()-y);
                    if (distance < minDistance) {
                        target = prey;
                        minDistance = distance;
                    }
                }

                if (target != null) {
                    // attack the prey
                    if (minDistance <= 1) {
                        if (target.getAttacked(this)) {
                            System.out.println(this.getName() + " killed " + target.getName());
                            hunting = false;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            target.getInfected(this);
                            relax();
                        }
                        else {
                            try {
                                Thread.sleep(1000/this.getStrength());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // move towards the nearest prey
                    int dx = target.getX() - x;
                    int dy = target.getY() - y;
                    if (dx != 0) {
                        x += dx / Math.abs(dx);
                    }
                    if (dy != 0) {
                        y += dy / Math.abs(dy);
                    }
                }
            }
            else {
                x = (x + App.randInt(-1, 1) + App.GRID_SIZE) % App.GRID_SIZE;
                y = (y + App.randInt(-1, 1) + App.GRID_SIZE) % App.GRID_SIZE;
            }

            this.setX(x);
            this.setY(y);

            try {
                Thread.sleep(1000/this.getSpeed());
            } catch (InterruptedException e) {
                break;
            }
        }
        // interrupt the thread
        Thread.currentThread().interrupt();
    }

    /**
     * Chill out.
     */
    private void relax() {
        for (int i = 0; i < 10; i++) {
            this.setX((getX() + App.randInt(-1, 1) + App.GRID_SIZE) % App.GRID_SIZE);
            this.setY((getY() + App.randInt(-1, 1) + App.GRID_SIZE) % App.GRID_SIZE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @return true if predator is hunting, false otherwise
     */
    public boolean getHunting() {
        return hunting;
    }

    /**
     * @return preys
     */
    public List<Prey> getPreys() {
        return preys;
    }

    /**
     * @return infection chance
     */
    public float getInfection_chance() {
        return infection_chance;
    }
}

