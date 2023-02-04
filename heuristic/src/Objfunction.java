
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

class Evaluations {
    static public double get_total_delays(_Solver_ s) {
        double delay = 0;
        for (Order o : s.orders.OrderList) {
            delay += Math.max(o.T_delivered-o.T_expected, 0);
        }
        return delay;
    }
    
    static public double get_delay_rate(_Solver_ s) {
        double total = s.orders.OrderList.length;
        double delay_num = 0;
        for (Order o : s.orders.OrderList) {
            if (o.T_delivered > o.T_expected)
                delay_num += 1;
        }
        return delay_num/total;
    }
}


