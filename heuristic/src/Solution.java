import javax.swing.plaf.ColorUIResource;

class Solution {

    int[][] X; //decsion variable for MainVehicle
    Orders orders;
    Nodes nodes;
    Courier courier;
    Drone Drone;
    double[][] distanceMatrix;


}

class TrivalSolution extends Solution {



    public void GreedySolution(){ 
        //initial the vehicle time = 0;
        // Until: All orders has been done.
            // Compute the prioScore for every orders
            // Loop: Choose the order with highest prio,
                // -- if the expected arrive time is feasible to pickup:
                        //add the node to the route
                        //update the vehicle time
                        //update the order status, feasible order, etc..
                // -- else:
                        //pass this node and try the next node
            
    }
    

}



class ResupplySolution extends Solution {

}



class Functions{

    public double[] computePrioScore(){
            // Get the distance of every feasible nodes
            // add Prio base on the how close is the expected delivery time

            
        return[] 
    }

}