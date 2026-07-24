package tuple;

import java.util.*;
import color.Sort;
import expr.*;
import wnbag.ArcFunction;
import wnbag.TupleBag;
import wnbag.WNtuple;


/**
 * This class represents the support of WN function-tuples mapping on multi-sets
 * @author Lorenzo Capra
 */

public final class TupleSupport implements FunctionTuple, SingleArg<ArcFunction, FunctionTuple> {
    
    private final ArcFunction func;
    private boolean simplified;
    
    /**
     * base constructor: creates the support of a bag-expression
     * @param b a bag-expression
     */
    public TupleSupport(ArcFunction b) {
        this.func = b;
    }


    //new version: the support is a unary operator
    // ok (uncomment) once the asSetFunction has been implemented
    @Override
    public FunctionTuple specSimplify() {
        if (this.func instanceof TupleBag) {
            TupleBag bag = (TupleBag)this.func;
            Set<? extends WNtuple> support = bag.properSupport();
            
            if (support.isEmpty()) {
                return getFalse();
            } 
            //else {
            //    Set<FunctionTuple> suppfunc = new HashSet<>();
            //    for     (WNtuple t : support) {
            //        suppfunc.add(t.asSetFunction() );
            //    }
            //    return TupleSum.factory(suppfunc, bag.disjoined());
        }
        
        return this;
    }

    @Override
    public boolean differentFromZero() {
        return false;
    }

    @Override
    public boolean isConstant() {
        return this.func.isConstant();
    }
    
    
   @Override
   public boolean equals (Object o) {
        return SingleArg.super.isEqual(o);
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.func);
        
        return hash;
    }
    
    @Override
    public String toString() {
        return "{"+this.func+'}';
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return this.func.splitDelimiters();
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
    
    @Override
    public final Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }

   
    @Override
    public TupleSupport buildOp(ArcFunction arg) {
       return new TupleSupport(arg);    
    }

    @Override
    public String symb() {
        return "{" + this.func + " }";
    }

    @Override
    public ArcFunction getArg() {
        return this.func;
    }

}

