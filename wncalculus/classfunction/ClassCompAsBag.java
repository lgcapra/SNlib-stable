package wncalculus.classfunction;

import wncalculus.bagexpr.SetComp;

/**
 * this class represents the bag-composition between set-functions
 * @author lorenzo cape
 */
public class ClassCompAsBag extends SetComp<SetFunction>{
    
    public ClassCompAsBag(SetFunction left, SetFunction right) {
        super(left, right);
    }

    @Override
    public ClassCompAsBag buildOp(SetFunction left, SetFunction right) {
        return new ClassCompAsBag(left,right);
    }
    
}
