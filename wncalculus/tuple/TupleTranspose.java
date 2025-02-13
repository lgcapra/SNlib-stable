package tuple;

import java.util.*;
import expr.*;

/**
 *
 * @author Lorenzo Capra
 */
public final class TupleTranspose implements FunctionTuple, UnaryOp<FunctionTuple> {

    private final FunctionTuple arg; // the function to be transposed
    private boolean simplified;
    
    /**
     * base constructor: creates the transpose of a function-tuple 
     * @param t a function-tuple
     */
    public TupleTranspose (FunctionTuple t) {
        this.arg = t;
    }
        
    @Override
    public TupleTranspose buildOp( FunctionTuple t) {
        return new TupleTranspose( t);
    }
    
    @Override
    public Domain getCodomain() {
        return this.arg.getDomain();
    }

    @Override
    public Domain getDomain() {
        return this.arg.getCodomain();
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return this.arg.splitDelimiters();
    }
    
    @Override
    public boolean equals (Object o) {
        return UnaryOp.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.arg);
        return hash;
    }

    /**
     * clone <tt>this</tt> transpose; the method is overridden because the
     * arity of the operand and that of the operator are "inverted"
     * of the operator are the same (to be overridden otherwise)
     * @return a clone of <tt>this</tt> with the specified co-domain
     */
    /*
    @Override
    public TupleTranspose clone (final Domain newdom, final Domain newcd) {
        return new TupleTranspose(getArg().clone(newcd, newdom).cast());
    }*/

    @Override
    public FunctionTuple specSimplify() {
        if (this.arg instanceof Tuple) 
            return ((Tuple)this.arg).transpose();
        
        if (this.arg instanceof AllTuple) 
            return  AllTuple.getInstance( getCodomain(), getDomain() ); 
        
        return this.arg instanceof EmptyTuple ? EmptyTuple.getInstance( getCodomain(), getDomain() ) : this; 
    }

    @Override
    public boolean isConstant() {
        return this.arg.isConstant();
    }

    @Override
    public boolean differentFromZero() {
        return this.arg.differentFromZero();
    }

    @Override
    public FunctionTuple getArg() {
        return this.arg;
    }
    
    @Override
    public String symb() {
        return "'";
    }

    @Override
    public final String toString() {
        return toStringPost();
    }

    @Override
    public boolean isInvolution() {
        return true;
    }
    
    @Override
    public boolean isDistributive (Class<? extends MultiArgs> optk) {
         return optk.equals(TupleSum.class);
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
    public Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }
    
}
