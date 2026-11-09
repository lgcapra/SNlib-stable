package wnbag;

import bagexpr.Bag;
import classfunction.All;
import classfunction.ConstantFunction;
import classfunction.ElementaryFunction;
import classfunction.Projection;
import color.ColorClass;
import expr.Domain;
import guard.Equality;
import org.junit.jupiter.api.Test;
import wncolorfunction.LinearComb;
import wncolorfunction.LinearCombScalar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BagOperationsTest {

    private static final ColorClass cc = new ColorClass(2, 3);

    private static final Projection p1 =
            Projection.builder(1, cc);

    private static final Projection p2 =
            Projection.builder(2, cc);

    @Test
    void testSumSameElement() {
        LinearComb a = new LinearComb(p1, 2);
        LinearComb b = new LinearComb(p1, 3);

        Bag<ElementaryFunction> result = a.sum(b);

        assertEquals(5, result.mult(p1));
        assertEquals(1, result.asMap().size());
    }

    @Test
    void testSumDifferentElements() {
        LinearComb a = new LinearComb(p1, 2);
        LinearComb b = new LinearComb(p2, 3);

        Bag<ElementaryFunction> result = a.sum(b);

        assertEquals(2, result.mult(p1));
        assertEquals(3, result.mult(p2));
        assertEquals(2, result.asMap().size());
    }

    @Test
    void testSumCancellation() {
        LinearComb a = new LinearComb(p1, 1);
        LinearComb b = new LinearComb(p1, -1);

        Bag<ElementaryFunction> result = a.sum(b);

        assertTrue(result.asMap().isEmpty());
    }

    @Test
    void testSpecSimplifySingleElement() {
        LinearComb lc = new LinearComb(p1, 1);

        var result = lc.specSimplify();

        assertInstanceOf(Projection.class, result);
        assertEquals(p1, result);
    }

    @Test
    void testSpecSimplifySingleElementWithCoefficient() {
        LinearComb lc = new LinearComb(p1, 3);

        var result = lc.specSimplify();

        assertInstanceOf(LinearComb.class, result);

        LinearComb simplified = (LinearComb) result;
        assertEquals(3, simplified.mult(p1));
        assertEquals(1, simplified.asMap().size());
    }

    @Test
    void testSpecSimplifyExtractsGCD() {
        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, 6);
        terms.put(p2, 9);

        LinearComb lc = new LinearComb(terms);

        var result = lc.specSimplify();

        assertInstanceOf(LinearCombScalar.class, result);

        LinearCombScalar scalar = (LinearCombScalar) result;

        // Verifica il coefficiente estratto
        assertEquals(3, scalar.k());

        // Verifica la combinazione lineare interna
        assertInstanceOf(LinearComb.class, scalar.getArg());

        LinearComb inner = (LinearComb) scalar.getArg();

        assertEquals(2, inner.mult(p1));
        assertEquals(3, inner.mult(p2));
        assertEquals(2, inner.asMap().size());
    }

    @Test
    void testReplaceProjection() {
        Domain dom = new Domain(cc, cc);
        Equality eq = (Equality) Equality.builder(p1, p2, true, dom);

        LinearComb lc = new LinearComb(p2, 2);

        LinearComb result = lc.replace(eq);

        assertEquals(2, result.mult(p1));
        assertEquals(0, result.mult(p2));
        assertEquals(1, result.asMap().size());
    }

    @Test
    void testReplaceMergesCoefficients() {
        Equality eq = (Equality) Equality.builder(p1, p2, true, new Domain(cc, cc));

        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, 2);
        terms.put(p2, 3);

        LinearComb lc = new LinearComb(terms);

        LinearComb result = lc.replace(eq);

        assertEquals(5, result.mult(p1));
        assertEquals(1, result.asMap().size());
    }

    @Test
    void testReplaceCancelsCoefficients() {
        Equality eq = (Equality) Equality.builder(p1, p2, true, new Domain(cc, cc));

        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, 2);
        terms.put(p2, -2);

        LinearComb lc = new LinearComb(terms);

        LinearComb result =lc.replace(eq);

        assertTrue(result.asMap().isEmpty());
    }

    @Test
    void testReplaceProjectionWithSuccessor() {
        ColorClass orderedCc = new ColorClass(4, 3, true);

        Domain dom = new Domain(orderedCc, orderedCc);

        Projection q1 = Projection.builder(1, orderedCc);
        Projection q2 = Projection.builder(2, 1, orderedCc);

        Equality eq = (Equality) Equality.builder(q1, q2, true, dom);

        LinearComb lc = new LinearComb(
                Projection.builder(2, orderedCc)
        );

        LinearComb result = (LinearComb) lc.replace(eq);

        Projection expected = Projection.builder(1, 2, orderedCc);

        assertEquals(1, result.mult(expected));
        assertEquals(1, result.asMap().size());
    }

    @Test
    void testIndexSetSingleProjection() {
        LinearComb lc = new LinearComb(p1, 2);

        Set<Integer> indexes = lc.indexSet();

        assertEquals(Set.of(1), indexes);
    }

    @Test
    void testIndexSetMultipleProjections() {
        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, 2);
        terms.put(p2, 3);

        LinearComb lc = new LinearComb(terms);

        Set<Integer> indexes = lc.indexSet();

        assertEquals(Set.of(1, 2), indexes);
    }

    @Test
    void testIndexSetIgnoresMultiplicity() {
        LinearComb lc = new LinearComb(p1, 5);

        Set<Integer> indexes = lc.indexSet();

        assertEquals(1, indexes.size());
        assertTrue(indexes.contains(1));
    }

    @Test
    void testCardLbSingleProjection() {
        LinearComb lc = new LinearComb(p1, 3);

        assertEquals(3 * p1.cardLb(), lc.cardLb());
    }

    @Test
    void testCardLbMultiplePositiveCoefficients() {
        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, 2);
        terms.put(p2, 3);

        LinearComb lc = new LinearComb(terms);

        Integer expected = 2 * p1.cardLb() + 3 * p2.cardLb();

        assertEquals(expected, lc.cardLb());
    }

    @Test
    void testCardLbMixedSigns() {
        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, 2);
        terms.put(p2, -3);

        LinearComb lc = new LinearComb(terms);

        assertNull(lc.cardLb());
    }

    @Test
    void testCardLbMultipleNegativeCoefficients() {
        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, -2);
        terms.put(p2, -3);

        LinearComb lc = new LinearComb(terms);

        Integer expected = -2 * p1.cardLb() - 3 * p2.cardLb();

        assertEquals(expected, lc.cardLb());
    }

    @Test
    void testSeparateConstOnlyNonConstants() {
        LinearComb lc = new LinearComb(p1, 2);

        List<? extends Bag<ElementaryFunction>> result = lc.separateConst();

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).mult(p1));
    }

    @Test
    void testSeparateConstOnlyConstants() {
        ConstantFunction constant = All.getInstance(cc);
        LinearComb lc = new LinearComb(constant, 3);

        List<? extends Bag<ElementaryFunction>> result = lc.separateConst();

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).mult(constant));
    }

    @Test
    void testSeparateConstMixed() {
        ConstantFunction constant = All.getInstance(cc);

        Map<ElementaryFunction, Integer> terms = new HashMap<>();
        terms.put(p1, 2);
        terms.put(constant, 3);

        LinearComb lc = new LinearComb(terms);

        List<? extends Bag<ElementaryFunction>> result = lc.separateConst();

        assertEquals(2, result.size());

        // Prima parte: termini non costanti
        assertEquals(2, result.get(0).mult(p1));

        // Seconda parte: termini costanti
        assertEquals(3, result.get(1).mult(constant));
    }
}