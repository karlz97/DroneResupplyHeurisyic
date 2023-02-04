import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

class Batch {
    static public void run_resupply(Instance ins, Setting set) {
        
        ResupplySolver solver = new ResupplySolver(ins.orders, ins.nodes, set.objF, set.courierArray, set.droneArray);
        System.out.println("-------------------------   GreedySolution:   --------------------------");
        solver.genGreedySolve();
        solver.printSolution();    
        System.out.println();
        /* call LNS1 to improve the solution */
        // System.out.println("---------------------   LNS1_truck (500) Solution  ---------------------");
        // //solver.LNS1t(500,2); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        // solver.LNS1t(500, 2);
        // System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        // solver.printSolution(); 
        // System.out.println();
        System.out.println("---------------------   LNS1_drone (500) Solution  ---------------------");
        solver.LNS1r(2000,3);
        System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        solver.printSolution();
    }

}

class Instance {
    Nodes nodes;
    Orders orders;
    // ArrayList<Integer> ground_starts;
    // ArrayList<Integer> drone_starts;
    Integer[] ground_starts;
    Integer[] drone_starts;
    Double[][] groundDistanceMatrix;
    Double[][] droneDistanceMatrix; 
    
    public Instance(String path) throws IOException {
        /* Read data */
        Double[][] nodes_data = ReadDataFromCSV.readDoubleToMatrix(path + "/nodes_data.csv");
        ArrayList<Integer[]> start_data = ReadDataFromCSV.readIntToList(path + "/starts.csv");
        // `/starts`第一行是ground staIrts 的 node id（行数）， 第二行是 drone starts 的 node id
        this.groundDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "/Tt.csv");
        this.droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "/insgen/Td.csv"); 
        
        /* turn into useful data structure */
        this.nodes = new Nodes(nodes_data);
        this.orders = new Orders(nodes.orderNodeList);
        // ground_starts =  new ArrayList<Integer>(Arrays.asList(start_data.get(0)));
        // drone_starts = new ArrayList<Integer>(Arrays.asList(start_data.get(1)));
        this.ground_starts = start_data.get(0);
        this.drone_starts = start_data.get(1);
        
    }
}

class Setting {
    ObjF_latePunish objF;
    Courier[] courierArray;
    Drone[] droneArray;

    public Setting(Instance ins, int courier_num, int drone_num, 
                        double drone_max_endurance, double punish_factor) {
        Node[] nodeList = ins.nodes.NodeList;        

        ArrayList<Courier> courierlist = new ArrayList<>();
        ArrayList<Drone> dronelist = new ArrayList<>();
        for (int i = 0; i < courier_num; i++) {
            courierlist.add(new Courier(i, nodeList[ins.ground_starts[i]], ins.groundDistanceMatrix));
        }
        for (int i = 0; i < drone_num; i++) {
            dronelist.add(new Drone(i, nodeList[ins.drone_starts[i]], ins.droneDistanceMatrix, drone_max_endurance));
        }

        this.courierArray = courierlist.toArray(courierArray);
        this.droneArray = dronelist.toArray(droneArray);

        this.objF = new ObjF_latePunish(punish_factor);
    }





}