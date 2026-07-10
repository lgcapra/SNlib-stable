package wncolorfunction;

import bagexpr.BagExpr;
import bagexpr.ScalarProd;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.ColorClass;
import color.Sort;
import expr.Domain;
import expr.Expression;

import java.util.Map;

public final class LinearCombScalar extends ScalarProd<ElementaryFunction> implements ColorFunction {

    public LinearCombScalar(LinearComb f, int coeff) {
        super(f, coeff);
    }

    @Override
    public LinearCombScalar buildOp(BagExpr<ElementaryFunction> arg) {
        return buildScProd(arg, this.k);
    }

    @Override
    public LinearCombScalar buildScProd(BagExpr<ElementaryFunction> arg, int coeff) {
        return new LinearCombScalar((LinearComb) arg, coeff);
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
     public Map<Sort, Integer> splitDelimiters() {
            return super.splitDelimiters();
    }

    @Override
    public ColorClass getSort() {
       return ((ClassFunction) getArg()).getSort();
    }

    @Override
    public <E extends ClassFunction> E copy(ColorClass newcc) {
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public <E extends ClassFunction> E setDefaultIndex() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDefaultIndex'");
    }

    @Override
    public LinearCombScalar clone(Domain newdom) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clone'");
    }
}
