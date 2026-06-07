package wnbag;

import bagexpr.BagComp;
import bagexpr.BagExpr;
import expr.ParametricExpr;

public final class ArcFunComp extends BagComp<WNtuple> implements ArcFunction {

     public ArcFunComp(ArcFunction l, ArcFunction r) {
         super(l, r);
     }

     @Override
     public ArcFunComp buildOp(BagExpr<WNtuple> left, BagExpr<WNtuple> right) {
            return new ArcFunComp((ArcFunction) left, (ArcFunction) right);
     }

}
