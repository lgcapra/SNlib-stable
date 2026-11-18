
package wnbag;

import classfunction.*;
import color.ColorClass;
import expr.Domain;
import expr.Interval;
import guard.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import util.Pair;
import wncolorfunction.ColorFunction;
import wncolorfunction.LinearComb;
import wncolorfunction.LinearCombScalar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class WNtupleTest {

    private Method baseFilterReductionMethod;
    private Method splitAtPositionsMethod;
    private Method filterIdxMethod;

    @BeforeEach
    void setUp() throws Exception {
        baseFilterReductionMethod = WNtuple.class.getDeclaredMethod("baseFilterReduction", Map.class, Map.class);
        baseFilterReductionMethod.setAccessible(true);
        splitAtPositionsMethod = WNtuple.class.getDeclaredMethod("splitAtPositions", Map.class);
        splitAtPositionsMethod.setAccessible(true);
        filterIdxMethod = WNtuple.class.getDeclaredMethod("filterIdx", WNtuple.class);
        filterIdxMethod.setAccessible(true);
    }

    private ArcFunction invokeBaseFilterReduction(WNtuple tuple, Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap, Map<ColorClass, Map<Boolean, Set<Membership>>> membMap) throws Exception {
        return (ArcFunction) baseFilterReductionMethod.invoke(tuple, equalityMap, membMap);
    }

    @ParameterizedTest(name = "singletonFirst={0}, equality={1}")
    @MethodSource("baseFilterReductionCases")
    void baseFilterReduction_preservesUnsupportedLinearCombination(boolean singletonFirst, boolean equalitySign) throws Exception {

        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});

        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(new Pair<>(p1, 2), new Pair<>(p2, 3));

        Guard relation = Equality.builder(p1, p2, equalitySign, domain);

        Subcl subcl = Subcl.factory(1, cc);
        Guard membership = Membership.build(singletonFirst ? p2 : p1, subcl, domain);

        Guard filter = And.factory(relation, membership);

        List<ColorFunction> components = singletonFirst ? List.of(p1, lc) : List.of(lc, p2);

        WNtuple tuple = new WNtuple(filter, components, True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        SortedSet<Equality> relations = new TreeSet<>();
        relations.add((Equality) relation);

        equalityMap.put(cc, Map.of(equalitySign, relations));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertInstanceOf(WNtuple.class, result);
        WNtuple reduced = (WNtuple) result;

        assertAll(
                () -> assertEquals(tuple.filter(), reduced.filter()),
                () -> assertEquals(tuple.getComponents(), reduced.getComponents()),
                () -> assertEquals(tuple.guard(), reduced.guard())
        );
    }

    static Stream<Arguments> baseFilterReductionCases() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(false, false));
    }

    @Test
    void baseFilterReduction_returnsEmptyForEqualSingletonsWithInequality() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Guard inequality = Equality.builder(p1, p2, false, domain);

        WNtuple tuple = new WNtuple(inequality, List.of(p1, p1), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        SortedSet<Equality> inequalities = new TreeSet<>();
        inequalities.add((Equality) inequality);
        equalityMap.put(cc, Map.of(false, inequalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertEquals(tuple.getEmpty(), result);
    }

    @Test
    void baseFilterReduction_removesEqualityBetweenIdenticalSingletons() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Guard equality = Equality.builder(p1, p2, true, domain);
        Guard otherEquality = Equality.builder(p1, p2.setExp(1), true, domain);
        Guard filter = And.factory(equality, otherEquality);

        WNtuple tuple = new WNtuple(filter, List.of(p1, p1), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add((Equality) equality);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertNotSame(tuple, result);
        assertInstanceOf(WNtuple.class, result);
    }

    @Test
    void baseFilterReduction_preservesConstraintsWithWeightedProjection() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(p2, 2);

        Guard inequality = Equality.builder(p1, p2, false, domain);
        Guard otherEquality = Equality.builder(p1, p2.setExp(1), true, domain);
        Guard filter = And.factory(inequality, otherEquality);

        WNtuple tuple = new WNtuple(filter, List.of(p1, lc), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        SortedSet<Equality> inequalities = new TreeSet<>();
        inequalities.add((Equality) inequality);
        equalityMap.put(cc, Map.of(false, inequalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertInstanceOf(WNtuple.class, result);
        WNtuple reduced = (WNtuple) result;

        assertAll(
                () -> assertEquals(tuple.filter(), reduced.filter(), "I vincoli devono essere conservati"),
                () -> assertEquals(tuple.getComponents(), reduced.getComponents(), "Le componenti e le molteplicità devono essere preservate"),
                () -> assertEquals(tuple.guard(), reduced.guard())
        );
    }

    @Test
    void baseFilterReduction_preservesEqualityBetweenDisjointComponents() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(p2, 2);

        Guard equality = Equality.builder(p1, p2, true, domain);

        WNtuple tuple = new WNtuple(equality, List.of(p1, lc), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add((Equality) equality);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertSame(tuple, result);
    }

    @Test
    void baseFilterReduction_preservesFilterWhenBothComponentsAreMultiElement() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc1 = new LinearComb(new Pair<>(p1, 2), new Pair<>(p2, 3));
        LinearComb lc2 = new LinearComb(new Pair<>(p1, 4), new Pair<>(p2, 5));

        Guard equality = Equality.builder(p1, p2, true, domain);

        WNtuple tuple = new WNtuple(equality, List.of(lc1, lc2), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add((Equality) equality);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertSame(tuple, result);
    }

    static Stream<Arguments> invalidEqualityIndices() {
        return Stream.of(
                Arguments.of(0, 2), // i < 0
                Arguments.of(1, 0), // j < 0
                Arguments.of(3, 4), // i >= h_tuple.size()
                Arguments.of(1, 3)  // j >= h_tuple.size()
        );
    }

    @ParameterizedTest
    @MethodSource("invalidEqualityIndices")
    void baseFilterReduction_skipsEqualitiesWithInvalidIndices(int firstIndex, int secondIndex) throws Exception {

        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Constructor<Projection> projectionConstructor = Projection.class.getDeclaredConstructor(int.class, int.class, ColorClass.class);
        projectionConstructor.setAccessible(true);

        Projection p1 = projectionConstructor.newInstance(firstIndex, 0, cc);
        Projection p2 = projectionConstructor.newInstance(secondIndex, 0, cc);

        Constructor<Equality> equalityConstructor = Equality.class.getDeclaredConstructor(Projection.class, Projection.class, boolean.class, Domain.class);
        equalityConstructor.setAccessible(true);

        Equality equality = equalityConstructor.newInstance(p1, p2, true, domain);

        Projection tupleP1 = Projection.builder(1, cc);
        Projection tupleP2 = Projection.builder(2, cc);

        WNtuple tuple = new WNtuple(List.of(tupleP1, tupleP2), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add(equality);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertSame(tuple, result);
    }

    @Test
    void baseFilterReduction_distinctSingletons_movesEqualityToGuard() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);
        WNtuple tuple = new WNtuple(eq, List.of(p1, p2), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();
        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add(eq);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        WNtuple reduced = assertInstanceOf(WNtuple.class, result);
        assertTrue(reduced.filter().isTrue());
        WNtuple expected = new WNtuple(List.of(p1, p1), eq, true);
        assertEquals(expected.normalize(), reduced.normalize());
    }

    @Test
    void baseFilterReduction_orderedClassWithDifferentSuccessors() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, 0, cc);
        Projection p2 = Projection.builder(2, 1, cc);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);
        WNtuple tuple = new WNtuple(eq, List.of(p1, p2), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();
        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add(eq);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();
        membMap.put(cc, Map.of(true, Set.of()));

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        WNtuple reduced = assertInstanceOf(WNtuple.class, result);
        assertTrue(reduced.filter().isTrue());
        Guard expectedGuard = Equality.builder(p1, p2.setExp(2), true, domain);
        WNtuple expected = new WNtuple(List.of(p1, p1.setExp(-1)), expectedGuard, true);
        assertEquals(expected.normalize(), reduced.normalize());
    }

    @Test
    void baseFilterReduction_removesRedundantMembershipOnConstant() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain domain = new Domain(cc);

        Subcl selected = Subcl.factory(1, cc);
        Projection p = Projection.builder(1, cc);

        Membership membership = Membership.build(p, selected, domain);

        WNtuple tuple = new WNtuple(membership, List.of(selected), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();
        membMap.put(cc, Map.of(true, Set.of(membership)));

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertInstanceOf(WNtuple.class, result);
        WNtuple reduced = (WNtuple) result;

        assertAll(
                () -> assertEquals(True.getInstance(tuple.getCodomain()), reduced.filter()),
                () -> assertEquals(tuple.getComponents(), reduced.getComponents()),
                () -> assertEquals(tuple.guard(), reduced.guard())
        );
    }

    @Test
    void baseFilterReduction_membershipRestrictsConstant() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain domain = new Domain(cc);

        Subcl selected = Subcl.factory(1, cc);
        Projection p = Projection.builder(1, cc);

        Membership membership = Membership.build(p, Subcl.factory(2, cc), domain);

        WNtuple tuple = new WNtuple(membership, List.of(selected), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();
        membMap.put(cc, Map.of(true, Set.of(membership)));

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertNotSame(tuple, result);
    }

    @Test
    void baseFilterReduction_membershipOnNonConstantFunction() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);

        Membership membership = Membership.build(p, Subcl.factory(2, cc), domain);

        WNtuple tuple = new WNtuple(membership, List.of(p), True.getInstance(domain), false);

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();
        membMap.put(cc, Map.of(true, Set.of(membership)));

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        WNtuple reduced = assertInstanceOf(WNtuple.class, result);
        assertTrue(reduced.filter().isTrue());
        assertEquals(membership, reduced.guard());
        assertEquals(tuple.getComponents(), reduced.getComponents());
    }

    @Test
    void baseFilterReduction_movesInequalityBetweenSingletonsToGuard() throws Exception {

        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality inequality = (Equality) Equality.builder(p1, p2, false, domain);

        WNtuple tuple = new WNtuple(inequality, List.of(p1, p2), True.getInstance(domain), true);

        SortedSet<Equality> inequalities = new TreeSet<>();
        inequalities.add(inequality);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        equalityMap.put(cc, Map.of(false, inequalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membershipMap = new TreeMap<>();

        ArcFunction reduced = invokeBaseFilterReduction(tuple, equalityMap, membershipMap);

        assertInstanceOf(WNtuple.class, reduced);
        WNtuple result = (WNtuple) reduced;

        assertTrue(result.filter().isTrue());
        assertEquals(inequality, result.guard());
        assertEquals(tuple.getComponents(), result.getComponents());
        assertEquals(domain, result.getDomain());
        assertEquals(domain, result.getCodomain());
    }

    @Test
    void baseFilterReduction_subtractsGuardedIntersectionForComplementOverlap() throws Exception {

        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        ElementaryFunction complement = ProjectionComp.factory(p2);

        Equality inequality = (Equality) Equality.builder(Projection.builder(1, cc), Projection.builder(2, cc), false, domain);

        WNtuple tuple = new WNtuple(inequality, List.of(p1, complement), True.getInstance(domain), true);

        SortedSet<Equality> inequalities = new TreeSet<>();
        inequalities.add(inequality);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();

        equalityMap.put(cc, Map.of(false, inequalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membershipMap = new TreeMap<>();

        ArcFunction reduced = invokeBaseFilterReduction(tuple, equalityMap, membershipMap);

        WNtuple unrestricted = tuple.build(True.getInstance(domain), True.getInstance(domain));
        WNtuple correction = new WNtuple(True.getInstance(domain), List.of(p1, new LinearComb(p1, -1)), inequality, true);
        ArcFunction expected = ArcFunSum.factory(unrestricted, correction);
        assertEquals(expected.normalize(), reduced.normalize());
        assertEquals(domain, reduced.getDomain());
        assertEquals(domain, reduced.getCodomain());
    }

    @ParameterizedTest
    @MethodSource("nonSingletonSubclassCases")
    void baseFilterReduction_restrictsBothComponentsKeepingEquality(boolean equality, boolean reverse) throws Exception {
        ColorClass cc = new ColorClass(7, new Interval[]{new Interval(2, 2), new Interval(3, 3)});
        Domain domain = new Domain(cc, cc);

        All all = All.getInstance(cc);
        Subcl subclass = Subcl.factory(1, cc);

        Guard filter = Equality.builder(Projection.builder(1, cc), Projection.builder(2, cc), equality, domain);

        List<ColorFunction> components = reverse ? List.of(subclass, all) : List.of(all, subclass);

        WNtuple tuple = new WNtuple(filter, components, True.getInstance(domain), true);

        ArcFunction result = invokeBaseFilterReduction(tuple, filter.equalityMap(), filter.membMap());

        if (equality) {
            WNtuple expected = new WNtuple(filter, List.of(subclass, subclass), True.getInstance(domain), true);

            assertEquals(expected, result);
            assertEquals(filter, ((WNtuple) result).filter());
            assertEquals(expected, result.normalize());
        } else {
            assertSame(tuple, result);
        }
    }

    private static Stream<Arguments> nonSingletonSubclassCases() {
        return Stream.of(
                Arguments.of(true, false),
                Arguments.of(true, true),
                Arguments.of(false, false),
                Arguments.of(false, true)
        );
    }

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
    void baseCompose_replacesProjectionWithLinearComb() {
        ColorClass cc = new ColorClass(2, 3);

        Domain leftDomain = new Domain(cc);
        Domain rightDomain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb replacement = new LinearComb(p2, 3);

        WNtuple left = new WNtuple(List.of(p1), True.getInstance(leftDomain), false);
        WNtuple right = new WNtuple(List.of(replacement, new LinearComb(p1)), True.getInstance(rightDomain), false);
        WNtuple result = (WNtuple) left.baseCompose(right);

        LinearComb expected = new LinearComb(p2, 3);
        assertEquals(expected, result.getComponents().getFirst());
    }

    @Test
    void baseCompose_returnsEmptyLinearCombWhenReplacementIsEmpty() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        LinearComb empty = new LinearComb(cc);

        WNtuple left = new WNtuple(List.of(p1), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(empty), True.getInstance(domain), false);
        WNtuple result = (WNtuple) left.baseCompose(right);

        LinearComb actual = (LinearComb) result.getComponents().getFirst();
        assertTrue(actual.asMap().isEmpty());
    }

    @Test
    void baseCompose_returnsEmptyLinearCombWhenLeftLinearCombIsEmpty() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc);

        LinearComb empty = new LinearComb(cc);

        WNtuple left = new WNtuple(List.of(empty), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(new LinearComb(cc)), True.getInstance(domain), false);
        WNtuple result = (WNtuple) left.baseCompose(right);

        LinearComb actual = (LinearComb) result.getComponents().getFirst();

        assertTrue(actual.asMap().isEmpty());
    }

    @Test
    void repetedIndexCompose_doesNotAddFilterWithoutRepeatedIndices() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        WNtuple left = new WNtuple(List.of(p1, p2), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(new LinearComb(p2), new LinearComb(p1)), True.getInstance(domain), false);

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
    void repetedIndexCompose_preservesSuccessorBetweenRepeatedIndices() {
        ColorClass cc = new ColorClass(2, 9, true);

        Domain inputDomain = new Domain(cc);
        Domain intermediateDomain = new Domain(cc);
        Domain outputDomain = new Domain(cc, cc);

        // <c1, !(c1)>
        WNtuple left = new WNtuple(
                True.getInstance(outputDomain),
                List.of(Projection.builder(1, cc), Projection.builder(1, 1, cc)),
                True.getInstance(intermediateDomain),
                true
        );

        // <S_C>
        WNtuple right = new WNtuple(
                True.getInstance(intermediateDomain),
                List.of(All.getInstance(cc)),
                True.getInstance(inputDomain),
                true
        );

        ArcFunction composed = left.repetedIndexCompose(right);

        assertInstanceOf(WNtuple.class, composed);
        WNtuple result = (WNtuple) composed;

        // c1 = !-1(c2), equivalente a c2 = !(c1).
        Guard expectedFilter = Equality.builder(Projection.builder(1, cc), Projection.builder(2, -1, cc), true, outputDomain);

        assertAll(
                () -> assertEquals(expectedFilter, result.filter(), "Gli indici ripetuti devono mantenere " + "la differenza tra i successori"),

                () -> assertEquals(List.of(All.getInstance(cc), All.getInstance(cc)), result.getComponents(), "Il successore dell'insieme universale è ancora S_C"),

                () -> assertEquals(True.getInstance(inputDomain), result.guard()),

                () -> assertEquals(inputDomain, result.getDomain()),

                () -> assertEquals(outputDomain, result.getCodomain())
        );
    }

    @Test
    void composition_expandsRepeatedIndexWithDifferentSuccessors() {
        ColorClass cc = new ColorClass(2, 9, true);

        Domain inputDomain = new Domain(cc);
        Domain intermediateDomain = new Domain(cc);
        Domain outputDomain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection succP1 = Projection.builder(1, 1, cc);

        WNtuple left = new WNtuple(
                True.getInstance(outputDomain),
                List.of(new LinearComb(p1, succP1), new LinearComb(p1)),
                True.getInstance(intermediateDomain),
                true
        );

        WNtuple right = new WNtuple(
                True.getInstance(intermediateDomain),
                List.of(new LinearComb(All.getInstance(cc))),
                True.getInstance(inputDomain),
                true
        );

        Guard equalFilter = Equality.builder(
                Projection.builder(1, cc),
                Projection.builder(2, cc),
                true,
                outputDomain
        );

        WNtuple equalPairs = new WNtuple(
                equalFilter,
                List.of(All.getInstance(cc), All.getInstance(cc)),
                True.getInstance(inputDomain),
                true
        );

        Guard successorFilter = Equality.builder(
                Projection.builder(1, cc),
                Projection.builder(2, 1, cc),
                true,
                outputDomain
        );

        WNtuple successorPairs = new WNtuple(
                successorFilter,
                List.of(All.getInstance(cc), All.getInstance(cc)),
                True.getInstance(inputDomain),
                true
        );

        ArcFunction expected = ArcFunSum.factory(equalPairs, successorPairs).normalize();

        ArcFunction result = assertDoesNotThrow(() -> new ArcFunComp(left, right).normalize(), "La componente con più termini deve essere espansa " + "prima di costruire il filtro degli indici ripetuti");

        assertAll(
                () -> assertEquals(expected, result, "La composizione deve conservare entrambi i contributi: " + "uguaglianza e relazione di successore"),
                () -> assertEquals(inputDomain, result.getDomain()),
                () -> assertEquals(outputDomain, result.getCodomain())
        );
    }

    private ArcFunction invokeSplitAtPositions(WNtuple tuple, Map<ColorClass, Set<Integer>> targetPositions) throws Exception {
        return (ArcFunction) splitAtPositionsMethod.invoke(tuple, targetPositions);
    }

    @Test
    void splitAtPositions_returnsSameTupleWhenNoPositionIsExpandable() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        LinearComb lc = new LinearComb(p, 2);

        WNtuple tuple = new WNtuple(List.of(lc), True.getInstance(domain), false);

        ArcFunction result = invokeSplitAtPositions(tuple, Map.of(cc, Set.of(2)));
        assertSame(tuple, result);
    }

    @Test
    void splitAtPositions_doesNotExpandLinearCombWithSingleElement() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        LinearComb lc = new LinearComb(p1, 2);

        WNtuple tuple = new WNtuple(List.of(lc), True.getInstance(domain), false);

        ArcFunction result = invokeSplitAtPositions(tuple, Map.of(cc, Set.of(1)));
        assertSame(tuple, result);
    }

    @Test
    void splitAtPositions_doesNotWrapTupleWhenMultiplicityIsOne() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(new Pair<>(p1, 1), new Pair<>(p2, 1));

        WNtuple tuple = new WNtuple(List.of(lc, p1), True.getInstance(domain), false);

        ArcFunction result = invokeSplitAtPositions(tuple, Map.of(cc, Set.of(1)));
        assertInstanceOf(ArcFunSum.class, result);
    }

    @Test
    void splitAtPositions_doesNotExpandWhenColorClassIsNotTargeted() throws Exception {
        ColorClass cc1 = new ColorClass(2, 9);
        ColorClass cc2 = new ColorClass(3, 10);
        Domain domain = new Domain(cc1, cc1);

        Projection p1 = Projection.builder(1, cc1);
        Projection p2 = Projection.builder(2, cc1);

        LinearComb lc = new LinearComb(new Pair<>(p1, 1), new Pair<>(p2, 1));

        WNtuple tuple = new WNtuple(List.of(lc, p1), True.getInstance(domain), false);

        ArcFunction result = invokeSplitAtPositions(tuple, Map.of(cc2, Set.of(1)));
        assertSame(tuple, result);
    }

    @Test
    void splitAtPositions_doesNotExpandWhenPositionIsNotTargeted() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(new Pair<>(p1, 1), new Pair<>(p2, 1));

        WNtuple tuple = new WNtuple(List.of(lc, p1), True.getInstance(domain), false);

        ArcFunction result = invokeSplitAtPositions(tuple, Map.of(cc, Set.of(2)));
        assertSame(tuple, result);
    }

    private Map<ColorClass, Set<Integer>> invokeFilterIdx(WNtuple tuple) throws Exception {
        return (Map<ColorClass, Set<Integer>>) filterIdxMethod.invoke(tuple, tuple);
    }

    @Test
    void testFilterIdx_FilterEquality() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);
        Guard filter = And.factory(equality);
        WNtuple tuple = new WNtuple(filter, List.of(p1, p2), True.getInstance(domain), false);

        Map<ColorClass, Set<Integer>> result = invokeFilterIdx(tuple);

        assertEquals(Set.of(1, 2), result.get(cc));
    }

    @Test
    void testFilterIdx_FilterEqualityAndMembership() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});

        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);
        Membership membership = Membership.build(p3, Subcl.factory(1, cc), domain);

        Guard filter = And.factory(equality, membership);
        WNtuple tuple = new WNtuple(filter, List.of(p1, p2, p3), True.getInstance(domain), false);

        Map<ColorClass, Set<Integer>> result = invokeFilterIdx(tuple);

        assertEquals(Set.of(1, 2), result.get(cc));
    }

    @Test
    void testSpecSimplify_ExtractLinearCombWithCoefficientOne() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(p1, 1);

        WNtuple tuple = new WNtuple(List.of(lc, p2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();

        assertInstanceOf(WNtuple.class, result);

        WNtuple simplified = (WNtuple) result;
        assertEquals(p1, simplified.getComponents().getFirst());
    }

    @Test
    void testSpecSimplify_ExtractLinearCombWithCoefficient() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(p1, 3);

        WNtuple tuple = new WNtuple(List.of(lc, p2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();

        assertInstanceOf(ArcFunScalar.class, result);

        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(3, scalar.k());

        WNtuple simplified = (WNtuple) scalar.getArg();
        assertEquals(p1, simplified.getComponents().getFirst());
    }

    @Test
    void testSpecSimplify_ExtractLinearCombScalar() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(p1, p2);
        LinearCombScalar scalarComponent = new LinearCombScalar(lc, 4);

        WNtuple tuple = new WNtuple(List.of(scalarComponent, p1), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();

        assertInstanceOf(ArcFunScalar.class, result);

        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(4, scalar.k());

        WNtuple simplified = (WNtuple) scalar.getArg();
        assertEquals(lc, simplified.getComponents().getFirst());
    }

    @Test
    void testSpecSimplify_ExtractMultipleCoefficients() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc1 = new LinearComb(p1, 2);
        LinearComb lc2 = new LinearComb(p2, 3);

        WNtuple tuple = new WNtuple(List.of(lc1, lc2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();

        assertInstanceOf(ArcFunScalar.class, result);

        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(6, scalar.k());

        WNtuple simplified = (WNtuple) scalar.getArg();

        assertEquals(p1, simplified.getComponents().get(0));
        assertEquals(p2, simplified.getComponents().get(1));
    }

    @Test
    void testSpecSimplify_ExtractCoefficientBeforeFilterReduction() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);
        Guard filter = And.factory(equality);

        LinearComb lc = new LinearComb(p1, 3);

        WNtuple tuple = new WNtuple(filter, List.of(lc, p2), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();

        assertInstanceOf(ArcFunScalar.class, result);

        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(3, scalar.k());

        WNtuple simplified = (WNtuple) scalar.getArg();

        assertEquals(filter, simplified.filter());
        assertEquals(p1, simplified.getComponents().get(0));
        assertEquals(p2, simplified.getComponents().get(1));
    }

    @Test
    void testSpecSimplify_TrivialFilter() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);

        WNtuple tuple = new WNtuple(List.of(p1), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertSame(tuple, result);
    }

    @Test
    void testSpecSimplify_FilterEqualityReduction() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality equality = (Equality) Equality.builder(p1, p2, true, domain);
        Guard filter = And.factory(equality);

        WNtuple tuple = new WNtuple(filter, List.of(p1, p1), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertTrue(simplified.filter().isTrivial());
    }

    @Test
    void testSpecSimplify_FilterMembershipReduction() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Subcl subcl = Subcl.factory(1, cc);

        Membership membership = Membership.build(p1, subcl, domain);
        Guard filter = And.factory(membership);

        WNtuple tuple = new WNtuple(filter, List.of(All.getInstance(cc)), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertInstanceOf(WNtuple.class, result);
        WNtuple simplified = (WNtuple) result;
        assertTrue(simplified.filter().isTrivial());
        assertEquals(subcl, simplified.getComponents().getFirst());
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

    @ParameterizedTest(name = "equality={0}, linearComb={1}")
    @MethodSource("baseGuardReductionSuccessorCases")
    void baseGuardReduction_movesRelationWithSuccessors(boolean equality, boolean linearComb) {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p2 = Projection.builder(2, 2, cc);
        Projection p1 = Projection.builder(1, 1, cc);

        // <!2(c2), !c1>
        List<ColorFunction> components = linearComb ? List.of(new LinearComb(p2, 2), new LinearComb(p1, 3)) : List.of(p2, p1);

        // !c1 =/!= !3(c2)
        Guard originalGuard = Equality.builder(p1, Projection.builder(2, 3, cc), equality, domain);

        WNtuple tuple = new WNtuple(True.getInstance(domain), components, originalGuard, true);

        WNtuple result = tuple.baseGuardReduction();

        // c1 =/!= !-1(c2).
        Guard expectedFilter = Equality.builder(Projection.builder(1, cc), Projection.builder(2, -1, cc), equality, tuple.getCodomain());

        assertAll(
                () -> assertNotSame(tuple, result),

                () -> assertEquals(expectedFilter, result.filter(), "Il filtro deve compensare i successori e usare le posizioni"),

                () -> assertEquals(True.getInstance(domain), result.guard(), "La clausola trasferita deve essere rimossa dalla guardia"),

                () -> assertEquals(tuple.getComponents(), result.getComponents(), "Le componenti e le molteplicità devono essere preservate"),

                () -> assertEquals(tuple.getDomain(), result.getDomain()),
                () -> assertEquals(tuple.getCodomain(), result.getCodomain()),

                () -> assertEquals(originalGuard, tuple.guard(), "La tupla originale non deve essere modificata")
        );
    }

    static Stream<Arguments> baseGuardReductionSuccessorCases() {
        return Stream.of(
                Arguments.of(true, false),
                Arguments.of(false, false),
                Arguments.of(true, true),
                Arguments.of(false, true)
        );
    }

    @Test
    void typeSetFilterReduction_matchesFiniteDomainSemantics() throws Exception {
        FilterReductionSemanticTest.main(new String[0]);
    }

    @Test
    void normalize_reducesFilteredLinearCombinationPreservingMultiplicity() {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Guard inequality = Equality.builder(p1, p2, false, domain);

        WNtuple tuple = new WNtuple(inequality, List.of(p1, new LinearComb(Map.of(p1, 2, p2, 3))), True.getInstance(domain), true);

        WNtuple survivingTuple = new WNtuple(inequality, List.of(p1, p2), True.getInstance(domain), true);

        ArcFunction expected = new ArcFunScalar(survivingTuple, 3).normalize();

        ArcFunction result = tuple.normalize();

        assertEquals(expected, result, "La normalizzazione deve eliminare il contributo <x1,x1> " + "e conservare il coefficiente 3 del contributo <x1,x2>");
    }

    private static Stream<Arguments> specSimplifyCases() {
        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        All all = All.getInstance(cc);
        Guard trueGuard = True.getInstance(domain);

        Guard eq = Equality.builder(p1, p2, true, domain);
        Guard ineq = Equality.builder(p1, p2, false, domain);

        WNtuple identical = new WNtuple(
                trueGuard, List.of(p1, p1), trueGuard, true
        );

        return Stream.of(
                Arguments.of(
                        "eq - p2, p1",
                        eq,
                        List.of(new LinearComb(p2), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard, List.of(p2, p1), eq, true
                        )
                ),
                Arguments.of(
                        "eq - All, All",
                        eq,
                        List.of(new LinearComb(all), new LinearComb(all)),
                        new WNtuple(
                                eq, List.of(all, all), trueGuard, true
                        )
                ),
                Arguments.of(
                        "ineq - p2, p1",
                        ineq,
                        List.of(new LinearComb(p2), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard, List.of(p2, p1), ineq, true
                        )
                ),
                Arguments.of(
                        "ineq - All, All",
                        ineq,
                        List.of(new LinearComb(all), new LinearComb(all)),
                        new WNtuple(
                                ineq, List.of(all, all), trueGuard, true
                        )
                ),
                Arguments.of(
                        "eq - p1, p1",
                        eq,
                        List.of(new LinearComb(p1), new LinearComb(p1)),
                        identical
                ),
                Arguments.of(
                        "ineq - p1, p1",
                        ineq,
                        List.of(new LinearComb(p1), new LinearComb(p1)),
                        identical.getEmpty()
                ),
                Arguments.of(
                        "eq - All, p1",
                        eq,
                        List.of(new LinearComb(all), new LinearComb(p1)),
                        identical
                ),
                Arguments.of(
                        "ineq - All, p1",
                        ineq,
                        List.of(new LinearComb(all), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard,
                                List.of(
                                        new LinearComb(Map.of(all, 1, p1, -1)),
                                        p1
                                ),
                                trueGuard,
                                true
                        )
                )
        );
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource({
            "specSimplifyCases",
            "specSimplifyCasesWithSuccessor",
            "specSimplifyCasesWithTupleSuccessor"
    })
    void testSpecSimplify(
            String description,
            Guard filter,
            List<ColorFunction> components,
            ArcFunction expected
    ) {
        WNtuple tuple = new WNtuple(
                filter,
                components,
                True.getInstance(filter.getDomain()),
                true
        );

        ArcFunction result = tuple.specSimplify();

        System.out.println(tuple);
        System.out.println("Result: " + result + "\n");

        assertEquals(expected.getDomain(), result.getDomain(), description);
        assertEquals(expected.getCodomain(), result.getCodomain(), description);

        assertEquals(
                expected.normalize(),
                result.normalize(),
                description
        );
    }

    private static Stream<Arguments> specSimplifyCasesWithSuccessor() {
        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p1Succ = Projection.builder(1, 1, cc);
        Projection p2Succ = Projection.builder(2, 1, cc);

        All all = All.getInstance(cc);
        Guard trueGuard = True.getInstance(domain);

        // Filtro sulle uscite: y1 =/!= succ(y2).
        Guard eq = Equality.builder(p1, p2Succ, true, domain);
        Guard ineq = Equality.builder(p1, p2Succ, false, domain);

        // Per <x2, x1>, la condizione sugli ingressi diventa:
        // x2 =/!= succ(x1).
        Guard inputEq = Equality.builder(p2, p1Succ, true, domain);
        Guard inputIneq = Equality.builder(p2, p1Succ, false, domain);

        WNtuple identical = new WNtuple(
                trueGuard, List.of(p1, p1), trueGuard, true
        );

        return Stream.of(
                Arguments.of(
                        "successor in filter: eq - p2, p1",
                        eq,
                        List.of(new LinearComb(p2), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard, List.of(p2, p1), inputEq, true
                        )
                ),
                Arguments.of(
                        "successor in filter: eq - All, All",
                        eq,
                        List.of(new LinearComb(all), new LinearComb(all)),
                        new WNtuple(
                                eq, List.of(all, all), trueGuard, true
                        )
                ),
                Arguments.of(
                        "successor in filter: ineq - p2, p1",
                        ineq,
                        List.of(new LinearComb(p2), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard, List.of(p2, p1), inputIneq, true
                        )
                ),
                Arguments.of(
                        "successor in filter: ineq - All, All",
                        ineq,
                        List.of(new LinearComb(all), new LinearComb(all)),
                        new WNtuple(
                                ineq, List.of(all, all), trueGuard, true
                        )
                ),
                Arguments.of(
                        "successor in filter: eq - p1, p1",
                        eq,
                        List.of(new LinearComb(p1), new LinearComb(p1)),
                        identical.getEmpty()
                ),
                Arguments.of(
                        "successor in filter: ineq - p1, p1",
                        ineq,
                        List.of(new LinearComb(p1), new LinearComb(p1)),
                        identical
                ),
                Arguments.of(
                        "successor in filter: eq - All, p1",
                        eq,
                        List.of(new LinearComb(all), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard, List.of(p1Succ, p1), trueGuard, true
                        )
                ),
                Arguments.of(
                        "successor in filter: ineq - All, p1",
                        ineq,
                        List.of(new LinearComb(all), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard,
                                List.of(
                                        new LinearComb(Map.of(all, 1, p1Succ, -1)),
                                        p1
                                ),
                                trueGuard,
                                true
                        )
                )
        );
    }

    private static Stream<Arguments> specSimplifyCasesWithTupleSuccessor() {
        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p1Succ = Projection.builder(1, 1, cc);
        Projection p2Succ = Projection.builder(2, 1, cc);

        All all = All.getInstance(cc);
        Guard trueGuard = True.getInstance(domain);

        // Filtro sulle uscite: y1 =/!= y2.
        Guard eq = Equality.builder(p1, p2, true, domain);
        Guard ineq = Equality.builder(p1, p2, false, domain);

        // Per <succ(x2), x1>, la condizione sugli ingressi diventa:
        // succ(x2) =/!= x1.
        Guard inputEq = Equality.builder(p2Succ, p1, true, domain);
        Guard inputIneq = Equality.builder(p2Succ, p1, false, domain);

        WNtuple shifted = new WNtuple(
                trueGuard, List.of(p1Succ, p1), trueGuard, true
        );

        return Stream.of(
                Arguments.of(
                        "successor in tuple: eq - p2', p1",
                        eq,
                        List.of(new LinearComb(p2Succ), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard, List.of(p2Succ, p1), inputEq, true
                        )
                ),
                Arguments.of(
                        "successor in tuple: ineq - p2', p1",
                        ineq,
                        List.of(new LinearComb(p2Succ), new LinearComb(p1)),
                        new WNtuple(
                                trueGuard, List.of(p2Succ, p1), inputIneq, true
                        )
                ),
                Arguments.of(
                        "successor in tuple: eq - p1', p1",
                        eq,
                        List.of(new LinearComb(p1Succ), new LinearComb(p1)),
                        shifted.getEmpty()
                ),
                Arguments.of(
                        "successor in tuple: ineq - p1', p1",
                        ineq,
                        List.of(new LinearComb(p1Succ), new LinearComb(p1)),
                        shifted
                ),
                Arguments.of(
                        "successor in tuple: eq - All, p1'",
                        eq,
                        List.of(new LinearComb(all), new LinearComb(p1Succ)),
                        new WNtuple(
                                trueGuard,
                                List.of(p1Succ, p1Succ),
                                trueGuard,
                                true
                        )
                ),
                Arguments.of(
                        "successor in tuple: ineq - All, p1'",
                        ineq,
                        List.of(new LinearComb(all), new LinearComb(p1Succ)),
                        new WNtuple(
                                trueGuard,
                                List.of(
                                        new LinearComb(Map.of(all, 1, p1Succ, -1)),
                                        p1Succ
                                ),
                                trueGuard,
                                true
                        )
                )
        );
    }

}