package oop.hunt.part2;

/**
 * The Water class that contains the values for the water spots
 */
public class Water extends Spot {
    private int replenish_speed;

    /**
     * Constructor for Water class
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param name name of the water
     * @param max_animals max number of animals the water can hold
     * @param replenish_speed how fast the water replenishes water
     */
    public Water(int x, int y, String name, int max_animals, int replenish_speed) {
        super(x, y, name, max_animals);
        this.replenish_speed = replenish_speed;
    }

    /**
     * @return replenish_speed
     */
    public int getReplenish_speed() {
        return replenish_speed;
    }
}