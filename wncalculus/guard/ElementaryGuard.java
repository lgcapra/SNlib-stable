package wncalculus.guard;

import java.util.HashSet;
import wncalculus.color.ColorClass;
import wncalculus.classfunction.*;
import wncalculus.expr.*;

/**
 * this class defines the super-type for WN elementary guards;
 * they are implemented as unmodifiable objects, through suitable
 * builder methods (no need of redefining Object.equals and Object.hashCode)
 * @author Lorenzo Capra
 */
public abstract class ElementaryGuard extends Guard implements SingleSortExpr  {

    private final Domain domain; //  it is explicitly indicated; otherwise it is inferred
    private final boolean sign;
    private final Projection arg1;
    private final ElementaryFunction arg2;
     
    /**
     * creates an elementary WN guard, throwing an IllegalDomain exception if the colors of the arguments
     * are different, and they are not included in the specified domain; the guard is then put into a "canonical" form;
     * @param op1 the first operand
     * @param op2 the second operand
     * @param opsign the operator's sign
     * @param dom the domain of the guard
     */
     public ElementaryGuard(final Projection op1, final ElementaryFunction op2, final boolean opsign, final Domain dom)  {
        this.arg1 = op1;
        this.arg2 = op2;
        this.sign = opsign;
        this.domain = dom;
        super.setSimplified(true);
     }
     
     /**
      * check for the colours of the operands
      * @param op1 the 1st operand
      * @param op2 the 2nd operand
      * @param dom the guard's domain
      * @return the guard's colour
      * @throws IllegalDomain if the elementary guard's members must do not have the same color
      * or their color doesn't belong to the domain
      */
     protected static ColorClass checkDomain (final Projection op1, final ElementaryFunction op2, final Domain dom) {
          ColorClass cc = op1.getSort();
          if (! cc.equals(op2.getSort()) ) //
            throw new IllegalDomain("elementary guard's members must have the same color!");
        
        if (! dom.support().contains(cc) ) //if dom is an HashSet remove the constructor
            throw new IllegalDomain("the guard's color-class must belong to the domain:\ncc: "+cc+", dom: "+dom);
        
        return cc;
     }
     
    /**
     * checks for the index of a variable w.r.t. a domain
     * @param f a variable (i.e., a projection)
     * @param dom a color domain
     * @return the reference to the variable
     * @throws IllegalDomain if the variable's index doesn't match the domain
     */
    protected static Projection checkIndex(Projection f,  Domain dom) {
         if (f.getIndex() > dom.mult(f.getSort()) ) 
            throw new IllegalDomain("projection index ("+f.getIndex()+") beyond the color ("+f.getSort()+") domain's ("+dom+")  multiplicity!");
         
         return f;
     }
         
     
     /** 
     * @return the first operand
     */
    public final Projection getArg1() {
        return this.arg1;
    }
    
    /** 
     * @return the second operand
     */
    public ElementaryFunction getArg2() {
        return this.arg2;
    }
    
    /** 
     * @return the operator's sign
     */
    public final Boolean sign() {
        return this.sign;
    }
    
     /** 
     * @return the domain
     */
     @Override
     public final Domain getDomain() {
         return this.domain;
     }
    
    /** 
     * @return the color-class
     */
    @Override
    public final ColorClass getSort() {
        return this.arg1.getSort();
      }

    
    /**
     * @return the comparison operator symbol
     */
    public abstract String opSymb();
    
    
    /**
     * does a symbol replacement in {this} guard, according to the specified equality
     * @param eq an equality
     * @return the guard obtained from @code {this} by replacing symbols according to
     * a given equality
     */
    public abstract Guard replace(Equality eq) ;
    
    
    abstract Guard copy(ColorClass cc, Domain newdom) ;    
    
     @Override
    public final Guard clone(Domain newdom, Domain newcd) {
       Sort cc = getSort();
       
       return newdom.mult(cc) != 0 ? clone(newdom) : copy( (ColorClass) newdom.sort(cc.name()), newdom); //we first check if color cc is present in newdom
    }
    
    /** 
     * "translates" this elementary guard to a corresponding intersection form
     * assuming that the guard is associated to the specified variable 
     * @param f a variable (i.e., a projection)
     * @return an intersection corresponding to <code>f[this]</code>;
     * <code>null</code> if <code>f</code> doesn' appear in <code>[this]</code>;
     */
    public abstract Intersection toSetfunction(Projection f);
    
    /** 
     * "translates" this elementary guard to a corresponding intersection form
     * assuming that the guard is associated to specified intersection form
     * @param in an intersection form
     * @return an intersection corresponding to <code>in[this]</code>;
     * <code>null</code> if no operand of <code>in</code> appears in <code>[this]</code>;
     */
    public SetFunction toSetfunction(Intersection in) {
        for (SetFunction f : in.getArgs()) {
            Intersection equiv;
            if (f instanceof Projection && (equiv = toSetfunction((Projection) f)) != null) {
                HashSet<SetFunction> equivargs = new HashSet<>(in.getArgs());
                equivargs.addAll(equiv.getArgs());
                
                return Intersection.factory(equivargs);
            }
        }
        
        return null;
    }
    
    /**
     * calculates the expression corresponding to <tt>this</tt> guard, which
     * is assumed to be associated to a given class-function
     * @param f a class-function mapping to a set
     * @return the function corresponding to <tt>this</tt> guard, associated with @param f
     */
    public SetFunction toSetfunction(SetFunction f) {
        if (f instanceof Projection)
            return toSetfunction((Projection)f);
        
        if (f instanceof Intersection)
            return toSetfunction((Intersection)f);
        
        return null;
    }
    
    /** 
     * @return the opposite of <code>this</code> guard, e.g., X_1 == X_2 &rarr; X_1 != X_2
     */
    public abstract Guard opposite ();
    
    
    /** 
     * @return  the index of the first argument 
     */
    public final Integer firstIndex() {
        return this.arg1.getIndex();
    } 
    
    
    @Override
    public final boolean isElemAndForm() {
        return true;
    }
    
}
