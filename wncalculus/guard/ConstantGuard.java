package wncalculus.guard;

import java.util.*;
import wncalculus.expr.Domain;
import wncalculus.expr.ParametricExpr;
import wncalculus.expr.Sort;

/**
 * the super-class of constant guards; note that for their particular
 * meaning constant guards are not {@link ElementaryGuard}s
 * @author Lorenzo
 */
public abstract class ConstantGuard extends Guard  {
    
    private Domain domain; //  it must be explicitly indicated
    /**
     * creates a log. constant with a given support
     * @param dom the color domain
     */
    public ConstantGuard (Domain dom)  {
        if (dom == null) 
            throw new IllegalArgumentException("the domain cannot be null!");
        
        this.domain = dom;
        super.setSimplified(true);
    }
    
    /**
     * the method is overriden with a stub implementation 
     * @param simp the simplified flag (here ignored)
     */
     @Override
     public final void setSimplified(boolean simp) {}
     
    @Override
     public final boolean isConstant () {
         return true;
     }
    
    @Override
     public final Domain getDomain() {
         return this.domain;
     }
    
    @Override
    final public Map<Sort,Integer> splitDelimiters () {
        return new HashMap<>();
    }
    
   @Override
   public final Set<Integer> indexSet() {
       return Collections.EMPTY_SET;
   }
   
    /**
     *
     * @param newdom
     * @return
     */
    public abstract Guard clone(Domain newdom);
    
     @Override
    public ParametricExpr clone(Domain newdom, Domain newcd) {
       return clone(newdom);    
    }
    
}
