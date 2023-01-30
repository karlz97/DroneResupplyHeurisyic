
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
            if(OrderList[j] == null ){
                OrderList[j]  = new Order(j);   
            }
            if(orderNodeList[i].isCstm == true){
                if(orderNodeList[i].isRstr == true){
                    System.out.println("WARNING!!!! \t nodeData Problem IN <Order Class repeat>");
                }else{
                    OrderList[j].cstmNode = orderNodeList[i]; 
                    OrderList[j].T_expected = orderNodeList[i].orderT;
                }
            }
            if(orderNodeList[i].isRstr == true){
                //System.out.println("look " + orderNodeList[i].id);
                OrderList[j].rstrNode = orderNodeList[i]; 
                OrderList[j].T_prepared = orderNodeList[i].orderT;
            }
        }
    }

    boolean allDone(){
        for (int i = 0; i < OrderList.length; i++){
            if(!OrderList[i].isDelivered){
                return false;
            }
        }
        return true;
    }

    boolean checkConsitancy(){
        // TODO
        return true;
    }

}

class Order {
    final int id;
    Vehicle deliVehicle;
    Vehicle pickVehicle;
    Node cstmNode;
    Node rstrNode; //这些都是指针，这不比用下标方便直接多了; 非也，调用距离矩阵的时候麻烦了
    double T_released;
    double T_prepared;
    double T_expected; 
    double T_delivered = 10000;
    boolean isReleased;
    boolean isPicked;
    boolean isDelivered;
    
    public Order(int id){
        this.id = id;
        isReleased = true;
        isPicked = false;
        isDelivered = false;
    }

    public Order(int id, Node cstmNode, Node rstrNode, double T_prepared, double T_expected){
        this(id);
        this.cstmNode = cstmNode;
        this.rstrNode = rstrNode;
        this.T_released = 0;
        this.T_prepared = T_prepared;
        this.T_expected = T_expected;
    }

    void reset_r(){  //reset order to released status
        T_delivered = 10000; 
        isReleased = true;
        isPicked = false;
        isDelivered = false;
        //cstmNode.reset();
        //rstrNode.reset();
    }

    Node getNode(){
        // automatically get the pickup/deliever node
        // return the cstm node if not picked, return the rstr node if not picked
        if(isPicked == true){
            return cstmNode;
        }else{
            return rstrNode;
        }

    }

    double getTime(){
        /*  return the T_released if not released
            return the T_prepared if not prepared
            else return the T_expected
        */
        if(isReleased == false){
            return T_released;
        }else if(isPicked == false){
            return T_prepared;
        }else{
            return T_expected;
        }
    }

    boolean isFeasible(double arriveTime){
        if(isReleased == false){
            if (arriveTime >= T_released) {
                return true; 
            }else{
                return false;
            }
        }else if(isPicked == false){
            if (arriveTime >= T_prepared) {
                return true;
            }else{
                return false;
            }
        }else if(isDelivered == false ){
            return true;
        }else{
            return false;
        }
    }

    boolean update(Vehicle vehicle, double time) {
        // automatically update the status no matter it is a pickup or delivery
        if(isReleased == false){
            Functions.printAlert("WARNING!!! Update to a [unrealeased] order");
            return false;   //没有release无法update
        }else if(isPicked == false && isDelivered == false){
            pickVehicle = vehicle;
            isPicked = true;
            return true;
        }else if(isDelivered == false ){
            deliVehicle = vehicle;
            isDelivered = true;
            T_delivered = time;
            return true;
        }else{
            throw new java.lang.RuntimeException("Update to a [delivered] order. Order:" + this.id + "; Vehicle:" + vehicle.id);
            // Functions.printAlert("WARNING!!! Update to a [delivered] order. Order:" + this.id + "; Vehicle:" + vehicle.id);
            // return false;
        }
    }

    


   

}