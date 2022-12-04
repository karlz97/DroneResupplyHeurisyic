import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException {
        main2_multi();
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
        droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/Td.csv"); //TODO 暂时使用truck矩阵 
        
        /* initialize Nodes */
        nodes = new Nodes(dataMatrix);
        Node startnode = new StartEndNode(12, 1, 1, 's')    ; //在这个测试例子中courier的startnode是第13个，对应distanceMatrix中第12
    
        /* initialize Orders(by orderNodeList) */
        orders = new Orders(nodes.orderNodeList);  
        
        /* initialize vehicle */
        Courier[] courierList = new Courier[]{new Courier(0, startnode, truckDistanceMatrix)};
        Drone[] droneList = new Drone[]{new Drone(0, nodes.NodeList[1], droneDistanceMatrix)};
        droneList[0].computeFeasibleFlight(nodes);
        droneList[0].showFeasibleFlight();


        /* initialize solution */
        ObjF_latePunish objF = new ObjF_latePunish(1);
        //TrivalSolver solver = new TrivalSolver(orders, nodes, objF, courier, truckDistanceMatrix); 
        ResupplySolver solver = new ResupplySolver(orders, nodes, objF, courierList, droneList);

        /* call generate greedy solution */
        solver.genGreedySolve();
        System.out.println("---------------------   GreedySolution finished     ---------------------");
        solver.printSolution();    
        System.out.println();
        /* call LNS1 to improve the solution */
        System.out.println("---------------------   LNS1_truck (500) Solution  ---------------------");
        solver.LNS1t(500,2); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        solver.printSolution(); 
        System.out.println();
        
        //solver.LNS1r_test(3);
        System.out.println("---------------------   LNS1_drone (5000) Solution  ---------------------");
        solver.LNS1r(500,2);
        solver.printSolution(); 
        // System.out.println("Routes: ");
        // for (Iterator<Node> it = solver.globalOptSolution.courierRoute.iterator(); it.hasNext();) {
        //     System.out.print( it.next().id + ",");
        //     // debug::: System.out.print( it.next().id + "(" +  + ")" + " --> ");
        // }

        System.out.println();

        System.out.println("---------------------   Manually Solution 1      ---------------------");
        /* mannally built a solution */ 
        ArrayList<Node> MRoute = null;
        ArrayList<Node> MFlight = null;
        Integer[] routeArray = {0,1,4,6,8,9,2,7,5};
        Integer[] flightArray = {1,-1,-1,3,3,3,4,1};
        Drone drone = droneList[0];
        MRoute = Functions.buildNodeSeqFromArray(routeArray, startnode, nodes.NodeList);
        MFlight = Functions.buildNodeSeqFromArray(flightArray, null, nodes.NodeList);
        List<Node>[] Mflights = new List[1]; //for now only 1 drone.
        Mflights[0] = MFlight;
        HashMap<Node, MeetPoint> meetPoints = new HashMap<Node, MeetPoint>();

        ArrayList<Node>[] MRoutes = new ArrayList[solver.couriers.length];
        for (int i = 0; i < MRoutes.length; i++) {
            MRoutes[i] = new ArrayList<Node>();
        }
        MRoutes[0] = MRoute;
        Solution MSolution = new Solution(MRoutes, Mflights, meetPoints); 
        /* recover the solution and instantiate it */
        solver.globalOptSolution = MSolution;
        solver.printSolution();


        // System.out.println("---------------------   Manually Solution 2     ---------------------");
        // /* mannally built a solution */ 
        // MRoute = null;
        // MFlight = null;
        // routeArray = new Integer[]{0,1,2,6,5,3,7,8,9};
        // flightArray = new Integer[]{4,4,8,4};
        // drone = droneList[0];
        // MRoute = Functions.buildNodeSeqFromArray(routeArray, startnode, nodes.NodeList);
        // MFlight = Functions.buildNodeSeqFromArray(flightArray, null, nodes.NodeList);
        // Mflights = new List[1];
        // Mflights[0] = MFlight;
        // MSolution = new Solution(MRoute, Mflights); 

        // /* recover the solution and instantiate it */
        // solver.globalOptSolution = MSolution;
        // solver.printSolution();
    }    

    private static void main2_multi() throws IOException{
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
        Courier[] courierList = new Courier[]{new Courier(0, startnode, truckDistanceMatrix), new Courier(1, startnode, truckDistanceMatrix)};
        Drone[] droneList = new Drone[]{new Drone(0, startnode, droneDistanceMatrix)};


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
        Drone[] droneList = new Drone[]{new Drone(0, startnode, droneDistanceMatrix)};


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
