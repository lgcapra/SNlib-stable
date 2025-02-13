package expr;

import java.util.Map;

/**
 * this interface is the super-type for unary (sorted) operators,
 * in which the operand and operator's types may be different
 * @author lorenzo capra
 * @param <E> operand type
 * @param <F> operator type
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
    
    
    @Override
    default F clone (final Domain newdom) {
        return buildOp(getArg().clone(newdom). cast());
    }
    
    @Override
    default F clone (final Map<Sort, Sort> split_map) {
        return buildOp(getArg().clone(split_map). cast());
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
