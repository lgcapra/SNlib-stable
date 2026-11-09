package wnbag;

import classfunction.All;
import classfunction.ClassFunction;
import classfunction.Projection;
import classfunction.Subcl;
import color.ColorClass;
import expr.Interval;
import expr.Domain;
import guard.*;
import org.junit.jupiter.api.Test;
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
    void lemma4_returnsNullWhenThereAreNoExtraVariables() throws Exception {
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

        ArcFunction result = (ArcFunction) method.invoke(comp, comp, 5);
        assertNull(result);
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
}