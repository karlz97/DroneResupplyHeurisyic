import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.NodeList;
import org.w3c.dom.css.Counter;



public class Main {
    public static void main(String[] args) throws IOException {
        main2();
    }



    private static void main2() throws IOException{
        Double[][] dataMatrix;
        Double[][] truckDistanceMatrix;
        Nodes nodes;
        Orders orders;
        /* readData from csv */
        dataMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/exNODES.csv");
        truckDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/Tt.csv");
        
        /* initialize Nodes */
        nodes = new Nodes(dataMatrix);
        Node startnode = new StartEndNode(12, 1, 1, 's'); //在这个测试例子中courier的startnode是第13个，对应distanceMatrix中第12
    
        /* initialize Orders(by orderNodeList) */
        orders = new Orders(nodes.orderNodeList);  
        
        /* initialize vehicle */
        Courier courier = new Courier(0, 10, startnode /*nodes.NodeList[2]*/);

        /* initialize solution */
        ObjF_latePunish objF = new ObjF_latePunish(1);
        TrivalSolution solution = new TrivalSolution(orders, nodes, objF, courier, truckDistanceMatrix); 

        /* call generate greedy solution */
        solution.genGreedySolution();
        System.out.println("---------------------   GreedySolution      ---------------------");
        solution.printSolution();    

        /* call LNS1 to improve the solution */
        solution.LNS1t(100); //finish in a acceptable time(less than 5 min) at 10,000,000 (千万次), 
        System.out.println("---------------------   LNS1(100) Solution  ---------------------");
        solution.printSolution();  

        // System.out.println("---------------------   MILP Solution       ---------------------");
        // ArrayList<Node> MILP_Route = new ArrayList<Node>();
        // Integer[] routeArray = {0,1,4,6,9,2,7,5,3,8};
        // MILP_Route = Functions.buildFromArray(routeArray, startnode, nodes.NodeList);
        // Functions.printRouteSeq(MILP_Route);
        // solution.instantiateSolution(MILP_Route);
        // System.out.println("ObjF: " + solution.ObjfValue());
    }    


    private static void main1() throws IOException{
        Double[][] dataMatrix;
        Double[][] truckDistanceMatrix;
        Nodes nodes;
        Orders orders;
        /* readData from csv */
        dataMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/exNODES.csv");
        truckDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix("../insgen/Tt.csv");
        Functions.printMatrix(dataMatrix);
        Functions.printMatrix(truckDistanceMatrix);
        
        /* initialize Nodes */
        nodes = new Nodes(dataMatrix);
        Node startnode = new StartEndNode(12, 1, 1, 's'); //在这个测试例子中courier的startnode是第13个，对应distanceMatrix中第12
    
        /* initialize Orders(by orderNodeList) */
        //System.out.println("foo" + nodes.NodeList[0].coord[1]);
        orders = new Orders(nodes.orderNodeList);  
        //System.out.println(orders.OrderList[2].id);
        //System.out.println(orders.OrderList[2].cstmNode.id);
        //System.out.println(orders.OrderList[2].rstrNode.id);
        //System.out.println(orders.OrderList[2].T_prepared);
        //System.out.println(orders.OrderList[2].T_expected);
        
        /* initialize vehicle */
        Courier courier = new Courier(0, 10, startnode /*nodes.NodeList[2]*/);


        /* initialize solution */
        ObjF_latePunish objF = new ObjF_latePunish(1);
        TrivalSolution solution = new TrivalSolution(orders, nodes, objF, courier, truckDistanceMatrix); 

        /* call generate greedy solution */
        solution.genGreedySolution();
        solution.printSolution();

        /* test on heuristics   */
        ArrayList<Order> removedOrderList = solution.shawRemoval_fast(solution.courier.routeSeq, 4, 2);
        //ArrayList<Order> removedOrderList = solution.randomRemoval(solution.courier.routeSeq, 2);
        
        Functions.printRouteSeq(courier.routeSeq);
        Functions.printOrderList(removedOrderList);

        solution.courierGlobalRouteSeq = solution.regeretInsert(solution.courier.routeSeq, removedOrderList, 3);
        solution.instantiateSolution(); 
        solution.printSolution();
        //System.out.println("ObjF: " + solution.ObjfValue());

        /* print route of solution 
        solution.printSolution();
        System.out.println(solution.ObjfValue());*/

        /*for (Order order : solution.orders.OrderList) {
            System.out.println(order.T_delivered);
        }*/
    }


    private static void main0(){
        ArrayList<Double[]> dataInList = new ArrayList<Double[]>();
        try {
            ReadDataFromCSV csvReader = new ReadDataFromCSV("exNODES.csv");
            csvReader.readDoubleValues(dataInList);
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("watchout!");
        Double[][] testMatrix = ReadDataFromCSV.trun2Matirx(dataInList);
        //just to test:
        for (int i = 0; i < testMatrix.length; i++) {
            for (int j = 0; j <testMatrix[i].length; j++) {  //testValue.get(0) 第一行元素的长度
                System.out.print(testMatrix[i][j]+"\t");
            }
            System.out.println();
        }
    }



}
