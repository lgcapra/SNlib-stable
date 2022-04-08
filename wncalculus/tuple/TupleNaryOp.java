package wncalculus.tuple;

import java.util.*;
import wncalculus.expr.*;
import wncalculus.color.ColorClass;

/**
 * this class represent the super-type for tuple logical n-ary operators
 * @author Lorenzo Capra
 */
public abstract class TupleNaryOp  implements FunctionTuple , N_aryOp<FunctionTuple> {
  
    //ATTENZIONE: dato che gli operandi (tuple) NON vengono ordinati usiamo Set anzichè List
    private final Set</*? extends*/ FunctionTuple> args;//the operand's list
    
    private boolean simplified;
    
    /** build a n-ary tuple-operator from a (non empty) Set of tuples,
 possibly checking the size of tuples; an unmodifiable view of the Set is built
     * @throws NoSuchElementException if the collection is empty
     * @throws IllegalDomain if the check flag is set and tuples do not have a coherent size
    */
    TupleNaryOp(Set<? extends FunctionTuple> tuples, boolean check) {
         if (check)
            Expressions.checkArity(tuples);
        
        this.args = Collections.unmodifiableSet(tuples);
    }
    
    @Override
    public final Set<? extends FunctionTuple> getArgs() {
        return this.args;
    }
    
    //VEDI commento in calce ai CAMPI
    @Override
    public final boolean equals (Object o) {
        return N_aryOp.super.isEqual(o);
    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.args);
        
        return hash;
    }

    @Override
    public final Map<Sort, Integer> splitDelimiters() {
        return ColorClass.mergeSplitDelimiters(this.args);
    } 
        
     @Override
    public final boolean simplified () {
        return this.simplified;
    }
    
    @Override
    public final void setSimplified(boolean simplified) {
       this.simplified = simplified;
    }
    
    @Override
    public final String toString() {
        return N_aryOp.super.toStringOp();
    }
    
    @Override
    public final Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }

}
