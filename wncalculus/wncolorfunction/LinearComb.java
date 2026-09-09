package wncolorfunction;

import bagexpr.Bag;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import classfunction.Projection;
import color.ColorClass;
import color.Sort;
import expr.Domain;
import guard.Equality;
import util.Pair;
import util.Util;

import java.util.*;
import java.util.Map.Entry;

    /**
     * this class defines linear combination of basic class-funtions
     * @author lorenzo capra
     */
    public final class LinearComb extends Bag<ElementaryFunction> implements ColorFunction {

        private final ColorClass cc;
        private HashMap<Integer, Map<ElementaryFunction, Integer>> components; // cache

        /**
         * base constructor: creates a linear-combination of (elementary) class-functions
         * @param m a map corresponding to the linear combination
         */
        public LinearComb(Map<? extends ElementaryFunction, Integer> m, boolean check) {
            super(m);
            if (m.isEmpty()) {
                throw new IllegalArgumentException("Empty linear combination");
            }
            ElementaryFunction first = m.keySet().iterator().next();
            this.cc = first.getSort();    
            if (check) {
                for (ElementaryFunction elementaryFunction : m.keySet()) {
                    if (!elementaryFunction.getSort().equals(this.cc)) {
                        throw new IllegalArgumentException("Elementary functions with different color classes");
                    }
                }
            }
        } 

        public LinearComb(Map<? extends ElementaryFunction, Integer> m ) {
            this(m, false);
        }

        /**
         * creates a linear-combination from a collection
         * @param c a collection of class-functions
         */
        public LinearComb(Collection<? extends ElementaryFunction> c, boolean check) {
            this(Util.asMap(c), check);
        }


        /**
         * creates a linear-combination from a varargs list of functions
         * @param f a list of functions
         */
        public LinearComb(ElementaryFunction ... f) {
            this(Arrays.asList(f), false);
        }

        /**
         * creates an elementary (i.e., single-element) linear-combination
         * @param f the only function
         * @param k its multiplicity
         */
        public LinearComb(ElementaryFunction f, int k) {
            this(Util.singleMap(f, k));
        }
        /**
         * creates an elementary (i.e., single-element) linear-combination with multiplicity 1
         * @param f the only function
         */
        public LinearComb(ElementaryFunction f) {
            this(f, 1);
        }
        
        /**
         * creates an empty linear-combination, corresponding to the given color class 
         * @param cc
         */
        public LinearComb (ColorClass cc) {
            super(new Domain(cc), new Domain(cc));
            this.cc = cc;
        }
        
        /**
         * builds a linear-combination from a collection of pairs (function, multiplicity)
         * parameters are inverted to avoid ambiguity with the other constructor    
         * @param c a collection of pairs (function, multiplicity)
         * @param check whether to check the color class consistency
         */
        public LinearComb (boolean check, Collection<Pair<ElementaryFunction, Integer>> c) {
            this(Util.pairsAsMap(c), check);
        }

        public LinearComb (Pair<ElementaryFunction, Integer> ... c) {
            this(false, Arrays.asList(c));
        }

        // ========== ClassFunction interface implementation ==========

        @Override
        public ColorClass getSort() {
            return this.cc;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E extends ClassFunction> E setDefaultIndex() {
            Set<Integer> s = indexSet();
            if (s.isEmpty() || s.size() == 1 && s.iterator().next() == 1)
                return (E) this;

            Map<ElementaryFunction, Integer> m = new HashMap<>();
            support().forEach(x -> { m.put(x.setDefaultIndex(), mult(x)); });

            return (E) new wncolorfunction.LinearComb(m, false);
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
        @SuppressWarnings("unchecked")
        public <E extends ClassFunction> E replace(Equality eq) {
            HashMap<ElementaryFunction, Integer> copy = new HashMap<>();
            asMap().forEach((key1, value) -> {
                ElementaryFunction k = key1.replace(eq);
                copy.compute(k, (key, m) -> value + (m == null ? 0 : m));
            });

            return (E) new wncolorfunction.LinearComb(copy, false);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <E extends ClassFunction> E copy(ColorClass newcc) {
            HashMap<ElementaryFunction, Integer> newmap = new HashMap<>();
            asMap().forEach((key, value) -> newmap.put(key.copy(newcc), value));

            return (E) new wncolorfunction.LinearComb(newmap, false);
        }

        public wncolorfunction.LinearComb build(Map<ElementaryFunction, Integer> smap, boolean disj) {
            return new wncolorfunction.LinearComb(smap, false);

        }


        @Override
        public  LinearComb buildEmpty(Domain dom, Domain codom) {
            return new LinearComb(asMap(), false);
        }

        @Override
        public  LinearComb build(Map<? extends ElementaryFunction, Integer> m) {
            return new LinearComb (m, false);
        }

        @Override
        public Map<Sort, Integer> splitDelimiters() {
            return super.splitDelimiters();
        }


        @Override
        public LinearComb setIndex(int idx) {
            Set<Integer> iset = this.indexSet();
            if (iset.isEmpty())
                return this;

            if (iset.size() == 1) {
                HashMap<ElementaryFunction, Integer> m = new HashMap<>();
                for (Entry<? extends ElementaryFunction, Integer> e : this.asMap().entrySet()) {
                    m.put(e.getKey().setIndex(idx), e.getValue());
                }
                return new LinearComb(m, false);
            }

            throw new IllegalArgumentException();                   
        }

        
    }
