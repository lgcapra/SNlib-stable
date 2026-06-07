package wnbag;

import bagexpr.BagExpr;
import bagexpr.BagTranspose;

public final class ArcFunTransp extends BagTranspose<WNtuple> implements ArcFunction {

    public ArcFunTransp(ArcFunction f) {
        super(f);
    }

    @Override
    public BagExpr<WNtuple> buildOp(BagExpr<WNtuple> arg) {
        return new ArcFunTransp((ArcFunction) arg);
    }

    // to complete
    
}
