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

    private final Collection <? extends BagExpr<E>> args ;
    
    //constructor: raises an exception if the collection is buildOp
    protected N_aryBagOp(Collection <? extends BagExpr<E>> c, boolean check) {
        if (c.size() < 2) //
            throw new IllegalArgumentException("cannot create an n-ary operator with " + c.size() + " operand(s)!\n");

        if (check)
            Expressions.checkArity(c);
        
        this.args = Collections.unmodifiableCollection(c);
    }
    

    //maps (if possible) the sum to the bag's sum
    @Override
    public BagExpr<E> specSimplify() {
      if ( Util.checkAll(this.args, Bag.class::isInstance) ) {
        Iterator<? extends BagExpr<E>> iterator = this.args.iterator();
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
        return false;
    }

    @Override
    public final void setSimplified(boolean simplified) {
        // Do nothing, as simplified is always false
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
        final int hash = 7;
        return 59 * hash + Objects.hashCode(this.args);
    }


    @SuppressWarnings("unchecked")
    @Override
    public final N_aryBagOp<E> clone(Map<Sort, Sort> split_map) {
        return (N_aryBagOp<E>) BagExpr.super.clone(split_map); // default implementation is fine
    }
    
}
