public class Node {
    double[] coord = new double[2];
    String  nodeClass;
    boolean isSply; //isSuppliable?
    boolean isCstm; //isCustomer? 
    boolean isRstr; //isRestaurant
    boolean isDrbs; //isDroneBase?

    public Node(double x, double y, int n){
        coord[0] = (double)x;
        coord[1] = (double)y;
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


}

 
class droneBaseNode extends Node{
    public droneBaseNode(double x, double y, int n) {
        super(x, y, n);
    }
}
