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
    private static final Map<ComplexKey, Subcl> VALUES = new HashMap<>();
    
    /**creates a subclass (diffusion) function
     * @param i the subclass index
     * @param cc the color class
     * @throws IllegalArgumentException if the subclass index is out of bounds
     */
    private Subcl(final int i, final ColorClass cc)  {
        super(cc);
        this.index = i;
    }
    
    /**
     * build a subclass (diffusion) function
     * @param i the subclass index
     * @param cc the color class
     * @return a diffusion function
     * @throws IllegalArgumentException if, for any reasons, some of the arguments is incorrect 
     */
    public static Subcl factory (int i, ColorClass cc) {
        if (!cc.isSplit()) 
            throw new IllegalArgumentException("cannot build a sublcass of an ordered or unsplit class\n");
                
        if (i < 1 || i > cc.subclasses() )
            throw new IllegalArgumentException("incorrect subclass index");
        
        Subcl s;
        ComplexKey k = new ComplexKey(cc, i);
        if ( (s = VALUES.get(k)) == null) 
            VALUES.put(k, s = new Subcl(i, cc)) ;
        
        return s;
    }   
        
    /**
     *
     * @return the subclass index
     */
    public int index() {
        return this.index;
    }
    
    
   /**
     * @return the interval associated with the sublclass;
 if the subclass is an union, the sum of corresponding intervals
 if it is ordered, the fixedSize of the corresponding sub-interval
     */
    @Override
    public Interval card() {
        return getSort().getConstraint( this.index );
    }
    
    
    @Override
    public int splitDelim () { //new (da controllare)
        return card().lb() == 1  && card().ub() != 1 ? 1 : super.splitDelim(); //could be optimized?
    }
    public int splitDelimV0 () { //new (da controllare)
        ColorClass cc = getSort(); 
        return card().lb() == 1  && card().ub() != 1 ? cc.lb() : super.splitDelim();
    }
    
    /**
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
        return "S_"+ getSort().name() + "{"+this.index + "}";
    }
    
    @Override
    public Subcl copy(ColorClass newcc) {
        return Subcl.factory(this.index, newcc);
    }
    
}
 