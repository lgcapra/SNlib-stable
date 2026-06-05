//package wnbag;
//
//import java.util.*;
//
//import bagexpr.Bag;
//import classfunction.ClassFunction;
//import classfunction.ElementaryFunction;
//import classfunction.Projection;
//import color.ColorClass;
//import color.Sort;
//import expr.*;
//import util.Util;
//import guard.Equality;
//
///**
// * this class defines linear combination of basic class-funtions
// * @author lorenzo capra
// */
//public final class LinearComb implements ClassFunction {
//
//    private final Map<ElementaryFunction, Integer> map;
//    private final ColorClass cc;
//    private HashMap<Integer, Map<ElementaryFunction, Integer>> components; // cache
//
//    /**
//     * base constructor: creates a linear-combination of (elementary) class-functions
//     * @param m a map corresponding to the linear combination
//     */
//    public LinearComb(Map<ElementaryFunction, Integer> m) {
//        Map<ElementaryFunction, Integer> mutable = new HashMap<>(m);
//        mutable.values().removeAll(Collections.singleton(0)); // rimuovi i 0
//
//        if (mutable.isEmpty()) {
//            throw new NoSuchElementException("LinearComb map cannot be empty");
//        }
//
//        ElementaryFunction first = mutable.keySet().iterator().next();
//        this.cc = first.getSort();
//        this.map = Collections.unmodifiableMap(mutable);
//    }
//
//    /**
//     * creates a linear-combination from a map with disjointness flag (ignored for compatibility)
//     * @param m a map corresponding to the linear combination
//     * @param disjoint ignored (kept for API compatibility)
//     */
//    public LinearComb(Map<ElementaryFunction, Integer> m, boolean disjoint) {
//        this(m);  // ignoriamo il flag disjoint
//    }
//
//    /**
//     * creates a linear-combination from a collection
//     * @param c a collection of class-functions
//     */
//    public LinearComb(Collection<? extends ElementaryFunction> c) {
//        this(Util.asMap(c));
//    }
//
//    /**
//     * creates a linear-combination from a varargs list of functions
//     * @param f a list of functions
//     */
//    public LinearComb(ElementaryFunction ... f) {
//        this(Arrays.asList(f));
//    }
//
//    /**
//     * creates an elementary (i.e., single-element) linear-combination
//     * @param f the only function
//     * @param k its multiplicity
//     */
//    public LinearComb(ElementaryFunction f, int k) {
//        this(Util.singleMap(f, k));
//    }
//
//    /**
//     * creates an empty linear combination
//     * @param cc the color class
//     */
//    public LinearComb(ColorClass cc) {
//        this.map = Collections.emptyMap();
//        this.cc = cc;
//    }
//
//    // ========== Getters e metodi essenziali ==========
//
//    public Map<ElementaryFunction, Integer> asMap() {
//        return this.map;
//    }
//
//    public Set<ElementaryFunction> support() {
//        return this.map.keySet();
//    }
//
//    public int mult(ElementaryFunction e) {
//        Integer x = this.map.get(e);
//        return x != null ? x : 0;
//    }
//
//    public int size() {
//        return this.map.size();
//    }
//
//    public boolean isEmpty() {
//        return this.map.isEmpty();
//    }
//
//    // ========== ClassFunction interface implementation ==========
//
//    @Override
//    public ColorClass getSort() {
//        return this.cc;
//    }
//
//    @Override
//    public LinearComb setDefaultIndex() {
//        Set<Integer> s = indexSet();
//        if (s.isEmpty() || s.size() == 1 && s.iterator().next() == 1)
//            return this;
//
//        Map<ElementaryFunction, Integer> m = new HashMap<>();
//        support().forEach(x -> { m.put(x.setDefaultIndex(), mult(x)); });
//
//        return new LinearComb(m);
//    }
//
//    @Override
//    public int splitDelim() {
//        return ClassFunction.splitDelim(support(), getSort());
//    }
//
//    /**
//     * extracts the components of this l.c. composed of terms with the same index
//     */
//    public HashMap<Integer, Map<ElementaryFunction, Integer>> components() {
//        if (this.components == null) {
//            this.components = new HashMap<>();
//            support().forEach(f -> {
//                Integer i = f instanceof Projection ? ((Projection)f).getIndex() : 0;
//                Map<ElementaryFunction, Integer> b = this.components.get(i);
//                if (b == null)
//                    this.components.put(i, b = new HashMap<>());
//                b.put(f, mult(f));
//            });
//
//            Map<ElementaryFunction, Integer> b;
//            if (this.components.size() > 1 && (b = this.components.get(0)) != null) {
//                this.components.remove(0);
//                this.components.get(this.components.keySet().iterator().next()).putAll(b);
//            }
//        }
//        return this.components;
//    }
//
//    /**
//     * @return the index set of this function
//     */
//    @Override
//    public Set<Integer> indexSet() {
//        Set<Integer> s = components().keySet();
//        return s.contains(0) ? Collections.emptySet() : s;
//    }
//
//    @Override
//    public LinearComb replace(Equality eq) {
//        HashMap<ElementaryFunction, Integer> copy = new HashMap<>();
//        asMap().entrySet().forEach(x -> {
//            ElementaryFunction k = x.getKey().replace(eq);
//            Integer m = copy.get(k);
//            copy.put(k, x.getValue() + (m == null ? 0 : m));
//        });
//
//        return new LinearComb(copy);
//    }
//
//    @Override
//    public LinearComb copy(ColorClass newcc) {
//        HashMap<ElementaryFunction, Integer> newmap = new HashMap<>();
//        asMap().entrySet().forEach(e -> { newmap.put(e.getKey().copy(newcc), e.getValue()); });
//
//        return new LinearComb(newmap);
//    }
//
//    public LinearComb build(Map<ElementaryFunction, Integer> smap, boolean disj) {
//        return new LinearComb(smap);
//    }
//
//    @Override
//    public ParametricExpr clone(Map<Sort, Sort> split_map) {
//        return null;
//    }
//
//    @Override
//    public LinearComb clone(final Domain newdom) {
//        return (LinearComb) ClassFunction.super.clone(newdom);
//    }
//
//    @Override
//    public Map<Sort, Integer> splitDelimiters() {
//        return ClassFunction.super.splitDelimiters();
//    }
//
//    @Override
//    public boolean simplified() {
//        return false;
//    }
//
//    @Override
//    public void setSimplified(boolean simplified) {
//
//    }
//
//    @Override
//    public Domain getDomain() {
//        return null;
//    }
//
//    @Override
//    public Domain getCodomain() {
//        return null;
//    }
//
//    @Override
//    public <E extends Expression> Class<E> type() {
//        return null;
//    }
//
//    @Override
//    public String toString() {
//        if (isEmpty())
//            return "0_" + cc;
//
//        StringBuilder sb = new StringBuilder();
//        boolean first = true;
//        for (Map.Entry<ElementaryFunction, Integer> e : asMap().entrySet()) {
//            if (!first && e.getValue() >= 0)
//                sb.append("+");
//            sb.append(e.getValue()).append(".").append(e.getKey());
//            first = false;
//        }
//        return sb.toString();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof LinearComb)) return false;
//        LinearComb other = (LinearComb) o;
//        return this.map.equals(other.map) && this.cc.equals(other.cc);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(this.map, this.cc);
//    }
//
//    public Bag<ElementaryFunction> asBag() {
//        return new LinearCombBag(this.asMap());
//    }
//}