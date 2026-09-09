package bagexpr;

import java.util.*;
import expr.*;


/**
 * this type represents the sum of bag-expressions
 * @author lorenzo capra
 * @param <E> the bag's type
 */
public abstract class BagSum<E extends ParametricExpr> extends N_aryBagOp<E> {

    Integer card = null; // the cardinality of the sum (if it exists)
    
    protected BagSum(Collection<? extends BagExpr<E>> c, boolean check) {
        super(c, check);
    }
    
   @Override
    public final Bag<E> Op(Bag<E> b1, Bag<E> b2){
        return b1.sum(b2);
    }

    @Override
    public final Integer cardLb() {
        if (card == null) {
            card = 0;
            for (BagExpr<E> b : this.getArgs()) {
                Integer c = b.cardLb();
                if (c == null)
                    return card = null;
                card += c;
            }
        }
        return card;
    }
    
}
