package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.Domain;
import wncalculus.expr.Expressions;
import wncalculus.expr.NonTerminal;
import wncalculus.expr.ParametricExpr;

/**
 * @author Lorenzo Capra
   general-purpose implementation of a (non-empty) Bag, viewed as a set pairs (singleton-bags)
   multiplicity are mapped (non mapped elements' multiplicity is zero)
   @param <E> the bag's elements type
 */
public abstract class AbstractBag<E extends ParametricExpr> implements Bag<E> {
        
    private final Map<? extends E,Integer> map ; // the bag is implemented as a map for the sake of efficiency
    private final Domain   dom, codom;
    private boolean simplified;

    
    /**
     * main constructor: builds a <tt>AbstractBag</tt> from a <tt>Map</tt>, which is
     * assumed non-empty; map elements associated with value zero are removed
     * in a destructive way from the map
     * the map is backed by the bag, which provides an unmodifiable view on it
     * @param m a map
     * @throws NoSuchElementException if the map is empty
    */
    public AbstractBag(Map<? extends E, Integer> m) {
       E e = m.keySet().iterator().next(); // throws an exception if m is empty
       m.values().removeAll(Collections.singleton(0));
       Expressions.checkArity(m.keySet());
       this.dom   = e.getDomain();
       this.codom = e.getCodomain();
       this.map = Collections.unmodifiableMap(m);
    }
    
    /**
     * build an empty bag 
     * @param dom the bag's domain
     * @param codom the bag's codomain
     */
    public AbstractBag(Domain dom, Domain codom) {
        this.dom        = dom;
        this.codom      = codom;
        this.simplified = true;
        this.map = Collections.EMPTY_MAP;
    }
    
    
        
    @Override
    public final Map<? extends E, Integer> asMap() {
        return this.map;
    }
              
    @Override
    public final Domain getDomain() {
        return this.dom;
    }

    @Override
    public final Domain getCodomain() {
        return this.codom;
    }
    
     @Override
    public final boolean simplified() {
        return this.simplified;
    }

    @Override
    public final void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }

    @Override
    public final boolean isConstant() {
        return support().stream().allMatch(e -> e.isConstant());
    }
    
    @Override
    public final Class<E> bagType() {
        return support().iterator().next().type();
    }
    
    @Override
    public final String toString( ) {
        if ( this.map.isEmpty() ) 
            return Bag.EMPTY + "_" + this.codom.names();;
            
        Iterator<? extends Map.Entry<? extends E, Integer>> it = this.map.entrySet().iterator(); //necessario?
        Map.Entry<? extends E, Integer> next = it.next();
        String repr = next.getValue()+ prnterm(next.getKey());
        for (int k; it.hasNext() ; repr += k + prnterm(next.getKey()) ) 
            if ( (k = (next = it.next()).getValue() ) > 0)
                repr += '+';
        
        return repr;
    }
    
    private static String prnterm(Object o) {
        return o instanceof NonTerminal ? "("+o+')' : o.toString();       
    }

    @Override
    public final boolean equals (Object o) {
        return super.equals(o) || o != null && getClass().equals(o.getClass()) && ((Bag)o).asMap().equals(this.map);
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.map);
        return hash;
    }
    
}
