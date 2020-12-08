package uk.ac.ed.inf.aqmaps;

//import java.io.IOException;
//import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Aqmaps App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    public void testPositionConstructor() {
        assertTrue( new Position(-3.188396, 55.944425) != null );
    }

    final Position p0 = new Position(-3.188396, 55.944425);

    public void testPositionLatitude() {
        assertTrue(p0.getLat() == 55.944425);
    }

    public void testPositionLongitude() {
        assertTrue(p0.getLng() == -3.188396);
    }
    
    public void testCentreInPlayArea() {
        assertTrue(p0.inConfinementArea());
    }
    
    public void testBoundaryOutOfPlayArea() {
        Position pos = new Position(-3.192473, 55.946233);
        assertFalse(pos.inConfinementArea());
    }
    
    public void testPositionOutOfPlayArea() {
        Position pos = new Position(-3.192000, 55.942000);
        assertFalse(pos.inConfinementArea());
    }
    
    boolean approxEq(double d0, double d1) {
        final double epsilon = 1.0E-12d;
        return Math.abs(d0 - d1) < epsilon;
    }
    
    boolean approxEq(Position p0, Position p1) {
        return approxEq(p0.getLat(), p1.getLat()) && approxEq(p0.getLng(), p1.getLng()); 
    }
    
    public void testNextPositionNotIdentity() {
        Position p1 = p0.nextPosition(90);
        assertFalse(approxEq(p0, p1));
    }
    
    public void testNorthThenSouth() {
        Position p1 = p0.nextPosition(90);
        Position p2 = p1.nextPosition(270);
        assertTrue(approxEq(p0, p2));
    }
    
    public void testEastThenWest() {
        Position p1 = p0.nextPosition(0);
        Position p2 = p1.nextPosition(180);
        assertTrue(approxEq(p0, p2));
    }
    
    public void testNorthSouthEastWest() {
        Position p1 = p0.nextPosition(90);
        Position p2 = p1.nextPosition(270);
        Position p3 = p2.nextPosition(0);
        Position p4 = p3.nextPosition(180);
        assertTrue(approxEq(p0, p4));
    }
    
    public void testNEisNorthEast() {
        Position p1 = p0.nextPosition(10);
        assertTrue(p1.getLat() > p0.getLat() && p1.getLng() > p0.getLng());
    }
    
    public void testSWisSouthWest() {
        Position p1 = p0.nextPosition(190);
        assertTrue(p1.getLat() < p0.getLat() && p1.getLng() < p0.getLng());
    }
    
    public void testNEthenSW() {
        Position p1 = p0.nextPosition(10);
        Position p2 = p1.nextPosition(190);
        assertTrue(approxEq(p0, p2));
    }
    
//    /**
//     * Rigourous Test :-)
//     * @throws InterruptedException 
//     * @throws IOException 
//     */
//    public void testApp() throws IOException, InterruptedException
//    {
//        String[] days = new String[] {"01", "02", "03", "04", "05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28"};
//        String[] months = new String[] {"01", "02", "03", "04", "05","06","07","08","09","10","11","12"};
//        String[] years = new String[] {"2020", "2021"};
//        double startlat = 55.9444;//55.944425;
//        double startlng = -3.1878;//-3.188396;
//        int seed = 5679;
//        App.port = "80";
//        
//        // Generate the random seed
//        App.rnd = new Random(seed);
//        for (String day : days) {
//            for (String month : months) {
//                for (String year : years) {
//                    Map map = new Map(day, month, year); 
//                    Position initialPos = new Position(startlng, startlat);
//                    Drone drone = new Drone(map, initialPos);
//                    drone.droneSimulator();
//                    assertTrue(drone.getSensorsToVisit().size() == 0);
//                    assertTrue(drone.getCurrPos().closeToStart(initialPos));
//                }
//            }
//        }
//    }
//    
//    public void testApp29_30() throws IOException, InterruptedException
//    {
//        String[] days = new String[] {"29","30"};
//        String[] months = new String[] {"01", "03", "04", "05","06","07","08","09","10","11","12"};
//        String[] years = new String[] {"2020", "2021"};
//        double startlat = 55.944425;
//        double startlng = -3.188396;
//        int seed = 5679;
//        App.port = "80";
//        
//        // Generate the random seed
//        App.rnd = new Random(seed);
//        for (String day : days) {
//            for (String month : months) {
//                for (String year : years) {
//                    Map map = new Map(day, month, year); 
//                    Position initialPos = new Position(startlng, startlat);
//                    Drone drone = new Drone(map, initialPos);
//                    drone.droneSimulator();
//                    assertTrue(drone.getSensorsToVisit().size() == 0);
//                    assertTrue(drone.getCurrPos().closeToStart(initialPos));
//                }
//            }
//        }
//    }
//    
//    public void testApp31() throws IOException, InterruptedException
//    {
//        String[] days = new String[] {"31"};
//        String[] months = new String[] {"01", "03", "05","07","08","10","12"};
//        String[] years = new String[] {"2020", "2021"};
//        double startlat = 55.944425;
//        double startlng = -3.188396;
//        int seed = 5679;
//        App.port = "80";
//        
//        // Generate the random seed
//        App.rnd = new Random(seed);
//        for (String day : days) {
//            for (String month : months) {
//                for (String year : years) {
//                    Map map = new Map(day, month, year); 
//                    Position initialPos = new Position(startlng, startlat);
//                    Drone drone = new Drone(map, initialPos);
//                    drone.droneSimulator();
//                    assertTrue(drone.getSensorsToVisit().size() == 0);
//                    assertTrue(drone.getCurrPos().closeToStart(initialPos));
//                }
//            }
//        }
//    }
}
