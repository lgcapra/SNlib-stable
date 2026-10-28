package wnbag;

import bagexpr.BagComp;
import bagexpr.BagExpr;
import classfunction.ClassFunction;
import color.ColorClass;
import expr.Domain;
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

                    //Property 15
                    ArcFunScalar ccr = (ArcFunScalar) cc_res;
                    card = ccr.k();
                    cc_res = property15((ArcFunComp)ccr.getArg(), right, card);
                    //System.out.println(cc_res.toString());

                    //Lemma 4 TODO


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
                    card = ccr.k();
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

    private ArcFunScalar property15(ArcFunComp ccr, WNtuple right, int card){
        WNtuple rtx = ccr.right().cast();
        Guard rightFilter = rtx.filter();
        var rightComps = rtx.getComponents();

        // Keep only the non-equality guards from the right filter.
        Set<ElementaryGuard> crightF = nonEqualityGuards(rightFilter);

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

                crightF = (Set<ElementaryGuard>) scaleGuardIndex(crightF, scaled, rightFilter.getDomain());
            }
        }

        rtx = new WNtuple(redRightComps, right.getDomain());
        rightFilter = newGuard(crightF, rtx.getCodomain());
        var tleft = new WNtuple(cleftComps, rightFilter.getDomain());
        rtx = (WNtuple) rtx.joinFilter(rightFilter);
        return new ArcFunScalar(new ArcFunComp(tleft, rtx), card);
    }

    private Set<ElementaryGuard> nonEqualityGuards(Guard guard) {
        Set<ElementaryGuard> result = new HashSet<>();

        if (guard instanceof And and) {
            for (Guard g : and.getArgs()) {
                if (!g.isEquality()) {
                    result.add(g.cast());
                }
            }
        } else if (guard instanceof ElementaryGuard eg && !eg.isEquality()) {
            result.add(eg);
        }

        return result;
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
        final List<Set<Integer>> idxPos = tleft.projectionIndexPositions();

        // check repeated projection indices
        boolean repeatedIndex = false;
        for (Set<Integer> e : idxPos) {
            if (e.size() != 1) {
                repeatedIndex = true;
                break;
            }
        }
        return repeatedIndex;
    }

    private List<WNtuple> splitHomogeneousTuples(WNtuple left) {
        final List<LinearComb> leftComps = Util.copyAndCast(left.getComponents(), LinearComb.class);

        final List<List<LinearComb>> l2lb = new ArrayList<>();

        for (LinearComb t : leftComps) {
            List<LinearComb> llb = Util.copyAndCast(t.separateConst(), LinearComb.class);
            l2lb.add(llb);
        }

        final Set<Collection<LinearComb>> cpr = Util.cartesianProd(l2lb);

        final List<WNtuple> result = new ArrayList<>();

        for (Collection<LinearComb> llc : cpr) {
            result.add(new WNtuple(Util.copyAndCast(llc, LinearComb.class), left.guard(), false));
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
                LinearComb component = tleft.getComponent(i, tleft.getSort()).cast();

                if (component.asMap().size() > 1) {
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
