package uk.ac.ed.inf.aqmaps;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mapbox.geojson.*;

public class WriteFiles {  
    /**
     * Outputs the flight path of the drone as a Feature object
     * 
     * @param path - flight path of the drone
     * @return Feature object
     */
    public static Feature displayPath(List<Position> path) {
        List<Point> points = new ArrayList<>();
        for (Position pos : path) {
            points.add(Point.fromLngLat(pos.getLng(), pos.getLat()));
        }
        var ls = LineString.fromLngLats(points);
        var myFeature = Feature.fromGeometry(ls);
        
        return myFeature;
    }
    
    /**
     * Outputs the sensors of a map as a list of Feature objects with 
     * the appropriate properties for visited and unvisited sensors
     * 
     * @param visitedSensors   - list of visited sensors
     * @param unvisitedSensors - list of unvisited sensors
     * @throws IOException
     * @throws InterruptedException
     * @return List of Feature objects
     */
    public static List<Feature> displayMap(List<Sensor> visitedSensors, List<Sensor> unvisitedSensors) throws IOException, InterruptedException {
        List<Feature> features = new ArrayList<>();
        
        for (Sensor visited : visitedSensors) {
            var myFeature = visited.toFeature();
            visited.addProperties(myFeature, true);
            features.add(myFeature);  
        } 
        
        for (Sensor unvisited : unvisitedSensors) {
            var myFeature = unvisited.toFeature();
            unvisited.addProperties(myFeature, false);
            features.add(myFeature);
        } 
        
        return features;
    }
    
    /**
     * Outputs the drone confinement area as a Feature object for debugging purposes
     * 
     * @param lng1 - first longitude edge
     * @param lng2 - second longitude edge
     * @param lat1 - first latitude edge
     * @param lat2 - second latitude edge
     * @return Feature object
     */
    public static Feature displayConfinementArea (double lng1, double lng2, double lat1, double lat2) {
        var forrestHill = Point.fromLngLat(lng1, lat1);
        var meadows = Point.fromLngLat(lng1, lat2);
        var kfc = Point.fromLngLat(lng2, lat1);
        var buccleuch = Point.fromLngLat(lng2, lat2);
        
        var coords = Arrays.asList(forrestHill, meadows, buccleuch, kfc, forrestHill);
        var border = LineString.fromLngLats(coords);
        return Feature.fromGeometry(border);
    }
    
    /**
     * Writes the input string parameter to the specified input file
     * 
     * @param fname - file to be written to
     * @param str   - String object to be written to the file
     * @throws IOException
     */
    public static void writeToFile(String fname, String str) throws IOException {
        var writer = new BufferedWriter(new FileWriter(fname));
        writer.write(str);
        writer.close();
    }
    
    /**
     * Writes the input FeatureCollection to a .geojson file named with the input date
     * 
     * @param fc    - FeatureCollection object
     * @param day   - day for the filename
     * @param month - month for the filename
     * @param year  - year for the filename
     * @throws IOException
     */
    public static void writeGeoJSON(FeatureCollection fc, String day, String month, String year) throws IOException {
        // Create a JSON-formatted string of the feature collection
        var json = fc.toJson();
        // Create output file 
        var date = day + "-" + month + "-" + year;
        writeToFile("readings-" + date + ".geojson", json);
    }
    
    /**
     * Writes the input String to a .txt file named with the input date
     * 
     * @param log   - String object
     * @param day   - day for the filename
     * @param month - month for the filename
     * @param year  - year for the filename
     * @throws IOException
     */
    public static void writeLogFile(String log, String day, String month, String year) throws IOException {
        var date = day + "-" + month + "-" + year;
        writeToFile("flightpath-" + date + ".txt", log);
    }
}
