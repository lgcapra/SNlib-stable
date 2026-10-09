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
                List<WNtuple> homogeneousTuples = splitHomogeneousTuples(left);

                if (homogeneousTuples.size() > 1) {
                    return new ArcFunComp(
                            ArcFunSum.factory(homogeneousTuples),
                            comp.right().cast()
                    );
                }

                if (!(left.filter().isTrivial())) {
                    //to do
                    return res;
                }

                if (!(comp.right() instanceof WNtuple right))
                    return res;

                if (!right.filter().isTrivial()) { // the right tuple's filter is moved on the left
                    return new ArcFunComp(left.joinGuard(right.filter()).cast(), right.withoutFilter().cast());
                }

                // left tuple, with no constant components
                // Lemma 1
                final Guard lg = left.guard();
                if (! (lg.isTrivial() || lg.isElemAndForm()) )
                    return res;

                int card = 1; // the cardinality of the right-tuple components non-projected
                final Map<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> splitColors
                      = left.splitColors();

                //System.out.println("DEBUG (a):\n"+left+'\n'+splitColors+'\n'+right);
                final Map<ColorClass, ArcFunction> results = new HashMap<>(); // the divided-by color outcomes
                for (Entry<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> cleft : splitColors.entrySet()) {
                   final Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>> pair = cleft.getValue();
                   List<? extends ColorFunction> cleftComps = pair.getKey();
                   final Set<? extends ElementaryGuard> cleftG=  pair.getValue();
                   // new Property 2: we extract constant components from the left tuple, if any
                   final Map<Integer, ColorFunction> constants = Util.select(cleftComps, ColorFunction::isConstant);
                   cleftComps = Util.remove(cleftComps, constants.keySet()); // we remove (in the event) constant components from the left tuple's components
                   final SortedSet<Integer> leftindx = new TreeSet<>(ClassFunction.indexSet(cleftComps));
                   leftindx.addAll(Guard.indexSet(cleftG)); // includes the guard indices too
                   final ColorClass cc = cleft.getKey();
                   final List<? extends ColorFunction> rightComps = right.getHomSubTuple(cc),
                                                    redRightComps = Util.projection(rightComps, leftindx);

                   final WNtuple rtx = new WNtuple(redRightComps, right.getDomain()); //right.guard() ?
                   ArcFunction cc_res;
                   Guard leftGuard = newGuard(cleftG, rtx.getCodomain());
                   if (redRightComps != rightComps) { // some components of the right tuple are projected out
                        for (int i = 1; i <= rightComps.size(); i++) { // we check the cardinality of the non-projected components of the right tuple
                            if (! leftindx.contains(i) ) {
                                final Integer c = rightComps.get(i -1).cardLb();
                                    if (c == null)
                                        return res; // the cardinality of a non-projected component is not known, so we cannot apply Lemma 1
                                    else
                                        card *= rightComps.get(i -1).cardLb();
                            }
                        }
                        // we rescale (in the event) rojection indices in the tuple sx
                       Map<Integer, Integer> scaled = Util.scaledIndex(leftindx);
                       if (leftindx.last() > leftindx.size()) {
                           cleftComps = ClassFunction.scaleIndex(cleftComps, scaled);
                           leftGuard = scaleGuardIndex(leftGuard, scaled);
                       }
                    }

                    WNtuple tleft = new WNtuple(cleftComps, leftGuard, false);

                    if (isRepeatedIndex(tleft)) {
                        cc_res = tleft.repetedIndexCompose(rtx);
                    } else {
                        cc_res = tleft.baseCompose(rtx);
                    }

                    cc_res = ArcFunExpansion.factory(cc_res, constants);
                    results.put(cc, cc_res);
                } // for each color class

                // we build the scalar product of juxtaposition of results
                // System.out.println("DEBUG (b):\n"+results);
                res = ArcFunScalar.factory(ArcFunJuxt.factory(results, right.guard()), card); // k⋅(Tr∘TX′);
            }
        }
        //System.out.println("(ArcFunComp) simplified to: " + res + ", class: " + res.getClass()); //debug
        return res;
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
        final List<LinearComb> leftComps =
                (List<LinearComb>) Util.cast(left.getComponents(), LinearComb.class);

        final List<List<LinearComb>> l2lb = new ArrayList<>();

        for (LinearComb t : leftComps) {
            List<LinearComb> llb =
                    (List<LinearComb>) Util.cast(t.separateConst(), LinearComb.class);
            l2lb.add(llb);
        }

        final Set<Collection<LinearComb>> cpr =
                Util.cartesianProd(l2lb);

        final List<WNtuple> result = new ArrayList<>();

        for (Collection<LinearComb> llc : cpr) {
            result.add(
                    new WNtuple(
                            (List<LinearComb>) llc,
                            left.guard(),
                            false
                    )
            );
        }

        return result;
    }

    private static Guard scaleGuardIndex(Guard g, Map<Integer, Integer> m) {
        if (g == null || g.isTrivial() || m.isEmpty()) {
            return g;
        }

        switch (g) {
            case Equality eq -> {
                var p1 = eq.getArg1();
                var p2 = eq.getArg2();

                Integer n1 = m.get(p1.getIndex());
                Integer n2 = m.get(p2.getIndex());

                if (n1 != null) {
                    p1 = p1.setIndex(n1);
                }
                if (n2 != null) {
                    p2 = p2.setIndex(n2);
                }

                return Equality.builder(p1, p2, eq.sign(), g.getDomain());
            }
            case And and -> {
                Set<Guard> args = new HashSet<>();
                for (Guard a : And.getArgs(g)) {
                    args.add(scaleGuardIndex(a, m));
                }
                return And.factory(args);
            }
            case Or or -> {
                Set<Guard> args = new HashSet<>();
                for (Guard a : or.getArgs()) {
                    args.add(scaleGuardIndex(a, m));
                }
                return Or.factory(args, or.disjoined());
            }
            case Neg neg -> {
                return Neg.factory(scaleGuardIndex(neg.getArg(), m));
            }
            case Membership mem -> {
                var p = mem.getArg1();
                Integer ni = m.get(p.getIndex());
                if (ni != null) {
                    p = p.setIndex(ni);
                }
                return Membership.build(p, mem.getArg2(), mem.sign(), mem.getDomain());
            }
            default -> {
            }
        }

        return g;
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
