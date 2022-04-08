package wncalculus.tuple;

import java.util.*;
import wncalculus.classfunction.All;
import wncalculus.classfunction.SetFunction;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.expr.Sort;
import wncalculus.util.ComplexKey;

/**
 * singleton-like class implementing "universe" tuples
 * possible alternative implementation (currently not used)
 * @author lorenzo capra
 */
//def da rivedere: integrare con FunctionTuple.buildConstantTuple
public final class AllTuple1  implements FunctionTuple {

    private final Domain domain, codomain;
    /**
     * creates a map between colors and sub-lists of constants All functions
     * mathcing the specified codomain
     * @param codom the codomain
     * @return a map between colors and sub-lists of constants All functions,
     * according with the given codomain
     */
    public static Map<ColorClass, List<SetFunction>> mapToFunctions(Domain codom) {
        Map<ColorClass, List<SetFunction>> map = new HashMap<>();
        codom.support().forEach(s -> {
            ColorClass cc = (ColorClass) s;
            map.put(cc, Collections.nCopies(codom.mult(s), All.getInstance(cc)));
        });
        return map;
    }
    
    public AllTuple1(Domain codom, Domain dom) {
        domain = dom;
        codomain = codom;
    }
    
    private static final Map<ComplexKey, AllTuple1> AT_VALUES = new HashMap<>();
    
    /**
     * build a "Universe" tuple
     * @param codom the tuple's codomain
     * @param dom the tuple's domain
     * @return a "Universe" tuple of the given co-domain
     */
    public static AllTuple1 getInstance(Domain codom, Domain dom) {
        ComplexKey k = new ComplexKey(codom, dom);
        AllTuple1 at = AT_VALUES.get(k);
        if ( at == null) {
            AT_VALUES.put(k, at = new AllTuple1( codom, dom));
            at.setSimplified(true); //OPTIMIZATION
        }
        
        return at;
    }
    
    @Override
    public boolean isTrue() {
        return true;
    }

  
    @Override
    public AllTuple1 clone(Domain newdom, Domain newcd) {
        return getInstance(newcd, newdom);
    }
    


    @Override
    public boolean differentFromZero() {
        return true;
    }

   
    @Override
    public Integer cardLb() {
        int card = 1;
        for (Map.Entry<? extends Sort, Integer> x : getCodomain().asMap().entrySet()) {
            int lb = x.getKey().lb();
            for (int i = 0; i < x.getValue() ; i++)
                card = Math.multiplyExact(card , lb);
        }
        
        return card;
    }
    

    String symbol() {
        return "All";
    }

    
    @Override
    public Map<Sort, Integer> splitDelimiters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public final Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }

    @Override
    public boolean simplified() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSimplified(boolean simplified) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Domain getDomain() {
        return this.domain;
    }

    @Override
    public Domain getCodomain() {
        return this.codomain;
    }
}
