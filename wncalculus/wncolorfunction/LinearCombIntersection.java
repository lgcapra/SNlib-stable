package wncolorfunction;

import bagexpr.*;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.ColorClass;
import expr.Domain;
import expr.Expression;
import wnbag.ArcFunIntersection;
import wnbag.ArcFunction;
import wnbag.WNtuple;

import java.util.Collection;
import java.util.Map;

/**
 * Concrete intersection operator for color-function bags (linear combinations).
 */
public final class LinearCombIntersection extends BagIntersection<ElementaryFunction> implements ColorFunction {

    public LinearCombIntersection(Collection <? extends ArcFunction> c, boolean check) {
        super((Collection<? extends BagExpr<ElementaryFunction>>) c, check);
    }

    @Override
    public LinearCombIntersection buildOp(Collection<? extends BagExpr<ElementaryFunction>> args) {
        return new LinearCombIntersection((Collection<? extends ArcFunction>) args, true);
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
    public Bag<ElementaryFunction> build(Map<? extends ElementaryFunction, Integer> m) {
        return null;
    }

    @Override
    public Bag<ElementaryFunction> buildEmpty(Domain dom, Domain codom) {
        return null;
    }

    @Override
    public <E extends Expression> Class<E> type() {
        return (Class<E>) ColorFunction.class;
    }
}
