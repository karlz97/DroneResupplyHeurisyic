import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.NClob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Main {
    public static void main(String[] args) throws IOException {
        groups_run_major();
    } 

    private static void groups_run_sensitive() throws IOException {
        long stime = System.currentTimeMillis();
        String group_path = "../eleme_b5_s20/";
        String output_path = "../eleme_output/";
        File file = new File(group_path);
        File[] subdirs = file.listFiles();
        for (File f : subdirs) {
            String f_name = f.getName();
            String path = group_path + f_name + '/';
            batch_sensitive(path, output_path);
        } 
        long etime = System.currentTimeMillis();
        System.out.printf("the program takes: %d s.", (etime - stime)/1000);
    }

    private static void groups_run_duration() throws IOException {
        long stime = System.currentTimeMillis();
        String group_path = "../eleme_b5_s15_total/";
        String output_path = "../eleme_output/";
        File file = new File(group_path);
        File[] subdirs = file.listFiles();
        for (File f : subdirs) {
            String f_name = f.getName();
            String path = group_path + f_name + '/';
            batch_duration(path, output_path);
        } 
        long etime = System.currentTimeMillis();
        System.out.printf("the program takes: %d s.", (etime - stime)/1000);
    }

    private static void groups_run_major() throws IOException {
        long stime = System.currentTimeMillis();
        String group_path = "../eleme_b5_s15_total/";
        String output_path = "../eleme_output/";
        File file = new File(group_path);
        File[] subdirs = file.listFiles();
        for (File f : subdirs) {
            String f_name = f.getName();    
            String path = group_path + f_name + '/';
            // batch_major(path, output_path);
            batch_major2(path, output_path);
        } 
        long etime = System.currentTimeMillis();
        System.out.printf("the program takes: %d s.", (etime - stime)/1000);
    }


    public static void run_major(String[] args) throws IOException {
        String path = "../eleme_b5_s15/0301_rg_1_sec_0/";
        String output_path = "../eleme_output/";
        long stime = System.currentTimeMillis();
        batch_major(path, output_path);
        // batch_main1(path);
        //main3_multi(path);
        // main2_multi(path);
        long etime = System.currentTimeMillis();

        // 计算执行时间
        System.out.printf("the program takes: %d s.", (etime - stime)/1000);
    }

    private static void batch_duration(String input_path, String output_path) throws IOException {
        Instance ins = new Instance(input_path);
        String[] components = input_path.split("/");
        String group_name = components[components.length - 2]; 
        String output_file = output_path + group_name + "_duration_output.csv";
        double punish_factor = 120;
        int Nc = 4, Nd = 1;
        double[] durations = new double[] {10,15,20,25,30};
        try {
            FileWriter writer = new FileWriter(output_file, true);
            // writer.write("\r\n");
            /* 3-5c + 1d */
            for (double duration: durations) {
                Setting set_i = new Setting(ins, Nc, Nd, duration, punish_factor);
                double[] evaluation = Batch.run_resupply(ins, set_i);
                writer.append(ins.name+","+Nc+","+Nd+","+duration+",");
                for (int i = 0; i < evaluation.length; i++) {
                    writer.append(Double.toString(evaluation[i])+",");
                }
                writer.append("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void batch_major2(String input_path, String output_path) throws IOException {
        Instance ins = new Instance(input_path);
        String[] components = input_path.split("/");
        String group_name = components[components.length - 2]; 
        String output_file = output_path + group_name + "_output.csv";
        double drone_max_endurance = 20, punish_factor = 600;
        int[] NumOfCouriers = new int[] {3,4,5};
        int[] NumOfDrones= new int[] {1,2};
        try {
            FileWriter writer = new FileWriter(output_file, true);
            for (int Nc: NumOfCouriers) {
                for (int Nd: NumOfDrones) {
                    Setting set_i = new Setting(ins, Nc, Nd, drone_max_endurance, punish_factor);
                    double[] evaluation = Batch.run_resupply(ins, set_i);
                    writer.append(ins.name+","+Nc+","+Nd+","+drone_max_endurance+",");
                    for (int i = 0; i < evaluation.length; i++) {
                        writer.append(Double.toString(evaluation[i])+",");
                    }
                    writer.append("\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void batch_major(String input_path, String output_path) throws IOException {
        Instance ins = new Instance(input_path);
        String[] components = input_path.split("/");
        String group_name = components[components.length - 2]; 
        String output_file = output_path + group_name + "_output.csv";
        double drone_max_endurance = 20, punish_factor = 120;
        int[] NumOfCouriers = new int[] {3,4,5};
        try {
            FileWriter writer = new FileWriter(output_file, true);
            for (int Nc: NumOfCouriers) {
                int Nd = 1;
                Setting set_i = new Setting(ins, Nc, Nd, drone_max_endurance, punish_factor);
                double[] evaluation = Batch.run_resupply(ins, set_i);
                writer.append(ins.name+","+Nc+","+Nd+","+drone_max_endurance+",");
                for (int i = 0; i < evaluation.length; i++) {
                    writer.append(Double.toString(evaluation[i])+",");
                }
                writer.append("\n");
            }
            /* 3c + 2d */ 
            int Nc = 3, Nd = 2;
            Setting set_i = new Setting(ins, Nc, Nd, drone_max_endurance, punish_factor);
            double[] evaluation = Batch.run_resupply(ins, set_i);
            writer.append(ins.name+","+Nc+","+Nd+","+drone_max_endurance+",");
            for (int i = 0; i < evaluation.length; i++) {
                writer.append(Double.toString(evaluation[i])+",");
            }
            writer.append("\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void batch_major_append(String input_path, String output_path) throws IOException {
        Instance ins = new Instance(input_path);
        String[] components = input_path.split("/");
        String group_name = components[components.length - 2]; 
        String output_file = output_path + group_name + "_output.csv";
        double drone_max_endurance = 20, punish_factor = 120;
        int[] NumOfCouriers = new int[] {3,4,5};
        try {
            FileWriter writer = new FileWriter(output_file, true);
            int Nc = 4, Nd = 2;
            Setting set_i = new Setting(ins, Nc, Nd, drone_max_endurance, punish_factor);
            double[] evaluation = Batch.run_resupply(ins, set_i);
            writer.append(ins.name+","+Nc+","+Nd+","+drone_max_endurance+",");
            for (int i = 0; i < evaluation.length; i++) {
                writer.append(Double.toString(evaluation[i])+",");
            }
            writer.append("\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void batch_sensitive(String input_path, String output_path) throws IOException {
        Instance ins = new Instance(input_path);
        String[] components = input_path.split("/");
        String group_name = components[components.length - 2]; 
        String output_file = output_path + group_name + "_output.csv";
        double drone_max_endurance = 20, punish_factor = 120;
        try {
            FileWriter writer = new FileWriter(output_file, true);
            int Nc = 4, Nd = 1;
            Setting set_i = new Setting(ins, Nc, Nd, drone_max_endurance, punish_factor);
            double[] evaluation = Batch.run_resupply(ins, set_i);
            writer.append(ins.name+","+Nc+","+Nd+","+drone_max_endurance+",");
            for (int i = 0; i < evaluation.length; i++) {
                writer.append(Double.toString(evaluation[i])+",");
            }
            writer.append("\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
