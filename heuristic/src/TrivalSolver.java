import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


import javax.crypto.spec.PBEKeySpec;
import javax.naming.InitialContext;

class TrivalSolver extends TruckOnly_Solver_ {

    public TrivalSolver(Orders orders, Nodes nodes, Objfunction f, Courier[] courierList){
        super(orders, nodes, f, courierList);
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
            shawRemoval_Courier(sizeOfNeiborhood, 3);
            /* insert heuristic */
            regeretInsert_Courier(removedOrderList, 3);
            /* decide whether accept new solution */
            instantiateSolution_t(couriers);
            double tempObjValue = ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(couriers);
                printSolution_Courier(candidateSolution);
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        instantiateSolution();
    } 

    public void LNS2t_base(int maxIteration, int sizeOfNeiborhood){ 
        Solution candidateSolution = new Solution(globalOptSolution);
        removedOrderList = new ArrayList<>();
        double minObjfValue = ObjfValue();
        /* Acceptance and Stopping Criteria */
        int iter = 0;
        while (iter < maxIteration) {
            /* resume the status to global optimal */
            recoverFromSolution(candidateSolution);
            /* remove heuristic */
            randomRemoval(sizeOfNeiborhood);
            /* insert heuristic */
            regeretInsert_Courier(removedOrderList, 3);
            /* decide whether accept new solution */
            instantiateSolution_t(couriers);
            double tempObjValue   = ObjfValue();
            if (tempObjValue < minObjfValue) {
                minObjfValue = tempObjValue;
                candidateSolution = new Solution(couriers);
                printSolution_Courier(candidateSolution);
            }
            iter++;
        }
        globalOptSolution = candidateSolution;
        instantiateSolution();
    }

    public void genGreedySolve(){ 
        Node candidatNode;
        double eptArrivetime;
        // Until: All orders has been done.
        ArrayList<Order> to_assign_orderList = new ArrayList<>(Arrays.asList(orders.OrderList));
        ArrayList<Order>[] courier_half_assign_list = new ArrayList[couriers.length];
        for (int i = 0; i < couriers.length; i++) {
            courier_half_assign_list[i] = new ArrayList<>();
        }

        while (!orders.allDone()){
            double max = 0;
            double tempPrio = 0;
            int courierIndex = 0;
            Order candidateOrder = orders.OrderList[0];
            /* Compute the prioScore for every orders */
            for (int i = 0; i < couriers.length; i++) {      
                Courier courier = couriers[i];          
                for(Order o : merge_list(to_assign_orderList, courier_half_assign_list[i])){
                    tempPrio = computeOrderPrioScore(o, courier);
                    if (tempPrio >= max){
                        max = tempPrio;
                        candidateOrder = o;
                        courierIndex = i;
                    }
                }
            }
            /* choose one with highest order with priority score*/
            Courier courier = couriers[courierIndex];
            candidatNode = candidateOrder.getNode();
            if (candidateOrder.isPicked == false) {
                courier_half_assign_list[courierIndex].add(candidateOrder);
                to_assign_orderList.remove(candidateOrder);
            } else {
                courier_half_assign_list[courierIndex].remove(candidateOrder);
            }
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
        for (Courier c : couriers)
            Functions.printRouteSeq_with_time(c.routeSeq);
        /* update globalSolution */
        this.globalOptSolution = new Solution(couriers);
    }
    


    /*          Herusitics operators           */
    // shawRemoval need to update alot before apply to resupply LNS
    void shawRemoval_from_1c(Courier courier, int q, int p){  //q is the number of orders been removed
        //perform shawRemoval to one courier
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
            index = (int) orderPool.takeitem(index);
            //System.out.println("index2:" + index);
            Order order = orders.OrderList[index];
            removedOrderList.add(order);
            routeSeq.remove(order.cstmNode);
            routeSeq.remove(order.rstrNode);
        }
        //return removedOrderList;
    }

    void shawRemoval_Courier(int q, int p){
        int poolSize = Math.max(orders.OrderList.length/2 + 1, q);
        shawRemoval_Courier(q, p, poolSize);
    }

    /* q is the number of remove. p is the power of randomness */
    void shawRemoval_Courier(int q, int p, int poolSize){ 
        assert poolSize > q;
        int len = orders.OrderList.length;

        ArrayList <Order> toRemoveOrderList = new ArrayList<>(len);
        Collections.addAll(toRemoveOrderList, orders.OrderList);

        OddPool orderPool = new OddPool(poolSize);
        int r = rand.nextInt(len);
        Order order_in = toRemoveOrderList.get(r);

        for (Order o : toRemoveOrderList) {
            double relatedness = order_relatedness(couriers[0], order_in, o);
            orderPool.inpool(relatedness, o);         
            //一趟循环下来，pool里面将所有relatedness排好序并带有对应的index
        }

        int count = 0;
        while (count < q) {
            int index = randomExpOne(p, orderPool.size());
            Order o = (Order) orderPool.takeitem(index); 
            toRemoveOrderList.remove(o);
            removedOrderList.add(o);
            super.removeOrderFromCurrentStates(o);
            count ++;
        }
    }

    void randomRemoval(int q){   //q is the number of remove.
        int count = 0;
        int len = orders.OrderList.length;
        ArrayList <Order> toRemoveOrderList = new ArrayList<>(len);
        Collections.addAll(toRemoveOrderList, orders.OrderList);
        while (count < q) {
            Order o = toRemoveOrderList.get(rand.nextInt(toRemoveOrderList.size()));
            toRemoveOrderList.remove(o);
            removedOrderList.add(o);
            super.removeOrderFromCurrentStates(o);
            count ++;
        }
    }

    void randomRemoval_from_1c(Courier courier, int q){
        //constrain: 不能remove被由drone pickup的点（drone remove的时候会管），也不能remove meetNode
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

    void regeretInsert_Courier(ArrayList<Order> removedOrdersList, int k){
        Repair mostRegretInsertation = new Repair_1cc();
        while (!removedOrdersList.isEmpty()) {
            Order insertOrder = null;
            /* insert the i_th order */ 
            for (Order o : removedOrdersList) {
                for (Courier c : couriers) {
                    Repair candInsertation = regeretRepair_1o_to_1c(c, o, k);
                    //maxRegret.inpool(presudoSol.objfValue, presudoSol.routeSeq); 
                    if (candInsertation.value > mostRegretInsertation.value) {
                        mostRegretInsertation =  candInsertation;
                        insertOrder = o;
                    }
                }
            }
            /* update the 'toInsertList' and 'removedOrdersList' */
            removedOrdersList.remove(insertOrder);
            mostRegretInsertation.takeEffect(); 

        }
        return;
    }

    void regeretInsert_to_1c(Courier courier, ArrayList<Order> removedOrdersList, int k){
        //perform regret Insert to one courier
        //alternative common approach exist, but I just want to unify this kind of operation as Oddpool
        Repair_1cc mostRegretInsertation = new Repair_1cc();
        while (!removedOrdersList.isEmpty()) {
            Order insertOrder = null;
            /* insert the i_th order */ 
            for (Order o : removedOrdersList) {
                Repair_1cc candInsertation = regeretRepair_1o_to_1c(courier, o, k);
                //maxRegret.inpool(presudoSol.objfValue, presudoSol.routeSeq); 
                if (candInsertation.value > mostRegretInsertation.value) {
                    mostRegretInsertation =  candInsertation;
                    insertOrder = o;
                }

            }
            /* update the 'toInsertList' and 'removedOrdersList' */
            removedOrdersList.remove(insertOrder);
            mostRegretInsertation.takeEffect();  
        }
        return;
    }

    Repair_1cc regeretRepair_1o_to_1c(Courier courier, Order order, int k){
        //regret insert one order to one courier
        ArrayList<Node> toInsertList = new ArrayList<Node>(courier.routeSeq);  
        int length = toInsertList.size();
        OddPool insertObjfPool = new OddPool(k + 1);
        /* test every insert position */
        /* insert pickup position  */
        
        for (int i = 1; i <= length; i++) { //插入pickup node 从1开始可以插入，可插到最后。
            ArrayList<Node> toInsert_rstr = new ArrayList<Node>(toInsertList); // the route to be insert(in the rstr insert step)
            toInsert_rstr.add(i, order.rstrNode); 
            /* inset delivery position */
            for (int j = i + 1; j <= Math.max(i + 1, length); j++) {
                ArrayList<Node> toInsert_cstm = new ArrayList<Node>(toInsert_rstr); // the route to be insert(in the cstm insert step)
                toInsert_cstm.add(j, order.cstmNode);   //TODO: a lot can be optimized, there is no need for create a new array?
                /* rebuild the solution base on the tempRoute */
                courier.routeSeq = toInsert_cstm;
                instantiateSolution_t_one(courier);
                /* compute the ObjF & record the lowest k ObjF */
                insertObjfPool.inpool( - ObjfValue(), toInsert_cstm); 
                              
                //'- ObjfValue' inpool will save the max k, we need save the lowest k 
            }
        } 
        /*recover*/ courier.routeSeq = new ArrayList<>(toInsertList);
        /* calculate the regretK, return the propInsertation */
        double regretk = regretK(insertObjfPool);
        
        @SuppressWarnings("unchecked")
        Repair_1cc propInsertation = new Repair_1cc(courier, (ArrayList<Node>) insertObjfPool.indexlist.getFirst(), regretk);
        return propInsertation;
    }


    Repair_1cc bestRepair_1o_to_1c(Courier courier, Order order){
        //greedily insert one order to one courier
        double bestObjValue = -1; 
        Repair_1cc repair = new Repair_1cc(courier);
        ArrayList<Node> toInsertList = new ArrayList<Node>(courier.routeSeq);  
        int length = toInsertList.size();
        /* test every insert position */
        /* insert pickup position  */
        
        for (int i = 1; i <= length; i++) { //插入pickup node 从1开始可以插入，可插到最后。
            ArrayList<Node> toInsert_rstr = new ArrayList<Node>(toInsertList); // the route to be insert(in the rstr insert step)
            toInsert_rstr.add(i, order.rstrNode); 
            /* inset delivery position */
            for (int j = i + 1; j <= Math.max(i + 1, length); j++) {
                ArrayList<Node> toInsert_cstm = new ArrayList<Node>(toInsert_rstr); // the route to be insert(in the cstm insert step)
                toInsert_cstm.add(j, order.cstmNode);   //TODO: a lot can be optimized, there is no need for create a new array?
                /* rebuild the solution base on the tempRoute */
                courier.routeSeq = toInsert_cstm;
                instantiateSolution_t_one(courier);
                if (ObjfValue() > bestObjValue) {
                    bestObjValue = ObjfValue();
                    repair.routeSeq = courier.routeSeq;
                }
            }
        } 
        /*recover*/ courier.routeSeq = new ArrayList<>(toInsertList);
        return repair;
    }

    private static<T> List<T> merge_list(List<T> list1,List<T> list2){
        ArrayList<T> list = new ArrayList<>();
        list.addAll(list1);
        list.addAll(list2);
        return list;
    }
}   