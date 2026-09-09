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
     
     /**
     * build a composition between (expressions over) bag-of-functions
     * @param left the bleft-hand bag-expression
     * @param right the bright-hand bag-expression
     * @throws UnsupportedOperationException if the functions are not "linear"
     * @throws IllegalDomain if the operands cannot be composed due their (co-)domains
     */
    public BagComp(BagExpr<E> left, BagExpr<E> right) {
         if (! left.composable(right) ) {
            System.out.println(left.getDomain());
            System.out.println(right.getCodomain());
            throw new IllegalDomain("incompatible domains!\n");
         }
         
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
       return false;
    }
    
    @Override
    public final void setSimplified(boolean simplified) {
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
    public final boolean isLeftAssociative(Class<? extends SingleArg> optk) {
        return ScalarProd.class.isAssignableFrom(optk); // alternative: false
    }
    
    /** 
      * after preliminarily checking whether either operand is an empty bag,
      * if both operands are bags distributes the composition over pairs of bag's elements
     */
    @Override
    public BagExpr<E> specSimplify () {
        BagExpr<E> lx = left(), rx = right();
        
        if (lx.zeroCard() || rx.zeroCard())
            return build();

        if (lx.isConstant()) {
             Integer n = rx.cardLb();
            if (n != null)
                return buildScProd(lx.clone(rx.getDomain()).cast(), n); // Property 1
        }

        //both lx and rx are non-null, with lx other than constant
        if (rx instanceof ScalarProd<E> sp) {
            return buildScProd(buildOp(lx, sp.getArg()), sp.k()); // to do: to check whether the composition of a bag with a scalar product can be simplified by "pushing" the scalar product down the composition tree, i.e. by applying distributivity
        }

        if (lx instanceof ScalarProd<E> sp) {
            return buildScProd(buildOp(sp.getArg(), rx), sp.k()); // to do: to check whether the composition of a bag with a scalar product can be simplified by "pushing" the scalar product down the composition tree, i.e. by applying distributivity
        }
        
                 
        if (lx instanceof Bag<E> lb) { // we can distribute the composition over the bag elements
            ArrayList<BagExpr<E>> blist = new ArrayList<>();
            lb.asMap().forEach((lkey, lvalue) -> 
                blist.add(buildScProd(buildOp(lkey.cast(), rx), lvalue )));

            return buildSum(blist); // efficient
        }

        if (rx instanceof Bag<E> rb) {
            ArrayList<BagExpr<E>> blist = new ArrayList<>();
            rb.asMap().forEach((rkey, rvalue) -> 
                blist.add(buildScProd(buildOp(lx, rkey.cast()), rvalue)));
                               
           return buildSum(blist); // efficient     
        }
         
        return this;
    }
    
    @Override
    public final Class<E> bagType() {
        return left().bagType();
    }


    @Override
    public final String toString() {
        return toStringOp();
    }


    @Override
    public final Integer cardLb() {
        return null; // the cardinality of a composition is not known in general
    }

    @Override
    public final Map<Sort, Integer> splitDelimiters() {
        return BagExpr.super.splitDelimiters();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final BagComp<E> clone(Map<Sort, Sort> split_map) {
        return (BagComp<E>) BagExpr.super.clone(split_map);
    }

    
}
