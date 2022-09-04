import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.text.Position;

class ResupplySolver extends TrivalSolver{
    Drone[] droneList;    //drone and courier are per Solution resources use as memory to instantiate solution

    public ResupplySolver(Orders orders, Nodes nodes, Objfunction f, Courier courier, Drone[] droneList,
            Double[][] truckDistanceMatrix) {
        super(orders, nodes, f, courier, truckDistanceMatrix);
        this.droneList = droneList;
    }


    void instantiateSolution_d(Courier courier){
        ArrayList<Node> routeSeq = courier.routeSeq;
        /* reset all order, nodes to initial */
        for(int i = 0; i<orders.OrderList.length; i++) {
            orders.OrderList[i].reset_r();  //only reset the order but not the related node.
        }
        for(int i = 0; i<nodes.NodeList.length; i++) {
            nodes.NodeList[i].reset_r(); //reset the isMeet, recreate it below
        }
        for (Drone d : droneList) {
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
        for(int i = 1; i<routeSeq.size(); i++) {  //start after the startnode
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
                //TODO debug::
                Functions.printDebug("meetNode!");

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
            //update the order
            if(currNode.orderNum != -1){ 
                currOrder.update(courier, courier.time); 
            }
        }
    }

    @Override
    void instantiateSolution(){
        instantiateSolution_d(courier);
        //instantiateSolution_t(courierGlobalRouteSeq);
    }
    
    @Override
    public void printSolution(){
        //instantiateSolution_t(courierGlobalRouteSeq);
        System.out.println("Routes: ");
        for (Iterator<Node> it = courierGlobalRouteSeq.iterator(); it.hasNext();) {
            System.out.print( it.next().id + " --> ");
            // debug::: System.out.print( it.next().id + "(" +  + ")" + " --> ");
        }
        if (orders.allDone()) {
            System.out.println("finished.");   
        }else{
            System.out.println("unfinieshed.");
        }
        System.out.println("ObjF: " + ObjfValue());
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

    public void LNS1r(int maxIteration, int sizeOfNeiborhood){
        
        /* 仅用于储存临时解，全局最优解在globalRouteSeq中 */
        ArrayList<Node> candidateRoute = new ArrayList<Node>(courier.routeSeq);   
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0;
        int r; Drone d;
        while (iter < maxIteration) {
            /* ------------ remove heuristic -------------- */
            /* remove the flight first; 1.remove the whole flight, 2.only canceled the fly task + remove useless transfer */
            //randomly choose one drone
            r = rand.nextInt(this.droneList.length);
            d = droneList[r];
            this.randomSupplyRemovalOne(d);

            /* remove the courier node if necessary */
            if(removedOrderList.size() < sizeOfNeiborhood) {
                this.randomRemoval(courier, sizeOfNeiborhood - removedOrderList.size());
            }

            /* ------------ insert heuristic -------------- */
            /* insert flight */
            this.randomSupplyFlightCreate_order();
            r = rand.nextInt(this.droneList.length);
            this.randomTransferFlightCreate(d);

            /* insert courier */
            this.regeretInsert(courier, removedOrderList, 3);

            /* instantiateSolution */
            this.instantiateSolution_d(courier);
            double tempObjValue = this.ObjfValue();


            /*  TODO accept solution with probability */
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                //candidateRoute = courier.routeSeq; 这样写会有bug，因为在下一轮while循环中 同样的 courier.routeSeq 会被 shawRemoval_fast
                candidateRoute.clear(); candidateRoute.addAll(courier.routeSeq);
                //System.out.println("miniObjValue: " + minObjfValue);
                Functions.printRouteSeq(candidateRoute);
            }
            iter++;
            /*  */
        }
        courierGlobalRouteSeq = candidateRoute;
        instantiateSolution();
    } 


    /*          optimize the flights           */
    /* find the better drone base between two flights */
    
    
    /* remove useless(loop) transfer */


    /* remove the useless pure resupply node for courier */
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
                // empty the meet related information of node
                f.supplyNode.reset();

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


        // update the status of drone
        drone.position = drone.flights.get(drone.flights.size() - 1).landNode;

        
        return;
    }

    /* Randomly choose a flight and cancel its supply task */
    void randomSupplyRemovalOne(Drone drone){
        if (drone.flights.size() == 0)
            return;
        int r = rand.nextInt(drone.flights.size());
        Flight f =  drone.flights.get(r);
        if (f.supplyNode != null) {
            f.supplyNode.reset();
        }
        if (f.pickupNode != null) {
            Order o = orders.OrderList[f.pickupNode.orderNum];
            //add order to removedOrderList
            this.removedOrderList.add(o);
            // remove the corresponding delivery node
            Node deliveryNode = o.cstmNode;
            courier.routeSeq.remove(deliveryNode);
            // remove the supply task from flight
            f.pickupNode = null;
            f.supplyNode = null;
        }

        //update the status of drone
        drone.position = drone.flights.get(drone.flights.size() - 1).landNode;
        return;
    }



    /*          repair operators            */


    /* Randomly choose an removed & 1st order-drone-feasible order: and greedily insert to */
    /* 1st order-drone-feasible means drone is at the restaurant, don't have to transfer */
    void randomSupplyFlightCreate_order(){      
        
        // find all removed & 1st order-drone-feasible orders 
        ArrayList<Order> feasibleOrderList = new ArrayList<>();
        for (Order o : removedOrderList) {
            if (o.rstrNode.isDrbs) {
                feasibleOrderList.add(o);
            }
        }
        
        // Randomly CHOOSE one order to resupply
        if (feasibleOrderList.size() == 0)
            return;

        int r = rand.nextInt(feasibleOrderList.size());
        Order o = feasibleOrderList.get(r);
        removedOrderList.remove(o);  //remove the oreder from <reinsert list>

        Node pickupNode = o.rstrNode, supplyNode, landNode; //assume pickupNode == launchNode;
        Drone drone = droneList[0];
        for (Drone d : droneList) {
            if (d.position == pickupNode) {
                drone = d; //find the drone in that node
                break;
            }
        }    
        
        //TODO -------Built the supply flight, resupply it p-randomly to the node with cloest time
        
        //randomly supply the a node and randomly land a node, built the flight

        //debug:::
        if (drone.feasibleSupplySet == null) {
            Functions.printDebug("null! id:" + drone.id);
        }


        List<Node> feasibleSupplyList = drone.feasibleSupplySet[pickupNode.id];
        r = rand.nextInt(feasibleSupplyList.size());
        supplyNode = feasibleSupplyList.get(r);
        

        List<Node> feasibleLandList = drone.feasibleLandSet[pickupNode.id][supplyNode.id];
        r = rand.nextInt(feasibleLandList.size());
        landNode = feasibleLandList.get(r);

        Flight f = new Flight(pickupNode, pickupNode, supplyNode, landNode);
        // update supply node (note the meet information)
        supplyNode.reset();
        supplyNode.isMeet = true;
        supplyNode.meetCourier = courier;
        supplyNode.meetDrone = drone;

        drone.flights.add(f);
        
        // update the status of drone
        drone.position = f.landNode;
        
    }

    void randomTransferFlightCreate(Drone drone){
        // find all feasible transfer Node for the drone 
        List<Node> feasibleTransferList = drone.feasibleTransferSet[drone.position.id];
        
        // Randomly CHOOSE one and built the transfer flight
        int r = rand.nextInt(feasibleTransferList.size());
        Node transferNode = feasibleTransferList.get(r);
        Flight f = new Flight(drone.position, transferNode);
        drone.flights.add(f);
        // update the status of drone
        drone.position = f.landNode;

        return;
    }


    
    
}
