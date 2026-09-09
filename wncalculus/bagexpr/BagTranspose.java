package bagexpr;

import java.util.*;
import expr.*;


/**
 * This class implements the transposing of a bag of bexpr-tuples
 * @author Lorenzo Capra
 * @param <E> the bag-expression's type
 */
public abstract class BagTranspose<E extends ParametricExpr> extends UnaryBagOp<E> {

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
        return BagSum.class.isAssignableFrom(optk);
    }

}
