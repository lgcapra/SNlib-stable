package wnbag;

import bagexpr.BagComp;
import bagexpr.BagExpr;
import classfunction.ProjectionBased;
import color.ColorClass;
import util.Util;
import wncolorfunction.ColorFunction;

import java.util.*;
import java.util.stream.Collectors;

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
            if (comp.left() instanceof TupleBag tupleBag){
                if (tupleBag.size() == 1){ //(n⋅T)∘T′→n⋅(T∘T′)
                    WNtuple t = tupleBag.support().iterator().next();
                    int i = t.cardLb();
                    return ArcFunScalar.factory(new ArcFunComp(t, (ArcFunction) comp.right()), i);
                } else { // (T1+T2+…)∘T′→T1∘T′+T2∘T′+…
                    return ArcFunSum.factory(
                            tupleBag.support().stream()
                                    .map(t -> new ArcFunComp(t, (ArcFunction) comp.right()))
                                    .toList()
                    );
                }
            }
            // caso base: composizione tra WNtuple (tuple di class-functions)
            if (comp.left() instanceof WNtuple left){ // Lemma 1
                if (!(comp.right() instanceof WNtuple right))
                    return res;

                TreeSet<Integer> leftindx = left.getComponents().stream()
                        .flatMap(cf -> cf.indexSet().stream())
                        .collect(Collectors.toCollection(TreeSet::new));
                 
                List<ColorFunction> rightcomp = right.getComponents();
                List<? extends ColorFunction> reducedComponents = Util.projection(rightcomp, leftindx);
                if (reducedComponents == rightcomp) {

                } else {
                    WNtuple tx = new WNtuple(reducedComponents,right.guard(), false);
                    // calcoliamo la cardinalità della sotto-tupla dx "non proiettata"
                    int card = 1;
                    for (int i = 1; i <= rightcomp.size(); i++)
                        if (!leftindx.contains(i)) {
                             card *= rightcomp.get(i -1).cardLb(); 
                        }
                  // riscalare gli indici delle proiezioni nella tupla sx
                   ArrayList<ColorFunction> lscaled = new ArrayList<>(left.getComponents());
                   // to do 
                   WNtuple tr = new WNtuple(lscaled, left.guard(), false);
                   return ArcFunScalar.factory(new ArcFunComp(tr, tx), card); // k⋅(Tr∘TX′);
                     
                }
            }
        }
        return res;
    }

}
