import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Main {
    public static void main(String[] args) throws IOException {
        // String path = "../insgen/";
        String path = "../eleme_test/0301_rg_0_sec_1/";

        long stime = System.currentTimeMillis();
        //main3_multi(path);
        batch_main1(path);
        // main2_multi(path);
        long etime = System.currentTimeMillis();

        // 计算执行时间
        System.out.printf("the program takes: %d s.", (etime - stime)/1000);
    }

    private static void batch_main1(String path) throws IOException {
        Instance ins = new Instance(path);
        int Nc = 5, Nd = 1;
        double drone_max_endurance = 20, punish_factor = 60;
        Setting set_i = new Setting(ins, Nc, Nd, drone_max_endurance, punish_factor);
        Batch.run_resupply(ins, set_i);
    }
    
    private static void main3_multi(String path) throws IOException{
        Double[][] dataMatrix;
        Double[][] truckDistanceMatrix;
        Double[][] droneDistanceMatrix;
        Nodes nodes;
        Orders orders;
        /* readData from csv */
        dataMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "exNODES.csv");
        truckDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "Tt.csv");
        droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "Td.csv"); 
        
        /* initialize Nodes */
        nodes = new Nodes(dataMatrix);
        Node startnode = new StartEndNode(0, 0, 0, 's'); //在这个测试例子中courier的startnode是第13个，对应distanceMatrix中第12
    
        /* initialize Orders(by orderNodeList) */
        orders = new Orders(nodes.orderNodeList);  
        
        /* find a drone-base node */
        ArrayList<Node> droneBaseSet = new ArrayList<>();
        for (Node n : nodes.NodeList)
            if (n.isDrbs == true)
                droneBaseSet.add(n);
        
        
        /* initialize vehicle */
        // Courier[] courierList = new Courier[]{new Courier(0, startnode, truckDistanceMatrix), new Courier(1, startnode, truckDistanceMatrix), new Courier(2, startnode, truckDistanceMatrix)};
        Courier[] courierList = new Courier[]{
            new Courier(0, startnode, truckDistanceMatrix), 
            new Courier(1, startnode, truckDistanceMatrix), 
            new Courier(2, startnode, truckDistanceMatrix),
            new Courier(3, startnode, truckDistanceMatrix),
            new Courier(4, startnode, truckDistanceMatrix)
        };
        
        Random rand = new Random();
        double MAXFILGHT = 20; //In minutes 0 or 20
        Drone[] droneList = new Drone[]{new Drone(0, droneBaseSet.get(rand.nextInt(droneBaseSet.size())), droneDistanceMatrix, MAXFILGHT)
                                        // ,new Drone(1, droneBaseSet.get(rand.nextInt(droneBaseSet.size())), droneDistanceMatrix, MAXFILGHT)
                                    };
        for (Drone d : droneList) {
            d.computeFeasibleFlight(nodes.NodeList);
            d.showFeasibleFlight();
        }

        /* initialize solution */
        ObjF_latePunish objF = new ObjF_latePunish(60);
        //TrivalSolver solver = new TrivalSolver(orders, nodes, objF, courier, truckDistanceMatrix); 
        ResupplySolver solver = new ResupplySolver(orders, nodes, objF, courierList, droneList);

        /* call generate greedy solution */
        System.out.println("-------------------------   GreedySolution:   --------------------------");
        solver.genGreedySolve();
        Solution initiaSolution = new Solution(solver.globalOptSolution);
        solver.printSolution();    
        System.out.println();
        /* call LNS1 to improve the solution */
        System.out.println("---------------------   LNS1_truck (500) Solution  ---------------------");
        //solver.LNS1t(500,2); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        solver.LNS2t(2000, 4);
        double[] ground_evaluation = new double[3];
        ground_evaluation[0] = solver.ObjfValue();
        ground_evaluation[1] = Evaluations.get_delay_rate(solver);
        ground_evaluation[2] = Evaluations.get_total_delays(solver);
        System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        solver.printSolution(); 
        System.out.println();    
        // solver.globalOptSolution = new Solution(initiaSolution);
        // solver.printSolution();
        System.out.println("---------------------   LNS1_drone (500) Solution  ---------------------");
        solver.LNS1r(2500,3);
        System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        solver.printSolution();
        double[] drone_evaluation = new double[3];
        drone_evaluation[0] = solver.ObjfValue();
        drone_evaluation[1] = Evaluations.get_delay_rate(solver);
        drone_evaluation[2] = Evaluations.get_total_delays(solver);
        Functions.printDebug("ground solution: \n Objf:" + ground_evaluation[0] +  
                                " delay_rate:" + ground_evaluation[1] +
                                " total_delay:" + ground_evaluation[2] +
                            "\ndrone solution: \n Objf:" + drone_evaluation[0] +  
                            " delay_rate:" + drone_evaluation[1] +
                            " total_delay:" + drone_evaluation[2]);

    }    

    private static void main2_multi(String path) throws IOException{
        Double[][] dataMatrix;
        Double[][] truckDistanceMatrix;
        Double[][] droneDistanceMatrix;
        Nodes nodes;
        Orders orders;
        /* readData from csv */
        dataMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "exNODES.csv");
        truckDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "Tt.csv");
        droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "Td.csv"); 
        
        /* initialize Nodes */
        nodes = new Nodes(dataMatrix);
        Node startnode = new StartEndNode(12, 1, 1, 's'); 
        //在这个测试例子中courier的startnode是第13个，对应distanceMatrix中第12
    
        /* initialize Orders(by orderNodeList) */
        orders = new Orders(nodes.orderNodeList);
        
        /* find a drone-base node */
        Node droneStartNode = null;
        for (Node n : nodes.NodeList)
            if (n.isDrbs == true)
                droneStartNode = n;  
        
        /* initialize vehicle */
        Courier[] courierList = new Courier[]{
            new Courier(0, startnode, truckDistanceMatrix), 
            new Courier(1, startnode, truckDistanceMatrix), 
            new Courier(2, startnode, truckDistanceMatrix),
            //new Courier(3, startnode, truckDistanceMatrix),
            //new Courier(4, startnode, truckDistanceMatrix)
        };
        Drone[] droneList = new Drone[]{new Drone(0, droneStartNode, droneDistanceMatrix, 15)};


        /* initialize solution */
        ObjF_latePunish objF = new ObjF_latePunish(10);
        TrivalSolver solver = new TrivalSolver(orders, nodes, objF, courierList); 
        //ResupplySolver solver = new ResupplySolver(orders, nodes, objF, courier, droneList, truckDistanceMatrix);


        /* call generate greedy solution */
        System.out.println("-------------------------   GreedySolution:   --------------------------");
        solver.genGreedySolve();
        solver.printSolution();    
        System.out.println();
        /* call LNS1 to improve the solution */
        // System.out.println("---------------------   LNS1t (500) Solution  ---------------------");
        // solver.LNS1t(500,3); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        // solver.printSolution(); 
        // System.out.println();
        System.out.println("---------------------   LNS1t2 (500) Solution  ---------------------");
        // solver.LNS1t(2000, 3);
        solver.LNS1t2(2000, 3);
        System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        solver.printSolution(); 
        System.out.println();
    }  

    private static void main3() throws IOException{
        Double[][] dataMatrix;
        Double[][] truckDistanceMatrix;
        Double[][] droneDistanceMatrix;
        Nodes nodes;
        Orders orders;
        /* readData from csv */
        dataMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/exNODES.csv");
        truckDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/Tt.csv");
        droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/Td.csv"); 
        
        /* initialize Nodes */
        nodes = new Nodes(dataMatrix);
        Node startnode = new StartEndNode(12, 1, 1, 's')    ; //在这个测试例子中courier的startnode是第13个，对应distanceMatrix中第12
    
        /* initialize Orders(by orderNodeList) */
        orders = new Orders(nodes.orderNodeList);  

        /* find a drone-base node */
        Node droneStartNode = null;
        for (Node n : nodes.NodeList)
            if (n.isDrbs == true)
                droneStartNode = n;
        
        /* initialize vehicle */
        Courier[] courierList = new Courier[]{new Courier(0, startnode, truckDistanceMatrix)};
        Drone[] droneList = new Drone[]{new Drone(0, droneStartNode, droneDistanceMatrix, 15)};
        droneList[0].computeFeasibleFlight(nodes.NodeList);
        droneList[0].showFeasibleFlight();


        /* initialize solution */
        ObjF_latePunish objF = new ObjF_latePunish(1);
        //TrivalSolver solver = new TrivalSolver(orders, nodes, objF, courier, truckDistanceMatrix); 
        ResupplySolver solver = new ResupplySolver(orders, nodes, objF, courierList, droneList);

        /* call generate greedy solution */
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
        solver.LNS1r(200,2);
        System.out.println("-------------------------- v v v v v v v v v ---------------------------");
        solver.printSolution(); 
        System.out.println("---------------------   Manually Solution 1      ---------------------");
        /* mannally built a solution */ 
        ArrayList<Node> MRoute = null;
        ArrayList<Node> MFlight = null;
        Integer[] routeArray = {0,1,4,6,8,9,2,7,5};
        Integer[] flightArray = {1,-1,-1,1,1,-1,-1,3,3,3,4,1};
        // Drone drone = droneList[0];
        MRoute = Functions.buildNodeSeqFromArray(routeArray, startnode, nodes.NodeList);
        MFlight = Functions.buildNodeSeqFromArray(flightArray, null, nodes.NodeList);
        List<Node>[] Mflights = new List[1]; //for now only 1 drone.
        Mflights[0] = MFlight;
        HashMap<Node, MeetPoint> meetPoints = new HashMap<Node, MeetPoint>();
        MeetPoint mp = new MeetPoint(courierList[0], droneList[0], nodes.NodeList[4]);
        meetPoints.put(nodes.NodeList[4], mp);

        ArrayList<Node>[] MRoutes = new ArrayList[solver.couriers.length];
        for (int i = 0; i < MRoutes.length; i++) {
            MRoutes[i] = new ArrayList<Node>();
        }
        MRoutes[0] = MRoute;
        Solution MSolution = new Solution(MRoutes, Mflights, meetPoints); 
        /* recover the solution and instantiate it */
        solver.globalOptSolution = MSolution;
        solver.printSolution();
    }    
    private static void main2() throws IOException{
        Double[][] dataMatrix;
        Double[][] truckDistanceMatrix;
        Double[][] droneDistanceMatrix;
        Nodes nodes;
        Orders orders;
        /* readData from csv */
        dataMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/exNODES.csv");
        truckDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/Tt.csv");
            //TODO 暂时使用truck矩阵 
        droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/Tt.csv"); 
        
        /* initialize Nodes */
        nodes = new Nodes(dataMatrix);
        Node startnode = new StartEndNode(12, 1, 1, 's'); 
        //在这个测试例子中courier的startnode是第13个，对应distanceMatrix中第12
    
        /* initialize Orders(by orderNodeList) */
        orders = new Orders(nodes.orderNodeList);  
        
        /* initialize vehicle */
        //Courier courier = new Courier(0, startnode, truckDistanceMatrix);
        Courier[] courierList = new Courier[]{new Courier(0, startnode, truckDistanceMatrix)};
        Drone[] droneList = new Drone[]{new Drone(0, startnode, droneDistanceMatrix, 15)};


        /* initialize solution */
        ObjF_latePunish objF = new ObjF_latePunish(1);
        TrivalSolver solver = new TrivalSolver(orders, nodes, objF, courierList); 
        //ResupplySolver solver = new ResupplySolver(orders, nodes, objF, courier, droneList, truckDistanceMatrix);


        /* call generate greedy solution */
        solver.genGreedySolve();
        System.out.println("---------------------   GreedySolution finished     ---------------------");
        solver.printSolution();    
        System.out.println();
        /* call LNS1 to improve the solution */
        // solver.LNS2t(500,3); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        // System.out.println("---------------------   LNS2_truck (500) Solution  ---------------------");
        // solver.printSolution(); 
        // System.out.println();
        System.out.println("---------------------   LNS1_truck (500) Solution  ---------------------");
        //solver.LNS1t(500,2); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        solver.LNS1t(500, 2);
        solver.printSolution(); 
        System.out.println();

        System.out.println("---------------------   Manual Solution       ---------------------");
        // ArrayList<Node> MILP_Route = new ArrayList<Node>();
        // Integer[] routeArray = {0,1,4,6,9,2,7,5,3,8};
        // MILP_Route = Functions.buildFromArray(routeArray, startnode, nodes.NodeList);
        // Functions.printRouteSeq(MILP_Route);
        // solver.instantiateSolver(MILP_Route);
        // System.out.println("ObjF: " + solver.ObjfValue());

        ArrayList<Node> MRoute = null;
        Integer[] routeArray = {0,1,4,6,9,2,7,5,3,8};
        MRoute = Functions.buildNodeSeqFromArray(routeArray, startnode, nodes.NodeList);
        ArrayList<Node>[] MRoutes = new ArrayList[solver.couriers.length];
        for (int i = 0; i < MRoutes.length; i++) {
            MRoutes[i] = new ArrayList<Node>();
        }
        MRoutes[0] = MRoute;
        Solution MSolution = new Solution(MRoutes); 
        /* recover the solution and instantiate it */
        solver.globalOptSolution = MSolution;
        solver.printSolution();
    }    


}
