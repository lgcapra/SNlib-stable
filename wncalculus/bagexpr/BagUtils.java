package bagexpr;

import java.util.*;
import java.util.function.BiFunction;

import expr.Domain;
import expr.IllegalDomain;
import guard.*;
import logexpr.GuardedExpr;
import tuple.FunctionTuple;
import tuple.Tuple;
import tuple.AbstractTuple;
import tuple.AllTuple;
import util.Pair;
import util.Util;
import wnbag.WNtuple;
import logexpr.LogicalExprs;

/**
 * Utility statiche per operazioni su bag e mappe guardie->coeff
 */
public final class BagUtils {

    private BagUtils() {}

    // product: versione varargs / list (copia di LogicalBag.product)
    public static Map<Guard, List<Integer>> product(List<Map<Guard, Integer>> lm) {
        // replace empty maps with {null -> 0}
        for (ListIterator<Map<Guard, Integer>> ite = lm.listIterator(); ite.hasNext(); )
            if ( ite.next().isEmpty() )
                ite.set(Collections.singletonMap(null, 0)); // null stands for "true"

        List<Set<Map.Entry<Guard, Integer>>> c = new ArrayList<>();
        lm.forEach( m -> { c.add( m.entrySet() ); }); // build the list of sets
        HashMap<Guard, List<Integer>> res = new HashMap<>();
        Util.cartesianProd(c).stream()
                .map( l -> combineGuardCoeff((List<? extends Map.Entry<Guard, Integer>>) l))
                .forEachOrdered(entry -> {
                    Guard g = entry.getKey();
                    if ( g != null && ! g.isFalse() && res.putIfAbsent(g, entry.getValue()) != null)
                        throw new IllegalArgumentException("identical combinations of guards found! ->\n"+g+ "\nhere is the initial list of maps:\n"+lm);
                });

        return mergeGuards(res);
    }

    @SafeVarargs
    public static Map<Guard, List<Integer>> product(Map<Guard, Integer> ... list_of_entries) {
        return product (Arrays.asList(list_of_entries));
    }

    private static Pair<Guard, List<Integer>> combineGuardCoeff (List<? extends Map.Entry<Guard, Integer>> entry_list) {
        final List<Guard> lg = new ArrayList<>();
        final List<Integer> li = new ArrayList<>();
        final Domain[] commonDomainHolder = new Domain[1];  // holder per evitare riassegnazione

        entry_list.forEach( e -> {
            Guard g = e.getKey();
            if (g != null) { // a null value corresponds to "true"
                if (commonDomainHolder[0] == null) {
                    commonDomainHolder[0] = g.getDomain();
                }
                lg.add(g);
            }
            li.add(e.getValue()); // must be added also for null guards!
        });

        if (lg.isEmpty())
            throw new IllegalArgumentException("the list just contains null guards (representing empty arc functions!\n");

        if (Collections.frequency(li, 0) == li.size())
            return new Pair<>(null, null);

        // Clone all guards to the common domain if domains differ
        Domain commonDomain = commonDomainHolder[0];
        if (lg.size() > 1 && commonDomain != null) {
            Domain finalDomain = commonDomain;
            List<Guard> clonedLg = new ArrayList<>();
            for (Guard g : lg) {
                if (!g.getDomain().equals(finalDomain)) {
                    // Clone the guard to the common domain
                    clonedLg.add(g.clone(finalDomain));
                } else {
                    clonedLg.add(g);
                }
            }
            lg.clear();
            lg.addAll(clonedLg);
        }

        try {
            return new Pair<>((Guard) (And.factory(lg).normalize(true)), li);
        } catch (IllegalDomain | IllegalArgumentException e) {
            System.err.println("WARNING: cannot combine guards: " + e.getMessage());
            throw new IllegalArgumentException("Guard combination failed: " + e.getMessage(), e);
        }
    }


    // mergeGuards: invert map and OR keys with same values (copiato da LogicalBag)
    public static <E> Map<Guard, E> mergeGuards (Map<Guard, E> m) {
        if ( Util.injective(m) ) // m is injective
            return m;

        Map<E, Set<Guard>> im = Util.invert(m); // the inverted map
        HashMap<Guard, E> mm = new HashMap<>();
        im.entrySet().forEach( e -> { mm.put((Guard) Or.factory(e.getValue(), true).normalize(), e.getKey()); });

        return mm;
    }

    // mapGuardsToCoefficients: given a Bag of Tuple-like elements, extract map guard->coefficient using f
    // SERVE?
    public static Map<Guard, Integer> mapGuardsToCoefficients(Bag<WNtuple> bag, BiFunction<Integer,Integer,Integer> f) {
        Map<Guard, Integer> m = new HashMap<>();
        if (bag == null) return m;

        Bag<WNtuple> b =(Bag<WNtuple>) bag.normalize(); // normalized
        if (b.isEmpty()) {
            m.put(True.getInstance(b.getDomain()), 0);
            return m;
        }

        //Set<? extends WNtuple> keys = b.support();
        //if (keys.size() == 1) { // elementary bag
        //    WNtuple k = keys.iterator().next();
        //    Guard g;
        //    if (k instanceof AllTuple) {
        //        g = True.getInstance(b.getDomain());
        //    } else 
        //    if (k instanceof GuardedExpr) {
            //    g = (Guard) ((GuardedExpr) k).guard();
            //} else if (k instanceof Tuple) {
            //    g = ((Tuple) k).guard();
            //} else {
            //    throw new ClassCastException("Unsupported FunctionTuple type in BagUtils.mapGuardsToCoefficients: " + k.getClass());
            //}
            //m.put(g, b.mult(k));
        //}
        //else {
        //    keys.forEach(x -> {
        //        Guard g;
        //        //if (x instanceof AllTuple) {
        //        //    g = True.getInstance(b.getDomain());
        //        //} else 
        //        if (x instanceof GuardedExpr) {
        //            g = (Guard) ((GuardedExpr) x).guard();
        //        } else if (x instanceof Tuple) {
        //            g = ((Tuple) x).guard();
        //        } else {
        //            throw new ClassCastException("Unsupported FunctionTuple type in BagUtils.mapGuardsToCoefficients: " + x.getClass());
        //        }
        //        setVal(m, g, b.mult(x), f);
      //      });

      //      if (m.size() > 1) // optimization
      //          disjoinMapOfGuards(m, f);
      //  }

      //  if (!LogicalExprs.disjoined(m.keySet())) {
      //      System.out.println("le guardie dovrebbero essere disgiunte: " + m.keySet());
      //      throw new Error();
      //  }

      //  Integer truecoeff = m.get(null);
      //  if (truecoeff != null) {
      //        m.remove(null);
      //        m.put(True.getInstance(b.getDomain()), truecoeff);
      //    }

      //  Guard implicit = Neg.factory(Or.factory(m.keySet(), true));
      //  implicit = (Guard) implicit.normalize(true);

      //  if (!implicit.isFalse()) {
      //      m.put(implicit, 0);
      //  }

      return m;
    }

    public static Map<Guard, Integer> mapGuardsToMaxCoefficients(Bag<WNtuple> bag) {
        return mapGuardsToCoefficients(bag, (Integer x, Integer y) -> Math.max(x, y));
    }

    public static Map<Guard, Integer> mapGuardsToSumCoefficients(Bag<WNtuple> bag) {
        return mapGuardsToCoefficients(bag, (Integer x, Integer y) -> x + y);
    }


    // helper methods used during disjoin of guard maps
    private static void disjoinMapOfGuards(Map<Guard, Integer> m, BiFunction<Integer,Integer,Integer> f) {
        Set<Guard> guards = new HashSet<>(m.keySet());
        while (!guards.isEmpty()) {
            Iterator<Guard> ite = guards.iterator();
            Guard g1 = ite.next();
            ite.remove();
            int k1 = m.get(g1);
            for (; ite.hasNext() ; ) {
                Guard g2 = ite.next();
                Guard[] parts = disjoinParts(g1, g2);
                if (parts != null) {
                    Integer k2 = m.get(g2);
                    m.remove(g1);
                    m.remove(g2);
                    ite.remove();
                    setVal(m, parts[0], f.apply(k1, k2), f);
                    guards.add(parts[0]);
                    if (!parts[1].isFalse()) {
                        setVal(m, parts[1], k1, f);
                        guards.add(parts[1]);
                    }
                    if (!parts[2].isFalse()) {
                        setVal(m, parts[2], k2, f);
                        guards.add(parts[2]);
                    }
                    break;
                }
            }
        }
    }

    private static void setVal(Map<Guard, Integer> m, Guard g, Integer v, BiFunction<Integer,Integer,Integer> f) {
        Integer k = m.get(g);
        m.put(g, k == null ? v : f.apply(k, v));
    }

    private static Guard[] disjoinParts(Guard g1, Guard g2) {
        Guard g;
        Guard[] res = new Guard[3];
        if (g1 == null) {
            res[0] = g2;
            res[1] = (Guard) (Neg.factory(g2).normalize(true));
            res[2] = False.getInstance(g2.getDomain());
        } else if (g2 == null) {
            res[0] = g1;
            res[1] = False.getInstance(g1.getDomain());
            res[2] = (Guard) (Neg.factory(g1).normalize(true));
        } else if (!(g = (Guard) (And.factory(g1, g2).normalize(true))).isFalse()) {
            res[0] = g;
            res[1] = (Guard) (g1.diff(g2).normalize(true));
            res[2] = (Guard) (g2.diff(g1).normalize(true));
        } else
            res = null;

        return res;
    }

    // Minimal no-op disjoin: mantiene compatibilità col codice esistente.
    @SuppressWarnings("unchecked")
    public static <E extends expr.ParametricExpr> bagexpr.BagExpr<E> disjoin(bagexpr.BagExpr<E> b) {
        return b;
    }

}