import java.util.HashMap;
import java.util.Map;

import classfunction.All;
import classfunction.Projection;
import color.ColorClass;
import expr.Domain;
import expr.Interval;
import wnbag.ArcFunction;
import wnbag.TupleBag;
import wnbag.WNtuple;
import wncolorfunction.LinearComb;

public class ExpandTests {

    private static ColorClass defaultColorClass() {
        return new ColorClass("X", new Interval(3, 3));
    }

    private static Projection p(ColorClass cc, int idx) {
        return Projection.builder(idx, cc);
    }

    private static void assertExpansion(String name, WNtuple input, TupleBag expected) {
        ArcFunction expanded = input.expand();

        if (!(expanded instanceof TupleBag actual)) {
            throw new AssertionError(name + ": expand() deve restituire TupleBag, ma ha restituito "
                    + expanded.getClass().getSimpleName());
        }

        if (!expected.equals(actual)) {
            throw new AssertionError(
                    name + ": risultato non atteso\n"
                            + "atteso:   " + expected.asMap() + "\n"
                            + "ottenuto: " + actual.asMap()
            );
        }

        System.out.println(name + " PASSED");
        System.out.println("Originale: " + input);
        System.out.println("Espanso:   " + actual);
    }

    public static void testNoAll() {
        ColorClass cc = defaultColorClass();
        Projection x1 = p(cc, 1);
        Projection x2 = p(cc, 2);
        Domain dom = new Domain(cc, cc);

        // <1x1 + 3x2, x1>
        WNtuple input = new WNtuple(
                dom,
                new LinearComb(x1, x2, x2, x2),
                new LinearComb(x1)
        );

        WNtuple exp1 = new WNtuple(
                dom,
                new LinearComb(x1),
                new LinearComb(x1)
        );

        WNtuple exp2 = new WNtuple(
                dom,
                new LinearComb(x2),
                new LinearComb(x1)
        );

        Map<WNtuple, Integer> expectedMap = new HashMap<>();
        expectedMap.put(exp1, 1);
        expectedMap.put(exp2, 3);

        assertExpansion("testNoAll", input, new TupleBag(expectedMap));
    }

    public static void testProduct() {
        ColorClass cc = defaultColorClass();
        Projection x1 = p(cc, 1);
        Projection x2 = p(cc, 2);
        Domain dom = new Domain(cc, cc);

        // <1x1 + 2x2, 3x1 + 4x2>
        WNtuple input = new WNtuple(
                dom,
                new LinearComb(x1, x2, x2),
                new LinearComb(x1, x1, x1, x2, x2, x2, x2)
        );

        Map<WNtuple, Integer> expectedMap = getwNtupleIntegerMap(dom, x1, x2);

        assertExpansion("testProduct", input, new TupleBag(expectedMap));
    }

    private static Map<WNtuple, Integer> getwNtupleIntegerMap(Domain dom, Projection x1, Projection x2) {
        WNtuple p11 = new WNtuple(dom, new LinearComb(x1), new LinearComb(x1));
        WNtuple p12 = new WNtuple(dom, new LinearComb(x1), new LinearComb(x2));
        WNtuple p21 = new WNtuple(dom, new LinearComb(x2), new LinearComb(x1));
        WNtuple p22 = new WNtuple(dom, new LinearComb(x2), new LinearComb(x2));

        Map<WNtuple, Integer> expectedMap = new HashMap<>();
        expectedMap.put(p11, 3); // 1 * 3
        expectedMap.put(p12, 4); // 1 * 4
        expectedMap.put(p21, 6); // 2 * 3
        expectedMap.put(p22, 8); // 2 * 4
        return expectedMap;
    }

    public static void testAllExample() {
        ColorClass cc = defaultColorClass();
        Projection x1 = p(cc, 1);
        Projection x2 = p(cc, 2);
        Domain dom = new Domain(cc, cc);

        // <1x1 + 3x2, All>
        WNtuple input = new WNtuple(
                dom,
                new LinearComb(x1, x2, x2, x2),
                new LinearComb(All.getInstance(cc))
        );

        WNtuple exp1 = new WNtuple(
                dom,
                new LinearComb(x1),
                new LinearComb(All.getInstance(cc))
        );

        WNtuple exp2 = new WNtuple(
                dom,
                new LinearComb(x2),
                new LinearComb(All.getInstance(cc))
        );

        Map<WNtuple, Integer> expectedMap = new HashMap<>();
        expectedMap.put(exp1, 1);
        expectedMap.put(exp2, 3);

        assertExpansion("testAllExample", input, new TupleBag(expectedMap));
    }

    public static void main(String[] args) {
        System.out.println("=== ExpandTests ===");

        testNoAll();
        testProduct();
        testAllExample();

        System.out.println("\nTUTTI I TEST ESEGUITI CON SUCCESSO");
    }
}
