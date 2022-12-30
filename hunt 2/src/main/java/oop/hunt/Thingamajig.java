package oop.hunt;

public class Thingamajig {
    private int x;
    private int y;

    public Thingamajig(int x, int y) {
        this.x = x;
        this.y = y;
    }


    /** 
     * @return int
     */
    public int getX() {
        return x;
    }

    
    /** 
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    
    /** 
     * @return int
     */
    public int getY() {
        return y;
    }

    
    /** 
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }
}
