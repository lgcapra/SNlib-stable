package logexpr;

import expr.Expression;
import expr.NonTerminal;

/**
 * this interface defines an expression with an associated logical condition
 * (a <code>null</code> condition corrisponds to "true")
 * @author lorenzo capra
 * @param <E> the type of guarded expression
 */
public interface GuardedExpr<E extends LogicalExpr> extends NonTerminal {
    
    /**
     * @return the associated logical condition
     */
    LogicalExpr guard();
    
    /**
     * @return the associated (possibly not boolean) expression
     */
    E expr();
    
    
    /**
     * 
     * @param expr an expression
     * @param guard a boolean condition (<code>null</code> means "true")
     * @return a guarded expression of the same type as <code>this</code>
     * @throws ClassCastException if the concrete type of <code>guard</code> is not
     * the expected one
     */
    E build(E expr, LogicalExpr guard);
    
    
    @Override
    default Expression genSimplify() {
        LogicalExpr g = guard().normalize();
        E e = expr();
        if (g.isTrue() )
            return e;
        
        if ( g.isFalse() )
           return e.getFalse();
        
        LogicalExpr f = e.normalize();
        return f.isFalse() ?  f : build(f.cast(), g);
    }
    
}
