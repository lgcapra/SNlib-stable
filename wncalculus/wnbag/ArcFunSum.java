package wnbag;

import java.util.Collection;
import java.util.List;
import bagexpr.BagExpr;
import bagexpr.BagSum;

public final class ArcFunSum extends BagSum<WNtuple> implements ArcFunction {
    
    private ArcFunSum(Collection<? extends ArcFunction> c) {
        super(c, true);
    }
    
    public static ArcFunction factory(Collection<? extends ArcFunction> c) {
        if (c.size() == 1)
            return c.iterator().next();
        
        return new ArcFunSum(c);
    }

    public static ArcFunction factory(ArcFunction ... l) {
        return factory(List.of(l));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ArcFunction buildOp(Collection<? extends BagExpr<WNtuple>> args) {
        return factory((Collection<? extends ArcFunction>) args);
    }

}
