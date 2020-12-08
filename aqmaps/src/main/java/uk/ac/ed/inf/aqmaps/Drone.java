package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {
 
    private Position currPos;
    private int moves;
    private Map map;
    
    // String to be written to the output .txt file
    private String log;
    
    // Sensor that the drone is trying to visit
    private Sensor targetSensor;
    
    // List of sensors to be visited by the drone
    private List<Sensor> sensorsToVisit = new ArrayList<Sensor>();
    
    // Final list of visited sensors
    private List<Sensor> visitedSensors = new ArrayList<>();
    
    // Flight path of the drone
    private List<Position> flightPath = new ArrayList<>();
    
    // Flight path directions of the drone
    private List<Integer> flightPathDirections = new ArrayList<>();
    
    public Drone(Map currMap, Position currPos) throws IOException, InterruptedException {
        this.map = currMap;
        this.currPos = currPos;
        this.moves = 150;
        this.log = "";
        this.sensorsToVisit = currMap.getSensors();
        this.targetSensor = this.closestSensor();
    } 
    
    // Used for testing
    public Position getCurrPos() {
        return currPos;
    }
    
    public String getLog() {
        return log;
    }
    
    public List<Sensor> getSensorsToVisit() {
        return sensorsToVisit;
    }
    
    public List<Sensor> getVisitedSensors() {
        return visitedSensors;
    }
    
    public List<Position> getFlightPath() {
        return flightPath;
    }
    
    public List<Integer> getFlightPathDirection() {
        return flightPathDirections;
    }
     
    /**
     * Calculates the distances of sensor from the drone and returns the sensor with smallest distance
     * 
     * @return The closest sensor
     */
    private Sensor closestSensor() throws IOException, InterruptedException {
        // Initialize closestDist to +inf
        double closestDist = 100000;
        Sensor closestSensor = null;
        
        var noOfSensors = sensorsToVisit.size();
        for (int i=0; i < noOfSensors; i++) {
            var currSensor = sensorsToVisit.get(i);
            var coord = currSensor.toPosition();
            double currDist = currPos.distanceBetween(coord);
            if (closestDist > currDist) {
                closestDist = currDist;
                closestSensor = currSensor;
            }
        }
        return closestSensor;
    }
    
    /**
     * Checks whether moving the drone from its current position to the next position will intersect with a building
     * 
     * @param building - a building that is a no fly zone
     * @param nextPos  - the intended next position of the drone
     * @return true if this drone movement intersect with a building, false otherwise
     */
    private boolean checkIntersect(List<Point> building, Position nextPos) {
        var intersect = false;
        var move = new Line2D.Double(currPos.getLng(), currPos.getLat(), nextPos.getLng(), nextPos.getLat());
        
        for (int i=0; i < building.size() - 1; i++) {
            var start = building.get(i);
            var end = building.get(i+1);
            var startPt = new Point2D.Double(start.longitude(), start.latitude());
            var endPt = new Point2D.Double(end.longitude(), end.latitude());
            var currLine = new Line2D.Double(startPt, endPt);
            intersect = move.intersectsLine(currLine);
            
            if (intersect) break;
        }       
        return intersect;
    }
  
    /**
     * Checks whether moving the drone from its current position to the next position will intersect with any buildings
     * 
     * @param nextPos  - the intended next position of the drone
     * @return true if this drone movement intersect with any building, false otherwise
     */
    private boolean checkIntersectForAllBuildings(Position nextPos) {
        var intersect = false;
        for (Feature f : map.getNoFlyZones()) {
            var p = (Polygon) f.geometry();
            var lp = p.coordinates(); // Always going to have only 1 List<Point>
            intersect = checkIntersect(lp.get(0), nextPos);
            if (intersect) break;
        }
        return intersect;
    }
    
    /**
     * Checks if the drone still has not used up the allowed number of moves
     * 
     * @return true if the drone has moves left (>0), false otherwise
     */
    private boolean hasMoves() {
        return moves > 0;
    }
    
    /**
     * Checks if the drone still has sensors to visit
     * 
     * @return true if the drone has sensors to visit, false otherwise
     */
    private boolean hasSensorsToVisit() {
        return sensorsToVisit.size() > 0;
    }
    
    /**
     * Moves the drone to the next position and updates the number of moves left
     * 
     * @param nextPos - the position that the drone will move to
     */
    private void move(Position nextPos) {
        currPos = nextPos;
        moves--; 
    }
    
    /**
     * Removes sensor from sensorsToVisit list and adds it to visitedSensors list
     * 
     */
    private void visit() {
        sensorsToVisit.remove(targetSensor);
        visitedSensors.add(targetSensor);
    }
    
    /**
     * Gets the direction for the drone where moving towards this direction will decrease the Euclidean distance
     * between the drone and the closest sensor, all while making sure the direction does not bring the drone into
     * a nofly zone and outside the drone confinement area
     * 
     * @param targetPos - destination position
     * @return the direction that brings the drone closest to the destination position
     */
    private int getBestDirection(Position targetPos) throws IOException, InterruptedException {
        // Get direction towards the closest sensor
        var direction = currPos.getDirection(targetPos);
        
        // Move to this direction
        var nextPos = currPos.nextPosition(direction);
        
        // If going towards this direction intersects buildings or is outside the play area, we need a new direction
        if (checkIntersectForAllBuildings(nextPos) || !nextPos.inConfinementArea()) {
            double minDistance = 100000;
            int index = -1;
            
            // Loop through all possible directions
            for (int i = 0; i < 360; i += 10) {
                
                // Try to move towards this direction
                nextPos = currPos.nextPosition(i);
                
                // If going towards this direction intersects with buildings or is outside play area, ignore this direction
                if(!checkIntersectForAllBuildings(nextPos) && nextPos.inConfinementArea()) {
                
                    // Calculate distance between this position and the closest sensor
                    var currDist = nextPos.distanceBetween(targetPos);
                    if (currDist < minDistance) {
                        minDistance = currDist;
                        index = i;
                    }
                }
            }
            return index;
        }
        
        return direction;
    }
    
    /**
     * Gets the next best direction: first, try getting the opposite of the input direction
     * If moving towards this direction enters no fly zones or leaves the confinement area,
     * get a valid random direction that is not equal to the input direction.
     * 
     * @param direction - current direction
     * @return next best direction
     */
    private int getAnotherDirection(int direction) {               
        var dir = oppositeDirection(direction);
        var nextPos = currPos.nextPosition(dir); 
        while (checkIntersectForAllBuildings(nextPos) || !nextPos.inConfinementArea() || dir == direction) {
            dir = App.rnd.nextInt(36) * 10;
            nextPos = currPos.nextPosition(dir);
        }
        return dir;
    }
    
    /**
     * Check whether or not the drone is stuck in a loop
     * 
     * @param currDir - drone's current direction
     * @return true if drone is stuck, false otherwise
     */
    private boolean checkStuck(int currDir) {
        var size = flightPathDirections.size();
        var last3Directions = new int[3];
        var ctr = 2;
        
        // Get the last three directions that the drone moved to
        for (int i = 0; i < 3; i++) {
            var curr = flightPathDirections.get(size-(1+i));
            last3Directions[ctr] = curr;
            ctr--;
        }
        
        var bool1 = last3Directions[0] == last3Directions[2]; 
        var bool2 = last3Directions[1] == currDir;
        var bool3 = oppositeDirection(last3Directions[0]) == last3Directions[1];
        var bool4 = oppositeDirection(last3Directions[2]) == currDir;
        
        if (bool1 && bool2 && bool3 && bool4) return true;
        else return false;
    }
     
    /**
     * Calculates the direction opposite to the input direction
     * 
     * @param direction - direction of the drone
     * @return direction opposite to the input 
     */
    private int oppositeDirection(int direction) {
        int opDir = -1;
        if (direction >= 0 && direction < 180) {
            opDir = direction + 180;
        } else {
            opDir = direction - 180;
        }
        return opDir;
    }
    
    /**
     * Print the drone's performance for debugging purposes
     */
    private void printDronePerformance(Position startPos) {
        System.out.println("-----------------------------------------------------------------");
        System.out.println("DATE: " + map.getDay() + "/" + map.getMonth() + "/" + map.getYear());
        System.out.println("UNVISITED SENSORS: " + sensorsToVisit.size());
        System.out.println("MOVES: " + (150-moves));
        boolean backToInitial = false;
        if (currPos.closeToStart(startPos)) backToInitial = true;
        System.out.println("IS IT BACK TO STARTING POSITION?: " + backToInitial);
    }
    
    /**
     * Drone control algorithm
     */
    public void droneSimulator() throws IOException, InterruptedException {
        // Initialize starting position and add to the flight path
        flightPath.add(currPos);
        
        // Get a copy of the initial position of the drone
        var startPos = currPos;
        
        // Counter for output .txt file
        var ctr = 1;
        
        var visitSensorThisMove = false;
        
        // Get position of closest sensor to the drone's current position
        var targetSensorPos = targetSensor.toPosition();
        
        while(hasMoves()) {
            if (hasSensorsToVisit()) {
                
                // If drone visits a sensor this move, find the next closest target sensor
                if (visitSensorThisMove) {
                    targetSensor = closestSensor();
                    targetSensorPos = targetSensor.toPosition();
                }
                
                // Get direction towards the target sensor and move to it
                var direction = getBestDirection(targetSensorPos);
                //System.out.println(direction);
                var nextPos = currPos.nextPosition(direction);
                
                // Check if moving to the next position causes the drone to be stuck
                if (flightPathDirections.size() >= 3) {
                    //int tryChangingSensors = 0;
                    if (checkStuck(direction)) {
                        direction = getAnotherDirection(direction);
                        nextPos = currPos.nextPosition(direction);
                    }
                }
                
                // Update drone's movement to log file
                log += ctr + "," + currPos.getLng() + "," + currPos.getLat() + "," + direction + "," + nextPos.getLng() + "," + nextPos.getLat() + ",";
                ctr++;
                
                move(nextPos);

                // Store the direction performed by the drone
                flightPathDirections.add(direction);
                
                // Store the position moved by the drone
                flightPath.add(nextPos);
                
                // If the target sensor is within distance to the drone, visit this sensor
                if (nextPos.withinDistance(targetSensorPos)) {
                    visitSensorThisMove = true;
                    visit();
                } else {
                    visitSensorThisMove = false;       
                    
                    // Check if we have another closer sensor and visit if within distance
                    targetSensor = closestSensor();
                    targetSensorPos = targetSensor.toPosition();
                    if (nextPos.withinDistance(targetSensorPos)) {
                        visitSensorThisMove = true;
                        visit();
                    }
                }
              
                // Update drone's sensor visit to log file
                var loc = "";
                if (visitSensorThisMove) loc = targetSensor.getLocation();
                else loc = "null";              
                log += loc + "\n";
                
            } 
            // If all sensors have been visited, go back to initial position
            else {
                // Get direction to start position
                var direction = getBestDirection(startPos);
                
                if (checkStuck(direction)) {
                    direction = getAnotherDirection(direction);
                }
                
                // Move towards this direction
                var nextPos = currPos.nextPosition(direction);
                
                log += ctr + "," + currPos.getLng() + "," + currPos.getLat() + "," + direction + "," + nextPos.getLng() + "," + nextPos.getLat() + ",null\n";
                ctr++;
                
                move(nextPos);
                flightPathDirections.add(direction);
                flightPath.add(nextPos); 
                
                // If drone is close to its starting position, exit while loop
                if (startPos.closeToStart(nextPos)) break;
            }
        }
        printDronePerformance(startPos);
    }
            
}
