package guard;

import java.util.HashMap;
import java.util.Map;
import expr.Domain;

/**
 * This class implements the "false" constant guard.
 * False instances are created as unique objects
 * @author Lorenzo Capra
 */
public final class False extends ConstantGuard  {
     
    private final static String SYMBOL ="false";
    
    /**
     *
     */
    public final static Map<Domain, False> VALUES = new HashMap<>();
    
     
     /**
     * creates a class diffusion function with an associated default color constraint
     */
    private False(Domain dom)  {
        super(dom);       
    }
    
    /**
     * build a False guard
     * @param dom the guard's domain
     * @return a False constant
     */
    public static final False getInstance(Domain dom) {
        False g;
        if ((g = VALUES.get(dom)) == null) 
            VALUES.put(dom, g = new False(dom)) ;
        
        return g;
    }

    @Override
    public String toString() {
         return SYMBOL;
    }

    @Override
    public False clone(Domain  new_dom)  {
        return getInstance(new_dom);
    }
   
    @Override
    public boolean isFalse() {
        return true;
    }
    
    @Override
    public  boolean differentFromZero() {
       return false ;
    }
    
}
