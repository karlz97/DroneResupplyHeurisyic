import java.util.ArrayList;

public abstract class Repair {
    double objValue;
    public Repair(){}

    abstract boolean takeEffect();

}

class Repair_1c extends Repair{ 
    Courier courier; //This is only a reference to the courier
    ArrayList<Node> routeSeq; //This is a solid copy of route

    public Repair_1c(){
        objValue = Integer.MIN_VALUE;
    }

    public Repair_1c(Courier courier, ArrayList<Node> routeSeq, double objValue){
        this.courier = courier;
        this.routeSeq = routeSeq;
        this.objValue = objValue;
    }

    @Override
    public boolean takeEffect() {
        this.courier.routeSeq = this.routeSeq;
        return true;
    }

}