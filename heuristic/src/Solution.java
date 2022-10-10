import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Solution {
    double objfValue;
    ArrayList<Node>[] courierRoutes;
    LinkedList<Node>[] flightSeqs;
    public Solution(Solution s) {
        this.objfValue = s.objfValue;
        courierRoutes = new ArrayList[s.courierRoutes.length];
        for (int i = 0; i < s.courierRoutes.length; i++) {
            this.courierRoutes[i] = new ArrayList<>(s.courierRoutes[i]);
        }   
        if (s.flightSeqs != null) {   
            flightSeqs = new LinkedList[s.flightSeqs.length];
            for (int i = 0; i < s.flightSeqs.length; i++) {
                flightSeqs[i] = new LinkedList<>(s.flightSeqs[i]);
            }   
        }
    }

    public Solution(List<Node>[] courierRoutes) {   
        //如果减少对新建solution的调用，在一个solution上更改，可以节省反复创建ArrayList
            for (int i = 0; i < courierRoutes.length; i++) {
                this.courierRoutes[i] = new ArrayList<>(courierRoutes[i]);  
            }
        }

    public Solution(Courier[] courierList) {
        for (int i = 0; i < courierList.length; i++) {
            this.courierRoutes[i] = courierList[i].routeSeq; 
        }
    }

    public Solution(List<Node>[] courierRoutes, List<Node>[] flightSeqs) {
        this(courierRoutes);
        this.flightSeqs = new LinkedList[flightSeqs.length];
        for (int i = 0; i < flightSeqs.length; i++) {
            this.flightSeqs[i] = new LinkedList<Node>(flightSeqs[i]);
        }
    }

    public Solution(Courier[] courierList, Drone[] droneList) {
        this(courierList);
        flightSeqs = new LinkedList[droneList.length];
        for (int i = 0; i < droneList.length; i++) {
            flightSeqs[i] = serializeFlights(droneList[i].flights);
        }
    }

    public static LinkedList<Node> serializeFlights(ArrayList<Flight> flights) {
        LinkedList<Node> flightSeq = new LinkedList<>();
        for (Flight flight : flights) {
            flightSeq.offer(flight.launchNode);
            flightSeq.offer(flight.pickupNode);
            flightSeq.offer(flight.supplyNode);
            flightSeq.offer(flight.landNode);
        }
        return flightSeq;
    }

    public static ArrayList<Flight> deSerializeFlights(LinkedList<Node> flightSeq) {
        if (flightSeq == null) {
            return new ArrayList<Flight>();
        }
        
        ArrayList<Flight> flights = new ArrayList<>();
        for (int i = 0; i < flightSeq.size();) {
            Node launchNode = flightSeq.get(i++);
            Node pickupNode = flightSeq.get(i++);
            Node supplyNode = flightSeq.get(i++);
            Node landNode = flightSeq.get(i++);
            flights.add( new Flight(launchNode, pickupNode, supplyNode, landNode) );
        }
        return flights;
    }

    public ArrayList<Flight> deSerializeFlights(int droneId) {
        return deSerializeFlights(flightSeqs[droneId]);
    }

    
}

class oneInsertOfCourier { 
    Courier courier; //This is only a reference to the courier
    ArrayList<Node> routeSeq; //This is a solid copy of route
    double objValue;

    public oneInsertOfCourier(){
        objValue = Integer.MIN_VALUE;
    }

    public oneInsertOfCourier(Courier courier, ArrayList<Node> routeSeq, double objValue){
        this.courier = courier;
        this.routeSeq = routeSeq;
        this.objValue = objValue;
    }

    public void takeEffect() {
        this.courier.routeSeq = this.routeSeq;
    }
}
