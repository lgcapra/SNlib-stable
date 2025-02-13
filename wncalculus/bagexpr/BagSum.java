package bagexpr;

import util.Util;
import java.util.*;
import color.ColorClass;
import expr.*;


/**
 * this type represents the sum of bag-expressions
 * @author lorenzo capra
 * @param <E> the bag's type
 */
public final class BagSum<E extends ParametricExpr> implements N_aryOp<BagExpr<E>>, BagExpr<E>  {
    
    private final Collection </*? extends*/ BagExpr<E>> args ;
    private boolean simplified;
    
    //constructor: raises an exception if the collection is buildOp
    private BagSum(Collection <? extends BagExpr<E>> c, boolean check) {
        this.args = Collections.unmodifiableCollection( c);
        if (check)
            Expressions.checkArity(c);
    }
   
    
    //factory methods: assume that the collectiom is not empty (otherwise throw an exception)

    /**
     * main builder method;
     * build a sum of bag-expressions from a collection; if the collection is a singleton
     * its only element is returned
     * @param <E> the bag's type
     * @param c a collection of bag-expressions
     * @param check domain-check flag
     * @return the bag-expression resulting from a sum of bag-expressions
     */
    public static <E extends ParametricExpr> BagExpr<E> factory (Collection<? extends BagExpr<E>> c,  boolean check) {
        return c.size() < 2 ?  c.iterator().next() : new BagSum(c, check);
    }
            
    /**
     * build a sum of bag-expressions from a list (varargs) of bag-expressions
     * @param <E> the bag's type
     * @param args a list of bag-expressions
     * @return the bag-expression resulting from a sum of bag-expression
     */
    public static <E extends ParametricExpr> BagExpr<E> factory(BagExpr<E> ... args) {
        return factory(Arrays.asList(args), false);
    }
    
    //maps (if possible) the sum to the bag's sum
    @Override
    public BagExpr<E> specSimplify() {
      if ( Util.checkAll(this.args, Bag.class::isInstance) ) {
          Iterator<? extends BagExpr<E>> iterator = this.args.iterator();
          Bag<E> b0 = (Bag<E>) iterator.next(); // first bag-operand
          Map<E,Integer> b = new HashMap<>(b0.asMap()); // "copy" of b0
          while (iterator.hasNext())
              sum(b, (((Bag<E>)iterator.next()).asMap()));

          return build(b);
      }
      
      return this;
    } 

    @Override
    public String symb() {
        return " + ";
    }

    
    @Override
    public Class<? extends BagExpr> type() {
        return BagExpr.class;
    }

    @Override
    public String toString() {
        return N_aryOp.super.toStringOp();
    }

    @Override
    public Collection</*? extends*/ BagExpr<E>> getArgs() {
        return this.args;
    }

    /**
     *
     * @param args a (possibly empty) collection of bag expressions 
     * @return  if the collection is non empty, a sum (like factory)
     * otherwise an empty bag
     */
    @Override
    public BagExpr<E> buildOp(Collection<? extends BagExpr<E>> args) {
         return args.isEmpty() ? build() : factory(args, true);
    }

    
    @Override
    public Bag<E> getIde() {
        return this.args.iterator().next().build();
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
    
    /*
    "sums" two bags by putting the result in the first one, which is returned
    */
    private Map<E,Integer> sum (Map<E,Integer> b, Map<? extends E,Integer> m) {
        if ( !m.isEmpty() ) {
            Map<E, Integer> i = intersection(b,m);
            b.putAll(m);
            b.putAll(i); //this sequence is the right one
        }
        
        return b;
    }
    
    /*
    efficiently computes the "i" of two bags, by summing multiplicities 
    */
    private Map<E,Integer> intersection (Map<? extends E,Integer> b, Map<? extends E,Integer> m) {
        Map<? extends E, Integer> small = b, big = m;
        if (b.size() > m.size()) {
            small = m;
            big = b;
        }
        Integer v;
        E key;
        Map<E,Integer> i = new HashMap<>();
        for (Map.Entry<? extends E,Integer> x : small.entrySet()) 
            if ( (v = big.get(key = x.getKey()) ) != null)
                i.put(key, v + x.getValue());
        //System.out.println("intersection: "+i); //debug
        return i;
    }

    @Override
    public Class<E> bagType() {
        return this.args.iterator().next().bagType();
    }

    @Override
    public Bag<E> build(Map<E, Integer> m) {
        return this.args.iterator().next().build(m);
    }
    
    @Override
    public Bag<E> build(Domain dom, Domain codom) {
        return this.args.iterator().next().build(dom, codom);
    }
    
    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof N_aryOp && N_aryOp.super.isEqual((N_aryOp)o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.args);
        
        return hash;
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return ColorClass.mergeSplitDelimiters(this.args);
    }

    
}
