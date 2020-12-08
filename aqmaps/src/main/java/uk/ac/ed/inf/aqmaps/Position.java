package uk.ac.ed.inf.aqmaps;

public class Position {
    
    // Attributes are final because they should be immutable
    private final double lng;
    private final double lat;
    
    public Position(double lng, double lat) {
        this.lat = lat;
        this.lng = lng;                
    }
    
    public double getLng() {
        return lng;
    }
    
    public double getLat() {
        return lat;
    }
    
    /**
     * Calculates the direction to move to the destination position
     * 
     * @param destination - the position where drone will move towards
     * @return direction of movement
     */
    public int getDirection(Position destination) {
        
        // Calculate the change in latitude and longitude
        double opp = destination.lat - lat;
        double adj = destination.lng - lng;
        
        // Calculate the angle using arctan
        double radian = Math.atan2(opp,adj);
        
        // Handles negative values to suit the coursework's definition of direction
        if (radian < 0) {
            radian += 2 * Math.PI;
        }        
        
        // Drone can only be sent in a direction which is a multiple of 10 degrees
        double degree = Math.toDegrees(radian);
        var nearestDeg = (int) Math.round(degree/10) * 10;
        
        // 360 degrees is not allowed, hence assign (equivalent) 0 degrees
        if (nearestDeg == 360) nearestDeg = 0;
        
        return nearestDeg;
    }
    
    /**
     * Calculates the next position of the drone given the direction
     * 
     * @param direction - direction of movement
     * @return next position of the drone after moving towards the given direction
     */
    public Position nextPosition(int direction) {
        
        // Each movement by a drone is of length 0.0003 degrees
        double r = 0.0003;
        
        // Calculate the new longitude and latitude using planar trigonometry
        double newLat = lat + r * Math.sin(Math.toRadians(direction));
        double newLng = lng + r * Math.cos(Math.toRadians(direction));      
        return new Position(newLng, newLat);
    }
    
    /**
     * Calculates euclidean distance between current position and the input position
     * 
     * @param pos - position object (i.e. a sensor's position)
     * @return distance of a position object from another position object
     */
    public double distanceBetween(Position pos) {
        return Math.sqrt(Math.pow(pos.lng-lng, 2) + Math.pow(pos.lat-lat, 2));
    }
    
    /**
     * Checks whether a sensor is within distance of a drone
     * i.e. within 0.0002 degrees from the drone's position
     * 
     * @param pos - position object (i.e. a sensor's position)
     * @return true if the sensor is within distance, false otherwise
     */
    public boolean withinDistance(Position pos) {
        double distance = distanceBetween(pos);
        return distance < 0.0002;
    }
    
    /**
     * Checks whether a drone returns close to its initial position
     * i.e. within 0.0003 degrees from the initial position
     * 
     * @param pos - position object (i.e. initial position of drone)
     * @return true if the drone is close to its initial position, false otherwise
     */
    public boolean closeToStart(Position pos) {
        double distance = distanceBetween(pos);
        return distance < 0.0003;
    }
    
    // Method to check whether the drone is within the confinement area
    /**
     * Checks whether a drone is within the confinement area
     * 
     * @return true if the drone within confinement area, false otherwise
     */
    public boolean inConfinementArea() {
        boolean lng_check = lng > -3.192473 && lng < -3.184319;
        boolean lat_check = lat > 55.942617 && lat < 55.946233;
        return lng_check && lat_check; 
    }
}
