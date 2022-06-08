package wncalculus.tuple;

import java.util.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.*;
import wncalculus.logexpr.GuardedExpr;
import wncalculus.logexpr.LogicalExpr;

/**
 * this abstract class defines function-tuples prefixed by a filter
 * @author Lorenzo Capra
 */
public final class FilteredTuple implements FunctionTuple, GuardedExpr<FunctionTuple> {
    
    private final FunctionTuple  expr;
    private       Guard          filter; //null means true!
    private boolean simplified;
    
    /**
     * creates a <tt>FilteredTuple</tt>
     * @param f a filter
     * @param expr a function-tuple
     */
    private FilteredTuple (Guard f, FunctionTuple expr) {
        if (expr == null)
            throw new NullPointerException("null expression!!");
            
       if (f == null)
            throw new NullPointerException("null filter!!");
        
        if (! expr.getCodomain().equals( f.getDomain() ) ) 
            throw new IllegalDomain("filter's domain must be the same as function'co-domain");    
        
        this.filter = f;
        this.expr   = expr;
    }
    
    /**
     * creates a <tt>FilteredTuple</tt> skipping a trivial filter
     * @param f a filter
     * @param expr a function-tuple
     */
    public static FunctionTuple factory (Guard f, FunctionTuple expr) {
        return f instanceof True ? expr : new FilteredTuple (f, expr);
    }
    
    @Override
    public Domain getDomain() {
        return this.expr.getDomain();
    }

    @Override
    public Domain getCodomain() {
        return this.expr.getCodomain();
    }
    
    @Override
    public FunctionTuple specSimplify() {
        if (this.expr instanceof AllTuple) 
            return new Tuple(this.filter, AllTuple.toMap( getCodomain()), getDomain());
                    
        if (this.expr instanceof TupleSum ) {
            var orexpr = (TupleSum ) this.expr;
            Collection<FilteredTuple> cgf = new ArrayList<>();
            orexpr.getArgs().forEach(fx -> { cgf.add(new FilteredTuple (this.filter, fx)); });
            return TupleSum.factory(cgf, orexpr.disjoined());
        }

        if (this.expr instanceof FilteredTuple ) { //new
           var g_expr = (FilteredTuple ) this.expr;
           return new FilteredTuple(And.factory(this.filter, g_expr.filter), g_expr.expr);
        }

        if (this.expr instanceof Tuple) 
            return  ((Tuple) this.expr).joinFilter(this.filter);
        
        return this;     
    }
    
    @Override
    public FilteredTuple clone(Domain newdom) {
        return new FilteredTuple (this.filter, (FunctionTuple) this.expr.clone(newdom));
    }
    
    @Override
     public FilteredTuple clone (final  Map<Sort, Sort> split_map) {
         return new FilteredTuple ((Guard)this.filter.clone(split_map), (FunctionTuple) this.expr.clone(split_map));
     }
    
    @Override
    public  boolean isFalse() {
        return this.filter.isFalse() || this.expr.isFalse();
    }

    @Override
    public boolean differentFromZero() {
        return this.filter.differentFromZero() && this.expr.differentFromZero();
    }

    @Override
    public boolean isTrue() {
        return this.filter.isTrue() && this.expr.isTrue();
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        Map<Sort,Integer>  delims  =  new HashMap<>();
        delims.putAll(this.filter.splitDelimiters());
        ColorClass.joinDelims(delims , this.expr.splitDelimiters());
        
        return delims;
    }

    @Override
    public boolean isConstant() {
        return  this.expr.isConstant();
    }
    
    @Override
    public boolean equals(Object other) {
        boolean res = other instanceof FilteredTuple;
        if (res) {
            FilteredTuple gf = (FilteredTuple) other;
            res = Objects.equals(this.filter, gf.filter)  && Objects.equals(this.expr,gf.expr);
        }
        
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.expr);
        hash = 71 * hash + Objects.hashCode(this.filter);
        
        return hash;
    }

    
    @Override
    public String toString () {
        return "["+this.filter+']' + '('+this.expr+')' ;
    }
        
    

    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Guard guard() {
        return this.filter;
    }

    @Override
    public FunctionTuple expr() {
        return this.expr;
    }

    @Override
    public FunctionTuple build(FunctionTuple expr, LogicalExpr guard) {
        return factory((Guard) guard, expr); 
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
    public final Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }
    
}
