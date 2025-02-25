package classfunction;

import bagexpr.SetComp;
import expr.Domain;
import expr.IllegalDomain;

/**
 * this class represents the bag-composition between set-functions
 *
 * @author lorenzo cape
 */
public class ClassCompAsBag extends SetComp<SetFunction> {

    public ClassCompAsBag(final SetFunction left, final SetFunction right) {
        super(left, right);
    }

    @Override
    public ClassCompAsBag buildOp(final SetFunction left, final SetFunction right) {
        return new ClassCompAsBag(left, right);
    }

    @Override
    public ClassCompAsBag clone(final Domain newdom) {
        if (newdom.mult(oneSorted()) == 0) {
            throw new IllegalDomain();
        }
        return this;
    }

}
