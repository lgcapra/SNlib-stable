package guard;

import java.util.*;
import logexpr.NotOp;
import expr.Domain;
import expr.Sort;

/**
 * this class represent the boolean negation operator
 * @author Lorenzo Capra
 */
public final class Neg extends Guard implements NotOp<Guard> {

     private final Guard arg;
   
     private Neg (Guard g) {
        this.arg = g;
     }
     
    /**
     * build a <tt>Neg</tt> operator from a given guard; if the guard is elementary,
     * results in its opposite
     * @param g a guard
     * @return the <tt>Neg</tt> of the given guard; if the guard is elementary,
     * its opposite
     */
    public static Guard factory(Guard g) {
         return g instanceof ElementaryGuard ? ((ElementaryGuard)g).opposite() : new Neg(g);
     }

     @Override
    public Guard buildOp(Guard arg) {
        return factory (arg);
    }

    @Override
    public Guard getArg() {
        return this.arg;
    }

    @Override
    public String symb() {
        return "not";
    }
    

    @Override
    public boolean equals (Object o) {
        return NotOp.super.isEqual(o);
    }

    
    @Override
     public int hashCode() {
         return Objects.hashCode(this.arg) + 11;
     }
    

    @Override
    public Map<Sort, Integer> splitDelimiters() { 
        return this.arg.splitDelimiters();
    }
    
    @Override
    public Neg clone(Domain  new_dom)  {
        return new_dom.equals(getDomain()) ? this : new Neg(this.arg.clone(new_dom));
    }

    @Override
    public final Set<Integer> indexSet() { 
         return this.arg.indexSet();
     }
    
    @Override
    public final String toString() {
        return NotOp.super.toStringOp();
    }
    
}
