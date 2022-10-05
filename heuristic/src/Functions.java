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

    static public void printSolution_Flight(Solution s) {
        if (s.flightSeqs != null) {
            for (int i = 0; i < s.flightSeqs.length; i++) {
                System.out.println("Flights[" + i + "]:");
                LinkedList<Node> flightSeq =  s.flightSeqs[i];
                for (Node node : flightSeq) {
                    System.out.print( node.id + " --> ");
                }
                System.out.println();
            }
        } else {
            System.out.println("flightseq is empty");  
        }
    }
    
    static public void printSolution_Courier(Solution s, Orders orders) {
        System.out.println("Routes: ");
        for (Iterator<Node> it = s.courierRoute.iterator(); it.hasNext();) {
            System.out.print( it.next().id + " --> ");
            // debug::: System.out.print( it.next().id + "(" +  + ")" + " --> ");
        }
        if (orders.allDone()) {
            System.out.println("finished.");   
        }else{
            System.out.println("unfinieshed.");
        }
    }
}

