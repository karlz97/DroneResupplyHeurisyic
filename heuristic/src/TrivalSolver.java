import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.naming.InitialContext;

class TrivalSolver extends _Solver_ {
    Courier courier;
    Objfunction Objf; 
    Random rand = new Random();
    ArrayList<Order> removedOrderList;
    

    public TrivalSolver(Orders orders, Nodes nodes, Objfunction f, Courier courier){
        super(orders, nodes);
        this.Objf = f;
        this.courier = courier;
        // vehicleList = new Vehicle[1];
        // vehicleList[0] = courier;
    }

    public double ObjfValue(){
        return Objf.computeObjValue(this);
    }

    public void printSolution(){
        ArrayList<Node> route = globalOptSolution.courierRoute;
        instantiateSolution_t_routeSeq(route);
        Functions.printSolution_Courier(globalOptSolution, orders);
        System.out.println("ObjF: " + ObjfValue());
    } 

    @Override
    public void recoverFromSolution(Solution solution) {
        courier.routeSeq = new ArrayList<>(solution.courierRoute);
    }

    void instantiateSolution_t(Courier courier){
        instantiateSolution_t_routeSeq(courier.routeSeq);
    }

    private void instantiateSolution_t_routeSeq(ArrayList<Node> routeSeq){
        /* reset all order, nodes to initial */
        for(int i = 0; i<orders.OrderList.length; i++) {
            orders.OrderList[i].reset_r();  //only reset the order but not the related node.
        }
        for(int i = 0; i<nodes.NodeList.length; i++) {
            nodes.NodeList[i].reset_r();
        }

        /* set the status of courier, order, node by follow the routeSeq */
        Node currNode;      //temp variables to speedup the program
        Order currOrder;    //temp variables
        courier.position  = routeSeq.get(0); //deal with startnode
        courier.time = 0;
        for(int i = 0; i<routeSeq.size(); i++) {  //start at the startnode //start after the startnode
            currNode = routeSeq.get(i);
            
            /*  courier, order  &  node */
            if(currNode.orderNum != -1){    //if it is an order node, update the order
                currOrder = orders.OrderList[currNode.orderNum];
                courier.time = earlistExecuteTime(currOrder, courier); //Because during a feasible solution route, it always pickup first.
                currOrder.update(courier, courier.time);  //For more structured progame and mode reuse, here is couples of overhead 
            }else{                          //if it is not an order node, don't need update the order
                courier.time = earlistExecuteTime(currNode, courier);
            }
            courier.position = currNode;
            currNode.T_courier = courier.time;  //update the node
        }
    }

    @Override
    void instantiateSolution(){
        recoverFromSolution(globalOptSolution);
        instantiateSolution_t(courier);
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
            shawRemoval_fast(courier, sizeOfNeiborhood, 3);
            /* insert heuristic */
            regeretInsert(courier, removedOrderList, 3);
            /* decide whether accept new solution */
            instantiateSolution_t(courier);
            double tempObjValue = ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(courier);
                Functions.printRouteSeq(candidateSolution.courierRoute);
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        instantiateSolution();
    }

    public void LNS2t(int maxIteration, int sizeOfNeiborhood){ 
        Solution candidateSolution = new Solution(globalOptSolution);
        removedOrderList = new ArrayList<>();
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0;
        while (iter < maxIteration) {
            /* resume the status to global optimal */
            recoverFromSolution(candidateSolution);
            
            /* remove heuristic */
            randomRemoval(courier, sizeOfNeiborhood);
            /* insert heuristic */
            regeretInsert(courier, removedOrderList, 3);
            /* decide whether accept new solution */
            instantiateSolution_t(courier);
            double tempObjValue = ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(courier);
                Functions.printRouteSeq(candidateSolution.courierRoute);
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        instantiateSolution();
    }

    public void genGreedySolve(){ 
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
            candidateOrder.update(courier, courier.time);
        }

        /* update globalSolution */
        this.globalOptSolution = new Solution(courier);

    }
    


    /*          Herusitics operators           */
    // shawRemoval need to update alot before apply to resupply LNS
    void shawRemoval_fast(Courier courier, int q, int p){  //q is the number of orders been removed
        ArrayList<Node> routeSeq = courier.routeSeq;
        int orderlistlen = orders.OrderList.length;
        Order order_in = orders.OrderList[rand.nextInt(orderlistlen)];    //randomly choose an order
        //ArrayList<Order> removedOrderList = new ArrayList<Order>();
        OddPool orderPool = new OddPool(orderlistlen);

        for (int i = 0; i < orderlistlen; i++) {
            Order o = orders.OrderList[i];
            if (o.pickVehicle != courier || o.deliVehicle != courier) {
                continue;
            }
            double relatedness = order_relatedness(courier, order_in, o);
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
        //return removedOrderList;
    }

    void randomRemoval(Courier courier, int q){
        //constrain: 不能remove被由drone pickup的点（drone remove的时候会管），也不能remove meetNode
        //TODO feasible removable order(order that delievry by courier)
        ArrayList<Node> routeSeq = courier.routeSeq;
        ArrayList<Order> toremoveOrderList = new ArrayList<>();
        for (Node n : routeSeq) {
            if (n.isRstr) { //通过Rstr点来确定该点对应的订单是否由courier承运
                Order o = orders.OrderList[n.orderNum];
                if (!o.rstrNode.isMeet && !o.cstmNode.isMeet) { //确定被移除的order对应的node不是meetNode
                    toremoveOrderList.add(o);    
                }
            }
        }
        for (int i = 0; i < q; i++) {
            Order o = toremoveOrderList.get(rand.nextInt(toremoveOrderList.size()));
            toremoveOrderList.remove(o);
            if (!removedOrderList.contains(o)) {
                removedOrderList.add(o);
                routeSeq.remove(o.cstmNode);
                routeSeq.remove(o.rstrNode);
            } else {
                i--;
            }
        }
        //return removedOrderList;
    }

    void regeretInsert(Courier courier, ArrayList<Order> removedOrdersList, int k){
        int length = removedOrdersList.size();
        //OddPool maxRegret = new OddPool(1);  
        //alternative common approach exist, but I just want to unify this kind of operation as Oddpool
        for (int i = 0; i < length; i++) {
            /* insert the i_th order */ 
            double maxRegret = 0; int index_maxRegret = 0; ArrayList<Node> tempInsertList = new ArrayList<Node>();
            for (int j = 0; j < removedOrdersList.size(); j++) {
                PseudoSolution presudoSol = regeretInsertOne(courier.routeSeq, removedOrdersList.get(j), k);
                //maxRegret.inpool(presudoSol.objfValue, presudoSol.routeSeq); 
                if (presudoSol.objValue > maxRegret) {
                    maxRegret = presudoSol.objValue;
                    tempInsertList = presudoSol.routeSeq;
                    index_maxRegret = j;
                }
            }
            /* update the 'toInsertList' and 'removedOrdersList' */
            removedOrdersList.remove(index_maxRegret);
            courier.routeSeq = tempInsertList; 
        }
        return;
    }

    PseudoSolution regeretInsertOne(ArrayList<Node> toInsertList, Order order, int k){
        int length = toInsertList.size();

        OddPool insertObjfPool = new OddPool(k + 1);
        /* test every insert position */
        /* insert pickup position  */
        
        for (int i = 1; i < length + 1; i++) { //插入pickup node 从1开始可以插入，可插到最后。
            ArrayList<Node> toInsert_rstr = new ArrayList<Node>(toInsertList); // the route to be insert(in the rstr insert step)
            toInsert_rstr.add(i, order.rstrNode); 
            /* inset delivery position */
            for (int j = i + 1; j < Math.max(i + 2, length + 1); j++) {
                ArrayList<Node> toInsert_cstm = new ArrayList<Node>(toInsert_rstr); // the route to be insert(in the cstm insert step)
                toInsert_cstm.add(j, order.cstmNode);   //TODO: a lot can be optimized, there is no need for create a new array?
                /* rebuild the solution base on the tempRoute */
                instantiateSolution_t_routeSeq(toInsert_cstm);
                /* compute the ObjF & record the lowest k ObjF */
                insertObjfPool.inpool( - ObjfValue(), toInsert_cstm); 
                
                //TODO debug:: hotfix
                //Functions.printRouteSeq(toInsert_cstm);
                
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
    
    double order_relatedness(Vehicle vehicle, Order o1, Order o2){
        double p_dis = 1; 
        double p_time = 1;
        //float p_nodetype = 5;
        double relatedness = p_dis * ( vehicle.callNodeDistance(o1.cstmNode, o2.cstmNode) + 
                                        vehicle.callNodeDistance(o1.rstrNode, o2.rstrNode) ) + 
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