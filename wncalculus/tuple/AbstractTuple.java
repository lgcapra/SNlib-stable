package wncalculus.tuple;

import java.util.*;
import wncalculus.classfunction.ClassFunction;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.*;
import wncalculus.util.Util;

/**
 * this class represents an arbitrary tuple of class-functions, that may be
 * both boolean functions or linear combinations
 * @author lorenzo capra
 * @param <E> the tuple's elements' type (either BoolFunction or BagFunction)
 */
public abstract class AbstractTuple<E extends ClassFunction> implements Expression, Transposable  {
    //we assume that tuple's components color-classes are consistent: c1.equals(c2) <-> c1.compareTo(c2) (i.e. different colors must have different names)
    private  final    SortedMap<ColorClass , List<? extends E>> hom_parts ; // the map between colors and homogenous sub-tuples composing this tuple 
    private  final    Guard   filter, guard; 
    //cache
    private String   str; // caching (to get efficiency when ordering)
    private List<E>  components; //caching
    private boolean  simplified;
        
    /* checks for the tuple's parameters (the filter is trivial) */
    private void checkPar(final SortedMap<ColorClass, List<? extends E>> m, final Guard g) {
        String msg = "";
       if (g == null)
            msg += "the tuple's guard is null! cannot create it; ";
       if (m == null)
            msg +=  "the tuple's component map is null! cannot create it!";
       if (!msg.isEmpty())
            throw new IllegalArgumentException(msg);
    }
        
    /**
     * base constructor (the others mostly build on it): creates a tuple from a map of colors to corresponding class-function lists;
     * only the relative order of functions of the same color actually matters, the order among different colours (based non names)
     * the tuple's codomain is inferred, the tuple's guard (and consequently the tuple's domain)
     * must be specified; projection or_indexes occurring on the tuple must correctly range over the corresponding
     * colour bounds of the domain, otherwise an exception is raised
     * creates an unmodifiable data structures so any attempt to modify the tuple will raise an exception 
     * @param f the tuple filter
     * @param g the tuple guard
     * @param m the colors-subtuples map  
     * @param check domain-checkPar flag
     * @throws IllegalDomain if the checkPar flag is set and there are some incongruences on the domains
     * @throws IllegalArgumentException if some argument is <code>null</code>
     */
    public AbstractTuple (final Guard f, final SortedMap<ColorClass, List<? extends E>> m, final Guard g, /*final*/ boolean check) {
        //checkPar(f, m, g);
        check = true;
        HashMap<ColorClass, Integer> tcd = buildTupleCodom(m, check ? g.getDomain() : null);
        if (f==null)
            this.filter = True.getInstance(new Domain(tcd)); //messo per compatibilità con cli ...
        else if (check && !tcd .equals( f.getDomain().asMap()))  // the tuple's codomain and the filter domain must coincide
            throw new IllegalDomain (tcd+" and "+f.getDomain()+ ": derived and filter's domains incompatible!\nf: "+f); 
        else
            this.filter = f;
        
        this.guard  = g;
        this.hom_parts = Collections.unmodifiableSortedMap(m);
    }
    
    
    /**
     * builds a tuple from a map of colors to corresponding class-function lists with a default guard 
     * @param f the tuple's filter
     * @param m the map
     * @param g the tuple' domain
     */
    public AbstractTuple (final Guard f, final SortedMap<ColorClass, List<? extends E>> m, final Domain d, final boolean check) {
      this(f, m, True.getInstance(d), check);
    }
    
    
   /**
     * this constructor should be used from outside the library, at expression parsing time:
     * builds a tuple from a list of class-functions; functions are grouped by colour, only the relative
     * order of functions of the same color actually matters, the order among different colours (based non names)
     * the tuple's codomain is inferred: note that the tuple's guard (and consequently the tuple's domain)
     * must be specified; projection or_indexes occurring on the tuple must correctly range over the corresponding
     * colour bounds of the domain, otherwise an exception is raised
     * creates an unmodifiable data structures so any attempt to modify the tuple will raise an exception 
     * @param f the tuple's filter
     * @param g the tuple's guard
     * @param l the list of class-functions 
     * @param check domain checkPar flag
     * @throws IllegalDomain if the checkPar flag is set and there are some incongruences on the domains
     * @throws IllegalArgumentException if some argument is <code>null</code>
     */
    public AbstractTuple (final Guard f, final List<? extends E> l, Guard g, final boolean check) { 
        this(f, Util.sortedmapFeatureToList(l, ClassFunction::getSort), g, true);   
    }
    
    /**
     * builds a tuple with a default guard from a list of class-functions
     * 
     * @param f the tuple's filter
     * @param d the tuple's domain
     * @param l the list of class-functions 
     * @param check domain checkPar flag 
     */
    public AbstractTuple (final Guard f, final List<? extends E> l, Domain d, final boolean check) { 
        this(f, l, True.getInstance(d), check);   
    }
    
    /**
     * base constructor (2) : creates a tuple from a map of colors to corresponding class-function lists,
     * with a default filter (i.e., an ordinary tuple of functions);
     * @param g the tuple guard
     * @param m the colors-subtuples map  
     * @param check domain-checkPar flag
     * @throws IllegalDomain if the checkPar flag is set and there are some incongruences on the domains
     * @throws IllegalArgumentException if some argument is <code>null</code>
     */
    public AbstractTuple (final SortedMap<ColorClass, List<? extends E>> m, final Guard g, /*final*/ boolean check) {
        checkPar(m,g);
        check = true;
        var tcd = buildTupleCodom(m, check ? g.getDomain() : null); 
        this.filter =  True.getInstance(new Domain(tcd));
        this.guard  =  g;
        this.hom_parts = Collections.unmodifiableSortedMap(m);
    }
    
    /**
     * creates a tuple from a map of colors to corresponding class-function lists,
     * with a default filter (i.e., an ordinary tuple of functions) and a default guard, with the specified domain;
     */ 
    public AbstractTuple (final SortedMap<ColorClass, List<? extends E>> m, final Domain d, final boolean check) {
        this(m, True.getInstance(d), check);
    }
    
    /**
     * creates a tuple from a list of class-functions, with a default filter 
     * and a default guard, with the specified domain;
     */
    public AbstractTuple(List<? extends E> l, final Domain d, boolean check) {
        this(Util.sortedmapFeatureToList(l, ClassFunction::getSort), d, check);
    }
    
    /**
     * creates a tuple from a list of class-functions, with a default filter 
     * and a given guard;
     */
    public AbstractTuple(List<? extends E> l, final Guard g, boolean check) {
        this(Util.sortedmapFeatureToList(l, ClassFunction::getSort), g, check);
    }
    
     
     /**
      * infers the tuple's codomain from a map colors-(sub)tuples possibly checking the map againts a color domain
      * representing the entire tuple's domain
      * if the specified tuple's domain is <code>null</code> skips the checkPar
      * @param <E> the type of tuple components
      * @param map the map
      * @param dom the tuple'd domain
      * @return the tuple's codomain (as a map)
      * @throws IllegalDomain if some variable index doesn't match the specified (non-null) domain
      */
     static <E extends ClassFunction> HashMap<ColorClass, Integer> buildTupleCodom (final SortedMap<ColorClass , List<? extends E>> map, final Domain dom) {
            /*Sorted*/HashMap<ColorClass, Integer> tcd = new /*Tree*/HashMap<>(); // the tcd's structure
            map.entrySet().forEach((var entry) -> {
                var st = entry.getValue();
                if (st.isEmpty()) {
                    throw new IllegalDomain();
                }
                else {
                    var cc = entry.getKey();
                    if (dom != null) {
                        Set<? extends Integer> idxset = ClassFunction.indexSet(st); // the projection or_index set of st
                        if (! idxset.isEmpty() && Collections.max(idxset)  > dom.mult(cc) )
                            throw new IllegalDomain("failed tuple's building:\nincorrect domain specification (projection index outside the range of color "+cc+
                                    "\ntuple components: "+st+"), domain: "+dom);
                    }
                    tcd.put(cc, st.size());
                }
           });
        return tcd; 
     }
     
    
    /**
     * builder method: build a tuple with the same components as <tt>this</tt>
     * @param filter a guard representing a filter
     * @param guard a guard
     * @return a tuple with the same components as <tt>this</tt> and the specified
     * filter, guard, domain
     */
    public abstract <T extends AbstractTuple> T build (final Guard filter, final Guard guard);    
    
    
    /**
     *
     * @return the (possibly null, i.e., trivial) tuple's guard
     */
    public final Guard guard() {
        return this.guard;
    }
    
    /**
     *
     * @return the (possibly null, i.e., trivial) tuple's filter
     */
    public final Guard filter() {
        return this.filter;
    }
    
    @Override
    public final Domain getDomain() {
        return this.guard.getDomain();
    }

    @Override
    public final Domain getCodomain() {
        return this.filter.getDomain();
    }
    
    
    /** 
     * @return <tt>true</tt> if and only if the filter and the guard are
     * the constant true
     */
    public final boolean hasTrivialFilters() {
        return this.guard.isTrivial() && this.filter.isTrivial();
    }
    
    /**
     * @return the unique color class, if the tuple is 1-sorted; <code>null</code> otherwise
     */
    public final ColorClass getSort() {
        return this.hom_parts.size() == 1 ? this.hom_parts.keySet().iterator().next() : null;           
    }
    
    /**
     * @return a view of the color-homogeneous parts of this tuple 
     */
    public final SortedMap<ColorClass, List<? extends E>> getHomSubTuples() {
        return this.hom_parts;
    }
    
    /**
     * @param cc a color class
     * @return (a read-only) sub-list of components of the specified color class;
     * an empty list if there is no such a sub-list
     */
    public final List<? extends E> getHomSubTuple(ColorClass cc) {
        return this.hom_parts.getOrDefault(cc, Collections.EMPTY_LIST);
    }
   
    /**
     * @return (an unmodifiable view of) the tuple components;
     * the list is ordered w.r.t. colour-classes
     */
    public final List<E> getComponents() {
        if (this.components == null) {
            var c = getSort();
            if (c != null){
                this.components = (List<E>) this.hom_parts.get(c);
            }
            else {
                List<E> mycomps = new ArrayList<>();
                this.hom_parts.entrySet().forEach(x -> {mycomps.addAll(x.getValue()); });
                this.components = Collections.unmodifiableList(mycomps);
            }
        }
        return this.components;
    }
            
    /**
     @param i a relative position
     @param cc a color class
     @return the i-th component of the homogenous sub-tuple of the specified color
     @throws IndexOutOfBoundsException if the position is out of the correct range
     */
    public final E getComponent(int i, ColorClass cc) {
        return getHomSubTuple(cc).get(i - 1);
    }     
        
    @Override
    public final boolean isConstant() {
        return ClassFunction.indexSet(getComponents()).isEmpty() && this.guard.isConstant();
     }
        
    /**
     * 
     * @return a copy of <code>this</code> tuple with a trivial filter,
     * <code>this</code> if the filter is trivial
     */
    public final <T extends AbstractTuple> T withoutFilter () {
        if (this.filter.isTrivial()) {
            return cast();
        }
        else {
            T copy = build (True.getInstance(getCodomain()), this.guard);
            //copy.setSimplified( simplified() );
            return copy;
        }
    }
    /**
     * 
     * @return a copy of <code>this</code> tuple with a trivial guard,
     * or <code>this</code> if the guard is trivial
     */
    public final <T extends AbstractTuple> T withoutGuard() {
        if (this.guard.isTrivial()) { 
            return cast();
        }
        else {
            T copy = build (this.filter, True.getInstance(getDomain()));
            //copy.setSimplified( simplified() );
            return copy;
        }
    }
    
    /**
     * applies a filter to <tt>this</tt> tuple, by joining with the existing onr
     * @param f a filter
     * @return a tuple with the new filter; <tt>this</tt> tuple if the new filter coincides with the current one
     */
    public final <T extends AbstractTuple> T joinFilter(Guard f) {
        return build(And.factory(this.filter, f), this.guard) ;
        
    }
    
    public final <T extends AbstractTuple> T joinGuard(Guard g) {
        return build(this.filter, And.factory(this.guard, g));
    }
    
    /**
     * eficiently clones the tuple's components
     * @param split_map a map from old and new sorts
     * @return a (sorted) map containing the cloned sub-tupls
     */
    protected final SortedMap<ColorClass, List<? extends E> > cloneComps (final  Map<Sort, Sort> split_map) {
        SortedMap<ColorClass, List<? extends E> > m = new TreeMap<>();
        this.hom_parts.entrySet().forEach( (var x) -> {
            ColorClass cc = x.getKey(), n_cc = (ColorClass) split_map.get(cc);
            if (n_cc == null) { // color cc not mapped 
                m.put(cc, x.getValue());
            }
            else {
                m.put(n_cc, (List<? extends E>) ClassFunction.copy(x.getValue(),n_cc));
            }
        });
        return m;
    }
    
    
    @Override
    public final String toString () {
        if (this.str == null) {
            var t = "<";
            t = getComponents().stream().map( x -> x.toString() + ',').reduce(t, String::concat);
            t = t.substring(0,t.length()-1)+'>';
            this.str = (filter.isTrivial() ? "" : "[" + filter + ']')  + t + (guard.isTrivial() ? "" : "[" +guard + ']');
        }
        return this.str;
    }
    
    @Override
    public final boolean equals(Object o) {
        var res = super.equals(o);
        if (! res && o != null && getClass().equals( o.getClass() ) )  {
            var t = (AbstractTuple)o;
            res =  t.guard.equals(this.guard) && t.filter.equals(this.filter) && Objects.equals(t.hom_parts,this.hom_parts);
        }
        return res;
    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.hom_parts);
        hash = 97 * hash + Objects.hashCode(this.filter);
        hash = 97 * hash + Objects.hashCode(this.guard);
        return hash;
    }
    
    @Override
    public final boolean simplified() {
        return this.simplified; 
    }

    @Override
    public final void setSimplified(boolean simplified) {
       this.simplified = simplified;
    }
    
    @Override
    public final AbstractTuple<E> clone(Domain newdom) {
        return build(filter.clone(newdom), guard.clone(newdom)) ; 
    }
        
}
