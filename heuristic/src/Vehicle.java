import javax.swing.text.Position;

class Vehicle {
    int id;
    double speed;
    Node position;
    double time;

}


class Courier extends Vehicle{
    public Courier(int id, double speed, Node startPosition){
        this.id = id;
        this.speed = speed;
        this.position = startPosition; 
    }
}

class Drone extends Vehicle{

}


