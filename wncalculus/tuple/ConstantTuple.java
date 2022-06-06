package wncalculus.tuple;

import java.util.*;
import wncalculus.expr.Domain;
import wncalculus.expr.Sort;

/**
 * the super-type of "boolean" tuple-constants
 * @author lorenzo capra
 */
public abstract class ConstantTuple implements FunctionTuple {
    
    private final Domain dom, codom;
    
    /**
     * build a <tt>ConstantTuple</tt> of given co-domain
     * @param codom the tuple's codomain
     * @param dom the tuple's domain
     */
    protected ConstantTuple(Domain codom, Domain dom) {
        this.dom = dom;
        this.codom = codom;
    }
   
    @Override
    public final Map<Sort, Integer> splitDelimiters() {
        return new HashMap<>();
    }

    @Override
    public final boolean isConstant() {
        return true;
    }

    @Override
    public final Domain getDomain() {
        return this.dom; 
    }

    @Override
    public final Domain getCodomain() {
        return this.codom;
    }
    
    abstract String symbol();
    
    @Override
    public boolean simplified() {
        return true;
    }

    @Override
    public void setSimplified(boolean simplified) { }
    
    @Override
    public final String toString() {
        return '<'+symbol()+">_"+ getCodomain().names();//"<\u2297S>";
    }
    
     @Override
    public final Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }
}
