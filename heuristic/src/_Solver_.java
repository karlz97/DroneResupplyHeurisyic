import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.management.RuntimeErrorException;


abstract class _Solver_ {
    Orders orders;
    Nodes nodes;
    Objfunction Objf;
    Solution globalOptSolution;
    DecimalFormat df = new DecimalFormat("#.00");

    public _Solver_(Orders orders, Nodes nodes/*, Objfunction f*/){
        this.orders = orders;
        this.nodes = nodes;
        /*this.Objf = f;*/
    }

    abstract public double ObjfValue();
    
    abstract public void printSolution();

    abstract public void recoverFromSolution(Solution solution);

    abstract void instantiateSolution();  //instantiate the Solution including setup from route.

    abstract void removeOrderFromCurrentStates(Order order);

    public double earlistExecuteTime_lfn(Order order, Vehicle vehicle){
        Node orderNode;
        double prepared_lfn; //prepared time left from now
        if (order.isPicked == false){
            orderNode = order.rstrNode;
            prepared_lfn = order.T_prepared - vehicle.time;
        } else{
            orderNode = order.cstmNode;
            prepared_lfn = 0;
        }
        
        if (order.isDelivered){
            throw new RuntimeException("Working on delivered order");
        }
        return Math.max(prepared_lfn, vehicle.callNodeDistance(orderNode, vehicle.position));
    }

    public double earlistExecuteTime(Order order, Vehicle vehicle){
        return vehicle.time + earlistExecuteTime_lfn(order, vehicle);
    }

    public double earlistExecuteTime(Node node, Vehicle vehicle){
        return vehicle.time + vehicle.callNodeDistance(node, vehicle.position);
    }

    public double computeOrderPrioScore(Order order, Vehicle vehicle){
        // Earilst pickup time left from now 
        double pickup_effort = earlistExecuteTime_lfn(order, vehicle); 
        double pickup_time = earlistExecuteTime(order, vehicle);
        // how close is the expected delivery time
        double remaintime = order.T_expected - pickup_time;
        pickup_effort = 0;
        //double score = Math.max(2/remaintime,0) + 10/earilstPickup_lfn; //note that remain time is also acoount by time.
        /**/// double score =  10/(pickup_effort + 1) + 10/(remaintime + 1);
        double score =  - 2*pickup_effort - remaintime;
        //double score =  max(10/earilstPickup_lfn - remaintime);

        // score must always be a positive number
        return score; 
    }

    public void printSolution_Flight(Solution s) {
        if (s.flightSeqs != null) {
            for (int i = 0; i < s.flightSeqs.length; i++) {
                System.out.print("Flight of Drone <" + i + ">: ");
                LinkedList<Node> flightSeq =  s.flightSeqs[i]; int counter = 0;
                for (Node n : flightSeq) {  
                    if ((++counter - 1)% 4 == 0 && counter != 1) 
                        System.out.print( " | ");  
                    if (n == null)
                        continue;
                    if (n.isMeet && counter % 3 == 0) {
                        System.out.print( n.id + "[" + df.format(n.T_drone) + "] --> ");  
                        if (n.T_courier != n.T_drone) {
                            System.out.print("\033[31;1m" + " !! \033[0m");
                        }
                    } else
                        System.out.print( n.id + " --> ");


                }
                System.out.println();
            }
        } else {
            System.out.println("flightseq is empty");  
        }
    }
    
    public void printSolution_Courier(Solution s) {
        System.out.println("Routes: ");
        for (int i = 0; i < s.courierRoutes.length; i++) {
            System.out.print( "Routes of Courier <" + i + ">:");  
            for (Iterator<Node> it = s.courierRoutes[i].iterator(); it.hasNext();) {
                Node n = it.next();
                // System.out.print( n.next().id + " --> ");
                System.out.print( n.id + "[" + df.format(n.T_courier) + "] --> ");  
                // debug::: System.out.print( it.next().id + "(" +  + ")" + " --> ");
            }
            if (orders.allDone()) {
                System.out.println("finished.");   
            }else{
                System.out.println("unfinieshed.");
            }
        }
    }


}