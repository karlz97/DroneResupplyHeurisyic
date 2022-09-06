import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


abstract class Solver {
    Orders orders;
    Nodes nodes;
    Double[][] distanceMatrix; //距离矩阵应该合并到courier中
    Objfunction Objf;
    Solution globalOptSolution;

    public Solver(Orders orders, Nodes nodes/*, Objfunction f*/){
        this.orders = orders;
        this.nodes = nodes;
        /*this.Objf = f;*/
    }

    public Solver(Orders orders, Nodes nodes, Double[][] distanceMatrix/*, Objfunction f*/){
        this.orders = orders;
        this.nodes = nodes;
        this.distanceMatrix = distanceMatrix;
        /*this.Objf = f;*/
    }

    abstract public double ObjfValue();
    
    abstract public void printSolution();

    abstract public void recoverFromSolution(Solution solution);

    abstract void instantiateSolution();  //instantiate the Solution including setup from route.

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

    public Object getitem(int i) {     // remove i and return indexlist[i]
        Object item;
        item = indexlist.get(i);
        indexlist.remove(i);
        list.remove(i);
        currlen -- ;
        return item;
    }

    public void inpool(double value, Object index) {
        //System.out.println("currlen:" + currlen + "truelen" + list.size());
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


    private void inhead(double value, Object index) {
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
        for (int i = 0; i < currlen; i ++) {
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


class Solution {
    double objfValue;
    ArrayList<Node> courierRoute;
    LinkedList<Node>[] flightSeqs;
    public Solution(Solution s) {
        this.objfValue = s.objfValue;
        this.courierRoute = new ArrayList<>(s.courierRoute);
        if (s.flightSeqs != null) {   
            flightSeqs = new LinkedList[s.flightSeqs.length];
            for (int i = 0; i < s.flightSeqs.length; i++) {
                flightSeqs[i] = new LinkedList<>(s.flightSeqs[i]);
            }   
        }
    }

    public Solution(Courier courier) {
        courierRoute = new ArrayList<Node>(courier.routeSeq);
    }


    public Solution(Courier courier, Drone[] drones) {
        this(courier);
        flightSeqs = new LinkedList[drones.length];
        for (int i = 0; i < drones.length; i++) {
            flightSeqs[i] = serializeFlights(drones[i].flights);
        }
    }

    private LinkedList<Node> serializeFlights(ArrayList<Flight> flights) {
        LinkedList<Node> flightSeq = new LinkedList<>();
        for (Flight flight : flights) {
            flightSeq.offer(flight.launchNode);
            flightSeq.offer(flight.pickupNode);
            flightSeq.offer(flight.supplyNode);
            flightSeq.offer(flight.landNode);
        }
        return flightSeq;
    }

    public ArrayList<Flight> deSerializeFlights(LinkedList<Node> flightSeq) {
        if (flightSeq == null) {
            return new ArrayList<Flight>();
        }
        
        ArrayList<Flight> flights = new ArrayList<>();
        for (int i = 0; i < flightSeq.size(); i++) {
            Node launchNode = flightSeq.poll();
            Node pickupNode = flightSeq.poll();
            Node supplyNode = flightSeq.poll();
            Node landNode = flightSeq.poll();
            flights.add( new Flight(launchNode, pickupNode, supplyNode, landNode) );
        }
        return flights;
    }

    public ArrayList<Flight> deSerializeFlights(int droneId) {
        return deSerializeFlights(flightSeqs[droneId]);
    }

    
}