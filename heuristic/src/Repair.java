import java.util.ArrayList;

public abstract class Repair {
    double value;
    public Repair(){}

    abstract boolean takeEffect();

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
        this.courier.routeSeq = this.routeSeq;
        return true;
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
        this.drone.flights = this.flightSeq;
        return true;
    }
}