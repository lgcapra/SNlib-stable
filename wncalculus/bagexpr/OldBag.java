//package bagexpr;
//
//import expr.ParametricExpr;
//
//import java.util.*;
//
//
///**
// * @author Lorenzo Capra
// * the abstract data type for a "generalised" Bag, i.e., a set whose elements have an associated
// * (signed) integer multiplicity
// * @param <E> the domain of the elements of the bag
// */
//public interface OldBag<E extends ParametricExpr> extends BagExpr<E>  {
//
//    static final String EMPTY = "<null>";
//    /**
//     * @return a map-view of this bag
//     */
//    Map<? extends E,Integer> asMap ();
//
//    /**
//     * @param e a given element
//     * @return the multiplicity of the element in the multiset; 0 if it is not present
//     */
//    default int mult(E e) {
//        Integer x = asMap().get(e);
//        return x != null ? x : 0;
//    }
//
//    /**
//     * @return the support of the multi-set, i.e., the set of its elements with multiplicity
//        different from zero; the set is backed by the bag
//    */
//    default Set<? extends E> support() {
//        return asMap().keySet();
//    }
//
//    @Override
//    default boolean isConstant() {
//        return support().stream().allMatch( x -> x.isConstant() );
//    }
//
//    /**
//     * @return the bag's support size
//     */
//    default int size() {
//        return asMap().size();
//    }
//
//    /**
//     * @return <code>true</code> if and only if the multi-set is build
//     */
//    default boolean isEmpty() {
//        return size() == 0;
//    }
//
//    /**
//     * @return the elements of this bag with coefficients greater than zero:
//     * <tt>null</tt> if, for any reasons, they cannot be computed
//     */
//    default Set<? extends E> properSupport() {
//        HashSet<E> pset = new HashSet<>();
//        asMap().entrySet().stream().filter(e -> ( e.getValue() > 0 )).forEachOrdered(e -> { pset.add(e.getKey()); });
//
//        return pset;
//    }
//
//    /**
//     *
//     * @return <code>true</code> if and only if <code>this</code> is a true bag
//     */
//    default boolean isProperBag () {
//        return isEmpty() || Collections.min(asMap().values()) > 0;
//    }
//
//
//    /**
//     * does the scalar product between <code>this</code> bag and a given coefficient
//     * @param coeff an integer coefficient
//     * @return a new bag obtained by multiplying <code>this</code>' coefficients by
//     * by <code>coeff</code> ; <code>this</code> if the bag is empty ot <code>coeff == 1</code>
//     */
//    default OldBag<E> scalarProd(int coeff) {
//        if ( isEmpty() || coeff == 1 )
//            return this;
//
//        HashMap<E,Integer> m = new HashMap<>();
//        asMap().forEach((key, value) -> m.put(key, coeff * value));
//
//        return build(m);
//    }
//
//    /**
//     * does the sum between <code>this</code> bag and a given bag
//     * @param b a bag to be summed with <code>this</code>
//     * @return a new bag obtained by summing <code>this</code> and <code>b</code>;
//     * <code>this</code> if <code>b</code> is empty
//     */
//    default OldBag<E> sum(OldBag<E> b) {
//        if (isEmpty())
//            return b;
//        if (b.isEmpty())
//            return this;
//
//        HashMap<E,Integer> m = new HashMap<>(asMap());
//        b.asMap().forEach((key, value) -> m.merge(key, value, Integer::sum));
//
//        return build(m);
//    }
//
//
//    default OldBag<E> clone() throws CloneNotSupportedException {
//         return build(asMap());
//    }
//
//    default OldBag<E> intersection(OldBag<E> b){
//        if (isEmpty())
//            return this;
//        if (b.isEmpty())
//            return b;
//
//        HashMap<E,Integer> m = new HashMap<>(asMap());
//        b.asMap().forEach((key, value) -> m.merge(key, value, Integer::min));
//
//        return build(m);
//    }
//}
