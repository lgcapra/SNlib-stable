package wnbag;

import bagexpr.BagExpr;
import bagexpr.ScalarProd;
import util.Util;

public final class ArcFunScalar extends ScalarProd<WNtuple> implements ArcFunction {

    public ArcFunScalar(ArcFunction f, int coeff) {
        super(f, coeff);
     }


    @Override
    public ArcFunScalar buildOp(BagExpr<WNtuple> arg) {  
        return new ArcFunScalar(arg.cast(), this.k);
    }  

    public ArcFunction specSimplify() {
        ArcFunction x = super.specSimplify().cast();
        if (x instanceof ArcFunScalar sprod) {
            if (sprod.getArg() instanceof WNtuple res) {
                return new TupleBag(Util.singleMap(res, sprod.k()));
            }
        }
        return x;
    }
}
