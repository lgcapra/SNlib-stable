package wncalculus.tuple;

import java.util.*;
import wncalculus.bagexpr.*;
import wncalculus.expr.*;
import wncalculus.wnbag.FunctionTupleBag;


/**
 * This class represents the support of WN function-tuples mapping on multi-sets
 * @author Lorenzo Capra
 */

public final class TupleSupport implements FunctionTuple, SingleArg<BagExpr<FunctionTuple>, FunctionTuple> {
    
    private final BagExpr<FunctionTuple> func;
    private boolean simplified;
    
    /**
     * base constructor: creates the support of a bag-expression
     * @param b a bag-expression
     */
    public TupleSupport(BagExpr<FunctionTuple> b) {
        this.func = b;
    }


    //new version: the support is a unary operator
    @Override
    public FunctionTuple specSimplify() {
        if (this.func instanceof FunctionTupleBag) {
            FunctionTupleBag bag = (FunctionTupleBag)this.func;
            Set<? extends FunctionTuple> support = bag.properSupport();
            
            return support.isEmpty() ? getFalse(): TupleSum.factory(support, bag.disjoined()); // holds null if the support cannot yet be computed
        }
        
        if (this.func instanceof BagComp) { //may be generalized?
            BagComp<FunctionTuple> comp = (BagComp<FunctionTuple>) this.func;
            
            return new TupleComposition(new TupleSupport(comp.left()),new TupleSupport(comp.right()));
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
    public TupleSupport buildOp(BagExpr<FunctionTuple> arg) {
       return new TupleSupport(arg);    
    }

    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BagExpr<FunctionTuple> getArg() {
        return this.func;
    }


}

