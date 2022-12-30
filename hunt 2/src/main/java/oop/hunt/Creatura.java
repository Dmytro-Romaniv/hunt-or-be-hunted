package oop.hunt;

public class Creatura extends Thingamajig {
    private String name;
    private int health;
    private int speed;
    private int strength;
    private String species_name;

    public Creatura(int x, int y, String name, int health, int speed, int strength, String species_name) {
        super(x, y);
        this.name = name;
        this.health = health;
        this.speed = speed;
        this.strength = strength;
        this.species_name = species_name;
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
    public int getHealth() {
        return health;
    }

    
    /** 
     * @param health
     */
    public void setHealth(int health) {
        this.health = health;
    }
    
    /** 
     * @return int
     */
    public int getSpeed() {
        return speed;
    }

    
    /** 
     * @param speed
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    
    /** 
     * @return int
     */
    public int getStrength() {
        return strength;
    }

    
    /** 
     * @param strength
     */
    public void setStrength(int strength) {
        this.strength = strength;
    }

    
    /** 
     * @return String
     */
    public String getSpecies_name() {
        return species_name;
    }

    
    /** 
     * @param species_name
     */
    public void setSpecies_name(String species_name) {
        this.species_name = species_name;
    }
}