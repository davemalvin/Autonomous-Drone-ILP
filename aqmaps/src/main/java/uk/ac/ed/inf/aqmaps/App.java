package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.util.Random;
import com.mapbox.geojson.*;


public class App 
{       
    public static Random rnd;
    public static String port;
    
    /**
     * Main function that reads input arguments, calls the methods to move the drone
     * with respect to the input, and generates the output .geojson and .txt files
     * 
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main( String[] args ) throws IOException, InterruptedException
    {        
        // Command-line input arguments
        String day = args[0];
        String month = args[1];
        String year = args[2];
        double startlat = Double.parseDouble(args[3]);
        double startlng = Double.parseDouble(args[4]);
        int seed = Integer.parseInt(args[5]);
        port = args[6];
        
        // Generate the random seed
        rnd = new Random(seed);
        
        // Instantiate map and drone
        var map = new Map(day, month, year); 
        var initialPos = new Position(startlng, startlat);
        var drone = new Drone(map, initialPos);
        
        // Run the drone algorithm
        drone.droneSimulator();

        
        // Create output files 
        var geojson = WriteFiles.displayMap(drone.getVisitedSensors(), drone.getSensorsToVisit());
        geojson.add(WriteFiles.displayPath(drone.getFlightPath()));
        
        // ** FOR TESTING PURPOSES **
        //geojson.add(WriteFiles.displayConfinementArea(-3.192473,-3.184319,55.946233,55.942617));
        //geojson.addAll(map.getNoFlyZones()); 
        
        var fc = FeatureCollection.fromFeatures(geojson);
        WriteFiles.writeGeoJSON(fc, day, month, year);
        WriteFiles.writeLogFile(drone.getLog(), day, month, year);    
    }
}