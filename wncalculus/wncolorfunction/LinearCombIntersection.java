package wncolorfunction;

import bagexpr.*;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.ColorClass;
import expr.Domain;

import java.util.Collection;
import java.util.Map;

/**
 * Concrete intersection operator for color-function bags (linear combinations).
 */
public final class LinearCombIntersection extends BagIntersection<ElementaryFunction> implements ColorFunction {

    public LinearCombIntersection(Collection<? extends LinearComb> terms) {
        super(terms, true);
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
    @SuppressWarnings("unchecked")
    public LinearCombIntersection buildOp(Collection<? extends BagExpr<ElementaryFunction>> args) {
        return new LinearCombIntersection((Collection<? extends LinearComb>) args);
    }

    @Override
    public ColorClass getSort() {
        return ((ColorFunction) getArgs().iterator().next()).getSort();
    }

    @Override
    public <E extends ClassFunction> E copy(ColorClass newcc) {
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public <E extends ClassFunction> E setDefaultIndex() {
        throw new UnsupportedOperationException("Unimplemented method 'setDefaultIndex'");
    }

    // TO DO
    @Override
    public <E extends ClassFunction> E setIndex(int idx) {
        throw new UnsupportedOperationException("Unimplemented method 'setIndex'");
    }

    @Override
    public LinearComb clone(Domain newdom) {
        throw new UnsupportedOperationException("Unimplemented method 'clone'");
    }
}
