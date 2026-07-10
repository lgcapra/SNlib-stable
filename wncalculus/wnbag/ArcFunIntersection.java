package wnbag;

import bagexpr.BagExpr;
import bagexpr.BagIntersection;
import expr.Expression;

import java.util.Collection;

/**
 * Concrete intersection operator for arc functions (bags of WNtuple).
 */
public final class ArcFunIntersection extends BagIntersection<WNtuple> implements ArcFunction {

    public ArcFunIntersection(Collection <? extends ArcFunction> c, boolean check) {
        super(c, check);
    }

    @Override
    public ArcFunIntersection buildOp(Collection<? extends BagExpr<WNtuple>> args) {
        return new ArcFunIntersection((Collection<? extends ArcFunction>) args, true);
    }
}
