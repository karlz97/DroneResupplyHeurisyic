
abstract class Objfunction {
    abstract public double computeObjValue(_Solver_ s);
}

class ObjF_latePunish extends Objfunction {
    double alpha; //paramter

    ObjF_latePunish(double alpha){
        this.alpha = alpha; 
    }

    @Override
    public double computeObjValue(_Solver_ s) {
        double ObjValue = 0;
        for ( Order ord : s.orders.OrderList){
            ObjValue += objfByOne(ord);
        }
        return ObjValue;
    }

    private double objfByOne(Order ord){
        return ord.T_delivered + alpha * Math.max((ord.T_delivered - ord.T_expected) , 0);
    }

}


