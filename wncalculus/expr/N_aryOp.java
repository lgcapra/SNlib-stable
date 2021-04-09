package wncalculus.expr;

import java.util.*;
import wncalculus.util.Util;

/**
 * This interface defines the ADT for n-ary (n greater than 1) associative operator
 * in which the operands and operator'a types coincide..
 * @author Lorenzo Capra
 * @param <E> the type of operands/operator
 */
public interface N_aryOp<E extends ParametricExpr> extends MultiArgs<E,E>  {
    
     /**
      * @return the identity for this operator; <code>null</code> if there is no identity
      */
     E getIde();
             
    /**
     * generically simplifies a n-ary operator
     * if the operands are a set use a set
     * associativity. idempotent, identity properties are possibly applied
     * (if the operator is marked "idempotent" then duplicates are erased)
     * @return an equivalent simplified term
     * */
     @Override
     default E genSimplify ( ) {
        //System.out.println("NaryOpOp.gensimplify\n"+this.toStringDetailed());//debug*/
        Collection<E> argscopy = associate();
        boolean changed = argscopy != null;
        if (! changed )      
           argscopy = Util.copy( getArgs() );
        if (Expressions.normalize(argscopy) || argscopy.size() > 1 && argscopy.removeAll(Collections.singleton(getIde()) ) )
            changed = true;
        //System.out.println("(NaryOpOp) --->\n"+argscopy+norm);//debug*/
        return changed ? argscopy.isEmpty() ? getIde() : buildOp(argscopy) : cast() ;
    }
    
              
     /**
      * distribute <code>this</code> operator over a nested one of a given type
      * (it works also if nested is not actually present in the operands)
      * performs just one step, i.e., considers just the first occurrence (if any)
      * of the nested operator
      * @param nested the (assumed nested) operation over which <code>this</code> operation is distributed
      * @return the distribution of  <code>this</code> over a nested operation
      * (the returned object has the same type as the nested one);
      * or <code>this</code> if no match is found
      * This implementation uses sets.
      */
    default E distribute( N_aryOp<E> nested ) {
        Collection<E> new_arg_set = new LinkedHashSet<>();
        for (E term : nested.getArgs() ) {
            HashSet<E> iset = new HashSet<>( getArgs() );
            iset.remove(nested); // copy of args without nested (efficient if an hashset is used)
            iset.add(term);
            new_arg_set.add( buildOp(iset) );
        }

        return nested.buildOp(new_arg_set);
    }
    
       
    default E distribute ( Class<? extends N_aryOp> typenested ) {
        N_aryOp<E> nestedop1 = null, nestedop2 = null;
        Collection<E> args = getArgs();
        for (E e : args) 
            if (typenested.isInstance(e)) {
                if (nestedop1 == null)
                    nestedop1 = (N_aryOp<E>) e;
                else {
                    nestedop2 = (N_aryOp<E>) e;
                    break;
                }
            } 
        
        if (nestedop1 == null && nestedop2 == null)
            return cast();
        
        HashSet<E> tail = new HashSet<>(getArgs());
        tail.remove(nestedop1);
        if (nestedop2 != null) { //by the way, also nestedop1 is ..
            tail.remove(nestedop2);
            E prod = nestedop2.buildOp(Util.binaryProduct(nestedop1.getArgs(), nestedop2.getArgs(), this::buildOp));
            tail.add(prod);
            return buildOp(tail);
        }
        else {
            return nestedop1.buildOp(Util.binaryProduct(nestedop1.getArgs(), Collections.singleton(buildOp(tail)), this::buildOp));
        }
    }
    
    
    /**
     * applies the associative property to <tt>this</tt> n-ary operator, by searching
     * for occurrences of the same type of operator among its operands
     * @return a collection of operands resulting from applying the associative property
     * <tt>null</tt> if no reduction is done
     */
    default Collection<E> associate () { 
        Collection<? extends E> args = getArgs();
        List<N_aryOp<E>> nested = new ArrayList<>();
        args.stream().filter( expr ->  getClass().isInstance( expr ) ). forEachOrdered(expr -> {
            nested.add((N_aryOp) expr);
         });
        
        if ( nested.isEmpty() )
            return null;
        
        Collection<E> argscopy = Util.copy(args);
        argscopy.removeAll(nested);
        nested.forEach( op -> { argscopy.addAll(op.getArgs()); });
        
        return argscopy;
    }
              
}
