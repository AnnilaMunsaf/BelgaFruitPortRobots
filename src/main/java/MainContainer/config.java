package MainContainer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

import util.Point2D;

public class config {
    public static Point2D triaging_station;
    public static Dictionary<String, Point2D> charging_stations = new Hashtable<String, Point2D>();
    public static double rottenProbability;

    static {
        // TRIAGING
        triaging_station = new Point2D(0, 0);
        rottenProbability = 0.1;

        // CHARGING
        charging_stations.put("6823", new Point2D(0,0));  // CHARGING STATION FOR ROBOT WITH TAG_ID 6823
    }



    public static Random rng = new Random();
}
