package oop.hunt;

import java.util.ArrayList;

public class Path extends Spot {
    public Path(int x, int y, String name, int max_animals) {
        super(x, y, name, max_animals);
    }

    public static ArrayList<Path> generatePath(int x1, int y1, int x2, int y2) {
        ArrayList<Path> paths = new ArrayList<>();
        paths.add(new Path(x1, y1, "Path", 999));
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
            paths.add(new Path(x, y, "Path", 999));
        }
        return paths;
    }
}