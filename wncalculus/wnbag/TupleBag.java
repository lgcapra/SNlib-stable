package wncalculus.wnbag;

import java.util.HashMap;
import java.util.Map;
import wncalculus.bagexpr.AbstractBag;
import wncalculus.expr.Domain;
import wncalculus.expr.Sort;
import wncalculus.guard.Guard;

/**
 *
 * this interface defines the super-type of SN arc functions
 * @author lorenzo capra
 */
public final class TupleBag extends AbstractBag<WNtuple> implements SNArcFunction {
    
    public TupleBag(Map<? extends WNtuple, Integer> m) {
        super(m);
    }
    
    public TupleBag(Domain dom, Domain codom) {
        super(dom,codom);
    }
    
   
    @Override
    public Integer card() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TupleBag build(Domain dom, Domain codom) {
        return new TupleBag(dom,codom);
    }

    @Override
    public TupleBag build(Map<WNtuple, Integer> m) {
        return new TupleBag(m);
    }

    @Override
     public TupleBag applyFilter(Guard f) {
        Map<WNtuple, Integer> mx = new HashMap<>();
        asMap().entrySet().forEach(e -> { mx.put( e.getKey().joinFilter(f), e.getValue()); });
        
        return new TupleBag(mx);
    }

   
}
