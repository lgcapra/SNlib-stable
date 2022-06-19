package wncalculus.classfunction;

import java.util.*;
import wncalculus.expr.*;
import wncalculus.guard.Equality;
import wncalculus.guard.Guard;
import wncalculus.logexpr.LogicalExpr;
import wncalculus.logexpr.SetExpr;
import wncalculus.util.Pair;
import wncalculus.util.Util;
import wncalculus.wnbag.LinearComb;

/**
 * this abstract class is the super-type of class-functions mapping to sets
 * @author lorenzo capra
 */
public abstract class SetFunction implements ClassFunction, SetExpr {

    private boolean simplified;
    private Domain dom ; //optimization (caching)
    
    
    protected SetFunction(boolean simp) {
        this.simplified = simp;
    }
    
    protected SetFunction () { }
    
    @Override
    public final Domain getDomain() {
        if (dom == null)
            dom = new Domain(getSort(),1);
        
        return dom;
    }
    
    
    @Override
    public final Domain getCodomain() {
        return getDomain();
    }
    
    
    @Override
    public final  ClassCompAsBag buildBagComp(SetExpr f) {
        return new ClassCompAsBag(this, f.cast());
    }
        
    
    @Override
    public LinearComb nullBag() {
        return new LinearComb(getSort());
    }
    
    /**
     * (default implementation to override)
     * @return an elementary linear-combination composed of <tt>this</tt> term 
     * DA TOGLIERE CON NUOVA IMPLEMENTAZIONE DEI BAG?
     */
    @Override
    public LinearComb asBag() {
    	throw new UnsupportedOperationException();
    }
    
    
    @Override
    public final All getTrue() {
        return All.getInstance(getSort());
    }

     @Override
     public final Empty getFalse() {
         return Empty.getInstance(getSort());
     }
     
      @Override
      public final SetFunction notFactory(LogicalExpr arg) {
          return Complement.factory((SetFunction) arg);
      }
      
      //the following methods return the passed argument, in the case the collection is a singleton

    
     @Override
     public final SetFunction andFactory(Collection<? extends LogicalExpr> args) {
          return Intersection.factory(Util.cast(args, SetFunction.class), false);
      }
      
      
      @Override
      public final SetFunction orFactory(Collection<? extends LogicalExpr> args, boolean disjoined) {
          return Union.factory(Util.cast(args, SetFunction.class), disjoined);
      }
      
     @Override
     public ClassComposition buildSetComp(SetExpr r)  {
         return new ClassComposition(this, r.cast());
     }
        
     @Override
     public final boolean differentFromZero() {
        Interval mycard = card();
        return mycard != null && mycard.lb() > 0;
    }
     
    /**
     *
     * @param ite a collection of class-functions mapping to sets
     * @return <tt>true</tt> if and only if every function is not equivalent to "zero"
     */
    public final static boolean differentFromZero(Iterable<? extends SetFunction> ite) {
        for (SetFunction f : ite) 
            if ( !f.differentFromZero() ) 
                return false;
        
        return true;
    }
        
    /**
     *
     * @return the size of any application of <tt>this</tt> function;
     * <tt>null</tt> if it cannot be computed, for any reasons
     */
    public abstract Interval card();
    
    @Override
    public final Integer cardLb() {
         Interval c = card();
         return c == null ? null : c.lb();
    }
    
    /** 
     * @return <tt>true</tt> if and only if this function has zero cardinality 
     */
    public boolean zeroCard () {
        var constr = card();
        if (constr == null) {
            //System.err.println(toStringDetailed()+": null card");
            //throw new NullPointerException();
            return false;
        }
        var card = constr.singleValue();
        return card != null && card == 0;
    }
    
    /**
     *
     * @return the difference between the lower bounds of <tt>this</tt> function's color-class cardinality
     * and the function's cardinality, if the respective constraints are "congruent";
     * 0, if they are not; <code>null</code> if function's cardinality cannot be computed 
     */
    public final Integer gap () {
        Interval mycard = card();
        if (mycard == null) {
            return null;
        } else {
            int gap = 0; // before -1
            var constr = getConstraint();
            if ( mycard.size() == constr .size() ) //  card and constr are either unbounded or "congruent" 
                  gap = constr.lb() - mycard.lb();
            
            return gap;
        }
    }
    
    /**
     *
     * @return <tt>true</tt> if and only if the size (cardinality) of <tt>this</tt> function
     * is less than or equal to one
     */
    public final boolean cardLeq1 () {
        Interval card = card();
        return card != null && card.singleValue(1) || 
               this instanceof Intersection && Util.find( ((Intersection)this).getArgs(), Projection.class) != null ;
    }
    
    /**
     * calculates the composition between type-set class-functions,
     * disregarding projection indices (i.e., considering functions as if they were "unary")
     * @param right the function to compose with <tt>this</tt>
     * @return a pair holding the composition result (<tt>null</tt> if, for any reasons,
     * the composition outcome cannot be derived) and an integer representing a split delimiter
     * required to solve the composition (in that case a pair (null,intval) would
     * be returned); if neither the composition outcome nor a split delimiter can be devised a
     * <code>null</code> pair is returned
     */
    public Pair<SetFunction,Integer> baseCompose (SetFunction right) {
        return null;
    }
    
    @Override
    public final Class<? extends SetFunction> type () {
        return SetFunction.class;
    }
    
    /**
     * puts a funaction to an equivalent set (sum) of simple (guarded) class-functions
     * this default version must be redefined if needed; it assumes that basic reductions have been
     * carried out; the arguments encode the basic predicates (guard) associated with the tuple in which
     * the function is embedded
     * Default implementation (to override)
     * @param ineqlist the inequalities 
     * @param inmap a map describing the membership "in" predicates
     * @param domain the guard domain
     * @param notinmap a map describing the membership "notin" predicates (there may be many, for a given variable)
     * @return if <tt>this</tt> is an "elementary" intersection-form, an equivalent
     * set (sum) of simple (guarded) class-functions, represented by <tt>Pair</tt>s (if any);
     * otherwise, an empty-set
    */ 
    public Set<? extends Pair<? extends SetFunction, ? extends Guard> > toSimpleFunctions (Set<? extends Equality> ineqlist, Map<Projection, Subcl> inmap, Map<Projection, Set<Subcl>> notinmap, Domain domain) {
         return Collections.EMPTY_SET;
    }
    
    
    @Override
    public void setSimplified(boolean simp) {
        this.simplified = simp;
    }
    
    @Override
    public final boolean simplified() {
        return this.simplified;
    }
    
}
