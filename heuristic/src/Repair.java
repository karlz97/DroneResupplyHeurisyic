import java.util.ArrayList;

public abstract class Repair {
    double value;
    Order order;
    public Repair(){}


    abstract boolean takeEffect();
    abstract boolean takeEffect_hold();
    abstract MeetPoint getMeetPoint();
    abstract void attachMeetNode();
    abstract void detachMeetNode();
}

class Repair_1cc extends Repair{ 
    Courier courier; //This is only a reference to the courier
    ArrayList<Node> routeSeq; //This is a solid copy of route

    public Repair_1cc(){
        value = Integer.MIN_VALUE;
    }

    public Repair_1cc(Courier courier){
        this();
        this.courier = courier;
    }

    public Repair_1cc(Courier courier, ArrayList<Node> routeSeq, double value){
        this.courier = courier;
        this.routeSeq = routeSeq;
        this.value = value;
    }

    @Override
    public boolean takeEffect() {
        this.value = Integer.MIN_VALUE;
        return this.takeEffect_hold();
    }

    @Override
    public boolean takeEffect_hold() {
        this.courier.routeSeq = this.routeSeq;
        return true;
    }

    @Override
    public MeetPoint getMeetPoint() {
        return null;
    }

    @Override
    void attachMeetNode() {
        return;
        // MeetPoint mp = new MeetPoint(c, d, n);
        // meetPointsMap.put(n, mp)
    }

    @Override
    void detachMeetNode() {
        return;
    }
}

class Repair_1dc extends Repair{ 
    Courier courier; //This is only a reference to the courier
    ArrayList<Node> routeSeq; //This is a solid copy of route
    Drone drone; //This is only a reference to the courier
    ArrayList<Flight> flightSeq; //This is a solid copy of route
    Node meetNode;
    

    public Repair_1dc(){
        value = Integer.MIN_VALUE;
    }
    public Repair_1dc(Courier courier, Drone drone){
        this.courier = courier;
        this.drone = drone;
    }

    public Repair_1dc(Courier courier, Drone drone, Node meetNode){
        this.courier = courier;
        this.drone = drone;
        this.meetNode = meetNode;
        this.routeSeq = new ArrayList<>(courier.routeSeq);
        this.flightSeq = new ArrayList<>(drone.flights);
    }

    public Repair_1dc(MeetPoint mp){
        this.courier = mp.courier;
        this.drone = mp.drone;
        this.meetNode = mp.meetNode;
        this.routeSeq = new ArrayList<>(mp.courier.routeSeq);
        this.flightSeq = new ArrayList<>(mp.drone.flights);
    }

    // public Repair_1dc(Courier courier, ArrayList<Node> routeSeq, Drone drone, ArrayList<Flight> flightSeq, MeetPoint mp, double value){
    //     this.drone = drone;
    //     this.flightSeq = flightSeq;
    //     this.value = value;
    // }

    @Override
    public boolean takeEffect() {
        this.value = Integer.MIN_VALUE;
        this.takeEffect_hold();
        this.attachMeetNode();
        return true;
    }

    @Override
    public boolean takeEffect_hold() {
        this.courier.routeSeq = this.routeSeq;
        this.drone.flights = this.flightSeq;
        return true;
    }


    @Override
    public MeetPoint getMeetPoint() {
        return new MeetPoint(courier, drone, meetNode);
    }
    
    @Override
    void attachMeetNode() {
        meetNode.isMeet = true;
        meetNode.meetCourier = courier;
        meetNode.meetDrone = drone;
        // MeetPoint mp = new MeetPoint(c, d, n);
        // meetPointsMap.put(n, mp)
    }

    @Override
    void detachMeetNode() {
        meetNode.isMeet = false;
        meetNode.meetCourier = null;
        meetNode.meetDrone = null;
    }

}