package MainContainer;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

import util.Point2D;

public class config {
    public static Point2D triaging_station;
    public static Dictionary<String, Point2D> charging_stations = new Hashtable<String, Point2D>();
    public static double rottenProbability;
    public static int batteryTime; // in seconds
    public static int chargingDuration; // in seconds
    public static Dictionary<String, Point2D> testPoints = new Hashtable<String, Point2D>();

    static {
        // TRIAGING
        triaging_station = new Point2D(15288, 14961);
        rottenProbability = 0;

        // CHARGING
        batteryTime = 60 * 100000;                                     // BATTERY  DURATION IN SECONDS
        chargingDuration = 30;                                         // CHARGING DURATION IN SECONDS
        // TEST POINTS
        testPoints.put("A", new Point2D(14798,13077)); 
        testPoints.put("B", new Point2D(15487,13754)); 
        testPoints.put("C", new Point2D(14992,15190)); 
        testPoints.put("D", new Point2D(13454,14198)); 
        testPoints.put("E", new Point2D(13575,15001)); 
        testPoints.put("F", new Point2D(12150,14883)); 

        charging_stations.put("6a75", testPoints.get("E"));  // CHARGING STATION FOR ROBOT WITH TAG_ID 6823

    }



    public static Random rng = new Random();
}
