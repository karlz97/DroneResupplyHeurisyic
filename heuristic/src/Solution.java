import java.util.ArrayList;
import java.util.Arrays;
abstract class Solution {
    Orders orders;
    Nodes nodes;
    Double[][] distanceMatrix;
    Objfunction Objf;

    public Solution(Orders orders, Nodes nodes/*, Objfunction f*/){
        this.orders = orders;
        this.nodes = nodes;
        /*this.Objf = f;*/
    }

    public Solution(Orders orders, Nodes nodes, Double[][] distanceMatrix/*, Objfunction f*/){
        this.orders = orders;
        this.nodes = nodes;
        this.distanceMatrix = distanceMatrix;
        /*this.Objf = f;*/
    }

    public double callNodeDistance(Node node1 ,Node node2){
        return distanceMatrix[node1.id][node2.id];
    }

    public double computeOrderPrioScore(Order order, Vehicle courier){
        Node orderNode;
        double earilstPickup_lfn;  //earilst pickup time left from now
        double prepared_lfn; //prepared time left from now
        if (!order.isPicked){
            orderNode = order.cstmNode;
            prepared_lfn = order.T_prepared - courier.time;
        } else{
            orderNode = order.rstrNode;
            prepared_lfn = 0;
        }
        
        if (order.isDelivered){
            return -1;
        }

        // Earilst pickup time left from now 
        double distance = callNodeDistance(orderNode ,courier.position);
        earilstPickup_lfn = Math.max(prepared_lfn, distance);

        // how close is the expected delivery time
        double remaintime = order.T_expected - courier.time;

        double score = 10/remaintime + 10/earilstPickup_lfn; //note that remain time is also acoount by time.
        // score is always a positive number
        return score; 
    }

    public double computeOrderPrioScore_old(Order order, Node vehiclePosition){
        Node orderNode;
        if (!order.isPicked){
            orderNode = order.cstmNode;
        } else{
            orderNode = order.rstrNode;
        }
        
        if (order.isDelivered){
            return -1;
        }
       
        // Get the distance between the order corresbonding nodes and current vehicle position
        /*// Norm2 approach 
        double x_order = orderNode.coord[0];
        double y_order = orderNode.coord[1];
        double x_vhcle  = vehiclePosition.coord[0];
        double y_vhcle  = vehiclePosition.coord[1];
        double distance = Functions.computeNorm2Distance(x_order, y_order, x_vhcle, y_vhcle); */
        
        // DistanceMatrix approach 
        double distance = callNodeDistance(orderNode ,vehiclePosition);
        // how close is the expected delivery time
        double remaintime = order.T_expected;

        double score = 10/remaintime + 10/distance; //note that remain time is also acoount by time.
        // score is always a positive number
        return score; 
    }


}

class TrivalSolution extends Solution {
    Courier courier;
    Objfunction Objf; 
    ArrayList<Node> CourierRoute = new ArrayList<Node>();

    public TrivalSolution(Orders orders, Nodes nodes, Objfunction f, Courier courier, Double[][] truckDistanceMatrix){
        super(orders, nodes, truckDistanceMatrix);
        this.Objf = f;
        this.courier = courier;
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
        int index;

        // Until: All orders has been done.
        while (!orders.allDone()){
            double[] scoreList = new double[orders.numOfOrders];

            // Compute the prioScore for every orders
            for(int i = 0; i < orders.numOfOrders; i++){
                scoreList[i] = computeOrderPrioScore(orders.OrderList[i],courier); 
            }
            /* choose one with highest order with priority score*/
            index = Functions.findMax(scoreList);
            candidateOrder = orders.OrderList[index];
            candidatNode = candidateOrder.getNode();
            
            /* add route to the route ArrayList */
            courier.routeSeq.add(candidatNode);

            /* compute expected arrive time */
            eptArrivetime = courier.time + callNodeDistance(courier.position, candidateOrder.getNode());
            
            /* update order, courier status */
            candidateOrder.update(courier.time);
            courier.position = candidatNode;
            courier.time = eptArrivetime;
        }
    }

    public void genGreedySolution_old(){ 
        /*  currently assume only one courier.
            [ known better approach exists. ]
        */
        //initial the vehicle time = 0;
        double couriertime = 0;
        Order candidateOrder;
        Node candidatNode;
        double eptArrivetime;


        // Until: All orders has been done.
        while (!orders.allDone()){
            double[] scoreList = new double[orders.numOfOrders];

            // Compute the prioScore for every orders
            for(int i = 0; i < orders.numOfOrders; i++){
                scoreList[i] = computeOrderPrioScore_old(orders.OrderList[i],courier.position); 
            }
    
            /*  Loop: Choose the feasible order with highest prio, add to the vehicle route
                this loop is to take care the case that the highest prio node is not prepared(find the next feasible)
                [ known better approach ]: 
                change the time factor in compute PrioSocre: remaintime = max(the courier arrival time, feasible time) */
            int index;
            for(int i = 0; i < orders.numOfOrders; i++){
                index = Functions.findMaxN(scoreList, i);
                candidateOrder = orders.OrderList[index];
                eptArrivetime = couriertime + callNodeDistance(courier.position, candidateOrder.getNode());
                // -- if the expected arrive time is feasible to pickup:
                if(candidateOrder.isFeasible(eptArrivetime)){
                    System.out.println("feasible ?");
                    candidatNode = candidateOrder.getNode(); //candidateOrder.getNode() automatically get the pickup/deliever node
                    //update the vehicle time
                    couriertime = eptArrivetime;
                    //add the node to the route
                    courier.routeSeq.add(candidatNode);  
                    //update the courier status & order status, feasible order, etc..
                    courier.position = candidatNode;
                    candidateOrder.update(couriertime);     //automatically update the status no matter it is a pickup or delivery
                    System.out.println("candidateNode " + candidatNode);
                    CourierRoute.add(candidatNode);
                    //finished find the candidateOrder, escape from the loop.
                    break;
                // -- else:
                }else{
                    //pass this node and try the next node
                    continue;
                }
            }
        }

    }

    public double ObjfValue(){
        return Objf.computeObjValue(this);
    }
    

}



/*class ResupplySolution extends TrivalSolution {

}
*/


class Functions{
    //for static functions only

    static public double computeNorm2Distance(double x1, double y1, double x2, double y2){
        return ( Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)) );
    }

    static public void printMatrix(Double[][] dataMatrix){
        for (int i = 0; i < dataMatrix.length; i++) {
            for (int j = 0; j <dataMatrix[i].length; j++) {  //testValue.get(0) 第一行元素的长度
                System.out.print(dataMatrix[i][j]+"\t");
            }
            System.out.println();
        }
    }

    static public int findMax(double[] matrix){
        //only works for matrix has at least one positive numbers
        //Find the first max number in a matrix
        double max = -1;
        int index = -1;
        for(int i = 0; i < matrix.length; i++){
            if(matrix[i] > max){
                max = matrix[i];
                index = i;
            }
        }
        if(max == -1){
            System.out.println("WARNING!!!! \t Did not find positive number in <FindMax>");
            return -1;
        }
        return index;
    }

    static public int findMaxN(double[] matrix, int n){
        /* count n begin with 0 (0 == the bigest)
        only works for matrix has at least one positive numbers
        Find the first max number in a matrix
        !!! Assume There is No duplicate values !!! */
        double[] temp = matrix.clone();  //For basic type array is deep copy
        Arrays.sort(temp); 
        //Check for duplicate values 
        if(n == 0){
            if(temp[n] == temp[n+1]){
                System.out.println("WARNING!!!! \t duplicate values Trouble in <findMaxN>");
                //return -1;
            }
        }else if(n == matrix.length-1){
            if(temp[n] == temp[n-1]){
                System.out.println("WARNING!!!! \t duplicate values Trouble in <findMaxN>");
                //return -1;
            }
        }else{
            if(temp[n] == temp[n+1] || temp[n] == temp[n-1]){
                System.out.println("WARNING!!!! \t duplicate values Trouble in <findMaxN>");
                //return -1;
            }
        }
        for(int i = 0; i < matrix.length; i++){
            if(matrix[i] == temp[n])
                return i;
        }
        System.out.println("WARNING!!!! \t No match between matrix and temp in <findMaxN>");
        return -1;
    }

    

}

