package guard;

import java.util.HashMap;
import java.util.Map;
import expr.Domain;

/**
 * This class implements the "true" constant guard.
 * True instances are created as unique objects
 * @author Lorenzo Capra
 */
public final class True extends ConstantGuard  {
    
    private final static Map<Domain,True> VALUES = new HashMap<>();
    
    private final static String SYMBOL ="true";

    /**
     * creates a class diffusion function with an associated default color constraint
     */
    private True(Domain dom) {
        super(dom);       
    }
    
    /**
     *
     * @param dom a colour-domain
     * @return a <tt>True</tt> constant of the given domain
     */
    public static final True getInstance(Domain dom) {
        True g;
        if ((g = VALUES.get(dom)) == null) 
            VALUES.put(dom, g = new True(dom)) ;
        
        return g;
    }
    
    @Override
    public boolean isTrivial() {
        return true;
    }
    
    @Override
    public String toString() {
        return SYMBOL;
    }

    @Override
    public True clone(Domain new_dom)  {
        return getInstance(new_dom);
    }

    @Override
    public boolean isTrue() {         
        return true;
    }
    
}
