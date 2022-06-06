package wncalculus.expr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * this interface defines a composition operator between (sorted) terms
 * the type of composition operator may be different from the operands'type
 * (e.g., a composition of set-type expressions meant as bag)
 * @author Lorenzo Capra
 * @param <E> the operands type
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
        if (res instanceof CompositionOp) {
            Class<E> type = left().type(); //the type of operands
            CompositionOp<?,?> cop = (CompositionOp<?,?>) res;
            E left = type.cast(cop.left()), right = type.cast(cop.right());
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
    
    /**
     * overrides the ancestor method, taking into account the fact that the
     * left operands' domain has to match the right operand's codomain
     * @param newdom the composition's new domain
     * @param newcd the composition's new co-domain
     * @param smap the map between orginal and split sorts
     * @return a clone of <tt>this</tt> composition with the new co-domains
     */
    @Override
    default F clone (final Domain newdom, final Domain newcd) {
        E left = left(), right = right();
        Domain left_dom = left.getDomain(); // the left operands' domain = right operand's codom
        if (left.getCodomain().equals(left_dom)) //optimization
            left_dom = newcd;
        else if (left_dom.equals(right.getDomain()))
            left_dom = newdom;
        else { //we re-build the left's domain
            HashMap<Sort,Integer > copy = new HashMap<>();
            left_dom.asMap().entrySet().forEach (e -> {
                Sort s = e.getKey(), ns;
                boolean not_found = newdom.mult(s) == 0 && newcd.mult(s) == 0; // the old getSort of the left's domain is not present in the new (co-)domain
                if (not_found && ( (ns = newdom.getSort(s.name() )) != null || (ns = newcd.getSort(s.name() )) != null) ) // in the new (co-)domain there is a getSort with that name
                    s = ns;
                copy.put(s, e.getValue());
            });

            left_dom  =  new Domain(copy);
        }
        
        return buildOp(left().clone(left_dom, newcd). cast(), right().clone(newdom, left_dom). cast());
    }
    
    @Override
    default String symb() {
        return SYMB;
    }
       
 }
