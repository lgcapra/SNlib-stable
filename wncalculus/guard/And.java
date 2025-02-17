package guard;

import java.util.*;
import graph.InequalityGraph;
import logexpr.AndOp;
import expr.*;
import classfunction.Projection;
import classfunction.Subcl;
import color.Color;
import color.ColorClass;
import color.SubclSet;
import util.Util;


/**
 * this class implements the boolean "AND" operator
 * @author Lorenzo Capra
 */
public final class And  extends NaryGuardOperator implements AndOp<Guard>  {

    /**
     * assuming that a given guard is an elementary "AND" form, calculates its set
     * of operands (a singleton if the guard is elementary)
     * @param g a guard
     * @return the set of operands of the guard, assumed an  elementary "AND" form (a singleton if the guard is elementary)
     * @throws ClassCastException if the guard is neither elementary nor an "AND" operator
     */
    public static Set<? extends Guard> getArgs(Guard g) {
        return g instanceof ElementaryGuard ? Collections.singleton(g) : ((And) g).getArgs();
    }

    private Map<Color, InequalityGraph> igraph; // cashing: the (possibly empty) map between colors and corresponding inequality graphs
    
    private And (Set<? extends Guard> guards, boolean check) {
        super(guards,/*check*/true);
    }
     /**
     * build an And operator from a collection of operands (if the collection is
     * a singleton, the only operand is "extracted")
     * @param arglist the collection of operands
     * @param check domain-check flag
     * @return the newly built guard
     * @throws IllegalDomain NullPointerException NoSuchElementException
     */
    public static Guard factory(Collection<?  extends Guard> arglist, boolean check) {
        Set<? extends Guard> asSet = Util.asSet(arglist);
        return asSet.size() < 2 ?  asSet.iterator().next() : new And (asSet, check);
    }
    
    /**
     * build an And operator from a collection of operands (if the collection is
     * a singleton, the only operand is "extracted"), by checking the domains
     * @param term_list the collection of operands
     * @return the newly built guard
     */
    public static Guard factory(Collection<? extends Guard> term_list)  {
        return factory(term_list, true);
    }
    
    /**
     * build an And operator from a list of operands expressed as varargs
     * @param check domain-check flag
     * @param args a list of operands
     * @return the newly built guard
     */
    public static Guard factory (boolean check, Guard ... args)  {
        return factory(Arrays.asList(args), check); 
    }
    
    /**
     * build an And operator from a list of operands expressed as varargs
     * by checking the domains
     * @param args a list of operands
     * @return  the newly built guard
     */
    public static Guard factory(Guard ... args) {
        return factory(true, args);
    }
    
    @Override
    public Guard buildOp(Collection<? extends Guard> args)  {
        return factory(args, false);
    }
    
    @Override
    public String symb() {
        return ",";
    }
        
    @Override
    public Map<Sort, Integer> splitDelimiters ( ) {    
        Map<Sort, Integer> delimiters = super.splitDelimiters();
        //System.out.println("ecco delims: "+delimiters); //debug
        if (simple() ) 
            igraph().entrySet().forEach(e -> {
                Color s = e.getKey();
                Interval card = s.card(); //s is either a color class or a Subcl(Set)
                InequalityGraph g = e.getValue();
                int X = g.chromaticNumber() , lb = card.lb();
                ColorClass cc = (ColorClass) s.getSort(); // significant is s is a Subcl(Set)
                if ( lb < X && ( card.unbounded() || X <= card.ub() ) ) {  // we first consider the predicate alone (disregarding if it is a tuple's filter)
                    //System.out.println("setChromaticBound "+(lb -card.lb() + X)); // debug    
                    //ColorClass.setDelim(delimiters, cc, cc.lb() - lb + X - 1 ); // old
                    ColorClass.setDelim(delimiters, cc, X - lb);
                }
                else { // we check whether the predicate is a filter (i.e., associated to a tuple)
                    var tuple = getRightTuple();
                    ColorClass.setDelim(delimiters, cc, g.splitDelimiter(tuple != null ? tuple.get(cc) : null));
                }
        });
        
        return delimiters;
    } 
    
    
    /**
     * for each colour of <code>this</code> and form, maps the associated "domain"
     * (either a color class or a subclass) into the associated inequality graph;
     * @param checkdom flag to check that (in the event of spit classes) the variable domains are subclasses
     * @return a map between the "colours" of inequalities in this guard and corresponding graphs
     *
     * NOTE this version assumes that for each colour the corresponding inequalities form
     * an independent set
     */
    public Map<Color, InequalityGraph> igraph (boolean checkdom) {
        if (this.igraph == null) {
            this.igraph = new HashMap<>();
            equalityMap().entrySet().forEach(e -> {
                ColorClass cc = e.getKey();
                SortedSet<Equality> inequalities = e.getValue().get(false);
                if (! (inequalities == null || inequalities.isEmpty() ) ) { 
                    InequalityGraph g = new InequalityGraph(inequalities );
                    Color c = cc;
                    if ( checkdom && cc.isSplit() ) { // memberships clauses may be present...
                        SubclSet sdom = checkDomain(g.vertexSet(), cc);
                        if (sdom != null)
                            c = sdom;
                    }
                    this.igraph.put(c, g);
                }
            });
        }
        //System.out.println("igraph di "+this +": "+this.igraph); //debug
        return Collections.unmodifiableMap(this.igraph);
    }
    
    /**
     * default version
     * @return a map between the "colours" of inequalities in this guard and corresponding graphs
     */
    public Map<Color, InequalityGraph> igraph () {
        return igraph(true);
    }
    
    
    /***
     * overloaded (simplified) version of igraph for single-color And
     * no check for variable mebership to subclasses done
     * @param cc the (pre-calculated) color class of the And 
     * @return a singleton map from the color to the inequality graph
     */
    public Map<Color, InequalityGraph> igraph (ColorClass cc) {
        if (this.igraph == null) {
            SortedSet<Equality> inequalities = equalityMap().get(cc).get(false);
            if (! (inequalities == null || inequalities.isEmpty() ) )  
                this.igraph =  Util.singleMap(cc, new InequalityGraph(inequalities ));
            else
                this.igraph = Collections.emptyMap();
        }
        //System.out.println("igraph di "+this +": "+this.igraph); //debug
        return this.igraph;
    }
    
    
    
    /**
     * checks whether the "domain" of a (assumed non empty) set of variables of a given color
     * is a (subset of) subclass(es)
     * @param vset the variables set
     * @return the subclass(es), seen as a color-type, to which variables are bound
     * (based on related memebership clauses)
     * <code>null</code> if for any reasons the check fails, or there are no memebership clauses
     */
    private SubclSet checkDomain (Set<? extends Projection> vset, ColorClass cc) {
        final Map<Boolean, Set<Membership>> m = membMap().get(cc);
        if (m != null) {
            final Set<Membership> in    = m.getOrDefault(true,Collections.emptySortedSet()), 
                                  notin = m.getOrDefault(false, Collections.emptySortedSet());
            if ( notin.isEmpty() && ! in.isEmpty()) { // the notin list is empty
                final Subcl v = Util.isConstantSurjective(Membership.mapSymbolsNoRep(in), vset);
                if (v != null)
                    return new SubclSet(v);
            }
            else if ( in.isEmpty() && ! notin.isEmpty() ) { // the in list is empty
                final Set<Subcl> sv = Util.isConstantSurjective(Membership.mapSymbols(notin), vset);
                if (sv != null)
                    return new SubclSet(sv);
            }
        }
        
        return null;
    }  

    @Override
    public And clone(Domain new_dom) {
        return new_dom.equals(getDomain()) ? this : new And (cloneArgs(new_dom), false);
    }
    
    
    /**
     * builds an "and" form from a collection of guards and a domain
     * @param eg_set the specified collection
     * @param dom a (non-null) domain
     * @return a corresponding "And" form, with the specified domain; if the collection is a singleton,
     * the  (only) contained guard; if it is empty, a True constant
     * @throws IllegalDomain if the operands' domains are different from the given one
     * @throws IllegalArgumentException if the domain is null.
     */
    public static Guard buildAndFormWithD (Collection<? extends Guard> eg_set, Domain dom) {
        if (dom == null)
            throw new IllegalArgumentException("expected a non-null domain");
            
        return eg_set.isEmpty() ? True.getInstance(dom) : And.factory(NaryGuardOperator.cloneArgs(eg_set, dom));
    }
    
            
    /**
     * replace symbols in an ordered set of equalities, leading to the canonical form
     * in one step: if during replacement some equality becomes "true" then it is skipped,
     * whereas if it becomes "false" the set is cleared
     * @param es a set of equalities to put into a canonical form
     * @return <tt>true</tt> if and only if the set is modified
     */
    public /*private*/static boolean toCanonicalForm (SortedSet<Equality> es) {
        final List<Equality> processed  = new ArrayList<>(),
                             to_process = new ArrayList<>(); // already processed
        boolean rep = false;
        do  {
            Equality eq ;
            if (to_process.isEmpty() ) 
                es.remove( eq = es.first() );
            else 
                eq = to_process.remove(0);//we take the last replacement result
            Boolean done = replaceEq (es, eq, to_process);
            if (done == null) {
                 es.clear();
                 return true;
            }
            
            if (done)
                rep = true;
            processed.add(eq);
        }
        while ( ! es.isEmpty() ) ;
        es.addAll(processed); //es is empty
        es.addAll(to_process);
        
        return rep;
    }
    
    /**
    replace symbols in a (possibly sorted) set of elementary guards according to an equality;
    guards involved in replacements are moved (in order) to a list; if some replacement results in
    @code {false} then the process immediately stops, and @code {null} is returned;
    replacements resulting in @code {true} instead are skipped
    @return @code {null} if some replacement results in @code {false}; @code {true} if some replacement
    has been done; @code {false} otherwise
    */
    private static <E extends ElementaryGuard> Boolean replaceEq (Set<? extends E> elgs, Equality eq,  List<E> replaced) {
        Boolean done = false;
        for (Iterator<? extends E> ite = elgs.iterator(); ite.hasNext() ;  ) {
            E g = ite.next();
            if (elgs instanceof SortedSet && g.firstIndex() > eq.secondIndex())
                break; //optimization
            
            final Guard f = g.replace(eq);
            if (f instanceof False) 
                return null;
            
            if ( ! g.equals(f) ) {
                 done = true;
                 ite.remove();
                 if (f instanceof ElementaryGuard) // if f is true then it is skipped
                    replaced.add((E) f); // f is already in its final form 
             }     
        }
        
        return done;
    }
    
    /**
     * mass version of @see replace: given a set of equalities, replaces accordingly symbols in
     * a (possibly sorted) set of elementary guards if during replacement some guard becomes "false" then the process
     * immediately stops, and <code>null</code> is returned; replacements resulting in "true" are skipped
     * @param <E> the type of elementary guard
     * @param egs the set of guards where doing replacements
     * @param eqs the set of equalities defining replacements 
     * @return <code>null</code> if some guards becomes <code>false</code>;
     * <code>true</code> if some replacement has been done, <code>false</code> otherwise
     */
    /*private*/ public static <E extends ElementaryGuard> Boolean replaceEq (Set<E> egs, SortedSet<? extends Equality> eqs) {
        boolean done = false;
        final List<E> replaced = new ArrayList<>();
        //System.out.print("replacment:" + egs +',' + eqs);
        for (Equality eq : eqs) {
            Boolean some_repl = replaceEq( egs, eq, replaced);
            if (some_repl == null ) 
                return null;
            
            if (some_repl)    
                done = true;
        }
        egs.addAll(replaced);
        //System.out.println(" : --> " + egs);
        return done;
    }
    
       
   /** 
     * removes redundant (in)equalities between symbols that refer to a singleton subclass
     * from an (assumed homogeneous!) list, by possibly adding the missing memberships
     */
   static boolean replaceRedEqWithMember(Set<Guard> arglist, Map <Boolean, SortedSet<Equality> > eqmap, Map<? extends Projection, Subcl > inmap) {
       boolean changed = false;
       for (Map.Entry<Boolean, SortedSet<Equality>> entry : eqmap.entrySet()) {
           boolean sign = entry.getKey();
           for (Equality e : entry.getValue()) {
               Projection p1 = e.getArg1(), p2 = e.getArg2();
               Subcl s = inmap.get(p1);
               if (s != null && s.card().ub() == 1)  
                    changed = arglist.remove(e) | arglist.add(Membership.build(p2, s, sign, e.getDomain() ) );
               else if ((s = inmap.get(p2)) != null && s.card().ub() == 1) 
                    changed = arglist.remove(e) | arglist.add(Membership.build(p1, s, sign, e.getDomain() ) );
            }
       }
       
        return changed;
    }
   
   /**
     * removes from a list of (elementary) guards those inequalities that are redundant due
     * to the presence of memebership clauses:
     * a) X^1 != X^2 and X^1 in C_1 and X^2 in C_2  b) X^1 != X^2 and X^1 in C_1 and X^2 notin C_1
     * redundant inequalities are also removed from the corresponding list (optimization)
     * @param arglist a list of guards
     * @param ineqs the pre-compute list of inequalities 
     * @param inmap the pre-computed "in" map
     * @param notinmap the pre-computed "notin" map
     */
    static boolean removeRedundantIneq(Collection<Guard> arglist, Collection <Equality> ineqs, Map<? extends Projection, Subcl > inmap, Map<? extends Projection, Set<Subcl>> notinmap) {
        boolean reduced = false;
        for (Iterator<Equality> it = ineqs.iterator(); it.hasNext();) {            
            Equality e = it.next();
            Projection p1 = e.getArg1(),p2 = e.getArg2();
            Subcl s1  = inmap.get( p1), s2   = inmap.get(p2);
            if ( s1 != null && s2 != null && ! s1.equals(s2) || 
                    notinmap.getOrDefault(p1, Collections.emptySet()).contains(s2) ||
                    notinmap.getOrDefault(p2, Collections.emptySet()).contains(s1) ) {  // p1 and p2 refer to different subclasses
                arglist.remove(e);
                it.remove(); //optimization
                reduced = true;
            }
        }
        
        return reduced;
    }
   
    /** if necessary, "rewrites" (once it is repeatedly applied) a collection of guards
        making all projection symbols involved in inequations refer to the same "domain" (i.e., subclasses);
        it searches for the occurrence of any inequality X^i <> X^j s.t. either X_i or X_j refers to a
        a subclass and the other not, or both X^i, X^j are associated with (not equal) non-membership clauses
        just performs one rewriting step
        assumes that redundant inequalities were already removed
        @return <code>true</code> if and only if any change is made 
    */
    static boolean setVarSameDomain(Collection<Guard> arglist, Collection<? extends Equality> ineqs, Map<? extends Projection, Subcl > inmap, Map<? extends Projection, Set<Subcl>> notinmap) {
        for (Equality e :   ineqs ) {            
            Projection p1 = e.getArg1(), p2 = e.getArg2(),   p = p2;
            Subcl s1  = inmap.get( p1),  s2 = inmap.get(p2), s = s1;
            Set<Guard> list1, list2, list3;
            Domain dom = e.getDomain();
            // we assume that s1 != null && s2 != null ==> s1 == s2  
            if ( s1 != null && s2 == null || s1 == null && (s = s2 ) != null &&  (p = p1) != null/*last redundant*/) { // p1 (p2) refers to a subclass C_1 (C_2), p2 (p1) doesn't (see the assumption ..): we logically add p2(1) in C_1(2) or p2(1) notin C_1(2)
                list1 = new HashSet<>(arglist);
                for (Subcl sx : notinmap.getOrDefault(p, Collections.emptySet())) // vengono rimosse da list1 tutte le eventuali clausole p notin ...
                    list1.remove(Membership.build(p, sx, false, dom)); // optimization
                list1.add(Membership.build(p, s, dom));
                arglist.remove(e);
                list2 = new HashSet<>(arglist);
                list2.add(Membership.build(p, s, false, dom));
                arglist.clear();
                arglist.add(Or.factory(true, And.factory(list1), And.factory(list2)));
                
                return true;
            }
            final Set<Subcl> notin1 = notinmap.getOrDefault(p1, Collections.emptySet()) , 
                             notin2 = notinmap.getOrDefault(p2, Collections.emptySet()) ;
            if (! notin1.equals(notin2) ){
                Set<Subcl> setdiff = new HashSet<>(notin2);
                setdiff.removeAll(notin1); // notin2 - notin1
                if ( !setdiff.isEmpty() )
                    p = p1;
                else { // notin1 - notin2 is not empty: just one rewriting step is done
                    setdiff = new HashSet<>(notin1); // may be a copy is not necessary ...
                    setdiff.removeAll(notin2);
                    p = p2; 
                }
                list1 = new HashSet<>(arglist);
                final Projection fp = p; //workaround: variables used in lambda must be final
                setdiff.forEach( sx -> { list1.add(Membership.build(fp, sx, false, dom)); });
                arglist.remove(e);
                notinmap.getOrDefault(fp, Collections.emptySet()).forEach(sx -> { arglist.remove(Membership.build(fp, sx, false, dom)); } ); // optimization: rimosse da list2 tutte le eventuali clausole p notin ...
                list2 = new HashSet<>(arglist);
                list3 = new HashSet<>();
                setdiff.forEach( sx -> { list3.add(Membership.build(fp, sx, dom)); });
                list2.add(Or.factory(list3, true));
                arglist.clear();
                arglist.add(Or.factory(true, And.factory(list1), And.factory(list2)));
                
                return true;
            }
        } 
        
        return false;
    }
    
    @Override
    public Guard specSimplify() { //new
        //System.out.println("And.specsimplify (1)\n"+this);
        if ( ! simple() )
            return this;
        //the equalities are first put in the canonical form
        HashSet<ColorClass> involved = new HashSet<>();
        for (Map.Entry<ColorClass, Map<Boolean, SortedSet<Equality>>> e : equalityMap().entrySet()) {
            SortedSet<Equality> es = e.getValue().get(true);
            if (es != null && es.size() > 1 && toCanonicalForm(es) ) {
                if (es.isEmpty())
                    return getFalse();
                
                involved.add(e.getKey());
             }
        }
        if (! involved.isEmpty() ) { //new
            HashSet<Guard> copy = new HashSet<>(); //we copy all guards but the equalities of colors involved in the canonization..
            getArgs().stream().filter(g -> ! (g.isEquality() && involved.contains(((Equality)g).getSort()))). forEachOrdered(g -> { copy.add(g);} );
            involved.forEach(cc -> { copy.addAll( equalityMap().get(cc).get(true) ); });
            
            return And.factory(copy);
        }
        //then symbols in inequalities and memberships are replaced, accordibg to equalites
        boolean replaced = false;
        for (Map.Entry<ColorClass, Map<Boolean, SortedSet<Equality>>> e :  equalityMap().entrySet()) {
            Boolean done;
            SortedSet<Equality> es  = e.getValue().get(true);
            if (es != null) {
                SortedSet<Equality> ies = e.getValue().get(false);
                if (ies != null ) { // we consider the corresponding set of inequalities
                    if ( (done = replaceEq(ies, es) ) == null)
                        return getFalse();
                    else if (done)
                        replaced = true;
                }
                Map<Boolean, Set<Membership>> mm = membMap().get(e.getKey());
                if (mm != null) // there exists some memebership clauses of the same color ...
                    for (Map.Entry<Boolean, Set<Membership>> x : mm.entrySet()) {
                        Set<Membership> ms = x.getValue();
                        if (ms != null) {
                            if ( (done = replaceEq(ms, es) ) == null)
                                return getFalse();
                            else if (done)
                                replaced = true;
                        }
                    }
            }    
        }
        
        if (replaced) {
            Guard rep = And.factory( getElementaryArgs() );
            reset(); // redundant, just for coherence
            //System.out.println("after replacment: -->\n"+rep); //debug
            return rep;
        }
        Guard red = super.specSimplify(); // reduce equalities and memberships;
        
        //la parte che segue non dovrebbe essere fatta se è un filtro di una tupla T
        if (red == this &&  (red  = reduceRedundanciesAndSetVarDomain() ) == this) 
            for (Map.Entry<Color, InequalityGraph> e : igraph().entrySet() ) { //here!
                int ub = e.getKey().card().ub();
                if ( ub > 0 && ub < e.getValue().chromaticNumber())   // constraint u.b. less than the chromatic N.
                    return  getFalse();
            }
        //System.out.println("(final return) -->\n"+red); //debug
        return red;
    }
    
    private Guard reduceRedundanciesAndSetVarDomain () {
        Set<Guard> oplist = null; // (light) copy of the operands        
        boolean changed = false;    
        for (Map.Entry<ColorClass, Map<Boolean, Set<Membership>>> e : membMap().entrySet()) {
            ColorClass cc = e.getKey();
            Map<Boolean, SortedSet<Equality>> eqmap = equalityMap().get(cc);
            if ( eqmap != null) { 
                Set<Membership> memb = e.getValue().get(true), notmemb ;
                Map<Projection, Subcl>      inmap    = Collections.EMPTY_MAP;
                Map<Projection, Set<Subcl>> notinmap = Collections.EMPTY_MAP;
                oplist = Util.lightCopy(oplist, getArgs());
                if (memb != null)  {
                    inmap = Membership.mapSymbolsNoRep(memb);
                    if ( replaceRedEqWithMember(oplist, eqmap, inmap)) {// e.g., X^1 = (!=) X^2 and X^1 in C_1 (|C_1|=1) -> X^1 in C_1 and X^2 (not)in C_1
                        changed = true;
                        continue; //safe: after this step the guards should be simplified
                    }
                }   
                Set<Equality> inequalities = eqmap.get(false);
                if ( inequalities != null ) {
                    if ( (notmemb = e.getValue().get(false) ) != null) 
                        notinmap = Membership.mapSymbols(notmemb);
                    if (memb != null)
                        changed = removeRedundantIneq(oplist, inequalities, inmap, notinmap) || changed; // this step may just reduce the set of inequalities
                    
                    changed = And.setVarSameDomain(oplist , inequalities, inmap, notinmap ) || changed;
                }
            }
        }
       
        return changed ? And.factory(oplist) : this;
    }
    
    @Override
    public boolean isElemAndForm() {
        return Util.checkAll(getArgs(), ElementaryGuard.class::isInstance);
    }
    
    /* 
     * @return the partition of the variable indices into independent parts
     * should be invoked on single-color guards 
     * assume that the guard has been simplified, in particular, symbol replacement
     * has been carried out 
     */
    /*public Set<HashSet<Integer>> independentSets () {
    	final ColorClass cc = getSort();
        if (cc != null && simple()) {
            final SortedSet<Equality> es = equality(cc, true);
            final Set<HashSet<Integer>> connectedIndices = igraph().get(cc).connectedIndices(); //independent inequalities
            if (es.isEmpty())
                return connectedIndices;
            
            final Map<Integer,Set<Integer>> indep_m = new HashMap<>(); //partition of equalities based on their first index (see the assumption)
            es.forEach(e -> { Util.addElem(e.firstIndex(), e.secondIndex(), indep_m); });
            for (Map.Entry<Integer, Set<Integer>> x : indep_m.entrySet())
                for (Iterator<HashSet<Integer>> ite = connectedIndices.iterator() ; ite.hasNext(); ite.remove()) {
                    final var iset = ite.next();
                    if (iset.contains(x.getKey()))  // logicamente : aggiungi a iset tutte le uguaglianze corrispondenti a x
                            iset.addAll(x.getValue());
                }
        }

    	return cc == null ? Collections.emptySet() : igraph().get(cc).connectedIndices();
    }*/
    
}
