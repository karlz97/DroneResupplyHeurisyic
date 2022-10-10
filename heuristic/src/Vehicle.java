import java.util.ArrayList;
import java.util.LinkedList;

abstract class Vehicle {
    int id;
    double speed;
    Node position;
    double time;
    Double[][] distanceMatrix;

    
    public Vehicle(int id, Node startPosition){
        this.id = id;
        //this.speed = speed;
        this.time = 0;
        this.position = startPosition; 
    }

    abstract public String removeOrderFromRoute(Order order);

    public double callNodeDistance(Node node1 ,Node node2){
        if(node1 == node2)
            return 0;
        return distanceMatrix[node1.id][node2.id];
    }

    // ArrayList<Route> routeSeq //方便起见没有再创类，免得每次使用还得初始化
}



class Courier extends Vehicle{
    ArrayList<Node> routeSeq; //直接引用NodeList里面的Node不用初始化
    ArrayList<Integer> timeSeq; 
    public Courier(int id, Node startPosition, Double[][] distanceMatrix){
        super(id, startPosition);
        this.distanceMatrix = distanceMatrix;
        routeSeq = new ArrayList<Node>();
        routeSeq.add(position); //将起始点加入
        timeSeq = new ArrayList<Integer>();
    }

    @Override
    public String removeOrderFromRoute(Order order) {
        Node rstrNode = order.rstrNode;
        Node cstmNode = order.cstmNode;
        boolean rm_rstr = routeSeq.remove(rstrNode);
        boolean rm_cstm = routeSeq.remove(cstmNode);
        if (rm_cstm && rm_rstr) 
            return "removed_both";

        if (rm_cstm) 
            return "removed_cstm";
        
        if (rm_rstr) 
            return "removed_rstr";

        return "removed_none";
    }
}

class Drone extends Vehicle{
    final int MAXFILGHT = 50;
    ArrayList<Flight> flights;
    int currFlight_id = 0; //mainly used to buildFlight(determine the arrive time of each node)
    LinkedList<Node>[] feasibleSupplySet;
    LinkedList<Node>[] feasibleTransferSet;
    LinkedList<Node>[][] feasibleLandSet;

    public Drone(int id, Node startPosition, Double[][] distanceMatrix){
        super(id, startPosition);
        flights = new ArrayList<Flight>();
        this.distanceMatrix = distanceMatrix;
    }

    @Override
    public String removeOrderFromRoute(Order order) {
        Node cstmNode = order.cstmNode;
        boolean isRemoved = false;
        for (Flight flight : flights) {
            if (flight.remove(cstmNode)) 
                isRemoved = true;
            if (isRemoved) {
                break;
            }
        }
        if (isRemoved) 
            return "removed_cstm";
        
        return "removed_none";
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
        boolean doneMeetNode = false;
        while (!doneMeetNode) {
            // /* check */
            // if (currFlight.launchNode != position) {
            //     Functions.printAlert("in Drone.buildFlight: currFlight.launchNode != position");   
            //     return -1;
            // }
            if (currFlight.supplyNode == meetNode) {
                doneMeetNode = true;
            }
            currFlight.launchTime = this.time;
            if (currFlight.pickupNode == null) { //this is a transfer flight
                currFlight.pickupTime = currFlight.launchTime;
                currFlight.supplyTime = currFlight.pickupTime;
                currFlight.landTime = currFlight.supplyTime 
                    + callNodeDistance(currFlight.launchNode,currFlight.landNode);
            } else {
                currFlight.pickupTime = currFlight.launchTime 
                    + callNodeDistance(currFlight.launchNode,currFlight.pickupNode);
                currFlight.supplyTime = currFlight.pickupTime 
                    + callNodeDistance(currFlight.pickupNode,currFlight.supplyNode);
                currFlight.landTime = currFlight.supplyTime 
                    + callNodeDistance(currFlight.supplyNode,currFlight.landNode);
            }

            currFlight.hasBuilt = true;                
            currFlight_id++;
            if (currFlight_id < flights.size()) {
                currFlight = flights.get(currFlight_id);
            } else {
                if (!doneMeetNode) 
                    Functions.printAlert("No meet node in <buildFlight>");
                break;
            }
            
        }
        return currFlight.supplyTime;
    }

    /* reversely build the current flight, update the gap time */
    void retroBuildFlight(double meetTime){
        currFlight_id--;
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
        currFlight_id++;
    }

    public void computeFeasibleFlight(Nodes nodes) {
        //unsafe way to use generic array
        feasibleSupplySet = new LinkedList[nodes.numOfNodes];
        feasibleTransferSet = new LinkedList[nodes.numOfNodes];
        feasibleLandSet = new LinkedList[nodes.numOfNodes][nodes.numOfNodes];

        //cancel the seting of dummy node(drone base)
        Node[] nodeList = nodes.NodeList;
        for (int i1 = 0; i1 < nodeList.length; i1++) {
            Node n1 = nodeList[i1];
            if (!n1.isDrbs) {
                continue;
            }
            
            for (int i2 = 0; i2 < nodeList.length; i2++) {
                Node n2 = nodeList[i2];
                Double dist1 = callNodeDistance(n1, n2);
                if (!n2.isSply || dist1 > this.MAXFILGHT) {
                    //Functions.printDebug("[level 1] continued, n2.isSply:"+ n2.isSply + ", dist1:" + dist1); 
                    continue;
                }
                for (int j = 0; j < nodeList.length; j++) {
                    Node m = nodeList[j];
                    Double dist2 = callNodeDistance(n2, m) + dist1;
                    if (!m.isDrbs || dist2 + dist1 > this.MAXFILGHT) {
                        //Functions.printDebug("[level 2] continued, n2.isSply:"+ n2.isSply + ", dist1+2:" + (dist1 + dist2)); //TODO
                        continue;
                    }
                    //Functions.printAlert("Not continued, n2.isSply:"+ n2.isSply + ", dist1+2:" + (dist1 + dist2)); 
                    if (feasibleSupplySet[n1.id] == null) {
                        feasibleSupplySet[n1.id] = new LinkedList<Node>();                
                    }
                    if (feasibleLandSet[n1.id][n2.id] == null) {
                        feasibleLandSet[n1.id][n2.id] = new LinkedList<Node>();                
                    }
                    feasibleSupplySet[n1.id].add(n2);
                    feasibleLandSet[n1.id][n2.id].add(m);
                }
            }
            
            for (int j = 0; j < nodeList.length; j++) {
                Node m = nodeList[j];
                Double dist = callNodeDistance(n1, m);
                if (!m.isDrbs || dist > this.MAXFILGHT) {
                    continue;
                }
                if (feasibleTransferSet[n1.id] == null) {
                    feasibleTransferSet[n1.id] = new LinkedList<Node>();                
                }
                feasibleTransferSet[n1.id].add(m);
            }
            
        }
    } 

    public void showFeasibleFlight() {
        System.out.println("feasibleSupplySet:");
        for (int i = 0; i < feasibleSupplySet.length; i++) {
            System.out.print("[" + i + "]:");
            if (feasibleSupplySet[i] != null)
                for (Node n : feasibleSupplySet[i])
                    System.out.print(n.id + ",");
            System.out.println();
        }
    }

    public void geteasibleFlight(Drone drone) {
        this.feasibleSupplySet = drone.feasibleSupplySet;
        this.feasibleLandSet = drone.feasibleLandSet;
        this.feasibleTransferSet = drone.feasibleTransferSet;
    } 

}

class Flight{
    //Flight defines as from one resupply node to another resupply node; not a drone base to another dorne base.
    boolean hasBuilt;
    double gapTime; //time between currently launch and last flight land.
    Node launchNode; double launchTime;
    Node pickupNode; double pickupTime;
    //Order order; 
    Node supplyNode;  double supplyTime;
    Node landNode;   double landTime;

    public Flight() {
    }

    public Flight(Node launchNode, Node landNode){
        this.hasBuilt = false;
        this.gapTime = 0;
        this.launchNode = launchNode;
        this.pickupNode = null;
        this.supplyNode = null;
        this.landNode = landNode;
    }

    public Flight(Node launchNode, Node pickupNode, 
            Node supplyNode, Node landNode){
        this.hasBuilt = false;
        this.launchNode = launchNode;
        this.pickupNode = pickupNode;
        this.supplyNode = supplyNode;
        this.landNode = landNode;
        
    }

    public boolean remove(Node node) {
        if (this.pickupNode == node) {
            pickupNode = null;
            supplyNode.isMeet = false;
            supplyNode = null;
            return true;
        }
        return false;
    }

    public void reset() {
        this.hasBuilt = false;
        this.launchTime = -1;
        this.pickupTime = -1;
        this.supplyTime = -1;
        this.landTime = -1;
        this.gapTime = 0;
        //supplyNode.isMeet = false;
    }


}


/*
private class Route {

} 
*/

