import java.util.ArrayList;
import java.util.Arrays;
class Solution {
    Orders orders;
    Nodes nodes;
    double[][] distanceMatrix;
    Objfunction Objf;

    public Solution(Orders orders, Nodes nodes/*, Objfunction f*/){
        this.orders = orders;
        this.nodes = nodes;
        /*this.Objf = f;*/
    }

    public Solution(Orders orders, Nodes nodes, double[][] distanceMatrix/*, Objfunction f*/){
        this.orders = orders;
        this.nodes = nodes;
        /*this.Objf = f;*/
    }

    public double callNodeDistance(Node node1 ,Node node2){
        return distanceMatrix[node1.id][node2.id];
    }

    public double computeOrderPrioScore(Order order, Node vehiclePosition){
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

    public TrivalSolution(Orders orders, Nodes nodes, Objfunction f, Courier courier){
        super(orders, nodes);
        this.Objf = f;
        this.courier = courier;
    }

    public void initialGreedySolution(){ 
        /*  currently assume only one courier.
            [ known better approach exists. ]
        */
        //initial the vehicle time = 0;
        double couriertime = 0;
        Order candidate;
        double eptArrivetime;
        Node temp;

        // Until: All orders has been done.
        while (orders.allDone()){
            double[] scoreList = new double[orders.numOfOrders];

            // Compute the prioScore for every orders
            for(int i = 0; i < orders.numOfOrders; i++){
                scoreList[i] = computeOrderPrioScore(orders.OrderList[i],courier.position); 
            }
    
            /*  Loop: Choose the feasible order with highest prio, add to the vehicle route
                this loop is to take care the case that the highest prio node is not prepared(find the next feasible)
                [ known better approach ]: 
                change the time factor in compute PrioSocre: remaintime = max(the courier arrival time, feasible time) */
            int index;
            for(int i = 0; i < orders.numOfOrders; i++){
                index = Functions.findMaxN(scoreList, i);
                candidate = orders.OrderList[index];
                eptArrivetime = couriertime + callNodeDistance(courier.position, candidate.getNode());
                // -- if the expected arrive time is feasible to pickup:
                if(candidate.isFeasible(eptArrivetime)){
                    //update the vehicle time
                    couriertime = eptArrivetime;
                    //add the node to the route
                    courier.routeSeq.add(candidate.getNode());  //candidate.getNode() automatically get the pickup/deliever node
                    //update the order status, feasible order, etc..
                    candidate.update();     //automatically update the status no matter it is a pickup or delivery
                    //finished find the candidate, escape from the loop.
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
                return -1;
            }
        }else if(n == matrix.length-1){
            if(temp[n] == temp[n-1]){
                System.out.println("WARNING!!!! \t duplicate values Trouble in <findMaxN>");
                return -1;
            }
        }else{
            if(temp[n] == temp[n+1] || temp[n] == temp[n-1]){
                System.out.println("WARNING!!!! \t duplicate values Trouble in <findMaxN>");
                return -1;
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

