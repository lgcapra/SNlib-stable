package wnbag;

import java.util.Collection;
import java.util.List;

import bagexpr.BagExpr;
import bagexpr.BagSum;

public final class ArcFunSum extends BagSum<WNtuple> implements ArcFunction {
    
    
    public ArcFunSum(Collection<? extends ArcFunction> l) {
        super(l, true);
     }

    public ArcFunSum(ArcFunction ... l) {
        this(List.of(l));
     }

    @Override
    public BagExpr<WNtuple> buildOp(Collection<? extends BagExpr<WNtuple>> args) {
        return ArcFunction.super.buildBagSum(args);
    }


}
