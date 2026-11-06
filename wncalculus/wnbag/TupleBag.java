package wnbag;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import bagexpr.Bag;
import classfunction.ElementaryFunction;
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

    @Override
    public ArcFunction specSimplify() {
        final ArcFunction res = super.specSimplify().cast();
        if (!(res instanceof TupleBag tb)) {
            return res;
        }

        Map<WNtuple, Integer> simplified = new HashMap<>();
        boolean changed = false;

        for (Map.Entry<? extends WNtuple, Integer> e : this.asMap().entrySet()) {
            ArcFunction normalized = e.getKey().normalize();

            if (normalized instanceof WNtuple nt) {
                if (nt != e.getKey()) changed = true;
                simplified.merge(nt, e.getValue(), Integer::sum);
            } else if (normalized instanceof TupleBag ntb) {
                changed = true;
                for (Map.Entry<? extends WNtuple, Integer> ne : ntb.asMap().entrySet()) {
                    simplified.merge(ne.getKey(), e.getValue() * ne.getValue(), Integer::sum);
                }
            } else {
                return normalized;
            }
        }
        //System.out.println("\nSimplified tuples: " + simplified);
        return changed ? new TupleBag(simplified) : tb;
    }

    @Override
    public Integer cardLb() {
        if (asMap().size() > 1) {
            boolean hasPositive = false;
            boolean hasNegative = false;

            for (Map.Entry<? extends WNtuple, Integer> e : asMap().entrySet()) {
                final int coeff = e.getValue();
                if (coeff > 0) hasPositive = true;
                if (coeff < 0) hasNegative = true;

                if (hasPositive && hasNegative) {
                    return null;
                }
            }
        }

        return super.cardLb();
    }

}
