package wncalculus.classfunction;

import java.util.Objects;
import wncalculus.logexpr.NotOp;
import wncalculus.expr.Interval;
/**
 * this class implements the "complementary" functional operator for ClassFunction
 * its semantics is : Complement(f(X)) \equiv S - f(X) 
 * @author lorenzo capra
 */
  public final class Complement extends UnaryClassOp implements NotOp<SetFunction> {
      
    private Complement(SetFunction f)  {
        super(f);
    }
    
    /**
     * build the complement of a given function
     * @param f a class-function mapping to a set
     * @return the <tt>Complement</tt> of the class-function; if the function is elementary
     * directly computes the result
     */
    public static SetFunction factory(SetFunction f) { //optimization: all specific reductions done at this level
        SetFunction res;
        if (f instanceof Projection) 
            res = (SetFunction) ProjectionComp.factory((Projection) f);
        else if (f instanceof ProjectionComp) 
            res = ((ProjectionComp) f).getArg();
        else if (f instanceof Subcl) 
            res = ((Subcl) f).opposite(); 
        else
            res = new Complement(f);
        
        return res;
    }
    
    @Override
    public SetFunction buildOp(SetFunction arg) {
        return factory (arg);
    }    
    
  
    @Override
    public String symb() {
        return "comp";
    }
  
    /** this smart implementation allows card to be computed either if the
     * argument's card is fixed, or it corresponds to the color class cardinality
     * @return  */
    @Override
    public Interval card() {
        Interval mycard = getArg().card(), res = null;
        if (mycard != null) {
            Interval in = getConstraint();
            if ( mycard.equals(in) ) 
                res = new Interval(0,0);
            else if (mycard.singleValue()) 
                res = in.unbounded() ? new Interval(in.lb()- mycard.lb() ) : new Interval(in.lb()- mycard.lb(), in.ub()- mycard.ub());
        }
        
        return res;
    }
    
    @Override
    public boolean equals (Object o) {
        return NotOp.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(getArg());
        
        return hash;
    }

}
