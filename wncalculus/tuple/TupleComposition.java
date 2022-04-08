package wncalculus.tuple;

import wncalculus.expr.*;
import wncalculus.logexpr.LogComposition;
import wncalculus.logexpr.SetExpr;
import wncalculus.wnbag.FunctionTupleBag;


/**
 * this class defines the composition between WN function-tuples
 * @author Lorenzo Capra
 */
public final class TupleComposition implements FunctionTuple, LogComposition<FunctionTuple> {
    
    private FunctionTuple left , right;
    private boolean simplified;
    
    /** creates a new composition between function-tuples after having possibly checked that the (co)domains are consistent
     * if the left operand is a Tuple its "reduce guard" flag is set up
     * @param left the left operand
     * @param right the right operand
     * @param check domain-check flag
     */
    public TupleComposition (FunctionTuple left , FunctionTuple right, boolean check) {
        if (/*check && */!left.getDomain().equals(right.getCodomain())) 
            throw new IllegalDomain("the domain of the left function and the codomain of the right one do not correspond!");
        
        setArgs(left,right);
    }
    
    /** creates a new composition between function-tuples assuming that the (co)domains are consistent
     * @param left left function
     * @param right right function
     */
    public TupleComposition (FunctionTuple left , FunctionTuple right)  {
        this(left, right, false);
    }
    
    //needed because inherited twice
    @Override
    public FunctionTupleBag nullBag() {
        return FunctionTuple.super.nullBag();
    }
    
    @Override
    public FunctionTupleBag asBag() {
        return FunctionTuple.super.asBag();
    }
    
    @Override
    public TupleComposition buildOp(FunctionTuple left, FunctionTuple right) {
        return new TupleComposition( left, right);
    }
    
    private void setArgs(FunctionTuple left, FunctionTuple right) {
        this.left   = left;
        if (left instanceof Tuple) //new
            ((Tuple)this.left).setReduceGuard(true);
        this.right  = right;
    }
    
    @Override
    public FunctionTuple specSimplify() {
        if ( left instanceof FilteredTuple ) {
            FilteredTuple ft = (FilteredTuple) left;
            return FilteredTuple.factory(ft.guard(), new TupleComposition(ft.expr(), right));
        } else { 
            //System.out.println("***\n"+this);
            FunctionTuple res;
            if (left.isTuple() && right.isTuple() && (res = left.asTuple().compose(right.asTuple())) != null ) {
                if ( res instanceof Tuple) //the composition has been solved
                    ((Tuple)res).setReduceGuard(false); // default condition (not needed?)
                //System.out.println("\n-->\n"+res);
                return res ;
            }
        }
        
        return this;
    }
    
    @Override
    public boolean differentFromZero() {
        return false;
    }

    @Override
    public final FunctionTuple left() {
        return this.left;
    }

    @Override
    public final FunctionTuple right() {
        return this.right;
    }
    
    @Override
    public final boolean isLeftAssociative(Class<? extends SingleArg> optk) {
        return optk.equals(TupleProjection.class); 
    }


    @Override
    public boolean equals(Object o) {
        return LogComposition.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.left.hashCode();
        hash = 53 * hash + this.right.hashCode();
        
        return hash;
    }

    @Override
    public final String toString() {
        return LogComposition.super.toStringOp();
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
    
    //overriden because muutipple inherited
    @Override
    public Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }
    
    @Override
    public TupleBagComp buildBagComp(SetExpr e) {
        return FunctionTuple.super.buildBagComp(e);
    }
}
