package wnbag;

import bagexpr.BagExpr;
import bagexpr.ScalarProd;

public final class ArcFunScalar extends ScalarProd<WNtuple> implements ArcFunction {

    public ArcFunScalar(ArcFunction f, int coeff) {
        super(f, coeff);
     }


    @Override
    public ArcFunScalar buildOp(BagExpr<WNtuple> arg) { // check if used somewhere else 
        throw new UnsupportedOperationException("Unimplemented method 'buildOp'");
    }  

}
