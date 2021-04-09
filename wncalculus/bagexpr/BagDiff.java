package wncalculus.bagexpr;

import wncalculus.expr.ParametricExpr;

/**
 * @author lorenzo capra
 * this class template just maps the difference to a sum
 */
public class BagDiff {
    
    /**
     * build the sum b1 + (-1)*b2
     * @param <E> the bag's type
     * @param b1 the minuend
     * @param b2 the subtraend
     * @return sum b1 + (-1)*b2
     */
    public static <E extends ParametricExpr> BagExpr<E> build(BagExpr<E> b1, BagExpr<E> b2) {
        return BagSum.factory(b1, ScalarProd.factory(b2, -1));
    }
    
}
