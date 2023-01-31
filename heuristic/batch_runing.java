public class instance {
    Nodes nodes;
    Double[][] truckDistanceMatrix;
    Double[][] droneDistanceMatrix; 
    
    public instance(String path) {
        Double[][] nodes_data = eadDataFromCSV.readDoubleToMatrix(path + "/nodes_data");
        truckDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "/Tt.csv");
        droneDistanceMatrix = ReadDataFromCSV.readDoubleToMatrix(path + "/insgen/Td.csv"); 
        
        nodes = new Nodes(nodes_data);
        Node 
    }

} 
public class setting