package wncolorfunction;
import java.util.Collection;
import bagexpr.BagExpr;
import bagexpr.BagSum;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.ColorClass;
import expr.Domain; 

public final class LinearCombSum extends BagSum<ElementaryFunction> implements ColorFunction  {

    public LinearCombSum(Collection<? extends LinearComb> terms) {
        super(terms, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public LinearCombSum buildOp(Collection<? extends BagExpr<ElementaryFunction>> args) {
        return new LinearCombSum((Collection<? extends LinearComb>) args);
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

    @Override
    public LinearComb clone(Domain newdom) {
        throw new UnsupportedOperationException("Unimplemented method 'clone'");
    }

    @Override
    public LinearComb setIndex(int idx) {
        throw new UnsupportedOperationException();
    }
}
