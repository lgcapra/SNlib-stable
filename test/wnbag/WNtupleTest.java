
package wnbag;

import classfunction.*;
import color.ColorClass;
import expr.Domain;
import expr.Interval;
import guard.*;
import org.junit.jupiter.api.Test;
import wncolorfunction.ColorFunction;
import wncolorfunction.LinearComb;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WNtupleTest {

    @Test
    void baseCompose_preservesConstantTerms() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        All all = All.getInstance(cc);

        LinearComb leftComb = new LinearComb(Map.of(p1, 2, all, 3));

        WNtuple left = new WNtuple(List.of(leftComb), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(new LinearComb(p2), new LinearComb(p1)), True.getInstance(domain), false);

        WNtuple result = (WNtuple) left.baseCompose(right);
        LinearComb expected = new LinearComb(Map.of(p2, 2, all, 3));
        assertEquals(expected, result.getComponents().getFirst());
    }

    @Test
    void repetedIndexCompose_doesNotAddFilterWithoutRepeatedIndices() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        WNtuple left = new WNtuple(List.of(p1, p2), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(
                        new LinearComb(p2),
                        new LinearComb(p1)),
                True.getInstance(domain), false);

        ArcFunction result = left.repetedIndexCompose(right);
        assertInstanceOf(WNtuple.class, result);
        WNtuple composed = (WNtuple) result;
        assertEquals("<c_2_2,c_2_1>", composed.toString());
        assertTrue(composed.filter().isTrivial());
    }

    @Test
    void repetedIndexCompose_addsEqualityForRepeatedIndex() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        WNtuple left = new WNtuple(List.of(p1, p1), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(p2, p1), True.getInstance(domain), false);

        ArcFunction result = left.repetedIndexCompose(right);
        assertInstanceOf(WNtuple.class, result);
        WNtuple composed = (WNtuple) result;
        assertFalse(composed.filter().isTrivial());
        Equality expectedFilter = (Equality) Equality.builder(Projection.builder(1, cc), Projection.builder(2, cc), true, composed.getCodomain());
        assertEquals(expectedFilter, composed.filter());
    }

    @Test
    void baseCompose_throwsIfLeftTupleHasNonTrivialGuard() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);

        WNtuple left = new WNtuple(List.of(p1, p2), equality, false);
        WNtuple right = new WNtuple(List.of(new LinearComb(p2), new LinearComb(p1)), True.getInstance(domain), false);

        assertThrows(IllegalStateException.class, () -> left.baseCompose(right));
    }

    @Test
    void baseCompose_throwsIfRightTupleHasNonTrivialFilter() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);

        WNtuple left = new WNtuple(List.of(p1, p2), True.getInstance(domain), false);
        WNtuple right = new WNtuple(equality, List.of(new LinearComb(p2), new LinearComb(p1)), True.getInstance(domain), false);

        assertThrows(IllegalStateException.class, () -> left.baseCompose(right));
    }

    @Test
    void specSimplify_returnsEmptyForDifferentSingletonsWithEqualityFilter() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);

        WNtuple tuple = new WNtuple(equality, List.of(p1, p2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(TupleBag.class, result);
        assertTrue(((TupleBag) result).isEmpty());
    }

    @Test
    void specSimplify_removesSatisfiedEqualityBetweenSingletons() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);

        Equality equality = (Equality) Equality.builder(p1, Projection.builder(2, cc), true, domain);

        WNtuple tuple = new WNtuple(equality, List.of(p1, p1), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertTrue(simplified.filter().isTrivial());
        assertEquals(List.of(p1, p1), simplified.getHomSubTuple(cc));
    }

    @Test
    void specSimplify_replacesMultisetComponentWithSingleton() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);

        WNtuple tuple = new WNtuple(equality, List.of(p1, new LinearComb(p2, 2)), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertTrue(simplified.filter().isTrivial());
        assertEquals(List.of(p1, new LinearComb(p1, 2)), simplified.getHomSubTuple(cc));
    }

    @Test
    void specSimplify_replacesFirstMultisetComponentWithSingleton() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);

        WNtuple tuple = new WNtuple(equality, List.of(new LinearComb(p1, 2), p2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertTrue(simplified.filter().isTrivial());
        assertEquals(List.of(new LinearComb(p2, 2), p2), simplified.getHomSubTuple(cc));
    }

    @Test
    void specSimplify_returnsEmptyForEqualSingletonsWithInequalityFilter() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality inequality = (Equality) Equality.builder(p1, p2, false, domain);

        WNtuple tuple = new WNtuple(inequality, List.of(p1, p1), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(TupleBag.class, result);
        assertTrue(((TupleBag) result).isEmpty());
    }

    @Test
    void specSimplify_restrictsAllWithOrderedSuccessorDifference() {
        ColorClass cc = new ColorClass(2, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, 1, cc);

        Equality inequality = (Equality) Equality.builder(p1, p2, false, domain);

        WNtuple tuple = new WNtuple(inequality, List.of(All.getInstance(cc), p2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertTrue(simplified.filter().isTrivial());
        assertNotEquals(All.getInstance(cc), simplified.getHomSubTuple(cc).get(0));
        assertEquals(p2, simplified.getHomSubTuple(cc).get(1));
    }

    @Test
    void specSimplify_restrictsSecondAllWithOrderedSuccessorDifference() {
        ColorClass cc = new ColorClass(2, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, 1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality inequality = (Equality) Equality.builder(p1, p2, false, domain);

        WNtuple tuple = new WNtuple(inequality, List.of(p1, All.getInstance(cc)), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertTrue(simplified.filter().isTrivial());
        assertEquals(p1, simplified.getHomSubTuple(cc).get(0));
        assertNotEquals(All.getInstance(cc), simplified.getHomSubTuple(cc).get(1));
    }

    @Test
    void specSimplify_preservesInequalityWhenComponentsCannotBeRestricted() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality inequality = (Equality) Equality.builder(p1, p2, false, domain);

        All all1 = All.getInstance(cc);
        All all2 = All.getInstance(cc);

        WNtuple tuple = new WNtuple(inequality, List.of(all1, all2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertEquals(inequality, simplified.filter());
        assertEquals(List.of(all1, all2), simplified.getHomSubTuple(cc));
    }

    @Test
    void specSimplify_returnsEmptyForSubclWithNegativeMembershipOnSameSubclass() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain dom = new Domain(cc);

        Subcl selected = Subcl.factory(1, cc);
        Projection p = Projection.builder(1, cc);
        Membership membership = Membership.build(p, selected, false, dom);

        WNtuple tuple = new WNtuple(membership, List.of(selected), True.getInstance(dom), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertEquals(Empty.getInstance(cc), simplified.getHomSubTuple(cc).getFirst());
    }

    @Test
    void specSimplify_preservesSubclWithNegativeMembershipOnDifferentSubclass() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain dom = new Domain(cc);

        Subcl selected = Subcl.factory(1, cc);
        Subcl excluded = Subcl.factory(2, cc);

        Projection p = Projection.builder(1, cc);
        Membership membership = Membership.build(p, excluded, false, dom);

        WNtuple tuple = new WNtuple(membership, List.of(selected), True.getInstance(dom), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertEquals(selected, simplified.getHomSubTuple(cc).getFirst());
    }

    @Test
    void specSimplify_extractsCommonCoefficientFromThreeLinearCombTermsWithMembership() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain dom = new Domain(cc, cc, cc);
        Domain codom = new Domain(cc, cc);

        Projection projection1 = Projection.builder(1, cc);
        Projection projection2 = Projection.builder(2, cc);
        Projection projection3 = Projection.builder(3, cc);

        Subcl subcl = Subcl.factory(1, cc);

        Membership membership = Membership.build(projection2, subcl, codom);

        LinearComb component = new LinearComb(Map.of(projection1, 2, projection2, -2, projection3, 4), false);

        WNtuple tuple = new WNtuple(membership, List.of(All.getInstance(cc), component), True.getInstance(dom), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(ArcFunScalar.class, result);
        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(2, scalar.k());
        WNtuple simplifiedTuple = (WNtuple) scalar.getArg();
        assertEquals(membership, simplifiedTuple.filter());
        assertEquals(True.getInstance(dom), simplifiedTuple.guard());
        List<ColorFunction> components = (List<ColorFunction>) simplifiedTuple.getHomSubTuple(cc);
        assertEquals(2, components.size());
        assertEquals(All.getInstance(cc), components.get(0));
        assertInstanceOf(LinearComb.class, components.get(1));
        LinearComb simplifiedComponent = (LinearComb) components.get(1);
        assertEquals(1, simplifiedComponent.asMap().get(projection1));
        assertEquals(-1, simplifiedComponent.asMap().get(projection2));
        assertEquals(2, simplifiedComponent.asMap().get(projection3));
        assertEquals(3, simplifiedComponent.asMap().size());
    }

    @Test
    void specSimplify_returnsEmptyForConflictingMembershipsOnFirstOfTwoComponents() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain dom = new Domain(cc, cc);
        Domain codom = new Domain(cc, cc);

        Projection projection1 = Projection.builder(1, cc);

        Subcl subcl1 = Subcl.factory(1, cc);
        Subcl subcl2 = Subcl.factory(2, cc);

        Membership membership1 = Membership.build(projection1, subcl1, codom);
        Membership membership2 = Membership.build(projection1, subcl2, codom);
        Guard filter = And.factory(List.of(membership1, membership2), true);

        WNtuple tuple = new WNtuple(filter, List.of(All.getInstance(cc), All.getInstance(cc)), True.getInstance(dom), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(TupleBag.class, result);
        TupleBag simplified = (TupleBag) result;
        assertTrue(simplified.asMap().isEmpty());
    }

    @Test
    void specSimplify_reducesMultipleMembershipsAndPreservesUnabsorbedOne() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain dom = new Domain(cc, cc, cc);
        Domain codom = new Domain(cc, cc, cc);

        Projection projection1 = Projection.builder(1, cc);
        Projection projection2 = Projection.builder(2, cc);
        Projection projection3 = Projection.builder(3, cc);

        Subcl subcl1 = Subcl.factory(1, cc);
        Subcl subcl2 = Subcl.factory(2, cc);
        Subcl subcl3 = Subcl.factory(3, cc);

        Membership membership1 = Membership.build(projection1, subcl1, codom);
        Membership membership2 = Membership.build(projection2, subcl2, codom);
        Membership membership3 = Membership.build(projection3, subcl3, codom);
        Guard filter = And.factory(List.of(membership1, membership2, membership3), true);

        WNtuple tuple = new WNtuple(filter, List.of(All.getInstance(cc), All.getInstance(cc), projection3), True.getInstance(dom), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        List<ColorFunction> components = (List<ColorFunction>) simplified.getHomSubTuple(cc);
        assertEquals(3, components.size());
        assertEquals(subcl1, components.get(0));
        assertEquals(subcl2, components.get(1));
        assertEquals(projection3, components.get(2));
        assertEquals(membership3, simplified.filter());
        assertEquals(True.getInstance(dom), simplified.guard());
    }

    @Test
    void specSimplify_splitsTwoLinearCombsReferencedByEqualityGuard() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain dom = new Domain(cc, cc, cc, cc);
        Domain codom = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        LinearComb lc1 = new LinearComb(Map.of(p1, 1, p2, 1));
        LinearComb lc2 = new LinearComb(Map.of(p3, 1, p4, 1));

        Equality equality = (Equality) Equality.builder(p1, p3, true, dom);

        WNtuple tuple = new WNtuple(True.getInstance(codom), List.of(lc1, lc2), equality, false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(TupleBag.class, result);
        TupleBag tuples = (TupleBag) result;
        assertEquals(4, tuples.asMap().size());
    }

    @Test
    void specSimplify_splitsLinearCombReferencedByMembershipGuard() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});

        Domain dom = new Domain(cc, cc, cc, cc);
        Domain codom = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        LinearComb lc1 = new LinearComb(Map.of(p1, 2, p2, 3));
        LinearComb lc2 = new LinearComb(Map.of(p3, 4, p4, 5));

        Subcl subcl1 = Subcl.factory(1, cc);
        Guard membership = Membership.build(p1, subcl1, dom);

        WNtuple tuple = new WNtuple(True.getInstance(codom), List.of(lc1, lc2), membership, false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(TupleBag.class, result);
        TupleBag tuples = (TupleBag) result;
        assertEquals(2, tuples.asMap().size());
        Collection<Integer> coefficients = tuples.asMap().values();
        assertTrue(coefficients.containsAll(List.of(2, 3)));
    }

    @Test
    void specSimplify_splitsLinearCombsReferencedByEqualityAndMembership() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});

        Domain dom = new Domain(cc, cc, cc, cc);
        Domain codom = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        LinearComb lc1 = new LinearComb(Map.of(p1, 2, p2, 3));
        LinearComb lc2 = new LinearComb(Map.of(p3, 4, p4, 5));

        Subcl subcl1 = Subcl.factory(1, cc);
        Guard equality = Equality.builder(p1, p2, true, dom);
        Guard membership = Membership.build(p3, subcl1, dom);

        Guard guard = And.factory(equality, membership);

        WNtuple tuple = new WNtuple(True.getInstance(codom), List.of(lc1, lc2), guard, false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(TupleBag.class, result);
        TupleBag tuples = (TupleBag) result;
        assertEquals(2, tuples.asMap().size());
        Collection<Integer> coefficients = tuples.asMap().values();
        assertTrue(coefficients.containsAll(List.of(20, 25)));
    }

    @Test
    void succMulti_shiftsProjectionsAndPreservesOtherElements() {
        ColorClass cc = new ColorClass(2, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, 1, cc);
        Projection p2 = Projection.builder(2, 2, cc);

        All all = All.getInstance(cc);

        LinearComb lc = new LinearComb(Map.of(p1, 2, p2, 3, all, 4));

        WNtuple tuple = new WNtuple(True.getInstance(domain), List.of(p1, p2), True.getInstance(domain), false);

        ColorFunction result = tuple.succMulti(-1, lc);
        assertInstanceOf(LinearComb.class, result);
        LinearComb simplified = (LinearComb) result;
        assertEquals(3, simplified.asMap().size());
        assertEquals(2, simplified.asMap().get(Projection.builder(1, 0, cc)));
        assertEquals(3, simplified.asMap().get(Projection.builder(2, 1, cc)));
        assertEquals(4, simplified.asMap().get(all));
    }

    @Test
    void collectGuardIndices_collectsIndicesRecursivelyFromAnd() throws Exception {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        Equality eq1 = (Equality) Equality.builder(p1, p2, true, domain);
        Equality eq2 = (Equality) Equality.builder(p2, p3, false, domain);

        Guard guard = And.factory(List.of(eq1, eq2), true);

        Set<Integer> indices = new HashSet<>();

        Method method = WNtuple.class.getDeclaredMethod("collectGuardIndices", Guard.class, Set.class);
        method.setAccessible(true);

        WNtuple.class.cast(new WNtuple(True.getInstance(domain), List.of(p1, p2, p3), guard, false));

        method.invoke(new WNtuple(True.getInstance(domain), List.of(p1, p2, p3), guard, false), guard, indices);
        assertEquals(Set.of(1, 2, 3), indices);
    }

    @Test
    void baseGuardReduction_doesNotModifyTupleWhenGuardIsTrivial() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        WNtuple tuple = new WNtuple(True.getInstance(domain), List.of(p1, p2), True.getInstance(domain), false);

        assertSame(tuple, tuple.baseGuardReduction());
    }

    @Test
    void baseGuardReduction_doesNotModifyTupleWhenNoEqualityCanBeMoved() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);
        Guard guard = And.factory(List.of(eq), true);

        WNtuple tuple = new WNtuple(True.getInstance(domain), List.of(All.getInstance(cc), All.getInstance(cc)), guard, false);

        assertSame(tuple, tuple.baseGuardReduction());
    }

    @Test
    void baseGuardReduction_movesEqualityToFilter() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);
        Guard guard = And.factory(List.of(eq), true);

        WNtuple tuple = new WNtuple(True.getInstance(domain), List.of(p1, p2), guard, false);

        WNtuple result = tuple.baseGuardReduction();
        assertNotSame(tuple, result);
        assertEquals(Equality.builder(Projection.builder(1, cc), Projection.builder(2, cc), true, domain), result.filter());
        assertTrue(result.guard().isTrivial());
    }

    @Test
    void baseGuardReduction_handlesSingleProjectionLinearComb() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        LinearComb lc = new LinearComb(Map.of(p3, 1));

        Equality moved = (Equality) Equality.builder(p3, p1, true, domain);
        Equality remaining = (Equality) Equality.builder(p1, p4, false, domain);

        Guard guard = And.factory(List.of(moved, remaining), true);

        WNtuple tuple = new WNtuple(True.getInstance(domain), List.of(lc, p1, All.getInstance(cc), All.getInstance(cc)), guard, false);

        WNtuple result = tuple.baseGuardReduction();
        assertNotSame(tuple, result);
        assertEquals(remaining, result.guard());
        assertInstanceOf(Equality.class, result.filter());
    }

    @Test
    void baseGuardReduction_preservesExistingElementaryFilter() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Guard existingFilter = Equality.builder(p1, p2, false, domain);
        Equality guardEquality = (Equality) Equality.builder(p1, p2, true, domain);

        Guard guard = And.factory(List.of(guardEquality), true);

        WNtuple tuple = new WNtuple(existingFilter, List.of(p1, p2), guard, false);

        WNtuple result = tuple.baseGuardReduction();
        assertNotSame(tuple, result);
        assertInstanceOf(And.class, result.filter());
        assertTrue(And.getArgs(result.filter()).contains(existingFilter));
        assertTrue(result.guard().isTrivial());
    }

    @Test
    void baseGuardReduction_preservesMultipleRemainingGuardClauses() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        Equality moved = (Equality) Equality.builder(p1, p2, true, domain);

        Equality remaining1 = (Equality) Equality.builder(p1, p4, false, domain);
        Equality remaining2 = (Equality) Equality.builder(p2, p3, false, domain);

        Guard guard = And.factory(List.of(moved, remaining1, remaining2), true);

        WNtuple tuple = new WNtuple(True.getInstance(domain), List.of(p1, p2, All.getInstance(cc), All.getInstance(cc)), guard, false);

        WNtuple result = tuple.baseGuardReduction();

        assertNotSame(tuple, result);
        assertInstanceOf(And.class, result.guard());
        Collection<Guard> remainingClauses = (Collection<Guard>) And.getArgs(result.guard());
        assertEquals(2, remainingClauses.size());
        assertTrue(remainingClauses.contains(remaining1));
        assertTrue(remainingClauses.contains(remaining2));
    }

    @Test
    void baseFilterReduction_handlesSingletonLinearCombWithMultiplicity() throws Exception {
        ColorClass cc = new ColorClass(2, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, 0, cc);
        Projection p2 = Projection.builder(2, 1, cc);

        LinearComb fi = new LinearComb(Map.of(p1, 2));
        LinearComb fj = new LinearComb(Map.of(p2, 1));

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);
        Guard filter = And.factory(List.of(eq), true);

        WNtuple tuple = new WNtuple(filter, List.of(fi, fj), True.getInstance(domain), false);

        Method method = WNtuple.class.getDeclaredMethod("baseFilterReduction", Map.class, Map.class);
        method.setAccessible(true);
        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = Map.of(cc, Map.of(true, new TreeSet<>(Set.of(eq))));
        Map<ColorClass, Map<Boolean, Set<Membership>>> membershipMap = Map.of();

        ArcFunction result = (ArcFunction) method.invoke(tuple, equalityMap, membershipMap);
        assertInstanceOf(WNtuple.class, result);
        WNtuple reduced = (WNtuple) result;
        LinearComb expected = new LinearComb(Projection.builder(2, 2, cc), 2);
        assertEquals(expected, reduced.getHomSubTuple(cc).getFirst());
        assertTrue(reduced.filter().isTrivial());
    }

    @Test
    void restrictConstantByMembership_excludesSelectedSubclassFromAll() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain dom = new Domain(cc);

        All all = All.getInstance(cc);
        Subcl selected = Subcl.factory(1, cc);
        Projection p = Projection.builder(1, cc);

        Membership membership = Membership.build(p, selected, false, dom);

        WNtuple tuple = new WNtuple(True.getInstance(dom), List.of(all), True.getInstance(dom), false);

        Method method = WNtuple.class.getDeclaredMethod("restrictConstantByMembership", ConstantFunction.class, Membership.class);
        method.setAccessible(true);

        ColorFunction result = (ColorFunction) method.invoke(tuple, all, membership);
        Map<ElementaryFunction, Integer> expectedTerms = new LinkedHashMap<>();
        expectedTerms.put(Subcl.factory(2, cc), 1);
        expectedTerms.put(Subcl.factory(3, cc), 1);
        LinearComb expected = new LinearComb(expectedTerms, false);
        assertEquals(expected, result);
    }

}
