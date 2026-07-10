package wncolorfunction;

import java.util.Map;

import bagexpr.BagExpr;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.Sort;

/**
 * Super type for the linear-combination (i.e. multiset) of color functions family used as tuple components.
 */
public interface ColorFunction extends ClassFunction, BagExpr<ElementaryFunction> {

    default Map<Integer, Map<ElementaryFunction, Integer>> components() { // To do
           throw new UnsupportedOperationException("components() is not supported for color functions");
    }

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

     //public default Class<ColorFunction> type() {
     //   return ColorFunction.class;
     //}
    

}
