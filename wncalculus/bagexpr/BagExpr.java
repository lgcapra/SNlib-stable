
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
    default BagExpr<E> buildSum(Collection<? extends BagExpr<E>> c) {
        throw new UnsupportedOperationException("bag-sum construction is not supported by this bag expression");
    }

    /**
     * Builds a scalar product for this bag-expression family.
     * The default implementation is intentionally conservative: only concrete
     * scalar-product implementations override it.
     *
     * @param arg the bag-expression to scale
     * @param coeff the integer coefficient
     * @return a scalar product built from the given operand and coefficient
     */
    default BagExpr<E> buildScProd(BagExpr<E> arg, int coeff) {
        throw new UnsupportedOperationException("scalar-product construction is not supported by this bag expression");
    }

    default BagIntersection<E> buildIntersection(BagExpr<E> left, BagExpr<E> right) {
        throw new UnsupportedOperationException("bag-intersection construction is not supported by this bag expression");
    }

        
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

    
}
