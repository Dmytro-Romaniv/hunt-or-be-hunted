package oop.hunt;

public class Spot extends Thingamajig {
    private String name;
    private int current_animals;
    private int max_animals;

    public Spot(int x, int y, String name, int max_animals) {
        super(x, y);
        this.name = name;
        this.max_animals = max_animals;
    }

    public void join() {
        if (current_animals < max_animals) {
            current_animals++;
        }
    }

    public void leave() {
        if (current_animals > 0) {
            current_animals--;
        }
    }

    public boolean isAtCapacity() {
        return current_animals >= max_animals;
    }

    public int getCurrent_animals() {
        return current_animals;
    }

    /** 
     * @return String
     */
    public String getName() {
        return name;
    }

    
    /** 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    
    /** 
     * @return int
     */
    public int getMax_animals() {
        return max_animals;
    }

    
    /** 
     * @param max_animals
     */
    public void setMax_animals(int max_animals) {
        this.max_animals = max_animals;
    }
}