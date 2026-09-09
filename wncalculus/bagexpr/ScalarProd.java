package bagexpr;

import expr.*;

/**
 * this class implements the bag's scalar product
 * @author lorenzo capra
 * @param <E> the bag's domain
 */
public abstract class ScalarProd<E extends ParametricExpr> extends UnaryBagOp<E> {

    protected final int k; 
    
    protected ScalarProd(BagExpr<E> arg, int coeff) {
        super(arg);
        this.k = coeff;
    }

    /**
     * 
     * @return the scalar value 
     */
    public int k() {
        return k;
    }

    @Override
    public String symb() {
        return this.k + "." + getArg();
    }

    @Override
    public boolean isDistributive (Class<? extends  MultiArgs> optk) {
        return BagSum.class.isAssignableFrom(optk);
    }
    
    //if the argument is a bag maps to the corresponding method
    //manca la trasposta
    @Override
    public BagExpr<E> specSimplify() {
        if (this.k == 0) {
            return build();
        }

        final BagExpr<E> arg = getArg();
        if (this.k == 1) {
            return arg;
        }

        if (arg instanceof ScalarProd sprod) {
            return buildScProd(sprod.getArg().cast(), sprod.k * this.k);
        }
        
        if (arg instanceof Bag) {
            Bag<E> b = (Bag<E>) arg;
            return b.isEmpty() ? b : b.scalarProd(this.k) ;
        }
        
        return this;
    } 

    @Override
    public boolean isInvolution() {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj || super.equals(obj) && this.k == ((ScalarProd<?>)obj).k;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return super.hashCode() + 97 * hash + this.k;
    }
    
    @Override
    public String toString() {
        return this.k + "*("+getArg()+')';
    }
    
}
