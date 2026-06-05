package wnbag;

import bagexpr.BagComp;

public final class ArcFunComp extends BagComp<WNtuple> implements ArcFunction {

     
     public ArcFunComp(ArcFunction l, ArcFunction r) {
         super(l, r);
     }

}
