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

    //careful: we assume that the tuple's components color-classes are consistent: c1.equals(c2) <-> c1.compareTo(c2) (i.e. different colors must have different names)
    private          SortedMap<ColorClass , List<? extends E>> hom_parts ; // the map between colors and homogenous sub-tuples composing this tuple 
    private          Guard   filter,   guard; //null means true!
    private final    Domain  codomain, domain; // can be different from one another - the codomain is inferred by the tuple form
    
    private String   str; // caching (to get efficiency when ordering)
    private List</*? extends*/ E>  components; //caching
    private boolean  simplified;
        
    /**
     * base constructor (the only that should be used from outside the library at parsing time):
     * builds a tuple from a list of class-functions; functions are grouped by colour, only the relative
     * order of functions of the same color actually matters, the order among different colours (based non names)
     * just allows for an easier, "standard" (in some sense, canonical) descritpiom;
     * what is important, the tuple's codomain is inferred; either the tuple's domain or the tuple's guard must be specified,
     * otherwise a NullPointerException is raised; if the specified filter (guard) values(s) is (are) null, then
     * a trivial filter (guard) is (are) associated with this tuple
     * projection or_indexes occurring on the tuple must correctly range over the corresponding
     * colour bounds of the domain, otherwise an exception is raised
     * creates an unmodifiable data structures so any attempt to modify the tuple will raise an exception 
     * @param f the specified tuple'fc filter
     * @param g the specified tuple'fc guard
     * @param l the specified list of class-functions 
     * @param dom the tuple'fc domain
     * @param check domain check flag
     * @throws NullPointerException if both the given domain and guard are <tt>null</tt>
     * @throws IllegalDomain if the domain doesn't match the guard, or some variable index
     * is out of the tuple range
     */
    public AbstractTuple (Guard f, List<? extends E> l, Guard g, Domain dom, boolean check) {
        if ( dom == null && g == null )  
            throw new NullPointerException("either the domain or the guard of a tuple must be specified!");
        
        if (dom == null) 
            dom = g.getDomain();
        else if ( g != null && !dom.equals(g.getDomain()) ) 
            throw new IllegalDomain (" guard and tuple domains are incompatible!");
        // tuple's co-domain is derived        
        /*Sorted*/HashMap<ColorClass, Integer> cd = new /*Tree*/HashMap<>(); // the codomain's structure
        SortedMap<ColorClass , List<? extends E>> map = Util.sortedmapFeatureToList(l, ClassFunction::getSort);
        for (Map.Entry<ColorClass, List<? extends E> > entry  : map.entrySet()) {
            List<? extends E>    st = entry.getValue();
            ColorClass cc = entry.getKey();
            if (check) {
                Set<? extends Integer> idxset = ClassFunction.indexSet(st); // the projection or_index set of st
                if ( ! idxset.isEmpty() && Collections.max(idxset)  > dom.mult(cc) )
                    throw new IllegalDomain("failed tuple's building:\nincorrect domain specification: projection index outside the range of color "+cc+
                        "\ntuple components: "+st+", domain: "+dom);
            }
            cd.put(cc, st.size());
        } 
        
        this.codomain = new Domain(cd);
        if (check && f != null && ! this.codomain.equals(f.getDomain())) {
            System.err.println("lista funzioni: "+l+"\nfiltro: "+f.toStringDetailed());
            throw new IllegalDomain (this.codomain+" and "+f.getDomain()+ ": (co)domains are incompatible!");
        }
        
        this.hom_parts = Collections.unmodifiableSortedMap(map);
        this.domain = dom;
        setGuard(g);
        setFilter(f); 
    }
    
    /**
     * efficiently builds a tuple from a map of colors to corresponding class-function lists;
     * it doesn't perform any check and true copy, it provides an unmodifiable view of the passed map
     * @param filter the tuple'fc filter (<code>null</code> means TRUE)
     * @param codomain the tuple'fc codomain (necessary only if filter is <code>null</code>)
     * @param map the specified map
     * @param guard the tuple'fc guard (<code>null</code> means TRUE)
     * @param domain the tuple'fc domain (necessary only if guard is <code>null</code>)
     */
    public AbstractTuple (Guard filter, Domain codomain, SortedMap<ColorClass, List<? extends E>> map, Guard guard, Domain domain) {
        if (filter != null)
            this.codomain  = filter.getDomain();
        else if (codomain != null)
              this.codomain  = codomain ;
        else 
            throw new IllegalDomain("canno built a tuple with null codomain!");
        
        if (guard != null)
            this.domain  = guard.getDomain();
        else if (domain != null)
              this.domain  = domain ;
        else 
            throw new IllegalDomain("canno built a tuple with null domain!");
        
        setFilter(filter);
        setGuard(guard);
        this.hom_parts = map;
    }
    
    
    private void setFilter(Guard filter) {
        if (! (filter instanceof True) )
            this.filter =filter;
    }
    
    private void setGuard(Guard guard) {
        if (! ( guard instanceof True) )
            this.guard = guard;
    }    
    
    /**
     * builder method: build a tuple with the same components as <tt>this</tt>
     * @param filter a guard representing a filter
     * @param guard a guard
     * @param domain the tuple's domain
     * @return a tuple with the same components as <tt>this</tt> and the specified
     * filter, guard, domain
     */
    public abstract <T extends AbstractTuple> T build (Guard filter, Guard guard, Domain domain);    
    
    @Override
    public final Domain getDomain() {
        return this.domain;
    }

    @Override
    public final Domain getCodomain() {
        return this.codomain;
    }
    
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
    
    /** 
     * @return <tt>true</tt> if and only if the filter and the guard are
     * the constant true
     */
    public final boolean hasTrivialFilters() {
        return this.guard == null && this.filter == null;
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
    public final List</*? extends*/ E> getComponents() {
        if (this.components == null) {
            ColorClass c = getSort();
            if (c != null) 
                this.components = (List<E>) this.hom_parts.get(c);
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
        return ClassFunction.indexSet(getComponents()).isEmpty() && (this.guard == null || this.guard.isConstant());
     }
        
    /**
     * 
     * @return a copy of <code>this</code> tuple with a trivial filter,
     * <code>this</code> if the filter is trivial
     */
    public final <T extends AbstractTuple> T withoutFilter () {
        if (this.filter == null)
            return cast();
        
        T copy = build (null, this.guard, this.domain);
        copy.setSimplified( simplified() );
        
        return copy;
    }
    /**
     * 
     * @return a copy of <code>this</code> tuple with a trivial guard,
     * or <code>this</code> if the guard is trivial
     */
    public final <T extends AbstractTuple> T withoutGuard() {
        if (this.guard == null)
            return cast();
        
        T copy = build (this.filter, null, this.domain);
        copy.setSimplified( simplified() );
        
        return copy;
    }
    
    /**
     * applies a filter to <tt>this</tt> tuple, by joining with the existing onr
     * @param f a filter
     * @return a tuple with the new filter; <tt>this</tt> tuple if the new filter coincides with the current one
     */
    public final <T extends AbstractTuple> T joinFilter(Guard f) {
        Guard  nf = join(this.filter, f);
        return nf != this.filter ? build(nf, this.guard, this.domain) : cast();
        
    }
    
    public final <T extends AbstractTuple> T joinGuard(Guard g) {
        Guard  ng = join(this.guard, g);
        return ng != this.guard ? build(this.filter, ng, this.domain) : cast();
    }
    
    /**
     * efficient overriding of the single-argument clone methods
     * @param nd the tuple's new domain
     * @return a copy of <tt>this</tt> tuple with new domain, assumed compliant with ("including")
     * the current domain
     */
    @Override
    public final AbstractTuple clone(Domain nd) {
        return nd.equals(getDomain()) ? this : build (filter(), guard() != null ? guard().clone(nd) : null, nd);
    }
    
    @Override
    public final String toString () {
        if (this.str == null) {
            String t = "<";
            t = getComponents().stream().map( x -> x.toString() + ',').reduce(t, String::concat);
            t = t.substring(0,t.length()-1)+'>';
            this.str = (filter == null ? "" : "[" + filter + ']')  + t + (guard == null ? "" : "[" +guard + ']');
        }
        
        return this.str;
    }
    
    @Override
    public final boolean equals(Object o) {
        boolean res = super.equals(o);
        if (! res && o != null && getClass().equals( o.getClass() ) )  {
            AbstractTuple t = (AbstractTuple)o;
            res =   Objects.equals(t.guard,this.guard) && Objects.equals(t.filter,this.filter) && Objects.equals(t.hom_parts,this.hom_parts);
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
    
    /**performs the "and" between guards knowing that eg == null means eg == true
     * @param p1 a guard
     * @param p2 another guard
     * @return the "AND" between guards
    */
    public static Guard join(Guard p1, Guard p2) {
        if (p1 == null)
            return p2;
        
        if (p2 == null)
            return p1;
        
        return And.factory(p1,p2);
    }
    
    /**
     * performs the "difference" between guards knowing that eg == null means eg == true
 p2 is assumed other than true (null)
     * @param p1 a guard
     * @param p2 another guard
     * @return the difference between guards
    */
    public static Guard subtr (Guard p1, Guard p2) {
        return p1 == null ?  Neg.factory(p2) : And.factory(p1, Neg.factory(p2));
    }
    
    /**
     * performs the "or" between guards knowing that eg == null means eg == true
     * @param p1 a guard
     * @param p2 another guard
     * @return the "OR" between guards 
    */
    public static Guard disjoin(Guard p1, Guard p2) {
        return p1 == null || p2 == null ? null : Or.factory(false, p1,p2);
    }
    
    
}
