package uk.ac.ed.inf.aqmaps;

import java.io.*;
import com.mapbox.geojson.*;
import com.google.gson.Gson;

public class Sensor {
    private final String location;
    private float battery;
    private String reading;
    
    public Sensor(String location, float battery, String reading) {
        this.location = location;
        this.battery = battery;
        this.reading = reading;
    }
    
    public String getLocation() {
        return location;
    }
    
    public double getBattery() {
        return battery;
    }
    
    public String getReading() {
        return reading;
    }
    
    /**
     * Get the position of the sensor
     * 
     * @return Position of the sensor
     */
    public Position toPosition() throws IOException, InterruptedException {
        
        // Get sensor details with the split What3Words
        var det = parseSensorDetails();  
        
        // Return the coordinates of the address
        return det.getCoordinates();
    }
    
    /**
     * Convert sensor into a Feature object
     * 
     * @return Feature object
     */
    public Feature toFeature() throws IOException, InterruptedException {
        // Split what3words
        var pos = toPosition();
        
        // Create a Feature object of this sensor
        var p = Point.fromLngLat(pos.getLng(), pos.getLat());
        var f = Feature.fromGeometry(p);    
        
        return f;
    }
    
    /**
     * Obtain the What3Words information for this sensor
     * 
     * @return The corresponding SensorDetails object
     */
    public SensorDetails parseSensorDetails() throws IOException, InterruptedException {
        
        // Split the What3Words address of the sensor
        var splitLoc = location.split("[.]");
        
        var what3word = splitLoc[0] + "/" + splitLoc[1] + "/" + splitLoc[2];
        var urlString = "http://localhost:" + App.port + "/words/" + what3word + "/details.json";
        var json = JsonParser.readJson(urlString);
        
        // Deserialising a JSON record to a Java object using its type
        SensorDetails details = new Gson().fromJson(json, SensorDetails.class);
        
        return details;
    }
    
    /**
     * Obtain the RGB string and marker symbol of the sensor according to its reading and battery
     * 
     * @return Length 2 array of String: first element is the RGB string & second element is the marker symbol
     */
    public String[] getProperties() {
        var rgbString = "";
        var markerSymbol = "";
        if (battery < 10 || reading.equals("NaN") || reading.equals("null")) {
            rgbString = "#000000";
            markerSymbol = "cross";
        } else {
            double r = Double.parseDouble(reading);
            if (r >= 0 && r < 32) { rgbString = "#00ff00"; markerSymbol = "lighthouse"; }
            else if (r >= 32 && r < 64) { rgbString = "#40ff00"; markerSymbol = "lighthouse"; }
            else if (r >=64 && r < 96) { rgbString = "#80ff00"; markerSymbol = "lighthouse"; }
            else if (r >= 96 && r < 128) { rgbString = "#c0ff00"; markerSymbol = "lighthouse"; }
            else if (r >= 128 && r < 160) { rgbString = "#ffc000"; markerSymbol = "danger"; }
            else if (r >= 160 && r < 192) { rgbString = "#ff8000"; markerSymbol = "danger"; }
            else if (r >= 192 && r < 224) { rgbString = "#ff4000"; markerSymbol = "danger"; }
            else if (r >= 224 && r < 256) { rgbString = "#ff0000"; markerSymbol = "danger"; }
        }
        return new String[] {rgbString, markerSymbol};
    }

    /**
     * Add properties to the sensor (represented as a Feature object) according to whether it has been visited or not
     * 
     * @param feature  - Feature object
     * @param hasVisited - true if sensor has been visited by drone, false otherwise
     */
    public void addProperties(Feature feature, boolean hasVisited) {  
        feature.addStringProperty("marker-size", "medium");
        feature.addStringProperty("location", location);
        
        if (hasVisited) {
            String[] prop = getProperties();
            feature.addStringProperty("rgb-string", prop[0]);
            feature.addStringProperty("marker-color", prop[0]);
            feature.addStringProperty("marker-symbol", prop[1]); 
        }
        else {
            feature.addStringProperty("rgb-string", "#aaaaaa");
            feature.addStringProperty("marker-color", "#aaaaaa");
        }   
    }
}
