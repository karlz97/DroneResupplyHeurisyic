import java.lang.reflect.Array;
import java.security.KeyRep;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.text.Position;

class ResupplySolver extends TrivalSolver{
    Drone[] drones;    //drone and courier are per Solution resources use as memory to instantiate solution

    public ResupplySolver(Orders orders, Nodes nodes, Objfunction f, Courier courier, Drone[] droneList,
            Double[][] truckDistanceMatrix) {
        super(orders, nodes, f, courier, truckDistanceMatrix);
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
    }
    
    @Override
    public void printSolution(){
        //instantiateSolution_t(courierGlobalRouteSeq);
        printSolution_Courier(globalOptSolution);
        printSolution_Flight(globalOptSolution);
        System.out.println("ObjF: " + ObjfValue());  
        //TODO solution.obj_value; ObjfValue实际上是对当前的Order状态计算得到的，如果没有initialization solution正确的话，结果可能有错
    } 

    
    private void printSolution_Flight(Solution s) {
        if (s.flightSeqs != null) {
            for (int i = 0; i < s.flightSeqs.length; i++) {
                System.out.println("Flights[" + i + "]:");
                LinkedList<Node> flightSeq =  s.flightSeqs[i];
                for (Node node : flightSeq) {
                    System.out.print( node.id + " --> ");
                }
                System.out.println();
            }
        } else {
            System.out.println("flightseq is empty");  
        }
    }

    private void printSolution_Courier(Solution s) {
        System.out.println("Routes: ");
        for (Iterator<Node> it = s.courierRoute.iterator(); it.hasNext();) {
            System.out.print( it.next().id + " --> ");
            // debug::: System.out.print( it.next().id + "(" +  + ")" + " --> ");
        }
        if (orders.allDone()) {
            System.out.println("finished.");   
        }else{
            System.out.println("unfinieshed.");
        }
    }

    @Override
    public void recoverFromSolution(Solution solution) {
        /* Nodes和Orders 中大部分信息可分为两类，一类时固定不变的元信息，另一类是用来initial solution的信息。
            但注意 Nodes中的 isMeet 和 meetCourier, meetDrone 是与解相关的信息，理想的话应该与Node解耦合，但是现在还没空做
            */
    
        for (Node n : nodes.NodeList) { 
            n.reset();
        }
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
                f.supplyNode.isMeet = true;
                f.supplyNode.meetCourier = this.courier;
                f.supplyNode.meetDrone = drones[i];
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

    public void LNS1r(int maxIteration, int sizeOfNeiborhood){  //TODO
        
        /* 仅用于储存临时解，全局最优解在globalOptSolution中 */
        Solution candidateSolution = null;
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0;
        int r; Drone d;
        while (iter < maxIteration) {
            /* resume the status from global optimal */
            recoverFromSolution(globalOptSolution);
            Functions.checkDuplicate(removedOrderList);

            /* ------------ remove heuristic -------------- */
            /* remove the flight first; 1.remove the whole flight, 2.only canceled the fly task + remove useless transfer */
            //randomly choose one drone
            r = rand.nextInt(this.drones.length);
            d = drones[r];
            //this.randomFlightRemovalOne(d); ///////////////////////////////

            /* remove the courier node if necessary */
            if(removedOrderList.size() < sizeOfNeiborhood) {
                this.randomRemoval(courier, sizeOfNeiborhood - removedOrderList.size());
            }           
            
            /* Print Removed Status */
            Functions.printDebug("--------------++ After Removal ++-------------");
            System.out.print("removedOrder's NodeList: ");
            for (Order o1 : removedOrderList) {
                System.out.print(o1.cstmNode.id + ", ");
                System.out.print(o1.rstrNode.id + ", ");
            }System.out.println();
            System.out.println("Drone position: "+ drones[0].position.id);   
            System.out.println();
            Functions.printRouteSeq(courier.routeSeq);
            
            //print flight
            Drone dd = drones[0];
            LinkedList<Node> flightSeq = new LinkedList<>();
            for (Flight flight : dd.flights) {
                flightSeq.add(flight.launchNode);
                flightSeq.add(flight.pickupNode);
                flightSeq.add(flight.supplyNode);
                flightSeq.add(flight.landNode);
            }
            System.out.print("Flights[0]:");
            int c = 0;
            for (Node node : flightSeq) {
                c ++;
                if( node != null)
                    System.out.print( node.id + " --> ");
                else
                    System.out.print("* --> ");
                if (c == 4) {
                    System.out.print("||");
                    c = 0;
                }
            }
            System.out.println("end");
            Functions.printDebug("---------------------------------------------");

            /* ------------ insert heuristic -------------- */
            /* insert flight */
            this.randomSupplyFlightCreate_order();
            r = rand.nextInt(this.drones.length);
            // this.randomTransferFlightCreate(d);

            /* insert courier */
            this.regeretInsert(courier, removedOrderList, 3);


            /* Print Insert Status*/
            Functions.printDebug("--------------++ After Insertaion ++-------------");
            System.out.print("removedOrderList: ");
            for (Order o1 : removedOrderList) {
                System.out.print(o1.cstmNode.id + ", ");
                System.out.print(o1.rstrNode.id + ", ");
            }System.out.println();

            //print courier route
            Functions.printRouteSeq(courier.routeSeq);
            
            //print flight route
            flightSeq = new LinkedList<>();
            for (Flight flight : dd.flights) {
                flightSeq.add(flight.launchNode);
                flightSeq.add(flight.pickupNode);
                flightSeq.add(flight.supplyNode);
                flightSeq.add(flight.landNode);
            }
            System.out.print("Flights[0]:");
            c = 0;
            for (Node node : flightSeq) {
                c ++;
                if( node != null)
                    System.out.print( node.id + " --> ");
                else
                    System.out.print("* --> ");
                if (c == 4) {
                    System.out.print("||");
                    c = 0;
                }
            }
            System.out.println("end");

            /* instantiateSolution */
            this.instantiateSolution_d(courier);
            double tempObjValue = this.ObjfValue();
            
            Functions.printAlert("ObjValue: " + tempObjValue);
            Functions.printDebug("-------------------------------------------------");

            // /* instantiateSolution */
            // this.instantiateSolution_d(courier);
            // double tempObjValue = this.ObjfValue();


            /*  TODO accept solution with probability */
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(courier,drones);
                System.out.println("miniObjValue: " + minObjfValue);
                Functions.printRouteSeq(candidateSolution.courierRoute);
            }
            iter++;
            /*  */
        }
        if (candidateSolution != null)
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

        Functions.printDebug("r:"+ r + "  flights.size():" + drone.flights.size()); //TODO  debug:::

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

        if (true) {   // debug prints
            // System.out.print("LastMeetNode:" + courier1RouteSeq.get(lastMeetNodePosition).id + " ,feasibleSupplyList: ");
            // for (Node n1 : feasibleSupplyList) {
            //     System.out.print(n1.id + ", ");
            // }System.out.println();
            // System.out.print("Courier routeSeq: ");
            // for (Node n1 : courier1RouteSeq) {
            //     System.out.print(n1.id + " --> ");
            // }System.out.println("end");
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
