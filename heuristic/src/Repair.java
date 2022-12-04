import java.util.ArrayList;

public abstract class Repair {
    double value;
    Order order;
    public Repair(){}


    abstract boolean takeEffect();
    abstract boolean takeEffect_hold();
    abstract MeetPoint getMeetPoint();
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

    // public Repair_1dc(Courier courier, ArrayList<Node> routeSeq, Drone drone, ArrayList<Flight> flightSeq, MeetPoint mp, double value){
    //     this.drone = drone;
    //     this.flightSeq = flightSeq;
    //     this.value = value;
    // }

    @Override
    public boolean takeEffect() {
        this.value = Integer.MIN_VALUE;
        return this.takeEffect_hold();
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

}