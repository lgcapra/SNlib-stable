package wncalculus.logexpr;

import java.util.Collection;
import wncalculus.expr.MultiArgs;
import wncalculus.expr.N_aryOp;
import wncalculus.tuple.FunctionTuple;
import static wncalculus.logexpr.LogicalExprs.checkComplementary;

/**
 * this interface defines the super-type of boolean operators that can be viewed as "conjunctions"
 * @author lorenzo capra
 * @param <E> the operands type
 */
public interface AndOp<E extends LogicalExpr> extends LogicalExpr, N_aryOp<E> { 
    
    /**
     * provides a sufficient condition for a given "And" expressions to be "false";
     * @return  <code>true</code> if the operator is "false" 
     */
    @Override
    default boolean isFalse() {
        return getArgs().contains(getFalse());
    }
    
    /**
     * provides a sufficient condition for an "And" expressions to be "true";
     * @return  <code>true</code> if the operator is "true"
     */
    @Override
    default boolean isTrue() {
        LogicalExpr True = getTrue();
        
        return getArgs().stream().allMatch( t -> True.equals(t) );
    }
    
  
    @Override
    default E getIde() {
       Class<E> type = type();
       
       return type.cast(getTrue());
    }
    
     /**
     * @return <code>true</code> if and only if <code>this</code> expression is
     * either terminal or an "And" operator
     */
    @Override
    default boolean isAndForm( ) {
        return true ;
    }
    
    /**
     * @return true if and only if <code>this</code> operator is formed solely by terminals
     * other than the boolean constants
     */
    @Override
    default boolean isNormalAndForm( ) {
        return LogicalExprs.simple(getArgs( ));
    }
    
    
    @Override
    default LogicalExpr getZero() {
        return getFalse();
    }
    
    //la "distributività" potrebbe essere considerata come proprietà generale degli operatori sfruttando isDistributive    
    @Override
    default E genSimplify ( ) {
        //System.out.println("(AndOp) " + this);
        E res = N_aryOp.super.genSimplify(); // super-type method
        //System.out.println("(AndOp) -->\n" + res);
        if (res instanceof AndOp) { 
            E False = getFalse().cast();
            AndOp<E> aop = (AndOp<E>) res;
            Collection<E> args = aop.getArgs();
            if ( args. contains(False) || ! type().equals( FunctionTuple.class) && checkComplementary(args))
                return False;
            
            /*OrOp<E> nested =  (OrOp<E>) Util.find(args, OrOp.class);
            if ( nested != null )
                res = aop.distribute( nested );*/
            res = aop.distribute(OrOp.class);
        }
        //System.out.println("(AndOp) ->\n" + res); //debug
        return res;
    }
    
    // (e1 + e2) * e3 = e1 * e3 + e2 * e3
    @Override
    default boolean isDistributive(Class<? extends MultiArgs> optk) {
        return OrOp.class.isAssignableFrom(optk);
    }
    
}
