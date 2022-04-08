package wncalculus.util;

import java.io.IOException;
import java.util.function.*;
import java.util.*;

import wncalculus.expr.Expression;

/**
 * this class collects utility methods for expressions manipulation
 * @author Lorenzo Capra
 */
public class Util  {    
   
   /**
    * searches for elements of given types in a collection
    * @param <E> the type of elements to be searched for
    * @param c the collection to be looked at
    * @param res a list where matching elements are copied
    * @param t the corresponding class tokens
    * @return <code>res</code>
    */
   public static <E>  List<E> filter (Collection<?> c, List<E> res, Class<? extends E> t ) {
       c.stream().filter(o -> t.isInstance(o)).forEachOrdered( o -> { res.add(t.cast(o)); }); 
       
       return res;
   }
       
    /**
     * checks whether in a given collection there is an element compatible with the specified type
     * @param <E> the collection's element type
     * @param c the collection
     * @param type the run-time type which is searched for
     * @return the first occurrence of an element which is compatible with type (doing a cast);
    *  null if there are no such elements
    */
    public static <E> E find(Collection<?> c, Class<E> type) {
         for (Object t : c) 
             if ( type.isInstance(t) ) 
                  return type.cast(t);
                 
         return null;
    }  
        
    /**
     * @param list a list 
     * @param type a type to be searched for
     * @return the position of the first occurrence of an element compatible with
        the specified types;  -1 if there are no such occurrences
     */
     public static int indexOf(List<?> list, Class<?> type) {   
        for (int i = 0; i < list.size() ; i++) 
            if ( type.isInstance(list.get(i)) ) 
                    return i;
                
        return -1;
    }
             
    /**
     * checks whether all the elements of a collection satisfy a given predicate
     * @param <E> the type of collection's elements
     * @param c a collection
     * @param p the predicate to be checked
     * @return <tt>true</tt> if and only if all the elements of the collection meet <tt>p</tt>
     */
    public static <E> boolean  checkAll (Collection<? extends E> c, Predicate<E> p) {
         return c.stream().allMatch(e -> p.test(e) );
    }
    
    /**
     * checks whether any elements of a collection satisfy a given predicate
     * @param <E> the type of collection's elements
     * @param c a collection
     * @param p the predicate to be checked
     * @return <tt>true</tt> if and only if any elements of the collection meet <tt>p</tt>
     */
    public static <E> boolean  checkAny (Collection<? extends E> c, Predicate<E> p) {
         return c.stream().anyMatch(e -> p.test(e) );
    }
    
    /**
     * extracts the elements of a givel collection that met a given predicate
     * the original element disposition is retained
     * @param <E> the type of collection's elements
     * @param c a collection
     * @param p the predicate to be checked
     * @return the set of collection's element meeting <tt>p</tt>
     */
    public static <E> HashSet<E>  filter (Collection<? extends E> c, Predicate<E> p) {
        HashSet<E> s = new HashSet<>();
        c.stream().filter(e -> p.test(e)).forEachOrdered( e -> { s.add(e); });
        
        return s;
    }
    
    /**
     * map the elements of a collection according to a feature ({@code HashSet} is used) and a value
     * @param <E> the the collection's type
     * @param <K> the feature's type
     * @param <V> the value's type
     * @param c the collection
     * @param feature a given feature of collection's elements
     * @param value a given value of collection's elements
     * @return  a feature-based map of collection's elements to sets of values sharing the feature
     */ 
    public static <E, K, V> HashMap<K, Set<V>> mapFeature(Collection<? extends E> c, Function<E,K> feature,  Function<E,V> value) {
        HashMap<K, Set<V>> m = new HashMap<>();
        c.forEach( f -> { addElem(feature.apply(f), value.apply(f), m); });
        
        return m;
    }
     
     
    /** 
     * map the elements of a collection according to a feature ({@code HashSet} is used)
     * @param <E> the collection's type
     * @param <K> the feature's type
     * @param c the collection
     * @param feature a given feature of collection's elements
     * @return a feature-based map of collection's elements to sets sharing the feature
     */
     /*public static <E, K> HashMap<K, Set<E>> mapFeature(Collection<? extends E> c, Function<E,K> feature) {
        HashMap<K, Set<E>> m = new HashMap<>();
        c.forEach( f -> { addElem(feature.apply(f), f, m); });
        
        return m;
    }*/
    public static <E, K> HashMap<K, Set<E>> mapFeature(Collection<? extends E> c, Function<E,K> feature) {
        return mapFeature(c, feature, e -> e);
    }
    /**
     * overloaded version of {@code mapFeature} using {@code SortedSet} for
     * the values of the map
     * @param <E> the collection elements' type
     * @param <K> the feature's type
     * @param c the collection
     * @param feature a given feature of collection's elements
     * @param comp a comparator of collection's elements; if {@code null} the natural ordering is used
     * @return a feature-based map of collection's elements into (ordered, if the comparator is not <code>null</code>)
     * sets sharing the feature
     */
     public static <E, K> Map<K,SortedSet<E>> mapFeature(Collection<? extends E> c, Function<E,K> feature, Comparator<? super E> comp ) {
        Map<K, SortedSet<E>> m = new HashMap<>();
        c.forEach( f -> { addOrdElem(feature.apply(f), f, m, comp); });
        
        return m;
    }
      
     /**
     * map the elements of a collection according to a feature: similar to @see mapFeature, but
     * for using lists instead of (sorted) sets
     * @param <E> the collection elements' type
     * @param <K> the feature's type
     * @param c the collection
     * @param feature a given feature of collection's elements
     * @return a (unmodifiable) feature-based map of collection's elements into lists sharing the feature
     */
     public static <E, K> Map<K, List<? extends E>> mapFeatureToList(Collection<? extends E> c, Function<E,K> feature ) {
        HashMap<K, List<E>> m = new HashMap<>();
        fillMap(m, c, feature);
        Map<K, List<? extends E>> um=Collections.unmodifiableMap(m);
        
        return um;
    }
     
    /**
     * map the elements of a collection according to a feature: similar to @see mapFeatureToList, but
     * for using a sorted map instaed of an hashMap
     * @param <E> the collection elements' type
     * @param <K> the feature's type
     * @param c the collection
     * @param feature a given feature of collection's elements
     * @return a (unmodifiable) feature-based map of collection's elements into lists sharing the feature
     */
    public static <E, K> SortedMap<K, List<? extends E>> sortedmapFeatureToList(Collection<? extends E> c, Function<E,K> feature ) {
        SortedMap<K, List<E>> m = new TreeMap<>();
        fillMap(m, c, feature);
        SortedMap<K, List<? extends E>> um = Collections.unmodifiableSortedMap(m);
        
        return um;
    }
    
    private static <E, K> void fillMap (Map<K, List<E>> m, Collection<? extends E> c, Function<E,K> feature ) {
        c.forEach( f -> {
            K k = feature.apply(f);
            List<E> l = m.get(k);
            if (l == null)
                m.put(k, l = new ArrayList<>());
            l.add(f);
        });
    }
     
    /**
     * filter the elements of a given sub-type, and possibly verifying a certain property, in a collection
     * @param <E> the collection's type
     * @param <F> a sub-type
     * @param c the given collection
     * @param type the specified sub-type's token
     * @return a set with the elements of the given sub-type, meeting the predicate
     */
    public static <E, F extends E> Set<F> getType(Collection<? extends E> c, Class<? extends F> type) {
        Set<F> s = new HashSet<>();
        c.stream().filter(e -> type.isInstance(e)).forEachOrdered(e -> { s.add(type.cast(e)); });
        
        return s;
    }
        
   /**
    * copies the specified collection into a list of specified type by
    * performing a cast of the collection's elements
    * @param <E> the collection's elements type
    * @param c a collection
    * @param t the token type used for the cast
    * @return a copy of the collection whose elements have been casted
    * @throws ClassCastException if any elements canno be casted
    */ 
   public static <E> List<E> copyAndCast(Collection<?> c, Class<E> t) {
       List<E> l = new ArrayList<>();
       c.forEach( x -> 
            { try{l.add(t.cast(x)); } 
                catch (ClassCastException e) {
                System.err.println(e.toString());
                throw new ClassCastException();
            } 
       });
       
       return l;
   }
   
   /**
    * performs a kind of safe cast on a collection's elements 
    * @param <E> the type to cast
    * @param c a collection
    * @param t a token type
    * @param check the type-check flag (is not set, the cast is unsafe!)
    * @return the given collections casted to <tt>Collection&lt;E&gt;</tt>
    * @throws ClassCastException if any elements cannot be casted
    */
   public static <E> Collection<E> cast(Collection<?> c, Class<E> t, boolean check) {
       if ( !check || checkAll(c, t::isInstance) )
           return (Collection<E>) c;
       
       throw new ClassCastException("collection elements have incompatible type\n");
   }
   
    /**
     * overloaded version of @see {cast} not enforcing any real type-check
     * (unsafe operation)
     * @param <E> the type to cast
     * @param c a collection
     * @param t a token type
     * @return the given collections casted to <tt>Collection&lt;E&gt;</tt>
     */
   public static <E> Collection<E> cast(Collection<?> c, Class<E> t) {
       return cast(c, t, false);
   }
   
   /**
    * given a collection, provides a List-view of it
    * @param <E> collection's elements type
    * @param c the collection
    * @return if the collection is actually a List, the collection itself (casted);
    * otherwise a copy of the collection
    */
   public static <E> List<E> asList (Collection<E> c) {
       return c instanceof List ? (List<E>)c : new ArrayList<>(c);
   }
   
   /**
    * given a Collection, provides a Set-view of it
    * @param <E> collection's elements type
    * @param c the collection
    * @return if the collection is actually a Set, the collection itself (casted);
    * otherwise a copy of the collection
    */
   public static <E> Set<E> asSet (Collection<E> c) {
       return c instanceof Set<?> ? (Set<E>)c : new HashSet<>(c);
   }
                   
    /**
     * build a modifiable singleton list
     * @param <T> the list domain
     * @param o the list element
     * @return a singleton list holding o
     */
    public static <T> List<T> singletonList(T o) {
        List<T> single = new ArrayList<>();
        single.add(o);
        
        return single;
    }
        
    /**
     * buil a unmodifiable singleton sorted set
     * @param <T> the set domain
     * @param t the set element
     * @param c a comparator
     * @return a singleton holding t
     */
    public static <T> SortedSet<T> singleton(T t, Comparator<? super T> c) {
        SortedSet<T> single = new TreeSet<>(c);
        single.add(t);
        
        return Collections.unmodifiableSortedSet(single);
    }
    
    /**
     * creates a single-map
     * @param <K> the "keys" type
     * @param <V> the "values" type
     * @param k a key
     * @param v a value
     * @return the map k to v
     */
    public static <K, V> HashMap<K,V> singleMap (K k, V v) {
        HashMap<K,V> map = new HashMap<>();
        map.put(k, v);
        
        return map;
    }
    
    /**
     * build a singleton sorted-map
     * @param <E> the value's type
     * @param <K> the key's type
     * @param k the key
     * @param e the value
     * @return a singleton sorted-map {k = e}
     */
    public static <E, K> SortedMap<K, E> singleSortedMap(K k, E e ) {
        TreeMap<K,  E> m = new TreeMap<>();
        m.put(k, e);
        
        return m;
    }
     
    /**
     * asks the user to press a key before going on
     * (for interactive debug purposes)
     */
    public static void getChar()  {
      try {
          System.out.print("press enter to continue");
          while ( System.in.read() != '\n') {  }
      } 
      catch (IOException ex) { }
    }
    
    /**
     * method for interactive debug
     * @param promp a string to prompt
     * @return the user's answer (<tt>true</tt> means yes)
     */
    public static boolean getYesNot(String promp)  {
      try {
          System.out.print(promp+" (Y/N)");
          int c;
          while ( Character.isSpaceChar(c = System.in.read()) ) {}
          
          return c == 'Y' || c == 'y';
      } 
      catch (IOException ex) { throw new Error(ex); }
      
    }
    
    /**
     * print the call stack for debugging purpose
     */
     public static void printStack() {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) { //debug
            System.out.println(ste);
        }
    }

    
    /**
     * creates an hash-map corresponding to a given collection
     * @param <K> the type of collection's elements
     * @param c the collection
     * @return a (possibly empty) map corresponding to <code>c</code>
     */
    public static <K> HashMap<K,Integer> asMap(Collection<? extends K> c) {
        HashMap<K,Integer> res = new HashMap<>();
        c.forEach(key -> { res.put(key, res.containsKey(key) ?  res.get(key)+1 :  1 ); });

        return res;
    }
        
    /**
     * insert a new element in a map of sets of corresponding type elements;
     * if the provided key is not yet mapped a new set is associated to it
     * @param <K> the type of map's keys
     * @param <E> the type of map's set values
     * @param k a key
     * @param m a map
     * @param e a value
     * @return <tt>true</tt> if and only if the element is added
     */
    public static <K,E>  boolean addElem(K k, E e, Map<K, Set<E> > m ) {
        Set<E> s = m.get(k);
        if (s == null)
            m.put(k, s = new HashSet<>());
        
        return s.add(e);
    }
    
    /**
     * insert a new element in a map of collections of corresponding type elements;
     * if the provided key is not yet mapped a new collection is associated to it:
     * depending of the type parameter either a @code{HashSet} or an @code{ArrayList} is used
     * this version is more general than the three-arguments one
     * @param <K> the type of map's keys
     * @param <E> the type of map's set values
     * @param k a key
     * @param m a map
     * @param e a value
     * @param type the type of newly created collections
     * @return <tt>true</tt> if and only if the element is added
     */
    public static <K,E>  boolean addElem(K k, E e, Map<K, Collection<E> > m , Class<? extends Collection> type) {
        Collection<E> s = m.get(k);
        if (s == null)
            m.put(k, s = Set.class.isAssignableFrom(type) ?  new HashSet<>() : new ArrayList<>());
        
        return s.add(e);
    }
    
    /**
     * insert a new element in a map of (sorted) sets of corresponding type elements; if the
     * given key is not yet mapped a new set is associated to it
     * @param <K> the type of map's keys
     * @param <E> the type of map's set values
     * @param k a key
     * @param m a map
     * @param e a value
     * @param comp a (possibly null) comparator of @code {E} elements
     * @return <tt>true</tt> if and only if the elementy is added
     */
    public static <K,E>  boolean addOrdElem(K k, E e, Map<K, SortedSet<E> > m, Comparator<? super E> comp) {
        SortedSet<E> s = m.get(k);
        if (s == null)
            m.put(k, s = new TreeSet<>(comp));
        
        return s.add(e);
    }
	
	    
    /**
     * performs the Cartesian product among a collection of sets of terms of a given type
     * @param <E> the terms' type
     * @param listofset the list of sets
     * @return a set of list corresponding to the product; a singleton containing
     * an empty list (by convention) if either <tt>sets</tt> or any set is empty
     */
    public static <E> Set<Collection<E>> cartesianProd(final Collection<? extends Set<? extends E>> listofset) {
        final boolean isalist = listofset instanceof List;
        final Set<Collection<E>> init = Collections.singleton(isalist ?
                Collections.emptyList() : Collections.emptySet());
    	Set<Collection<E>> res = init; //at the beginning res contains just an empty list
    	
        for (Set<? extends E> iset : listofset) {
            if ( iset.isEmpty() )
                return init;

            HashSet<Collection<E>> partial_res = new HashSet<>();
            for (Collection<E> y : res) {
                iset.forEach( x -> { 
                    Collection<E> temp = isalist ? new ArrayList<>(y) : new HashSet<>(y);
                    temp.add(x);
                    partial_res.add(temp);
                });	
            }
            res = partial_res;
        }
        
        return res;
    }
 
 //stackoverflow
 public static Set<Set<Object>> cartesianProduct(Set<?>... sets) {
    if (sets.length < 2)
        throw new IllegalArgumentException(
                "Can't have a product of fewer than two sets (got " +
                sets.length + ")");

    return _cartesianProduct(0, sets);
}

private static Set<Set<Object>> _cartesianProduct(int index, Set<?>... sets) {
    Set<Set<Object>> ret = new HashSet<>();
    if (index == sets.length) {
        ret.add(new HashSet<>());
    } else {
        sets[index].forEach(obj -> {
            for (Set<Object> set : _cartesianProduct(index+1, sets)) {
                set.add(obj);
                ret.add(set);
            }
        });
    }
    return ret;
}

/**
 * performs the cartesian product between the elements of two collections, on
 * which a given operator applies
 * @param <E> the collections' elements's type
 * @param c1 the 1st collection
 * @param c2 the 2nd collection
 * @param op the specified operator
 * @return an <code>HashSet</code> which results from applying the operator to
 * all pairs of collection elements
 */
public static <E> HashSet<E> binaryProduct(Collection<? extends E> c1, Collection<? extends E> c2, BiFunction<E,E,E> op) {
    //System.out.println(c1.size()+ " "+c2.size()); //debug
    HashSet<E> res = new HashSet<>();
    c1.forEach(x -> {
        c2.forEach(y -> {
            res.add(op.apply(x,y));
        });
       });
    //System.out.println("done"); //debug
    
    return res;
}

       
    /**
     * return the value modulo-n of a specified integer value
     * @param val the specfied integer
     * @param n the modulo size (assumed &gt; 0)
     * @return val modulo n (e.g., if val == -4 and n ==3, it returns 2)
     */
    public static int valueModN(int val, int n) {
        int new_val = Math.abs(val) % n; // new_val >= 0
        
        return new_val > 0 && val < 0 ? n - new_val : new_val;
    }

    /**
     * computes the integers between a lower (included) and an upper (excluded) bound
     * (both assumed strictly positive and congruent) that are missing in a given collection
     * of (assumed positive) values
     * @param c a collection of positive integers
     * @param lower a (positive) lower bound
     * @param upper a (positive) upper bound
     * @return the set of values between the specified bounds that are missing in the collection
     */
    public static HashSet<Integer> missing (Set<? extends Integer> c, int lower, int upper) {
        HashSet<Integer> numbers = new HashSet<>();
        for (int i = lower; i < upper; i++) 
            numbers.add(i);
        
        numbers.removeAll(c);
        return numbers;
    }    
    
    /**
     * given an (assumed) ordered collection of elements provided with a function mapping to an integer
     * (on which the order is defined), finds out the first occurrence of a missing "succ" value in the collection,
     * starting from a lower bound, and considering all successors;
     * helpful to find the missing value in the sequence 0,n-1 or 1,n (assuming that the list holds the remaining n-1 values)
     * @param <E> the type of collection's elements
     * @param c an (ordered) collection of symbols
     * @param lower a starting value
     * @param f the function associated with each element
     * @return the first occorrence of a missing succ value of the sequence lower, lower+1, ...;
     * lower + c.size() if the list contains all the above sequence of consecutive values
     */
    public static <E> int missingNext (Collection<? extends E> c, Function<E,Integer> f, int lower) {
        for (E x : c) 
            if (f.apply(x) == lower)
                lower++;
            else
                break;
        
        return lower;
    }
    
    /**
     * computes the set of integer values in an interval (whose bounds are given)
     * which are not included in a given set (of integers)
     * @param l the lower bound
     * @param u the upper bound
     * @param set the set of values
     * @return the set of values in [a,u] which are not contained in @code {set}
     */
    public static Set<Integer> missing (int l, final int u, Set<? extends Integer> set) {
        Set<Integer> res = new HashSet<>();
        for (; l <= u; ++l) //all the values in [l,u]
            if (!set.contains(l))
                res.add(l);
        
        return res;
    }
    
    /**
     * verifies that all the elements of a collection satisfy (share) a property,
     * checked through a pair-wise comparison - operates in linear time
     * @param <E> the collection's type
     * @param c a collection of elements
     * @param check the comparison function
     * @param textprop a text function describing the property (for log purposes)
     * @param ex the exception that should be raised
     * @return <tt>true</tt> if and only if the collection's element share the property;
     * raises an exception of the specified type (with an error message) if argument ex is other than
     * <tt>null</tt>
     */
    public static <E> boolean checkProperty(Collection<? extends E> c, BiFunction<E, E, Boolean> check, Function<E,String> textprop, Class<? extends RuntimeException> ex ) {
        if (!c.isEmpty()) {
            Iterator<? extends E> ite = c.iterator();
            E first = ite.next(), next;
            while ( ite.hasNext() ) 
                if ( ! check.apply(first, next = ite.next()) ) 
                    if (ex != null) {
                        try {
                            System.err.println(first + " and " + next + " don't match the samee property:\n" + textprop.apply(first)+ "\nvs\n" + textprop.apply(next)); //error message
                            throw ex.newInstance();
                        } 
                        catch (InstantiationException | IllegalAccessException ex1) {
                            throw new Error(ex1);
                        }
                    } 
                    else 
                        return false;
        }
        return true;
    }
    
    /**
     * invert a given map
     * @param <E> the type of values
     * @param <K> the type of keys
     * @param m the map
     * @return the "inverted" map
     */
    public static <E,K> Map<E,Set<K>> invert (Map<? extends K, ? extends E> m) {
        Map<E,Set<K>> im = new HashMap<>(); 
        m.entrySet().forEach( e -> { Util.addElem(e.getValue() , e.getKey(), im); });
        
        return im;
    }
    /**
     * computes the size of values of a map to sets
     * @param <E> the map's key type
     * @param <K> the map's value type
     * @param m a map to sets
     * @return the obverall size of values in the map 
     */
    public static <E,K>  int valuesSize(Map<? extends E,? extends Set<? extends K>> m) {
        int size = 0;
        
        return m.values().stream().map( x -> x.size()).reduce(size, Integer::sum);
    }
    
    /**
     * @param <E> the type of values
     * @param <K> the type of keys
     * @param m the map
     * @return <code>true</code> iff the map is injective
     */
    public static <K, E> boolean injective (Map<? extends K, ? extends E> m) {
         HashSet<E> set = new HashSet<>();
         
         return m.values().stream().allMatch( x -> set.add(x));
    }
    
    /**
     * checks whether a non-empty map is a constant "surjective" w.r.t. a give set
     * more specifically, checks that the map's key set contains the specified set
     * and all the set's keys map to the same value
     * @param <K> the map's key type
     * @param <E> the map's value type
     * @param m the map
     * @param set a set of keys
     * @return the single value present on the map, if there is just one, and the map's key
     * set coincides with the specified set; <code>null</code> otherwise
     */
    public static <K,E> E isConstantSurjective (Map<? extends K, ? extends E> m, Set<? extends K> set) {
       Collection<? extends E> vals;
       m.keySet().retainAll(set);
       
       return set.size() == m.size() && new HashSet<>(vals = m.values()).size() == 1 ? vals.iterator().next() : null;
    }
     
    
    /**
     * creates a new hashset from a given collection, or simply returns the set passed as an argument, depending on
     * whether the corresponding argument is <code>null</code> or not
     * @param <E> the type of set's elements
     * @param set a set
     * @param c a collection
     * @return <code>set</code>, if not null, otherwise the reference to a new set built from the given collection
     */
    public static <E> Set<E> lightCopy(Set<E> set, Collection<? extends E> c) {
        return set == null ? new HashSet<>(c) : set;
    }
    
    /**
     * version working on list
     * @param <E> the type of set's elements
     * @param list a list
     * @param c a collection
     * @return <code>list</code>, if not null, otherwise the reference to a new list built from the collection
     */
    public static <E> List<E> lightCopy(List<E> list, Collection<? extends E> c) {
        return list == null ? new ArrayList<>(c) : list;
    }
    
    /**
     * does a copy of a collection, preserving the collection's type (@code{Set}, code{List})
     * @param <E> collection's elements type
     * @param c a collection
     * @return a copy of @code{c}, of the same type
     */
    public static <E> Collection<E> copy(Collection<? extends E> c) {
        return c instanceof Set<?> ? new HashSet<>(c) : new ArrayList<>( c);
    }
    
    
    /**
     * checks whether the k-th bit of an integer is set
     * @param x an integer 
     * @param k a bit position 
     * @return <tt>true</tt> iff the k-th bit is set
     */
    public static boolean checkBit(int x, int k) {
        return (x & 1 << k) != 0;
    }
    
    
//debugging methods
    
    private static final HashSet<String> VALUES = new HashSet<>();
    
    /**
     * Debug method
     * @param <E>
     * @param e
     * @param m
     */
    static  <E extends Expression> void checkBuilderOneStep (E e, Map<ComplexKey,E> m ) {
        String descr = e.toStringDetailed();
        if ( !VALUES.add(descr) ) {
            System.out.println("WARNING!\nFOUND DUPLICATE ITEMS (SAME TEXT REPESENTATIONS) IN THE HASHMAP:");
            for (Map.Entry<ComplexKey, E> x : m.entrySet()) {
                if (descr.equals(x.getValue().toStringDetailed()))
                    System.out.println(x.getKey() + "("+x.getKey().hashCodes()+") -> "+x.getValue());
            }
            
            throw new RuntimeException("building failed\n");
        }
    }    

  public static int count;
  
}
