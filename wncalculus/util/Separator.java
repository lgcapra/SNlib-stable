package util;

/**
 * this interface defines a generic separator for objects of a given type E, i.e.,
 * a command that allows to distinguish (separate) homogeneous elements of E
 * @author Lorenzo Capra
 * @param <E> the objects type
 */
public interface Separator<E> {
    
    /**
     * this methods returns true iff o1 and o2 are similar, according to some criteria
     * @param o1 the first object
     * @param o2 the second object
     * @return true if and only if o1 and o2 are similar
     */
    boolean similar (E o1, E o2);
}
