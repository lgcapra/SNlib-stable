///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package wnbag;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import bagexpr.AbstractBag;
//import expr.Domain;
//import guard.Guard;
//import tuple.FilteredTuple;
//import tuple.FunctionTuple;
//import util.Util;
//
///**
// * this class defines SN functions as bags of <tt>FunctionTuple</tt>s
// * @author lorenzo capra
// */
//public final class FunctionTupleBag extends AbstractBag<FunctionTuple> implements SNArcFunction  {
//
//     public FunctionTupleBag(Map<FunctionTuple, Integer> m, boolean disjoint) {
//        super(m);
//        setDisjoined(disjoint);
//    }
//
//    public FunctionTupleBag(Map<FunctionTuple, Integer> m) {
//        this(m,false);
//    }
//
//    public FunctionTupleBag(Domain dom, Domain codom) {
//        super(dom, codom);
//    }
//
//    /**
//     * build an empty bag
//     * @param dom the bag's co-domain
//     */
//    public FunctionTupleBag(Domain dom) {
//        this(dom, dom);
//    }
//
//    //costruttori secondari (si potrebbero togliere: sono già definiti metodi corrispondenti in BagBuilder)
//
//    public FunctionTupleBag(FunctionTuple f , int k) {
//        super(Util.singleMap(f, k));
//    }
//
//
//    @Override
//    public FunctionTupleBag applyFilter(Guard f) {
//        Map<FunctionTuple, Integer> mx = new HashMap<>();
//        asMap().entrySet().forEach(e -> { mx.put(FilteredTuple.factory(f, e.getKey()), e.getValue()); });
//
//        return new FunctionTupleBag(mx, disjoined());
//    }
//
//    @Override
//    public Integer card() {
//        int card = 0;
//        for (Map.Entry<? extends FunctionTuple, Integer> x : asMap().entrySet()) {
//            Integer k = x.getKey().cardLb();
//            if (k == null)
//                return null;
//            card += k * x.getValue();
//        }
//        return card;
//    }
//
//    @Override
//    public FunctionTupleBag build(Domain dom, Domain codom) {
//        return new FunctionTupleBag(dom,codom);
//    }
//
//    public FunctionTupleBag build(Map<FunctionTuple, Integer> smap, boolean disj) {
//        FunctionTupleBag b = new FunctionTupleBag(smap);
//        b.setDisjoined(disj);
//        return b;
//    }
//
//    @Override
//    public FunctionTupleBag build(Map<FunctionTuple, Integer> m) {
//        return m == null || m.isEmpty() ? (FunctionTupleBag) build() : build(m, false);
//    }
//
//}
