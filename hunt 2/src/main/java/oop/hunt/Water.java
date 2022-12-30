package oop.hunt;

public class Water extends Spot {
    private int replenish_speed;

    public Water(int x, int y, String name, int max_animals, int replenish_speed) {
        super(x, y, name, max_animals);
        this.replenish_speed = replenish_speed;
    }

    /** 
     * @return int
     */
    public int getReplenish_speed() {
        return replenish_speed;
    }

    
    /** 
     * @param replenish_speed
     */
    public void setReplenish_speed(int replenish_speed) {
        this.replenish_speed = replenish_speed;
    }
}