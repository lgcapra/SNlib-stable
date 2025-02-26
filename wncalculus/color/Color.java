package color;

import expr.Interval;
import expr.Sort;

/**
 * this interface defines a common super-type for color-classes
 * and static subclasses
 * @author Lorenzo Capra
 */
public interface Color {
    
    /**
     * @return the cardinality of this colour (sub)class 
     */
    Interval card ();
    
    /**
     * @return corresponding sort 
     */
    Sort getSort();
    
}
