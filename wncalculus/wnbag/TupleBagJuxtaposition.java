package wnbag;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import bagexpr.Bag;
import bagexpr.BagExpr;
import color.Sort;
import expr.Domain;
import expr.N_aryOp;
import tuple.FunctionTuple;

/**
 *
 * @author lorenzo capra
 */
public final class TupleBagJuxtaposition implements  N_aryOp<ArcFunction>, ArcFunction {

    
    public static ArcFunction factory(List<BagExpr<FunctionTuple>> compositions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<WNtuple> bagType() {
        return WNtuple.class;
    }

    @Override
    public Collection<ArcFunction> getArgs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArcFunction buildOp(Collection<? extends ArcFunction> args) {
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
    public Bag<WNtuple> buildEmpty(Domain dom, Domain codom) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Bag<WNtuple> build(Map<? extends WNtuple, Integer> m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Integer cardLb() {
       return null; 
    }

    @Override
    public ArcFunction getIde() {
        throw new UnsupportedOperationException("Unimplemented method 'getIde'");
    }

    @Override
    public ArcFunction clone(Map<Sort, Sort> split_map) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clone'");
    }
}
