package wncalculus.tuple;

import wncalculus.bagexpr.SetComp;

/**
 * this class defines the bag-composition between <tt>FunctionTuple</tt>s 
 * @author lorenzo capra
 */
public final class TupleBagComp extends SetComp<FunctionTuple> {
    
    public TupleBagComp(FunctionTuple left, FunctionTuple right) {
        super(left, right);
    }
    
    @Override
    public TupleBagComp buildOp(FunctionTuple left, FunctionTuple right) {
        return new TupleBagComp(left,right);
    }
    
}
