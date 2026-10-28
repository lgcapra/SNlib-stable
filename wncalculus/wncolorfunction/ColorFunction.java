package wncolorfunction;

import java.util.Collection;
import java.util.Map;

import bagexpr.BagExpr;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.ColorClass;
import color.Sort;
import expr.Domain;

/**
 * Super type for the linear-combination (i.e. multiset) of color functions family used as tuple components.
COMPLETARE 
*/
public interface ColorFunction extends ClassFunction, BagExpr<ElementaryFunction> {

    @Override
    default int splitDelim() {
            throw new UnsupportedOperationException("splitDelim is not supported for color functions");
    }

    @Override
    default Map<Sort, Integer> splitDelimiters() {
        return BagExpr.super.splitDelimiters();
    }


     @Override
    default LinearComb buildEmpty(Domain dom, Domain codom) {
        return new LinearComb((ColorClass) dom.support().iterator().next());
    }

    @Override
     default LinearComb build(Map<? extends ElementaryFunction, Integer> m) {
            return new LinearComb (m, false);
    }

    @Override
    default LinearComb build() {
         return new LinearComb(getSort());
    }

    /*  
    default Class<ColorFunction> type() {
        return ColorFunction.class;
     }*/

    @Override
    default  BagExpr<ElementaryFunction> buildScProd(BagExpr<ElementaryFunction> arg, int coeff) {
            return new LinearCombScalar((LinearComb) arg, coeff);
    }

     @Override
    default BagExpr<ElementaryFunction> buildSum(Collection<? extends BagExpr<ElementaryFunction>> c) {
            throw new UnsupportedOperationException("Unimplemented method 'buildSum'");
    }

    @Override
    public default BagExpr<ElementaryFunction> buildIntersection(Collection<? extends BagExpr<ElementaryFunction>> c) {
            throw new UnsupportedOperationException("Unimplemented method 'buildIntersection'");
    }

    @Override
    default boolean zeroCard() {
        var card = cardLb();
        return card != null && card == 0;
    }
      

}
