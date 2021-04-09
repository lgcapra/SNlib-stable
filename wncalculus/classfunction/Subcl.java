package wncalculus.classfunction;

import java.util.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.Interval;
import wncalculus.util.*;

/**
 * this class defines sub-classes of a color class, denoted by C{i},
 * where index i refers to the color class partition's element (what is
 * usually referred to as "static subclass")
 * @author lorenzo capra
 * 
 */
public final class Subcl extends ConstantFunction {
 
    private final int index;
    private final Interval subinter; // new! defines a subset of contigous elements, in the case of an ordered subclass 
    
    private static final Map<ComplexKey, Subcl> VALUES = new HashMap<>();
    
    /**creates a possibly ordered subclass (diffusion) function
     * in the case of an ordered subclass an interval specifies the (sub)set of represented contigous values
     * @param i the subclass index
     * @param cc the color class
     * @param subint the subset of contigous values (in the case of an ordered subclass)
     * @throws IllegalArgumentException if the subclass index is out of bounds
     */
    private Subcl(final int i, final ColorClass cc, final Interval subint)  {
        super(cc);
        if (i < 1 || i > cc.subclasses() )
            throw new IllegalArgumentException("incorrect subclass index");
        
        this.index = i;
        this.subinter = subint;
    }
    
    /**
     * build a subclass (diffusion) function
     * @param i the subclass index
     * @param cc the color class
     * @param subint the subset of contigous values (in the case of an ordered subclass)
     * @return a diffusion function
     * @throws IllegalArgumentException if, for any reasons, some of the arguments is incorrect 
     */
    private static Subcl factory (int i, ColorClass cc, Interval subint) {
        if (!cc.isSplit()) 
            throw new IllegalArgumentException("cannot build a sublcass of an ordered or unsplit class\n");
        
        boolean ordered = cc.isOrdered(); 
        
        if (! (ordered || subint == null) )
            throw new IllegalArgumentException("unordered sublcass: the sub-interval of adjacent values must be null\n");
            
        // we check the sub-interval bounds; if null, we set the default subinterval, depending on whether the class is ordered or not
        int scsize = cc.getConstraint(i).lb(); // the subclass size
        if (subint != null) {
            if ( subint.ub() > scsize || subint.lb() < 1 )
                throw new IllegalArgumentException("inconsistent sub-interval bounds\n");
        }
        else if (ordered)
            subint = new Interval(1, scsize) ;
        
        Subcl s;
        ComplexKey k = new ComplexKey(cc, i, subint);
        if ( (s = VALUES.get(k)) == null) 
            VALUES.put(k, s = new Subcl(i, cc, subint)) ;
        
        return s;
    }
    
     /**
     * default builder method for a diffusion function
     * @param subcl the subclass index
     * @param cc the color-class
     * @return  a diffusion function
     * @throws IllegalArgumentException if, for any reasons, some of the arguments is incorrect 
     */

    public static Subcl factory (int subcl, ColorClass cc) {
        return factory (subcl, cc, null);
    }    
        
    /**
     *
     * @return the subclass index
     */
    public int index() {
        return this.index;
    }
    
    /**
     * 
     * @return the subclass sub-interval of contigous elements
     * (the empty interval if the color class is not ordered)
     */
    public Interval subInterval() {
        return this.subinter;
    }
   
    /**
     * 
     * @return <code>true</code> if and only if the color class is ordereds 
     */
   public boolean ordered () {
       return getSort().isOrdered();
   }
   /**
     * @return the interval associated with the sublclass;
     * if the subclass is an union, the sum of corresponding intervals
     * if it is ordered, the size of the corresponding sub-interval
     */
    @Override
    public Interval card() {
        if ( ordered() ) {
            int size = this.subinter.size();
            return new Interval(size, size);
        }
        
        return getSort().getConstraint( this.index );
    }
    
    
    /**
     * @return <code>true</code> if and only if this subclass is not parametric
     */
    boolean singleValue() {
        return card().singleValue() ;
    }
    
    @Override
    public int splitDelim () { //new (da controllare)
        ColorClass cc = getSort(); 
        
        return ( ! singleValue() && card().lb() == 1) ? cc.lb() : super.splitDelim();
    }
    
    // QUESTI DUE METODI SONO DA SISTEMARE
    
    /**
     * da sistemare nel caso di sottoclassi ordinate ...
     * @return the "complementary" subclass(es) of this subclass 
     */
    public SetFunction opposite () {
        ColorClass cc = getSort();
        int howmany = cc.subclasses();
        Set<SetFunction> complset = new HashSet<>();
        for (int i=1; i <= howmany; i++) 
            if (i != this.index) 
                complset.add( factory(i, cc));
        
        return Union.factory(complset, true) ; // disjoint union
    }
    
   
    @Override
    public String toString () {
        return "S_"+ getSort().name() + "{"+this.index + "}"+( ordered() ? this.subinter : "" );
    }
    
    @Override
    public Subcl copy(ColorClass newcc) {
        return Subcl.factory(this.index, newcc, this.subinter );
    }
    
}
 