/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wncalculus.wnbag;

import java.util.HashMap;
import java.util.Map;
import wncalculus.bagexpr.LogicalBag;
import wncalculus.expr.Domain;
import wncalculus.expr.ParametricExpr;
import wncalculus.guard.Guard;
import wncalculus.tuple.FilteredTuple;
import wncalculus.tuple.FunctionTuple;

/**
 * this class defines SN functions as bags of <tt>FunctionTuple</tt>s
 * @author lorenzo capra
 */
public final class FunctionTupleBag extends LogicalBag<FunctionTuple> implements SNArcFunction  {

     public FunctionTupleBag(Map<FunctionTuple, Integer> m, boolean disjoint) {
        super(m,disjoint);
    }
    
    public FunctionTupleBag(Map<FunctionTuple, Integer> m) {
        this(m,false);
    }
    
    public FunctionTupleBag(Domain dom, Domain codom) {
        super(dom, codom);
    }
    
    /**
     * build an empty bag
     * @param dom the bag's co-domain
     */
    public FunctionTupleBag(Domain dom) {
        this(dom, dom);
    }
    
    //costruttori secondari (si potrebbero togliere: sono già definiti metodi corrispondenti in BagBuilder)

    public FunctionTupleBag(FunctionTuple f , int k) {
        super (f, k);
    }

    
    @Override
    public FunctionTupleBag applyFilter(Guard f) {
        Map<FunctionTuple, Integer> mx = new HashMap<>();
        asMap().entrySet().forEach(e -> { mx.put(FilteredTuple.factory(f, e.getKey()), e.getValue()); });
        
        return new FunctionTupleBag(mx, disjoined());
    }

    @Override
    public FunctionTupleBag build(Domain dom, Domain codom) {
        return new FunctionTupleBag(dom,codom);
    }

    
     @Override
    public FunctionTupleBag build(Map<FunctionTuple, Integer> smap, boolean disj) {
        return new FunctionTupleBag(smap,disj);
    }
    
}
