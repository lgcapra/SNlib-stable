package wncalculus.expr;

/**
 * this interface denotes expressions that can be transposed
 * @author Lorenzo Capra
 */
public interface Transposable extends ParametricExpr {
    
    /**
     * @return the transpose operator apllied applied to <code>this</code> 
     */
    ParametricExpr buildTransp() ;
}
