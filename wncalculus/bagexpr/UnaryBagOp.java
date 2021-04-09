package wncalculus.bagexpr;

import java.util.Map;
import java.util.Objects;
import wncalculus.expr.Domain;
import wncalculus.expr.ParametricExpr;
import wncalculus.expr.UnaryOp;

/**
 * this abstract class represent any unary bag operator
 * @author lorenzo capra
 * @param <E> the bag's elements type
 */
public abstract class UnaryBagOp<E extends ParametricExpr> implements UnaryOp<BagExpr<E>>, BagExpr<E> {
    
    private final BagExpr<E> bexpr; // the function to be transposed
    private boolean simplified;
    
    /**
     * buils a unary bag-operator
     * @param b the bag-expression operand
     */
    public UnaryBagOp (BagExpr<E> b) {
        this.bexpr = b;
    }

    @Override
    public final BagExpr<E> getArg() {
        return this.bexpr;
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
    public final Class<? extends BagExpr> type() {
        return BagExpr.class;
    }

    @Override
    public final Class<E> bagType() {
        return this.bexpr.bagType();
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.bexpr);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) 
            return true;
        
        if (obj == null || getClass() != obj.getClass()) 
            return false;
        
        return Objects.equals(this.bexpr, ((UnaryBagOp<?>) obj).bexpr);
    }
    
    @Override
    public final Bag<E> build(Domain dom, Domain codom) {
        return this.bexpr.build(dom, codom);
    }
    
    @Override
    public final Bag<E> build(Map<E, Integer> m) {
        return getArg().build(m);
    }

}
