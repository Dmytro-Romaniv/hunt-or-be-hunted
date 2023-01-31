package oop.hunt.part2;

/**
 * The Spot class that is the parent class for all spots and contains their values and synchronization
 */
public abstract class Spot extends Thingamajig {
    private String name;
    private int current_animals;
    private int max_animals;

    /**
     * Constructor for Spot class
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param name name of the spot
     * @param max_animals max number of animals the spot can hold
     */
    public Spot(int x, int y, String name, int max_animals) {
        super(x, y);
        this.name = name;
        this.max_animals = max_animals;
    }

    /**
     * Join method for thread-safe adding an animal to the spot
     * @return true if the animal can join the spot, false otherwise
     */
    public synchronized boolean join() {
        if (current_animals < max_animals) {
            current_animals++;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Leave method for thread-safe removing an animal from the spot
     * @return true if the animal can leave the spot, false otherwise
     */
    public synchronized boolean leave() {
        if (current_animals > 0) {
            current_animals--;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * check if the spot is at capacity
     * @return true if the spot is at capacity, false otherwise
     */
    public synchronized boolean isAtCapacity() {
        return current_animals >= max_animals;
    }

    /**
     * @return current_animals
     */
    public int getCurrent_animals() {
        return current_animals;
    }

    /**
     * @return max_animals
     */
    public int getMax_animals() {
        return max_animals;
    }
}