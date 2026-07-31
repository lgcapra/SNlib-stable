package bagexpr;

import color.Sort;
import expr.Expressions;
import expr.N_aryOp;
import expr.ParametricExpr;
import util.Util;

import java.util.*;


/**
 * this type represents the sum of bag-expressions
 * @author lorenzo capra
 * @param <E> the bag's type
 */
public abstract class N_aryBagOp<E extends ParametricExpr> implements N_aryOp<BagExpr<E>>, BagExpr<E>  {

    protected final Collection <? extends BagExpr<E>> args ;
    protected boolean simplified;

    //constructor: raises an exception if the collection is buildOp
    protected N_aryBagOp(Collection <? extends BagExpr<E>> c, boolean check) {
        if (c.size() < 2) //
            throw new IllegalArgumentException("cannot create an n-ary operator with " + c.size() + " operand(s)!\n");

        this.args = Collections.unmodifiableCollection( c);
        if (check)
            Expressions.checkArity(c);
    }
    

    //maps (if possible) the sum to the bag's sum
    @Override
    public final BagExpr<E> specSimplify() {
      if ( Util.checkAll(this.args, Bag.class::isInstance) ) {
        Iterator<? extends BagExpr<E>> iterator = this.args.iterator();
        System.out.println(args + " (debug)");
        Bag<E> bres = iterator.next().cast();
        while (iterator.hasNext()) {
            Bag<E> bag = iterator.next().cast(); // next bag-operand
            bres = Op(bres, bag);
        }
        return bres;
      }
      return this;
    }

    abstract Bag<E> Op(Bag<E> b1, Bag<E> b2);

    @Override
    public final String symb() {
        return " + ";
    }

    @Override
    public final String toString() {
        return N_aryOp.super.toStringOp();
    }

    @Override
    public final Collection<? extends BagExpr<E>> getArgs() {
        return this.args;
    }
    
    @Override
    public final Bag<E> getIde() {
        return build();
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
    public final Class<E> bagType() {
        return this.args.iterator().next().bagType();
    }
    
    @Override
    public final boolean equals(Object o) {
        return this == o || o!= null && o.getClass() == getClass() && N_aryOp.super.isEqual((N_aryOp<?>)o);
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.args);
        
        return hash;
    }


    @SuppressWarnings("unchecked")
    @Override
    public final N_aryBagOp<E> clone(Map<Sort, Sort> split_map) {
        return (N_aryBagOp<E>) BagExpr.super.clone(split_map); // default implementation is fine
    }

    
}
