package expr;

import java.util.*;
import util.Util;

/**
 * This interface represents any operator with more than one operands, of the same type.
 * The operator and operandd's type may be different (e.g, the operands sets and the operator bag)
 * @author lorenzo capra
 * @param <E> the type of operands
 * @param <F> the operator's type
 */
public interface MultiArgs<E extends ParametricExpr, F extends ParametricExpr> extends NonTerminal {
    
    /**
     * @return the (unmodifiable) collection of arguments of this operator
     * @throws UnmodifiableCollectionException
     */
     Collection<? extends E> getArgs();
     
    /**
     * (to be overridden if needed)
     * checks whether <code>this</code> operator can be distributed over another a n-ary operation
     * @param optk a n-ary operator's type
     * @return <tt>true</tt> if and only if <tt>this</tt> can be distibuted over the specified operator type
     */
     default boolean isDistributive (Class<? extends MultiArgs > optk) {
         return false;
     }
     
    @Override
    default F clone (final Domain newdom) {
         return buildOp(clone(getArgs(), newdom, type()));
    }
    
     @Override
    default F clone (final  Map<Sort, Sort> split_map) {
        return buildOp(clone(getArgs(), split_map, type()));
    }
    
    /**
     * creates a new list of expressions of an arbitrary type setting a new sort support
     * @param <E> the expressions domain
     * @param arglist the list of expressions to be cloned
     * @param s_map a map between old and new sorts
     * @param type the expressions'  type
     * @param dom the expressions' new domain
     * @param cd the expressions' new codomain
     * @return a copy of the passed list of expressions with the specified new sorts and co-domains
     */
    public static <E extends ParametricExpr> Collection<E> clone(Collection<? extends E> arglist, Domain dom, Class<E> type) {
        Collection<E> res = arglist instanceof List ? new ArrayList<>() : new HashSet<>();
        arglist.forEach((var f) -> { res.add(type.cast(f.clone(dom))); });
        return res;
    }
    
    public static <E extends ParametricExpr> Collection<E> clone(Collection<? extends E> arglist, final  Map<Sort, Sort> split_map, Class<E> type) {
        Collection<E> res = arglist instanceof List ? new ArrayList<>() : new HashSet<>();
        arglist.forEach((var f) -> { res.add(type.cast(f.clone(split_map))); });
        return res;
    }
    
     /**
      * builds an operator from a given collection of operands
      * @param args the operand list
      * @return an operator built from the operand list
      * @throws ClassCastException if the type of any operands is not the right one
     */
     F buildOp(Collection<? extends E> args);
     
     /**
      * builds an operator from a given collection of operands
      * @param args the operand list
      * @return an operator built from the operand list
      * @throws ClassCastException if the type of any operands is not the right one
     */
     default F buildOp(E ... args) {
         return buildOp(Arrays.asList(args));
     }
     
             
    /**
     * generically simplifies a n-ary operator
     * if the operands are a set use a set
     * associativity. idempotent, identity properties are possibly applied
     * (if the operator is marked "idempotent" then duplicates are erased)
     * @return an equivalent simplified term
     * */
     @Override
     default F genSimplify ( ) {
        //System.out.println("NaryOpOp.gensimplify\n"+this.toStringDetailed());//debug*/
        Collection<E> argscopy = Util.copy( getArgs() );
        boolean changed = Expressions.normalize(argscopy) ;
       //System.out.println("(NaryOpOp) --->\n"+argscopy+norm);//debug*/
        return changed ?  buildOp(argscopy) : cast() ;
    }
     
     /**
      * checks the equality between <code>thsi</code> n-ary operator and
      * another (assumed not <code>null</code>), based on the particular collection
      * implementing the operands
      * @param other another operator
      * @return <code>true</code> if and only if two terms represent the same
      * n-ary operator
      */
     default boolean isEqual (Object other) {
          return this == other || other != null && getClass().equals(other.getClass()) && getArgs().equals(((MultiArgs)other).getArgs() ) ;   
     }
     
     
     /**
     * gives a textual description for a n-ary operator
     * @return the corresponding String
     */
     default String toStringOp()  {
        String opsymb = symb();
        StringBuilder res = new StringBuilder("(");
        getArgs().forEach(x -> { res.append(x).append(opsymb); });
        res.setCharAt(res.length() - 1, ')');
        int length = res.length();
        res.replace(length - opsymb.length(), length, ")");
        
        return res.toString();
    }
     
     @Override
     default Domain getDomain(){
         return getArgs().iterator().next().getDomain();
     }
     
     @Override
     default Domain getCodomain(){
         return getArgs().iterator().next().getCodomain();
     }
}
