package bagexpr;

import java.util.*;
import expr.*;


/**
 * This class implements the transposing of a bag of bexpr-tuples
 * @author Lorenzo Capra
 * @param <E> the bag-expression's type
 */
public final class BagTranspose<E extends Transposable> extends UnaryBagOp<E> {

    /**
     * build the transpose of a bag-expression
     * @param b a bag-expression
     */
    public BagTranspose (BagExpr<E> b) {
        super(b);
    }
    
    @Override
    public BagExpr<E> specSimplify() {
        BagExpr<E> arg = getArg();
        if ( ! (arg instanceof Bag) ) 
            return this;
        
        Bag<E> bag = (Bag<E>) arg;
        if (bag.isEmpty()) 
            return build(); //should be right!
        
        Class<E> type = bagType();
        Map<E,Integer> trmap = new HashMap<>();
        bag.asMap().entrySet().forEach( e -> { trmap.put( type.cast(e.getKey().buildTransp()) , e.getValue() ); });
        
        return bag.build(trmap); 
    }
    
    /*
    the domain and the codomain are "inverted"
    */
    @Override
    public Domain getDomain() {
        return super.getCodomain();
    }

    @Override
    public Domain getCodomain() {
        return super.getDomain();
    }
    
    @Override
    public BagTranspose buildOp(BagExpr<E> arg) {
        return new BagTranspose (arg);
    }

    @Override
    public String symb() {
        return "'";
    }
    
    @Override
    public String toString() {
        return toStringPost();
    }

    @Override
    public boolean isInvolution() {
        return true;
    }
    
    @Override
    public boolean isDistributive (Class<? extends  MultiArgs> optk) {
        return BagSum.class.equals(optk);
    }
    
     /**
     * clone <tt>this</tt> transpose; the method is overridden because the
     * arity of the operand and that of the operator are "inverted"
     * of the operator are the same (to be overridden otherwise)
     * @param newdom the new domain
     * @param newcd the new codomain
     * @param smap the map between old and new split sorts
     * @return a clone of <tt>this</tt> with the specified co-domain
     */
    /*
    @Override
    public BagTranspose<E> clone (final Domain newdom, final Domain newcd) {
        return new BagTranspose<> (getArg().clone(newcd, newdom). cast());
    }*/

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return getArg().splitDelimiters();
    }
    
}
