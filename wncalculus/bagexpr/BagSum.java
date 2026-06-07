package bagexpr;

import util.Util;
import java.util.*;
import color.Sort;
import expr.*;


/**
 * this type represents the sum of bag-expressions
 * @author lorenzo capra
 * @param <E> the bag's type
 */
public abstract class BagSum<E extends ParametricExpr> implements N_aryOp<BagExpr<E>>, BagExpr<E>  {
    
    private final Collection <? extends BagExpr<E>> args ;
    private boolean simplified;
    
    //constructor: raises an exception if the collection is buildOp
    protected BagSum(Collection <? extends BagExpr<E>> c, boolean check) {
        this.args = Collections.unmodifiableCollection( c);
        if (check)
            Expressions.checkArity(c);
    }
    

    //maps (if possible) the sum to the bag's sum
    @Override
    public BagExpr<E> specSimplify() {
      if ( Util.checkAll(this.args, Bag.class::isInstance) ) {
        Iterator<? extends BagExpr<E>> iterator = this.args.iterator();
        Bag<E> bsum = ((Bag<E>) iterator.next());
        try {
            bsum = bsum.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        } // copy of first bag-operand
        while (iterator.hasNext()) {
                Bag<E> bag = (Bag<E>) iterator.next(); // next bag-operand
                bsum = bsum.sum(bag); // sum between bags
             }
             return bsum;
        }  
    
      return this;
    }
        

    @Override
    public String symb() {
        return " + ";
    }

    @Override
    public String toString() {
        return N_aryOp.super.toStringOp();
    }

    @Override
    public Collection<? extends BagExpr<E>> getArgs() {
        return this.args;
    }
    
    @Override
    public Bag<E> getIde() {
        return build();
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }

    @Override
    public Class<E> bagType() {
        return this.args.iterator().next().bagType();
    }
    
    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof N_aryOp && N_aryOp.super.isEqual((N_aryOp<?>)o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.args);
        
        return hash;
    }


    @Override
    // could be moved in N_aryOp
    public Integer cardLb() {
        int card = 0;
        for (BagExpr<E> b : this.args) {
            Integer c = b.cardLb();
            if (c == null)
                return c;
            card += c;
        }      
        return card;     
    }


    @SuppressWarnings("unchecked")
    @Override
    public BagSum<E> clone(Map<Sort, Sort> split_map) {
        return (BagSum<E>) BagExpr.super.clone(split_map); // default implementation is fine
    }

    
}
