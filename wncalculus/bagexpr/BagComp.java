package bagexpr;

import java.util.*;

import color.Sort;
import expr.*;

/**
 * the super-type of the composition of bags of (linear) functions
 * @author lorenzo capra
 * @param <E> the bag's domain
 */
public abstract class BagComp<E extends ParametricExpr> implements BagExpr<E>, CompositionOp<BagExpr<E>,BagExpr<E>> {

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
        if (rx instanceof ScalarProd) { 
            ScalarProd<E> sp = (ScalarProd<E>) rx;
            return buildScProd(buildOp(lx, sp.getArg()), sp.k()); // to do: to check whether the composition of a bag with a scalar product can be simplified by "pushing" the scalar product down the composition tree, i.e. by applying distributivity
        }
        
        if (lx instanceof Bag ) {
            Bag<E> lb = (Bag<E>) lx;
            if (lb.isEmpty() ) 
                return build();
            
            if (rx instanceof Bag) {
                Bag<E> rb = (Bag<E>) rx;
                if (rb .isEmpty()) 
                    return build();
                
                if ( lb.isConstant() ) { // composition between a constant and a constant-size bag
                    Integer c = rb.cardLb();        
                    if (c != null)
                        return ((Bag<E>)lb.clone(rb.getDomain())).scalarProd(c);
                }
                else { //both lb and rb are (non empty) bags, with lb other than constant
                    ArrayList<BagExpr<E>> blist = new ArrayList<>();
                    if ( lb.size() == 1 ) { // the left one is a singleton bag
                        E lf = lb.support().iterator().next(); // the left bag's term
                        int k = lb.mult(lf); 
                        if (rb.size()== 1) { // the right one  is a singleton bag
                            E rf = rb.support().iterator().next();
//                            // to do
                        }
                        else   // the right operand is a bag with many terms
                            rb.asMap().forEach((key, value) -> blist.add(buildOp(lb, rb.build(value, key))));
                    } 
                    else  // the left operand is a bag with many terms  
                        lb.asMap().forEach((key, value) -> blist.add(buildOp(lb.build(value, key), rb)));

                    if (! blist.isEmpty() )
                        return buildBagSum(blist);
                }
            }
        } else if (lx instanceof ScalarProd) {
            ScalarProd<E> sp = (ScalarProd<E>) lx;
            return buildScProd(buildOp(sp.getArg(), rx), sp.k()); // to do: to check whether the composition of a bag with a scalar product can be simplified by "pushing" the scalar product down the composition tree, i.e. by applying distributivity
        }
         
        return this;
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
    public Integer cardLb() {
        return null;
    }


    @Override
    public BagComp<E> clone(Map<Sort, Sort> split_map) {
        return (BagComp<E>) BagExpr.super.clone(split_map);
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return BagExpr.super.splitDelimiters();
    }
    
}
