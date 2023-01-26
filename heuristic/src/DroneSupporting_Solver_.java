import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DroneSupporting_Solver_ extends TrivalSolver{
    Drone[] drones;    //drone and courier are per Solution resources use as memory to instantiate solution
    HashMap<Node, MeetPoint> meetPointsMap; /* just for recovery the meetpoint information from Solution: 
                                            the drone meet with which courier at supplyNode */
    
    public DroneSupporting_Solver_(Orders orders, Nodes nodes, Objfunction f, Courier[] courierList, Drone[] droneList) {
        super(orders, nodes, f, courierList);
        meetPointsMap = new HashMap<>();
        this.drones = droneList;
    }

    // void attachMeetNode(Node n, Courier c, Drone d) {
    //     n.isMeet = true;
    //     n.meetCourier = c;
    //     n.meetDrone = d;
    //     // MeetPoint mp = new MeetPoint(c, d, n);
    //     // meetPointsMap.put(n, mp)
    // }

    // void detachMeetNode(Node n) {
    //     n.isMeet = false;
    //     n.meetCourier = null;
    //     n.meetDrone = null;
    // }


    @Override
    public void recoverFromSolution(Solution solution) {
        /* Nodes和Orders 中大部分信息可分为两类，一类是固定不变的元信息，另一类是用来initial solution的信息。
            但注意 Nodes中的 isMeet 和 meetCourier, meetDrone 是与解相关的信息，理想的话应该与Node解耦合，但是现在还没空做
            */
        //recover courier & resset nodes
        super.recoverFromSolution(solution);
        //recover drone
        if (solution.flightSeqs == null) {
            for (int i = 0; i < drones.length; i ++) {
                //preserve dummy flight
                Node startPosition = drones[i].flights.get(0).launchNode;
                Flight dummpyFlight = new Flight(startPosition, startPosition);
                drones[i].flights.clear();
                //preserve dummy flight
                drones[i].flights = new ArrayList<Flight>();
                drones[i].flights.add(dummpyFlight);
            }
            meetPointsMap = new HashMap<>();
            return;
        }
        //recover meetNode
        assert solution.meetPoints != null;
        meetPointsMap = new HashMap<>(solution.meetPoints);
        for (int i = 0; i < drones.length; i ++) {
            drones[i].flights = solution.deSerializeFlights(i);
            for (Flight f : drones[i].flights) {
                Node supplyNode = f.supplyNode;
                if (supplyNode != null) {
                    supplyNode.isMeet = true;
                    supplyNode.meetCourier = meetPointsMap.get(supplyNode).courier;  
                    //meetCourier need search to findout, or another data structure to log all meet nodes
                    supplyNode.meetDrone = drones[i];   
                }
            }
        }


    }

    @Override
    public void removeOrderFromCurrentStates(Order o) {
        for (Courier c : couriers) {
            switch (c.removeOrderFromRoute(o)) {
                case "removed_both":
                    return;            
                case "removed_cstm":  //drone resupply case //this search could be implemnted by add aonther MAP: order->meetNode
                    Node rstrNode = o.rstrNode;
                    for (Drone d : drones){  //In order to get removed meet node
                        for (int i = 0; i < d.flights.size(); i++) {
                            Flight f = d.flights.get(i);
                            if (f.pickupNode == rstrNode) {
                                /* try to optimize the transfer flight */
                                d.cancelResupplyFlight(i);
                                //TODO 还是需要一个单独的程序来合并transfer flight
                                //f.pickupNode = null;
                                meetPointsMap.remove(f.supplyNode);
                                f.supplyNode.isMeet = false;
                                //f.supplyNode = null;
                                return;
                            }
                        }        
                    }
                    Functions.printDebug("No matche result in removeOrder");
                case "removed_none":
                    continue;               
            }
            Functions.printAlert("No order matched in all drones and couriers!");
        }
        return;
    }

    /* 1. cancel transfer flight from self to self
     * 2. combine two consequent transfer flight if possible */
    public boolean optimizeTransferFlight() {
        
        return false;
    }

    @Override
    void instantiateSolution(){
        recoverFromSolution(globalOptSolution);
        instantiateSolution_d();
        System.out.println("miniObjValue: " + this.ObjfValue()); //debug
    }

    boolean instantiateSolution_d(){
        int count = 0; 
        resetStates();
        // Functions.printRouteSeq(couriers[0].routeSeq);
        // Functions.printFlights(drones[0].flights);
        while(!initializeDone()) {
            // Functions.printDebug("count:" + count);
            if(count ++ > 10) {
                return false;
            }
            for (Courier c : couriers) 
                instantiate_1c(c);
            for (Drone d : drones) 
                instantiate_1d(d);
        }
        return true;
    }

    private boolean  initializeDone() {
        for (Courier c : couriers) 
            if (!c.buildDone) 
                return false;
        return true;
    }

    boolean isMeet; //isDrone*Courier meet here
    Drone meetDrone;
    Courier meetCourier;

    private  void  resetStates() {
        /* reset all order, nodes to initial */
        for(int i = 0; i<orders.OrderList.length; i++) {
            orders.OrderList[i].reset_r();  //only reset the order but not the related node.
        }
        for(int i = 0; i<nodes.NodeList.length; i++) {
            nodes.NodeList[i].reset_r(); 
        }
        for (Drone d : drones) {
            d.reset();
            for (Flight f : d.flights) {
                f.reset(); 
            }
            
        }        for (Courier c : couriers) {
            c.reset();
        }
    }

    private Node instantiate_1d(Drone drone) {
        ArrayList<Flight> flights = drone.flights;
        if (drone.currFlight_id >= flights.size()) {
            return null;
        }
        Flight currFlight = flights.get(drone.currFlight_id);
        Node currMeetNode = currFlight.supplyNode;
        /* No supply node: This Flights is a transfer flight */
        if (currMeetNode == null) {
            drone.currFlight_id += 1;
            return null;
        }

        /* Courier has not yet initialized (courier always initialize first) */
        Courier courier = currMeetNode.meetCourier; 
        if (currMeetNode.T_courier == -1 || courier.position != currFlight.supplyNode) {  //Alternative: if (courier != currFlight.supplyNode) {
            // Functions.printDebug("drone meetNode: " + currMeetNode.id);
            // Functions.printFlights(flights);
            return currMeetNode;
        }

        /* Initialize the currentFlight */ 
        /* compute the earlist time of drone arrive meetNode */
        drone.time = drone.buildFlight(currMeetNode);  
        //这里不应该加这句，但是此处meetPointsMap也不应该为null) assert courier == meetPointsMap.get(currFlight.supplyNode).courier; //only for check bug
        assert courier.time == currFlight.supplyNode.T_courier; //only for check bug
        
        /* determine the meet time of meetNode */
        if (courier.time > drone.time) {    //courier arrive later
            //reversely build Flight by supplyTime and determine the gap time;
            drone.retroBuildFlight(courier.time);
            drone.time = courier.time;
            currMeetNode.courierWaitTime = 0; 
        }else{      //drone arrive later
            //add wait time at last node
            currMeetNode.courierWaitTime = drone.time - courier.time;  
            courier.time = drone.time;
        }
        currMeetNode.T_drone = drone.time;
        /*  update the order status.... tell the order it has been picked up;
        Not exactly the right place and right time (should be in 'buildFlight' & 'retroBuildFlight')
        but update here will not hurt because the pickup time is not important. 
        */
        Flight lastFlight = drone.flights.get(drone.currFlight_id - 1);
        orders.OrderList[lastFlight.pickupNode.orderNum].update(drone, lastFlight.pickupTime);
        return currMeetNode;
    }

    private Node instantiate_1c(Courier courier){        
        ArrayList<Node> routeSeq = courier.routeSeq;
        /* try to instantiate solution by courier route untill the first meet node */
        Node currNode = courier.position;      //temp variables to speedup the program
        Order currOrder = null;    //temp variables

        //deal with already reach the end
        if (courier.buildDone == true || currNode == routeSeq.get(routeSeq.size() - 1)) {
            courier.buildDone = true;
            return null;
        }
        //deal with meetNode /* if drone has not yet initialized */
        if (currNode.isMeet && currNode.T_drone == -1){
            // Functions.printDebug("courier meetNode: " + currNode.id);
            // Functions.printRouteSeq(courier.routeSeq);
            return currNode;
        }
        
        //下面这块儿有问题，导致在meetNode不能正确地update order：
        //continue to initiate
        for(int i =  routeSeq.indexOf(currNode) + 1; i<routeSeq.size(); i++) {  //start after the startnode
            currNode = routeSeq.get(i);

            /*  compute the earlist time of courier arrive meetNode */
            if(currNode.orderNum != -1){    //if it is an order node, update the order
                currOrder = orders.OrderList[currNode.orderNum];
                courier.time = earlistExecuteTime(currOrder, courier); //Because during a feasible solution route, it always pickup first.
                currOrder.update(courier, courier.time); 
            }else{                          //if it is not an order node, don't need update the order
                courier.time = earlistExecuteTime(currNode, courier);
            }
            currNode.T_courier = courier.time;
            courier.position = currNode;
            /* currNode is a meetNode */
            if (currNode.isMeet) {  
                return currNode;
            }else{  /* currNode is not a meetNode */
                currNode.courierWaitTime = 0;
                currNode.T_courier = courier.time;
            }      
        }
        
        return null;
    }

    void instantiateSolution_d_one(Courier courier){
        ArrayList<Node> routeSeq = courier.routeSeq;
        /* try to instantiate solution by courier route untill the first meet node */
        Node currNode;      //temp variables to speedup the program
        Order currOrder = null;    //temp variables
        courier.position  = routeSeq.get(0); //deal      with startnode
        courier.time = 0; 
        for (Drone d : drones) {
            d.time = 0;
        }
        for(int i = 1; i<routeSeq.size(); i++) {  //start after the startnode
            currNode = routeSeq.get(i);

            /*  compute the earlist time of courier arrive meetNode */
            if(currNode.orderNum != -1){    //if it is an order node, update the order
                currOrder = orders.OrderList[currNode.orderNum];
                courier.time = earlistExecuteTime(currOrder, courier); //Because during a feasible solution route, it always pickup first.
            }else{                          //if it is not an order node, don't need update the order
                courier.time = earlistExecuteTime(currNode, courier);
            }
            currNode.T_courier = courier.time;
            
            /* currNode is a meetNode */
            if (currNode.isMeet) {  
                //debug:: //Functions.printDebug("meetNode! Node:" + currNode.id);

                Drone drone = currNode.meetDrone;
                /*  compute the earlist time of drone arrive meetNode */
                drone.time = drone.buildFlight(currNode); 

                /* determine the meet time of meetNode */
                if (courier.time > drone.time) {    //courier arrive later
                    //reversely build Flight by supplyTime and determine the gap time;
                    drone.retroBuildFlight(courier.time);
                    drone.time = courier.time;
                    currNode.courierWaitTime = 0; 
                }else{      //drone arrive later
                    //add wait time at last node
                    currNode.courierWaitTime = drone.time - courier.time;  
                    courier.time = drone.time;
                }
                //update the node
                courier.position = currNode;
                currNode.T_drone = drone.time;
                
                /*  update the order status.... tell the order it has been picked up;
                Not exactly the right place and right time (should be in 'buildFlight' & 'retroBuildFlight')
                but update here will not hurt because the pickup time is not important. 
                */
                orders.OrderList[drone.flights.get(drone.currFlight_id - 1).pickupNode.orderNum].update(drone, -1);


            }else{  /* currNode is not a meetNode */
                currNode.courierWaitTime = 0;
                courier.position = currNode;
                currNode.T_courier = courier.time;
            }      
            //update the order
            if(currNode.orderNum != -1){ 
                //System.out.println("a"); //debug::
                currOrder.update(courier, courier.time); 
            }
        }
    }


    
    @Override
    public void printSolution(){
        printSolution(globalOptSolution);
    } 

    public void printSolution(Solution s){
        recoverFromSolution(s);
        instantiateSolution_d();
        printSolution_Courier(s);
        printSolution_Flight(s);
        System.out.println("ObjF: " + ObjfValue());  
    } 

}


class MeetPoint {
    Courier courier;
    Drone drone;
    Node meetNode;

    public MeetPoint(){};
    public MeetPoint(Courier c, Drone d, Node n) {
        courier = c;
        drone = d;
        meetNode = n;
    }
}
