import java.lang.reflect.Array;
import java.security.KeyRep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.text.Position;

class ResupplySolver extends DroneSupporting_Solver_{
    OddPool candidatePool;
  
    public ResupplySolver(Orders orders, Nodes nodes, Objfunction f, Courier[] courierArray, Drone[] droneArray) {
        super(orders, nodes, f, courierArray, droneArray);
    }
    
    /*              Heuristic               */
    @Override  
    public void genGreedySolve(){
        super.genGreedySolve();
        globalOptSolution.flightSeqs = new LinkedList[drones.length];
        for (int i = 0; i < drones.length; i++) {
            globalOptSolution.flightSeqs[i] = Solution.serializeFlights(drones[i].flights);
        }
        globalOptSolution.meetPoints =  new HashMap<Node, MeetPoint>();
    }
    

    @Override
    public void LNS1t(int maxIteration, int sizeOfNeiborhood){ 
        /* 仅用于储存临时解，全局最优解在globalOptSolution中 */
        Solution candidateSolution = new Solution(globalOptSolution);
        removedOrderList = new ArrayList<>();
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0;
        while (iter < maxIteration) {
            /* resume the status to global optimal */
            recoverFromSolution(candidateSolution);
            
            /* remove heuristic */
            shawRemoval_Courier(sizeOfNeiborhood, 3);
            this.randomRemoval(sizeOfNeiborhood); 
            /* insert heuristic */
            bestInsert_Courier(removedOrderList, 6, 3);
            /* decide whether accept new solution */
            instantiateSolution_t(couriers);
            double tempObjValue = ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(couriers);
                printSolution_Courier(candidateSolution);
                System.out.println("ObjF: " + ObjfValue());
                iter = 0;
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        globalOptSolution.flightSeqs = new LinkedList[drones.length];
        for (int i = 0; i < drones.length; i++) {
            globalOptSolution.flightSeqs[i] = Solution.serializeFlights(drones[i].flights);
        }
        globalOptSolution.meetPoints =  new HashMap<Node, MeetPoint>();
        instantiateSolution();
    } 

    public void LNS2t(int maxIteration, int sizeOfNeiborhood){ 
        /* 仅用于储存临时解，全局最优解在globalOptSolution中 */
        Solution candidateSolution = new Solution(globalOptSolution);
        removedOrderList = new ArrayList<>();
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0;
        while (iter < maxIteration) {
            /* resume the status from global optimal */
            recoverFromSolution(candidateSolution);
            /* ------------ remove heuristic -------------- */
            this.randomRemoval(sizeOfNeiborhood);   
            Functions.checkDuplicate(removedOrderList);
            /* ------------ insert heuristic -------------- */
            while (!removedOrderList.isEmpty()) {
                courierRepairOne(removedOrderList, 6, 3);
            }
            /* instantiateSolution */
            this.instantiateSolution_d();
            double tempObjValue = this.ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                assert meetPointsMap != null;
                candidateSolution = new Solution(couriers, drones, meetPointsMap);
                Functions.printAlert("----- better solution ----:");
                printSolution(candidateSolution);
                iter = 0;
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        this.instantiateSolution();
    } 

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
        int iter = 0;
        while (iter < maxIteration) {
            /* resume the status from global optimal */
            recoverFromSolution(candidateSolution);
            // meetPointCounter(); //for debug

            /* ------------ remove heuristic -------------- */
            /* remove the flight first; 1.remove the whole flight, 2.only canceled 
                the fly task + remove useless transfer */
            //randomly choose one drone
            this.randomRemoval(sizeOfNeiborhood);   
            Functions.checkDuplicate(removedOrderList);

            // Functions.printRouteSeq_with_time(couriers[0].routeSeq);
            // Functions.printFlights(drones[0].flights);
            // Functions.printDebug(" - - - - - - -");
            /* ------------ insert heuristic -------------- */
            while (!removedOrderList.isEmpty()) {
                integrationRepairOne(removedOrderList, 6, 3);
                // Functions.printDebug("(V): Finished inserting one");
            }
            /* instantiateSolution */
            this.instantiateSolution_d();
            // System.out.println("----ONE ROUND OF COMPLETE REPAIR----");
            double tempObjValue = this.ObjfValue();
            /* For test: 
            Functions.printDebug("----- temp solution ----:");
            Functions.printRouteSeq_with_time(couriers[0].routeSeq);
            Functions.printFlights(drones[0].flights);
            System.out.println("ObjF: " + ObjfValue());  */ 

            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                assert meetPointsMap != null;
                // meetPointCounter(); //for debug
                candidateSolution = new Solution(couriers, drones, meetPointsMap);
                Functions.printAlert("----- better solution ----:");
                printSolution(candidateSolution);
                iter = 0;
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        this.instantiateSolution();
    } 

    private void meetPointCounter() {
        int c1=0, c2=0;
        for (Node n : nodes.NodeList) {
            if (n.isMeet)
                c1++;
        }
        assert meetPointsMap.size() == c1;
    }


    // /*          optimize the flights           */
    // /* find the better drone base between two flights */
    
    // /* remove useless(loop) transfer */


    // /* remove the useless pure resupply node for courier */
    // void removeUselessResupplyNode(Courier courier){
    //     ArrayList<Node> route = courier.routeSeq;
    //     for (int i = 0; i < route.size(); i++) {
    //         Node n = route.get(i);
    //         if (n.orderNum == -1 && !n.isMeet) {
    //             route.remove(i);
    //         }
    //     }
    //     return;
    // }


    /*          Herusitics operators           */

    /*          destory operators               */
    /* Random remove orders from all orders and update to courier and drones */
    void randomRemoval(int q){   //q is the number of remove.
        int count = 0;
        ArrayList <Order> toRemoveOrderList = new ArrayList<>(orders.OrderList.length);
        Collections.addAll(toRemoveOrderList, orders.OrderList);
        while (count < q) {
            Order o = toRemoveOrderList.get(rand.nextInt(toRemoveOrderList.size()));
            toRemoveOrderList.remove(o);
            count ++;
            if (o.rstrNode.isMeet || o.cstmNode.isMeet) //[暂时]不移除meetNode(直接跳过)
                continue;
            removedOrderList.add(o);
            super.removeOrderFromCurrentStates(o);
            
        }
    }

    /* Randomly remove a Flight and its following flights from a drone */
    /* abandon method */
    void randomFlightRemovalOne(Drone drone){
        /* generate randomly choose a flight, remove it and its following flights */
        if (drone.flights.size() == 0)
            return;
        int r = rand.nextInt(drone.flights.size() - 1) + 1;
        //Add the picked node's and the subsequence nodes' corresbonding orders to the 'removedOrderList' 
        //+ remove the cooresbounding deliver node
        for (int j = r; j < drone.flights.size(); j++) {
            Flight f =  drone.flights.get(j);
            if (f.pickupNode != null) {
                Courier courier = f.supplyNode.meetCourier;
                // empty the meet related information of node
                meetPointsMap.remove(f.supplyNode);
                f.supplyNode.reset();
                // remove the corresponding order
                Order o = orders.OrderList[f.pickupNode.orderNum];
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
    /* abandon method */
    void randomSupplyRemovalOne(Drone drone){
        if (drone.flights.size() == 0)
            return;
        int r = rand.nextInt(drone.flights.size());
        Flight f =  drone.flights.get(r);
        Courier courier = null;
        if (f.pickupNode != null) {
            courier = f.supplyNode.meetCourier;
            meetPointsMap.remove(f.supplyNode);
            f.supplyNode.reset();
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
    /* Don't forget update 'meetPointsMap' */


    void courierRepairOne(ArrayList<Order> removedOrdersList, int pool_size, int p) {
        // OddPool repair_pool = new OddPool(Math.min(pool_size, removedOrderList.size())); //为何要min到removedOrderList.size()？
        OddPool repair_pool = new OddPool(pool_size);
        /* Get regretion/Objf value from every order - drone - courier pair
         * Get all (o-d-c speicific) repairs options inpool
        */
        for (Order o : removedOrderList) {
            for (Courier c : couriers) {
                Repair candRepair = courier_repairOne(o, c);
                candRepair.order = o;
                repair_pool.inpool(candRepair.value, candRepair);
            }
            // Functions.printDebug("finished trying Order: " + o.id);
        }
        // p-randomly choose a repair with highest value and make it take effect
        int pick_index = randomExpOne(p, repair_pool.size());
        Repair repair = (Repair) repair_pool.takeitem(pick_index);
        // 下面takeEffect的抽象不完全，不会让meetPoint生效。
        repair.attachMeetNode();
        MeetPoint mp = repair.getMeetPoint(); /* !!!Don't forget update 'meetPointsMap' */        
        if (mp != null) //means its a resupply repair
            meetPointsMap.put(mp.meetNode, mp);
        removedOrdersList.remove(repair.order);
        repair.takeEffect();
    }

    //repair are always done one by one. repair one will always be a building block
    void integrationRepairOne(ArrayList<Order> removedOrdersList, int pool_size, int p){   
        // OddPool repair_pool = new OddPool(Math.min(pool_size, removedOrderList.size())); //为何要min到removedOrderList.size()？
        OddPool repair_pool = new OddPool(pool_size);
        /* Get regretion/Objf value from every order - drone - courier pair
         * Get all (o-d-c speicific) repairs options inpool
        */
        for (Order o : removedOrderList) {
            for (Drone d : drones) {
                for (Courier c : couriers) {
                    Repair candRepair = resupply_repairOne(o, c, d);
                    if (candRepair != null) {
                        candRepair.order = o;
                        repair_pool.inpool(candRepair.value, candRepair);
                    }
                }   
            }

            for (Courier c : couriers) {
                Repair candRepair = courier_repairOne(o, c);
                candRepair.order = o;
                repair_pool.inpool(candRepair.value, candRepair);
            }
            // Functions.printDebug("finished trying Order: " + o.id);
        }
        // p-randomly choose a repair with highest value and make it take effect
        int pick_index = randomExpOne(p, repair_pool.size());
        Repair repair = (Repair) repair_pool.takeitem(pick_index);
        // 下面takeEffect的抽象不完全，不会让meetPoint生效。
        repair.attachMeetNode();
        MeetPoint mp = repair.getMeetPoint(); /* !!!Don't forget update 'meetPointsMap' */        
        if (mp != null) //means its a resupply repair
            meetPointsMap.put(mp.meetNode, mp);
        removedOrdersList.remove(repair.order);
        repair.takeEffect();
    }

    private Repair resupply_repairOne(Order order, Courier courier, Drone drone) {
        return bestRepair_drone_specify(order, courier, drone);
    }

    private Repair courier_repairOne(Order order, Courier courier) {
        return bestRepair_1o_to_1c(courier, order);
    }

    /* Here will not provide <estimation> or use <regretion> methodology 
    * GIVEN order, the pickupnode and delievery node already fixed
    * TO BE DETERMINE: 1. meetNode (only those currently in this courier's route counts)
    *                  2. where to insert the flight in drone's flightSeq
    *                  3. where to insert delieveryNode in courier's routeSeq 
    */
    private Repair_1dc bestRepair_drone_specify(Order order, Courier courier, Drone drone) {
        /* Check drone pickable */
        Node rstrmNode = order.rstrNode;
        if (!rstrmNode.isDrbs || drone.feasibleSupplySet[rstrmNode.id].isEmpty())
            return null;
        /* Gathering feasible insert point of drone [? CASE 1: need to transfer --> ?]
         * TODO !!MAKE SURE: 允许从自己到自己的transfer flight!! 否则这儿变得很麻烦 
        */
        ArrayList<Integer> feasible_flight_insertIndex = new ArrayList<>();
        for (int i = 0; i < drone.flights.size(); i ++) {
            if (drone.feasibleTransferSet[drone.flights.get(i).landNode.id].contains(rstrmNode))
                feasible_flight_insertIndex.add(i);
        }
        if (drone.feasibleTransferSet[drone.flights.get(0).launchNode.id].contains(rstrmNode))
                feasible_flight_insertIndex.add(-1);  
                //插入位置为-1意思是插入在队列之首（TODO 考虑创建drone的时候就加入一个dummy flight 来解决种种麻烦）
        
        /* Gathering feasible meetNode */
        HashSet<Node> feasible_meetNode_set = new HashSet<>();
        for (Node n : drone.feasibleSupplySet[rstrmNode.id]) {
            if (courier.routeSeq.contains(n) && !n.isMeet) //不允许一个点meet两次，重复meetNode
                feasible_meetNode_set.add(n);
                 //此处为取交集的动作，可能可以优化
        }   
        if (feasible_meetNode_set.size() == 0)
            return null;

        /* prepare standards */        
        double bestObjValue = Integer.MAX_VALUE;
        Repair_1dc best_repair = null;
        ArrayList<Flight> originalFlights = new ArrayList<>(drone.flights);
        ArrayList<Node> originalRouteSeq = new ArrayList<Node>(courier.routeSeq); 

        /* trying to find the optimal insertation */
        for (Node meetNode : feasible_meetNode_set) {
            for (int k : feasible_flight_insertIndex) {
                Node isNode = (k == -1)?  drone.flights.get(0).launchNode : drone.flights.get(k).landNode;
                Flight transfer = new Flight(isNode, rstrmNode); //isNode : insert-start node; MAKE SURE: 允许从自己到自己的transfer flight!! 否则这儿变得很麻烦 
                Flight resupply = new Flight(rstrmNode, rstrmNode, meetNode, isNode);
                Flight ff = null;
                if (k < drone.flights.size() - 1)
                    ff = drone.concateFlights(resupply, drone.flights.get(k + 1));
                //if (ff == null) means its a direct concate, no transfer flight
                drone.flights.add(k+1, transfer);
                drone.flights.add(k+2, resupply);
                if (k != drone.flights.size() - 1 && ff != null) { 
                    //it means a transfer flight need to be add
                    drone.flights.add(k+3, ff);
                }
                /* ------------------------------- Courier part -------------------------------------- */
                // estimate_1 : base on[meetNode, flight_insertIndex] to speed up the calculate   
                int meetNodeIndex = courier.routeSeq.indexOf(meetNode);
                // 理论上上面已经做过筛选，这里应该不用判断
                // if (meetNodeIndex == -1) {
                //     continue;
                // }
                /* cstmNode只能插入在meetNode之后： */
                for (int j = meetNodeIndex + 1; j <= Math.max(meetNodeIndex + 1, originalRouteSeq.size()); j++) {
                    // estimate_2 : base on[meetNode, flight_insertIndex, courier_delieverIndex] to speed up the calculate  
                    ArrayList<Node> toInsert_cstm = new ArrayList<Node>(originalRouteSeq); // the route to be insert(in the cstm insert step)
                    toInsert_cstm.add(j, order.cstmNode);  
                    /* rebuild the solution base on the tempRoute */
                    courier.routeSeq = toInsert_cstm;
                    Repair_1dc repair = new Repair_1dc(courier, drone, meetNode);
                    repair.attachMeetNode();    
                    /* 下面一行信息量较大
                        instantiateSolution成功了排除了dead lock,同时也为调用ObjfValue作准备
                        若 ObjectValue > bestObjValue 则将这个repair作为候选repair*/
                    if (instantiateSolution_d() && ObjfValue() < bestObjValue) {
                        bestObjValue = ObjfValue();
                        best_repair = repair;
                    }
                    repair.detachMeetNode();
                }
                /*recover*/ drone.flights = new ArrayList<>(originalFlights);
            }
            // System.out.println(" --=============== + ====================--");
        }
        courier.routeSeq = originalRouteSeq;
        return best_repair;
    }

    /* cabinate version: resupply will not apply to the courier exsiting route, courier will need to add trip to 
     * the resupply node(cabinate)
     * 
     * Here will not provide <estimation> or use <regretion> methodology 
     * GIVEN order, the pickupnode and delievery node already fixed
     * TO BE DETERMINE: 1. meetNode
     *                  2. where to insert the flight in drone's flightSeq
     *                  3. where to insert meetNode in courier's routeSeq
     *                  4. where to insert delieveryNode in courier's routeSeq 
    */
    private Repair_1dc bestRepair_drone_specify_cabinate (Order order, Courier courier, Drone drone) {
        /* Check drone pickable */
        Node rstrmNode = order.rstrNode;
        if (!rstrmNode.isDrbs || drone.feasibleSupplySet[rstrmNode.id].isEmpty())
            return null;
        /* Gathering feasibile insert point of drone [? CASE 1: need to transfer --> ?]
         * //TODO !! MAKE SURE: 允许从自己到自己的transfer flight!! 否则这儿变得很麻烦 
        */
        ArrayList<Integer> feasible_flight_insertIndex = new ArrayList<>();
        for (int i = 0; i < drone.flights.size(); i ++) {
            if (drone.feasibleTransferSet[drone.flights.get(i).landNode.id].contains(rstrmNode))
                feasible_flight_insertIndex.add(i);
        }
        if (drone.feasibleTransferSet[drone.flights.get(0).launchNode.id].contains(rstrmNode))
                feasible_flight_insertIndex.add(-1);
        
        /* prepare standards */        
        double bestObjValue = globalOptSolution.objfValue;
        Repair_1dc repair = new Repair_1dc(courier,drone);

        /*  */
        ArrayList<Flight> originalFlights = new ArrayList<>(drone.flights);
        for (Node meetNode : drone.feasibleSupplySet[rstrmNode.id]) {
            for (int k : feasible_flight_insertIndex) {
                Node isNode = (k == -1)?  drone.flights.get(0).launchNode : drone.flights.get(k).landNode;
                Flight transfer = new Flight(isNode, rstrmNode); //isNode : insert-start node
                Flight resupply = new Flight(rstrmNode, rstrmNode, meetNode, isNode);
                Flight ff = drone.concateFlights(resupply, drone.flights.get(k + 1));
                //if (ff == null) means its a direct concate, no transfer flight
                drone.flights.add(k+1, transfer);
                drone.flights.add(k+2, resupply);
                if (k != drone.flights.size() - 1 && ff != null) { 
                    //it means a transfer flight need to be add
                    drone.flights.add(k+3, ff);
                }
                /* ------------------------------- Courier part -------------------------------------- */
                ArrayList<Node> toInsertList = new ArrayList<Node>(courier.routeSeq);  
                int length = toInsertList.size();
                for (int i = 1; i <= length; i++) { //meetIndex
                    ArrayList<Node> toInsert_rstr = new ArrayList<Node>(toInsertList); // the route to be insert(in the rstr insert step)
                    toInsert_rstr.add(i, order.rstrNode); 
                    // estimate_1 : base on[meetNode, flight_insertIndex, courier_meetIndex] to speed up the calculate   
                    for (int j = i + 1; j <= Math.max(i + 1, length); j++) {
                        // estimate_2 : base on[meetNode, flight_insertIndex, courier_meetIndex, courier_delieverIndex] to speed up the calculate  
                        ArrayList<Node> toInsert_cstm = new ArrayList<Node>(toInsert_rstr); // the route to be insert(in the cstm insert step)
                        toInsert_cstm.add(j, order.cstmNode);  
                        /* rebuild the solution base on the tempRoute */
                        courier.routeSeq = toInsert_cstm;
                        instantiateSolution_t_one(courier);
                        if (ObjfValue() > bestObjValue) {
                            bestObjValue = ObjfValue();
                            repair.routeSeq = new ArrayList<>(courier.routeSeq);
                            repair.flightSeq = new ArrayList<>(drone.flights);
                            repair.meetNode = meetNode;
                        }
                    }
                }
                /*recover*/ drone.flights = new ArrayList<>(originalFlights);
            }
        }
        return repair;
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
