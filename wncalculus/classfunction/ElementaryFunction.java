package classfunction;

import java.util.Map;
import color.ColorClass;
import expr.Sort;
import wnbag.LinearComb;

/**
 * this abstract class is the super type of elementary SN functions (including the empty function);
 * each elementary function is unmodifiable and simplified just after its creation
 * it is provided with a suitable builder method ensuring that 
 * elementary function instances are unique - there is no need to redefine
 * <code>Object.equals</code> and <code>Object.hashCode</code>
 * @author lorenzo capra
 */
public abstract class ElementaryFunction extends SetFunction  {  
    
    private final ColorClass cc;  
    
    /**
     * build an elementary class-function
     * @param cc the function's color-class
     */
    protected ElementaryFunction (final ColorClass cc) {
        super(true); //already simplified
        this.cc = cc;
    }
    
    @Override
    public final ColorClass getSort() {
        return this.cc;
    }
    
    /**
     * clones <code>this</code> elementary function by assigning a new color class
     * @param split_map a map between old and new color classes
     * @return a clone of <code>this</code> (<code>this</code> if the mapping is <code>null</code>)
     */
    @Override
    public ElementaryFunction clone (final Map<Sort, Sort> split_map) {
        final  ColorClass n_cc = (ColorClass) split_map.get(cc);
        return n_cc == null? this : copy(n_cc);
    } 
    
    @Override
    public final void setSimplified(final boolean simp) { }
    
   
   @Override
   public final LinearComb asBag () {
       return new LinearComb(this, 1);
   }

}
