package uk.ac.ed.inf.aqmaps;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

public class Map {
    private String day;
    private String month;
    private String year;
    private List<Sensor> sensors = new ArrayList<>();
    private List<Feature> noFlyZones = new ArrayList<>();
    
    public Map(String day, String month, String year) throws IOException, InterruptedException {
        this.day = day;
        this.month = month;
        this.year = year;
        this.parseSensors();
        this.parseNoFlyZones();
    }
    
    public String getDay() {
        return day;
    }
    
    public String getMonth() {
        return month;
    }
    
    public String getYear() {
        return year;
    }
    
    public List<Sensor> getSensors() {
        return sensors;
    }
    
    public List<Feature> getNoFlyZones() {
        return noFlyZones;
    }
    
    /**
     * Obtain the sensors for this map and assign to the attribute sensors
     */
    private void parseSensors() throws IOException, InterruptedException {
        // Generate the json URL for air quality data
        var date = year + "/" + month + "/" + day;
        var urlString = "http://localhost:" + App.port + "/maps/" + date + "/air-quality-data.json";
        var json = JsonParser.readJson(urlString);
        
        // Deserialising a JSON list to a Java object using its type
        Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
        ArrayList<Sensor> sensorsToVisit = new Gson().fromJson(json, listType);
        
        sensors = sensorsToVisit;
    }
    
    /**
     * Obtain the no-fly zones for this map and assign to the attribute noFlyZones
     */
    private void parseNoFlyZones() throws IOException, InterruptedException {
        var urlString = "http://localhost:" + App.port + "/buildings/no-fly-zones.geojson";
        var source = JsonParser.readJson(urlString);
        
        var fc = FeatureCollection.fromJson(source);
        var features = fc.features();
        
        noFlyZones = features;     
    }
}