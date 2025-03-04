package classfunction;

import expr.*;
import color.*;
import java.util.*;
import guard.Equality;

/**
 * This interface represents the root of the hierarchy describing SN functions
 * mapping on a color class, i.e., tuple components. Its concrete
 * implementations meet the interpreter pattern. ClassFunction objects are
 * data-objects
 * @author Lorenzo Capra
 */
public interface ClassFunction extends SingleSortExpr {

    /**
     * computes the (min) split-delimiter for a collection of (homogenous)
     * ClassFunction, considering both the split-delimiters of terms and the
     * (max positive/negative) successor bounds of projection symbols (in the
     * event of an ordered colour-class)
     * @param terms a collection of functions
     * @param s the collection's (ordered) colour-class
     * @return the collection's split-delimiter
     */
    public static int splitDelim(final Collection<? extends ClassFunction> terms, final ColorClass s) {
        var delim = 0;
        for (ClassFunction f : terms) {
            if (!(f instanceof ProjectionBased)) //optimization
            {
                delim = ColorClass.lessIf2ndNotZero(f.splitDelim(), delim);
            }
        }
        //we find out the max offset between positive and negative successors
        if (s.isOrdered()) {
            int max_succ = 0, min_succ = 0; //the "max" (positive/neg.) split delimiters for terminal symbols
            for (ClassFunction f : terms) {
                if (f instanceof ProjectionBased projectionBased) {
                    int succ = projectionBased.getSucc();
                    if (succ > 0) {
                        max_succ = Math.max(max_succ, succ);
                    } else if (succ < 0) {
                        min_succ = Math.min(min_succ, succ);
                    }
                }
            }
            delim = ColorClass.lessIf2ndNotZero(max_succ - min_succ + 1 - s.lb(), delim);// the offset between successors of projection-terms
        }
        return delim;
    }

    /**
     * @return the class-function's colour class
     */
    @Override
    ColorClass getSort();

    /**
     * @return the general color constraint associated with <code>this</code>
     * function
     */
    default Interval getConstraint() {
        return getSort().card();
    }

    /**
     * static version of indexSet working on collections
     * @param c a given collection of functions
     * @return the overall collection's index set
     */
    public static Set<Integer> indexSet(final Collection<? extends ClassFunction> c) {
        final Set<Integer> idxset = new HashSet<>();
        c.forEach(f -> {
            idxset.addAll(f.indexSet());
        });
        return idxset;
    }

    /**
     * creates a clone of <code>this</code> class-function, of a colour which is
     * assumed compatible with the current one, in an optimized way builds on
     * @see {copy}
     * @param newcc a colour
     * @param <E> the type to which the term is "casted"
     * @return a (casted) copy of <code>this</code>, or <code>this</code> if the
     * specified colour coincides with the current one
     */
    default <E extends ClassFunction> E clone(final ColorClass newcc) {
        return getSort().equals(newcc) ? cast() : copy(newcc);
    }

    @Override
    default ClassFunction clone(final Domain newdom) {
        if (newdom.mult(getSort()) == 0) {
            throw new IllegalDomain("impossible cloning: null sort");
        }
        return this;
    }

    /**
     * creates a copy of <code>this</code> class-function with another colour
     * @param <E> the type to which the term is "casted"
     * @param newcc a new colour
     * @return a copy of <code>this</code>
     */
    abstract <E extends ClassFunction> E copy(final ColorClass newcc);

    /**
     * creates a copy of given a collection of <code>ClassFunction</code>s with
     * a given new color; the type of returned collection (either a list or a
     * set) is the same as the passed one
     * @param <E> the type of collection's elements (either set- or bag-functions)
     * @param arglist a collection of class-functions
     * @param newsort the new color of functions
     * @return a cloned collection of terms with a new color-class
     */
    static <E extends ClassFunction> Collection<E> copy(final Collection<? extends E> arglist, final ColorClass newsort) {
        final Collection<E> res = arglist instanceof Set ? new HashSet<>() : new ArrayList<>();
        arglist.forEach((var t) -> {
            res.add(t.copy(newsort));
        });
        return res;
    }

    /**
     * replaces projection symbols in @code{this} function according to a given equality
     * @param <E> the function's type
     * @param eq an equality
     * @return a function corresponding to <code>this</code>, modulo a
     * replacement of symbols, according to the equality default implementation
     * - to override if needed
     */
    default <E extends ClassFunction> E replace(final Equality eq) {
        return cast();
    }

    /**
     * destructively replaces in the specified list of functions (assumed
     * homogeneous) each the occurrences of symbols, according to a given equality
     * @param <E> function's elements type
     * @param lf the specified list of functions
     * @param e an equality
     * @return <code>true</code> if and only if the list is modified
     */
    public static <E extends ClassFunction> boolean replace(final List<E> lf, final Equality e) {
        var replaced = false;
        for (ListIterator<E> ite = lf.listIterator(); ite.hasNext();) {
            final E f = ite.next(), pf;
            if (f.getSort().equals(e.getSort()) && (pf = f.replace(e)) != f) {
                replaced = true;
                ite.set(pf);
            }
        }
        return replaced;
    }

    /**
     * sets to one the index for all projections appearing in this term should
     * be invoked on single-index functions, even if no check is done!
     * @param <E> the function's type
     * @return a copy of this term with the new index or <code>this</code> if
     * the index is the same as the current one or the function is a constant
     */
    <E extends ClassFunction> E setDefaultIndex();

    /**
     * given an iterator over ClassFunction, builds a corresponding list in
     * which all components are assigned the specified projection index
     * @param <E> the function's type
     * @param ite the collection of class-functions
     * @return a list of class-functions with the new projection index
     */
    public static <E extends ClassFunction> List<E> setDefaultIndex(final Iterable<? extends E> ite) {
        final List<E> new_list = new ArrayList<>();
        ite.forEach(f -> {
            new_list.add(f.setDefaultIndex());
        });
        return new_list;
    }

    /**
     * checks whether a class-function is "ordered"
     * @param <E> the type of function
     * @param f a class-function
     * @return the function, if it is defined on an ordered class
     * @throws IllegalArgumentException if the function's color-class is not
     * ordered
     */
    public static <E extends ClassFunction> E checkOrdered(final E f) {
        if (f.getSort().isOrdered()) {
            return f;
        }
        throw new IllegalArgumentException("the Successor cannot be applied to unordered color class");
    }

    /**
     * @return the set of projection indexes occurring on this class-function
     */
    default Set<Integer> indexSet() {
        return Collections.emptySet();
    }

    /**
     * 
     * @return the color class split delimiter due to the successor
     * @throws UnsupportedOperationException if the method is not overwritten
     */
    /*default*/ int succDelim() ;
    /*  {
        if (getSort().isOrdered()) {
            throw new UnsupportedOperationException("succDelim should be redefined");
        }
        return 0;
    }*/

}
