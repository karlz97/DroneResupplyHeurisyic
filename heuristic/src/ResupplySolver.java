import java.lang.reflect.Array;
import java.security.KeyRep;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.text.Position;

class ResupplySolver extends DroneSupporting_Solver_{
  
    public ResupplySolver(Orders orders, Nodes nodes, Objfunction f, Courier[] courierList, Drone[] droneList) {
        super(orders, nodes, f, courierList, droneList);
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
        /* 仅用于储存临时解，全局最优解在globalOptSolution中 */
        Solution candidateSolution = new Solution(globalOptSolution);
        removedOrderList = new ArrayList<>();
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0; int r;
        while (iter < maxIteration) {
            /* resume the status from global optimal */
            recoverFromSolution(candidateSolution);
            Functions.checkDuplicate(removedOrderList);
            /* ------------ remove heuristic -------------- */
            /* remove the flight first; 1.remove the whole flight, 2.only canceled 
                the fly task + remove useless transfer */
            //randomly choose one drone
            r = rand.nextInt(this.drones.length);
            Drone d = drones[r];
            this.randomFlightRemovalOne(d);
            /* remove the courier node if necessary */
            if(removedOrderList.size() < sizeOfNeiborhood) {
                this.randomRemoval_from_1c(courier, sizeOfNeiborhood - removedOrderList.size());
            }           
            /* ------------ insert heuristic -------------- */
            /* insert flight */
            this.randomSupplyFlightCreate_order();
            r = rand.nextInt(this.drones.length);
            /* insert courier */
            this.regeretInsert_to_1c(courier, removedOrderList, 3);
            /* instantiateSolution */
            this.instantiateSolution_d_one(courier);
            double tempObjValue = this.ObjfValue();
        
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(courier,drones);
                printSolution(candidateSolution);
                iter = 0;
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        instantiateSolution();
    } 

    /* LNS2
     * optimize the solution *after* genGreedySolution
     * --------------------- operator ---------------------
     * remove heuristic: Shaw removal //& Random removal  
     * insert heuristic: Regret heuristic & Random insertation 
     * no dynamic weight adjustment of different heuristics.
     * 
     */
    public void LNS2r(int maxIteration, int sizeOfNeiborhood){  //TODO
        /* 仅用于储存临时解，全局最优解在globalOptSolution中 */
        Solution candidateSolution = new Solution(globalOptSolution);
        removedOrderList = new ArrayList<>();
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0; int r;
        while (iter < maxIteration) {
            /* resume the status from global optimal */
            recoverFromSolution(candidateSolution);
            Functions.checkDuplicate(removedOrderList);

            /* ------------ remove heuristic -------------- */
            /* remove the flight first; 1.remove the whole flight, 
                2.only canceled the fly task + remove useless transfer */
            //randomly choose one drone
            r = rand.nextInt(this.drones.length);
            Drone d = drones[r];
            this.randomFlightRemovalOne(d);

            /* remove the courier node if necessary */
            if(removedOrderList.size() < sizeOfNeiborhood) {
                this.randomRemoval_from_1c(courier, sizeOfNeiborhood - removedOrderList.size());
            }           
            
            /* Print Removed Status --------------*/

            /* ------------ insert heuristic -------------- */
            /* insert flight */
            this.randomSupplyFlightCreate_order();
            r = rand.nextInt(this.drones.length);
            // this.randomTransferFlightCreate(d);

            /* insert courier */
            this.regeretInsert_to_1c(courier, removedOrderList, 3);

            /* Print Insert Status---------*/

            /* instantiateSolution */
            this.instantiateSolution_d_one(courier);
            double tempObjValue = this.ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(courier,drones);
                printSolution(candidateSolution);
                iter = 0;
            } else { /*  TODO accept solution with probability */
                
            }   
            iter++;
            /*  */
        }
        globalOptSolution = candidateSolution;
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
        if (drone.flights.size() == 0)
            return;
        int r = rand.nextInt(drone.flights.size());
        //Add the picked node's and the subsequence nodes' corresbonding orders to the 'removedOrderList' 
        //+ remove the cooresbounding deliver node
        for (int j = r; j < drone.flights.size(); j++) {
            Flight f =  drone.flights.get(j);
            Node pickupNode =  f.pickupNode;
            if (pickupNode != null) {
                // empty the meet related information of node
                f.supplyNode.reset();
                // remove the corresponding order
                Order o = orders.OrderList[pickupNode.orderNum];
                this.removedOrderList.add(o);
                // remove the corresbounding delivery node
                Node deliveryNode = o.cstmNode;
                courier.routeSeq.remove(deliveryNode);
            }
        }
        //Remove the picked node and the subsequence nodes from flights & update the status of drone
        if (r == 0) {
            drone.position = drone.flights.get(0).launchNode;
            drone.flights.subList(r, drone.flights.size()).clear();
        } else {
            drone.flights.subList(r, drone.flights.size()).clear();
            drone.position = drone.flights.get(drone.flights.size() - 1).landNode;
        }        
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
    /* [constraints]: the next supply node must after the last supply node in the order of courier route seq*/

    //repair are always done one by one. repair one will always be a building block
    void regretIntegrationRepairOne(int speci_size){  
        //Get regretion value from every order, then choose one to repair
        int size = Math.min(speci_size, removedOrderList.size());
        OddPool regretPool = new OddPool(size);
            //for every order get the loss of every possible insertation
    }


    void randomSupplyFlightCreate_order(){      
        // find all removed & 1st order-drone-feasible orders 
        ArrayList<Order> feasibleOrderList = new ArrayList<>();
        for (Order o : removedOrderList) {
            if (o.rstrNode.isDrbs) {
                for (Drone d : drones) {
                    if (d.position == o.rstrNode) {
                        feasibleOrderList.add(o);   // Functions.printDebug("feasible drone pickup node: " + o.rstrNode.id + ", drone_id: " + d.id);   
                    }  
                } 
            }
        }
        // Randomly CHOOSE one order to resupply
        if (feasibleOrderList.size() == 0) {
            // Functions.printAlert("can't find feasible order");
            return;
        }

        int r = rand.nextInt(feasibleOrderList.size());
        Order theInsertOrder = feasibleOrderList.get(r);
        removedOrderList.remove(theInsertOrder);  //remove the oreder from <reinsert list>

        Node pickupNode = theInsertOrder.rstrNode, supplyNode, landNode; //assume pickupNode == launchNode;
        Drone drone = null; 
        for (Drone d : drones) {
            if (d.position == pickupNode) {
                drone = d; //find the drone in that node; 实际上如果有多个drone还应考虑完成时间最早者
                break;
            }
        }   
        if (drone == null) 
            Functions.printAlert("can't find the drone at the location");
        

        //TODO -------Built the supply flight, resupply it p-randomly to the node with closet time
        
        //randomly supply the a node and randomly land a node, built the flight

        /* find the last meetNode, then find the feasible resupply positions */
        List<Node> feasibleSupplyList = new ArrayList<Node>();
        LinkedList<Node> Set1 = drone.feasibleSupplySet[pickupNode.id];
        Courier courier1 = this.courier;  //以后有多个courier时需要其他方法来确定
        ArrayList<Node> courier1RouteSeq = courier1.routeSeq;
        int lastMeetNodePosition = 0;
        for (int i = courier1RouteSeq.size() - 1; i >= 0; i--) {
            if (courier1RouteSeq.get(i).isMeet) 
                lastMeetNodePosition = i; //找到lastMeetNode
        } 
        for (int i = lastMeetNodePosition; i < courier1RouteSeq.size(); i ++) {
            Node n = courier1RouteSeq.get(i);
            if (Set1.contains(n))
                feasibleSupplyList.add(n);
        }

        if (feasibleSupplyList.isEmpty()) {
            Functions.printAlert("can't find feasibleSupplyList");
            removedOrderList.add(theInsertOrder);
            return;
        }
           
        /* find the last supply node in the courier's routeseq */
    
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
        
        /* greedily insert the delivery (cstm) node to courier */        
        for (int i = lastMeetNodePosition; i < courier1RouteSeq.size(); i++) { //find the position of drone supplynode
            if(courier1RouteSeq.get(i) == supplyNode) {
                r = rand.nextInt(courier1RouteSeq.size() - i);
                courier1RouteSeq.add(i + r + 1, theInsertOrder.cstmNode);
            }
        }

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
