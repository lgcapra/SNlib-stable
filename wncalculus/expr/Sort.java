package expr;

import java.util.*;

/**
 * this abstract class is the super-type for any kind of possibly parametric sort (e.g., a colour class)
 * @author Lorenzo Capra
 */
public abstract class Sort implements Comparable<Sort> {

    private final String name; // sort's name
     
     /**
     base constructor
     @param name the sort's name: the first character must be an upper-case letter
     (no other check is done)
     @throws NullPointerException if the name is null
     @throws IllegalArgumentException if the name doesn't start with an upper-case letter
     */
    public Sort( String name) {
        if (name== null) 
            throw new NullPointerException("the sort name must be indicated!");
        
        if (!Character.isUpperCase(name.charAt(0))) 
            throw new IllegalArgumentException("the initial letter of sort's name must be upper case!");
        
        this.name = name;
    }
    
    /** 
     @return the name of this sort
     */ 
     public final String name() {
         return this.name;
     }
     
     /**
      * checks whether this sort and the specified one share the name
      * @param s a sort 
      * @return true iff this sort and the specified one share the name
      */
     public final boolean sameName(Sort s) {
         return this.name.equals(s.name);
     }
     
     /** 
     * performs a comparison between sorts first based on their names,
     * then on their constraints
     * it is not <code>equals</code>-equivalent, nontheless this implementation
     * fully matches the assumption that domains should be composed of sorts with
     * different names 
     * @param s a specified sort
     * @return the result of the comparison between <code>this</code> and the specified sort
     */
    @Override
    public final int compareTo(Sort s) {
        int cmp = this.name.compareTo(s.name);
        if (cmp == 0)
            cmp = this.card().compareTo(s.card());
        
        return cmp;
    }
 
     /**
     * @return the (possibly parametric) cardinality expressed as an interval
     */
    public abstract Interval card ();
    
    
    /**
     * split this sort, if possible (in the case of a parametric term)
     * @param delim the split delimiter (should be greater than or equal to the lower bound
     * of sort's cardinality)
     * @return a boolean map to the two sorts resulting from the split of this;
     * an empty map if no split is done (for any reasons)
     */
    public abstract Map<Boolean, Sort> split2 (int delim);
    
    /**
     * "merges" the constraint of <code>this</code> and a given sorts;
     * operates in a non destructive way and assumes that sorts are compatible
     * @param s a sort
     * @return the sort resulting from merging <code>this</code>,
     * <code>null</code> if no merge is possible
     * @throws IndexOutOfBoundsException if the sorts are not compliant to one another
     */
    public abstract Sort merge (Sort s);
    
    
    /**
     * 
     * @return the sort lower bound 
     */
    public final int lb () {
        return card().lb();
    }
    
    /**
     * 
     * @return the sort upper bound
     */
    public final int ub () {
        return card().ub();
    }
    
     /**
     *
     * @return <code>true</code> if and only if the sorts' constraint is unbounded
     */
    public boolean unbounded() {
        return card().unbounded();
    }
    
    /**
     *
     * @return a positive int, corresponding to the interval fixedSize,
     * if the colour-class is not parametric; -1 otherwisee
     */
    public final int fixedSize() {
        Integer card = card().singleValue();
        return card == null ? -1 : card;
    }
    
    /**
     * 
     * @return <code>true</code> if and only the sort size is constant 
     */
    public boolean hasFixedSize() {
        return fixedSize() >= 0;
    }
    
     @Override
    public String toString () {
        return this.name;
    }
        
}
