package uk.ac.ed.inf.heatmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.*;

/**
 * Hello world!
 *
 */
public class App 
{
    /**
     * Writes the input string parameter to the specified input file.
     * 
     * @param fileName - name of the file to be written to
     * @param s - String object to be written to the file
     * @throws IOException
     */
    private static void writeToFile(String fileName, String s) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(s);
        writer.close();
    }
    
    /**
     * Outputs the RGB string as a String object according 
     * to the value of the input (air quality reading)
     * 
     * @param x - air quality reading 
     * @return String object
     */
    public static String getRGBString (int x) {
        String rgbString = "";
        if (x >= 0 && x < 32) rgbString = "#00ff00";
        else if (x >= 32 && x < 64) rgbString = "#40ff00";
        else if (x >=64 && x < 96) rgbString = "#80ff00";
        else if (x >= 96 && x < 128) rgbString = "#c0ff00";
        else if (x >= 128 && x < 160) rgbString = "#ffc000";
        else if (x >= 160 && x < 192) rgbString = "#ff8000";
        else if (x >= 192 && x < 224) rgbString = "#ff4000";
        else if (x >= 224 && x < 256) rgbString = "#ff0000";
        return rgbString;
    }
    
    /**
     * Outputs the drone confinement area as a Feature object
     * 
     * @param lng1 - first longitude of the drone confinement area
     * @param lng2 - second longitude of the drone confinement area
     * @param lat1 - first latitude of the drone confinement area
     * @param lat2 - second latitude of the drone confinement area
     * @return Feature object
     */
    public static Feature droneConfinementArea (double lng1, double lng2, double lat1, double lat2) {
        Point forrestHill = Point.fromLngLat(lng1, lat1);
        Point meadows = Point.fromLngLat(lng1, lat2);
        Point kfc = Point.fromLngLat(lng2, lat1);
        Point buccleuch = Point.fromLngLat(lng2, lat2);
        
        List<Point> coords = Arrays.asList(forrestHill, meadows, buccleuch, kfc, forrestHill);
        LineString border = LineString.fromLngLats(coords);
        return Feature.fromGeometry(border);
    }
    
    /**
     * Main function that reads the input file argument, calls the methods to obtain 
     * the drone confinement area and the RGB string of each prediction in the
     * input file, and generates the output heatmap.geojson file.
     * 
     * @param args
     * @throws IOException
     */
    public static void main( String[] args) throws IOException
    {
        // Open predictions.txt file
        Scanner scan = new Scanner(new File(args[0]));
        
        // Initialise a list of features 
        List<Feature> features = new ArrayList<>();
       
        // Longitudes and latitudes of the drone confinement area
        double lng1 = -3.192473;
        double lng2 = -3.184319;
        double lat1 = 55.946233;
        double lat2 = 55.942617;
        
        // Add feature to list of features for the drone confinement area
        Feature border = droneConfinementArea(lng1, lng2, lat1, lat2);
        features.add(border);
        
        // The dimension of one polygon/rectangle
        double diffLng = (lng2 - lng1) / 10.0;
        double diffLat = (lat1 - lat2) / 10.0;
        
        // Get a copy of the original value of lng1
        double copylng1 = lng1;
        
        // Read each line of input file until EOF
        while (scan.hasNextLine()) {
            // Get the current line of predictions and split them accordingly to a list of Strings
            String currLine = scan.nextLine();
            String[] predictions = currLine.split(", ");
            
            // Check if there are exactly 10 predictions in the current line
            if (predictions.length != 10) {
                System.out.println("There are missing predictions!");
                System.exit(0);
            }
            
            // Go through each prediction from left to right
            for (int i = 0; i < 10; i++) {
                int currVal = Integer.parseInt(predictions[i]);
                
                // Check if current prediction is valid
                if (currVal < 0 || currVal >= 256) {
                    System.out.println("Invalid prediction: should only be between 0 and 255 inclusive!");
                    System.exit(0);
                }
                
                // Create a Feature object from the Polygon made with the Points
                Point pt1 = Point.fromLngLat(lng1, lat1);
                Point pt2 = Point.fromLngLat(lng1 + diffLng, lat1);
                Point pt3 = Point.fromLngLat(lng1 + diffLng, lat1 - diffLat);
                Point pt4 = Point.fromLngLat(lng1, lat1 - diffLat);              
                List<List<Point>> rectangle = Arrays.asList(Arrays.asList(pt1, pt2, pt3, pt4, pt1));
                Polygon p = Polygon.fromLngLats(rectangle);
                Feature f = Feature.fromGeometry(p);
                
                // Obtain the RGB string for the current prediction
                String rgbString = getRGBString(currVal);
                
                // Add required properties to the Feature object and add it to list of features
                f.addStringProperty("fill", rgbString);
                f.addStringProperty("rgb-string", rgbString);
                f.addNumberProperty("fill-opacity", 0.75);
                features.add(f);
                
                // Adjust longitude for next prediction in the current line
                lng1 += diffLng;
            }
            // Adjust longitude and latitude appropriately for the next line of predictions
            lng1 = copylng1;
            lat1 -= diffLat;
        }
        scan.close();
        
//        String currLine = scan.nextLine();
//        String[] predictions = currLine.split(", ");
//        System.out.println(predictions);
//        for (int i = 1; i <= 10; i++) {
//            int currVal = Integer.parseInt(predictions[i-1]);
//            System.out.println(currVal);
//            String rgbString = getRGBString(currVal);
//            System.out.println(rgbString);
//          
//            Point pt1 = Point.fromLngLat(lng1, lat1);
//            Point pt2 = Point.fromLngLat(lng1 + diffLng, lat1);
//            Point pt3 = Point.fromLngLat(lng1 + diffLng, lat1 - diffLat);
//            Point pt4 = Point.fromLngLat(lng1, lat1 - diffLat);
//            
//            List<List<Point>> polygon = Arrays.asList(Arrays.asList(pt1, pt2, pt3, pt4, pt1));
//            Polygon p = Polygon.fromLngLats(polygon);
//            Feature f = Feature.fromGeometry(p);
//            f.addStringProperty("fill", rgbString);
//            f.addStringProperty("rgb-string", rgbString);
//            f.addNumberProperty("fill-opacity", 0.75);
//            features.add(f);
//            lng1 = lng1 + diffLng;
//        }
        
        
        // Create polygons
//        while (scan.hasNextLine()) {
//            int lineNo = 1;
//            String currLine = scan.nextLine();
//            String[] predictions = currLine.split(", ");
//            
//            for (int i = 1; i <= 10; i++) {
//                int currVal = Integer.parseInt(predictions[i-1]);
//                String rgbString = getRGBString(currVal);
//              
//                Point pt1 = Point.fromLngLat(lng1, lat1);
//                Point pt2 = Point.fromLngLat((lng1 + diffLng) * (double) i, lat1);
//                Point pt3 = Point.fromLngLat((lng1 + diffLng) * (double) i, lat1 - diffLat);
//                Point pt4 = Point.fromLngLat(lng1, lat1 - diffLat);
//                
//                List<List<Point>> polygon = Arrays.asList(Arrays.asList(pt1, pt2, pt3, pt4, pt1));
//                Polygon p = Polygon.fromLngLats(polygon);
//                Feature f = Feature.fromGeometry(p);
//                f.addStringProperty("fill", rgbString);
//                f.addStringProperty("rgb-string", rgbString);
//                f.addNumberProperty("fill-opacity", 0.75);
//                features.add(f);
//            }
//            
//            lat1 = lat1 - diffLat;
//            lineNo++;
//        }
        FeatureCollection fc = FeatureCollection.fromFeatures(features);
        String json = fc.toJson();
        writeToFile("heatmap.geojson", json);
    }
}