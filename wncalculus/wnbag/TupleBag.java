package wnbag;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import bagexpr.Bag;
import expr.Domain;
import util.Util;

/**
 *
 * this class defines the super-type of SN arc functions
 * @author lorenzo capra
 */
public final class TupleBag extends Bag<WNtuple> implements ArcFunction {
    
    public TupleBag(Map<? extends WNtuple, Integer> m) {
        super(m);
    }

    public TupleBag(boolean simplified, Collection<? extends WNtuple> tuples) {
        this(Util.asMap(tuples));
    }

    public TupleBag(boolean simplified, WNtuple... tuples) {
        this(Util.asMap(Arrays.asList(tuples)));
    }
    
    public TupleBag(Domain dom, Domain codom) {
        super(dom,codom);
    }


    @Override
    public TupleBag buildEmpty(Domain dom, Domain codom) {
        return new TupleBag(dom,codom);
    }

    @Override
    public TupleBag build(Map<? extends WNtuple, Integer> m) {
        return new TupleBag(m);
    }

    //@Override
    // public TupleBag applyFilter(Guard f) {
    //     Map<WNtuple, Integer> mx = new HashMap<>();
    //     asMap().entrySet().forEach(e -> { mx.put( e.getKey().joinFilter(f), e.getValue()); });
    //
    //     return new TupleBag(mx);
    // }


    @Override
    public TupleBag clone(Domain newdom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TupleBag clone() {
        return new TupleBag(new HashMap<>(asMap()));
    }


}
