package guard;

import java.util.*;
import classfunction.*;
import color.ColorClass;
import expr.*;
import util.ComplexKey;
import util.Util;

/**
 * this class represents predicates checking for the membership of a
 * variable (i.e., a projection function) to a given subclass
 * @author Lorenzo Capra
 */
public final class Membership extends ElementaryGuard  {
    
    private static final Map< ComplexKey, Membership> VALUES = new HashMap<>();
    //caching
    public Map<ColorClass, Map<Boolean, Set<Membership>>> membMap;

    /**
     * given a list of membership clauses, extracts a map from symbols to the subclasses
     * (possibly more than one) they are associated to
     * @param mlist a membership list
     * @return a corresponding map symbols &rarr; sets of subclasses
     */
    public static Map<Projection, Set<Subcl>> mapSymbols(Collection<? extends Membership> mlist) {
        Map<Projection, Set<Subcl>> map = new HashMap<>();
        mlist.forEach( m -> { Util.addElem(m.getArg1(), m.getArg2(), map); });
        
        return map;
    }

    /**
     * given a set of membership clauses,
     * extracts a map from symbols to corresponding subclasses
     * @param mset a membership list
     * @return a corresponding map symbols &rarr; subclasses
     * @throws IllegalArgumentException if the same symbols occurs more than once
     */
    public static Map<Projection, Subcl> mapSymbolsNoRep(Set<? extends Membership> mset) {
        Map<Projection, Subcl> map = new HashMap<>();
        mset.stream().filter(m -> (map.putIfAbsent(m.getArg1(), m.getArg2()) != null)).forEachOrdered(_item -> {
            throw new IllegalArgumentException("many symbols refer to the same subclass: "+mset);
        });
        
        return map;
    }
    
    /**
    basic constructor: builds a membership clause
    @throws IllegalDomain
    */
    private Membership (Projection p, Subcl s, boolean sign, Domain dom)  {
        super(p, s, sign, dom);
    }
    
    /**
     * base builder method: if the sign is "not in" and there are only two subclasses
     * invert the sign and replace the subclass with the other one
     * @param p1 a projection
     * @param sc a subclass
     * @param sign the "in" sign
     * @param dom the guard's domain
     * @return a Membership clause
     * @throws IllegalDomain if the two functions don't have the same colour
     */
    public static Membership build(Projection p1, Subcl sc, boolean sign, Domain dom) {
        ColorClass cc = checkDomain(p1,sc,dom);
        checkIndex(p1, dom);
        if (! sign && cc.subclasses() == 2) { //  there are just two subclasses: not in -> in
            sc = Subcl.factory(sc.index() == 1 ? 2 : 1, cc);
            sign = true; 
        }
        ComplexKey k = new ComplexKey(p1, sc, sign, dom);
        Membership m = VALUES.get(k);
        if ( m == null) 
            VALUES.put(k,  m = new Membership(p1, sc, sign, dom)) ;
        
        return m;
    }
    
    /**
     * overloaded builder method: creates an "in" Membership clause
     * @param p1 a projection
     * @param sc a subclass
     * @param dom the guard's domain
     * @return a "in" Membership clause
    */
    public static Membership build(Projection p1, Subcl sc, Domain dom)  {
        return build(p1, sc, true, dom);
    }
    
    /**
     * creates a Membership for a given subclass index
     * @param p1 a projection
     * @param index a subclass index
     * @param sign the predicate's sign (in/notin)
     * @param dom the guard's domain
     * @return a Membership clause
     */
    public static Membership build(Projection p1, int index, boolean sign, Domain dom)  {
        return build(p1, Subcl.factory(index, p1.getSort()), sign, dom);
    }
    
    //TENUTO x COMPATIBILITA CON VECCHIA VERSIONE -Si PUO' ELIMINARE
    /**
     * main factory method of Membership
     * @param p1 a given projection
     * @param idxset a static subclass index set
     * @param sign in or not in flag
     * @param dom a given colour domain
     * @return a reference to either a Membership clause or a logical constant, depending
     * on <code>idxset</code> composition
     * @throws IllegalArgumentException if either the projection's color class is not split,
     * or the specified subclass indices are not coherent
     */
    public static Membership factory(Projection p1, Set<? extends Integer> idxset, boolean sign, Domain dom)  {
        return build (p1, idxset.iterator().next(), sign, dom);
    }
            
    @Override
    public Subcl getArg2() {
        return (Subcl) super.getArg2();
    }
    
    /**
     * 
     * @return the index of the associated subclass 
     */
    public int index () {
        return getArg2().index();
    }
    /**
     * the method is overridden in an optimized way, so that
     * a membership guard is already set simplified
     * @param simp the simplified flag (here ignored)
     */
     @Override
     public final void setSimplified(boolean simp) {
         super.setSimplified(true);
     }
    
    @Override
    public int splitDelim() {
        return getArg2().splitDelim();
    }
    
    @Override
    public String opSymb() {
        //return "\u2208" ;
        return sign() ? " in " : " !in ";
    }

    @Override
    public Membership opposite() {
       return build (getArg1(), getArg2(), !sign(), getDomain()); 
    }
    
    @Override
    public Set<Integer> indexSet() {
        return getArg1().indexSet();
    }
    
    @Override
    public String toString() {
        return getArg1()+ opSymb()+ getSort().name()+ "{"+ getArg2().index() + "}" /*+ hashCode()*/;
    }

    @Override
    public Intersection toSetfunction(Projection f) {
       return Objects.equals(f.getIndex(), firstIndex()) ? (Intersection)Intersection.factory(f, sign() ? getArg2() : getArg2().opposite()) : null; 
    }
    
   
    @Override
    public boolean isMembership() {
        return sign() ;
    }
    
    
    @Override
    public boolean isNotinMembership() {
        return ! sign() ;
    }
    

    @Override
    public Membership replace(Equality eq) {
        Projection p = getArg1().replace(eq);
        
        return p == getArg1() ? this : Membership.build( p, getArg2(), sign(), getDomain() ) ;
    }

    
    @Override
    public Guard clone(Map<Sort, Sort> split_map) {
        Domain nd = getDomain().setSupport(split_map);
        ColorClass cc = getSort(), n_cc = (ColorClass)split_map.get(cc);
        if (n_cc == null) {
            return clone(nd);
        } else {
            return Membership.build(getArg1().copy(n_cc), getArg2().copy(n_cc), sign(), nd);
        }
    }
    
    @Override
    public Membership clone(Domain new_dom)  {
        return build(getArg1(), getArg2(), sign(), new_dom);
    }
    
    //new
    @Override
    public Map<ColorClass, Map<Boolean, Set<Membership>>> membMap() {
        if (this.membMap == null){
            this.membMap = Collections.singletonMap(getSort(), Collections.singletonMap(sign(), Collections.singleton(this)));
        }
        return this.membMap; 
    }

    
}
