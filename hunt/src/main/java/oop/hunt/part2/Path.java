package oop.hunt.part2;

import java.util.ArrayList;

/**
 * The Path class that contains the path generation algorithm
 */
public class Path extends Spot {
    /**
     * Constructor for Path class
     * @param x starting x coordinate
     * @param y starting y coordinate
     * @param name name of the path
     * @param max_animals max number of animals the path can feed
     */
    public Path(int x, int y, String name, int max_animals) {
        super(x, y, name, max_animals);
    }

    /**
     * Generates a path from a starting point to an ending point
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return ArrayList of points that make up the path
     */
    public static ArrayList<Path> generatePath(int x1, int y1, int x2, int y2) {
        ArrayList<Path> paths = new ArrayList<>();
        paths.add(new Path(x1, y1, "", 0));
        int x = x1;
        int y = y1;
        while (x != x2 || y != y2) {
            int dx = Math.abs(x - x2);
            int dy = Math.abs(y - y2);
            if (dx > dy) {
                if (x < x2) {
                    x++;
                } else if (x > x2) {
                    x--;
                }
            } else {
                if (y < y2) {
                    y++;
                } else if (y > y2) {
                    y--;
                }
            }
            paths.add(new Path(x, y, "", 0));
        }
        return paths;
    }
}