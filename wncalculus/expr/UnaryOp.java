package expr;

import java.util.ArrayList;
import java.util.Collection;

/**
 * this interface is the super-type for unary (sorted) operators
 * in which the operand and operator's types coincide
 * @author Lorenzo Capra
 * @param <E> the operand's type
 */
public interface UnaryOp<E extends ParametricExpr>  extends SingleArg<E,E> {

    
    /**
     * @return <code>true</code> if and only if op(op(x)) = x
     * CAREFUL: default implementation - to be overridden if needed
     */
    boolean isInvolution() ;
         
   /**
     * checks whether <code>this</code> unary operator can be distributed over a n-ary operation
     * @param optk a n-ary operator's type
     * @return <code>true</code> if and onll if <code>this</code> can be distibuted over the specified operator type
     */
    default boolean isDistributive (Class<? extends MultiArgs> optk) {
        return false;
    }
         
    @Override
    default E genSimplify() {
        //System.out.println("\nunary op genSimplify of " + this+':'+getClass()); //debug
        E res = SingleArg.super.genSimplify();
        if (res == this) {
            E arg = getArg();
            if (isInvolution() && getClass().equals(arg.getClass())  )
                res = ((SingleArg<E,E>)arg).getArg() ;
            else {
                MultiArgs<E,E> op;
                if (arg instanceof MultiArgs && isDistributive( (op= ((MultiArgs)arg )).getClass() ) ) {
                    Collection<E> nargs = new ArrayList<>();
                    op.getArgs().forEach( e -> { nargs.add( buildOp(e).cast() );  });//casts Expression to E
                    res =  op.buildOp(nargs);
                }
            }
        }
        
        return res;
    }
    
}
