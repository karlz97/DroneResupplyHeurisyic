import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
class Main2 {
    public static void main(String[] args) throws IOException {
        // String path = "../insgen/";
        String path = "../eleme_test/0301_rg_0_sec_1/";
        long stime = System.currentTimeMillis();
        main_multi(path);
        // main2_multi(path);
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("the program takes: %d s.", (etime - stime)/1000);
    }
    private static void main_multi(String path) throws IOException{
        Instance ins = new Instance(path);
        int Nc = 3, Nd = 1;
        double drone_max_endurance = 20, punish_factor = 60;
        Setting set_i = new Setting(ins, Nc, Nd, drone_max_endurance, 60);
        Batch.run_resupply(ins, set_i);
    }
}

class Batch {
    static public double[] run_resupply(Instance ins, Setting set) {
        ResupplySolver solver = new ResupplySolver(ins.orders, ins.nodes, set.objF, set.courierArray, set.droneArray);
        System.out.println("-------------------------   GreedySolution:   --------------------------");
        solver.genGreedySolve();
        solver.printSolution();    
        System.out.println();
        /* call LNS1 to improve the solution */
        System.out.println("---------------------   LNS1_truck (500) Solution  ---------------------");
        //solver.LNS1t(500,2); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        solver.LNS2t(250, 4);
        double[] evaluation = new double[6];
        evaluation[0] = solver.ObjfValue();
        evaluation[1] = Evaluations.get_delay_rate(solver);
        evaluation[2] = Evaluations.get_total_delays(solver);
        System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        solver.printSolution(); 
        System.out.println("---------------------   LNS1_drone (500) Solution  ---------------------");
        solver.LNS1r(1000,3);
        System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        solver.printSolution();
        evaluation[3] = solver.ObjfValue();
        evaluation[4] = Evaluations.get_delay_rate(solver);
        evaluation[5] = Evaluations.get_total_delays(solver);
        Functions.printDebug("ground solution: \n Objf:" + evaluation[0] +  
                                " delay_rate:" + evaluation[1] +
                                " total_delay:" + evaluation[2] +
                            "\ndrone solution: \n Objf:" + evaluation[3] +  
                            " delay_rate:" + evaluation[4] +
                            " total_delay:" + evaluation[5]);
        
        
        return evaluation; 
    }


}

class Instance {
    String name;
    Nodes nodes;
    Orders orders;
    Double[][] groundDistanceMatrix;
    Double[][] droneDistanceMatrix; 
    ArrayList<Node> droneBaseSet;
    // Integer[] ground_starts;
    // Integer[] drone_starts;

    public Instance(String path) throws IOException {
        /* Read data */
        String[] components = path.split("/");
        this.name = components[components.length - 1];         
        Double[][] nodes_data = ReadDataFromCSV.readDoubleToMatrix(path + "/exNodes.csv");
        // `/starts`第一行是ground staIrts 的 node id（行数）， 第二行是 drone starts 的 node id
        this.groundDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "/Tt.csv");
        this.droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "/Td.csv"); 
        /* turn into useful data structure */
        this.nodes = new Nodes(nodes_data);
        this.orders = new Orders(nodes.orderNodeList);
        this.droneBaseSet = new ArrayList<>();
        for (Node n : nodes.NodeList)
            if (n.isDrbs == true)
                droneBaseSet.add(n);
        // ArrayList<Integer[]> start_data = ReadDataFromCSV.readIntToList(path + "/starts.csv");
        // this.ground_starts = start_data.get(0);
        // this.drone_starts = start_data.get(1);
    }
}

class Setting {
    ObjF_latePunish objF;
    Courier[] courierArray;
    Drone[] droneArray;

    public Setting(Instance ins, int courier_num, int drone_num, 
                        double drone_max_endurance, double punish_factor) {
        Node[] nodeList = ins.nodes.NodeList;        
        Node startnode = new StartEndNode(0, 0, 0, 's');
        Random rand = new Random();
        Node dstartnode = ins.droneBaseSet.get(rand.nextInt(ins.droneBaseSet.size()));
        ArrayList<Courier> courierlist = new ArrayList<>();
        ArrayList<Drone> dronelist = new ArrayList<>();
        for (int i = 0; i < courier_num; i++) {
            courierlist.add(new Courier(i, startnode, ins.groundDistanceMatrix));
        }
        for (int i = 0; i < drone_num; i++) {
            dronelist.add(new Drone(i, dstartnode, ins.droneDistanceMatrix, drone_max_endurance));
        }
        this.courierArray = new Courier[courier_num];
        this.droneArray = new Drone[drone_num];
        this.courierArray = courierlist.toArray(courierArray);
        this.droneArray = dronelist.toArray(droneArray);
        for (Drone d : droneArray) {
            d.computeFeasibleFlight(nodeList);
            d.showFeasibleFlight();
        }
        this.objF = new ObjF_latePunish(punish_factor);
    }




}