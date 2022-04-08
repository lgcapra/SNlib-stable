package wncalculus.classfunction;

import java.util.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.expr.Interval;
import wncalculus.guard.Equality;
import wncalculus.util.ComplexKey;


/**
 * this class defines the ProjectionComp complement function 
 * @author lorenzo capra
 */
public final class ProjectionComp extends SetFunction implements ProjectionBased  {
    
    private final Projection pr;
    
    private static final Map<ComplexKey, ProjectionComp> VALUES = new HashMap<>();
    
     /**
     * constructor
     * @param index the projection's index
     * @param exp the successor argument
     * @param cc  the projection's color
     */
    private ProjectionComp (Projection arg)  {
        this.pr = arg;
    }
    
    /**
     * basic factory method: builds the complement of a projection
     * @param pr the projection
     * @return either a projection complement, or a projection, if the color class of
     * <code>pr</code> is ordered and of (fixed) card two
    */
    public static ProjectionBased factory (Projection pr) {
        ColorClass cc = pr.getSort();
        if (cc.isOrdered() && cc.fixedSize( ) == 2) 
            return pr.setExp(pr.getSucc() == 0 ? 1 : 0);
        
        ProjectionComp pc;
        ComplexKey k = new ComplexKey(cc, pr);
        if ( (pc = VALUES.get(k) ) == null) 
            VALUES.put(k, pc = new ProjectionComp (pr)) ;
        
        return pc;
    }
    
    
    @Override
    public int getIndex() {
        return this.pr.getIndex();
    }

   
    @Override
    public int getSucc() {
        return this.pr.getSucc();
    }
      
    /**
     *
     * @return the <tt>Projection</tt> complemented
     */
    public Projection getArg() {
        return this.pr;
    }
    
    /**
     * @return the constraint [lb-1,x], where x is either infinity or ub-1
     */
    @Override
    public Interval card() {
        Interval in = getConstraint();
        
        return in.unbounded() ? new Interval(in.lb() - 1) : new Interval(in.lb() - 1, in.ub()-1);
    }
    
           
    
    @Override
    public SetFunction baseCompose(SetFunction right) {
        SetFunction res = null;
        Interval rcard = right.card();
        if (rcard != null) {
            int lb= rcard.lb();
            if (lb  > 1) 
                res = All.getInstance(getSort());
            else if (lb == 1 && rcard.ub() == 1)
                res = Complement.factory(Successor.factory(getSucc(),right));
            else {
                //System.err.println("cannot solve "+this+'.'+right.toStringDetailed()); //debug
            }
        }
        
        return res;
   } 
    
    @Override
    public int splitDelim() {
        ColorClass c = getSort();
        
        return c.lb() == 2 && c.ub() != 2  ? 2 : this.pr.splitDelim(); // the function cardinality is 1 and the constraint is [2,..]
    }
    
    /**
     * replaces the variable of @code{this} function according to the specified equality
     * assuming that: 1) the color-class is the same, 2) the equality is not "trivial"
     * (the members have different indices)
     * @param eq an equality
     * @return the function corresponding to @code{this}, modulo the replacement induced by the
     * equality; @code{this} is the second index of the equality coincides with @code{this} index
     */
    @Override
    public SetFunction replace(Equality eq) {
        Projection p = this.pr.replace(eq);
        
        return p == this.pr ? this : factory(p).cast();
    }
    
        
    @Override
    public ProjectionComp setDefaultIndex() {
    	Projection p = this.pr.setDefaultIndex();
    	
        return p == this.pr ? this :  (ProjectionComp) factory(p) ;
    } 
    
    @Override
    public String toString() {
        return "S-" + this.pr;
    }

    @Override
    public Projection opposite() {
        return this.pr;
    }

    @Override
    public SetFunction copy(ColorClass newcc) {
        return factory(this.pr.copy(newcc)).cast();
    }

    @Override
    public SetFunction clone(Domain newdom, Domain newcd) {
            Projection p = (Projection) this.pr.clone(newdom, newcd);

            return p == this.pr ? this : factory(p).cast();
    }


    @Override
    public ColorClass getSort() {
            return this.pr.getSort();
    }

    @Override
    public Set<Integer> indexSet() {
            return this.pr.indexSet();
    }
    
}
