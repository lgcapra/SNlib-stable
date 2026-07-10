package bagexpr;

import java.util.Collection;
import expr.ParametricExpr;

/**
 * Abstract N_ary intersection between bag expressions.
 * When both operands are concrete bags, the result is computed pointwise
 * by taking the minimum multiplicity for each common element.
 *
 * @param <E> bag element type
 */
public abstract class BagIntersection<E extends ParametricExpr> extends N_aryBagOp<E> {

    protected BagIntersection(Collection<? extends BagExpr<E>> c, boolean check) {
        super(c, check);
    }

    @Override
    public Bag<E> Op(Bag<E> b1, Bag<E> b2){
        return b1.intersection(b2);
    }

    @Override
    public final Integer cardLb() {
        return null;
    }

}