package guard;

import java.util.*;
import classfunction.Projection;
import static classfunction.Projection.*;
import color.ColorClass;
import expr.*;
import logexpr.LogicalExprs;
import util.Util;
import util.Pair;

/**
 * this class represents the super-type for n-ary boolean op.s
 * @author Lorenzo Capra
 */
public abstract class NaryGuardOperator extends Guard implements N_aryOp<Guard>  {
    
    private final Set<Guard> args;   // the operand's list
    
    // caching
    private Map<ColorClass, Map<Boolean, SortedSet<Equality>> >   eq_map;
    private Map<ColorClass, Map<Boolean, Set<Membership>> >       memb_map;
    private final boolean congrsign = this instanceof And ; // the "congruent sign"
    private final boolean simple ;
    
    /**
    * basic constructor: builds a n-ary operator from a set of guards
     * @param guards a set of guards (the operands)
     * @param check check-domain flag
    * @throws IllegalDomain if the guards' domains are different
    */
    protected NaryGuardOperator(Set<? extends Guard> guards, boolean check) {
        if (check)
            Expressions.checkDomain(guards);
        this.args   =  Collections.unmodifiableSet(guards);
        this.simple =  LogicalExprs.simple(this.args);
    }
    
    /**
     * calculates the map of equalities included in <tt>this</tt> guard, grouped
     * by color and sign
     * @return the (possibly empty) map of equalities included in <tt>this</tt> guard, grouped
     * by color (first) and sign; if the guard is not a simple form then returns an empty map 
     */
    @Override
    public final Map<ColorClass, Map <Boolean, SortedSet<Equality> > > equalityMap() {
        if ( this.eq_map == null) { 
            this.eq_map = simple() ?  Collections.unmodifiableMap( setEqualityMap() ) : Collections.EMPTY_MAP;
        }
        
        return this.eq_map;
    }
    
   /**
     * calculates the map of memberships included in <tt>this</tt> guard, grouped
     * by color and sign
     * @return the (possibly empty) map of memberships included in <tt>this</tt> guard, grouped
     * by color (first) and sign; if the guard is not a simple form then returns an empty map
     */
    @Override
    public final Map<ColorClass, Map <Boolean, Set<Membership> > > membMap() {
        if ( this.memb_map == null) {
            this.memb_map = simple() ? Collections.unmodifiableMap( setMemberMap() ) : Collections.EMPTY_MAP;
        }
        
        return this.memb_map;
    }
    
    /*
    initializes the color-map for memberships, based on their sign
    */
    private Map<ColorClass, Map <Boolean, Set<Membership> > > setMemberMap() {
        Map<ColorClass, Map <Boolean, Set<Membership> > > map = new HashMap<>();
        this.args.stream().filter(g -> g instanceof Membership).forEachOrdered(g -> {
            ColorClass cc = ((Membership)g).getSort();
            Map<Boolean, Set<Membership>> m_cc = map.get(cc);
            if (m_cc == null)
                map.put(cc, m_cc = new HashMap<>());
            Membership gm = (Membership) g;
            Util.addElem(gm.sign(), gm, m_cc);
        });
        
        return map;
    }
    
    /*
    initializes the color-maps for equalities, based on their sign
    */
    private Map<ColorClass, Map<Boolean, SortedSet<Equality>> > setEqualityMap () {
        Map<ColorClass, Map<Boolean, SortedSet<Equality>> > map  = new HashMap<>();
        //Comparator<Equality> comp = (e1,e2) -> { int c = e1.firstIndex().compareTo(e2.firstIndex()); //the equalities have the same color and sign
                    //if (c == 0 && (c = e1.secondIndex().compareTo(e2.secondIndex()) ) ==0) c = e1.getSucc().compareTo(e2.getSucc()); return c;};
        this.args.stream().filter(g -> g instanceof Equality).forEachOrdered( g -> {
            ColorClass cc = ((Equality)g).getSort();
            Map<Boolean, SortedSet<Equality>> m_cc = map.get(cc);
            if (m_cc == null)
                map.put(cc, m_cc = new HashMap<>());
            Equality ge = (Equality) g;
            Util.addOrdElem(ge.sign(), ge, m_cc, null );
        });
        
        return map;
    }
    
    
    /**
     * calculates the set of equalities of a given color and sign (congruent w.r.t. the operator's
     * type)
     * @param cc a color class
     * @param congr congruence flag (<tt>true</tt> if the operator is <tt>And</tt>, <tt>false</tt> otherwise)
     * @return the corresponding (possibly empty) set of "congruent" equalities (depends of the kind of logical operator)
     */
    public final SortedSet<Equality> congruentEq (ColorClass cc, boolean congr) {
        Map<Boolean, SortedSet<Equality>> cmap = equalityMap().get(cc);
        
        return cmap != null ? cmap.getOrDefault(congr == this.congrsign, Collections.emptySortedSet()) : Collections.emptySortedSet();
    }
    
  
    /**
     * calculates the specified set of equalities included in <tt>this</tt> guard
     * @param cc a color class
     * @param sign the equal/not-equal flag
     * @return the (possibly empty) corresponding set of (in)equalities (depends of the sign)
     */
    public final SortedSet<Equality> equality (ColorClass cc, boolean sign) {
        Map<Boolean, SortedSet<Equality>> emap = equalityMap().get(cc);
        
        return emap != null ? emap.getOrDefault(sign, Collections.emptySortedSet()) : Collections.emptySortedSet();
    }
    
   
    /**
     * 
     * @param cc a color class
     * @param congr congruence flag
     * @return the corresponding (possibly empty) set of "congruent" memeberships
     * (depends on the kind of logical operator:
     * in is congruent for <tt>And</tt>, notin for <tt>Or</tt>);
     */
    public final Set<Membership> congruentMemb(ColorClass cc, boolean congr) {
        Map<Boolean, Set<Membership>> cmap;
        return !cc.isSplit() || (cmap = membMap().get(cc)) == null ? Collections.EMPTY_SET 
                  :  cmap.getOrDefault(congr == this.congrsign, Collections.EMPTY_SET); //efficient
    }
    
    /**
     * calculates the included set of membership clauses
     * @param cc a color class
     * @param sign in/notin flag
     * @return the corresponding (possibly empty) set of membership clauses of
     * specified sign (in/notin) and color
     */
    public final Set<Membership> membership (ColorClass cc, boolean sign) {
        Map<Boolean, Set<Membership>> map = membMap().get(cc);
        return map != null ? map.getOrDefault(sign, Collections.EMPTY_SET) : Collections.EMPTY_SET;
    }
            
     /*
     enforce recomputing both the "congruent" maps
     */
    void reset() {
        this.eq_map = null;
        this.memb_map = null;
    }
       
   @Override
   public final Set<? extends Guard> getArgs() {
        return this.args;
    }
   
   /*
   collects the elementary guards of this operator into a set
   */
   final Set<? extends ElementaryGuard> getElementaryArgs() {
        Set<ElementaryGuard> eset = new HashSet<>();
        for (Map<Boolean, SortedSet<Equality>> x : equalityMap().values()) {
            eset.addAll(x.getOrDefault(true, Collections.emptySortedSet()));
            eset.addAll(x.getOrDefault(false, Collections.emptySortedSet()));
        }
        
        for (Map<Boolean, Set<Membership>> y : membMap().values()) {
            eset.addAll(y.getOrDefault(true, Collections.emptySortedSet()));
            eset.addAll(y.getOrDefault(false, Collections.emptySortedSet()));
        }
        
        return eset;
   }
    
    
    @Override
    public final boolean equals (Object o) {
        return N_aryOp.super.isEqual(o);
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        
        return 47 * hash + Objects.hashCode(this.args);
    }
    
    @Override
    public final String toString() {
        return N_aryOp.super.toStringOp();
    }
    
    
    @Override
     public final Set<Integer> indexSet() {
         return Guard.indexSet(this.args);
     }
      
    /**
     * if the guard is simple (@see NaryLogOP.simple()) computes the split-delimiters of
     * (ordered) colour classes by considering the successor's offsets in (in)equalities
     * @return a (possibly empty) map from colours to split-delimiters 
     */
    @Override
    public Map<Sort, Integer> splitDelimiters ( ) {    
        Map<Sort, Integer> delimiters = new HashMap<>();
        if ( simple() ) // needed ?
            equalityMap().entrySet().forEach( (var e) -> {
                ColorClass cc = e.getKey();
                if (cc.isOrdered() && cc.parametric()) {
                    Collection<Projection> cp = new HashSet<>();
                    e.getValue().getOrDefault(true, Collections.emptySortedSet()).forEach(x -> { 
                        cp.add(x.getArg2()); });
                    e.getValue().getOrDefault(false,Collections.emptySortedSet()).forEach(x -> {
                        cp.add(x.getArg2()); });
                    ColorClass.setDelim(delimiters, cc, succDelim(maxSuccOffset(cp, cc), cc));
                }
            });
           return delimiters;
         }
 
    @Override
    public Guard specSimplify() {
        //System.out.println("NaryGuardOperator (218)\n"+this); //debug
        Guard red = reduceMemberships();
         //System.out.println("->\n"+red); //debug
        return red == this ? reduceEqualities() : red;
    }
    
    /**
     * clone a collection of guards so that they have the given new domain
     * @param c a collection of guards
     * @param dom the new domain of guards
     * @return a <tt>Set</tt> of copies of the guards associated with the new domain
     */
    public final static Set<? extends Guard> cloneArgs ( Collection<? extends Guard> c , Domain dom)  {
        Set<Guard> new_args = new HashSet<>();
        c.forEach(g -> { new_args.add(g.clone(dom) ); });
        
        return new_args;
    }
     
    /**
     * @param dom the (possibly) new domain
     * @return a copy of <code>this</code> guard's operands of the specified domain
     */
    public final Set<? extends Guard> cloneArgs (Domain dom)  {
        return cloneArgs(this.args, dom);
    }    
            
   /** 
        Reduces membership clauses
        first checks whether two (assumed non empty and homogeneous!) sets of guards
        contains many membership clauses involving the same projection symbol:
        as for the "congruent" list, if there are more than one such clause
        (either the guard is an "And" and the sign is "in", or the guard is an "Or" and the sign is "notin");
        as for the other list, if the number of such clauses corresponds to the the number of subclasses
        (the guard is an "Or" and the sign is "in", or the guard is an "And" and the sign is "notin")
        in either case returns true; 
        then consider lists of similar "opposite" (i.em., non congruent) operands: if there is one of the same 
        length as the number (n) of subclasses return true (same meaning as above); conversely, any list of
        length n -1 is replaced by the corresponding complemtary membership;
        if no change has been done, finally compares each congruent term (there is at most one, after step 1),
        with the corresponding opposite list, first checking for the presence of a complementary membership, then
        (in the case of a negative check) eliminating similar opposite terms
        @return <code>this</code> if no changes are done
     */
   private Guard reduceMemberships () {
        Set<Guard> guards = null; // (light) copy of the operands
        for (Map.Entry<ColorClass, Map<Boolean, Set<Membership>>> em : membMap().entrySet()) {
            ColorClass cc = em.getKey();
            final int n = cc.subclasses();
            if (n > 1) { // partitioned class
                Map<Boolean, Set<Membership>> map_cc = em.getValue();
                Set<Membership> c_memb  = map_cc.getOrDefault( this.congrsign, Collections.EMPTY_SET), 
                                nc_memb = map_cc.getOrDefault(!this.congrsign, Collections.EMPTY_SET);
                HashMap<Integer, Set<Membership>> c_i_map = Util.mapFeature(c_memb,  m -> m.firstIndex()/*, m -> m.subcl().index()*/);
                for (Set<Membership> cs :  c_i_map.values() )  
                    if ( cs.size() > 1  ) 
                        return (Guard) getZero(); 
                // for each projection, there is at most one "congruent" membership of color cc
                Set<Map.Entry<Integer, Set<Membership>>> nc_i_set = Util.mapFeature( nc_memb,  m -> m.firstIndex() ).entrySet();
                for (Map.Entry<Integer, Set<Membership>> sim_nc : nc_i_set) {
                    Set<Membership> ncs = sim_nc.getValue(), cs; 
                    int m = ncs.size(), i ;
                    if ( m == n || (cs = c_i_map.get(i = sim_nc.getKey())) != null && ncs.contains(cs.iterator().next().opposite()) ) //cs is either empty or a singleton ...
                        return  (Guard) getZero();
                    // there are no complementary guards are and, for each projection, less than n "non-congruent" memberships
                    if ( cs != null ||  m > 1 && (m == n -1 || this instanceof /*Or*/And) ) { 
                        guards = Util.lightCopy(guards, this.args);
                        guards.removeAll(ncs);
                        if (cs == null) {// no corresponding congruent term: ncs is replaced by the equivalent "opposite"
                            Projection pi = ncs.iterator().next().getArg1();
                            HashSet<Membership> oppargs = new HashSet<>();
                            for (int j = 1 ; j <= n ; j++) {
                                Membership mem = Membership.build(pi, j, this.congrsign, getDomain());
                                if (!ncs.contains(mem.opposite())) //sbagliato: se sono più di uno bisogna aggiungere l'operatore
                                    oppargs.add(mem);
                            }
                            guards.add(/*And*/Or.factory(oppargs,true)); //va bene anche se this è And (Or): in quel caso oppargs è singleton
                        }
                    }
                }
            }
        }
       //System.out.println("before: "+this+"\nafter: "+buildOp(guards)); 
        return guards == null ? this : (Guard) buildOp(guards);
    }
      
   /*
   map (non)congruent equalities of a given color (assumed already sorted) into lists characterized by
   the same indices
   */
   Map<Pair<Integer,Integer>, List<? extends Equality>> similarCongrEqMap(ColorClass  cc, boolean congr)  {
        return Util.mapFeatureToList( congruentEq(cc, congr), e -> new Pair<>(e.firstIndex(), e.secondIndex()) );
   }
   
   /*
   efficiently checks if two (complementary) sets of elementary guards conain any opposite elements
   */
   private static  <E extends ElementaryGuard> boolean chekForCompl(Set<E> cset, Set<E> nc_set) {
       return cset.size() > nc_set.size() ? Util.checkAny(nc_set, t -> cset.contains(t.opposite())) : Util.checkAny(cset, t -> nc_set.contains(t.opposite()));
   }
   
   /**
    reduces the (in)equalities in <code>this</code> operator
    @return <code>null</code> if some further split is needed (of an ordered class);
    <code>this</code> if no changes are done
    SISTEMARE
    */
   private Guard reduceEqualities() {
       Set<Guard> argscopy = null;
       for (Map.Entry<ColorClass, Map<Boolean, SortedSet<Equality>>> e : equalityMap().entrySet()) {
           ColorClass cc = e.getKey();
           SortedSet<Equality> cset = congruentEq(cc,true), nc_set = congruentEq(cc,false);
           if (chekForCompl(cset, nc_set))
               return (Guard) getZero();
           
           if ( cc.isOrdered() ) {
                Map<Pair<Integer, Integer>, List<? extends Equality>> cmap = null;     
                int lb  = cc.lb(), size;
                // we first check for the presence of non-singleton, similar congruents lists, and (in case of singleton), that of complementary non.congruent
                boolean to_split = false;
                if (! this.congrsign) {// an "and" is excluded, assuming it is already canonical
                    cmap = similarCongrEqMap(cc, true);
                    for (List<? extends Equality> similar : cmap.values() )  
                        if ( (size = similar.size() ) > 1) {
                            if ( similar.get(size-1).getSucc() - similar.get(0).getSucc() < lb)   
                                 return getTrue() ; // if this.args is returned it means the term should be split ..
                            
                            to_split = true;
                        }
                }
                if (to_split)
                    continue;
                //similar congruents lists of color cc are singletons: we consider the non congruent lists    
                final boolean fixed_size = cc.hasFixedSize();
                for (Map.Entry<Pair<Integer, Integer>, List<? extends Equality>> sim_nc : similarCongrEqMap(cc, false).entrySet()) {
                    List<? extends Equality> poset = sim_nc.getValue(); //list of non congruent, similar terms
                    size = poset.size();
                    if ( fixed_size && size  == lb)  
                        return (Guard) getZero(); // the outcome is either 0 or S ...
                    else { 
                        final Pair<Integer, Integer> key = sim_nc.getKey();
                        final int exp, min, max;
                        boolean one_missing = fixed_size && size == lb -1;
                        List<? extends Equality> singlet; // the corresponding congruent singleton list   
                        if (cmap == null/*this.congrsign*/)
                            cmap = similarCongrEqMap(cc, true);
                        if ( one_missing || ( singlet = cmap.get( key )) != null && 
                           (max = poset.get(size - 1).getSucc()) - (min = poset.get(0).getSucc()) < lb && Math.abs((exp = singlet.get(0).getSucc()) - min) < lb && Math.abs(exp  - max) < lb) { 
                               argscopy = Util.lightCopy(argscopy, this.args);
                               argscopy.removeAll(poset);
                               if (one_missing) 
                                   argscopy.add(Equality.builder(Projection.builder(key.getKey(), cc), Projection.builder(key.getValue(), Util.missingNext(poset, Equality::getSucc, 0), cc), this.congrsign, getDomain()));
                           }
                      }
                }
            } 
       }
            
        return argscopy == null ? this : (Guard) buildOp(argscopy);
    }   

    /**
     *
     * @return <tt>true</tt> if <tt>this</tt> operator's operands are simple
     * (@see {LogicalExprs.simple})
     */
    public final boolean simple() {
        return this.simple;
    }    
    
    /**
     * @return the guard's color-class, if the guard is single-sort, and all predicates
     * are either (in)equalities or memberships; <tt>null</tt> in all the other cases
     */
    public final ColorClass getSort() {
        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> c_e = equalityMap();
		Map<ColorClass, Map<Boolean, Set<Membership>>> c_m = membMap();
        int n1 = c_e.size(), n2 = c_m.size(), s;
        if (n1 < 2 && n2 < 2 && (s = n1 + n2) > 0 && (s < 2 || c_e.keySet().equals(c_m.keySet())) )  {
	        ColorClass cc = n1 == 1 ? c_e.keySet().iterator().next() : c_m.keySet().iterator().next();
	        if (equality(cc,true).size() + equality(cc,false).size() + membership(cc,true).size() + membership(cc,false).size() == this.args.size())
	        	return cc ;
        }
        return null;
      }
    
}