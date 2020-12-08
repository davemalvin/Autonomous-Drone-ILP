package uk.ac.ed.inf.aqmaps;

public class SensorDetails {
    private String country;
    private Square square;
    private String nearestPlace;
    private Position coordinates;
    private String words;
    private String language;
    private String map;
   
    public static class Square {
        Position southwest;
        Position northeast;
    }
    
    public Position getCoordinates() {
        return coordinates;
    }   
}
