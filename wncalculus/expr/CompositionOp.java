package expr;

import java.util.ArrayList;
import java.util.Collection;

/**
 * this interface defines a composition operator between (sorted) terms
 * the type of composition operator may be different from the operands'type
 * (e.g., a composition of set-type expressions meant as bag)
 * @author Lorenzo Capra
 * @param <E>  operand type
 * @param <F>  operand type
 */
public interface CompositionOp<E extends ParametricExpr, F extends ParametricExpr> extends TwoArgs<E,F> {
   
    final static String SYMB = /*"\u25CB"*/ " . ";

    
    /**
     * @param optk an operator type
     * @return <code>true</code> if and only if this composition is left-associative
     * w.r.t. the specified operator*/
    boolean isLeftAssociative (Class<? extends SingleArg> optk);
    
    
    @Override
    default F genSimplify () {
        F res = TwoArgs.super.genSimplify();
        if (res instanceof CompositionOp cop) {
            //Class<E> type = left().type(); //the type of operands
            E left = cop.left().cast(), right = cop.right().cast();
            SingleArg<E,F> uop;
            if (left instanceof UnaryOp && isLeftAssociative((uop = (SingleArg) left).getClass()) ) 
                return  uop.buildOp( buildOp( uop.getArg(), right).cast());
            
            MultiArgs<E,F> op;
            if (left instanceof MultiArgs && isDistributive( (op = (MultiArgs) left).getClass()) ) {
                Collection<E> args = new ArrayList<>();
                for (E e : op. getArgs())
                    args.add( buildOp(e, right).cast());
                
                return  op.buildOp(args);
            }

            if (right instanceof N_aryOp && isDistributive((op =( N_aryOp) right ). getClass()) ) {
                Collection<E> args = new ArrayList<>();
                for (E e : op. getArgs()) 
                    args.add(buildOp(left, e).cast());
                
                res =   op.buildOp(args);
            }
        }
    
        return res;
    }
    
    @Override
    default Domain getDomain() {
        return right().getDomain();
    }
    
    @Override
    default Domain getCodomain() {
        return left().getCodomain();
    }
        
    
    @Override
    default String symb() {
        return SYMB;
    }
       
 }
