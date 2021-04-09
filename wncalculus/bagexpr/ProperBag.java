package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.ParametricExpr;
import wncalculus.expr.Sort;

/**
 * this class defines a unary operator that extratcs the positive part of a bag
 * @author lorenzo capra
 * @param <E> the bag's elements type
 */
public class ProperBag<E extends ParametricExpr> extends UnaryBagOp<E> {
    
    /**
     * build a proper-bag from a bag-expression
     * @param b a bag-expression
     */
    public ProperBag(BagExpr<E> b) {
        super(b);
    }
    
    @Override
    public boolean isInvolution() {
        return false;
    }

    @Override
    public ProperBag<E> buildOp(BagExpr<E> arg) {
        return new ProperBag(arg);
    }

    @Override
    public String symb() {
        return "proper";
    }
    
    @Override
    public BagExpr<E> specSimplify() {
        BagExpr<E> arg = getArg();
        if (arg instanceof Bag){
            Bag<E> b = (Bag<E>) arg;
            if (b.isProperBag())
                return b;
            
            Set<? extends E> supp =  b.properSupport();
            if (supp != null) {
                HashMap<E, Integer> bprop = new HashMap<>();
                supp.forEach(e -> { bprop.put(e, b.mult(e)); });
                
                return b.build(bprop);
            }
        }
        
        return  this;
    }
    
    @Override
    public String toString() {
        return toStringOp();
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return getArg().splitDelimiters();
    }
        
}