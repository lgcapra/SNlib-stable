package color;

import java.util.*;
import classfunction.Subcl;
import expr.*;

/**
 * this class represents a color type defined by a (proper, non-empty) subset of subclasses
 * of a given split class
 * @author lorenzo capra
 */
public final class SubclSet implements Color {
    private final Set<? extends Subcl> subclasses;
    private final ColorClass cc; // the corresponding color class
    //cache
    private Interval card;
    
    /**
     * build a color type from a proper, non empty subset of static subclasses
     * @param set a set of static subclasses
     * @param check domain check flag
     * @throws IllegalDomain if the check flag is set and subclasses refer to different colors
     * @throws NoSuchElementException if the set is empty
     * @throws IllegalArgumentException if the set size is equal to the class partition size
     */
    public SubclSet (final Set<? extends Subcl> set, final boolean check) {
        if (check) 
            Expressions.checkDomain(set);
        this.cc = set.iterator().next().getSort();
        if (set.size() == this.cc.subclasses())
            throw new IllegalArgumentException("the subclass set corresponds to the whole class!");
        
        this.subclasses = Collections.unmodifiableSet(set);
    }
    
    /**
     * build a color type from a proper, non empty subset of static subclasses,
     * without doing any check on their colors
     * @param set a set of static subclasses
     */
    public SubclSet (final Set<? extends Subcl> set) {
        this(set, false);
    }
    
    /**
     * build a color type corresponding to a single static subclass
     * @param sc a static subclass
     */
    public SubclSet (final Subcl sc) {
        this(Collections.singleton(sc));
    }
    
    @Override
    public Interval card() {
        if (this.card == null) {
            Iterator<? extends Subcl> it = this.subclasses.iterator();
            this.card = it.next().card();
            while (it.hasNext())
                this.card = this.card.sum(it.next().card());
        }
        
        return this.card;
    }
    
    @Override
    public boolean equals (Object o) {
        return o instanceof SubclSet && this.subclasses.equals( ((SubclSet)o).subclasses);
    }

    @Override
    public int hashCode() {
        var hash = 7;
        hash = 19 * hash + Objects.hashCode(this.subclasses);
        return hash;
    }

    @Override
    public Sort getSort() {
        return this.cc;
    }
    
    @Override
    public String toString() {
        return this.subclasses.toString();
    }
    
}
