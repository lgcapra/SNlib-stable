package wncalculus.bagexpr;

import java.util.Map;
import java.util.Objects;
import wncalculus.expr.CompositionOp;
import wncalculus.expr.Domain;
import wncalculus.expr.SingleArg;
import wncalculus.logexpr.SetExpr;

/**
 * this class implements the bag-composition between set-expressions 
 * @author lorenzo capra
 */
public abstract class SetComp<E extends SetExpr> implements BagExpr<E>, CompositionOp<E,BagExpr<E>> {

    private final E left,right;
    private boolean simplified;
    
    public SetComp(E left, E right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public final Class<E> bagType() {
        return left.type();
    }
    
    @Override
    public final Class<? extends BagExpr> type() {
        return BagExpr.class;
    }

    @Override
    public final boolean simplified() {
        return simplified;
    }

    @Override
    public final void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }

    @Override
    public final LogicalBag<E> build(Domain dom, Domain codom) {
        return (left.asBag()).build(dom,codom).cast();
    }

    @Override
    public final LogicalBag<E> build(Map<E, Integer> m) {
        LogicalBag<E> b = left.asBag();
        return (b.build(m));
    }

    @Override
    public BagExpr<E> genSimplify() {
        BagExpr<E> res = CompositionOp.super.genSimplify();
        if (res == this) {
            if (left.isFalse() || right.isFalse())
                return build(getDomain(), getCodomain());
            //if the right operand has cardinality one it reduces to a set-composition
            Integer k = right.cardLb();
            if  (k != null && k == 1 )
                res = left.buildSetComp(right).asBag();
        }
        
        return res;
    }

    @Override
    public final boolean isLeftAssociative(Class<? extends SingleArg> optk) {
        return false;
    }

    @Override
    public final E left() {
        return this.left;
    }

    @Override
    public final E right() {
        return this.right;
    }
    
    @Override
    public final String toString() {
        return CompositionOp.super.toStringOp();
    }
    
    @Override
    public final boolean equals (Object o) {
        return CompositionOp.super.isEqual(o);
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.left);
        hash = 31 * hash + Objects.hashCode(this.right);
        return hash;
    }

}
