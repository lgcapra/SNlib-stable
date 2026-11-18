package wnbag;

import classfunction.*;
import color.ColorClass;
import expr.Domain;
import expr.Interval;
import guard.*;
import wncolorfunction.*;
import java.lang.reflect.Method;
import java.util.*;

/** Independent finite-domain oracle. Run with Java 21; no test framework required. */
public final class FilterReductionSemanticTest {
    private static long valuations;
    private static int cases;
    private static final Method REDUCE;
    static {
        try {
            REDUCE = WNtuple.class.getDeclaredMethod("baseFilterReduction", Map.class, Map.class);
            REDUCE.setAccessible(true);
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    public static void main(String[] args) throws Exception {
        exercise(new ColorClass("U", 3), new int[]{3});
        exercise(new ColorClass("O", 3, true), new int[]{3});
        exercise(new ColorClass("Two", 2, true), new int[]{2});
        exercise(new ColorClass("S", new Interval[]{new Interval(1,1), new Interval(2,2)}), new int[]{1,2});
        exercise(new ColorClass("D", new Interval[]{new Interval(2,2), new Interval(2,2)}), new int[]{2,2});
        System.out.println("PASS: " + cases + " reductions; " + valuations + " input/output multiplicity comparisons");
    }
    private static void exercise(ColorClass cc, int[] subclasses) throws Exception {
        Domain input = new Domain(cc, 3), output = new Domain(cc, 2);
        List<ElementaryFunction> functions = new ArrayList<>();
        functions.add(All.getInstance(cc));
        functions.add(Empty.getInstance(cc));
        for (int index = 1; index <= 3; index++) {
            for (int shift = 0; shift < (cc.isOrdered() ? cc.fixedSize() : 1); shift++) {
                Projection p = Projection.builder(index, shift, cc);
                functions.add(p);
                functions.add(ProjectionComp.factory(p));
            }
        }
        if (cc.subclasses() > 1) {
            for (int index = 1; index <= cc.subclasses(); index++) functions.add(Subcl.factory(index, cc));
        }
        for (ElementaryFunction first : functions) for (ElementaryFunction second : functions) {
            for (boolean sign : new boolean[]{true,false}) {
                for (int shift = 0; shift < (cc.isOrdered() ? cc.fixedSize() : 1); shift++) {
                    Guard filter = Equality.builder(Projection.builder(1, cc), Projection.builder(2, shift, cc), sign, output);
                    Guard inputGuard = Equality.builder(Projection.builder(1, cc), Projection.builder(3, cc), false, input);
                    WNtuple before = new WNtuple(filter, List.of(first, second), inputGuard, true);
                    ArcFunction after = reduce(before);
                    check(before, after, cc, subclasses);
                    check(before, before.normalize(), cc, subclasses);
                    if (!first.isFalse() && !second.isFalse() && (first.cardLeq1() || second.cardLeq1())) {
                        assertRemoved(after, filter);
                    }
                }
            }
            if (cc.subclasses() > 1) {
                for (boolean sign : new boolean[]{true,false}) {
                    Guard filter = Membership.build(Projection.builder(1,cc), Subcl.factory(1,cc), sign, output);
                    WNtuple before = new WNtuple(filter, List.of(first, second), True.getInstance(input), true);
                    check(before, reduce(before), cc, subclasses);
                }
            }
        }
        // Public pipeline: coefficient extraction, membership expansion and normalization.
        if (cc.subclasses() > 1) {
            Guard filter = Membership.build(Projection.builder(1,cc), Subcl.factory(1,cc), true, output);
            LinearComb mixed = new LinearComb(Map.of(All.getInstance(cc), 2, Projection.builder(3,cc), -1));
            WNtuple before = new WNtuple(filter, List.of(mixed, Projection.builder(2,cc)), True.getInstance(input), true);
            ArcFunction after = before.normalize();
            check(before, after, cc, subclasses);
            assertRemoved(after, filter);
        }
    }
    private static ArcFunction reduce(WNtuple tuple) throws Exception {
        return (ArcFunction) REDUCE.invoke(tuple, tuple.filter().equalityMap(), tuple.filter().membMap());
    }
    private static void assertRemoved(ArcFunction expression, Guard removed) {
        if (expression instanceof WNtuple tuple) {
            if (!tuple.filter().isTrivial() && And.getArgs(tuple.filter()).contains(removed)) throw new AssertionError("Clause retained: " + tuple);
        } else if (expression instanceof ArcFunSum sum) {
            for (var part : sum.getArgs()) assertRemoved((ArcFunction) part, removed);
        } else if (expression instanceof TupleBag bag) {
            for (var tuple : bag.asMap().keySet()) assertRemoved(tuple, removed);
        } else if (expression instanceof ArcFunScalar scalar) assertRemoved((ArcFunction)scalar.getArg(), removed);
    }
    private static void check(WNtuple before, ArcFunction after, ColorClass cc, int[] subclasses) {
        if (!before.getDomain().equals(after.getDomain()) || !before.getCodomain().equals(after.getCodomain()))
            throw new AssertionError("Changed domains");
        int n = Arrays.stream(subclasses).sum();
        for (int x = 0; x < n; x++) for (int y = 0; y < n; y++) for (int z = 0; z < n; z++) {
            int[] input = {x,y,z};
            for (int a = 0; a < n; a++) for (int b = 0; b < n; b++) {
                int[] output = {a,b};
                int expected = value(before,input,output,cc,subclasses);
                int actual = value(after,input,output,cc,subclasses);
                valuations++;
                if (expected != actual) throw new AssertionError(before + " -> " + after
                        + "; input=" + Arrays.toString(input) + "; output=" + Arrays.toString(output)
                        + "; expected=" + expected + "; actual=" + actual);
            }
        }
        cases++;
    }
    private static int value(ArcFunction expression, int[] input, int[] output, ColorClass cc, int[] sub) {
        if (expression instanceof WNtuple tuple) {
            if (!guard(tuple.guard(),input,sub) || !guard(tuple.filter(),output,sub)) return 0;
            int result = 1;
            var parts = tuple.getHomSubTuple(cc);
            for (int k = 0; k < parts.size(); k++) result *= component(parts.get(k),input,output[k],sub);
            return result;
        }
        if (expression instanceof ArcFunSum sum) {
            int result = 0;
            for (var term : sum.getArgs()) result += value((ArcFunction)term,input,output,cc,sub);
            return result;
        }
        if (expression instanceof ArcFunScalar scalar) return scalar.k()*value((ArcFunction)scalar.getArg(),input,output,cc,sub);
        if (expression instanceof TupleBag bag) {
            int result = 0;
            for (var term : bag.asMap().entrySet()) result += term.getValue()*value(term.getKey(),input,output,cc,sub);
            return result;
        }
        throw new AssertionError("Unsupported result: " + expression.getClass());
    }
    private static int component(ColorFunction function, int[] input, int output, int[] sub) {
        if (function instanceof Empty) return 0;
        if (function instanceof All) return 1;
        if (function instanceof Projection p) return projection(p,input,sub) == output ? 1 : 0;
        if (function instanceof ProjectionComp p) return projection(p.getArg(),input,sub) != output ? 1 : 0;
        if (function instanceof Subcl s) return subclass(output,sub) == s.index() ? 1 : 0;
        if (function instanceof LinearComb lc) {
            int result = 0;
            for (var term : lc.asMap().entrySet()) result += term.getValue()*component(term.getKey(),input,output,sub);
            return result;
        }
        throw new AssertionError("Unsupported component: " + function);
    }
    private static int projection(Projection p, int[] input, int[] sub) {
        return Math.floorMod(input[p.getIndex()-1] + p.getSucc(), Arrays.stream(sub).sum());
    }
    private static int subclass(int value, int[] sub) {
        for (int k = 0; k < sub.length; k++) { if (value < sub[k]) return k+1; value -= sub[k]; }
        throw new AssertionError();
    }
    private static boolean guard(Guard guard, int[] values, int[] sub) {
        if (guard.isTrue()) return true;
        if (guard.isFalse()) return false;
        if (guard instanceof Equality eq) return eq.sign() == (projection(eq.getArg1(),values,sub) == projection(eq.getArg2(),values,sub));
        if (guard instanceof Membership m) return m.sign() == (subclass(projection(m.getArg1(),values,sub),sub) == m.getArg2().index());
        if (guard instanceof And and) return and.getArgs().stream().allMatch(g -> guard(g,values,sub));
        if (guard instanceof Or or) return or.getArgs().stream().anyMatch(g -> guard(g,values,sub));
        throw new AssertionError("Unsupported guard: " + guard);
    }
}
