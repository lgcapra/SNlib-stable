package wncalculus.expr;

import java.util.*;
import wncalculus.color.ColorClass;

/**
 *
 * @author lorenzo capra
 */
public interface TwoArgs<E extends ParametricExpr, F extends ParametricExpr> extends MultiArgs<E,F>{
    /**
     * @return the "left" operand */
    E left();
    
    /**
     * @return the "right" operand */
    E right();
    
    
    /**
     * @return <tt>true</tt> if and only <tt>this</tt> operator is commutative 
     */
    default boolean isCommutative() {
        return false;
    }
    
    /**
     * @return either a set or a list, depending on whether the operator is commutative
     * important because @see {isEqual} builds on it
     * 
     */
    @Override
    default Collection</*? extends*/ E> getArgs() {
        List<E> args = Arrays.asList(left(),right());
        return isCommutative() ? new HashSet<>(args) : args;
    }
    
    /**
     * generically simplifies a binary operator
     * @return an equivalent simplified term
     */
    @Override
    default F genSimplify () {
        Class<E> type = left().type();
        E left  = type.cast(left().normalize( )), right = type.cast(right().normalize( ));
        
        return left.equals(left()) && right.equals(right()) ? cast() : buildOp(left, right);
    }
    
    /**
      * checks the equality between <code>this</code>) and another binary operator
      * (assumed not <code>null</code>), based on their operands considered in order
      * @param other a binary op
      * @return <code>true</code> if and only if the operators are equal
      */
     /*default boolean equals (TwoArgs<?,?> other) {
          return other.getClass().equals(getClass())  && ( Objects.equals(left(),  other.left()  ) &&
                   Objects.equals(right(), other.right()) || isCommutative() &&  Objects.equals(left(),  other.right()  ) &&
                   Objects.equals(right(), other.left() ) );
     }*/
     
     /**
     * gives a textual description for a binary operator
     * @return its corresponding String
     */
    @Override
    default String toStringOp () {
        Object left = left(), right = right();
        String sleft = left.toString(), sright = right.toString();
        if ( left instanceof NonTerminal ) 
            sleft = "(" + sleft + ')';
        if (right instanceof NonTerminal) 
            sright = "(" + sright + ')';
        
        return sleft + " " + symb() + " " + sright;
    }
    
    /**
     * builder method
     * @param left the first operand
     * @param right the second operand
     * @return a binary operator of the same type as <code>this</code>
     */
    F buildOp (E left, E right);
    
    
    @Override
    default F buildOp(Collection<? extends E> args) {
        if (args.size() != 2)
            throw new IllegalArgumentException("a binary op requires two arguments");
        
        Iterator<? extends E> x = args.iterator();
        return buildOp(x.next(),x.next());
    }
    
     
    @Override
    default F clone (final Domain newdom) {
        return buildOp(left().clone(newdom).cast(), right().clone(newdom). cast());
    }
    
    
    @Override
    default F clone (final Map<Sort, Sort> split_map) {
        return buildOp(left().clone(split_map). cast(), right().clone(split_map). cast());
    }
    
    
    @Override
    default Map<Sort, Integer> splitDelimiters() {
        ParametricExpr [] args = {left(), right()};
        
        return  ColorClass.mergeSplitDelimiters(Arrays.asList(args));
    }
}
