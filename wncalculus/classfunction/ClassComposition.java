package wncalculus.classfunction;

import java.util.*;
import wncalculus.expr.*;
import wncalculus.color.ColorClass;
import wncalculus.logexpr.LogComposition;

/**
 *
 * @author Lorenzo Capra
 * this class defines basic compositions between ClassFunctions
 * the left function must be assumed unary
 */
public final class ClassComposition extends SetFunction implements LogComposition<SetFunction> {
    
   private final SetFunction left, right;
    
    /** creates a new basic-composition between class-functions after having possibly checked that the left one is unary
     * @param left the left operand
     * @param right the right operand
     * @param check the operands "check" flag (same color-class and not multi-index left)
     * @throws IllegalDomain IllegalArgumentException
     */
    public ClassComposition (SetFunction left, SetFunction  right, boolean check) {
        if (check) {
            if (!left.getSort().equals(right.getSort())) 
                throw new IllegalDomain(left+" ("+left.getSort()+") "+right+" ("+right.getSort()+")");

            if (left.indexSet().size() > 1) 
                throw new IllegalArgumentException("the left function must hold at most one index!");
        }
        
        this.left  = left;
        this.right = right;
    }
    
    /** creates a new basic-composition between class-functions assuming that the left one is unary
     * @param left left-composed function
     * @param right right-composed function
     */
    public ClassComposition (SetFunction left, SetFunction  right)  {
        this(left, right, false);
    }
    
    @Override
    public ClassComposition buildOp(SetFunction left, SetFunction right)  {
        return new ClassComposition(left, right);
    }
    
    @Override
    public ColorClass getSort() {        
        return this.left.getSort();
    }    
    
    //the following two redefinitions are only needed because the methods are doubly inherithed from two interfaces
   
    @Override
    public boolean equals (Object o) {
        return LogComposition.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.left);
        hash = 59 * hash + Objects.hashCode(this.right);
        return hash;
    }

    @Override
    public Interval card() {
        return null;
    }

    @Override
    public SetFunction specSimplify() {
        SetFunction compres;
        Interval rcard = this.right.card(); 
        if (rcard != null && rcard.ub() == 0) 
            compres = Empty.getInstance(getSort());
        else if ( (compres = this.left.baseCompose(this.right) ) == null) 
            compres = this;
        
        return compres;
    }

    
   /*@Override
    public SetFunction clone (final Domain newdom, final Domain newcd) {
        return  (SetFunction) super.clone(newdom, newcd);
    }*/

    @Override
    public SetFunction left() {
        return this.left;
    }

    @Override
    public SetFunction right() {
        return this.right;
    }
    
    @Override
    public boolean isLeftAssociative(Class<? extends SingleArg> optk) {
        return optk.equals(Successor.class) ; 
    }

    @Override
    public Set<Integer> indexSet() {
        return this.right.indexSet();
    }
    
    /** 
     * @return the split delimiter of <code>this</code> function 
     * optimized version: it avoids unnecessary split when the composition
     * result may be inferred
     */
    @Override
    public final int splitDelim () {
        int delim = 0;
        if (this.left.isConstant()) 
            delim = this.right.splitDelim();
        else { // left is injective ...
            Interval rightcard;
            int lb  =  getConstraint().lb() , left_gap =  this.left.gap(), right_lb;
            if ( left_gap < 0 || (rightcard = this.right.card()) == null || rightcard.singleValue() )   // intuitively: left or right's card cannot be computed or the right card is constant...
                delim =  ColorClass.minSplitDelimiter(this.left.splitDelim() , this.right.splitDelim(), getSort()); // no optimization..
            else if ( left_gap > 0 && ( right_lb = rightcard.lb() ) > 0 && right_lb  <= left_gap)  //optimization
               delim = lb - right_lb + left_gap   ;
        }
        //System.out.println("delim: "+delim); //debug
        return delim; // avoids unnecessary splits ... we argued that the composition res. is S or 0 
    }
    
  
    @Override
    public ClassFunction setDefaultIndex( ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String toString() {
        return LogComposition.super.toStringOp();
    }

    @Override
    public SetFunction copy(ColorClass newcc) {
        return new ClassComposition(this.left.copy(newcc), this.right.copy(newcc));
    }
    
    //needed because it inherits two default methods
    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return super.splitDelimiters();
    }

}
