import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.text.Position;

class ResupplySolution extends TrivalSolution{
    Drone[] droneList;    //drone and courier are per Solution resources use as memory to instantiate solution
    Double[][] drone_distanceMatrix;
    public ResupplySolution(Orders orders, Nodes nodes, Objfunction f, Courier courier, Drone drone,
            Double[][] truckDistanceMatrix) {
        super(orders, nodes, f, courier, truckDistanceMatrix);
        this.drone = drone;
        this.drone_distanceMatrix = drone.distanceMatrix;
    }


    //@Override
    void instantiateSolution_d(ArrayList<Node> routeSeq, ArrayList<Flight> flights){
        /* reset all order, nodes to initial */
        for(int i = 0; i<orders.OrderList.length; i++) {
            orders.OrderList[i].reset_r();  //only reset the order but not the related node.
        }
        for(int i = 0; i<nodes.NodeList.length; i++) {
            nodes.NodeList[i].reset(); //reset the isMeet, recreate it below
        }
        for (Flight currflight : flights) {
            currflight.reset(nodes); 
        }
        for (Drone d : droneList) {
            d.reset();
        }

        /* try to instantiate solution by courier route untill the first meet node */
        Node currNode;      //temp variables to speedup the program
        Order currOrder;    //temp variables
        courier.position  = routeSeq.get(0); //deal      with startnode
        courier.time = 0;
        int i = 1; 
        for(; i<routeSeq.size(); i++) {  //start after the startnode
            currNode = routeSeq.get(i);
            /*  compute the earlist time of courier arrive meetNode */
            if(currNode.orderNum != -1){    //if it is an order node, update the order
                currOrder = orders.OrderList[currNode.orderNum];
                courier.time = earlistExecuteTime(currOrder, courier); //Because during a feasible solution route, it always pickup first.
                //currOrder.update(courier.time);  //For more structured progame and mode reuse, here is couples of overhead 
            }else{                          //if it is not an order node, don't need update the order
                courier.time = earlistExecuteTime(currNode, courier);
            }
            currNode.T_courier = courier.time;
            
            /* currNode is a meetNode */
            if (currNode.isMeet) {  
                Drone drone = currNode.meetDrone;
                /*  compute the earlist time of drone arrive meetNode */
                drone.meetTime = drone.buildFlight(currNode); 

                /* determine the meet time of meetNode */
                if (courier.time > drone.time) {    //courier arrive later
                    //reversely build Flight by supplyTime and determine the gap time;
                    drone.retroBuildFlight(courier.time);
                    currNode.courierWaitTime = 0; 
                }else{      //drone arrive later
                    //add wait time at last node
                    currNode.courierWaitTime = drone.time - courier.time;  
                    courier.time = drone.time;
                }
                //update the node
                courier.position = currNode;
                currNode.T_drone = drone.time;
            }else{  /* currNode is not a meetNode */
                currNode.courierWaitTime = 0;
                courier.position = currNode;
                currNode.T_courier = courier.time;
            }      
        }
    }
    
    /*              Heuristic               */
    
    /* LNS1
     * optimize the solution *after* genGreedySolution
     * --------------------- operator ---------------------
     * remove heuristic: Shaw removal //& Random removal  
     * insert heuristic: Regret heuristic
     * no dynamic weight adjustment of different heuristics.
     * 
     */

    public void LNS1r(int maxIteration){
        
    } 



    /*          Herusitics operators           */

    /*          destory operators               */
    /* Randomly remove a Flight and its following flights from a drone */
    void randomFlightRemovalOne(Drone drone){
        /* generate randomly choose a flight, remove it and its following flights */
        int r = rand.nextInt(drone.flights.size());
        //Add the picked node's and the subsequence nodes' corresbonding orders to the 'removedOrderList' 
        //+ remove the cooresbounding deliver node
        for (int j = r; j < drone.flights.size(); j++) {
            Flight f =  drone.flights.get(j);
            Node pickupNode =  f.pickupNode;
            if (pickupNode != null) {
                Order o = orders.OrderList[pickupNode.orderNum];
                // remove the corresponding delivery node
                this.removedOrderList.add(o);
                // remove the corresbounding delivery node
                Node deliveryNode = o.cstmNode;
                courier.routeSeq.remove(deliveryNode);
            }
        }
        //Remove the picked node and the subsequence nodes from flights
        drone.flights.subList(r, drone.flights.size() - 1).clear();
        return;
    }

    /* Randomly choose a flight and cancel its supply task */
    void randomSupplyRemovalOne(Drone drone){
        int r = rand.nextInt(drone.flights.size());
        Flight f =  drone.flights.get(r);
        Node pickupNode = f.pickupNode;
        if (pickupNode != null) {
            Order o = orders.OrderList[pickupNode.orderNum];
            //add order to removedOrderList
            this.removedOrderList.add(o);
            // remove the corresponding delivery node
            Node deliveryNode = o.cstmNode;
            courier.routeSeq.remove(deliveryNode);
            // remove the supply task from flight
            pickupNode = null;
            f.supplyNode = null;
        }
        return;
    }

    void removeUselessResupplyNode(Courier courier){
        ArrayList<Node> route = courier.routeSeq;
        for (int i = 0; i < route.size(); i++) {
            Node n = route.get(i);
            if (n.orderNum == -1 && !n.isMeet) {
                route.remove(i);
            }
        }
        return;
    }

    /*          repair operators            */


    /* Randomly choose an removed & 1st order-drone-feasible order: and greedily insert to */
    /* 1st order-drone-feasible means drone is at the restaurant, don't have to transfer */
    ArrayList<Order> randomSupplyFlightCreate_order(){      
        
        // find all removed & 1st order-drone-feasible orders 
        ArrayList<Order> feasibleOrderList = new ArrayList<>();
        for (Order o : removedOrderList) {
            if (o.rstrNode.isDrbs) {
                feasibleOrderList.add(o);
            }
        }
        
        // Randomly CHOOSE one order to resupply
        int r = rand.nextInt(feasibleOrderList.size());
        Order o = feasibleOrderList.get(r);
        removedOrderList.remove(o);  //remove the oreder from <reinsert list>

        Node pickupNode = o.rstrNode, supplyNode, landNode; //assume pickupNode == launchNode;
        Drone drone;
        for (Drone d : droneList) {
            if (d.position == pickupNode) {
                drone = d; //find the drone in that node
                break;
            }
        }    
        
        //TODO -------Built the supply flight, resupply it p-randomly to the node with cloest time
        
        //randomly supply the a node and randomly land a node, built the flight
        List<Node> feasibleSupplyList = drone.feasibleSupplySet[pickupNode.id];
        r = rand.nextInt(feasibleSupplyList.size());
        supplyNode = feasibleSupplyList.get(r);

        List<Node> feasibleLandList = drone.feasibleLandSet[pickupNode.id][supplyNode.id]
        r = rand.nextInt(feasibleLandList.size());
        landNode = feasibleLandList.get(r);

        Flight f = new Flight(pickupNode, pickupNode, supplyNode, landNode);
        drone.flights.add(f);
        //-----------------------------------------------------------
        drone.position = f.landNode;
        
    }

    ArrayList<Order> randomTransferFlightCreate(Drone drone, int removeNum){
        // find all feasible transfer Node for the drone 
        List<Node> feasibleTransferList = drone.feasibleTransferSet[drone.position];
        
        // Randomly CHOOSE one and built the transfer flight
        int r = rand.nextInt(feasibleTransferList.size());
        Node transferNode = feasibleTransferList.get(r);
        Flight f = new Flight(drone.position, transferNode);
        drone.flights.add(f);
        //----------------------------------------------------------
        drone.position = f.landNode;

        return null;
    }


    
    
}
