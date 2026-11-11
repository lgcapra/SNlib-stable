
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

    private Method cardLeq1Method;
    private Method baseFilterReductionMethod;
    private Method succMultiMethod;
    private Method differMultiMethod;
    private Method interMultiMethod;
    private Method splitAtPositionsMethod;
    private Method filterIdxMethod;

    @BeforeEach
    void setUp() throws Exception {
        cardLeq1Method = WNtuple.class.getDeclaredMethod("cardLeq1", ColorFunction.class);
        cardLeq1Method.setAccessible(true);
        baseFilterReductionMethod = WNtuple.class.getDeclaredMethod("baseFilterReduction", Map.class, Map.class);
        baseFilterReductionMethod.setAccessible(true);
        succMultiMethod = WNtuple.class.getDeclaredMethod("succMulti", int.class, ColorFunction.class);
        succMultiMethod.setAccessible(true);
        differMultiMethod = WNtuple.class.getDeclaredMethod("differMulti", ColorFunction.class, ColorFunction.class);
        differMultiMethod.setAccessible(true);
        interMultiMethod = WNtuple.class.getDeclaredMethod("interMulti", ColorFunction.class, ColorFunction.class);
        interMultiMethod.setAccessible(true);
        splitAtPositionsMethod = WNtuple.class.getDeclaredMethod("splitAtPositions", Map.class);
        splitAtPositionsMethod.setAccessible(true);
        filterIdxMethod = WNtuple.class.getDeclaredMethod("FilterIdx", WNtuple.class);
        filterIdxMethod.setAccessible(true);
    }

    private boolean invokeCardLeq1(WNtuple tuple, ColorFunction cf) throws Exception {
        return (boolean) cardLeq1Method.invoke(tuple, cf);
    }

    @ParameterizedTest
    @MethodSource("cardLeq1Cases")
    void cardLeq1_returnsExpectedResult(WNtuple tuple, ColorFunction function, boolean expected) throws Exception {
        assertEquals(expected, invokeCardLeq1(tuple, function));
    }

    static Stream<Arguments> cardLeq1Cases() {
        // Caso 1: Projection
        ColorClass cc1 = new ColorClass(2, 9);
        Domain domain1 = new Domain(cc1);
        Projection p1 = Projection.builder(1, cc1);
        WNtuple tuple1 = new WNtuple(List.of(p1), domain1);

        // Caso 2: LinearComb vuota
        ColorClass cc2 = new ColorClass(2, 9);
        Domain domain2 = new Domain(cc2);
        LinearComb lc2 = new LinearComb(cc2);
        WNtuple tuple2 = new WNtuple(
                List.of(Projection.builder(1, cc2)), domain2);

        // Caso 3: LinearComb con una Projection e coefficiente 1
        ColorClass cc3 = new ColorClass(2, 9, true);
        Domain domain3 = new Domain(cc3);
        Projection p3 = Projection.builder(1, cc3);
        LinearComb lc3 = new LinearComb(p3, 1);
        WNtuple tuple3 = new WNtuple(List.of(p3), domain3);

        // Caso 4: LinearComb con una Projection e coefficiente 2
        ColorClass cc4 = new ColorClass(2, 9, true);
        Domain domain4 = new Domain(cc4);
        Projection p4 = Projection.builder(1, cc4);
        LinearComb lc4 = new LinearComb(p4, 2);
        WNtuple tuple4 = new WNtuple(List.of(p4), domain4);

        // Caso 5: LinearComb con più elementi
        ColorClass cc5 = new ColorClass(2, 9, true);
        Domain domain5 = new Domain(cc5);
        Projection p5a = Projection.builder(1, cc5);
        Projection p5b = Projection.builder(2, cc5);
        LinearComb lc5 = new LinearComb(p5a, p5b);
        WNtuple tuple5 = new WNtuple(List.of(p5a), domain5);

        return Stream.of(
                Arguments.of(tuple1, p1, true),
                Arguments.of(tuple2, lc2, true),
                Arguments.of(tuple3, lc3, true),
                Arguments.of(tuple4, lc4, false),
                Arguments.of(tuple5, lc5, false)
        );
    }

    private ColorFunction invokeSuccMulti(WNtuple tuple, int k, ColorFunction cf) throws Exception {
        return (ColorFunction) succMultiMethod.invoke(tuple, k, cf);
    }

    @Test
    void succMulti_returnsSameFunctionWhenKIsZero() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        ColorFunction result = invokeSuccMulti(tuple, 0, p);

        assertSame(p, result);
    }

    @Test
    void succMulti_incrementsProjectionSuccessor() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        int initialSucc = p.getSucc();

        WNtuple tuple = new WNtuple(List.of(p), domain);

        int k = 2;
        Projection result = (Projection) invokeSuccMulti(tuple, k, p);

        assertEquals(initialSucc + k, result.getSucc());
    }

    @Test
    void succMulti_incrementsProjectionsInsideLinearComb() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        LinearComb lc = new LinearComb(p1, p2);

        WNtuple tuple = new WNtuple(List.of(p1), domain);

        LinearComb result = (LinearComb) invokeSuccMulti(tuple, 1, lc);

        assertEquals(2, result.asMap().size());
        assertTrue(result.asMap().containsKey(p1.setExp(p1.getSucc() + 1)));
        assertTrue(result.asMap().containsKey(p2.setExp(p2.getSucc() + 1)));
    }

    @Test
    void succMulti_preservesMultiplicitiesInsideLinearComb() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        LinearComb lc = new LinearComb(p, 3);

        WNtuple tuple = new WNtuple(List.of(p), domain);

        LinearComb result = (LinearComb) invokeSuccMulti(tuple, 1, lc);

        Projection shifted = p.setExp(p.getSucc() + 1);

        assertEquals(3, result.asMap().get(shifted));
    }

    @Test
    void toLinearComb_returnsSameLinearCombWhenFunctionIsLinearComb() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        LinearComb lc = new LinearComb(p, 2);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        Method method = WNtuple.class.getDeclaredMethod("toLinearComb", ColorFunction.class);
        method.setAccessible(true);

        LinearComb result = (LinearComb) method.invoke(tuple, lc);

        assertSame(lc, result);
    }

    @Test
    void toLinearComb_convertsElementaryFunctionToLinearCombWithMultiplicityOne() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        Method method = WNtuple.class.getDeclaredMethod("toLinearComb", ColorFunction.class);
        method.setAccessible(true);

        LinearComb result = (LinearComb) method.invoke(tuple, p);

        assertEquals(1, result.asMap().size());
        assertEquals(1, result.asMap().get(p));
    }

    private LinearComb invokeDifferMulti(WNtuple tuple, ColorFunction f1, ColorFunction f2) throws Exception {
        return (LinearComb) differMultiMethod.invoke(tuple, f1, f2);
    }

    @Test
    void differMulti_returnsEmptyLinearCombWhenFunctionsAreEqual() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        LinearComb result = invokeDifferMulti(tuple, p, p);

        assertTrue(result.asMap().isEmpty());
    }

    @Test
    void differMulti_subtractsCoefficientsOfCommonElements() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        LinearComb lc1 = new LinearComb(p, 3);
        LinearComb lc2 = new LinearComb(p, 1);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        LinearComb result = invokeDifferMulti(tuple, lc1, lc2);

        assertEquals(1, result.asMap().size());
        assertEquals(2, result.asMap().get(p));
    }

    @Test
    void differMulti_removesElementWhenDifferenceOfCoefficientsIsZero() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        LinearComb lc1 = new LinearComb(new Pair<>(p1, 2), new Pair<>(p2, 1));
        LinearComb lc2 = new LinearComb(new Pair<>(p1, 2), new Pair<>(p3, 1));

        WNtuple tuple = new WNtuple(List.of(p1), domain);

        LinearComb result = invokeDifferMulti(tuple, lc1, lc2);

        assertFalse(result.asMap().containsKey(p1));
        assertEquals(1, result.asMap().get(p2));
        assertEquals(-1, result.asMap().get(p3));
    }

    @Test
    void differMulti_returnsNegativeCoefficientWhenSecondMultiplicityIsGreater() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        LinearComb lc1 = new LinearComb(p, 1);
        LinearComb lc2 = new LinearComb(p, 3);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        LinearComb result = invokeDifferMulti(tuple, lc1, lc2);

        assertEquals(1, result.asMap().size());
        assertEquals(-2, result.asMap().get(p));
    }

    @Test
    void differMulti_preservesElementsPresentOnlyInFirstFunction() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        LinearComb lc1 = new LinearComb(p1, 2);
        LinearComb lc2 = new LinearComb(p2, 1);
        WNtuple tuple = new WNtuple(List.of(p1), domain);

        LinearComb result = invokeDifferMulti(tuple, lc1, lc2);

        assertEquals(2, result.asMap().get(p1));
        assertEquals(-1, result.asMap().get(p2));
    }

    @Test
    void differMulti_convertsElementaryFunctionsToLinearCombs() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        WNtuple tuple = new WNtuple(List.of(p1), domain);

        LinearComb result = invokeDifferMulti(tuple, p1, p2);

        assertEquals(1, result.asMap().get(p1));
        assertEquals(-1, result.asMap().get(p2));
    }

    private ColorFunction invokeInterMulti(WNtuple tuple, ColorFunction f1, ColorFunction f2) throws Exception {
        return (ColorFunction) interMultiMethod.invoke(tuple, f1, f2);
    }

    @Test
    void interMulti_returnsFirstFunctionWhenFunctionsAreEqual() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p = Projection.builder(1, cc);
        LinearComb lc = new LinearComb(p, 2);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        ColorFunction result = invokeInterMulti(tuple, lc, lc);

        assertSame(lc, result);
    }

    @Test
    void interMulti_returnsSecondFunctionWhenFirstIsAll() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        All all = All.getInstance(cc);
        Projection p = Projection.builder(1, cc);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        ColorFunction result = invokeInterMulti(tuple, all, p);

        assertSame(p, result);
    }

    @Test
    void interMulti_returnsFirstFunctionWhenSecondIsAll() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        All all = All.getInstance(cc);
        Projection p = Projection.builder(1, cc);
        WNtuple tuple = new WNtuple(List.of(p), domain);

        ColorFunction result = invokeInterMulti(tuple, p, all);

        assertSame(p, result);
    }

    @Test
    void interMulti_returnsCommonElementsWithMultiplicityOne() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        LinearComb lc1 = new LinearComb(new Pair<>(p1, 3), new Pair<>(p2, 2));
        LinearComb lc2 = new LinearComb(new Pair<>(p1, 1), new Pair<>(p3, 4));

        WNtuple tuple = new WNtuple(List.of(p1), domain);

        LinearComb result = (LinearComb) invokeInterMulti(tuple, lc1, lc2);

        assertEquals(1, result.asMap().size());
        assertEquals(1, result.asMap().get(p1));
    }

    @Test
    void interMulti_returnsEmptyLinearCombWhenThereAreNoCommonElements() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc1 = new LinearComb(p1, 2);
        LinearComb lc2 = new LinearComb(p2, 3);

        WNtuple tuple = new WNtuple(List.of(p1), domain);

        LinearComb result = (LinearComb) invokeInterMulti(tuple, lc1, lc2);

        assertTrue(result.asMap().isEmpty());
    }

    private ArcFunction invokeBaseFilterReduction(WNtuple tuple, Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap, Map<ColorClass, Map<Boolean, Set<Membership>>> membMap) throws Exception {
        return (ArcFunction) baseFilterReductionMethod.invoke(tuple, equalityMap, membMap);
    }

    @ParameterizedTest(name = "singletonFirst={0}, equality={1}")
    @MethodSource("baseFilterReductionCases")
    void baseFilterReduction_restrictsComponents(boolean singletonFirst, boolean equalitySign) throws Exception {

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

        assertNotSame(tuple, result);
        assertInstanceOf(WNtuple.class, result);
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
    void baseFilterReduction_removesInequalityBetweenDisjointComponents() throws Exception {
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

        assertNotSame(tuple, result);
        assertInstanceOf(WNtuple.class, result);
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
    void baseFilterReduction_distinctSingletons_preservesEquality() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        WNtuple tuple = new WNtuple(List.of(p1, p2), True.getInstance(domain), false);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();
        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add(eq);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertSame(tuple, result);
    }

    @Test
    void baseFilterReduction_orderedClassWithDifferentSuccessors() throws Exception {
        ColorClass cc = new ColorClass(2, 9, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, 0, cc);
        Projection p2 = Projection.builder(2, 1, cc);

        WNtuple tuple = new WNtuple(List.of(p1, p2), True.getInstance(domain), false);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);

        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = new TreeMap<>();
        SortedSet<Equality> equalities = new TreeSet<>();
        equalities.add(eq);
        equalityMap.put(cc, Map.of(true, equalities));

        Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = new TreeMap<>();
        membMap.put(cc, Map.of(true, Set.of()));

        ArcFunction result = invokeBaseFilterReduction(tuple, equalityMap, membMap);

        assertSame(tuple, result);
    }

    @Test
    void baseFilterReduction_membershipDoesNotChangeConstant() throws Exception {
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

        assertSame(tuple, result);
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

        assertSame(tuple, result);
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

        WNtuple left = new WNtuple(
                List.of(empty),
                True.getInstance(domain),
                false
        );

        WNtuple right = new WNtuple(
                List.of(new LinearComb(cc)),
                True.getInstance(domain),
                false
        );

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
    void testFilterIdx_FilterMembership() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain domain = new Domain(cc);
        Projection p1 = Projection.builder(1, cc);

        Membership membership = Membership.build(p1, Subcl.factory(1, cc), domain);

        Guard filter = And.factory(membership);

        WNtuple tuple = new WNtuple(filter, List.of(p1), True.getInstance(domain), false);

        Map<ColorClass, Set<Integer>> result = invokeFilterIdx(tuple);

        assertEquals(Set.of(1), result.get(cc));
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

        assertEquals(Set.of(1, 2, 3), result.get(cc));
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
    void testSpecSimplify_SplitAndRecursiveSimplification() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);

        Subcl subcl1 = Subcl.factory(1, cc);
        Subcl subcl2 = Subcl.factory(2, cc);

        LinearComb lc = new LinearComb(subcl1, subcl2);

        Membership membership = Membership.build(p1, subcl1, domain);
        Guard filter = And.factory(membership);

        WNtuple tuple = new WNtuple(filter, List.of(lc), True.getInstance(domain), false);

        ArcFunction result = tuple.specSimplify();
        assertNotSame(tuple, result);
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

    private static Stream<Arguments> specSimplifyCases() {
        System.out.println("\nBase Case\n");

        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality eq = (Equality) Equality.builder(
                p1, p2, true, domain
        );

        Equality ineq = (Equality) Equality.builder(
                p1, p2, false, domain
        );

        return Stream.of(
                Arguments.of(
                        "eq - p2, p1",
                        eq,
                        List.of(new LinearComb(p2), new LinearComb(p1)),
                        "[c_1_1 = c_1_2]<c_1_2,c_1_1>"
                ),
                Arguments.of(
                        "eq - All, All",
                        eq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(All.getInstance(cc))
                        ),
                        "[c_1_1 = c_1_2]<S_C_1,S_C_1>"
                ),
                Arguments.of(
                        "ineq - p2, p1",
                        ineq,
                        List.of(new LinearComb(p2), new LinearComb(p1)),
                        "<1c_1_2-1c_1_1,c_1_1>"
                ),
                Arguments.of(
                        "ineq - All, All",
                        ineq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(All.getInstance(cc))
                        ),
                        "[c_1_1 != c_1_2]<S_C_1,S_C_1>"
                ),
                Arguments.of(
                        "eq - p1, p1",
                        eq,
                        List.of(new LinearComb(p1), new LinearComb(p1)),
                        "<c_1_1,c_1_1>"
                ),
                Arguments.of(
                        "ineq - p1, p1",
                        ineq,
                        List.of(new LinearComb(p1), new LinearComb(p1)),
                        "<null>_@C_1^2"
                ),
                Arguments.of(
                        "eq - All, p1",
                        eq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(p1)
                        ),
                        "<c_1_1,c_1_1>"
                ),
                Arguments.of(
                        "ineq - All, p1",
                        ineq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(p1)
                        ),
                        "<1S_C_1-1c_1_1,c_1_1>"
                )
        );
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("specSimplifyCases")
    void testSpecSimplify(String description, Guard guard, List<ColorFunction> components, String expected) {

        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        WNtuple tuple = new WNtuple(
                guard,
                components,
                True.getInstance(domain),
                true
        );

        ArcFunction result = tuple.specSimplify();

        System.out.println(tuple);
        System.out.println("Result: " + result + "\n");

        assertEquals(expected, result.toString());
    }

    private static Stream<Arguments> specSimplifyCasesWithSuccessor() {
        System.out.println("\nSuccessor in filter\n");

        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Projection p2Succ = (Projection) Successor.factory(1, p2);

        Equality eq = (Equality) Equality.builder(
                p1, p2Succ, true, domain
        );

        Equality ineq = (Equality) Equality.builder(
                p1, p2Succ, false, domain
        );

        return Stream.of(
                Arguments.of(
                        "eq - p2, p1",
                        eq,
                        List.of(new LinearComb(p2), new LinearComb(p1))
                ),
                Arguments.of(
                        "eq - All, All",
                        eq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(All.getInstance(cc))
                        )
                ),
                Arguments.of(
                        "ineq - p2, p1",
                        ineq,
                        List.of(new LinearComb(p2), new LinearComb(p1))
                ),
                Arguments.of(
                        "ineq - All, All",
                        ineq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(All.getInstance(cc))
                        )
                ),
                Arguments.of(
                        "eq - p1, p1",
                        eq,
                        List.of(new LinearComb(p1), new LinearComb(p1))
                ),
                Arguments.of(
                        "ineq - p1, p1",
                        ineq,
                        List.of(new LinearComb(p1), new LinearComb(p1))
                ),
                Arguments.of(
                        "eq - All, p1",
                        eq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(p1)
                        )
                ),
                Arguments.of(
                        "ineq - All, p1",
                        ineq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(p1)
                        )
                )
        );
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("specSimplifyCasesWithSuccessor")
    void testSpecSimplify_WithSuccessor(String description, Guard guard, List<ColorFunction> components) {

        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        WNtuple tuple = new WNtuple(
                guard,
                components,
                True.getInstance(domain),
                true
        );

        System.out.println(tuple);
        System.out.println("Result: " + tuple.specSimplify() + "\n");
    }

    private static Stream<Arguments> specSimplifyCasesWithTupleSuccessor() {
        System.out.println("\nSuccessor in tuple\n");

        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Projection p1Succ = (Projection) Successor.factory(1, p1);
        Projection p2Succ = (Projection) Successor.factory(1, p2);

        Equality eq = (Equality) Equality.builder(
                p1, p2, true, domain
        );

        Equality ineq = (Equality) Equality.builder(
                p1, p2, false, domain
        );

        return Stream.of(
                Arguments.of("eq - p2', p1", eq,
                        List.of(new LinearComb(p2Succ), new LinearComb(p1))),

                Arguments.of("ineq - p2', p1", ineq,
                        List.of(new LinearComb(p2Succ), new LinearComb(p1))),

                Arguments.of("eq - p1', p1", eq,
                        List.of(new LinearComb(p1Succ), new LinearComb(p1))),

                Arguments.of("ineq - p1', p1", ineq,
                        List.of(new LinearComb(p1Succ), new LinearComb(p1))),

                Arguments.of("eq - All, p1'", eq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(p1Succ)
                        )),

                Arguments.of("ineq - All, p1'", ineq,
                        List.of(
                                new LinearComb(All.getInstance(cc)),
                                new LinearComb(p1Succ)
                        ))
        );
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("specSimplifyCasesWithTupleSuccessor")
    void testSpecSimplify_WithTupleSuccessor(String description, Guard guard, List<ColorFunction> components) {

        ColorClass cc = new ColorClass(1, 3, true);
        Domain domain = new Domain(cc, cc);

        WNtuple tuple = new WNtuple(
                guard,
                components,
                True.getInstance(domain),
                true
        );

        System.out.println(tuple);
        System.out.println("Result: " + tuple.specSimplify() + "\n");
    }

}
