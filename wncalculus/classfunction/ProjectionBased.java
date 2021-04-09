package wncalculus.classfunction;

import java.util.Collection;
import wncalculus.expr.SingleSortExpr;
import wncalculus.util.Pair;

/**
 * this abstract class defines the template for projection-based functions
 * @author Lorenzo Capra
 */
public interface ProjectionBased extends SingleSortExpr {
    
     /**
      * 
      * @return the function's index
      */
     Integer getIndex() ;
  
    /**
     *
     * @return the successor parameter of <tt>this</tt> function
     * (0 if the function's color-class is not ordered)
     */
    Integer getSucc();
    
    /**
     * 
     * @return the complementary of <tt>this</tt> function 
     */
    ProjectionBased opposite();
    
    
    /**
     * given a collection of class-functions computes the max (in absolute value)
     * negative/positive successors considering ProjectionBased terms
     * @param terms a collection of functions
     * @return the pair  min(0,{negative_successors}), max(0,{positive_successors})
     */
    public static Pair<Integer,Integer> succBounds (Collection<? extends ClassFunction> terms) {
        int max_succ = 0, min_succ = 0, succ; //the "max" (positive/neg.) split delimiters for terminal symbols
        for (ClassFunction f : terms) 
            if (f instanceof ProjectionBased)
                if ( (succ = ((ProjectionBased) f).getSucc()) > 0 ) 
                    max_succ = Math.max(max_succ,  succ);
                else if (succ < 0 )
                    min_succ = Math.min(min_succ, succ);
        
        return new Pair<>(min_succ, max_succ); 
    }
    
    @Override
    default SetFunction cast () {
        return (SetFunction) this;
    }
    
}
