import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
abstract class Solution {
    Orders orders;
    Nodes nodes;
    Double[][] distanceMatrix;
    Objfunction Objf;
    Vehicle[] vehicleList;

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

    abstract public double ObjfValue();
    
    abstract public void printSolution();

    abstract void instantiateSolution(ArrayList<Node> routeSeq);  //instantiate the Solution including setup from route.

    public double callNodeDistance(Node node1 ,Node node2){
        return distanceMatrix[node1.id][node2.id];
    }

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
            return -1;
        }
        // Get the distance between the order corresbonding nodes and current vehicle position
        /*// Norm2 approach 
        double x_order = orderNode.coord[0];
        double y_order = orderNode.coord[1];
        double x_vhcle  = vehiclePosition.coord[0];
        double y_vhcle  = vehiclePosition.coord[1];
        double distance = Functions.computeNorm2Distance(x_order, y_order, x_vhcle, y_vhcle); */
        return Math.max(prepared_lfn, callNodeDistance(orderNode, vehicle.position));
    }

    public double earlistExecuteTime(Order order, Vehicle vehicle){
        return vehicle.time + earlistExecuteTime_lfn(order, vehicle);
    }

    public double earlistExecuteTime(Node node, Vehicle vehicle){
        return vehicle.time + callNodeDistance(node, vehicle.position);
    }

    public double computeOrderPrioScore(Order order, Vehicle vehicle){
        // Earilst pickup time left from now 
        double earilstPickup_lfn = earlistExecuteTime_lfn(order, vehicle); 

        // how close is the expected delivery time
        double remaintime = order.T_expected - vehicle.time;

        //double score = Math.max(2/remaintime,0) + 10/earilstPickup_lfn; //note that remain time is also acoount by time.
        double score =  10/earilstPickup_lfn;
        //double score =  max(10/earilstPickup_lfn - remaintime);

        // score must always be a positive number
        return score; 
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

    static public void printRouteSeq(List<Node> routeSeq){
        System.out.println("Routes: ");
        for (Iterator<Node> it = routeSeq.iterator(); it.hasNext();) {
            System.out.print( it.next().id + " --> ");
        }
        System.out.print("over");
        System.out.println();
    }

    static public void printOrderList(List<Order> orderList){
        System.out.println("OrderList: ");
        for (Iterator<Order> it = orderList.iterator(); it.hasNext();) {
            System.out.print( it.next().id + ", " );
        }
        System.out.println();
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
        /*  Check for duplicate values 
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
        }*/
        for(int i = 0; i < matrix.length; i++){
            if(matrix[i] == temp[n])
                return i;
        }
        System.out.println("WARNING!!!! \t No match between matrix and temp in <findMaxN>");
        return -1;
    }

    

}

class OddPool {  //OddPool for 'ordered Pool'
    LinkedList<Double> list;
    LinkedList<Object> indexlist;
    int currlen;
    final int length;
    public OddPool(int length) {
        list = new LinkedList<Double>(); 
        indexlist = new LinkedList<Object>(); 
        this.currlen = 0;
        this.length = length;
    }

    public void inpool(double value, Object index){
        System.out.println("currlen:" + currlen + "truelen" + list.size());
        if (currlen > 0 ) {
            if (value > list.getFirst() ){   //smaller than the last 
                inhead(value, index);
            } else if (value > list.getLast() || currlen < length ) {
                inbody(value, index);
            } 
        } else {
            list.add(value);
            indexlist.add(index);
            currlen ++;
        }
    }


    private void inhead(double value, Object index){
        list.addFirst(value);
        indexlist.addFirst(index);
        if (currlen == length) {
            list.removeLast();
            indexlist.removeLast();
        } else {
            currlen ++;
        }
    }

    private void inbody(double value, Object index){
        for (int i = 0; i < currlen; i++) {
            if ( value > list.get(i) ) {
                list.add(i, value);
                indexlist.add(i,index);
                break;
            } else {
                list.addLast(value);
                indexlist.addLast(index);
                break;
            }
        }
        if (currlen == length) {
            list.removeLast();
            indexlist.removeLast();
        } else {
            currlen ++;
        }
    }



}

class PseudoSolution {
    ArrayList<Node> routeSeq;
    double objValue;

    public PseudoSolution(ArrayList<Node> routeSeq, double objValue){
        this.routeSeq = routeSeq;
        this.objValue = objValue;
    }
}
