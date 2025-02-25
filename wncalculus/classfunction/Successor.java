package classfunction;

import java.util.Objects;
import expr.*;

/**
 * this class implements the (k-th) successor unary functional operator it
 * cannot be considered neither n-ary nor unary
 *
 * @author lorenzo capra
 */
public final class Successor extends UnaryClassOp {

    private final int exp;//operator's exponent
    private final static String OPSYMB = "!";

    private Successor(final int k, final SetFunction f) {
        super(ClassFunction.checkOrdered(f));
        this.exp = k;
    }

    /**
     * builds the k-th successor of a given function- performing a number of
     * reductions e.g., the 0-th successor corresponds to the function itself,
     * the successor of a constant is the constant..
     *
     * @param k the successor operator's exponent
     * @param f the specified function
     * @return the k-th successor of a function
     * @throws IllegalArgumentException if the function's color class is
     * unordered
     */
    public static SetFunction factory(final int k, final SetFunction f) {
        if (k == 0 || f instanceof ConstantFunction) {
            return f;
        } else {
            final SetFunction res;
            switch (f) {
                case Projection pr -> res = pr.setExp(k + pr.getSucc());
                case ProjectionComp prcomp -> res = ProjectionComp.factory(prcomp.getArg().setExp(prcomp.getSucc() + k)).cast();
                case Successor sf -> res = Successor.factory(k + sf.exp, sf.getArg());
                case Complement complement -> res = Complement.factory(Successor.factory(k, complement.getArg()));
                default -> res = new Successor(k, f);
            }
            return res;
        }
    }

    @Override
    public SetFunction buildOp(final SetFunction arg) {
        return Successor.factory(this.exp, arg);
    }

    @Override
    public boolean isDistributive(final Class<? extends MultiArgs> optk) {
        return N_aryClassOperator.class.isAssignableFrom(optk);
    }

    /**
     * @return the successor operator's exponent
     */
    public int getExp() {
        return this.exp;
    }

    @Override
    public boolean equals(Object o) {
        final Successor s;
        return this == o || o instanceof Successor && (s = (Successor) o).exp == this.exp && s.getArg().equals(getArg());
    }

    @Override
    public int hashCode() {
        var hash = 7;
        hash = 97 * hash + this.exp + 7 * Objects.hashCode(getArg());
        return hash;
    }

    @Override
    public String symb() {
        return OPSYMB + this.exp;
    }

    @Override
    public Interval card() {
        return getArg().card();
    }

    @Override
    public boolean isInvolution() {
        return false;
    }

}
