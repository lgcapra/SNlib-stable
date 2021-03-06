package wncalculus.classfunction;

import wncalculus.bagexpr.SetComp;
import wncalculus.expr.Domain;
import wncalculus.expr.IllegalDomain;
import wncalculus.expr.ParametricExpr;

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

    @Override
    public ClassCompAsBag clone(Domain newdom) {
        if (newdom.mult(oneSorted()) != 0)
            return this;
        else
            throw new IllegalDomain(); 
    }
    
}
