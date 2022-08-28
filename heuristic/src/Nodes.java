/* Mainly for storing nodes data */

class Nodes{
    /**
    Design Decision: Nodes does not include the start and end nodes.
    Design Decision: Still use the Virtual Node idea to enable multiple visit to a dronebase node
    **/
    
    int numOfNodes = 0; //to count normal Node (emit the StartNode and EndNodes)
    int numOfOrderNode = 0; //to count order Node only
    Node[] NodeList;    
    Node[] orderNodeList;
    //Node[] truckStartNodes;
    //Node[] droneStartNodes;


    public Nodes(Double[][] nodeData){

        /*count Numbers... 数组长度只能静态声明很烦,只能暂时记个数 */
        for (int i = 0; i < nodeData.length; i++) {
            Double[] temp = nodeData[i];
            /*count the number of normal Nodes */
            if ( temp[3] < 0){    //emit the StartNode and EndNodes
                continue;  
            }
            numOfNodes ++;
            /*count the number of order Nodes */
            if ( temp[2] < 300 && temp[2] >= 100){
                numOfOrderNode ++;
            }
        }
    //System.out.println(numOfOrderNode);

        /*initialize NodeList[] */
        NodeList = new Node[numOfNodes];
        numOfNodes = 0;

        /*initialize orderNodeList[] */
        orderNodeList = new Node[numOfOrderNode];
        numOfOrderNode = 0;

        /*fill the NodeList */
        for (int i = 0; i < nodeData.length; i++) {
            Double[] temp = nodeData[i];
            /*count the number of normal Nodes */
            if ( temp[3] < 0){    //emit the StartNode and EndNodes
                continue;  
            }
            NodeList[numOfNodes] = new Node(numOfNodes, temp[0], temp[1], temp[2].intValue(), temp[3].intValue(), temp[4]);

            /* check */
            //System.out.println("temp - x,y: " + temp[0] + ", " + temp[1]);
            //System.out.println("node - x,y: " + NodeList[numOfNodes].coord[0] + ", " + NodeList[numOfNodes].coord[1]);


            /*count the number of order Nodes */
            if ( temp[2] < 300 && temp[2] >= 100){    //only temp[2] between 100 - 299 are data belongs to orders. 
                //System.out.println(numOfOrderNode);
                orderNodeList[numOfOrderNode] = NodeList[numOfNodes];
                //System.out.println("order - x,y: " + orderNodeList[numOfNodes].orderT + ", " + orderNodeList[numOfNodes].orderNum);
                numOfOrderNode++;
            }
            numOfNodes++;
        }
    }
}



class Node {
    int id;
    double[] coord = new double[2];
    int orderNum;
    double  orderT;   //storge the time information, 
                    //not useful in nodes, just use to initialize 'Orders'.
    String  nodeClass;
    boolean isSply; //isSuppliable?
    boolean isCstm; //isCustomer? 
    boolean isRstr; //isRestaurant
    boolean isDrbs; //isDroneBase?

    boolean isMeet; //isDrone*Courier meet here
    double  courierWaitTime = -1;
    double  T_courier = -1; //arrive time of courier
    double  T_drone = -1;   //arrive time of drone

    public Node(){}

    public Node(int id, double x, double y, int n, int o, double orderT /*, int idn*/){
        this.id = id;
        coord[0] = (double)x;
        coord[1] = (double)y;
        orderNum = o;
        this.orderT = orderT;
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
        return this.id;
    }

    void reset(){
        T_courier = -1;
        T_drone = -1;
    }
}

 
class StartEndNode extends Node{
    char type;
    public StartEndNode(int id, double x, double y, char c){
        this.id = id;
        coord[0] = (double)x;
        coord[1] = (double)y;
        if(c != 's' && c != 'e'){
            System.out.println("WARNING!!!! \t Undefined StartEndNode Type");
        }
        type = c; 
    }
}