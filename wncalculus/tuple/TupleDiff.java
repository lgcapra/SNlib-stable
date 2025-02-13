package tuple;

import java.util.*;
import expr.*;


/**
 * this class defines the difference operator between function-tuples
 * @author Lorenzo Capra
 */
public final class TupleDiff implements FunctionTuple, TwoArgs<FunctionTuple,FunctionTuple> {

    private final FunctionTuple min, subtr; //the minuend, and the subtrahend
    private boolean simplified;
    
    /** base constructor */
    private TupleDiff (FunctionTuple min , FunctionTuple subtr)  {
        this.min   = min;
        this.subtr = subtr;
    }
    
    /**
     * factory method: builds a <tt>FunctionTuple</tt> which is the difference between the operands,
     * by immediatley operating some simplifications (if possible)
     * @param min the minuend
     * @param subtr the subtrahend
     * @param check domain-check flag
     * @return the <tt>FunctionTuple</tt> which is the difference between the operands
     * @throws IllegalDomain if the arity of operands is not the same
     */
    public static FunctionTuple factory (FunctionTuple min , FunctionTuple subtr , boolean check) { // anticipates specific reductions
        if (check && ! ( min.getCodomain().equals(subtr.getCodomain()) &&  min.getDomain().equals(subtr.getDomain())) ) 
                throw new IllegalDomain("minuend and subtrahend must be compliant!");
        
        if (min.isFalse() || subtr.isTrue()) 
            return min.getFalse(); // might be a general rule 
        
        if ( subtr.isFalse()) 
            return min; //as above
         
        if (min instanceof TupleSum) 
            return ((TupleSum)min).diff(subtr); //ATTENZIONE: cast necessario
        
        if (subtr instanceof TupleSum)
            return min.subtract((TupleSum) subtr);
        
        if (subtr instanceof TupleDiff && ((TupleDiff)subtr).min instanceof AllTuple) // X - (ALL - Y)
            return TupleIntersection.factory(min, ((TupleDiff)subtr).subtr);
        
        //sistemare (ottimmizzare il caso All - x
        if (subtr instanceof Tuple) {
            if (min instanceof AllTuple)
                return ((Tuple) subtr).complement();  
            
            if (min instanceof Tuple)  
                return ((Tuple)min).diff((Tuple)subtr); //ATTENZIONE: cast necessari
        }
        //res = new TupleDiff(min , subtr);
        //System.out.println("TupleDiff (98)\n"+this+"\n-->"+res);//debug
        return new TupleDiff(min , subtr) ;
    }
    
    /** overloaded constructor not checking domains' compliance
     * @param min the minuend
     * @param subtr the subtrahend
     * @return the <tt>FunctionTuple</tt> which is the difference between the operands
     */
    public static FunctionTuple factory (FunctionTuple min , FunctionTuple subtr )  {
        return factory (min, subtr, false);
    }
    
    /**
     *
     * @return the minuend
     */
    public FunctionTuple minuend () {
        return this.min;
    }
    
    /**
     *
     * @return the subtrahend
     */
    public FunctionTuple subtrahend () {
        return this.subtr;
    }
    
  
    @Override
    public Domain getCodomain() {
        return this.subtr.getCodomain();
    }

    @Override
    public Domain getDomain() {
        return this.subtr.getDomain();
    }

    @Override
    public boolean differentFromZero() {
        return false;
    }

    /*@Override
    public FunctionTuple specSimplify() {
        if (this.min.isFalse() || this.subtr.isTrue()) 
            return getFalse(); // is a general rule 
        
        if (this.subtr.isFalse()) 
            return this.min;
         // is a general rule
       
        if (this.min instanceof TupleSum) 
            return ((TupleSum)this.min).diff(this.subtr); //ATTENZIONE: cast necessario
        
        if (this.subtr instanceof TupleSum)
            return this.min.subtract((TupleSum) this.subtr);
        
        //sistemare (ottimmizzare il caso All - x
        FunctionTuple res = null;
        if (this.subtr instanceof Tuple)
            if (this.min instanceof AllTuple)
                res  = ((Tuple) this.subtr).complement();  
            else if (this.min instanceof Tuple)  
                res = ((Tuple)this.min).diff((Tuple)this.subtr); //ATTENZIONE: cast necessari
        //System.out.println("TupleDiff (98)\n"+this+"\n-->"+res);//debug
        return res == null ? this : res;
    }*/

    @Override
    public String symb() {
        return "-";
    }

    @Override
    public boolean equals (Object o) {
        return TwoArgs.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.min);
        hash = 23 * hash + Objects.hashCode(this.subtr);
        return hash;
    }

    @Override
    public FunctionTuple left() {
        return this.min;
    }

    @Override
    public FunctionTuple right() {
        return this.subtr;
    }

    @Override
    public FunctionTuple buildOp(FunctionTuple left, FunctionTuple right) {
        return TupleDiff.factory(left, right); 
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

}
