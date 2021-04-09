package wncalculus.expr;

import java.util.Collection;

/**
 * this interface is the super-type for any operator
 * @author Lorenzo Capra
 */
public interface NonTerminal extends ParametricExpr {
    
    /**
     * @return the operator's symbol
    */
    public String symb ();
    
        
    
}
