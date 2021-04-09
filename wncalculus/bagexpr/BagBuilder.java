package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.Domain;
import wncalculus.expr.ParametricExpr;
import wncalculus.util.Util;
/**
 * this interface describes a generic bag builder
 * @author lorenzo capra
 * @param <E> the bag's domain
 */
public interface BagBuilder<E extends ParametricExpr> {
    
    /**
     * builds an empty bag of the same type as <code>this</code>, with a given arity
     * @param dom the bag's domain
     * @param codom the bag's codomain
     * @return an empty bag of the specified arity
     */
    Bag<E> build(Domain dom, Domain codom);
    
    /**
     * safely builds a bag of the same type as <tt>this</tt> from a given
     * (possibly empty) map
     * @param m a map
     * @return the corresponding bag
     * @throws NoSuchElementException if the map is empty
     */
    Bag<E> build (Map<E, Integer> m) ;
    
    //derived methods
    
    /**
     * builds a singleton-bag
     * @param e the bag's element
     * @param k the multiplicity
     * @return the bag k.e
     */
    default Bag<E> build (int k, E e) {
        return build(Util.singleMap(e, k));
    }
    
    /**
     * builds a bag from a collection of terms
     * @param c a collection of terms
     * @return the bag matching the collection
     */
    default Bag<E> build (Collection<? extends E> c) {
         return build(Util.asMap(c));
     }
     
    /**
     * builds a bag from a list (varargs) of terms
     * @param c a varargs of terms
     * @return the bag matching the list
     */
    default Bag<E> build (E ... c) {
         return build(Arrays.asList(c));
     }
 
}
