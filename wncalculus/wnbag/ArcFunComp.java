package wnbag;

import bagexpr.BagComp;
import bagexpr.BagExpr;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import color.ColorClass;
import expr.Domain;
import expr.ParametricExpr;
import guard.And;
import guard.ElementaryGuard;
import guard.Guard;
import guard.NaryGuardOperator;
import guard.True;
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

//         if (res instanceof ArcFunComp comp) {
//             if (comp.left() instanceof WNtuple left && left.simple()) {
//                 System.out.println("DEBUG: entered specSimplify with WNtuple left");
//
//                 List<WNtuple> homogeneousTuples = splitHomogeneousTuples(left);
//
//                 if (homogeneousTuples.size() > 1) {
//                     System.out.println("DEBUG: splitting homogeneous tuples");
//                     return new ArcFunComp(
//                             ArcFunSum.factory(homogeneousTuples),
//                             comp.right().cast()
//                     );
//                 }
//
//                 if (!(left.filter().isTrivial())) {
//                     System.out.println("DEBUG: left filter not trivial");
//                     return res;
//                 }
//
//                 if (!(comp.right() instanceof WNtuple right)) {
//                     System.out.println("DEBUG: right is not WNtuple");
//                     return res;
//                 }
//
//                 if (!right.filter().isTrivial()) {
//                     System.out.println("DEBUG: right filter not trivial");
//                     return new ArcFunComp(left.joinGuard(right.filter()).cast(), right.withoutFilter().cast());
//                 }
//
//                 final Guard lg = left.guard();
//                 if (!(lg.isTrivial() || lg.isElemAndForm())) {
//                     System.out.println("DEBUG: left guard not acceptable: " + lg);
//                     return res;
//                 }
//
//                 System.out.println("DEBUG: proceeding with composition");
//
//                 int card = 1;
//                 final Map<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> splitColors
//                         = left.splitColors();
//
//                 System.out.println("DEBUG: splitColors = " + splitColors);
//                 final Map<ColorClass, ArcFunction> results = new HashMap<>();
//
//                 for (Entry<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> cleft : splitColors.entrySet()) {
//                     System.out.println("DEBUG: processing color class " + cleft.getKey());
//
//                     final Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>> pair = cleft.getValue();
//                     List<? extends ColorFunction> cleftComps = pair.getKey();
//                     final Set<? extends ElementaryGuard> cleftG = pair.getValue();
//
//                     System.out.println("DEBUG: cleftComps before = " + cleftComps);
//
//                     final Map<Integer, ColorFunction> constants = Util.select(cleftComps, ColorFunction::isConstant);
//                     System.out.println("DEBUG: constants = " + constants);
//
//                     cleftComps = Util.remove(cleftComps, constants.keySet());
//                     System.out.println("DEBUG: cleftComps after remove = " + cleftComps);
//
//                     final SortedSet<Integer> leftindx = new TreeSet<>(ClassFunction.indexSet(cleftComps));
//                     leftindx.addAll(Guard.indexSet(cleftG));
//                     System.out.println("DEBUG: leftindx = " + leftindx);
//
//                     final ColorClass cc = cleft.getKey();
//                     final List<? extends ColorFunction> rightComps = right.getHomSubTuple(cc);
//                     System.out.println("DEBUG: rightComps = " + rightComps);
//
//                     final List<? extends ColorFunction> redRightComps = Util.projection(rightComps, leftindx);
//                     System.out.println("DEBUG: redRightComps = " + redRightComps);
//
//                     final List<? extends ColorFunction> scaledRedRightComps =
//                             scaleRedRightComps(redRightComps, rightComps, leftindx);
//                     System.out.println("DEBUG: scaledRedRightComps = " + scaledRedRightComps);
//
//                     if (scaledRedRightComps == null) {
//                         System.out.println("DEBUG: scaledRedRightComps is null, returning res");
//                         return res;
//                     }
//                 }
//             }
//         }

                 //System.out.println("\nArcFunComp.specSimplify of:\n" + res+':'+getClass()); //debug
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

                    final List<? extends ColorFunction> scaledRedRightComps = redRightComps;
//                            scaleRedRightComps(redRightComps, rightComps, leftindx);
//
//                    if (scaledRedRightComps == null) {
//                        return res;
//                    }

                   final WNtuple rtx = new WNtuple(scaledRedRightComps, right.getDomain()); //right.guard() ?
                   ArcFunction cc_res;
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
                        if (leftindx.last() > leftindx.size()) {
                            cleftComps = ClassFunction.scaleIndex(cleftComps, Util.scaledIndex(leftindx));
                        }
                    }

                    WNtuple tleft = new WNtuple(cleftComps, newGuard(cleftG, rtx.getCodomain()), false);

                    if (isRepeatedIndex(tleft)) {
                        cc_res = new BaseComp(tleft, rtx);
                    } else {
                        cc_res = tleft.baseCompose(rtx);
                    }

                    cc_res = ArcFunExpansion.factory(cc_res, constants);
                    results.put(cc, cc_res);
                } // for each color class

                // we build the scalar product of juxtaposition of results
                //System.out.println("DEBUG (b):\n"+results);
                res = ArcFunScalar.factory(ArcFunJuxt.factory(results, right.guard()), card); // k⋅(Tr∘TX′);
            }
        }
        //System.out.println("(ArcFunComp) simplified to: " + res + ", class: " + res.getClass()); //debug
        return res;
    }

    private static boolean isRepeatedIndex(WNtuple tleft) {
        final Map<Integer, Set<Integer>> idxPos = tleft.projectionIndexPositions();

        // check repeated projection indices
        boolean repeatedIndex = false;
        for (Entry<Integer, Set<Integer>> e : idxPos.entrySet()) {
            if (e.getValue().size() != 1) {
                repeatedIndex = true;
                break;
            }
        }
        return repeatedIndex;
    }

//    private List<? extends ColorFunction> scaleRedRightComps(
//            List<? extends ColorFunction> redRightComps,
//            List<? extends ColorFunction> rightComps,
//            SortedSet<Integer> leftindx) {
//
//        if (redRightComps == rightComps) {
//            return redRightComps;
//        }
//
//        // Crea una lista di remapping per i nuovi indici
//        // La nuova posizione i-esima avrà indice i
//        final List<Integer> oldIndices = new ArrayList<>(leftindx);
//        final Map<Integer, Integer> positionRemapping = new HashMap<>();
//        for (int i = 0; i < oldIndices.size(); i++) {
//            positionRemapping.put(oldIndices.get(i), i + 1);
//        }
//
//        final List<ColorFunction> tmp = new ArrayList<>();
//
//        for (ColorFunction cf : redRightComps) {
//            if (cf instanceof LinearComb lc) {
//                final Map<ElementaryFunction, Integer> newMap = new HashMap<>();
//
//                for (Map.Entry<? extends ElementaryFunction, Integer> e : lc.asMap().entrySet()) {
//                    final ElementaryFunction ef = e.getKey();
//                    final int mult = e.getValue();
//
//                    if (ef instanceof classfunction.Projection p) {
//                        final Integer newIdx = positionRemapping.get(p.getIndex());
//                        if (newIdx != null) {
//                            newMap.merge(p.setIndex(newIdx), mult, Integer::sum);
//                        } else {
//                            newMap.merge(ef, mult, Integer::sum);
//                        }
//                    } else {
//                        newMap.merge(ef, mult, Integer::sum);
//                    }
//                }
//
//                tmp.add(new LinearComb(newMap, false));
//            } else {
//                tmp.add(cf);
//            }
//        }
//
//        return tmp;
//    }

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

    private static Guard newGuard(Set<? extends ElementaryGuard> s, Domain d) {
        if (s.isEmpty())
            return True.getInstance(d);
        
        return And.factory(NaryGuardOperator.cloneArgs(s, d));
    }

    // this inner class is used temporarily for debugging purposes, to skip the normalization loop in the simplification of a composition between two tuples
    // it represents the base case of composition between two tuples, where the left tuple is a simple one 
    // (i.e. it contains only linear combinations of class-functions without constant components) and single-colour 
    // and the associated guard --of the same color-- is trivial or an elementary conjunction
    // in addition all the components of the right tuple are projected by the left tuple
    // better to replace it with a suitable method in WNtuple (?), but for the moment it is used only for debugging purposes
    final static class BaseComp implements ArcFunction { 
        private final WNtuple l, r;

        public BaseComp(WNtuple l, WNtuple r) {
         this.l = l;
         this.r = r;
        }

        public String toString() {
            return "baseComp("+l+" , "+r+')';  
        }

        @Override
        public ParametricExpr clone(Domain newdom) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'clone'");
        }

        @Override
        public Integer cardLb() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'cardLb'");
        }

        @Override
        public boolean simplified() {
            return true;
        }

        @Override
        public void setSimplified(boolean simplified) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setSimplified'");
        }

        @Override
        public Domain getDomain() {
            return r.getDomain();
        }

        @Override
        public Domain getCodomain() {
            return l.getCodomain();
        }

    }
}
