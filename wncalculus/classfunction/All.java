package classfunction;

import java.util.*;
import color.ColorClass;
import expr.Interval;

/**
 * this class defines diffusion class-functions (denoted by S)
 * @author lorenzo capra
 */
public final class All extends ConstantFunction  {
  
    private static final Map<ColorClass,All> VALUES = new HashMap<>();
    
    private final static String Symbol = "S";
     
    /**
     * creates a class diffusion function with an associated default color constraint
     */
    private All(ColorClass cc) {
        super(cc);
    }
    
    /**
     *
     * @param cc a colour class 
     * @return build an <tt>All</tt> constant of given colour
     */
    public static final All getInstance(ColorClass cc) {
        All f;
        if ((f = VALUES.get(cc)) == null) 
            VALUES.put(cc, f = new All(cc)) ;
        
        return f;
    }
        
    /**
     * @return the constraint associated with the color class
     */
    @Override
    public Interval card() {
        return getConstraint();
    }
    
    /**
     * sets a new constraint for <tt>this</tt> function
     * @param newconstr the new constraint
     * @return a copy of <tt>this</tt> with the new constraint
     */
    public ClassFunction setConstraint(Interval[] newconstr) {
        return getInstance(getSort().setConstraint(newconstr));
    }
        
    /**
     *
     * @return the description of <tt>All</tt>
     */
    @Override
    public String toString() {
        return Symbol + '_'+ getSort().name();
    }

    @Override
    public boolean isTrue() {
        return true;
    }

    @Override
    public SetFunction copy(ColorClass newcc) {
        return getInstance(newcc);
    }

    @Override
    public boolean elementary() {
        return false;
    }

}
