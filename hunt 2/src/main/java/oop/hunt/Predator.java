package oop.hunt;

import java.util.List;

import static oop.hunt.Main.GRID_SIZE;
import static oop.hunt.Main.randInt;

public class Predator extends Creatura implements Runnable {
    private boolean mode = false;
    private float infection_chance = (float) (Math.random() * 0.4);
    private List<Prey> preys;

    public Predator(int x, int y, String predatorName, List<Prey> preys) {
        super(x, y, predatorName, 20, randInt(4, 6), randInt(7, 10), "Zombie");
        this.preys = preys;
    }

    @Override
    public void run() {
        while (this.getHealth() > 0) {
            int x = getX();
            int y = getY();

            if (mode) {
                // find the nearest prey


                Prey target = null;
                int minDistance = Integer.MAX_VALUE;
                for (Prey prey : preys) {
                    if (prey.getHiding()) {
                        continue;
                    }
                    int dx = prey.getX() - x;
                    int dy = prey.getY() - y;
                    int distance = dx * dx + dy * dy;
                    if (distance < minDistance) {
                        target = prey;
                        minDistance = distance;
                    }
                }

                if (target != null) {
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
                x = (x + randInt(-1, 1) + GRID_SIZE) % GRID_SIZE;
                y = (y + randInt(-1, 1) + GRID_SIZE) % GRID_SIZE;
            }

            this.setX(x);
            this.setY(y);

            // check if there is any prey within range
            if (preys.size() > 0) {
                for (Prey prey : preys) {
                    if (prey.getHiding()) {
                        mode = false;
                        continue;
                    }
                    int dx = Math.abs(prey.getX() - x);
                    int dy = Math.abs(prey.getY() - y);
                    if (dx <= 1 && dy <= 1) {
                        if (prey.getAttacked(this)) {
                            System.out.println(this.getName() + " killed " + prey.getName());
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            prey.getInfected(this);
                            mode = false;
                            relax();
                        }
                        else {
                            try {
                                Thread.sleep(1000/this.getStrength());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                        }
                    else if (dx <= 10 && dy <= 10) {
                        mode = true;
                    }
                    else {
                        mode = false;
                    }
                }
            }
            else {
                mode = false;
            }

            try {
                Thread.sleep(1000/this.getSpeed());
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void relax() {
        for (int i = 0; i < 10; i++) {
            this.setX((getX() + randInt(-1, 1) + GRID_SIZE) % GRID_SIZE);
            this.setY((getY() + randInt(-1, 1) + GRID_SIZE) % GRID_SIZE);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // getters and setters
    public boolean getMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public List<Prey> getPreys() {
        return preys;
    }

    public float getInfection_chance() {
        return infection_chance;
    }
}

