package oop.hunt.part2;

/**
 * The Plant class that contains the values for the emeralds
 */
public class Plant extends Spot {
    private int replenish_speed;

    /**
     * Constructor for Plant class
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param name name of the plant
     * @param max_animals max number of animals the plant can feed
     * @param replenish_speed how fast the plant replenishes food
     */
    public Plant(int x, int y, String name, int max_animals, int replenish_speed) {
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