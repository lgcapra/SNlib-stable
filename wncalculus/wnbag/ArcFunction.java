package wnbag;


import java.util.Collection;
import java.util.Map;

import bagexpr.Bag;
import bagexpr.BagExpr;
import expr.Domain;
import tuple.FunctionTuple;

/**
 * the super type of SN arc functions, i.e., bags of SN function-tuples
 * more precisely, an ArcFunction is a bag expression where the base consituting elements are WNtuples
 * @author lorenzo
 */
public interface ArcFunction extends BagExpr<WNtuple> {
    
    /*
    return the set-function corresponding to this arc-function, if it exists; otherwise, return null
    should be overridden by all arc-functions that can be interpreted as set-functions
     */
    default FunctionTuple asSetFunction() {
        return null;
    }   

    @Override
    default Class<WNtuple> bagType() {
        return WNtuple.class;
    }

    @Override
    public default Class<ArcFunction> type() {
        return ArcFunction.class;
    }

    @Override
    default Bag<WNtuple> buildEmpty(Domain dom, Domain codom) {
        return new TupleBag(dom, codom);
    }

    @Override
    default Bag<WNtuple> build(Map<? extends WNtuple, Integer> m) {
        return new TupleBag(m);
    }

    @SuppressWarnings("unchecked")
    @Override
     default ArcFunSum buildBagSum(Collection<? extends BagExpr<WNtuple>> c) {
        return new ArcFunSum((Collection<? extends ArcFunction>) c);
     }

     @Override
     default ArcFunScalar buildScProd(BagExpr<WNtuple> arg, int coeff) {
        return new ArcFunScalar((ArcFunction) arg, coeff);
     }

    @Override
    default ArcFunIntersection buildIntersection(BagExpr<WNtuple> left, BagExpr<WNtuple> right) {
        return new ArcFunIntersection((ArcFunction) left, (ArcFunction) right);
    }

     @Override
    public default TupleBag build() {
            return new TupleBag(getDomain(), getCodomain());
    }

    @Override
     public default ArcFunction buildTransp() {
        return new ArcFunTransp(this);
     }



}
