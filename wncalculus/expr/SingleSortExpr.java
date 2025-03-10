package expr;

import java.util.*;
import color.ColorClass;

/**
 * this interface is represents expressions syntactically composed of terms of the same sort
 * @author Lorenzo Capra
 */
public interface SingleSortExpr extends ParametricExpr {
    
    /**
     * @return <code>this</code> term's sort; <code>null</code> if
     * <code>this</code> term is many-sorted
     */
    Sort getSort() ;
     
     /**
     * @return the term's sort's split-delimiter
     */
    int splitDelim();
    

    @Override
    default Map<Sort,Integer> splitDelimiters () {
        final Map<Sort,Integer> map = new HashMap<>();
        ColorClass.setDelim(map, getSort(), splitDelim());        
        return map;
    }
    
}
