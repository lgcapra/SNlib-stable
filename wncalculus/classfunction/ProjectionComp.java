package classfunction;

import java.util.*;
import color.ColorClass;
import expr.Interval;
import expr.Sort;
import guard.Equality;
import util.ComplexKey;
import util.Pair;

/**
 * this class defines the Projection complement function
 * @author lorenzo capra
 */
public final class ProjectionComp extends ProjectionBased {

    private final Projection pr;
    private static final Map<ComplexKey, ProjectionComp> VALUES = new HashMap<>();

    /**
     * @param index the projection's index
     * @param exp the successor argument
     * @param cc the projection's color
     */
    private ProjectionComp(final Projection arg) {
        super(arg.getSort());
        this.pr = arg;
    }

    /**
     * base factory method: builds the complement of a projection
     * @param pr the projection
     * @return either a projection complement, or a projection, if the color
     * class of <code>pr</code> is ordered and of (fixed) card two
     */
    public static ProjectionBased factory(final Projection pr) {
        final ColorClass cc = pr.getSort();
        if (cc.isOrdered() && cc.fixedSize() == 2) {
            return pr.setExp(pr.getSucc() == 0 ? 1 : 0);
        } else {
            ProjectionComp pc;
            ComplexKey k = new ComplexKey(cc, pr);
            if ((pc = VALUES.get(k)) == null) {
                VALUES.put(k, pc = new ProjectionComp(pr));
            }
            return pc;
        }
    }

    @Override
    public int getIndex() {
        return this.pr.getIndex();
    }

    @Override
    public int getSucc() {
        return this.pr.getSucc();
    }

    /**
     * @return the <code>Projection</code> complemented
     */
    public Projection getArg() {
        return this.pr;
    }

    /**
     * @return the constraint [lrb-1,x], where x is either infinity or ub-1
     */
    @Override
    public Interval card() {
        final var in = getConstraint();
        return in.unbounded() ? new Interval(in.lb() - 1) : new Interval(in.lb() - 1, in.ub() - 1);
    }

    @Override
    public Pair<SetFunction, Integer> baseCompose(final SetFunction right) {
        final var rcard = right.card();
        if (rcard != null) {
            if (rcard.lb() > 1) {
                return new Pair<>(All.getInstance(getSort()), null);
            }
            if (rcard.lb() == 1) {
                if (rcard.ub() == 1) {
                    return new Pair<>(Complement.factory(Successor.factory(getSucc(), right)), null);
                }
                //System.err.println("cannot solve "+this+'.'+right.toStringDetailed()); //debug
                return new Pair<>(null, 1); //split required
            }
        }
        return super.baseCompose(right);
    }

    @Override
    public int splitDelim() {
        final var c = getSort();
        return c.lb() == 2 && c.ub() != 2 ? 2 : this.pr.splitDelim(); // the function cardinality is 1 and the constraint is [2,..]
    }

    /**
     * replaces the variable of @code{this} function according to the specified
     * equality assuming that: 1) the color-class is the same, 2) the equality
     * is not "trivial" (the members have different indices)
     * @param eq an equality
     * @return the function corresponding to @code{this}, modulo the replacement
     * induced by the equality; @code{this} is the second index of the equality
     * coincides with @code{this} index
     */
    @Override
    public SetFunction replace(final Equality eq) {
        final Projection p = this.pr.replace(eq);
        return p == this.pr ? this : factory(p).cast();
    }

    @Override
    public ProjectionComp setDefaultIndex() {
        final Projection p = this.pr.setDefaultIndex();
        return p == this.pr ? this : (ProjectionComp) factory(p);
    }

    @Override
    public String toString() {
        return "S-" + this.pr;
    }

    @Override
    public Projection opposite() {
        return this.pr;
    }

    @Override
    public SetFunction copy(final ColorClass newcc) {
        return factory(this.pr.copy(newcc)).cast();
    }

    @Override
    public final ProjectionBased clone(final Map<Sort, Sort> split_map) {
        Projection p = (Projection) this.pr.clone(split_map);
        return p == this.pr ? this : factory(p);
    }

    @Override
    public Set<Integer> indexSet() {
        return this.pr.indexSet();
    }

}
