package wnbag;

import bagexpr.BagExpr;
import bagexpr.BagIntersection;

/**
 * Concrete intersection operator for arc functions (bags of WNtuple).
 */
public final class ArcFunIntersection extends BagIntersection<WNtuple> implements ArcFunction {

    public ArcFunIntersection(ArcFunction left, ArcFunction right) {
        super(left, right);
    }

    @Override
    public ArcFunIntersection buildOp(BagExpr<WNtuple> left, BagExpr<WNtuple> right) {
        return new ArcFunIntersection((ArcFunction) left, (ArcFunction) right);
    }
}
