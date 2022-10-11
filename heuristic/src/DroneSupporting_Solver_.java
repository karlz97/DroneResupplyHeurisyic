import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class DroneSupporting_Solver_ extends TrivalSolver{
    Drone[] drones;    //drone and courier are per Solution resources use as memory to instantiate solution
    HashMap<Node, MeetPoint> meetPointsMap;
    
    public DroneSupporting_Solver_(Orders orders, Nodes nodes, Objfunction f, Courier[] courierList, Drone[] droneList) {
        super(orders, nodes, f, courierList);
        this.drones = droneList;
    }

    @Override
    public void recoverFromSolution(Solution solution) {
        /* Nodes和Orders 中大部分信息可分为两类，一类时固定不变的元信息，另一类是用来initial solution的信息。
            但注意 Nodes中的 isMeet 和 meetCourier, meetDrone 是与解相关的信息，理想的话应该与Node解耦合，但是现在还没空做
            */
        //recover courier & resset nodes
        super.recoverFromSolution(solution);
        //recover drone
        if (solution.flightSeqs == null) {
            for (int i = 0; i < drones.length; i ++) {
                drones[i].flights.clear();
            }
            return;
        }
        //recover meetNode
        for (int i = 0; i < drones.length; i ++) {
            drones[i].flights = solution.deSerializeFlights(i);
            for (Flight f : drones[i].flights) {
                Node supplyNode = f.supplyNode;
                if (supplyNode != null) {
                    supplyNode.isMeet = true;
                    supplyNode.meetCourier = solution.meetPoints.get(supplyNode).courier;  //TODO
                    //meetCourier need search to findout, or another data structure to log all meet nodes
                    supplyNode.meetDrone = drones[i];   
                }
            }
        }

    }

    @Override
    public void removeOrderFromCurrentStates(Order order) {
        for (int i = 0; i < couriers.length; i++) {
            switch (couriers[i].removeOrderFromRoute(order)) {
                case "removed_both":
                    return;
                
                case "removed_cstm":
                    for (Drone d : drones)
                        if(d.removeOrderFromRoute(order) == "removed_cstm")
                            return;
                case "removed_none":
                    break;               
                default:
                    Functions.printDebug("No matche result in removeOrder");
                    break;
            }
            Functions.printAlert("No order matched in all drones and couriers!");
        }
        return;
    }

    void instantiateSolution_d(){
        resetStates();
        while(initializeDone()) {
            for (Courier c : couriers) 
                instantiate_1c(c);
            for (Drone d : drones) 
                instantiate_1d(d);
        }
    }

    private boolean  initializeDone() {
        for (Courier c : couriers) 
            if (!c.buildDone) 
                return false;
        return true;
    }


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
            
        }
        for (Courier c : couriers) {
            c.reset();
        }
    }

    private void instantiate_1d(Drone drone) {
        ArrayList<Flight> flights = drone.flights;
        if (drone.currFlight_id >= flights.size()) {
            return;
        }
        Flight currFlight = flights.get(drone.currFlight_id);
        Node currMeetNode = currFlight.supplyNode;
        /* No supply node: This Flights is a transfer flight */
        if (currMeetNode == null)
            return;
        /* Courier has not yet initialized (courier always initialize first) */
        if (currMeetNode.T_courier == -1)  //Alternative: if (currFlight.supplyNode.meetCourier.position != currFlight.supplyNode) {
            return;
        
        /* Initialize the currentFlight */ 
        /* compute the earlist time of drone arrive meetNode */
        drone.time = drone.buildFlight(currMeetNode);  
        Courier courier = currFlight.supplyNode.meetCourier; 
        assert courier == meetPointsMap.get(currFlight.supplyNode).courier; //only for check bug
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
    }

    private void instantiate_1c(Courier courier){        
        ArrayList<Node> routeSeq = courier.routeSeq;
        /* try to instantiate solution by courier route untill the first meet node */
        Node currNode = courier.position;      //temp variables to speedup the program
        Order currOrder = null;    //temp variables

        //deal with already reach the end
        if (courier.buildDone == true || currNode == routeSeq.get(routeSeq.size() - 1)) {
            courier.buildDone = true;
            return;
        }
        //deal with meetNode /* if drone has not yet initialized */
        if (currNode.isMeet && currNode.T_drone == -1)
            return;
        
        //continue to initiate
        for(int i =  routeSeq.indexOf(currNode) + 1; i<routeSeq.size(); i++) {  //start after the startnode
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
                return;
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
            courier.position = currNode;
        }
    }

    private void instantiateSolution_d_one(Courier courier){
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
    void instantiateSolution(){
        recoverFromSolution(globalOptSolution);
        instantiateSolution_d();
        System.out.println("miniObjValue: " + this.ObjfValue()); //debug
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
