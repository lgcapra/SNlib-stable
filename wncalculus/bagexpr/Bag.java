package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.Domain;
import wncalculus.expr.ParametricExpr;
import wncalculus.expr.Sort;

//Si potrebbe definire una super-interfaccia che rappresenta bag di qualsiasi tipo

/**
 * @author Lorenzo Capra
 * the abstract data type for a "generalised" Bag, i.e., a set whose elements have an associated
 * (signed) integer multiplicity
 * @param <E> the domain of the elements of the bag 
 */
public interface Bag<E extends ParametricExpr> extends BagExpr<E>   {
    
    /**
     *
     */
    static final String EMPTY = "<null>";
    /** 
     * @return a map-view of this bag
     */
    Map<? extends E,Integer> asMap ();
     
    /**
     * @param e a given element
     * @return the multiplicity of the element in the multiset; 0 if it is not present
     */
    default int mult(E e) {
        Integer x = asMap().get(e);
        return x != null ? x : 0;
    }
    
    /** 
     * @return the support of the multi-set, i.e., the set of its elements with multiplicity
        different from zero; the set is backed by the bag
    */
    default Set<? extends E> support() {
        return asMap().keySet();
    }
    
    @Override
    default boolean isConstant() {
        return support().stream().allMatch( x -> x.isConstant() );
    }
    
    /**
     * @return the bag's support size
     */
    default int size() {
        return asMap().size();
    }
    
    /**
     * @return <code>true</code> if and only if the multi-set is build 
     */
    default boolean isEmpty() {
        return size() == 0;
    }
             
    /**
     * @return the elements of this bag with coefficients greater than zero:
     * <tt>null</tt> if, for any reasons, they cannot be computed
     */
    default Set<? extends E> properSupport() {
        HashSet<E> pset = new HashSet<>();
        asMap().entrySet().stream().filter(e -> ( e.getValue() > 0 )).forEachOrdered(e -> { pset.add(e.getKey()); });
            
        return pset;
    }
    
    /**
     * 
     * @return <code>true</code> if and only if <code>this</code> is a true bag 
     */
    default boolean isProperBag () {
        return isEmpty() || Collections.min(asMap().values()) > 0;
    }
    
    
    /**
     * SI DOVREBBE ELIMINARE (SPOSTARE NELLA CLASSE OMONIMA)
     * does the scalar product between <code>this</code> bag and a given coefficient 
     * @param coeff an integer coefficient
     * @return a new bag obtained by multiplying <code>this</code>' coefficients by
     * by <code>coeff</code> ; <code>this</code> if <code>coeff == 1</code>
     */
    default Bag<E> scalarProd(int coeff) {
        if ( isEmpty() ) 
            return this;
          
        HashMap<E,Integer> m = new HashMap<>();
        asMap().entrySet().forEach(e -> { m.put(e.getKey(), coeff * e.getValue()); });
        
        return build(m);
    }
    
    @Override
    default Class<? extends BagExpr> type() {
        return BagExpr.super.type();
    }
    
    /**
     * 
     * @return the bag's cardinality, meant as the cardinality of the application of the
     * corresponding bag-function to any argument resulting in a non-empty bag;
     * <code>null</code> if the cardinality cannot be computed (i.e., the bag-function is not 
     * constant-size)
     */
    Integer card();
   
    
    @Override
    default Bag<E> clone(Domain newdom) {
         if (isEmpty())
            return build(newdom, getCodomain()) ;
         
         HashMap<E,Integer> mapcopy = new HashMap<>();
         asMap().entrySet().forEach(e -> { mapcopy.put(bagType().cast(e.getKey().clone(newdom)), e.getValue()); });
         
         return build(mapcopy);
    }
    
    @Override
     default Bag<E> clone (final Map<Sort, Sort> split_map) {
        if (isEmpty())
            return build(getDomain().setSupport(split_map), getCodomain().setSupport(split_map)) ;
         
         HashMap<E,Integer> mapcopy = new HashMap<>();
         asMap().entrySet().forEach(e -> { mapcopy.put(bagType().cast(e.getKey().clone(split_map)), e.getValue()); });   
         return build(mapcopy);
     }
    
}
