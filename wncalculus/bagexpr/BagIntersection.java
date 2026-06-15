package bagexpr;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import color.Sort;
import expr.Expressions;
import expr.ParametricExpr;
import expr.TwoArgs;

/**
 * Abstract binary intersection between bag expressions.
 * When both operands are concrete bags, the result is computed pointwise
 * by taking the minimum multiplicity for each common element.
 *
 * @param <E> bag element type
 */
public abstract class BagIntersection<E extends ParametricExpr>
        implements BagExpr<E>, TwoArgs<BagExpr<E>, BagExpr<E>> {

    private final BagExpr<E> left;
    private final BagExpr<E> right;
    private boolean simplified;

    protected BagIntersection(BagExpr<E> left, BagExpr<E> right) {
        Expressions.checkArity(java.util.List.of(left, right));
        this.left = left;
        this.right = right;
    }

    @Override
    public final BagExpr<E> left() {
        return this.left;
    }

    @Override
    public final BagExpr<E> right() {
        return this.right;
    }

    @Override
    public final boolean simplified() {
        return this.simplified;
    }

    @Override
    public final void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public BagExpr<E> specSimplify() {
        BagExpr<E> l = left();
        BagExpr<E> r = right();

        // Idempotenza: A ∩ A = A
        if (l.equals(r)) {
            return l;
        }

        if (l instanceof Bag && r instanceof Bag) {
            Bag<E> lb = (Bag<E>) l;
            Bag<E> rb = (Bag<E>) r;

            if (lb.isEmpty() || rb.isEmpty()) {
                return build();
            }

            // Intersezione puntuale delle molteplicita': min(k1, k2)
            HashMap<E, Integer> out = new HashMap<>();
            for (Map.Entry<? extends E, Integer> e : lb.asMap().entrySet()) {
                E term = e.getKey();
                int k = Math.min(e.getValue(), rb.mult(term));
                if (k != 0) {
                    out.put(term, k);
                }
            }

            return out.isEmpty() ? build() : build(out);
        }

        return this;
    }

    @Override
    public String symb() {
        return " /\\ ";
    }

    @Override
    public String toString() {
        return TwoArgs.super.toStringOp();
    }

    @Override
    public final Class<E> bagType() {
        return left().bagType();
    }

    @Override
    public Integer cardLb() {
        Integer lc = left().cardLb();
        Integer rc = right().cardLb();
        return (lc == null || rc == null) ? null : Math.min(lc, rc);
    }

    @Override
    public BagIntersection<E> clone(Map<Sort, Sort> split_map) {
        return (BagIntersection<E>) BagExpr.super.clone(split_map);
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return BagExpr.super.splitDelimiters();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BagIntersection<?> other = (BagIntersection<?>) obj;

        // Commutativa: A /\ B == B /\ A
        return (Objects.equals(this.left, other.left) && Objects.equals(this.right, other.right))
                || (Objects.equals(this.left, other.right) && Objects.equals(this.right, other.left));
    }

    @Override
    public int hashCode() {
        // Hash simmetrico per commutativita'
        return Objects.hashCode(this.left) + Objects.hashCode(this.right);
    }
}