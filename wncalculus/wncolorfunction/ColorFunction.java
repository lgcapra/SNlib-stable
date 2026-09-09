package wncolorfunction;

import java.util.Collection;
import java.util.Map;

import bagexpr.BagExpr;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.Sort;

/**
 * Super type for the linear-combination (i.e. multiset) of color functions family used as tuple components.
COMPLETARE 
*/
public interface ColorFunction extends ClassFunction, BagExpr<ElementaryFunction> {

    @Override
    public default int splitDelim() {
            throw new UnsupportedOperationException("splitDelim is not supported for color functions");
    }

    @Override
    default Map<Sort, Integer> splitDelimiters() {
        return BagExpr.super.splitDelimiters();
    }

    @Override
    public default LinearComb build() {
         return new LinearComb(getSort());
    }

     public default Class<ColorFunction> type() {
        return ColorFunction.class;
     }

        @Override
        public default  BagExpr<ElementaryFunction> buildScProd(BagExpr<ElementaryFunction> arg, int coeff) {
            return new LinearCombScalar((LinearComb) arg, coeff);
        }

     @Override
        public default BagExpr<ElementaryFunction> buildSum(Collection<? extends BagExpr<ElementaryFunction>> c) {
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
