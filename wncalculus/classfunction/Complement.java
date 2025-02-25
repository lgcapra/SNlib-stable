package classfunction;

import java.util.Objects;
import logexpr.NotOp;
import expr.Interval;

/**
 * this class implements the "complementary" functional operator for
 * ClassFunction its semantics is : Complement(f(X)) \equiv S - f(X)
 * @author lorenzo capra
 */
public final class Complement extends UnaryClassOp implements NotOp<SetFunction> {

    private Complement(final SetFunction f) {
        super(f);
    }

    /**
     * build the complement of a given function
     * @param f a class-function mapping to a set
     * @return the <code>Complement</code> of the class-function; if the function is
     * elementary directly computes the result
     */
    public static SetFunction factory(final SetFunction f) { //optimization: all specific reductions done at this level
        SetFunction res;
        switch (f) {
            case Projection projection -> res = (SetFunction) ProjectionComp.factory(projection);
            case ProjectionComp projectionComp -> res = projectionComp.getArg();
            case Subcl subcl -> res = subcl.opposite();
            default -> res = new Complement(f);
        }
        return res;
    }

    @Override
    public SetFunction buildOp(final SetFunction arg) {
        return factory(arg);
    }

    @Override
    public String symb() {
        return "comp";
    }

    /**
     * smart implementation which allows card to be computed either if the
     * argument's cardinality is fixed or corresponds to the color class cardinality
     * @return the (possibly) parametric cardinality of <code>this</code>
     */
    @Override
    public Interval card() {
        final var mycard = getArg().card();
        if (mycard != null) {
            final var in = getConstraint();
            if (mycard.equals(in)) {
                return new Interval(0, 0);
            } else {
                final Integer card = mycard.singleValue();
                if (card != null) {
                    return in.unbounded() ? new Interval(in.lb() - card) : new Interval(in.lb() - mycard.lb(), in.ub() - card);
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return NotOp.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        var hash = 3;
        hash = 59 * hash + Objects.hashCode(getArg());
        return hash;
    }

}
