import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.naming.InitialContext;

class TrivalSolution extends Solution {
    Courier courier;
    Objfunction Objf; 
    Random rand = new Random();
    ArrayList<Node> courierGlobalRouteSeq = new ArrayList<Node>();

    public TrivalSolution(Orders orders, Nodes nodes, Objfunction f, Courier courier, 
                       Double[][] truckDistanceMatrix){
        super(orders, nodes, truckDistanceMatrix);
        this.Objf = f;
        this.courier = courier;
        // vehicleList = new Vehicle[1];
        // vehicleList[0] = courier;
    }

    public double ObjfValue(){
        return Objf.computeObjValue(this);
    }

    public void printSolution(){
        System.out.println("Routes: ");
        for (Iterator<Node> it = courierGlobalRouteSeq.iterator(); it.hasNext();) {
            System.out.print( it.next().id + " --> ");
        }
        if (orders.allDone()) {
            System.out.println("finished.");   
        }else{
            System.out.println("unfinieshed.");
        }
        System.out.println("ObjF: " + ObjfValue());
    } 


    @Override
    void instantiateSolution(ArrayList<Node> routeSeq){
        /* reset all order, nodes to initial */
        for(int i = 0; i<orders.OrderList.length; i++) {
            orders.OrderList[i].reset_r();  //only reset the order but not the related node.
        }
        for(int i = 0; i<nodes.NodeList.length; i++) {
            nodes.NodeList[i].reset();
        }

        /* set the status of courier, order, node by follow the routeSeq */
        Node currNode;      //temp variables to speedup the program
        Order currOrder;    //temp variables
        courier.position  = routeSeq.get(0); //deal with startnode
        courier.time = 0;
        for(int i = 1; i<routeSeq.size(); i++) {  //start after the startnode
            currNode = routeSeq.get(i);
            
            /*  courier, order  &  node */
            if(currNode.orderNum != -1){    //if it is an order node, update the order
                currOrder = orders.OrderList[currNode.orderNum];
                courier.time = earlistExecuteTime(currOrder, courier); //Because during a feasible solution route, it always pickup first.
                currOrder.update(courier.time);  //For more structured progame and mode reuse, here is couples of overhead 
            }else{                          //if it is not an order node, don't need update the order
                courier.time = earlistExecuteTime(currNode, courier);
            }
            courier.position = currNode;
            currNode.T_courier = courier.time;  //update the node
        }
    }

    void instantiateSolution(){
        instantiateSolution(courierGlobalRouteSeq);
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
    public void LNS1t(int maxIteration){ 
        
        /* 仅用于储存临时解，全局最优解在globalRouteSeq中 */
        ArrayList<Node> candidateRoute = new ArrayList<Node>(courier.routeSeq);   
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0;
        while (iter < maxIteration) {
            /* remove heuristic */
            ArrayList<Order> removedOrderList = shawRemoval_fast(courier.routeSeq, 2, 2);
            /* insert heuristic */
            courier.routeSeq = regeretInsert(courier.routeSeq, removedOrderList, 2);
            /* decide whether accept new solution */
            instantiateSolution(courier.routeSeq);
            double tempObjValue = ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                //candidateRoute = courier.routeSeq; 这样写会有bug，因为在下一轮while循环中 同样的 courier.routeSeq 会被 shawRemoval_fast
                candidateRoute.clear(); candidateRoute.addAll(courier.routeSeq);
                //System.out.println("miniObjValue: " + minObjfValue);
                Functions.printRouteSeq(candidateRoute);
            }
            iter++;
        }
        courierGlobalRouteSeq = candidateRoute;
        instantiateSolution();
    }

    

    public void largeNeiborhoodSearch(){

    }


    public void genGreedySolution(){ 
        /*  currently assume only one courier.
            [ known better approach exists. ]
        */
        //initial the vehicle time = 0;
        //double couriertime = 0;
        Order candidateOrder;
        Node candidatNode;
        double eptArrivetime;
        double[] scoreList = new double[orders.numOfOrders];

        // Until: All orders has been done.
        while (!orders.allDone()){
            //double max = 0;
            double tempPrio = 0;
            int index = 0;
            /* Compute the prioScore for every orders */
            for(int i = 0; i < orders.numOfOrders; i++){
                tempPrio = computeOrderPrioScore(orders.OrderList[i],courier);
                /*if (tempPrio >= max){
                    max = tempPrio;
                    index = i;
                }*/
                scoreList[i] = tempPrio;
            }

            /* choose one with highest order with priority score*/
            index = Functions.findMax(scoreList);
            candidateOrder = orders.OrderList[index];
            candidatNode = candidateOrder.getNode();
            
            /* add route to the route ArrayList */
            courier.routeSeq.add(candidatNode);

            /* compute expected arrive time */
            //eptArrivetime = courier.time + callNodeDistance(courier.position, candidateOrder.getNode());
            //System.out.println("old: " + eptArrivetime);
            eptArrivetime = earlistExecuteTime(candidateOrder, courier);
            //System.out.println("new: " + eptArrivetime);

            /* update order, courier status */
            courier.position = candidatNode;
            courier.time = eptArrivetime;
            candidateOrder.update(courier.time);
        }

        /* update globalSolution(courierGlobalRouteSeq) */
        courierGlobalRouteSeq.clear();
        courierGlobalRouteSeq.addAll(courier.routeSeq);

    }
    


    /*          Herusitics operators           */
    ArrayList<Order> shawRemoval_old(ArrayList<Node> routeSeq, int q, int p){  //a low efficiency implement, can be optimized a lot(the index of maxN problem)
        Order order_in = orders.OrderList[rand.nextInt(orders.OrderList.length)];    //randomly choose an order
        double[] relaArray = new double[orders.OrderList.length];
        ArrayList<Order> removedOrderList = new ArrayList<Order>();

        for (int i = 0; i < relaArray.length; i++) {
            relaArray[i] = order_relatedness(order_in, orders.OrderList[i]);
        }
        /* get the remove list by randomness (index by relatedness), no duplicate */
        ArrayList<Integer> removList = randomExpOneList(q, p, orders.OrderList.length);  
        /* convert the relatedness-index to the Order index */

        for (int i = 0; i < removList.size(); i++){          
            //           
            int index  = Functions.findMaxN(relaArray, removList.get(i));  // [Notice] Here also has probability get the same index
            //
            System.out.println("removeIndex: " + removList.get(i) + ","  + index);
            Order order = orders.OrderList[index];
            removedOrderList.add(order);
            routeSeq.remove(order.cstmNode);
            routeSeq.remove(order.rstrNode);
        }
        return removedOrderList;
    }

    ArrayList<Order> shawRemoval_fast(ArrayList<Node> routeSeq, int q, int p){  
        int orderlistlen = orders.OrderList.length;
        Order order_in = orders.OrderList[rand.nextInt(orderlistlen)];    //randomly choose an order
        ArrayList<Order> removedOrderList = new ArrayList<Order>();
        OddPool orderPool = new OddPool(orderlistlen);

        for (int i = 0; i < orderlistlen; i++) {
            double relatedness = order_relatedness(order_in, orders.OrderList[i]);
            orderPool.inpool(relatedness, i);         //一趟循环下来，pool里面将所有relatedness排好序并带有对应的index
        }
        //System.out.println("orderlistlen:" + orderlistlen);
        for (int i = 0; i < q; i++) {
            int index = randomExpOne(p, orderlistlen - i);
            //index = (int) orderPool.indexlist.get(index);  //convert the relatedness-index to the Order index 
            index = (int) orderPool.getitem(index);
            //System.out.println("index2:" + index);
            Order order = orders.OrderList[index];
            removedOrderList.add(order);
            routeSeq.remove(order.cstmNode);
            routeSeq.remove(order.rstrNode);
        }
        return removedOrderList;
    }

    ArrayList<Order> randomRemoval(ArrayList<Node> routeSeq, int q){
        ArrayList<Order> removedOrderList = new ArrayList<Order>();
        ArrayList<Order> toremoveOrderList = new ArrayList<Order>();
        toremoveOrderList.addAll(Arrays.asList(orders.OrderList));

        for (int i = 0; i < q; i++) {
            Order order = toremoveOrderList.get(rand.nextInt(toremoveOrderList.size()));
            toremoveOrderList.remove(order);
            removedOrderList.add(order);
            routeSeq.remove(order.cstmNode);
            routeSeq.remove(order.rstrNode);
        }
        return removedOrderList;
    }

    ArrayList<Node> regeretInsert(ArrayList<Node> toInsertList, ArrayList<Order> removedOrdersList, int k){
        int length = removedOrdersList.size();
        //OddPool maxRegret = new OddPool(1);  
        //alternative common approach exist, but I just want to unify this kind of operation as Oddpool
        for (int i = 0; i < length; i++) {
            /* insert the i_th order */ 
            double maxRegret = 0; int index_maxRegret = 0; ArrayList<Node> tempInsertList = new ArrayList<Node>();
            for (int j = 0; j < removedOrdersList.size(); j++) {
                PseudoSolution presudoSol = regeretInsertOne(toInsertList, removedOrdersList.get(j), k);
                //maxRegret.inpool(presudoSol.objfValue, presudoSol.routeSeq); 
                if (presudoSol.objValue > maxRegret) {
                    maxRegret = presudoSol.objValue;
                    tempInsertList = presudoSol.routeSeq;
                    index_maxRegret = j;
                }
            }
            /* update the 'toInsertList' and 'removedOrdersList' */
            removedOrdersList.remove(index_maxRegret);
            toInsertList = tempInsertList; 
        }
        return toInsertList;
    }


    PseudoSolution regeretInsertOne(ArrayList<Node> toInsertList, Order order, int k){
        int length = toInsertList.size();
        OddPool insertObjfPool = new OddPool(k + 1);
        /* test every insert position */
        /* insert pickup position  */
        
        for (int i = 1; i < length + 1; i++) { //插入pickup node 从1开始可以插入，可插到最后。
            ArrayList<Node> toInsert_rstr = new ArrayList<Node>(toInsertList);
            toInsert_rstr.add(i, order.rstrNode);
            /* inset delivery position */
            for (int j = i+1; j < Math.max(i + 2, length + 1); j++) {
                ArrayList<Node> toInsert_cstm = new ArrayList<Node>(toInsert_rstr);
                toInsert_cstm.add(j, order.cstmNode);   //TODO: a lot can be optimized, there is no need for create a new array
                /* rebuild the solution base on the tempRoute */
                instantiateSolution(toInsert_cstm);
                /* compute the ObjF & record the lowest k ObjF */
                insertObjfPool.inpool( - ObjfValue(), toInsert_cstm); 
                //'- ObjfValue' inpool will save the max k, we need save the lowest k 
            }
        } 
        /* calculate the regretK, return the propInsertation */
        double regretk = regretK(insertObjfPool);
        
        @SuppressWarnings("unchecked")
        PseudoSolution propInsertation = new PseudoSolution( (ArrayList<Node>) insertObjfPool.indexlist.getFirst(), regretk);
        return propInsertation;
    }


    private double regretK(OddPool insertObjfPool){
        double regretk = 0; 
        double minObjf =  - insertObjfPool.list.getFirst();
        for (int i = 1; i < insertObjfPool.length; i++) {
            regretk +=  (-insertObjfPool.list.get(i)) - minObjf; 
        }
        return regretk;
    }
    

    double order_relatedness(Order o1, Order o2){
        double p_dis = 1; 
        double p_time = 1;
        //float p_nodetype = 5;
        double relatedness = p_dis * ( callNodeDistance(o1.cstmNode, o2.cstmNode) + 
                                        callNodeDistance(o1.rstrNode, o2.rstrNode) ) + 
                             p_time * ( ( o1.T_prepared - o2.T_prepared ) + 
                                         ( o1.T_expected - o2.T_expected ) );
        return relatedness;                         
    }

    int randomExpOne(int p, float range){    
        /** 
         * get 1 with probability, 
         * bigger p, higher probability to get an int close or equal to one. 
         */
        float div = 1/range;
        float rd = rand.nextFloat();
        rd = (float) Math.pow(rd, p);
        int i;
        for (i = 1; i <= range; i++) {
            if ( i*div >= rd) {
                return (i-1);
            }
        }
        System.out.println("!!!     Error in <randomOne>");
        System.out.println("range: " + range + ", randomNumber: " + rd + " i*div: " +  i*div + " i*div: " + div);
        return (i-1);
    }

    ArrayList<Integer> randomExpOneList(int q, int p, int range){
        //q is the lenth of List
        ArrayList<Integer> randlist = new ArrayList<Integer>();
        for (int i = 0; i < q; i++) {
            int candidate =  randomExpOne(p, range);
            while (randlist.contains(candidate)) {
                candidate ++;
                if(candidate >= range){
                    candidate = randomExpOne(p, range); //exceed the range, regenerate
                }
            }
            randlist.add(candidate);
        }
        return randlist;
    }
    
    /*ArrayList<Integer> randomExpOneList(int q, int p, int range){
        //q is the lenth of List
        ArrayList<Integer> randlist = new ArrayList<Integer>();
        for (int i = 0; i < q; i++) {
            int candidate =  randomExpOne(p, range);
            randlist.add(candidate);
        }
        return randlist;
    }*/
}   