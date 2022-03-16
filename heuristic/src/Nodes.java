/* Mainly for storing nodes data */

public class Nodes{
    /**
    Design Decision: Nodes does not include the start and end nodes.
    
    **/
    
    int numOfNodes = 0; //to count normal Node (emit the StartNode and EndNodes)
    int numOfOrderNode = 0; //to count order Node only
    Node[] NodeList;    
    Node[] orderNodeList;
    //Node[] truckStartNodes;
    //Node[] droneStartNodes;


    public Nodes(Double[][] nodeData){

        //count Numbers... 数组长度只能静态声明很烦,只能暂时记个数
        for (int i = 0; i < nodeData.length; i++) {
            Double[] temp = nodeData[i];
            //count the number of normal Nodes
            if ( temp[3] < 0){    //emit the StartNode and EndNodes
                continue;  
            }
            numOfNodes ++;
            //count the number of order Nodes
            if ( temp[3] < 300 && temp[3] >= 100){
                numOfOrderNode ++;
            }
        }
        
        //initialize NodeList[]
        NodeList = new Node[numOfNodes];
        numOfNodes = 0;

        //initialize orderNodeList[]
        orderNodeList = new Node[numOfOrderNode];
        numOfOrderNode = 0;

        //fill the NodeList
        for (int i = 0; i < nodeData.length; i++) {
            Double[] temp = nodeData[i];
            //count the number of normal Nodes
            if ( temp[3] < 0){    //emit the StartNode and EndNodes
                continue;  
            }
            numOfNodes++;
            NodeList[numOfNodes] = new Node(numOfNodes, temp[1], temp[2], temp[3].intValue(), temp[4].intValue(), temp[5]);
            //count the number of order Nodes
            if ( temp[3] < 300 && temp[3] >= 100){
                numOfOrderNode++;
                NodeList[numOfOrderNode] = NodeList[numOfNodes];
            }
        }
    }






    

}



class Node {
    int nodeID;
    double[] coord = new double[2];
    int orderNum;
    double  T;   //storge the time information, 
                    //not useful in nodes, just use to initialize 'Orders'.
    String  nodeClass;
    boolean isSply; //isSuppliable?
    boolean isCstm; //isCustomer? 
    boolean isRstr; //isRestaurant
    boolean isDrbs; //isDroneBase?

    public Node(int id, double x, double y, int n, int o, double T /*, int idn*/){
        nodeID = id;
        coord[0] = (double)x;
        coord[1] = (double)y;
        orderNum = o;
        isSply = true;
        isDrbs = false;
        isCstm = false;
        isRstr = false;
        switch(n){
            case 201:
                nodeClass = "customer_normal";
                isCstm = true;
                break;
            case 202:
                nodeClass = "customer_unspliable";
                isCstm = true;
                isSply = false;
                break;
            case 101:
                nodeClass = "restaurant_normal";
                isRstr = true;
                break;
            case 102:
                nodeClass = "restaurant_unspliable";
                isRstr = true;      
                isSply = false;
                break;
            case 103:
                nodeClass = "restaurant_lauchnode";
                isRstr = true;
                isDrbs = true;
                break;
        }

    }

    public int getID(){
        return this.nodeID;
    }
}

 
