package wnbag;

import bagexpr.BagExpr;
import bagexpr.ScalarProd;

public final class ArcFunScalar extends ScalarProd<WNtuple> implements ArcFunction {

    public static ArcFunction factory(ArcFunction f, int coeff) {
        if (coeff == 0)
            return f.build();
        
        if (coeff == 1)
            return f;

        return new ArcFunScalar(f, coeff);
    }

    private ArcFunScalar(ArcFunction f, int coeff) {
        super(f, coeff);
     }


    @Override
    public ArcFunScalar buildOp(BagExpr<WNtuple> arg) {  
        return new ArcFunScalar(arg.cast(), this.k);
    }  

}
