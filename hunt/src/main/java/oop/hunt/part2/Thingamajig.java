package oop.hunt.part2;

/**
 * The base class for all things in the game
 */
public class Thingamajig {
    private int x;
    private int y;

    /**
     * Constructor for Thingamajig class
     * @param x starting x coordinate
     * @param y starting y coordinate
     */
    public Thingamajig(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
