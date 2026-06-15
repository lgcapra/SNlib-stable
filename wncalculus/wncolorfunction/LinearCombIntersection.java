package wncolorfunction;

import bagexpr.BagExpr;
import bagexpr.BagIntersection;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.ColorClass;
import expr.Domain;

import java.util.Map;

/**
 * Concrete intersection operator for color-function bags (linear combinations).
 */
public final class LinearCombIntersection extends BagIntersection<ElementaryFunction> implements ColorFunction {

    public LinearCombIntersection(ColorFunction left, ColorFunction right) {
        super(left, right);
    }

    @Override
    public LinearCombIntersection buildOp(BagExpr<ElementaryFunction> left, BagExpr<ElementaryFunction> right) {
        return new LinearCombIntersection((ColorFunction) left, (ColorFunction) right);
    }

    @Override
    public LinearCombIntersection buildIntersection(BagExpr<ElementaryFunction> left, BagExpr<ElementaryFunction> right) {
        return new LinearCombIntersection((ColorFunction) left, (ColorFunction) right);
    }

    @Override
    public LinearComb build(Map<? extends ElementaryFunction, Integer> m) {
        return new LinearComb(m);
    }

    @Override
    public LinearComb buildEmpty(Domain dom, Domain codom) {
        return new LinearComb((ColorClass) dom.support().iterator().next());
    }

    @Override
    public ColorClass getSort() {
        return ((ColorFunction) left()).getSort();
    }

    @Override
    public <E extends ClassFunction> E copy(ColorClass newcc) {
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public <E extends ClassFunction> E setDefaultIndex() {
        throw new UnsupportedOperationException("Unimplemented method 'setDefaultIndex'");
    }

    @Override
    public LinearCombIntersection clone(Domain newdom) {
        BagExpr<ElementaryFunction> l = left().clone(newdom).cast();
        BagExpr<ElementaryFunction> r = right().clone(newdom).cast();
        return new LinearCombIntersection((ColorFunction) l, (ColorFunction) r);
    }

}
