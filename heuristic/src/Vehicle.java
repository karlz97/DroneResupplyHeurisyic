import java.util.ArrayList;
import javax.swing.text.Position;

abstract class Vehicle {
    int id;
    double speed;
    Node position;
    double time;
    public Vehicle(int id, double speed, Node startPosition){
        this.id = id;
        this.speed = speed;
        this.time = 0;
        this.position = startPosition; 
    }

    // ArrayList<Route> routeSeq //方便起见没有再创类，免得每次使用还得初始化
}



class Courier extends Vehicle{
    ArrayList<Node> routeSeq; //直接引用NodeList里面的Node不用初始化
    ArrayList<Integer> timeSeq; 
    public Courier(int id, double speed, Node startPosition){
        super(id, speed, startPosition);
        routeSeq = new ArrayList<Node>();
        routeSeq.add(position); //将起始点加入
        timeSeq = new ArrayList<Integer>();
    }
}

class Drone extends Vehicle{
    final int MAXFILGHT = 10;
    double meetTime;
    ArrayList<Flight> flights;
    int currFlight_id = 0;
    Double[][] distanceMatrix;

    public Drone(int id, double speed, Node startPosition, Double[][] distanceMatrix){
        super(id, speed, startPosition);
        flights = new ArrayList<Flight>();
        this.distanceMatrix = distanceMatrix;
    }

    void reset(){
        this.currFlight_id = 0;
    }

    
    /* This will build all flights from currflight_id untill meet the meetnode 
        build launchTime...(etc)...landTime, set flag: hasBuilt. 
        >warning if the meetnode is not exist in flight.supplyNode
        >warning if the meetnode is exist unbuild supplyFlight before meetNode 
        return the earlist drone arriveTime of supplyNode */
    double buildFlight(Node meetNode){
        Flight currFlight = flights.get(currFlight_id);
        while (currFlight.supplyNode != meetNode) {
            /* check */
            if (currFlight.launchNode != position) {
                Functions.printAlert("in Drone.buildFlight: currFlight.launchNode != position");   
                return -1;
            }

            currFlight.launchTime = this.time;
            currFlight.pickupTime = currFlight.launchTime 
                 + callNodeDistance(currFlight.launchNode,currFlight.pickupNode);
            currFlight.supplyTime = currFlight.pickupTime 
                + callNodeDistance(currFlight.pickupNode,currFlight.supplyNode);
            currFlight.landTime = currFlight.supplyTime 
                + callNodeDistance(currFlight.supplyNode,currFlight.landNode);
            currFlight.hasBuilt = true;                
            currFlight_id++;
            currFlight = flights.get(currFlight_id);
        }

        currFlight.launchTime = this.time;
        currFlight.pickupTime = currFlight.launchTime 
            + callNodeDistance(currFlight.launchNode,currFlight.pickupNode);
        currFlight.supplyTime = currFlight.pickupTime 
            + callNodeDistance(currFlight.pickupNode,currFlight.supplyNode);
        currFlight.landTime = currFlight.supplyTime 
            + callNodeDistance(currFlight.supplyNode,currFlight.landNode);
        currFlight.hasBuilt = true;    
        currFlight_id++;
        return currFlight.supplyTime;
    }


    /* reversely build the current flight, update the gap time */
    void retroBuildFlight(double meetTime){
        // assume
        Flight currFlight = flights.get(currFlight_id);
        
        /* found the meetFlight_id, update the gapTime(waitTime) of flights  */
        currFlight.supplyTime = meetTime;
        currFlight.landTime = currFlight.supplyTime
            + callNodeDistance(currFlight.supplyNode,currFlight.landNode);
        currFlight.pickupTime = currFlight.supplyTime
            - callNodeDistance(currFlight.pickupNode ,currFlight.supplyNode);
        currFlight.launchTime = currFlight.pickupTime
            - callNodeDistance(currFlight.launchNode, currFlight.pickupNode);
        currFlight.hasBuilt = true;

        double lastLandTime;
        if (currFlight_id != 0) {
            Flight lastFlight = flights.get(currFlight_id-1);
            lastLandTime = lastFlight.landTime;
        }else{
            lastLandTime = 0;
        }
        currFlight.gapTime = currFlight.launchTime - lastLandTime;
    }

    // void buildFlight_reverse(Node meetNode, double meetTime){
    //     int meetFlight_id = currFlight_id;
    //     Flight tempFlight = flights.get(meetFlight_id);

    //     /* find the meetNode_id */
    //     while (tempFlight.supplyNode != meetNode) {
    //         /* check */
    //         if (tempFlight.launchNode != position) {
    //             Functions.printAlert("in Drone.buildFlight: currFlight.launchNode != position");   
    //         }
    //         meetFlight_id++;
    //         tempFlight = flights.get(meetFlight_id);
    //     }

    //     /* find the meetFlight_id, update the gapTime(waitTime) of flights  */
    //     tempFlight.supplyTime = meetTime;
    //     tempFlight.landTime = tempFlight.supplyTime
    //         + callNodeDistance(tempFlight.supplyNode,tempFlight.landNode);
    //     tempFlight.pickupTime = tempFlight.supplyTime
    //         - callNodeDistance(tempFlight.pickupNode ,tempFlight.supplyNode);
    //     tempFlight.launchTime = tempFlight.pickupTime
    //         - callNodeDistance(tempFlight.launchNode, tempFlight.pickupNode);
    //     tempFlight.hasBuilt = true;

    //     double lastLandTime;
    //     if (meetFlight_id != 0) {
    //         Flight lastFlight = flights.get(meetFlight_id-1);
    //         lastLandTime = lastFlight.landTime;
    //     }else{
    //         lastLandTime = 0;
    //     }
    //     tempFlight.gapTime = tempFlight.launchTime - lastLandTime;
    // }


    public double callNodeDistance(Node node1 ,Node node2){
        return distanceMatrix[node1.id][node2.id];
    }

}

class Flight{
    boolean hasBuilt;
    double gapTime; //time between currently launch and last flight land.
    Node launchNode; double launchTime;
    Node pickupNode; double pickupTime;
    //Order order; 
    Node supplyNode;  double supplyTime;
    Node landNode;   double landTime;


    public Flight(Node launchNode, Node pickupNode, 
            Node supplyNode, Node landNode){
        this.hasBuilt = false;
        this.launchNode = launchNode;
        this.pickupNode = pickupNode;
        this.supplyNode = supplyNode;
        this.landNode = landNode;
        this.gapTime = 0;
    }

    public void reset(Nodes nodes) {
        this.hasBuilt = false;
        this.launchTime = -1;
        this.pickupTime = -1;
        this.supplyTime = -1;
        this.landTime = -1;
        this.gapTime = 0;
        nodes.NodeList[supplyNode.id].isMeet =true;
    }


}


/*
private class Route {

} 
*/

