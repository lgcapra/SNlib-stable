package wncalculus.wnbag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import wncalculus.bagexpr.Bag;
import wncalculus.bagexpr.BagExpr;
import wncalculus.expr.Domain;
import wncalculus.expr.MultiArgs;
import wncalculus.expr.Sort;
import wncalculus.guard.Guard;
import wncalculus.tuple.FunctionTuple;

/**
 *
 * @author lorenzo capra
 */
public class TupleBagJuxtaposition implements BagExpr<FunctionTupleBag> , SNArcFunction, MultiArgs<FunctionTupleBag,BagExpr<FunctionTupleBag>> {

    
    public static FunctionTupleBag factory(List<BagExpr<FunctionTuple>> compositions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<FunctionTupleBag> bagType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<FunctionTupleBag> getArgs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BagExpr<FunctionTupleBag> buildOp(Collection<? extends FunctionTupleBag> args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public Bag<FunctionTupleBag> build(Domain dom, Domain codom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Bag<FunctionTupleBag> build(Map<FunctionTupleBag, Integer> m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <E extends SNArcFunction> E applyFilter(Guard f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Class<? extends BagExpr> type() {
        return BagExpr.super.type();
    }
}
