package bagexpr;

import java.util.*;
import expr.*;


/**
 * this type represents the sum of bag-expressions
 * @author lorenzo capra
 * @param <E> the bag's type
 */
public abstract class BagSum<E extends ParametricExpr> extends N_aryBagOp<E> {

    protected BagSum(Collection<? extends BagExpr<E>> c, boolean check) {
        super(c, check);
    }
    
   @Override
    public Bag<E> Op(Bag<E> b1, Bag<E> b2){
        return b1.sum(b2);
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

    
}
