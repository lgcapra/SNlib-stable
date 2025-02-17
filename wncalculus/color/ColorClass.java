package color;

import java.util.*;
import expr.*;

/**
 * This class defines descriptors for SN color classes of (possibly) parametric
 * cardinality.
 * 
 * @author lorenzo capra
 */

public final class ColorClass extends Sort implements Color {

    private final boolean ordered;// default unordered, non split
    private final Interval[] constraints;// the constraints associated to the class, default [[2,>]]

    // cashing
    private Interval card; // the class cardinality
    private int paramSubcl = -1; // the parametric sub-interval index (if the class is split and if there is one,
                                 // default -1 means no param subcl)

    /**
     * base constructor: builds a non split color-class
     * 
     * @param name     the color class name
     * @param interval the associated constraint
     * @param ordered  the ordering flag
     */
    public ColorClass(String name, Interval interval, boolean ordered) {
        super(name);
        this.constraints = new Interval[] { interval };
        this.ordered = ordered;
    }

    /**
     * creates an unordered, non-split color class
     * 
     * @param name     the color class name
     * @param interval the associated constraint
     */
    public ColorClass(String name, Interval interval) {
        this(name, interval, false);
    }

    /**
     * creates a non split class of name C_i
     * 
     * @param ide      the class index (subscript)
     * @param interval the associated constraint
     * @param ordered  ordering flag
     */
    public ColorClass(int ide, Interval interval, boolean ordered) {
        this("C" + ide, interval, ordered);
    }

    /**
     * creates an unordered, non split class of name C_i
     * 
     * @param ide      the class index (subscript)
     * @param interval the associated constraint
     */
    public ColorClass(int ide, Interval interval) {
        this(ide, interval, false);
    }

    /**
     * builds a non split class with a default parametric constraint [2,&gt;]
     * 
     * @param name    the class name
     * @param ordered ordering flag
     */
    public ColorClass(String name, boolean ordered) {
        this(name, new Interval(2), ordered);
    }

    /**
     * builds an unordered, not split class with a default (parametric) constraint
     * 
     * @param name the class name
     */
    public ColorClass(String name) {
        this(name, false);
    }

    /**
     * builds an ordered, not split class "C_i" with a default (parametric)
     * constraint
     * 
     * @param ide     the class identifier (subscript)
     * @param ordered ordering flag
     */
    public ColorClass(int ide, boolean ordered) {
        this("C_" + ide, ordered);
    }

    /**
     * builds an unordered, non split, class "C_i" with a default parametric
     * constraint
     * 
     * @param ide the class identifier (subscript)
     */
    public ColorClass(int ide) {
        this(ide, false);
    }

    /**
     * creates a partitioned color class, with at most one parametric subclass
     * 
     * @param name      the class name
     * @param intervals the intervals associated to ubclasses
     * @throws NullPointerException if @param intervals is <code>null</code>
     */
    public ColorClass(String name, Interval[] intervals) {
        super(name);
        if (intervals.length < 2)
            throw new IllegalArgumentException("cannot create a split color class: at least two subclasses required");

        for (int j = 0; j < intervals.length; j++)
            if (intervals[j].lb() < 1)
                throw new IllegalArgumentException("cannot create a color class: subclass lower bound < 1");
            else if (!intervals[j].singleton()) { // parametric interval (subclass)
                if (this.paramSubcl >= 0)
                    throw new IllegalArgumentException("cannot create a color class: two parametric subclasses");

                this.paramSubcl = j; // we store the parametric interval index
            }

        this.constraints = intervals;
        this.ordered = false;
    }

    // overloaded version of the above constructor

    /**
     * creates a split class with name C_i
     * 
     * @param ide       the class index (subscript)
     * @param intervals the intervals associated with subclasses
     */
    public ColorClass(int ide, Interval[] intervals) {
        this("C_" + ide, intervals);
    }

    /**
     *
     * @return <code>true</code> if and only if <code>this</code> color-class is
     *         ordered
     */
    public boolean isOrdered() {
        return this.ordered;
    }

    /**
     * 
     * @return the number of static sublcasses of this color class; 1 if the class
     *         is not split
     */
    public int subclasses() {
        return this.constraints.length;
    }

    /**
     *
     * @return <code>true</code> if and only if <code>this</code> color-class is
     *         partitioned
     */
    public boolean isSplit() {
        return subclasses() > 1;
    }

    /**
     * 
     * @return the set of color's subclass indices
     */
    public Set<Integer> subclIndexSet() {
        Set<Integer> idxset = new HashSet<>();
        for (int i = 1; i <= subclasses(); i++)
            idxset.add(i);

        return idxset;
    }

    /**
     *
     * @return the constraints array of this color class
     */
    public Interval[] getConstraints() {
        return this.constraints;
    }

    @Override
    public boolean unbounded() {
        int x = 0;
        if (this.paramSubcl > 0)
            x = this.paramSubcl;

        return this.constraints[x].unbounded(); // efficient implementation
    }

    /**
     * 
     * @return <code>true</code> if and only if the class is parametric
     */
    public boolean parametric() {
        return this.paramSubcl >= 0 || !this.constraints[0].singleton();
    }

    /**
     * returns a copy of the (global) constraints associated to the color class:
     * in case of a split class, all static subclass constraints are considered
     * 
     * @return an interval expressing the (parametric) cardinality of the class
     *         constraint
     */
    @Override
    public Interval card() {
        if (this.card == null) {
            if (this.constraints.length == 1)
                this.card = this.constraints[0];
            else { // partitioned
                int lb = 0, ub = 0;
                for (Interval x : this.constraints) {
                    lb += x.lb();
                    ub += x.ub();
                }
                if (this.paramSubcl >= 0 && this.constraints[this.paramSubcl].unbounded())
                    this.card = new Interval(lb);
                else
                    this.card = new Interval(lb, ub);
            }
        }

        return this.card;
    }

    /**
     * in case of a split class returns the constraints associated to a color
     * subclass:
     * 
     * @param subcl a subclass index
     * @return an Interval (the subclass constraint)
     * @throws IndexOutOfBoundsException if the subclass index is not in the correct
     *                                   range
     */
    public Interval getConstraint(int subcl) {
        return this.constraints[subcl - 1];
    }

    /**
     * sets new constraints(s) for this (possibly split) color class
     * 
     * @param newconstr the new constraints
     * @return a copy of this colour class with the new constraints; null if the
     *         new constraints is not consistent with the old one in terms of
     *         fixedSize
     */
    public ColorClass setConstraint(Interval[] newconstr) {
        int l = newconstr.length;
        ColorClass c = null;
        if (l == this.constraints.length)
            c = l > 1 ? new ColorClass(name(), newconstr) : new ColorClass(name(), newconstr[0], this.ordered);

        return c;
    }

    /**
     * sets a new constraints for this color class
     * 
     * @param newconstr the new constraints
     * @return a copy of this color class with the new constraints; return null if
     *         the
     *         color class is split
     */
    public ColorClass setConstraint(Interval newconstr) {
        return setConstraint(new Interval[] { newconstr });
    }

    /**
     * 
     * @return a representation consistent with the parser
     */
    @Override
    public String toString() {
        String out;
        if (isSplit()) {
            out = this.constraints[0].toString(name() + '{' + "1" + '}');
            for (int i = 1; i < this.constraints.length; out += ", "
                    + this.constraints[i].toString(name() + '{' + ++i + '}')) {
            }
        } else
            out = this.constraints[0].toString(name());

        return out;
    }

    /**
     * performs a comparison between color classes, considering their constraints
     * 
     * @param o the other color class
     * @return <code>true</code> if and only if <code>this</code> and <code>o</code>
     *         are equal
     */
    @Override
    public boolean equals(Object o) {
        ColorClass c;
        return this == o || o instanceof ColorClass && (c = (ColorClass) o).name().equals(name()) &&
                this.ordered == c.ordered && Arrays.equals(this.constraints, c.constraints);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.ordered ? 1 : 0);
        hash = 11 * hash + name().hashCode();
        hash = 11 * hash + Arrays.deepHashCode(this.constraints);

        return hash;
    }

    /**
     * split this colour class given a delimiter for the associated constraint;
     * in case of a partitioned class, the only possible parametric subclass
     * constraint is split accordingly
     * 
     * @param delim a given split-delimiter
     * @return a boolean map (false := 0, true := 1) to the two color-classes
     * obtained from splitting the constraints <code>this</code> ;
     * an empty map no split is performed
     */
    @Override
    public Map<Boolean, Sort> split2(final int delim) {
        int toSplit = Math.max(0, this.paramSubcl);
        Interval[] split = this.constraints[toSplit].split(delim);// the interval to be split
        if (split.length != 0) {//split
            Map<Boolean, Sort> res = new HashMap<>();
            Interval[] newarrc = this.constraints.clone();// the original constraints is copied
            newarrc[toSplit] = split[0];
            res.put(false, setConstraint(newarrc)); // false trands for "0"
            newarrc = this.constraints.clone();
            newarrc[toSplit] = split[1];
            res.put(true, setConstraint(newarrc)); // true stands for "1"
            return res;
        }
        return Collections.emptyMap();
    }

    /**
     * (possibly) adjusts a pre-calculated split delimiter so that it complies
     * with the color class bounds
     * 
     * @param d a delimiter offset
     * @return a (possibly) ajusted value for d
     */
    public int setDelim(final int d) {
        return unbounded() ? d : Math.min(d, ub() - lb());
    }

    /**
     * @param nd a (new) split-delimiter
     * @param d  a split-delimiter
     * @param lb a (sort) lower bound
     * @return <code>true</code> iff nd (new delim) is geq lb (sort lb) and
     *         either d (current delim) is less than lb or nd is less than d
     */
    public static boolean lessDelim(int nd, int d, int lb) {
        return nd >= lb && (d < lb || nd < d);
    }

    /**
     * 
     * @param nd a split delimiter
     * @param d  a split delimiter
     * @param s  a sort
     * @return either nd, or d. depending n whether @see lessDelim(nd,d,lb)
     */
    public static int minSplitDelimiter(int nd, int d, Sort s) {
        return lessDelim(nd, d, s.lb()) ? nd : d;
    }

    /**
     * @param a a positive value
     * @param b a (positive) value
     * @return a if a is not null and a is less then b or b is null; b otherwise
     */
    public static int lessIf2ndNotZero(int a, int b) {
        return a > 0 && (a < b || b == 0) ? a : b;
    }

    /**
     * @param c a collection of parametric terms assumed of the same arity
     * @return the split delimiters for the collection of terms; builds on @see
     *         joinDelims
     * @throws NoSuchElementException if the collection is empty
     */
    public static Map<Sort, Integer> mergeSplitDelimiters(Collection<? extends ParametricExpr> c) {
        Iterator<? extends ParametricExpr> ite = c.iterator();
        Map<Sort, Integer> delims = ite.next().splitDelimiters();
        while (ite.hasNext())
            joinDelims(delims, ite.next().splitDelimiters());
        // System.out.println("merge delimiters outcome: " +delims); // debug
        return delims;
    }

    /**
     * performs a kind of "join" between sort-delimiters, modifying the first one:
     * if a given sort of the second map is not present in the first one then the
     * corresponding
     * entry is added; otherwise, the corresponding delimiter is set as the minimal
     * (considering the
     * sort constraint lower bound); it relies on <code>setDelim</code>
     * 
     * @param delims1 first support delimiter
     * @param delims2 second support delimiter
     */
    public static void joinDelims(Map<Sort, Integer> delims1, Map<Sort, Integer> delims2) {
        delims2.keySet().forEach(key -> {
            setDelim(delims1, key, delims2.get(key));
        });
    }

    /**
     * COOMENT TO UPDATE!
     * set a split-delimiter value in the specified map of sorts if the delimiter
     * value is geq the sort lb and:
     * the sort is not yet mapped, or the new value is less than the current one or
     * the latter is less than the sort lb
     * 
     * @param delims a pre-computed map of slit-delimiters
     * @param s      a sort
     * @param newdel the ned delimiter for the sort
     */
    public static void setDelim(Map<Sort, Integer> delims, Sort s, int newdel) {
        if (newdel > 0) {
            Integer curdel = delims.get(s);
            if (curdel == null || newdel < curdel)
                delims.put(s, newdel);
        }
    }

    @Override
    public ColorClass merge(Sort s) {
        ColorClass c = (ColorClass) s;
        for (int i = 0; i < this.constraints.length; i++) {
            Interval m = this.constraints[i].merge(c.constraints[i]);
            if (m != null) {
                Interval[] new_constr = Arrays.copyOf(this.constraints, this.constraints.length);
                new_constr[i] = m;

                return setConstraint(new_constr);
            }
        }

        return null;
    }

    @Override
    public ColorClass getSort() {
        return this;
    }

    /**
     * 
     * @param offset an integral value
     * @return <code>true</code> if and only if this constraint fits the specified
     *         offset
     */
    public boolean fit(final int offset) {
        return card().fit(offset);
    }

}
