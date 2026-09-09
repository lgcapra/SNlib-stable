package wnbag;

import bagexpr.BagExpr;
import bagexpr.BagIntersection;
import java.util.Collection;
import java.util.List;

/**
 * Concrete intersection operator for arc functions (bags of WNtuple).
 */
public final class ArcFunIntersection extends BagIntersection<WNtuple> implements ArcFunction {

    private ArcFunIntersection(Collection <? extends ArcFunction> c, boolean check) {
        super(c, check);
    }

    public static ArcFunction factory(Collection<? extends ArcFunction> c) {
        if (c.size() == 1)
            return c.iterator().next();
        
        return new ArcFunIntersection(c, true);
    }

    public static ArcFunction factory(ArcFunction ... l) {
        return factory(List.of(l));
    }

     
    @SuppressWarnings("unchecked")
    @Override
    public ArcFunction buildOp(Collection<? extends BagExpr<WNtuple>> args) {
        return factory((Collection<? extends ArcFunction>) args);
    }

    @Override
    public BagExpr<WNtuple> specSimplify() {
        return super.specSimplify();
    }

}
