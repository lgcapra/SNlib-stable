package wnbag;

import bagexpr.AbstractBag;
import bagexpr.Bag;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import classfunction.Projection;
import color.ColorClass;
import color.Sort;
import expr.Domain;
import expr.ParametricExpr;
import guard.Equality;
import util.Util;

import java.util.*;

    /**
     * this class defines linear combination of basic class-funtions
     * @author lorenzo capra
     */
    public final class LinearComb extends AbstractBag<ElementaryFunction> implements ClassFunction {

        private final ColorClass cc;
        private HashMap<Integer, Map<ElementaryFunction, Integer>> components; // cache

        /**
         * base constructor: creates a linear-combination of (elementary) class-functions
         * @param m a map corresponding to the linear combination
         */
        public LinearComb(Map<ElementaryFunction, Integer> m) {
            super(m);
            if (m.isEmpty()) {
                throw new NoSuchElementException("LinearComb map cannot be empty");
            }
            ElementaryFunction first = m.keySet().iterator().next();
            this.cc = first.getSort();
            for (ElementaryFunction elementaryFunction : m.keySet()) {
                if (!elementaryFunction.getSort().equals(this.cc)) {
                    throw new IllegalArgumentException("All elementary functions must have the same color class");
                }
            }
        }

        /**
         * creates a linear-combination from a map with disjointness flag (ignored for compatibility)
         * @param m a map corresponding to the linear combination
         * @param disjoint ignored (kept for API compatibility)
         */
        public LinearComb(Map<ElementaryFunction, Integer> m, boolean disjoint) {
            this(m);  // ignoriamo il flag disjoint
        }

        /**
         * creates a linear-combination from a collection
         * @param c a collection of class-functions
         */
        public LinearComb(Collection<? extends ElementaryFunction> c) {
            this(Util.asMap(c));
        }

        /**
         * creates a linear-combination from a varargs list of functions
         * @param f a list of functions
         */
        public LinearComb(ElementaryFunction ... f) {
            this(Arrays.asList(f));
        }

        /**
         * creates an elementary (i.e., single-element) linear-combination
         * @param f the only function
         * @param k its multiplicity
         */
        public LinearComb(ElementaryFunction f, int k) {
            this(Util.singleMap(f, k));
        }

        // ========== ClassFunction interface implementation ==========

        @Override
        public ColorClass getSort() {
            return this.cc;
        }

        @Override
        public int splitDelim() {
            throw new UnsupportedOperationException("splitDelim is not supported for LinearComb2");
        }

        @Override
        public wnbag.LinearComb setDefaultIndex() {
            Set<Integer> s = indexSet();
            if (s.isEmpty() || s.size() == 1 && s.iterator().next() == 1)
                return this;

            Map<ElementaryFunction, Integer> m = new HashMap<>();
            support().forEach(x -> { m.put(x.setDefaultIndex(), mult(x)); });

            return new wnbag.LinearComb(m);
        }

        /**
         * extracts the components of this l.c. composed of terms with the same index
         */
        public HashMap<Integer, Map<ElementaryFunction, Integer>> components() {
            if (this.components == null) {
                this.components = new HashMap<>();
                support().forEach(f -> {
                    Integer i = f instanceof Projection ? ((Projection)f).getIndex() : 0;
                    Map<ElementaryFunction, Integer> b = this.components.get(i);
                    if (b == null)
                        this.components.put(i, b = new HashMap<>());
                    b.put(f, mult(f));
                });

                Map<ElementaryFunction, Integer> b;
                if (this.components.size() > 1 && (b = this.components.get(0)) != null) {
                    this.components.remove(0);
                    this.components.get(this.components.keySet().iterator().next()).putAll(b);
                }
            }
            return this.components;
        }

        /**
         * @return the index set of this function
         */
        @Override
        public Set<Integer> indexSet() {
            Set<Integer> s = components().keySet();
            return s.contains(0) ? Collections.emptySet() : s;
        }

        @Override
        public wnbag.LinearComb replace(Equality eq) {
            HashMap<ElementaryFunction, Integer> copy = new HashMap<>();
            asMap().forEach((key1, value) -> {
                ElementaryFunction k = key1.replace(eq);
                copy.compute(k, (key, m) -> value + (m == null ? 0 : m));
            });

            return new wnbag.LinearComb(copy);
        }

        @Override
        public wnbag.LinearComb copy(ColorClass newcc) {
            HashMap<ElementaryFunction, Integer> newmap = new HashMap<>();
            asMap().forEach((key, value) -> newmap.put(key.copy(newcc), value));

            return new wnbag.LinearComb(newmap);
        }

        public wnbag.LinearComb build(Map<ElementaryFunction, Integer> smap, boolean disj) {
            return new wnbag.LinearComb(smap);
        }

        @Override
        public ParametricExpr clone(Map<Sort, Sort> split_map) {
            throw new UnsupportedOperationException("clone with split_map is not supported for LinearComb2");
        }

        @Override
        public Map<Sort, Integer> splitDelimiters() {
            throw new UnsupportedOperationException("splitDelimiters is not supported for LinearComb2");
        }

        @Override
        public Bag<ElementaryFunction> build(Domain dom, Domain codom) {
            return null;
        }

        @Override
        public Bag<ElementaryFunction> build(Map<ElementaryFunction, Integer> m) {
            return null;
        }
    }
