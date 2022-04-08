package wncalculus.expr;

/**
 * this interface is the super-type for unary (sorted) operators,
 * in which the operand and operator's types may be different
 * @author lorenzo capra
 */
public interface SingleArg<E extends ParametricExpr, F extends ParametricExpr> extends NonTerminal {
    
    /**
     * @return the operator argument
     */
    E getArg();
    
    @Override
    default F genSimplify() {
        //System.out.println("\nunary op genSimplify of " + this+':'+getClass()); //debug
        E s_arg = getArg().normalize(). cast(); 
        // System.out.println("arg simplified:\n" + s_arg); //debug
        return s_arg != getArg() ? buildOp(s_arg) : cast();
    }
     
    /**
      * checks the equality between <code>this</code> unary operator and
      * another (assumed not <code>null</code>), based on their operands
      * @param other a unary op
      * @return <code>true</code> if and only if the two operators are equal
      */
    default boolean isEqual (Object other) {
          return other == this || other != null && other.getClass().equals(getClass()) && getArg().equals(((SingleArg)other).getArg()) ;
     }
   
     
    /**
     * gives a infix description for a unary operator
     * @return its corresponding String
     */
    default String toStringOp () {
        return symb() + '(' + getArg() + ')';
    }
        
     /**
     @return a postfix representation of the operator
     */
    default String toStringPost() {
        return "(" + getArg() + ')' + symb();
    }
    
    /**
     * clone <tt>this</tt> unary op assuming that the co-domains of the operand and
     * of the operator are the same (to be overridden otherwise)
     * @param newdom the new domain
     * @param newcd the new codomain
     * @param smap the map between old and new split sorts
     * @return a clone of <tt>this</tt> with the specified co-domain
     */
    @Override
    default F clone (final Domain newdom, final Domain newcd) {
        return buildOp(getArg().clone(newdom, newcd). cast());
    }
    
    
    /**
     * builds an operator like <code>this</code> 
     * @param arg  the specified operand
     * @return an operator like this with the specified operand  
     */
    F buildOp(E arg);
    
    @Override
    default Domain getDomain() {
        return getArg().getDomain();
    }
    
    @Override
    default Domain getCodomain() {
        return getArg().getCodomain();
    }
   
}
