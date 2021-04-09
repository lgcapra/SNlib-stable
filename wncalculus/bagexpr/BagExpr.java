
package wncalculus.bagexpr;

import wncalculus.expr.*;

/**
 * the super-type of expressions on bags (of expressions) defined over a given domain;
 * notice that a BagExpr may not be a Bag
 * NOTE currently bags are not parametric expression, even if in perspective they could become
 * @author Lorenzo Capra
 * @param <E> the bag's domain
 */
public interface BagExpr<E extends ParametricExpr> extends ParametricExpr, BagBuilder<E> {
    
    /**
     * @return an empty bag of the same type as <code>this</code> with the same size
     */
    default Bag<E> build() {
        return build(getDomain(),getCodomain());
    }
    
    
    @Override
    default Class<? extends BagExpr> type() {
        return BagExpr.class;
    }
    
    /**
     * @return the bag's elements type 
     */
    Class<E> bagType();
        
    /**
     * invokes the normalization algorithn just performing a cast
     * @return a normalized bag equivalent to <code>this</code> 
     */
    @Override
    default BagExpr<E> normalize() {
        return ParametricExpr.super.normalize().cast();
    }
    
}
