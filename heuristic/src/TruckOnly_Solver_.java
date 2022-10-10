import java.util.ArrayList;
import java.util.Random;

public class TruckOnly_Solver_ extends _Solver_ {
    Courier[] couriers; //Courier courier;
    Objfunction Objf; 
    Random rand = new Random();
    ArrayList<Order> removedOrderList;

    public TruckOnly_Solver_(Orders orders, Nodes nodes, Objfunction f, Courier[] courierList){
        super(orders, nodes);
        this.Objf = f;
        this.couriers = courierList;
    }

    public double ObjfValue(){
        return Objf.computeObjValue(this);
    }

    public void printSolution(){
        // Not necessary print the global solution, just the status of current arrangement
        instantiateSolution();
        printSolution_Courier(globalOptSolution);
        System.out.println("ObjF: " + ObjfValue());
    } 

    @Override
    public void recoverFromSolution(Solution solution) {
        nodes.reset();
        for (int i = 0; i < couriers.length; i++) {
            couriers[i].routeSeq = new ArrayList<>(solution.courierRoutes[i]);
        }
    }

    @Override
    public void removeOrderFromCurrentStates(Order order) {
    //search through all vehicle and remove the order from their routes.
        for (int i = 0; i < couriers.length; i++) {
            if(couriers[i].removeOrderFromRoute(order) == "removed_both")
                return;
        }
        Functions.printAlert("No order matched in all couriers!");
    }

    void instantiateSolution_t(Courier[] courierList){
        for (Courier c : courierList) {
            instantiateSolution_t_one(c);
        }
        
    }

    void instantiateSolution_t_one(Courier courier){
        ArrayList<Node> routeSeq = courier.routeSeq;
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
        instantiateSolution_t(couriers);
    }

    double regretK(OddPool insertObjfPool){
        double regretk = 0; 
        double minObjf =  - insertObjfPool.valuelist.getFirst();
        for (int i = 1; i < insertObjfPool.length; i++) {
            regretk +=  (-insertObjfPool.valuelist.get(i)) - minObjf; 
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
