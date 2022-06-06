package wncalculus.logexpr;

import wncalculus.bagexpr.BagComp;
import wncalculus.bagexpr.LogicalBag;
import wncalculus.bagexpr.SetComp;

/**
 * This interface represents expressions (function) representing parametric sets
 * @author lorenzo capra
 */
public interface SetExpr extends LogicalExpr {
    
     /**
     * @param e a set-expression
     * @return the bag-composition operator between <code>this</code> and another expression,
     * seen as bags; <code>null</code> if, for any reasons, the composition cannot be done
     * @throws ClassCastException if the type of e is not compatible with <code>this</code> type
     */
    <E extends SetExpr> SetComp<E> buildBagComp(SetExpr e);
    
    <E extends SetExpr> LogComposition<E> buildSetComp(SetExpr r);
     
    /** 
     * provides a bag-view of this expression
     * @param <E> the expression's type
     * @return a singleton bag corresponding to <tt>this</tt> expression
     * @throws ClassCastException if the type of e is not compatible with <code>this</code> type
     */
    abstract <E extends SetExpr> LogicalBag<E> asBag();
    
    /** 
     * provides the null bag corresponding to this kind of expression
     * @param <E> the expression's type
     * @return a null bag corresponding to <tt>this</tt> expression
     * @throws ClassCastException if the type of e is not compatible with <code>this</code> type
     */
    abstract <E extends SetExpr> LogicalBag<E> nullBag();
    
    /**
     * @return the lower-bound of <code>this</code> tuple's cardinality; <code>null</code> if, for any reason, the
     * cardinality cannot be computed 
    */
    default Integer cardLb() {
        return null;
    }
    
    
}
