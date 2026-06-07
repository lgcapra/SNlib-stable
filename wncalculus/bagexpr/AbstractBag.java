package bagexpr;

import java.util.*;

import expr.*;

/**
 * @author Lorenzo Capra
   general-purpose implementation of a (non-empty) Bag, viewed as a set pairs (singleton-bags)
   multiplicity are mapped (non mapped elements' multiplicity is zero)
   @param <E> the bag's elements type
 */
public abstract class AbstractBag<E extends ParametricExpr> implements Bag<E> {
        
    private final Map<E,Integer> map ; // the bag is implemented as a map for the sake of efficiency
    private final Domain   dom, codom;
    private boolean simplified, disjoined;

    
    /**
     * main constructor: builds a <tt>AbstractBag</tt> from a <tt>Map</tt>, which is
     * assumed non-empty; map elements associated with value zero are removed
     * in a destructive way from the map
     * the map is backed by the bag, which provides an unmodifiable view on it
     * @param m a map
     * @throws NoSuchElementException if the map is empty or if all its elements are associated with value zero
    */
    public AbstractBag(Map<? extends E, Integer> m) {
        for (E e : m.keySet()) {
            if(e.isParametric())
                throw new IllegalDomain("bag elements cannot be parametric expressions: "+e);
        }
       m.values().removeAll(Collections.singleton(0));
       Expressions.checkArity(m.keySet());
       E e = m.keySet().iterator().next();
       this.dom   = e.getDomain();
       this.codom = e.getCodomain();
       this.map = Collections.unmodifiableMap(m);
       this.disjoined = m.size() < 2;
    }
    
    /**
     * build an empty bag 
     * @param dom the bag's domain
     * @param codom the bag's codomain
     */
    public AbstractBag(Domain dom, Domain codom) {
        if (dom.isParametric() || codom.isParametric())
            throw new IllegalDomain("bags cannot be parametric expressions");
        this.dom        = dom;
        this.codom      = codom;
        this.simplified = true;
        this.map = Collections.emptyMap();
        this.disjoined  = true;
    }

    protected void setDisjoined(boolean disjoined) {
        this.disjoined = disjoined;
    }

    public boolean disjoined() {
        return this.disjoined;
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

    @Override
    public final Integer cardLb() {
        int card = 0;
        for (Map.Entry<? extends E, Integer> x : asMap().entrySet()) {
            Integer k = x.getKey().cardLb();
            if (k == null)
                return null;
            card += k * x.getValue();
        }
        return card;
    }
    

     //the following methods are protected because they can modify the current object  

     /* 
     * sets the multiplicity of an element; if the multiplicity is zero, the element is removed from the bag;   
     * @param k the multiplicity of the element
     * @param e the element
     */
    protected void set(E e, int k) {
        if (k == 0)
            this.map.remove(e);
        else
            this.map.put(e, k);
    }

    /**
     * @param b a bag    
     * sums up the multiplicities of the elements of <code>b</code> to those of <code>this</code>
     * @return <code>this</code> 
     * 
     */
    protected AbstractBag<E> sumUp(Bag<E> b) {
        for (Map.Entry<? extends E, Integer> x : b.asMap().entrySet()) {
            int k = b.mult(x.getKey());
            if (k != 0)
                this.set(x.getKey(), k + x.getValue());
        }
        return this;
    }

    /**
     * @param b a bag    
     * if <code>b</code> and <code>this</code> are proper bags, 
     * sets the multiplicity of an element to the minimum between its multiplicity in <code>this</code> and in <code>b</code>
     * @return <code>this</code> 
     * 
     */
    protected AbstractBag<E> intersection(Bag<E> b) {
        if (isProperBag() && b.isProperBag()) {
            for (Map.Entry<? extends E, Integer> x : b.asMap().entrySet()) {
                int k = b.mult(x.getKey());
                this.set(x.getKey(), Math.min(k, x.getValue()));
            }
        }
        return this;
    }

    @Override
    public  Bag<E> clone() throws CloneNotSupportedException {
        return (Bag<E>) super.clone();
    }

}
