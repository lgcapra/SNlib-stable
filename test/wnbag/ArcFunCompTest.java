package wnbag;

import bagexpr.Bag;
import classfunction.All;
import classfunction.ClassFunction;
import classfunction.Projection;
import classfunction.Subcl;
import color.ColorClass;
import expr.Interval;
import expr.Domain;
import guard.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import util.Util;
import wncolorfunction.ColorFunction;
import wncolorfunction.LinearComb;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ArcFunCompTest {

    @Test
    void lemma4_returnsScalarWhenRightFilterIsTrivial() throws Exception {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);

        WNtuple left = new WNtuple(True.getInstance(domain), List.of(p1), True.getInstance(domain), false);

        WNtuple right = new WNtuple(True.getInstance(domain), List.of(p1), True.getInstance(domain), false);

        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        int card = 5;

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, card);
        assertInstanceOf(ArcFunScalar.class, result);
        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(card, scalar.k());
        assertSame(comp, scalar.getArg());
    }

    @Test
    void lemma4_returnsScalarWhenFilterHasNoInequalities() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3)});
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);
        Subcl subcl = Subcl.factory(1, cc);

        Membership membership = Membership.build(p1, subcl, domain);

        WNtuple left = new WNtuple(List.of(p1), True.getInstance(domain), false);
        WNtuple right = new WNtuple(membership, List.of(p1), True.getInstance(domain), false);
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        int card = 5;

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, card);
        assertInstanceOf(ArcFunScalar.class, result);
        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(card, scalar.k());
        assertSame(comp, scalar.getArg());
    }

    @Test
    void lemma4_returnsScalarWhenThereAreNoExtraVariables() throws Exception {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality inequality = (Equality) Equality.builder(p1, p2, false, domain);
        Guard filter = And.factory(List.of(inequality), true);

        WNtuple left = new WNtuple(List.of(p1, p2), True.getInstance(domain), false);
        WNtuple right = new WNtuple(filter, List.of(p1, p2), True.getInstance(domain), false);
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        int card = 5;

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, 5);
        assertInstanceOf(ArcFunScalar.class, result);
        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(card, scalar.k());
        assertSame(comp, scalar.getArg());
    }

    @Test
    void lemma4_returnsNullWhenCardLbIsUnknown() throws Exception {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        LinearComb extraComponent = new LinearComb(Map.of(p2, 1, p3, -1));

        assertNull(extraComponent.cardLb());

        Equality inequality = (Equality) Equality.builder(p1, p2, false, domain);
        Guard filter = And.factory(List.of(inequality), true);

        WNtuple left = new WNtuple(List.of(p1), True.getInstance(domain), false);
        WNtuple right = new WNtuple(filter, List.of(p1, extraComponent, p3), True.getInstance(domain), false);
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, 5);
        assertNull(result);
    }

    @Test
    void lemma4_multipliesChromaticPolynomialForExtraOnlyComponent() throws Exception {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p1));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(p1, p2, p3));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));
        Equality inequality = (Equality) Equality.builder(p2, p3, false, domain);
        Guard filter = And.factory(List.of(inequality), true);
        WNtuple right = new WNtuple(filter, rightMap, True.getInstance(domain));

        assertEquals(1, left.getComponents().size());
        assertEquals(3, right.getComponents().size());
        assertEquals(Set.of(1), ClassFunction.indexSet(left.getComponents()));
        assertEquals(Set.of(2, 3), inequality.indexSet());

        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        int card = 5;

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, card);
        assertInstanceOf(ArcFunScalar.class, result);
        ArcFunScalar scalar = (ArcFunScalar) result;
        Integer lambda = p2.cardLb();
        Integer expectedK = card * lambda * (lambda - 1);
        assertEquals(expectedK, scalar.k());
    }

    @Test
    void lemma4_returnsNullWhenMixedComponentIsNotClique() throws Exception {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3), new Interval(4, 4)});
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p1, p2));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));

        Equality ineq1 = (Equality) Equality.builder(p1, p3, false, domain);
        Equality ineq2 = (Equality) Equality.builder(p2, p3, false, domain);
        Guard filter = And.factory(List.of(ineq1, ineq2), true);

        WNtuple right = new WNtuple(filter, rightMap, True.getInstance(domain));
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, 5);
        assertEquals(9, All.getInstance(cc).cardLb());
        assertNull(result);
    }

    @Test
    void lemma4_returnsZeroWhenLambdaIsSmallerThanCliqueSize() throws Exception {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p1, p2));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(p1, p2, p3));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));

        Equality ineq1 = (Equality) Equality.builder(p1, p2, false, domain);
        Equality ineq2 = (Equality) Equality.builder(p1, p3, false, domain);
        Guard filter = And.factory(List.of(ineq1, ineq2), true);

        WNtuple right = new WNtuple(filter, rightMap, True.getInstance(domain));

        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, 5);

        assertInstanceOf(ArcFunScalar.class, result);

        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(0, scalar.k());
    }

    @Test
    void lemma4_returnsScalarWhenMixedComponentIsClique() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p1, p2));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));

        Equality ineq1 = (Equality) Equality.builder(p1, p2, false, domain);
        Equality ineq2 = (Equality) Equality.builder(p1, p3, false, domain);
        Equality ineq3 = (Equality) Equality.builder(p2, p3, false, domain);
        Guard filter = And.factory(List.of(ineq1, ineq2, ineq3), true);

        WNtuple right = new WNtuple(filter, rightMap, True.getInstance(domain));
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        int card = 5;

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, card);
        assertInstanceOf(ArcFunScalar.class, result);
        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(9, All.getInstance(cc).cardLb());
        assertEquals(35, scalar.k());
    }

    @Test
    void lemma4_scalesLeftTupleAndRemainingGuards() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc, cc);

        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p2, p3));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));

        Equality ineq1 = (Equality) Equality.builder(p2, p3, false, domain);
        Equality ineq2 = (Equality) Equality.builder(p2, p4, false, domain);
        Equality ineq3 = (Equality) Equality.builder(p3, p4, false, domain);
        Guard filter = And.factory(List.of(ineq1, ineq2, ineq3), true);

        WNtuple right = new WNtuple(filter, rightMap, True.getInstance(domain));
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("lemma4", ArcFunComp.class, int.class);
        method.setAccessible(true);

        int card = 5;

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, card);
        assertInstanceOf(ArcFunScalar.class, result);
        ArcFunScalar scalar = (ArcFunScalar) result;
        assertEquals(9, All.getInstance(cc).cardLb());
        assertEquals(35, scalar.k());
    }

    @Test
    void property15_multipliesCardinalityOfRemovedEqualityClass() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p1));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));

        Equality eq = (Equality) Equality.builder(p2, p3, true, domain);

        WNtuple right = new WNtuple(eq, rightMap, True.getInstance(domain));

        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property15", ArcFunComp.class, int.class);
        method.setAccessible(true);

        int card = 5;

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, comp, card);

        assertNotNull(result);
        assertEquals(9, All.getInstance(cc).cardLb());
        assertEquals(45, result.k());
    }

    @Test
    void property15_projectsUnusedEqualityClassAndScalesIndices() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p2));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));

        Equality eq = (Equality) Equality.builder(p1, p3, true, domain);

        WNtuple right = new WNtuple(eq, rightMap, True.getInstance(domain));

        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property15", ArcFunComp.class, int.class);
        method.setAccessible(true);

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, comp, 5);

        assertNotNull(result);
        assertEquals(45, result.k());
    }

    @Test
    void property15_multipliesCardinalityOfMultipleRemovedEqualityClasses() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);
        Projection p5 = Projection.builder(5, cc);

        SortedMap<ColorClass, List<? extends ColorFunction>> leftMap = new TreeMap<>();
        leftMap.put(cc, List.of(p1));

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)));

        WNtuple left = new WNtuple(leftMap, True.getInstance(domain));

        Equality eq1 = (Equality) Equality.builder(p2, p3, true, domain);
        Equality eq2 = (Equality) Equality.builder(p4, p5, true, domain);

        Guard filter = And.factory(List.of(eq1, eq2), true);

        WNtuple right = new WNtuple(filter, rightMap, True.getInstance(domain));

        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property15", ArcFunComp.class, int.class);
        method.setAccessible(true);

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, comp, 5);

        assertNotNull(result);
        assertEquals(405, result.k());
    }

    @Test
    void property2_multipliesCardinalityOfProjectedOutComponents() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);

        List<ColorFunction> leftComps = List.of(p1);

        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);

        ArcFunComp comp = new ArcFunComp(right, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property2", List.class, Set.class, WNtuple.class, Map.class, ColorClass.class, int.class);
        method.setAccessible(true);

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, leftComps, Set.of(), right, Map.of(), cc, 5);

        assertNotNull(result);
        assertEquals(45, result.k());
    }

    @Test
    void property2_keepsComponentsUsedByRightGuard() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        List<ColorFunction> leftComps = List.of(p1);

        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);

        Equality ineq = (Equality) Equality.builder(p1, p2, false, domain);

        ArcFunComp comp = new ArcFunComp(right, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property2", List.class, Set.class, WNtuple.class, Map.class, ColorClass.class, int.class);
        method.setAccessible(true);

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, leftComps, Set.of(ineq), right, Map.of(), cc, 5);

        assertNotNull(result);
        assertEquals(5, result.k());
    }

    @Test
    void property2_returnsNullWhenProjectedOutComponentHasUnknownCardinality() throws Exception {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        LinearComb extraComponent = new LinearComb(Map.of(p2, 1, p3, -1));

        assertNull(extraComponent.cardLb());

        List<ColorFunction> leftComps = List.of(p1);

        SortedMap<ColorClass, List<? extends ColorFunction>> rightMap = new TreeMap<>();
        rightMap.put(cc, List.of(p1, extraComponent, p3));

        WNtuple right = new WNtuple(rightMap, True.getInstance(domain));

        ArcFunComp comp = new ArcFunComp(right, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property2", List.class, Set.class, WNtuple.class, Map.class, ColorClass.class, int.class);
        method.setAccessible(true);

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, leftComps, Set.of(), right, Map.of(), cc, 5);

        assertNull(result);
    }

    @Test
    void property2_returnsConstantTupleWhenAllLeftComponentsAreConstant() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        ColorFunction constant = All.getInstance(cc);

        assertTrue(constant.isConstant());

        List<ColorFunction> leftComps = List.of(constant);

        Map<Integer, ColorFunction> constants = Util.select(leftComps, ColorFunction::isConstant);

        assertEquals(Set.of(1), constants.keySet());

        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);

        Equality ineq = (Equality) Equality.builder(p1, p2, false, domain);

        ArcFunComp comp = new ArcFunComp(right, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property2", List.class, Set.class, WNtuple.class, Map.class, ColorClass.class, int.class);
        method.setAccessible(true);

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, leftComps, Set.of(ineq), right, constants, cc, 5);

        assertNotNull(result);
        assertEquals(5, result.k());
        assertFalse(result.getArg() instanceof ArcFunComp);
    }

    @Test
    void property2_scalesLeftTupleAndRemainingGuards() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc, cc);

        Projection p2 = Projection.builder(2, cc);
        Projection p4 = Projection.builder(4, cc);

        List<ColorFunction> leftComps = List.of(p2, p4);

        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)), domain);

        Equality ineq = (Equality) Equality.builder(p2, p4, false, domain);

        ArcFunComp comp = new ArcFunComp(right, right);

        Method method = ArcFunComp.class.getDeclaredMethod("property2", List.class, Set.class, WNtuple.class, Map.class, ColorClass.class, int.class);
        method.setAccessible(true);

        ArcFunScalar result = (ArcFunScalar) method.invoke(comp, leftComps, Set.of(ineq), right, Map.of(), cc, 5);

        assertNotNull(result);
        assertEquals(405, result.k());
    }

    @Test
    void splitHomogeneousTuples_splitsLinearCombIntoAlternatives() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(Map.of(p1, 1, p2, -1));

        WNtuple left = new WNtuple(List.of(lc), True.getInstance(domain), false);

        Domain receiverDomain = new Domain(cc);
        Projection receiverProjection = Projection.builder(1, cc);

        WNtuple receiver = new WNtuple(List.of(receiverProjection), receiverDomain);

        ArcFunComp comp = new ArcFunComp(receiver, receiver);

        Method method = ArcFunComp.class.getDeclaredMethod("splitHomogeneousTuples", WNtuple.class);
        method.setAccessible(true);

        List<WNtuple> result = (List<WNtuple>) method.invoke(comp, left);

        List<ColorFunction> expected = new ArrayList<>();
        for (Bag<?> bag : lc.separateConst()) {
            expected.add((ColorFunction) bag);
        }

        assertEquals(expected.size(), result.size());

        for (WNtuple tuple : result) {
            assertEquals(1, tuple.getComponents().size());
            assertTrue(expected.contains(tuple.getComponents().getFirst()));
            assertEquals(left.guard(), tuple.guard());
        }
    }

    @Test
    void splitHomogeneousTuples_keepsProjectionAndConstantUnchanged() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        All all = All.getInstance(cc);

        WNtuple left = new WNtuple(List.of(p1, all), True.getInstance(domain), false);

        Domain receiverDomain = new Domain(cc);
        Projection receiverProjection = Projection.builder(1, cc);

        WNtuple receiver = new WNtuple(List.of(receiverProjection), receiverDomain);

        ArcFunComp comp = new ArcFunComp(receiver, receiver);

        Method method = ArcFunComp.class.getDeclaredMethod("splitHomogeneousTuples", WNtuple.class);
        method.setAccessible(true);

        List<WNtuple> result = (List<WNtuple>) method.invoke(comp, left);

        assertEquals(1, result.size());

        WNtuple tuple = result.getFirst();

        assertEquals(2, tuple.getComponents().size());
        assertEquals(p1, tuple.getComponents().get(0));
        assertEquals(all, tuple.getComponents().get(1));
        assertEquals(left.guard(), tuple.guard());
    }

    @Test
    void splitSum_splitsLinearCombAtSpecifiedPosition() throws Exception {
        ColorClass cc = new ColorClass(2, 9);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        LinearComb lc1 = new LinearComb(Map.of(p1, 1, p2, -1));
        LinearComb lc2 = new LinearComb(Map.of(p2, 2, p3, 1));

        List<LinearComb> original = List.of(lc1, lc2);

        Domain receiverDomain = new Domain(cc);
        WNtuple receiver = new WNtuple(List.of(Projection.builder(1, cc)), receiverDomain);
        ArcFunComp comp = new ArcFunComp(receiver, receiver);

        Method method = ArcFunComp.class.getDeclaredMethod("splitSum", List.class, int.class);
        method.setAccessible(true);

        Set<List<LinearComb>> result = (Set<List<LinearComb>>) method.invoke(comp, original, 1);

        assertEquals(2, result.size());

        for (List<LinearComb> split : result) {
            assertEquals(2, split.size());
            assertEquals(lc2, split.get(1));
            assertEquals(1, split.get(0).asMap().size());
            assertTrue(split.get(0).asMap().containsKey(p1) || split.get(0).asMap().containsKey(p2));
        }
    }

    @Test
    void splitAndCompose_composesEachAlternativeAndSumsResults() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(Map.of(p1, 1, p2, -1));

        WNtuple left = new WNtuple(List.of(lc), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);

        Domain receiverDomain = new Domain(cc);
        WNtuple receiver = new WNtuple(List.of(Projection.builder(1, cc)), receiverDomain);
        ArcFunComp comp = new ArcFunComp(receiver, receiver);

        Method method = ArcFunComp.class.getDeclaredMethod("splitAndCompose", WNtuple.class, WNtuple.class, int.class);
        method.setAccessible(true);

        ArcFunction result = (ArcFunction) method.invoke(comp, left, right, 1);

        assertNotNull(result);
        assertInstanceOf(ArcFunSum.class, result);
    }

    @Test
    void composeColorTuples_usesBaseComposeWhenThereAreNoRepeatedIndices() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);

        WNtuple left = new WNtuple(List.of(p1), domain);
        WNtuple right = new WNtuple(List.of(All.getInstance(cc)), domain);
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("composeColorTuples", ArcFunComp.class);
        method.setAccessible(true);

        ArcFunction expected = left.baseCompose(right);

        ArcFunction result = (ArcFunction) method.invoke(comp, comp);

        assertEquals(expected, result);
    }

    @Test
    void composeColorTuples_splitsLinearCombWhenRepeatedIndicesExist() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(Map.of(p1, 1, p2, -1));

        WNtuple left = new WNtuple(List.of(lc, p1), domain);
        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("composeColorTuples", ArcFunComp.class);
        method.setAccessible(true);

        ArcFunction result = (ArcFunction) method.invoke(comp, comp);

        assertNotNull(result);
        assertInstanceOf(ArcFunSum.class, result);
    }

    @Test
    void composeColorTuples_usesRepeatedIndexComposeWhenNoLinearCombNeedsSplitting() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);

        WNtuple left = new WNtuple(List.of(p1, p1), domain);
        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);
        ArcFunComp comp = new ArcFunComp(left, right);

        Method method = ArcFunComp.class.getDeclaredMethod("composeColorTuples", ArcFunComp.class);
        method.setAccessible(true);

        ArcFunction expected = left.repetedIndexCompose(right);

        ArcFunction result = (ArcFunction) method.invoke(comp, comp);

        assertEquals(expected, result);
    }

    @Test
    void prerequisite_returnsTrueWhenEqualityComponentsAreEquivalentAndCardinalityIsGreaterThanOne()
            throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);

        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);

        Domain receiverDomain = new Domain(cc);
        WNtuple receiver = new WNtuple(List.of(Projection.builder(1, cc)), receiverDomain);
        ArcFunComp comp = new ArcFunComp(receiver, receiver);

        Method method = ArcFunComp.class.getDeclaredMethod("prerequisite", Set.class, WNtuple.class, ColorClass.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(comp, Set.of(eq), right, cc);

        assertTrue(result);
    }

    @Test
    void prerequisite_returnsFalseWhenEqualityComponentsAreDifferent() throws Exception {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Equality eq = (Equality) Equality.builder(p1, p2, true, domain);

        WNtuple right = new WNtuple(List.of(All.getInstance(cc), p2), domain);

        Domain receiverDomain = new Domain(cc);
        WNtuple receiver = new WNtuple(List.of(Projection.builder(1, cc)), receiverDomain);
        ArcFunComp comp = new ArcFunComp(receiver, receiver);

        Method method = ArcFunComp.class.getDeclaredMethod("prerequisite", Set.class, WNtuple.class, ColorClass.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(comp, Set.of(eq), right, cc);

        assertFalse(result);
    }

    @Test
    void testSpecSimplify_InitialSimplificationNotArcFunComp() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3)});
        Domain domain = new Domain(cc);

        Subcl subcl = Subcl.factory(1, cc);

        Projection p1 = Projection.builder(1, cc);

        WNtuple left = new WNtuple(List.of(subcl), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(p1), True.getInstance(domain), false);
        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunScalar.class, result);
    }

    @Test
    void testSpecSimplify_LeftIsNotWNtuple() {
        ColorClass cc = new ColorClass(2, 3);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);

        WNtuple t1 = new WNtuple(List.of(p1), True.getInstance(domain), false);
        WNtuple t2 = new WNtuple(List.of(p1), True.getInstance(domain), false);

        ArcFunComp innerComp = new ArcFunComp(t1, t2);
        ArcFunComp comp = new ArcFunComp(innerComp, t1);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunComp.class, result);
        assertSame(comp, result);
    }

    @Test
    void testSpecSimplify_SplitsHomogeneousTuples() {
        ColorClass cc = new ColorClass(2, new Interval[]{new Interval(2, 2), new Interval(3, 3)});
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Subcl subcl1 = Subcl.factory(1, cc);

        LinearComb lc = new LinearComb(Map.of(p1, 1, subcl1, 1));

        assertEquals(2, lc.separateConst().size());

        WNtuple left = new WNtuple(List.of(lc), True.getInstance(domain), false);
        WNtuple right = new WNtuple(List.of(All.getInstance(cc), All.getInstance(cc)), domain);

        assertTrue(left.simple());
        assertFalse(left.isConstant());

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunComp.class, result);
        ArcFunComp simplified = (ArcFunComp) result;
        assertInstanceOf(ArcFunSum.class, simplified.left());
    }

    @Test
    void testSpecSimplify_LeftFilterNotTrivial() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        LinearComb lc = new LinearComb(Map.of(p1, 1, p2, -1));
        Guard filter = Equality.builder(p1, p2, true, domain);

        WNtuple left = new WNtuple(filter, List.of(lc, p1), True.getInstance(domain), false);
        WNtuple right = new WNtuple(
                List.of(All.getInstance(cc), All.getInstance(cc)), domain
        );

        assertTrue(left.simple());
        assertFalse(left.filter().isTrivial());

        ArcFunComp comp = new ArcFunComp(left, right);


        ArcFunction result = comp.normalize();

        assertEquals("8[c_2_1 = c_2_2]<S_C_2,S_C_2>", result.toString());
    }

    @Test
    void testSpecSimplify_RightIsNotWNtuple() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        Projection p1 = Projection.builder(1, cc);

        WNtuple left = new WNtuple(List.of(p1), domain);
        WNtuple right1 = new WNtuple(List.of(All.getInstance(cc)), domain);
        WNtuple right2 = new WNtuple(List.of(p1), domain);
        ArcFunSum right = (ArcFunSum) ArcFunSum.factory(List.of(right1, right2));

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertInstanceOf(ArcFunSum.class, right);

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertSame(comp, result);
    }

    @Test
    void testSpecSimplify_MovesLeftGuardToRightFilter() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Guard guard = Equality.builder(p1, p2, true, domain);

        WNtuple left = new WNtuple(List.of(p1, p2), guard, false);
        WNtuple right = new WNtuple(List.of(p1, p2), True.getInstance(domain), false);

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertFalse(left.guard().isTrivial());

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunComp.class, result);
        ArcFunComp simplified = (ArcFunComp) result;
        WNtuple simplifiedLeft = (WNtuple) simplified.left();
        WNtuple simplifiedRight = (WNtuple) simplified.right();
        assertTrue(simplifiedLeft.guard().isTrivial());
        assertEquals(guard, simplifiedRight.filter());
    }

    @Test
    void testSpecSimplify_RightFilterNotTrivial() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Guard filter = And.factory(Equality.builder(p1, p2, true, domain), Equality.builder(p1, p2, false, domain));

        WNtuple left = new WNtuple(List.of(p1, p2), domain);
        WNtuple right = new WNtuple(filter, List.of(p1, p2), True.getInstance(domain), false);

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertFalse(right.filter().isTrivial());

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertSame(comp, result);
    }

    @Test
    void testSpecSimplify_PrerequisiteFails() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);

        Guard filter = Equality.builder(p1, p2, true, domain);

        WNtuple left = new WNtuple(List.of(p1, p2), domain);
        WNtuple right = new WNtuple(filter, List.of(p1, p1) , True.getInstance(domain), false);

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertFalse(right.filter().isTrivial());

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertSame(comp, result);
    }

    @Test
    void testSpecSimplify_Property2ReturnsScalarWithoutComposition() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc);

        WNtuple left = new WNtuple(List.of(All.getInstance(cc)), domain);
        WNtuple right = new WNtuple(List.of(All.getInstance(cc)), domain);

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertTrue(left.getComponents().getFirst().isConstant());

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunScalar.class, result);
    }

    @Test
    void testSpecSimplify_Property15ProjectsEqualityClass() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        Guard filter = Equality.builder(p2, p3, true, domain);

        WNtuple left = new WNtuple(List.of(p1, p1, p1), domain);
        WNtuple right = new WNtuple(filter, List.of(p1, All.getInstance(cc), All.getInstance(cc)), True.getInstance(domain), false);

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertFalse(right.filter().isTrivial());

        assertEquals(All.getInstance(cc).cardLb(), right.getComponent(2, cc).cardLb());
        assertEquals(right.getComponent(2, cc), right.getComponent(3, cc));

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunScalar.class, result);
    }

    @Test
    void testSpecSimplify_Property15ScalesIndices() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        Guard filter = Equality.builder(p2, p4, true, domain);

        WNtuple left = new WNtuple(List.of(p1, p3), domain);
        WNtuple right = new WNtuple(filter, List.of(p1, All.getInstance(cc), p3, All.getInstance(cc)), True.getInstance(domain), false);

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertFalse(right.filter().isTrivial());

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunScalar.class, result);
    }

    @Test
    void testSpecSimplify_Lemma4WithoutExtraVariables() {
        ColorClass cc = new ColorClass(2, 9);
        Domain domain = new Domain(cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);

        Guard filter = Equality.builder(p2, p3, false, domain);

        WNtuple left = new WNtuple(List.of(p2, p3), domain);
        WNtuple right = new WNtuple(filter, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)), True.getInstance(domain), false);

        assertTrue(left.simple());
        assertTrue(left.filter().isTrivial());
        assertFalse(right.filter().isTrivial());

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        assertInstanceOf(ArcFunScalar.class, result);
    }

    @Test
    void testSpecSimplify_Lemma4LambdaSmallerThanS() {
        ColorClass cc = new ColorClass(2, 2);
        Domain domain = new Domain(cc, cc, cc, cc);

        Projection p1 = Projection.builder(1, cc);
        Projection p2 = Projection.builder(2, cc);
        Projection p3 = Projection.builder(3, cc);
        Projection p4 = Projection.builder(4, cc);

        Guard e12 = Equality.builder(p1, p2, false, domain);
        Guard e23 = Equality.builder(p2, p3, false, domain);
        Guard e34 = Equality.builder(p3, p4, false, domain);

        Guard filter = And.factory(e12, e23, e34);

        WNtuple left = new WNtuple(List.of(p1, p2, p3), domain);
        WNtuple right = new WNtuple(filter, List.of(All.getInstance(cc), All.getInstance(cc), All.getInstance(cc), All.getInstance(cc)), True.getInstance(domain), false);

        ArcFunComp comp = new ArcFunComp(left, right);

        ArcFunction result = comp.specSimplify();
        ArcFunScalar scalar = assertInstanceOf(ArcFunScalar.class, result);
        assertEquals(0, scalar.k());
    }
}