package wnbag;

import bagexpr.Bag;
import bagexpr.BagComp;
import bagexpr.BagExpr;
import classfunction.All;
import classfunction.ClassFunction;
import classfunction.Projection;
import color.ColorClass;
import expr.Domain;
import graph.InequalityGraph;
import guard.*;
import util.Pair;
import util.Util;
import wncolorfunction.ColorFunction;
import wncolorfunction.LinearComb;

import java.util.*;
import java.util.Map.Entry;

import static guard.Guard.scaleGuardIndex;
import static wncolorfunction.LinearComb.splitSum;


public final class ArcFunComp extends BagComp<WNtuple> implements ArcFunction {
       
     public ArcFunComp(ArcFunction l, ArcFunction r) {
         super(l, r);
     }

     @Override
     public ArcFunComp buildOp(BagExpr<WNtuple> left, BagExpr<WNtuple> right) {
            return new ArcFunComp((ArcFunction) left, (ArcFunction) right);
     }

     @Override
    public ArcFunction specSimplify() {
        ArcFunction res = (ArcFunction) super.specSimplify();

        if (res instanceof ArcFunComp comp) {
            // caso base: composizione tra WNtuple (tuple di class-functions) dove sx contiene solo LinearCombs
            if (comp.left() instanceof WNtuple left && left.simple()){
                // Preliminary step: we make tuples' inner component exclusively contain constant or non-constant functions
                //we split the tuple so that components are homogeneous (only constants or non-constants)
                List<WNtuple> homogeneousTuples = splitHomogeneousTuples(left);

                if (homogeneousTuples.size() > 1) {
                    return new ArcFunComp(ArcFunSum.factory(homogeneousTuples), comp.right().cast());
                }

                if (!(left.filter().isTrivial())) {
                    //to do
                    return res;
                }

                if (!(comp.right() instanceof WNtuple right))
                    return res;

                if (!left.guard().isTrivial()) { // the left tuple's guard is moved on the right
                    return new ArcFunComp(left.withoutGuard().cast(), right.joinFilter(left.guard()).cast());
                }

                // left tuple, with no constant components
                // Lemma 1
                final Guard rf = right.filter();
                if (! (rf.isTrivial() || rf.isElemAndForm()) )
                    return res;

                int card = 1; // the cardinality of the right-tuple components non-projected
                final Map<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> splitColors
                      = left.splitColors(rf);
                //System.out.println("DEBUG (a):\n"+left+'\n'+splitColors+'\n'+right);
                final Map<ColorClass, ArcFunction> results = new HashMap<>(); // the divided-by color outcomes
                for (Entry<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> cleft : splitColors.entrySet()) {
                    final Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>> pair = cleft.getValue();
                    List<? extends ColorFunction> cleftComps = pair.getKey();
                    Set<? extends ElementaryGuard> crightF=  pair.getValue();
                    final ColorClass cc = cleft.getKey();

                    // Simple-form prerequisite
                    if (!prerequisite(crightF, right)) {
                        return res;
                    }

                    // new Property 2: we extract constant components from the left tuple, if any
                    final Map<Integer, ColorFunction> constants = Util.select(cleftComps, ColorFunction::isConstant);
                    ArcFunction cc_res = property2(cleftComps, crightF, right, constants, cc, card);
                    if (cc_res == null) {
                        return res;
                    }
                    //System.out.println(rtx);

                    // Property 15
                    ArcFunScalar ccr = (ArcFunScalar) cc_res;
                    card = ccr.k();
                    cc_res = property15((ArcFunComp)ccr.getArg(), card);
                    //System.out.println(cc_res);

                    // Lemma 4
                    ccr = (ArcFunScalar) cc_res;
                    card = ccr.k();
                    cc_res = lemma4((ArcFunComp)ccr.getArg(), card);
                    if (cc_res == null) {//The conditions are not met; I will attempt to simplify the left-hand tuple.
                        return new ArcFunComp(left.joinGuard(right.filter()).cast(), right.withoutFilter().cast());
                    }
                    ccr = (ArcFunScalar) cc_res;
                    card = ccr.k();
                    // If there is still something in the right filter, it is moved to the left guard position so that it can then be shifted into the left filter.
                    if(!(((WNtuple)(((ArcFunComp)ccr.getArg()).right())).filter().isTrivial())) {
                        cc_res = new ArcFunScalar(new ArcFunComp(((WNtuple)(((ArcFunComp)ccr.getArg()).left())).joinGuard(right.filter()).genSimplify().cast(), ((WNtuple)(((ArcFunComp)ccr.getArg()).right())).withoutFilter().cast()), card);
                    }
                    //System.out.println(cc_res);


                //Inutile perchè il filtro è sempre vuoto in questo punto del codice
//                   // analizzare i filtri della tupla destra (rightFilter)
//                    // verificare se per ognuna uno dei due membri
//                    // del filtro fa riferimento a una "somma" (linear comb di più elementi)
//                    // nella tupla destra, in questo caso si spezza la somma
//                    if(rightFilter.isElemAndForm()){
//                        for (ElementaryGuard elementaryGuard : crightF) {
//                            var i = elementaryGuard.getArg1().getIndex();
//                            if (redRightComps.get(i - 1) instanceof LinearComb rc) {
//                                if (rc.size() > 1) { //linear comb di più elementi (es somma)
//                                    cc_res = splitAndCompose(tleft, rtx, i, false);
//                                    System.out.println("DEBUG: " + cc_res);
//                                    return cc_res;
//                                }
//                            }
//                        }
//                    }

                    ccr = (ArcFunScalar) cc_res;
                    card = Objects.requireNonNull(ccr).k();
                    cc_res = composeColorTuples((ArcFunComp)ccr.getArg());

                    cc_res = ArcFunExpansion.factory(cc_res, constants);
                    //System.out.println("DEBUG (c):\n"+cc_res);
                    results.put(cc, cc_res);
                } // for each color class

                // we build the scalar product of juxtaposition of results
                // System.out.println("DEBUG (b):\n"+results);
                res = new ArcFunScalar(ArcFunJuxt.factory(results, right.guard()), card); // k⋅(Tr∘TX′);
            }
        }
        //System.out.println("(ArcFunComp) simplified to: " + res + ", class: " + res.getClass()); //debug
        return res;
    }

    private ArcFunction splitAndCompose(WNtuple left, WNtuple right, int i, boolean splitLeft) {
        WNtuple tupleToSplit = splitLeft ? left : right;

        List<LinearComb> tupleComps = (List<LinearComb>) Util.cast(tupleToSplit.getComponents(), LinearComb.class);
        ArcFunction result = null;

        for (List<LinearComb> splitComps : splitSum(tupleComps, i)) {
            WNtuple newTuple = new WNtuple(tupleToSplit.filter(), splitComps, tupleToSplit.guard(), false);
            ArcFunction newRes = splitLeft ? new ArcFunComp(newTuple, right) : new ArcFunComp(left, newTuple);
            result = result == null ? newRes : ArcFunSum.factory(result, newRes);
        }

        return result;
    }


    private boolean prerequisite(Set<? extends ElementaryGuard> guards, WNtuple right) {
        for (ElementaryGuard guard : guards) {
            if (guard.isMembership()) {
                return false;
            }
            Equality eq = (Equality) guard;
            var rc1 = right.getComponent(eq.getArg1().getIndex(), right.getSort());
            var rc2 = right.getComponent(eq.getArg2().getIndex(), right.getSort());

            // Continue only if the equality concerns equivalent components in the right tuple
            // and their cardinality is greater than one.
            if (!rc1.equals(rc2) || rc1.cardLb() <= 1) {
                return false;
            }
        }
        return true;
    }

    private ArcFunScalar property2(List<? extends ColorFunction> cleftComps, Set<? extends ElementaryGuard> crightF, WNtuple right, Map<Integer, ColorFunction> constants, ColorClass cc, int card){
        final Guard rf = right.filter();
        cleftComps = Util.remove(cleftComps, constants.keySet()); // we remove (in the event) constant components from the left tuple's components
        final SortedSet<Integer> leftindx = new TreeSet<>(ClassFunction.indexSet(cleftComps));
        leftindx.addAll(Guard.indexSet(crightF)); // includes the guard indices too
        final List<? extends ColorFunction> rightComps = right.getHomSubTuple(cc),
                redRightComps = Util.projection(rightComps, leftindx);
        //Guard rightFilter = newGuard(crightF, rtx.getCodomain());
        if (redRightComps != rightComps) { // some components of the right tuple are projected out
            for (int i = 1; i <= rightComps.size(); i++) { // we check the cardinality of the non-projected components of the right tuple
                if (! leftindx.contains(i) ) {
                    final Integer c = rightComps.get(i -1).cardLb();
                    if (c == null)
                        return null; // the cardinality of a non-projected component is not known, so we cannot apply Lemma 1
                    else
                        card *= rightComps.get(i -1).cardLb();
                }
            }
            // we rescale (in the event) rojection indices in the tuple sx
            Map<Integer, Integer> scaled = Util.scaledIndex(leftindx);
            if (leftindx.last() > leftindx.size()) {
                cleftComps = ClassFunction.scaleIndex(cleftComps, scaled);
                crightF = scaleGuardIndex(crightF, scaled, rf.getDomain());
            }
        }

        WNtuple rtx = new WNtuple(redRightComps, right.getDomain());
        Guard rightFilter = newGuard(crightF, rtx.getCodomain());
        return new ArcFunScalar(new ArcFunComp(new WNtuple(cleftComps, rightFilter.getDomain()), rtx.joinFilter(rightFilter).cast()), card);

    }

    private ArcFunScalar property15(ArcFunComp ccr, int card){
        WNtuple rtx = ccr.right().cast();
        Guard rightFilter = rtx.filter();
        var rightComps = rtx.getComponents();

        // Keep only the non-equality guards from the right filter.
        Set<Equality> crightF = new HashSet<>();
        collectIneq(rightFilter, crightF);

        WNtuple left = ccr.left().cast();
        var cleftComps = left.getComponents();

        // Collect the indices of the components occurring in the left tuple.
        final SortedSet<Integer> lidx = new TreeSet<>(ClassFunction.indexSet(cleftComps));

        // Each set represents the indices belonging to an equality class.
        Set<Set<Integer>> eqClass = extractEqualityClasses(rightFilter);
        //System.out.println(eqClass);

        // Include the indices required by the remaining guards.
        lidx.addAll(Guard.indexSet(crightF));
        // Project the right tuple onto the components whose indices are in lidx.
        var redRightComps = Util.projection(rightComps, lidx);

        if (redRightComps != rightComps) { // Some right-tuple components have been removed by the projection.
            for (Set<Integer> eqc : eqClass) {
                // If an equality class has no index among the retained components,
                // its contribution must be accounted for in the cardinality.
                if (Collections.disjoint(eqc, lidx)) {
                    // Use one representative index from the equality class.
                    int i = Collections.min(eqc);

                    // Multiply the cardinality by the lower-bound cardinality of the corresponding component in the original right tuple.
                    card *= rightComps.get(i - 1).cardLb();
                }
            }

            // Compute the new indices after projecting out unused components.
            Map<Integer, Integer> scaled = Util.scaledIndex(lidx);

            // If the retained indices are no longer consecutive starting from 1,
            // update the indices in the left tuple and in the remaining guards.
            if (lidx.last() > lidx.size()) {
                cleftComps = ClassFunction.scaleIndex(cleftComps, scaled);

                crightF = (Set<Equality>) scaleGuardIndex(crightF, scaled, rightFilter.getDomain());
            }
        }

        rtx = new WNtuple(redRightComps, rtx.getDomain());
        rightFilter = newGuard(crightF, rtx.getCodomain());
        var tleft = new WNtuple(cleftComps, rightFilter.getDomain());
        rtx = (WNtuple) rtx.joinFilter(rightFilter);
        return new ArcFunScalar(new ArcFunComp(tleft, rtx), card);
    }

    private ArcFunction lemma4(ArcFunComp comp, int card) {
        WNtuple left = comp.left().cast();
        WNtuple right = comp.right().cast();
        Guard rightFilter = right.filter();

        if (rightFilter == null || rightFilter.isTrivial()) {
            return new ArcFunScalar(comp, card);
        }

        var cleftComps = left.getComponents();
        final SortedSet<Integer> leftidx = new TreeSet<>(ClassFunction.indexSet(cleftComps));

        // I gather the members of the filter
        final Set<Equality> ineqs = new HashSet<>();
        collectIneq(rightFilter, ineqs);
        if (ineqs.isEmpty()) {
            return new ArcFunScalar(comp, card);
        }

        // Variables used in the filter
        final SortedSet<Integer> gIdx = new TreeSet<>();
        for (Equality e : ineqs) {
            gIdx.addAll(e.indexSet());
        }

        // Lemma 4 applies only if there are extra variables
        final SortedSet<Integer> extra = new TreeSet<>(gIdx);
        extra.removeAll(leftidx);
        if (extra.isEmpty()) {
            return null;
        }

        // Lambda: domain available; we use the minimum lb known for the components involved
        final List<? extends ColorFunction> rightComps = right.getComponents();
        int lambda = Integer.MAX_VALUE;
        for (int i : gIdx) {
            if (i < 1 || i > rightComps.size()) {
                return null;
            }
            Integer lb = rightComps.get(i - 1).cardLb();
            if (lb == null) {
                return null; // Cannot reliably estimate the coefficient.
            }
            if (lb < lambda) {
                lambda = lb;
            }
        }

        // Construction of the complete guard graph, and extraction of its connected components
        final InequalityGraph fullGraph = new InequalityGraph(ineqs);
        final Set<HashSet<classfunction.Projection>> ccSet = fullGraph.connectedComponents();

        long kTot = 1L;
        Set<Equality> cRightF = new HashSet<>(); // reduced guard g_X

        for (Set<classfunction.Projection> cc : ccSet) {
            // Inequalities for the current component
            final Set<Equality> compEqs = new HashSet<>();
            for (Equality e : ineqs) {
                if (cc.contains(e.getArg1()) && cc.contains(e.getArg2())) {
                    compEqs.add(e);
                }
            }
            if (compEqs.isEmpty()) {
                continue;
            }

            final InequalityGraph compGraph = new InequalityGraph(compEqs);
            final long pComp = compGraph.chromPolynomial(lambda);

            // Component indices
            final SortedSet<Integer> compIdx = new TreeSet<>();
            for (Equality e : compEqs) {
                compIdx.addAll(e.indexSet());
            }

            final SortedSet<Integer> xComp = new TreeSet<>(compIdx);
            xComp.retainAll(leftidx);

            if (xComp.isEmpty()) {
                // Completely extra block: I multiply directly by P(comp,lambda)
                kTot = Math.multiplyExact(kTot, pComp);
                continue;
            }

            // Mixed block: I apply the relationship with the clique to X_comp
            int s = xComp.size();
            if (lambda < s) {
                return new ArcFunScalar(new ArcFunComp(left, right), 0);
            }

            long edgesInX = 0;
            for (Equality e : compEqs) {
                if (leftidx.containsAll(e.indexSet())) {
                    edgesInX++;
                    cRightF.add(e);// Inequalities between variables in leftidx remain in the reduced filter.
                }
            }
            if (edgesInX != ((long)s * (s - 1)) / 2) {
                return null; // It is not a clique: certain preconditions (reduction) are required
            }

            long pClique = 1L;
            for (int i = 0; i < s; i++) {
                pClique = Math.multiplyExact(pClique, (long)(lambda - i));
            }

            if (pClique == 0L) {
                return new ArcFunScalar(new ArcFunComp(left, right), 0);
            }

            // Indivisible
            if (pComp % pClique != 0L) { // preliminary conditions not met
                return null;
            }

            long kComp = pComp / pClique;
            kTot = Math.multiplyExact(kTot, kComp);
        }

        if (kTot == 0L) {
            return new ArcFunScalar(new ArcFunComp(left, right), 0);
        }

        // Rescaling of indices
        // Collect the indices of the components occurring in the left tuple.
        final SortedSet<Integer> lidx = new TreeSet<>(ClassFunction.indexSet(cleftComps));

        // Include the indices required by the remaining guards.
        lidx.addAll(Guard.indexSet(cRightF));
        // Project the right tuple onto the components whose indices are in lidx.
        var redRightComps = Util.projection(rightComps, lidx);

        if (redRightComps != rightComps) { // Some right-tuple components have been removed by the projection.
            // Compute the new indices after projecting out unused components.
            Map<Integer, Integer> scaled = Util.scaledIndex(lidx);

            // If the retained indices are no longer consecutive starting from 1,
            // update the indices in the left tuple and in the remaining guards.
            if (lidx.last() > lidx.size()) {
                cleftComps = ClassFunction.scaleIndex(cleftComps, scaled);
                cRightF = (Set<Equality>) scaleGuardIndex(cRightF, scaled, rightFilter.getDomain());
            }
        }

        WNtuple rtx = new WNtuple(redRightComps, right.getDomain());
        rightFilter = newGuard(cRightF, rtx.getCodomain());
        WNtuple tleft = new WNtuple(cleftComps, rightFilter.getDomain());
        rtx = (WNtuple) rtx.joinFilter(rightFilter);

        return new ArcFunScalar(new ArcFunComp(tleft, rtx), Math.multiplyExact(card, Math.toIntExact(kTot)));
    }

    private static void collectIneq(Guard g, Set<Equality> out) {
        if (g == null || g.isTrivial()) {
            return;
        }
        if (g instanceof And and) {
            for (Guard a : and.getArgs()) {
                collectIneq(a, out);
            }
            return;
        }
        if (g instanceof Equality eq && !eq.sign()) {
            out.add(eq);
        }
    }

    private Set<Set<Integer>> extractEqualityClasses(Guard rightFilter) {
        // Extract the equalities from the right-side filter.
        // For an AND guard, group independent equalities;
        // for a single equality, create a group containing only that equality.
        final List<List<Equality>> equalities;
        if (rightFilter instanceof And and) {
            equalities = and.separateIndependentEqualities();
        } else if (rightFilter instanceof Equality equality) {
            equalities = List.of(List.of(equality));
        } else {
            equalities = Collections.emptyList();
        }

        final Set<Set<Integer>> eqClass = new HashSet<>();

        // Build the index set associated with each group of equalities.
        for (List<Equality> group : equalities) {
            Set<Integer> indices = new HashSet<>();
            for (Equality eq : group) {
                // Add the indices of both arguments of each equality.
                indices.addAll(eq.indexSet());
            }
            eqClass.add(indices);
        }

        return eqClass;
    }


    private static boolean isRepeatedIndex(WNtuple tleft) {
        for (Set<Integer> positions : tleft.projectionIndexPositions()) {
            if (positions.size() > 1) {
                return true;
            }
        }
        return false;
    }

    private List<WNtuple> splitHomogeneousTuples(WNtuple left) {
        final List<List<ColorFunction>> alternatives = new ArrayList<>();

        for (ColorFunction component : left.getComponents()) {
            if (component instanceof LinearComb lc) {
                final List<ColorFunction> separated = new ArrayList<>();

                for (Bag<?> bag : lc.separateConst()) {
                    separated.add((ColorFunction) bag);
                }

                alternatives.add(separated);

            } else if (component instanceof Projection || component instanceof All) {
                alternatives.add(List.of(component));

            } else {
                throw new IllegalArgumentException("Unsupported component type: " + component.getClass());
            }
        }

        final Set<Collection<ColorFunction>> combinations = Util.cartesianProd(alternatives);

        final List<WNtuple> result = new ArrayList<>();

        for (Collection<ColorFunction> combination : combinations) {
            result.add(new WNtuple(new ArrayList<>(combination), left.guard(), false));
        }

        return result;
    }


    private ArcFunction composeColorTuples(ArcFunComp comp) {
        WNtuple tleft = comp.left().cast();
        WNtuple rtx = comp.right().cast();
        if (!isRepeatedIndex(tleft)) {
            return tleft.baseCompose(rtx);
        }

        List<Set<Integer>> idxPos = tleft.projectionIndexPositions();

        for (Set<Integer> idxPo : idxPos) {
            if (idxPo.size() <= 1) {
                continue;
            }

            for (Integer i : idxPo) {
                ColorFunction component = tleft.getComponent(i, tleft.getSort());

                if (component instanceof LinearComb lc && lc.asMap().size() > 1) {
                    return splitAndCompose(tleft, rtx, i, true);
                }
            }
        }

        return tleft.repetedIndexCompose(rtx);
    }


    private static Guard newGuard(Set<? extends ElementaryGuard> s, Domain d) {
        if (s.isEmpty())
            return True.getInstance(d);

        return And.factory(NaryGuardOperator.cloneArgs(s, d));
    }

//    // this inner class is used temporarily for debugging purposes, to skip the normalization loop in the simplification of a composition between two tuples
//    // it represents the base case of composition between two tuples, where the left tuple is a simple one
//    // (i.e. it contains only linear combinations of class-functions without constant components) and single-colour
//    // and the associated guard --of the same color-- is trivial or an elementary conjunction
//    // in addition all the components of the right tuple are projected by the left tuple
//    // better to replace it with a suitable method in WNtuple (?), but for the moment it is used only for debugging purposes
//    final static class BaseComp implements ArcFunction {
//        private final WNtuple l, r;
//
//        public BaseComp(WNtuple l, WNtuple r) {
//         this.l = l;
//         this.r = r;
//        }
//
//        public String toString() {
//            return "baseComp("+l+" , "+r+')';
//        }
//
//        @Override
//        public ParametricExpr clone(Domain newdom) {
//            // TODO Auto-generated method stub
//            throw new UnsupportedOperationException("Unimplemented method 'clone'");
//        }
//
//        @Override
//        public Integer cardLb() {
//            // TODO Auto-generated method stub
//            throw new UnsupportedOperationException("Unimplemented method 'cardLb'");
//        }
//
//        @Override
//        public boolean simplified() {
//            return true;
//        }
//
//        @Override
//        public void setSimplified(boolean simplified) {
//            // TODO Auto-generated method stub
//            throw new UnsupportedOperationException("Unimplemented method 'setSimplified'");
//        }
//
//        @Override
//        public Domain getDomain() {
//            return r.getDomain();
//        }
//
//        @Override
//        public Domain getCodomain() {
//            return l.getCodomain();
//        }
//
//    }
}
