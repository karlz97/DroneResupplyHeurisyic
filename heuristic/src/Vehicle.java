import java.util.ArrayList;

import javax.swing.text.Position;
abstract class Vehicle {
    int id;
    double speed;
    Node position;
    double time;
    ArrayList<Node> routeSeq; //直接引用NodeList里面的Node不用初始化
    ArrayList<Integer> timeSeq; 
    // ArrayList<Route> routeSeq //方便起见没有再创类，免得每次使用还得初始化
}



class Courier extends Vehicle{
    public Courier(int id, double speed, Node startPosition){
        this.id = id;
        this.speed = speed;
        this.position = startPosition; 
        this.time = 0;
        routeSeq = new ArrayList<Node>();
        routeSeq.add(position); //将起始点加入
        timeSeq = new ArrayList<Integer>();
    }
}

class Drone extends Vehicle{

}


/*
private class Route {

} 
*/

