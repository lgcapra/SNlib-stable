package wncalculus.bagexpr;

import java.util.Map;
import wncalculus.expr.*;

/**
 * this class implements the bag's scalar product
 * @author lorenzo capra
 * @param <E> the bag's domain
 */
public final class ScalarProd<E extends ParametricExpr> extends UnaryBagOp<E> {

    private final int k; 
    
    private ScalarProd(BagExpr<E> arg, int coeff) {
        super(arg);
        this.k = coeff;
    }
    
    /**
     * factory method
     * @param <E> the bag's domain
     * @param arg a bag-expression
     * @param coeff an integer coefficient
     * @return the bag-expression, an build bag, or a ScalarProd object, depending
     * on the coefficient's value (1, 0, other)
     */
    public static <E extends ParametricExpr> BagExpr<E> factory(BagExpr<E> arg, int coeff) {
        BagExpr<E> res;
        switch (coeff) {
            case 1:
                res = arg;
                break;
            case 0:
                res = arg.build();
                break;
            default:
                res = new ScalarProd(arg, coeff);
        }
        return res;
    }

    
    @Override
    public ScalarProd buildOp(BagExpr<E> arg) {
        return new ScalarProd(arg, this.k);
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
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isDistributive (Class<? extends  MultiArgs> optk) {
        return BagSum.class.equals(optk);
    }
    
    //if the argument is a bag maps to the corresponding method
    //manca la trasposta
    @Override
    public BagExpr<E> specSimplify() {
        BagExpr<E> arg = getArg();
        if (arg instanceof ScalarProd) {
            ScalarProd<E> sparg = (ScalarProd<E>) arg;
            return new ScalarProd( sparg.getArg(), sparg.k * this.k);
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
        return super.equals(obj) && this.k == ((ScalarProd<?>)obj).k;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return super.hashCode() + 97 * hash + this.k;
    }
    
    @Override
    public String toString() {
        return "" +this.k + "*("+getArg()+')';
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return getArg().splitDelimiters();
    }
    
}
