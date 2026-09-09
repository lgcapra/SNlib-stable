
package bagexpr;

import color.Sort;
import expr.*;

import java.util.Collection;
import java.util.Map;

/**
 * the super-type of expressions on bags (of expressions) defined over a given domain;
 * notice that a BagExpr may not be a Bag
 * NOTE currently bags are not parametric expression, even if in perspective they could become
 * @author Lorenzo Capra
 * @param <E> the bag's domain
 */
public interface BagExpr<E extends ParametricExpr> extends ParametricExpr, BagBuilder<E> {
    
    /**
     * @return the bag's elements type 
     */
    Class<E> bagType();

    /**
     * @param c a collection of bag-expressions
     * @return a sum of bag-expressions like <code>this</code> with the specified collection of arguments
     */
    abstract BagExpr<E> buildSum(Collection<? extends BagExpr<E>> c);

    
    /**
     * Builds a scalar product for this bag-expression family.
     * @param arg the bag-expression to scale
     * @param coeff the integer coefficient
     * @return a scalar product built from the given operand and coefficient
     */
    abstract BagExpr<E> buildScProd(BagExpr<E> arg, int coeff);
   
    /**
     * @param c a collection of bag-expressions
     * @return an intersection of bag-expressions like <code>this</code> with
     */
    abstract BagExpr<E> buildIntersection(Collection<? extends BagExpr<E>> c);

        
    /**
     * invokes the normalization algorithn just performing a cast
     * @return a normalized bag equivalent to <code>this</code> 
     */
    @Override
    default BagExpr<E> normalize() {
        return ParametricExpr.super.normalize().cast();
    }

    @Override
    default Map<Sort,Integer> splitDelimiters (){
        throw new IllegalDomain("multisets are not parametric");
    }

    @Override
    default ParametricExpr clone(Map<Sort, Sort> split_map) {
            throw new UnsupportedOperationException("clone with split_map is not supported for multisets");
    }

    @Override
    default Bag<E> getNull() {
        return build();
    }

    default boolean zeroCard() {
        Integer card = cardLb();
        return card != null && card == 0;   
    }
    
}
