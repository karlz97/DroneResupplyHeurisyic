import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


class Functions{
    //for static functions only
    static boolean debug = true;
    static public void printAlert(String s){
        System.out.println("\033[31;1m" + "\t<Alert!>\t" + s + "\033[0m");
    }

    static public void printDebug(String s){
        if (debug) {
            System.out.println("\033[33m" + s + "\033[0m");
        }
    }


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

    static public ArrayList<Node> buildNodeSeqFromArray(Integer[] routeArray, Node startnode, Node[] NodeList){
        ArrayList<Node> routeSeq = new ArrayList<>();
        if (startnode != null)
            routeSeq.add(startnode);
        for (int i = 0; i < routeArray.length; i++) {
            if (routeArray[i] == -1)
                routeSeq.add(null);
            else
                routeSeq.add(NodeList[routeArray[i]]);
        }
        
        return routeSeq;
    }


    static public void printRouteSeq(List<Node> routeSeq){
        System.out.println("Routes: ");
        for (Iterator<Node> it = routeSeq.iterator(); it.hasNext();) {
            System.out.print( it.next().id + " --> ");
        }
        System.out.print("over");
        System.out.println();
    }

    static public void printRouteSeq_with_time(List<Node> routeSeq){
        System.out.println("Routes: ");
        for (Iterator<Node> it = routeSeq.iterator(); it.hasNext();) {
            Node n = it.next();
            System.out.print( n.id + "[" + n.T_courier + "] --> ");
        }
        System.out.print("over");
        System.out.println();
    }

    static public void printFlights(ArrayList<Flight> flights){
        List<Node> flightSeq = Solution.serializeFlights(flights);
        System.out.print("Flight: ");
        int counter = 0;
        for (Node n : flightSeq) {  
            if ((++counter - 1)% 4 == 0 && counter != 1) 
                System.out.print( " | ");  
            if (n == null)
                continue;   
            if (n.isMeet) 
                System.out.print( n.id + "[" + n.T_drone + "] --> ");  
            else
                System.out.print( n.id + " --> ");
        }
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
        for(int i = 0; i < matrix.length; i++){
            if(matrix[i] == temp[n])
                return i;
        }
        System.out.println("WARNING!!!! \t No match between matrix and temp in <findMaxN>");
        return -1;
    }

    static boolean checkDuplicate(ArrayList<Order> orderslist) {


        int size1 = orderslist.size();
        int size2 = new HashSet<Order>(orderslist).size();

        if (size1 != size2) {
            printAlert("Duplicate orders in orderlist!!");
            for (Order order : orderslist) {
                System.out.print(order.id + ",");
            }
            System.out.println();
            return true;
        }
        return false;
    }

    static boolean checkDuplicate(ArrayList<Order> orderslist, ArrayList<Node> routeseq) {
        int size1 = orderslist.size();
        int size2 = new HashSet<Order>(orderslist).size();

        if (size1 != size2) {
            printAlert("Duplicate orders in orderlist!!");
            for (Order order : orderslist) {
                System.out.print(order.id + ",");
            }
            System.out.println();
            return true;
        }
        return false;
    }

}

//TODO 没搞懂，list和indexlist是不是搞反了？？
class OddPool {  //OddPool for 'ordered Pool'
    // A map: valuelist --> indexlist
    // ordered by valuelist from big to small

    //Since this.length will not be large, it should be enough to use this implementation instead of Max Heap implementation
    // This is a low effeciency naive implementation of PriorityQueue
    LinkedList<Double> valuelist;
    LinkedList<Object> indexlist;
    int currlen;
    final int length;
    public OddPool(int length) {
        valuelist = new LinkedList<Double>(); 
        indexlist = new LinkedList<Object>(); 
        this.currlen = 0;
        this.length = length;
    }

    public int size() {
        return currlen;
    }

    public Object takeitem(int i) {     // remove i and return indexlist[i]
        Object item;
        item = indexlist.get(i);
        indexlist.remove(i);
        valuelist.remove(i);
        currlen -- ;
        return item;
    }

    public void inpool(double value, Object index) {
        if (currlen == length && valuelist.getLast() > value) {
            return;   
        }
        //System.out.println("currlen:" + currlen + "truelen" + list.size());
        if (currlen > 0 ) {
            if (value > valuelist.getFirst() ){   //smaller than the last 
                inhead(value, index);
            } else if (value > valuelist.getLast() || currlen < length ) {
                inbody(value, index);
            } 
        } else {
            valuelist.add(value);
            indexlist.add(index);
            currlen ++;
        }
    }


    private void inhead(double value, Object index) {
        valuelist.addFirst(value);
        indexlist.addFirst(index);
        if (currlen == length) {
            valuelist.removeLast();
            indexlist.removeLast();
        } else {
            currlen ++;
        }
    }

    private void inbody(double value, Object index){
        boolean already_in = false;
        for (int i = 0; i < currlen; i ++) {
            if ( value > valuelist.get(i) ) {
                valuelist.add(i, value);
                indexlist.add(i,index);
                already_in = true;
                break;
            } 
        }
        if(!already_in) {
            valuelist.addLast(value);
            indexlist.addLast(index);
        }
        if (currlen == length) {
            valuelist.removeLast();
            indexlist.removeLast();
        } else {
            currlen ++;
        }
    }
}