package wncalculus.bagexpr;

import java.util.*;
import wncalculus.expr.*;
import wncalculus.logexpr.SetExpr;

/**
 * the super-type of the composition of bags of (linear) functions
 * @author lorenzo capra
 * @param <E> the bag's domain
 */
public final class BagComp<E extends ParametricExpr> implements BagExpr<E>, CompositionOp<BagExpr<E>,BagExpr<E>> {

     private final BagExpr<E> left, right;
     private boolean simplified;
     
     /**
     * build a composition between (expressions over) bag-of-functions
     * @param left the bleft-hand bag-expression
     * @param right the bright-hand bag-expression
     * @throws UnsupportedOperationException if the functions are not "linear"
     * @throws IllegalDomain if the operands cannot be composed due their (co-)domains
     */
    public BagComp(BagExpr<E> left, BagExpr<E> right) {
         if (! left.composable(right) )
            throw new IllegalDomain();
         
         this.left = left;
         this.right= right;
     }
    
    @Override
    public final BagExpr<E>  left() {
        return this.left;
    }
    
    @Override
    public final BagExpr<E>  right() {
        return this.right;
    }
     
    @Override
    public final boolean simplified() {
        return simplified;
    }
    
    @Override
    public final void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
    
    /**
     * @param optk an operator type
     * @return <code>true</code> if and only if this composition is bleft-distributive
     * w.r.t. the specified operator
     */
    @Override
    public boolean isDistributive (Class<? extends  MultiArgs> optk) {
        return BagSum.class.isAssignableFrom(optk);
    }
    
    @Override
    public boolean isLeftAssociative(Class<? extends SingleArg> optk) {
        return ScalarProd.class.isAssignableFrom(optk); // alternative: false
    }
    
    /** 
      * after preliminarily checking whether either operand is an empty bag,
      * if both operands are bags distributes the composition over pairs of bag's elements
     */
    @Override
    public BagExpr<E> specSimplify () {
        BagExpr<E> lx = left(), rx = right();
        if (rx instanceof ScalarProd) { // si potrebbe mettere nella parte generica
            ScalarProd<E> sp = (ScalarProd<E>) rx;
            return ScalarProd.factory(new BagComp<>(lx, sp.getArg()), sp.k());
        }
        
        if (lx instanceof Bag ) {
            Bag<E> lb = (Bag<E>) lx;
            if (lb .isEmpty() ) 
                return build();
            
            if (rx instanceof Bag) {
                Bag<E> rb = (Bag<E>) rx;
                if (rb .isEmpty()) 
                    return build();
                
                if ( lb.isConstant() ) { // composition between a constant and a constant-size bag
                    Integer c = rb.card();        
                    if (c != null)
                        return ScalarProd.factory((BagExpr<E>) lb.clone(rb.getDomain()), c);
                }
                else { //both lb and rb are (non empty) bags, with lb other than constant
                    ArrayList<BagExpr<E>> blist = new ArrayList<>();
                    if ( lb.size() == 1 ) { // the left one is a singleton bag
                        E lf = lb.support().iterator().next(); // the left bag's term
                        int k = lb.mult(lf); 
                        if (rb.size()== 1) { // the right one  is a singleton bag
                            E rf = rb.support().iterator().next();
                            if (lf instanceof SetExpr) {
                                BagExpr<E> b = ((SetExpr)lf).buildBagComp((SetExpr)rf).cast(); // we try to compose the linear function with the right-one ...
                                if (b != null)
                                    return ScalarProd.factory( b, k * rb.mult(rf)).cast(); 
                            }
                        }
                        else   // the right operand is a bag with many terms
                            rb.asMap().entrySet().forEach(y -> { blist.add( new BagComp(lb, build(y.getValue(),y.getKey()))); });
                    } 
                    else  // the left operand is a bag with many terms  
                        lb.asMap().entrySet().forEach(x -> { blist.add(new BagComp(lb.build(x.getValue(), x.getKey()), rb )); });

                    if (! blist.isEmpty() )
                        return BagSum.factory(blist, false);
                }
            }
        }
         
        return this;
    }
    
    @Override
    public final Class<? extends BagExpr> type() {
        return BagExpr.class;
    }
    
    @Override
    public final Class<E> bagType() {
        return left().bagType();
    }
    
    @Override
    public String toString() {
        return toStringOp();
    }

    @Override
    public BagComp<E> buildOp(BagExpr<E> left, BagExpr<E> right) {
        return new BagComp(left, right);
    }

    @Override
    public Bag<E> build(Domain dom, Domain codom) {
        return this.left.build(dom, codom);
    }

    @Override
    public Bag<E> build(Map<E, Integer> m) {
        return this.left.build(m);
    }
    
}
