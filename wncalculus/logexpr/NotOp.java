package logexpr;

import java.util.Collection;
import java.util.HashSet;
import expr.N_aryOp;
import expr.UnaryOp;

/**
 * this interface defines the super-type of boolean operators that can be viewed as "negations"
 * @author lorenzo capra
 * @param <E> the operand's type
 */
public interface NotOp<E extends LogicalExpr> extends UnaryOp<E>, LogicalExpr {

    @Override
    E getArg();
    
    @Override
    default boolean isInvolution() {
        return true;
    }
    
    /*@Override
    default Pair<Boolean, OpBuilder<>> isDistributive (Class<? extends  N_aryOp> optk) {
        OpBuilder b = null;
        boolean distrib;
        if (distrib = OrOp.class.isAssignableFrom(optk)) 
           b = (Collection<? extends Expression> args) -> andFactory(Util.cast(args, LogicalExpr.class)) ;   
        else if (distrib = AndOp.class.isAssignableFrom(optk)) 
            b = (Collection<? extends Expression> args) -> orFactory(Util.cast(args, LogicalExpr.class),false) ;
        
        return  new Pair<>(distrib, b);
    }*/
    
    @Override
    default E genSimplify () {
        E res =  UnaryOp.super.genSimplify(); // super-type method
        if (res instanceof NotOp) {
            LogicalExpr arg = ((NotOp<?>) res).getArg();
            if (arg.isTrue()) 
               res =  getFalse().cast();
            else if (arg.isFalse()) 
               res =  getTrue().cast();
            else if (arg instanceof AndOp || arg instanceof OrOp) {
                Collection<E> nargs = new HashSet<>();
                ((N_aryOp<E>) arg). getArgs().forEach( e -> { nargs.add( buildOp(e).cast() );  });//casts Expression to E
                res = (arg instanceof AndOp ? orFactory(nargs, false) : andFactory(nargs)).cast();   
            }
        }   
        //System.out.println("(NotOp) -->\n"+res); //debug
        return res;
    }
    
    @Override
    default boolean isFalse() {
        return getArg().isTrue();
    }

    @Override
    default boolean isTrue() {
       return getArg().isFalse();
    }
    

}