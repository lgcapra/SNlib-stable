package wnbag;

import bagexpr.BagComp;
import bagexpr.BagExpr;
import classfunction.ClassFunction;
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
        //System.out.println("\nArcFunComp.specSimplify of:\n" + res+':'+getClass()); //debug
        if (res instanceof ArcFunComp comp) {
            // caso base: composizione tra WNtuple (tuple di class-functions) dove sx contiene solo LinearCombs
            if (comp.left() instanceof WNtuple left && left.simple()){
                // Preliminary step: we make tuples' inner component exclusively contain constant or non-constant functions
                final List<LinearComb> leftComps = (List<LinearComb>) Util.cast(left.getComponents(), LinearComb.class); 
                final List<List<LinearComb>> l2lb = new ArrayList<>();
                for (LinearComb t : leftComps) {
                    List<LinearComb> llb = (List<LinearComb>) Util.cast(t.separateConst(), LinearComb.class);                    
                    l2lb.add(llb);
                }
                   
                final Set<Collection<LinearComb>> cpr = Util.cartesianProd(l2lb); //actually a set of lists
                if (cpr.size() > 1) { //we split the tuple so that components are homogeneous (only constants or non-constants)
                    final Collection<WNtuple> sum = new ArrayList<>();
                    for (Collection<LinearComb> llc : cpr) {
                        sum.add(new  WNtuple((List<LinearComb>) llc, left.guard(), false));
                   }

                   return new ArcFunComp(ArcFunSum.factory(sum), comp.right().cast());
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
                    cc_res = ArcFunExpansion.factory(tleft.baseCompose(rtx), constants); // base case of composition between tuples (possibly expanded by the constant components of the left tuple)
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
