package wncalculus.guard;

import java.util.*;
import wncalculus.graph.InequalityGraph;
import wncalculus.logexpr.AndOp;
import wncalculus.expr.*;
import wncalculus.classfunction.ClassFunction;
import wncalculus.classfunction.Projection;
import wncalculus.classfunction.Subcl;
import wncalculus.color.Color;
import wncalculus.color.ColorClass;
import wncalculus.color.SubclSet;
import wncalculus.util.Util;


/**
 * this class implements the boolean "AND" operator
 * @author Lorenzo Capra
 */
public final class And  extends NaryGuardOperator implements AndOp<Guard>  {

    /*
    return the set of operands of a guard which is assumed an elementary and form
     */

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

    /**
     * derives the memberships map, color by color, for a guard which is assumed an elementary "AND"-form
     * @param g a guard
     * @return the memberships map for a guard which is assumed an elementary "AND"-form;
     * an empty map, if the guard doesn't match an elementary "AND"-form
     */

    public static Map<ColorClass, Map<Boolean, Set<Membership>>> membMap(Guard g) {
        if (g instanceof Membership) {
            Membership m = (Membership) g;
            
            return Collections.singletonMap(m.getSort(), Collections.singletonMap(m.sign(), Collections.singleton(m)));
        }
        
        return g instanceof And ? ((And) g).membershipMap() : Collections.EMPTY_MAP;
    }

    /**
     * derives the equality map, color by color, for a guard which is assumed an elementary "AND"-form
     * if not, returns an empty map
     * @param g a guard
     * @return the equality map, color by color, for a guard which is assumed an elementary "AND"-form;
     * an empty map, if the guard doesn' match this form
     */

    public static Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap(Guard g) {
        if (g instanceof Equality) {
            Equality e = (Equality) g;
            
            return Collections.singletonMap(e.getSort(), Collections.singletonMap(e.sign(), Util.singleton(e, null)));
        }
        
        return g instanceof And ? ((And) g).equalityMap() : Collections.EMPTY_MAP;
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
        if (simple() && delimiters.isEmpty() ) 
            igraph().entrySet().forEach(e -> {
                Color s = e.getKey();
                Interval card = s.card(); //s is either a color class or a Subcl(Set)
                InequalityGraph g = e.getValue();
                int X = g.chromaticNumber() , lb = card.lb(), ub;
                ColorClass cc = (ColorClass) s.getSort(); // significant is s is a Subcl(Set)
                if ( lb < X && ( ( ub = card.ub() ) < 0 || ub >= X) ) {  // lb < X <= ub
                    //System.out.println("setChromaticBound "+(lb -card.lb() + X)); // debug    
                    ColorClass.setDelim(delimiters, cc, cc.lb() - lb + X - 1 /*new Delimiter(cc.lb() - lb + X - 1, false)*/);
                }
                else {
                    List<? extends ClassFunction> rt = getRightTuple() != null ? getRightTuple().get(cc) : null;
                    ColorClass.setDelim(delimiters, cc, g.splitDelimiter(rt));
                }
        });
        
        return delimiters;
    } 
    
    
    /**
     * for each colour of <code>this</code> and form, maps the associated "domain"
     * (either a color class or a subclass) into the associated inequality graph;
     * @return a map between the "colours" of inequalities in this guard 
     * and corresponding graphs
     *
     * NOTE this version assumes that for each colour the corresponding inequalities form
     * an independent set
     */
    public Map<Color, InequalityGraph> igraph () {
        if (this.igraph == null) {
            this.igraph = new HashMap<>();
            equalityMap().entrySet().forEach(e -> {
                ColorClass cc = e.getKey();
                SortedSet<Equality> inequalities = e.getValue().get(false);
                if (inequalities != null) { 
                    InequalityGraph g = new InequalityGraph(inequalities );
                    Color c = cc;
                    if ( cc.isSplit() ) { // memberships clauses may be present...
                        SubclSet sdom = checkDomain(g.vertexSet(), cc);
                        if (sdom != null)
                            c = sdom;
                    }
                    this.igraph.put(c, g);
                }
            });
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
        Map<Boolean, Set<Membership>> m = membershipMap().get(cc);
        if (m != null) {
            Set<Membership> in    = m.getOrDefault(true,Collections.emptySortedSet()), 
                            notin = m.getOrDefault(false, Collections.emptySortedSet());
            if ( notin.isEmpty() && ! in.isEmpty()) { // the notin list is empty
                Subcl v = Util.isConstantSurjective(Membership.mapSymbolsNoRep(in), vset);
                if (v != null)
                    return new SubclSet(v);
            }
            else if ( in.isEmpty() && ! notin.isEmpty() ) { // the in list is empty
                Set<Subcl> sv = Util.isConstantSurjective(Membership.mapSymbols(notin), vset);
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
     * safely builds an "and" form from a list of guards and a domain ... (see the overloaded method);
     * @param eg_set the specified list, possibly containing <code>null</code> terms (standing for <code>true</code>)
     * @param dom a specified domain
     * @return an "And" corresponding to the list, with the specified domain; in the case the list is a singleton,
     * the  contained guard; <code>null</code> if the specified list is <code>null</code> or empty
     * (modulo <code>null</code>-terms erasure)
     * @throws IllegalDomain if the operands' domains are different
     */
    public static Guard buildAndForm (Collection<? extends Guard> eg_set, Domain dom) {
        if (eg_set == null )
            return null ;
        
        eg_set.removeAll(Collections.singleton(null));
        if (eg_set.isEmpty()) 
            return null ;
        
        return And.factory(dom == null || eg_set.iterator().next().getDomain().equals(dom) ? eg_set : NaryGuardOperator.cloneArgs(eg_set, dom));
    }
    
    /**
     * safely builds an "and" form from a list of guards (see the overloaded method);
     * @param eg_set the specified list
     * @return an And term corresponding to the list; in the case the list is a singleton,
     * the  contained guard; if the specified list is <code>null</code> or empty then returns <code>null</code>;
     */
    public static Guard  buildAndForm (Collection<? extends Guard> eg_set) {
        return  buildAndForm(eg_set, null);
    }
            
    /**
     * replace symbols in an ordered set of equalities, leading to the canonical form
     * in one step: if during replacement some equality becomes "true" then it is skipped,
     * whereas if it becomes "false" the set is cleared
     * @param es a set of equalities to put into a canonical form
     * @return <tt>true</tt> if and only if the set is modified
     */
    public /*private*/static boolean toCanonicalForm (SortedSet<Equality> es) {
        List<Equality> processed  = new ArrayList<>(),
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
            
            Guard f = g.replace(eq);
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
        List<E> replaced = new ArrayList<>();
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
                    notinmap.getOrDefault(p1, Collections.EMPTY_SET).contains(s2) ||
                    notinmap.getOrDefault(p2, Collections.EMPTY_SET).contains(s1) ) {  // p1 and p2 refer to different subclasses
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
            Set<Subcl> notin1 = notinmap.getOrDefault(p1, Collections.emptySet()) , 
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
                Map<Boolean, Set<Membership>> mm = membershipMap().get(e.getKey());
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
        for (Map.Entry<ColorClass, Map<Boolean, Set<Membership>>> e : membershipMap().entrySet()) {
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
     * DA FINIRE 
     * @return the parition of the variable indices into independent parts
     * should be invoked on single-color guards 
     * assume that the guard has been simplified, in particular, symbol replacement
     * has been carried out 
     */
    public Set<HashSet<Integer>> independentSets () {
    	ColorClass cc = getSort();
        if (cc != null) {
            SortedSet<Equality> es = equality(cc, true);
            Set<HashSet<Integer>> connectedIndices = igraph().get(cc).connectedIndices(); //independent inequalities
            if (es.isEmpty())
                return connectedIndices;
            Map<Integer,Set<Integer>> indep_m = new HashMap<>(); //partition of equalities based on their first index (see the assumption)
            es.forEach(e -> { Util.addElem(e.firstIndex(), e.secondIndex(), indep_m); });
            Iterator<HashSet<Integer>> ite;
            for (Map.Entry<Integer, Set<Integer>> x : indep_m.entrySet())
                for (ite = connectedIndices.iterator() ; ite.hasNext(); ) {
                    HashSet<Integer> iset = ite.next();
                    if (iset.contains(x.getKey()))  // logicamente : aggiungi a iset tutte le uguaglianze corrispondenti a x
                        iset.addAll(x.getValue());
                    ite.remove();
                }
        }
    	return cc == null ? Collections.emptySet() : igraph().get(cc).connectedIndices();
    }
    
}
