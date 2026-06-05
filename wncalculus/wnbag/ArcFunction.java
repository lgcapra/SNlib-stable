package wnbag;


import bagexpr.BagExpr;
import tuple.FunctionTuple;

/**
 * the super type of SN arc functions, i.e., bags of SN function-tuples
 * @author lorenzo
 */
public interface ArcFunction extends BagExpr<WNtuple> {
    
    /*
    return the set-function corresponding to this arc-function, if it exists; otherwise, return null
    should be overridden by all arc-functions that can be interpreted as set-functions
     */
    default FunctionTuple  asSetFunction() {
        return null;
    }   
}
