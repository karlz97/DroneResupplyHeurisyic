import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DroneSupporting_Solver_ extends TrivalSolver{
    Drone[] drones;    //drone and courier are per Solution resources use as memory to instantiate solution

    public DroneSupporting_Solver_(Orders orders, Nodes nodes, Objfunction f, Courier courier, Drone[] droneList) {
        super(orders, nodes, f, courier);
        this.drones = droneList;
    }

    void instantiateSolution_d(Courier courier){
        ArrayList<Node> routeSeq = courier.routeSeq;
        /* reset all order, nodes to initial */
        for(int i = 0; i<orders.OrderList.length; i++) {
            orders.OrderList[i].reset_r();  //only reset the order but not the related node.
        }
        for(int i = 0; i<nodes.NodeList.length; i++) {
            nodes.NodeList[i].reset_r(); 
        }
        for (Drone d : drones) {
            for (Flight f : d.flights) {
                f.reset(nodes); 
            }
            d.reset();
        }

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
        instantiateSolution_d(courier);
        System.out.println("miniObjValue: " + this.ObjfValue()); //debug
    }
    
    @Override
    public void printSolution(){
        printSolution(globalOptSolution);
    } 

    public void printSolution(Solution s){
        recoverFromSolution(s);
        instantiateSolution_d(courier);
        Functions.printSolution_Courier(s, orders);
        Functions.printSolution_Flight(s);
        System.out.println("ObjF: " + ObjfValue());  
    } 

    @Override
    public void recoverFromSolution(Solution solution) {
        /* Nodes和Orders 中大部分信息可分为两类，一类时固定不变的元信息，另一类是用来initial solution的信息。
            但注意 Nodes中的 isMeet 和 meetCourier, meetDrone 是与解相关的信息，理想的话应该与Node解耦合，但是现在还没空做
            */
        nodes.reset();
        courier.routeSeq = new ArrayList<>(solution.courierRoute);
        if (solution.flightSeqs == null) {
            for (int i = 0; i < drones.length; i ++) {
                drones[i].flights.clear();;
            }
            return;
        }

        for (int i = 0; i < drones.length; i ++) {
            drones[i].flights = solution.deSerializeFlights(i);
            for (Flight f : drones[i].flights) {
                if (f.supplyNode != null) {
                    f.supplyNode.isMeet = true;
                    f.supplyNode.meetCourier = this.courier; 
                    //meetCourier need search to findout, or another data structure to log all meet nodes
                    f.supplyNode.meetDrone = drones[i];   
                }
            }
        }

    }

}
