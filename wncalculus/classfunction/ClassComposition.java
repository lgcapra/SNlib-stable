package classfunction;

import java.util.*;
import expr.*;
import color.ColorClass;
import logexpr.LogComposition;

/**
 * @author Lorenzo Capra this class defines basic compositions between
 * ClassFunctions the left function is assumed unary
 */
public final class ClassComposition extends SetFunction implements LogComposition<SetFunction> {

    private final SetFunction left, right;
    // the following field may be set by the baseCompose method
    private Integer delim;

    /**
     * creates a new basic-composition between class-functions after having
     * possibly checked that the left one is unary
     *
     * @param left the left operand
     * @param right the right operand
     * @param check the operands "check" flag (same color-class and not
     * multi-index left)
     * @throws IllegalDomain IllegalArgumentException
     */
    public ClassComposition(final SetFunction left, final SetFunction right, final boolean check) {
        if (check) {
            if (!left.getSort().equals(right.getSort())) {
                throw new IllegalDomain(left + " (" + left.getSort() + ") " + right + " (" + right.getSort() + ")");
            }
            if (left.indexSet().size() > 1) {
                throw new IllegalArgumentException("the left function must hold at most one index!");
            }
        }
        this.left = left;
        this.right = right;
    }

    /**
     * creates a new basic-composition between class-functions assuming that the
     * left one is unary
     *
     * @param left left-composed function
     * @param right right-composed function
     */
    public ClassComposition(final SetFunction left, final SetFunction right) {
        this(left, right, false);
    }

    @Override
    public ClassComposition buildOp(final SetFunction left, final SetFunction right) {
        return new ClassComposition(left, right);
    }

    @Override
    public ColorClass getSort() {
        return this.left.getSort();
    }

    // the following two redefinitions are only needed because the methods are
    // doubly inherithed from two interfaces
    @Override
    public boolean equals(Object o) {
        return LogComposition.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.left);
        hash = 59 * hash + Objects.hashCode(this.right);
        return hash;
    }

    @Override
    public Interval card() {
        return null;
    }

    @Override
    public SetFunction specSimplify() {
        if (this.right.zeroCard()) {
            return Empty.getInstance(getSort()); // optimization(may be removed)
        } else {
            final var compres = this.left.baseCompose(this.right);
            if (compres != null) {
                if (compres.getKey() != null) {
                    return compres.getKey();
                }
                if (compres.getValue() != null) {
                    this.delim = compres.getValue();
                }
            }
            return this;
        }
    }

    @Override
    public SetFunction left() {
        return this.left;
    }

    @Override
    public SetFunction right() {
        return this.right;
    }

    @Override
    public boolean isLeftAssociative(final Class<? extends SingleArg> optk) {
        return optk.equals(Successor.class);
    }

    @Override
    public Set<Integer> indexSet() {
        return this.right.indexSet();
    }

    /**
     * @return the split delimiter of <code>this</code> function optimized
     * version: it avoids unnecessary split when the composition result may be
     * inferred
     */
    @Override
    public final int splitDelim() {
        if (this.delim != null) {
            return this.delim;
        } else {
            var splitdel = this.right.splitDelim();
            if (!this.left.isConstant()) {
                splitdel = ColorClass.lessIf2ndNotZero(this.left.splitDelim(), splitdel); // no optimization..
            }        
            //System.out.println("splitdel: ("+this+") "+splitdel); //debug
            return this.delim = splitdel;
        }

    }

    @Override
    public ClassFunction setDefaultIndex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return LogComposition.super.toStringOp();
    }

    @Override
    public SetFunction copy(final ColorClass newcc) {
        return new ClassComposition(this.left.copy(newcc), this.right.copy(newcc));
    }

    @Override
    public SetFunction clone(final Domain newdom) {
        return (SetFunction) super.clone(newdom);
    }

    // needed because it inherits two default methods
    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return super.splitDelimiters();
    }

    protected final void checkNeutral() {
        if (getSort().neutral()) {
            throw new IllegalArgumentException("Only All an Empty may have a neutral colour");
        }
    }

}
