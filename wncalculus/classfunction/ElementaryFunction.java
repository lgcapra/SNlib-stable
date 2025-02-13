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
 * <tt>Object.equals</tt> and <tt>Object.hashCode</tt>
 * @author lorenzo capra
 */
public abstract class ElementaryFunction extends SetFunction  {  
    
    private final ColorClass cc;  
    
    /**
     * build an elementary class-function
     * @param cc the function's color-class
     */
    protected ElementaryFunction (ColorClass cc) {
        super(true); //already simplified
        this.cc = cc;
    }
    
    @Override
    public final ColorClass getSort() {
        return this.cc;
    }
    
    
    /*
    @Override
    public final ElementaryFunction clone(Domain newdom, Domain newcd) {
        if (newdom.mult(cc) == 0 && newcd.mult(cc) == 0) { // the color-class is not present in the new (co-)domain             
            Sort ncc = newdom.getSort(cc.name());
            if (ncc != null || (ncc = newcd.getSort(cc.name())) != null)
                return copy((ColorClass) ncc);
            }
        
        return this;
    }
   */
    
    @Override
    public final ElementaryFunction clone (final Map<Sort, Sort> split_map) {
        Sort n_cc = split_map.get(cc);
        if (n_cc == null) {
            return this;
        }
        return copy((ColorClass)n_cc);
    } 
    
    @Override
    public void setSimplified(boolean simp) { }
    
   
   @Override
   public final  LinearComb asBag () {
	   return new LinearComb(this, 1);
   }
    
}
