package wncalculus.tuple;

import java.util.*;
import wncalculus.expr.Domain;
import wncalculus.expr.ParametricExpr;
import wncalculus.expr.Sort;
import wncalculus.util.ComplexKey;

/**
 * "singleton-like" class implementing empty-set tuples (that is, constant function-tuples
 mapping to 0) of given size
 * @author lorenzo capra
 */
//def da rivedere: integrare con FunctionTuple.buildConstantTuple
public final class EmptyTuple extends ConstantTuple {

    
    private EmptyTuple(Domain codom, Domain dom) {
        super(codom, dom);
    }
    
    /**
     * the hash-map used by the builder method (should be set private)
     */
    public static final HashMap<ComplexKey, EmptyTuple> ET_VALUES = new HashMap<>();
    
    /**
     * main bulder method: creates an <tt>EmptyTuple</tt> with a given co-domain
     * @param codom the tuple's codomain
     * @param dom the tuple's domain
     * @return an <tt>EmptyTuple</tt> with a given co-domain
     */
    public static EmptyTuple getInstance(Domain codom, Domain dom) {
        ComplexKey k = new ComplexKey(codom, dom);
        EmptyTuple et = ET_VALUES.get(k);
        if ( et == null) {
            ET_VALUES.put(k, et = new EmptyTuple( codom, dom));
            et.setSimplified(true); //OPTIMIZATION
        }
        
        return et;
    }
    
    @Override
    public boolean isFalse() {
        return true;
    }
  
    
    @Override
    public EmptyTuple clone(Domain newdom) {
         return getInstance(getCodomain(), newdom);
    }
    
    @Override
     public EmptyTuple clone (final  Map<Sort, Sort> split_map) {
         return getInstance(getCodomain().setSupport(split_map), getDomain().setSupport(split_map));
     }
    

    @Override
    public boolean differentFromZero() {
        return false;
    }

    
    /**
     * 
     * @return the corresponding <code>Tuple</code> object 
     * never used?
     */
    
    @Override
    public Integer cardLb() {
        return 0;
    }

    @Override
    String symbol() {
        return "Empty";
    }

}
