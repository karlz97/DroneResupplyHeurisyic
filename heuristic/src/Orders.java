
public class Orders {
    int numOfOrders;
    Order[] OrderList;
    
    public Orders(Node[] orderNodeList /*, int numOfOrders*/){
        /**  initialize the data from the pythonGen style of csv. file
        NOTE: Assume the `nodeData` only contains the order-related node and nodes are standalone. 
        nodeList refer to Class Nodes -> NodeList
        **/ 
        //check the data correct
        if ( orderNodeList.length%2 != 0 ){
            System.out.println("WARNING!!!! \t nodeData Problem IN <Orders Initialize>");
        }
        numOfOrders = orderNodeList.length/2;
        OrderList = new Order[numOfOrders];
        for (int i = 0; i < orderNodeList.length; i++ ){
            int j = orderNodeList[i].orderNum;   //降低了运行效率但是增加了可读性...
            OrderList[j]  = new Order(j);
            if(orderNodeList[i].isCstm == true){
                if(orderNodeList[i].isRstr == true){
                    System.out.println("WARNING!!!! \t nodeData Problem IN <Order Class repeat>");
                }else{
                    OrderList[j].cstmNode = orderNodeList[i].getID(); 
                    OrderList[j].T_expected = orderNodeList[i].T;
                }
                
            }
            if(orderNodeList[i].isRstr == true){
                OrderList[j].rstrNode = orderNodeList[i].getID(); 
                OrderList[j].T_prepared = orderNodeList[i].T;
            }
        }
    }
}

class Order {
    int orderID;
    int cstmNode;
    int rstrNode;
    double T_released;
    double T_prepared;
    double T_expected; 
    boolean isPicked;
    boolean isDelivered;

    public Order(int orderID){
        this.orderID = orderID;
    }

    public Order(int orderID, int cstmNode, int rstrNode, double T_prepared, double T_expected){
        this.orderID = orderID;
        this.cstmNode = cstmNode;
        this.rstrNode = rstrNode;
        this.T_prepared = T_prepared;
        this.T_expected = T_expected;
    }

    public Order(int cstmNode, int rstrNode, double T_prepared, double T_expected){
        this.cstmNode = cstmNode;
        this.rstrNode = rstrNode;
        this.T_prepared = T_prepared;
        this.T_expected = T_expected;
    }



}