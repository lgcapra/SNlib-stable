package wnbag;

import bagexpr.BagComp;
import bagexpr.BagExpr;
import classfunction.ProjectionBased;
import color.ColorClass;
import expr.Domain;
import expr.ParametricExpr;
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
                    return new ArcFunScalar(new ArcFunComp(t, (ArcFunction) comp.right()), i);
                } else { // (T1+T2+…)∘T′→T1∘T′+T2∘T′+…
                    return new ArcFunSum(
                            tupleBag.support().stream()
                                    .map(t -> new ArcFunComp(t, (ArcFunction) comp.right()))
                                    .toList()
                    );
                }
            }
            if (comp.left() instanceof WNtuple t){ // Lemma 1
                if (!(comp.right() instanceof WNtuple right))
                    return res;

                Set<Integer> used = t.getComponents().stream()
                        .flatMap(cf -> cf.indexSet().stream())
                        .collect(Collectors.toSet());

                Set<Integer> unused = right.getComponents().stream()
                        .flatMap(cf -> cf.indexSet().stream()).collect(Collectors.toSet());
                unused.removeAll(used);

                List<ColorFunction> reducedComponents = right.getComponents().stream()
                        .filter(cf -> {
                            Set<Integer> idx = cf.indexSet();
                            return used.containsAll(idx);
                        })
                        .collect(Collectors.toList());

                WNtuple tx = new WNtuple(
                        reducedComponents,
                        right.guard(),
                        true
                );

                List<ColorFunction> restComponents = right.getComponents().stream()
                        .filter(cf -> {
                            Set<Integer> idx = cf.indexSet();
                            return !idx.isEmpty() && unused.containsAll(idx);
                        })
                        .collect(Collectors.toList());

                WNtuple tr = new WNtuple(
                        restComponents,
                        right.guard(),
                        true
                );

                Map<Integer, ColorClass> indexSort = new HashMap<>();

                right.getComponents().forEach(cf -> {
                    if (cf instanceof ProjectionBased pb) {
                        indexSort.put(pb.getIndex(), pb.getSort());
                    }
                });

                int k = 1;

                for(Integer idx : unused){
                    ColorClass cc = indexSort.get(idx);
                    k *= cc.lb();
                }

                return new ArcFunScalar(new ArcFunComp(tr, tx), k); // k⋅(Tr∘TX′)
            }
        }
        return res;
    }

}
