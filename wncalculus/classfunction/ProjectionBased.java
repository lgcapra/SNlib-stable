package classfunction;

import color.ColorClass;
import java.util.Collection;
import util.Pair;

/**
 * this abstract class defines the template for projection-based functions
 * @author Lorenzo Capra
 */
public abstract class ProjectionBased extends ElementaryFunction  {

    /**
     * 
     * @param cc the color class of the function
     * @throws IllegalArgumentException if the class is not neutral
     */
    public ProjectionBased(final ColorClass cc) {
        super(cc);
        checkNeutral();
    }
     /**
      * 
      * @return the function's index
      */
     public abstract int getIndex() ;
  
    /**
     *
     * @return the successor parameter of <code>this</code> function
     * (0 if the function's color-class is not ordered)
     */
    public abstract int getSucc();
    
    /**
     * 
     * @return the complementary of <code>this</code> function 
     */
    public abstract ProjectionBased opposite();
    
    
    /**
     * given a collection of class-functions computes the max (in absolute value)
     * negative/positive successors considering ProjectionBased terms
     * @param terms a collection of functions
     * @return the pair  min(0,{negative_successors}), max(0,{positive_successors})
     */
    public static Pair<Integer,Integer> succBounds (final Collection<? extends ClassFunction> terms) {
        int max_succ = 0, min_succ = 0, succ; //the "max" (positive/neg.) split delimiters for terminal symbols
        for (ClassFunction f : terms) 
            if (f instanceof ProjectionBased projectionBased)
                if ( (succ = projectionBased.getSucc()) > 0 ) 
                    max_succ = Math.max(max_succ,  succ);
                else if (succ < 0 )
                    min_succ = Math.min(min_succ, succ);
        
        return new Pair<>(min_succ, max_succ); 
    }

    private void checkNeutral() {
        if (getSort().neutral()) {
            throw new IllegalArgumentException("Only constants may have a neutral colour");
        }
    }

    @Override
    public final int succDelim() {
        return getSucc();
    }
    
}
