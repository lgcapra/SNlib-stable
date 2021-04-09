package wncalculus.tuple;

import java.util.*;
import wncalculus.classfunction.*;
import wncalculus.color.*;
import wncalculus.expr.*;
import wncalculus.guard.*;
import wncalculus.graph.InequalityGraph;
import wncalculus.logexpr.LogicalExprs;
import wncalculus.util.Pair;
import wncalculus.util.Util;


/**
 * this class defines a (possibly guarded) basic tuple of boolean class-functions
 * @author Lorenzo Capra
 */
public final class Tuple extends AbstractTuple<SetFunction> implements FunctionTuple, Cloneable {
    
    private boolean reduce_guard; // signals whether the guard has to "absorbed" into the tuple (default: false)
    
    /**
     * Base constructor (the only which should be used from outside the library at tuple's parsing time):
     * builds a tuple from a list of class-functions, in which the order of tuple's element doesn't matter:
     * the tuple which is built is ordered, just for convenience, w.r.t.colour classes, i.e. elements of the
     * same colour are automatically grouped, respecting the relative initial order of elements.
     * The tuple's codomain is inferred; either the tuple's domain or the tuple's guard must be specified,
     * otherwise a NullPointerException is raised; if the specified filter (guard) values(s) is (are) null, then
     * a trivial filter (guard) is (are) associated with this tuple.
     * Variable indexes have to fall in the corresponding colour bounds of the domain,
     * otherwise an exception is raised.
     * Creates an unmodifiable data structures so any attempt to modify the tuple will raise an exception.
     * @param f the tuple's filter
     * @param g the tuple's guard
     * @param l the list of class-functions 
     * @param dom the tuple's domain
     * @throws IllegalDomain if the filter's domain doesn't match the tuple's composition,
     * or the guard's domain doesn't match the tuple's domain, or the domains of components
     * are different
     */
    public Tuple (Guard f, List<? extends SetFunction> l, Guard g, Domain dom) {
        super (f, l, g, dom, true);
    }
    
    //i seguenti costruttori derivati da quello base sono eliminabili (usati solo nel Main !)

    /**
     * build a tuple from a (varargs) list of class-functions
     * @param f  the tuple's filter
     * @param g  the tuple's guard
     * @param dom  the tuple's domain
     * @param args the list of tuple's components
     */
    public Tuple(Guard f, Guard g, Domain dom , SetFunction ... args) {
        this(f, Arrays.asList(args), g, dom);
    }
                
    /**
     * build a tuple without a guard/filter from a (varargs) list of class-functions
     * @param dom  the tuple's domain
     * @param args the list of tuple's components
     */
    public Tuple(Domain dom , SetFunction ... args) {
        this(null, Arrays.asList(args), null, dom);
    }    
    
    //aggiunti x compatibilitÃ  con cli (richiamano costruttore base)

    /**
     * build a tuple without a guard/filter from a list of class-functions
     * @param l a list of functions
     * @param dom the tuple's domain
     */
    public Tuple(List<? extends SetFunction> l, Domain dom) {
        this(null, l, null, dom );
    }

    //I SEGUENTI COSTRUTTORI SONO USATI INTERNAMENTE ALLA LIBRERIA PER EFFICIENZA 
    
    /**
     * efficiently builds a tuple from a map between colors and corresponding class-function lists;
     * it doesn't perform any check and true copy, it builds an unmodifiable view of the passed map
     * @param filter the tuple's filter (<code>null</code> means TRUE)
     * @param codomain the tuple's codomain (necessary only if filter is <code>null</code>)
     * @param m the specified map
     * @param guard the tuple's guard (<code>null</code> means TRUE)
     * @param domain the tuple's domain (necessary only if guard is <code>null</code>)
     */
    Tuple (Guard f, Domain cd, SortedMap<ColorClass, List<? extends SetFunction>> m, Guard g, Domain d) {
        super(f, cd, m, g, d);
    }
    
    /**
     * efficiently builds a tuple from a list of class-function, which is assumed to be of a given cc_low_case-class
 (the tuple's codomain is not inferred)
     * @param filter the tuple's filter (<code>null</code> means TRUE)
     * @param codomain the tuple'fc codomain (necessary only if filter is <code>null</code>)
     * @param cc a cc_low_case class
     * @param list the specified list (of cc_low_case @param {cc_name})
     * @param guard the tuple's guard (<code>null</code> means TRUE)
     * @param domain the tuple's domain (necessary only if guard is <code>null</code>)
     */
    Tuple (Guard filter, Domain codomain, ColorClass cc, List<? extends SetFunction> list, Guard guard, Domain domain) {
        this(filter, codomain, Util.singleSortedMap(cc, list), guard, domain);
    }
    
    
    /** 
     * build a singleton tuple
     * @param f the tuple's only component
     * @param g the tuple's guard
     */
    Tuple (SetFunction f , Guard g, Domain d)  {
        this(null, new Domain(f.getSort(),1), Util.singleSortedMap(f.getSort(), Collections.singletonList(f)), g, d );
    }
            
    /**
     * efficiently builds a tuple from a list of class-function
     * (the tuple's codomain is not inferred)
     * @param filter the tuple's filter (<code>null</code> means TRUE)
     * @param codomain the tuple'fc codomain (necessary only if filter is <code>null</code>)
     * @param list the specified list
     * @param guard the tuple's guard (<code>null</code> means TRUE)
     * @param domain the tuple's domain (necessary only if guard is <code>null</code>)
     */
    Tuple (Guard filter, Domain codomain, List<? extends SetFunction> list, Guard guard, Domain domain) {
        this(filter, codomain, Util.sortedmapFeatureToList(list, ClassFunction::getSort), guard, domain);
    }
    
     //Builder methods (some are private)
    
    /**
     * preserves the reduce_guard flag's value of <tt>this</tt> (new)
     */
     private Tuple build (Guard f, Domain cd, SortedMap<ColorClass, List<? extends SetFunction>> m, Guard g, Domain d) {
        Tuple t  = new Tuple(f, cd, m, g, d);
        t.reduce_guard = this.reduce_guard; //new!
        
        return t;
     }
    /**
     * builds a tuple with the same (co-)domain as <code>this</code> tuple
     * it doesn't perform any consistency check
     * @param filter the possibly null tuple'fc filter
     * @param list the tuple's components
     * @param guard the possibly null tuple's guard
     * @return a tuple with the same (co-)domain as <code>this</code> tuple
     */
    private Tuple build (Guard filter, List<? extends SetFunction> list, Guard guard) {
        Tuple tuple = new Tuple(filter, getCodomain(), list, guard, getDomain());
        tuple.reduce_guard = this.reduce_guard; //new!
        
        return tuple;
    }
    
    /**
     * builds a tuple with the same guard and filter as <code>this</code> tuple
     * and with the specified components
     * @param list  the tuple'fc components
     * @return  a tuple with the same guard and filter as <code>this</code> tuple
     */
    private Tuple build (List<? extends SetFunction> list) {
        return build( filter(), list, guard());
    }
    /**
     * builds a tuple with the same (co-)domain, and with the same
     * components as <code>this</code> tuple, but for those of the specified cc_low_case, that are given
 it doesn't perform any consistency check
     * @param filter the possibly null tuple's filter
     * @param cc a tuple cc_low_case class
     * @param list the tuple's components (assumed) of cc_low_case @param{cc_name}
     * @param guard the possibly null tuple's guard
     * @return a tuple with the same (co-)domain, and with the same
     * components as <code>this</code> tuple, but for those of the specified cc_low_case, that are given:
 <code>null</code> if @param{cc_name} doesn' appear in the tuple's codomain
     */
    private Tuple build (Guard filter, ColorClass cc, List<? extends SetFunction> list, Guard guard) {
        if (getHomSubTuple(cc) != null) {
            SortedMap<ColorClass, List<? extends SetFunction>> map = new TreeMap<>(getHomSubTuples());
            map.put(cc, list);
            
            return build (filter, getCodomain(), map, guard, getDomain());
        }
        
        return null;
    }
   
     /**
      * builder method:
     * builds a tuple with the same components as <code>this</code> tuple
     * it performs no consistency check 
     * @param filter the possibly null tuple's filter
     * @param guard the (possibly null) tuple's guard
     * @param domain the (possibly null) tuple's domain
     * @return a tuple with the same (co-)domain as <code>this</code> tuple
     */
    @Override
    public Tuple build(Guard filter, Guard guard, Domain domain) {
        Tuple tuple = new Tuple (filter, getCodomain(), getHomSubTuples(), guard, domain);
        tuple.reduce_guard = this.reduce_guard; //new!
        
        return tuple;
    }
    
     /**
     * builds a tuple with the same (co-)domain as <code>this</code> tuple (defined just for convenience)
     * it performs no consistency check 
     * @param filter the possibly null tuple'fc filter
     * @param map the tuple'fc components map
     * @param guard the possibly null tuple'fc guard
     * @return a tuple with the same (co-)domain as <code>this</code> tuple
     */
    private Tuple build(Guard filter, Guard guard) {
        return build(filter, guard, getDomain());
    }
        
    /**
     * set the reduce_guard flag (used to mark a left tuple-operand in composition)
     * @param flag the flag's value
     */
    public void setReduceGuard(boolean flag) {
        this.reduce_guard = flag;
    }
    
    /**
     * checks whether the "null bound" for the filter of <code>this</code> tuple
       is exceeded or not: chromatic numbers and tuple components (representing variable domains)
       are considered
     * @return <\code>true<\code> if the filter of this tuple (assumed an "elementary And form")
     * is unsatisfiable due to the
     * cardinality of tuple-components; in particular, if the (union) of variable domains
     * cannot be computed then <\code>false<\code> is returned
     * NOTE: assumes that the filter has been reduced
     */
    private boolean checkNullBound () {
        Guard f = filter();
        if (f instanceof Equality ) {
            Equality e = (Equality) f;
            
            return !((Equality) f).sign() && getComponent(e.firstIndex(), e.getSort()).cardLeq1();
        }
        for (Map.Entry<Color, InequalityGraph> e : ((And)f).igraph().entrySet()) {
            ColorClass cc = (ColorClass) e.getKey(); // the cast is safe ...because is a filter
            if (! cc.unbounded() ) { //may be we can k to ordered classes ..
                InequalityGraph g  = e.getValue();
                Interval  domcard  = g.ineqDomainCard( getHomSubTuple(cc) );
                if ( domcard == null) {
                    //System.out.println("cardinalty of union of "+t + ": null");
                    domcard = cc.card(); // if domcard cannot be computed we set it as the "biggest"
                }
                if ( g.chromaticNumber() > domcard.ub() ) { 
                   //System.out.println("tuple's filter unsatisfiable due to the cardinality of components"); //debug
                   return true;
                }
            }
        }
        
        return false;
    }
    
    /** 
     * when iteratively applied, it brings a tuple into a sum
     * of  tuples without inner "sums" and without "OR" predicates
     * (e.eg.,  &lang;X_1+X_2,tS&rang; &rarr; &lang;X_1,tS&rang; + &lang;X_2,tS&rang;)
     * @return an equivalent sum of tuples; <tt>this</tt> if no inner sums/"OR" filters are present
     * non recursive implementation (one expansion step) not ensuring disjointness */
    public FunctionTuple toEquivSimpleSum() {
        final Set<FunctionTuple> tuples = new HashSet<>();
        List<? extends SetFunction> mycomps = getComponents(), head, tail;
        boolean disjoint = false;
        int or_index = Util.indexOf(mycomps, Union.class);
        if (or_index >= 0) {    
            Union un = (Union) mycomps.get(or_index);
            head = mycomps.subList(0, or_index); 
            tail = mycomps.subList(or_index + 1, mycomps.size()); 
            for (SetFunction t : un.getArgs()) {
                List<SetFunction> new_arg_list = new ArrayList<>();
                new_arg_list.addAll(head);
                new_arg_list.add((SetFunction) t);
                new_arg_list.addAll(tail);
                tuples.add( build(new_arg_list) );
            }
            disjoint = un.disjoined();
        } else if ( filter() instanceof Or) {
            Or f = (Or) filter();
            f.getArgs().forEach((fx) -> { tuples.add( build(fx, mycomps, guard()) ); });
            disjoint = f.disjoined();
            //System.out.println("tuple expansion -> " + tuples); //debug
        } else if ( guard() instanceof Or) {
            Or f = (Or) guard();
            f.getArgs().forEach(gx -> { tuples.add( build ( filter(), mycomps, gx)); });
            disjoint = f.disjoined();
        } 

        return tuples.isEmpty() ? this : TupleSum.factory(tuples, disjoint );
    }    
        
    
    @Override
    public FunctionTuple specSimplify( ) {
        if (isTrue())
            return getTrue(); //new

        Guard simp_f = filter(), simp_g = guard();
        if (simp_f != null) {
            if ( (simp_f = (Guard) simp_f.normalize()) .isFalse() )
                return getFalse();

            simp_f.setAsFilter( getHomSubTuples() );
        }

        if (simp_g != null && (simp_g = (Guard) simp_g.normalize()). isFalse()) 
            return getFalse();

        boolean changed = false;
        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = And.equalityMap(simp_g);
        SortedMap<ColorClass, List<SetFunction>> tuplecopy = new TreeMap<>(); //si potrebbe ottimizzare
        for (Map.Entry<ColorClass, List<? extends SetFunction>> x: getHomSubTuples().entrySet()) {
            ColorClass c = x.getKey();
            ArrayList<SetFunction> args_c = new ArrayList<>(x.getValue());
            if (Expressions.normalize(args_c))
                changed = true;
            if (Util.checkAny( args_c, f -> f instanceof Empty )) 
                return getFalse();

            for (Equality eq: equalityMap.getOrDefault(c, Collections.emptyMap()).getOrDefault(true, Collections.emptySortedSet()))
                if (ClassFunction.replace(args_c, eq)) 
                   changed = true;

            tuplecopy.put(c, args_c);
        }

        if (changed) // some  tuple component have been reduced ..
            return build(simp_f, getCodomain(), Collections.unmodifiableSortedMap(tuplecopy), simp_g, getDomain());

        if (! (Objects.equals(simp_f ,filter()) && Objects.equals(simp_g, guard() )) ) // the filter or the guard have been reduced ..
            return build(simp_f,simp_g); //optimization (more efficient than previous bild ..)

        // no reduction carried out on the filter/guard/components of this tuple
        FunctionTuple res = toEquivSimpleSum() /*this*/; 
        if ( res  != this) //the tuple is expanded (because it contains "OR" elements)
            return res;
        // the tuple doesn't contain "OR" elements, neither in filters nor in its components
        // no reduction/replacement carried out on the filter/guard/components of this tuple
        if ( !this.reduce_guard && (res = TupleSum.factory(toConstSizeSum(equalityMap, And.membMap(simp_g) ), true ) ) != this ) { // questa semplificazione puÃ² essere critica come efficienza
            //System.out.println("--->\n"+res); //debug
            return res;
        }
        for (List<? extends SetFunction> args : getHomSubTuples().values())
            if ( args.stream().anyMatch( f -> f instanceof Intersection && f.zeroCard()) ) 
                return getFalse(); // ha senso qui e non prima perchÃ¨ viene dopo toConstSizeForm ...

        if ( simp_f != null && simp_f.isElemAndForm() ) { 
            equalityMap = And.equalityMap(simp_f);
            if ((res = reduceFilter(equalityMap, And.membMap(simp_f)) ) != this) {
                //System.out.println("->\n"+res); //debug
                return res;
            }
            //the filter is already reduced
            if ( checkNullBound() ) 
               return  getFalse(); 

            return reduceFilterIneqs(equalityMap); // puÃ² essere critica come efficienza
        }
        
        
        return this;
    }
    
           
    /** a sufficient condition for this tuple to map always different from 0
     * @return  <code>true</code> if <code>this</code> is shown to be different fromo the empty function
     */
    @Override
    public boolean differentFromZero() {
       return hasTrivialFilters() && SetFunction.differentFromZero(getComponents());
    }
    
    /** @return the split-delimiters map of <code>this</code> tuple, separately considering
  (in the order) the filters and the tuple'fc components
     */
    @Override
    public Map<Sort, Integer> splitDelimiters() {
        Guard g = guard(), f = filter();
        Map<Sort,Integer>  delims = g != null ? g.splitDelimiters() : new HashMap<>();
        if ( delims.isEmpty()) {
            if (f != null && ! f.isConstant() ) 
                delims = f.splitDelimiters();
            if (delims.isEmpty()) { //tuple components are considered
                Map<ColorClass, List<? extends SetFunction>> m = getHomSubTuples();
                for ( Map.Entry<ColorClass, List<? extends SetFunction>> x : m.entrySet()) 
                    ColorClass.setDelim(delims, x.getKey(), ClassFunction.splitDelim(x.getValue(), x.getKey()));
            }
        }
        //System.out.println("split delimiters di "+this+ " "+delims); //debug
        return delims;
    }
           
    @Override
    public ParametricExpr clone(Domain newdom, Domain newcd) {
        Guard c_g  =  guard(), c_f = filter();
        if (c_g != null)
            c_g  = (Guard) c_g.clone(newdom, null);
        if (c_f != null)
            c_f  = (Guard) c_f.clone(newcd, null);
        
        TreeMap<ColorClass, List<? extends SetFunction> > m = new TreeMap<>();
        //inefficiente,va sistemato!
        getHomSubTuples().entrySet().forEach( x -> {
            ColorClass cc = x.getKey();
            if (newcd.mult(cc) != 0) //color cc is present in newcd
                m.put(cc, x.getValue());
            else {
                ColorClass newcc = (ColorClass)newcd.sort(cc.name());
                m.put(newcc, (List<? extends SetFunction>) ClassFunction.copy(x.getValue(),newcc));
            }
        });
        
        return new Tuple (c_f, newcd, m, c_g, newdom);
    }
     

    @Override
    public boolean isTrue() {
        return guard() == null &&  filter() == null && Util.checkAll(getComponents(), All.class::isInstance) ;
    }
    
    @Override
    public boolean isFalse() {
        return guard() instanceof False || filter() instanceof False || Util.find(getComponents(), Empty.class) != null;
    }
    
    /** 
     * @return <tt>true</tt> if and only if <tt>this</tt> tuple contains a componen
     * of card zero (should be invoked once the tuple is in a right-composable form)
     */
    public boolean zeroCard() {
        return getComponents().stream().map(f -> f.card()).anyMatch(card -> card != null && card.ub() == 0);
    }
    
    
    /**
     * perform the composition of a list of (left) class-functions with a right one
     * @return the list of composition results 
     */
     private static List<SetFunction> getCompositions(List<? extends SetFunction> list, SetFunction right) {
        List<SetFunction> newcomps =  new ArrayList<>(); // the resulting tuple components
        list.forEach((sx) -> { newcomps.add(new ClassComposition(sx, right).specSimplify()); });
            
        return newcomps;
     }
            
    
    /** checks whether a list of class-functions, assumed homogeneous and single-index (or constant),
        is "left-composable", i.e., it is formed exclusively by either elementary functions or pure
        extended projection compl.; further, maps the different repeated projection-based functions;
        check results are stored in two maps passed as parameters
        @return <tt>true</tt> if and only id the list is a left-composable form
     */
    private static boolean checkLeftCompForm(Map<Integer, Integer> projections, Map<Integer, Collection<Integer>> complements, List<? extends SetFunction> list) {
        for (int i = 0 ; i < list.size() ; ++i ) {
            SetFunction f = list.get(i); 
            if ( f instanceof ProjectionBased)  {
                ProjectionBased p = (ProjectionBased) f;
                if (f instanceof Projection) 
                    projections.put(i+1, p.getSucc());
                else 
                    complements.put(i+1, Collections.singleton(p.getSucc())); 
            }
            else if ( f instanceof Intersection )  {
                Set<Integer> i_set = ((Intersection) f).extendedComplSucc();
                if (i_set.isEmpty()) 
                    return false;
                
                complements.put(i+1, i_set);
            }
            else if ( f instanceof NonTerminal  ) 
                return false;
        }
        
        return true;
    }
    
    
     /** 
     brings this tuple (assumed normalized) to an equivalent "index-separated" map of tuples, logically corresponding
     to a tuples' intersection-form, which each tuple contains at most one projection index
     (e.eg.,  <code>&lang;X_1 \cap X_2,X_2&rang; &rarr; {1, &lang;X_1,tS&rang;} , [2, &lang;X_2,X_2&rang;)}</code>;
     possible constant factors of inner intersections are preliminarily separated; 
     intersection operands are checked to contain only single-index separated components, otherwise the method returns null;
     be careful! the original filter is put in the resulting tuples, the guard is ignored!
     @return an "equivalent" map of indexes to corresponding single-index tuples (factors of an intersection-form);
     0 is mapped to a constant factor, if there is any; an empty map, if the tuple requires some further reduction;
     */
    public Map<Tuple,Integer> toIndexSeparatedMap() {
        Tuple[] tuples = constantsSeparatedForm(); // the constants in innner intersections are separated
        Set<? extends Integer> t_idx_set = ClassFunction.indexSet(tuples[0].getComponents()); // tuple'fc or_index set (possibly empty)
        Map<Tuple,Integer> tuple_map = new HashMap<>();
        int size = t_idx_set.size();
        if (size <= 1) { // optimization: either constant or single-indexed tuple
            if (tuples.length == 1) 
                tuple_map.put(tuples[0], size == 0 ? 0 : t_idx_set.iterator().next());
            else {
                tuple_map.put(tuples[0], t_idx_set.iterator().next());
                tuple_map.put(tuples[1], 0);
            }
            
            return tuple_map;
        }
        
        for (int k : t_idx_set) { // for each tuple's index, a k-homogeneous tuple is built
            List<SetFunction> hom_list = new ArrayList<>();
            for (SetFunction f : tuples[0].getComponents() ) { // in tuple tuples[0] inner intersections do not contain constants
                Set<? extends Integer> f_idx_set = f.indexSet();
                size = f_idx_set.size(); 
                if ( size < 2) 
                    hom_list.add(size == 0 || f_idx_set.contains(k) ? f : f.getTrue() );
                else if (f instanceof Intersection ) {
                    Set<SetFunction> ck = ((Intersection)f). getComponents(k);
                    hom_list.add(ck.isEmpty() ? f.getTrue() : Intersection.factory(ck) );  
                }
                else 
                    return Collections.EMPTY_MAP;  // f is an operator with multiple indices
            }
            tuple_map.put(build (filter(), hom_list, null) , k );
        }
        if (tuples.length == 2)
            tuple_map.put(tuples[1], 0); //constant tuple
        
        return tuple_map;
    }
    
    /** 
     * brings this tuple to an equivalent tuples' (intersection-)form, T_1 \cap T_2, encoded as a k-two array,
     * in which tuple T_2 contains constant factors of inner intersections of this tuple, that are separated in a consistent way
     * (e.eg.,  <X_1 \cap X_2 \cap S_{1,2},X_2&rang; &rarr; <X_1 \cap X_2,X_2&rang; \cap <S_{1,2}, S&rang;);
     * be careful! the original filter is put in the resulting tuples, the guard is ignored 
     * @return an equivalent tuple'fc array (holding the tuples of a tuples' intersection-form) in which constant factors
     * of inner intersections are separated; return {this} if in this tuple there are no inner intersections with constant factors
     * (in particular, if this is a constant tuple!)
     */
    private Tuple[] constantsSeparatedForm() {
        boolean found = false;
        List<SetFunction> tuple1_args = new ArrayList<>(), const_tuple_args = new ArrayList<>(); // the resulting tuples' operands
        for (SetFunction f : getComponents() ) {
            Set<ConstantFunction> sc;
            Intersection in;
            if ( f instanceof Intersection && ! (sc = Util.getType( (in = (Intersection)f).getArgs(), ConstantFunction.class ) ).isEmpty()) {
                found = true;
                HashSet<SetFunction> f_args = new HashSet<>(in.getArgs());
                f_args.removeAll(sc);
                tuple1_args.add(Intersection.factory(f_args));
                const_tuple_args.add(Intersection.factory(sc));
            }
            else {
                tuple1_args.add(f);
                const_tuple_args.add(f.getTrue());
            }
        }
        
        return found ?  new Tuple [] { build( filter(), tuple1_args, null), build (filter(), const_tuple_args, null)}
                : new Tuple [] {this};
    }
         
      /** 
     * brings this tuple to an equivalent (disjoint) list (logically corresponding to a sum) of constant-size function-tuples,
     * in which inner intersections are brought into a constant-size form 
     * in the particular cases in which the tuple is already "simple", or all of its components need further reductions,
     * a singleton list is returned; 
     * @return an equivalent list of disjoint, simple tuples; a singleton containing <tt>this</tt> tuple
     * if no expansion has been performed
     */
     private Collection<Tuple> toConstSizeSum(Map<ColorClass, Map<Boolean, SortedSet<Equality>>> eq_map, Map<ColorClass, Map<Boolean, Set<Membership>>> me_map) {
        Set<Tuple> sum = new HashSet<>();
        List< Set<? extends Pair<? extends SetFunction, ? extends Guard> > > list_of_sets = new ArrayList<>();
        boolean expanded = false;
        Domain dom = getDomain();
        for (Map.Entry<ColorClass, List<? extends SetFunction>> x : getHomSubTuples().entrySet()) {
            ColorClass c = x.getKey();
            SortedSet<Equality> ineq_list = eq_map.getOrDefault(c, Collections.emptyMap()).getOrDefault(false,Collections.emptySortedSet());
            Map<Boolean, Set<Membership>> mx = me_map.getOrDefault(c, Collections.emptyMap());
            Map<Projection, Subcl> inmap = Membership.mapSymbolsNoRep(mx.getOrDefault(true, Collections.EMPTY_SET));
            Map<Projection, Set<Subcl>> notinmap = Membership.mapSymbols(mx.getOrDefault(false, Collections.EMPTY_SET));        
            //if (! (ineq_list.isEmpty() && inmap.isEmpty() && notinmap.isEmpty()) ) //possible optimization
            for ( SetFunction f : x.getValue() ) {
                Set<? extends Pair<? extends SetFunction, ? extends Guard>> s = f.toSimpleFunctions(ineq_list , inmap, notinmap, dom);
                list_of_sets.add(s);
                if ( !s.equals(Collections.singleton (new Pair<>(f, null))) )
                    expanded = true;
            }
            //else {}
        }
        
        if (! expanded) 
            sum.add(this); // no expansion needed
        else
            Util.cartesianProd(list_of_sets).forEach(list -> {
                HashSet<Guard> t_guards  =  new HashSet<>();
                Guard g = guard();
                if (g instanceof And)
                    t_guards.addAll( ((And)g). getArgs() );
                else if (g != null )
                    t_guards.add(g);
                List<SetFunction> t_comps = new ArrayList<>();
                for (Pair<? extends SetFunction, ? extends Guard> p : list) {
                    t_comps.add(p.getKey());
                    if (p.getValue() != null) 
                        t_guards.add(p.getValue());
                }
                sum.add( build ( filter(), t_comps, And.buildAndForm(t_guards ) ) );
        });
        
        return sum;
    }
                
     /** 
     * applies to this tuple guard-reduction rules; the guard is expressed through an equality map
     * if the tuple is one sorted, and it contains any clauses that cannot be reduced,
     * then it is "extended" to a tuple of greater size, whose projection is equivalent to the original one;
     * @param cc a cc_low_case-class
     * @param eqmap the equality map
     * @return an equivalent (possibly modulo projection) elementary tuple which comes from applying filter rules;
     * <tt>this</tt>, if the guard is trivial; <tt>null</tt> if the guard is not trivial but the tuple
     * is not elementary and one-sorted
     * The algorithm could be further simplified if equalities were first applied in the tuple
     */
    public FunctionTuple reduceGuard(ColorClass cc, Map<Boolean, SortedSet<Equality>> eqmap) {
        //System.out.println("guard to be \"reduced\":\n" +this); //debug
        List<SetFunction> tuple_args = new ArrayList<>(getHomSubTuple(cc)); //the new list of components
        final int size = tuple_args.size();
        eqmap.entrySet().forEach( mx -> {
            mx.getValue().stream().filter(g -> !g.sameIndex()).forEachOrdered(g -> {
                boolean not_reduced = true; //this flag signals whether eg has been reduced...   
                for (ListIterator<SetFunction> ite = tuple_args.listIterator(); not_reduced && ite.hasNext() ; ){
                    SetFunction f = ite.next(), f_equiv;
                    if ( g.getSort().equals(f. getSort())  && (f_equiv = g.toSetfunction(f) ) != null ) {
                        //System.out.println("f_equiv: "+f_equiv);
                        ite.set(f_equiv); // new
                        not_reduced = false; // ends the inner for: the elementary guard reduction is performed only once ...
                    }
                }  
                if (not_reduced) 
                    tuple_args.add(g.toSetfunction(g.getArg1())); //trucco
            }); 
        });
        //System.out.println("guard \"reduction\" outcome\n" +tuple_args); //debug
        Tuple tupleres;
        SortedMap<ColorClass, List<? extends SetFunction>> singleSortedMap = Util.singleSortedMap(cc, tuple_args);
        if (size == 0 || tuple_args.size() == size) { //size == 0 is new!
            tupleres =  new Tuple (null, getCodomain(), singleSortedMap,  null, getDomain());
            tupleres.setReduceGuard(true);
            
            return tupleres;
        }
        tupleres = new Tuple(null, buildCodomain(tuple_args), singleSortedMap, null, getDomain());
        tupleres.setReduceGuard(true);
            
        return new TupleProjection( tupleres, size);
    }
          
     /**
     * performs the  filter (assumed not null) reduction considering equalities, and inequalities
     * which refer to tuple components (at least one) of cardinality  one;
     * IMPORTANT: should be called on tuples of constant-k (simple) form
     * IMPORTANT: the filter is assumed not null and an elementary and composed of equalities
     * @param equalityMap the filter's pre-computed equality map
     * @param membMap the filter's pre-computed  membership map
     * @return an equivalent reduced tuple, or <tt>this</tt> is no reductions have been done
     * @see reduceFilterIneqs
     */ 
    public FunctionTuple reduceFilter ( Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap, Map<ColorClass, Map<Boolean, Set<Membership>>> membMap) {
        //System.out.println("tupla da ridurre (filtro):\n"+getHomSubTuples()); //debug
        Guard filter = filter();
        Collection<Guard> to_remove = new LinkedList<>();
        SortedMap <ColorClass, List<SetFunction> > tuple_copy = new TreeMap<>(); // a "copy" of this tuple'fc components is built cc_low_case by cc_low_case
        boolean reduced = false;
        ColorClass cc;
        int i;
        List<SetFunction> h_tuple;
        for (Map.Entry<ColorClass, Map<Boolean, Set<Membership>>> mx : membMap.entrySet()) {
            h_tuple = new ArrayList<>( getHomSubTuple( cc= mx.getKey() )); // the sub-tuple of cc_low_case cc_name
            for (Map.Entry<Boolean, Set<Membership>> my : mx.getValue().entrySet())
                for (Membership m : my.getValue()) {
                    h_tuple.set( (i = m.firstIndex()) -1 , Intersection.factory(my.getKey() ? m.subcl() : m.subcl().opposite(), h_tuple.get( i -1 )));
                    to_remove.add(m); // m removed from the filter
                    reduced = true;
                }
            tuple_copy.put(cc, h_tuple);
        }
        for (Map.Entry<ColorClass, Map<Boolean, SortedSet<Equality>>> ex : equalityMap.entrySet()) {
            if ( (h_tuple  = tuple_copy.get( cc = ex.getKey()) ) == null)
                h_tuple = new ArrayList<>(getHomSubTuple(cc));
            for (Map.Entry<Boolean, SortedSet<Equality>> ey : ex.getValue().entrySet())
                for (Equality eq : ey.getValue()) {
                    int j = eq.secondIndex(), exp_diff;
                    SetFunction f_i = h_tuple.get( (i = eq.firstIndex() ) -1 ), f_j = h_tuple.get(j  -1),
                                succ_fi = f_i, succ_fj = f_j, inter;
                    if ( cc.isOrdered() && (exp_diff = eq.getArg2().getSucc() -  eq.getArg1().getSucc() ) != 0) {
                        succ_fi = succ(-exp_diff, f_i); // needed for the comparison below ...
                        succ_fj = succ( exp_diff, f_j);
                    }
                    inter = inter(f_i,succ_fj);
                    Boolean op = ey.getKey();
                    if ( inter. isFalse() ) { // i and j are disjoint components
                       if (op)   //equality
                          return getFalse(); //the empty tuple
                       
                       to_remove.add(eq); // eg removed  
                       reduced=true;
                    }
                    else {
                        boolean fi_card_leq_1 = f_i.cardLeq1(); 
                        if (fi_card_leq_1  ||  f_j.cardLeq1() ) { // the cardLb of f_i or that of f_j less than or equal than one
                            if (fi_card_leq_1) 
                                h_tuple.set(j -1 , op ? inter(f_j,succ_fi) : differ(f_j, succ_fi));
                            else 
                                h_tuple.set(i -1, op ? inter : differ(f_i, succ_fj));
                            to_remove.add(eq); // eg removed
                            reduced=true;
                        }
                        else if (op  && !f_i.equals(succ_fj) ) { // equality and cardinalities of both f_i and f_j greater than one
                            h_tuple.set(i -1, inter);
                            h_tuple.set(j -1, inter(f_j, succ_fi));
                            reduced = true;
                        }
                    }
                }
            tuple_copy.put(cc, h_tuple); // we set the new sub-tuple of cc_low_case cc_name
        }
        
        if (!reduced ) 
            return this;
        
        if (! to_remove.isEmpty() ) {
            Set<Guard> new_args = new HashSet<>( And.getArgs(filter) );
            new_args.removeAll(to_remove);
            filter = And.buildAndForm(new_args , getCodomain());
        }
        
        // remaining sub-tuples of this tuple are added to tuple_copy
        getHomSubTuples().entrySet().forEach(e -> { tuple_copy.putIfAbsent(e.getKey(), (List<SetFunction>) e.getValue()); });
        
        return new Tuple(filter, getCodomain(), Collections.unmodifiableSortedMap(tuple_copy), guard(), getDomain() );  
    }
            
    /** 
     * reduce a filter considering inequalities which refer to tuple components both with card &gt; 1;
     *  <tt>this</tt> tuple is assumed one-sorted (? not necessarily); the pre-computed map of inequalities
     *  in the filter is passed as an argument;
     *  the filter is assumed in turn to be an And form; basic filter reductions should have been
     *  previously done; some optimizations are performed
     *  performs just one reduction step! 
     *  it builds on @see {reduceFilterClassIneqs} 
     *  @param equalityMap the filter's pre-computed  equality map
     *  @return either a sum, corresponding to the expansion of <tt>this</tt> tuple
     *  due to the occurrence of an inequality which refers to "non-equal" (modulo successor)
     *  components, or <tt>this</tt>, if the tuple is a "fixed point"
     */
    public FunctionTuple reduceFilterIneqs(Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap) {
        //System.out.println("reduceFilterIneqs:\n"+this); //debug
        FunctionTuple res = this;
        for (Map.Entry<ColorClass, Map<Boolean, SortedSet<Equality>>> me : equalityMap.entrySet()) {
            ColorClass cc = me.getKey();
            Set<? extends Equality> ineq_set;
            if (  cc.ccSize() != 0 && ((ineq_set = me.getValue().get(false) ) != null) &&
                    (res = reduceFilterClassIneqs(ineq_set, cc)) != this)
                break;
        }
  
        return res;
    }
    /** 
     *  reduce a filter considering inequalities of a given cc_low_case class which refer to tuple components both with card &gt; 1;
     *  the pre-computed set of inequalities in the filter, assumed in turn to be an And form, is passed as an argument;
     *  performs just one reduction step! 
     *  @param ineq_set the filter's pre-computed inequality set
     *  @param cc the inequalities' cc_low_case-class
     *  @return either a sum, corresponding to the expansion of <tt>this</tt> tuple
     *  due to the occurrence of an inequality which refers to "non-equal" (modulo successor)components,
     *  or <tt>this</tt>
     */
    public FunctionTuple reduceFilterClassIneqs(final Set<? extends Equality> ineq_set, final ColorClass cc) {
        //System.out.println("reduceFilterClassIneqs:\n"+this+" , "+ineq_set); //debug
        for (Equality ineq : ineq_set) {    
            int i = ineq.firstIndex() , j , succ_diff;
            SetFunction f_i = getComponent(i , cc), f_j = getComponent(j = ineq.secondIndex() , cc),
                        succ_fi = f_i, succ_fj = f_j, diff ;
            Interval fi_card = f_i.card(), fj_card ;
            if (fi_card != null && fi_card.lb() > 1 && (fj_card = f_j.card()) != null && fj_card.lb() > 1) {
                if ( cc.isOrdered() && (succ_diff = ineq.getArg2().getSucc() -  ineq.getArg1().getSucc()) != 0) {
                    succ_fi = succ(-succ_diff,f_i); // needed for the comparison below ...
                    succ_fj = succ(succ_diff,f_j);
                }
                if ( ! f_i.equals(succ_fj) ) {
                    //System.out.println("filter reduction:\n"+this); //deb
                    Set<FunctionTuple> expansion = new HashSet<>(); // contains the result, expressed as a sum
                    Set<Guard> newargs = new HashSet<>(And.getArgs(filter()));
                    newargs.remove(ineq);
                    Guard newfilter = And.buildAndForm(newargs), guard = guard();
                    if ( !(diff = differ(f_i, succ_fj)).isFalse() ) {
                        List<SetFunction>  t1  = new ArrayList<>( getHomSubTuple(cc));
                        t1.set(i - 1 ,diff);
                        t1.set(j - 1, f_j); 
                        expansion.add(build(newfilter, cc, t1, guard) );
                    }
                    if (! (diff = differ(f_j, succ_fi)).isFalse()) {
                        List<SetFunction>   t2 = new ArrayList<>( getHomSubTuple(cc));
                        t2.set(j - 1, diff);
                        expansion.add(build (newfilter, cc, t2, guard) );
                    }
                    List<SetFunction> t3 = new ArrayList<>( getHomSubTuple(cc) ); //a copy of list'fc components
                    t3.set(i - 1, inter(f_i,succ_fj));
                    t3.set(j - 1, inter(f_j,succ_fi));
                    expansion.add(build (filter(), cc, t3, guard) );
                    //System.out.println("Tuple.reduce ineqs: -> " +c); //debug
                    return TupleSum.factory(expansion, true); // disjoined form   
                }
            }
        }
        return this;
    }
    
    private static SetFunction inter (SetFunction f1, SetFunction f2) {
        return (SetFunction) f1.andFactory(f1,f2). normalize();
    }
    
    private static SetFunction differ (SetFunction f1, SetFunction f2) {
        return (SetFunction) f1.diff(f2). normalize( );
    }
    
    private static SetFunction succ (int k, SetFunction f) {
        return (SetFunction) Successor.factory(k,f). normalize();
    }
    
    /*@Override
    public FunctionTupleBag buildBagComp(SetExpr right) {
        if ( ! (right instanceof Tuple || right instanceof AllTuple ) ) 
            return null;
        
        Tuple rt =  right instanceof AllTuple ? ((AllTuple) right).asTuple() : (Tuple) right , t;
        Guard guard , filter = filter() ;
        if (  filter != null )   
            return (t = withoutFilter()). buildBagComp(rt).applyFilter(filter);
        // the external filter is trivial
        guard = guard(); filter = rt.filter() ;
        SortedMap<ColorClass, List<? extends SetFunction>> my_parts = getHomSubTuples();
        int left_parts = my_parts.size(); // the number of parts of this
        List<BagExpr<FunctionTuple>> compositions;
        if (guard == null && filter == null) { // base case: there is no inner filter
             if (left_parts < 2) 
                 return onesortedTupleComposeAsBag(rt); 

             compositions  = new ArrayList<>();
             my_parts.entrySet().forEach( h_part -> {
                 ColorClass c = h_part.getKey();
                 List<? extends SetFunction> list = h_part.getValue();
                 compositions.add(new SetComp( new Tuple (null, new Domain(c , list.size()), Util.singleSortedMap(c,list), null, getDomain()),  rt));
            });
             
             return TupleBagJuxtaposition.factory(compositions);
        }
        // the inner filter is not trivial (!= null)
        //System.out.println("left:\n"+this+"\nright:\n"+rt); //debug
        if (guard != null)  // we move the left's tuple guard ... to the right
            return new TupleComposition( withoutGuard(), rt.build(filter != null ? (Guard) And.factory(filter, guard) : guard, rt.guard()));
        //the right function's filter cannot be "absorbed" : we try to reduce it by possibly "expanding" the left tuple
        //either the filter is fully absorbed or the left tuple is expanded .. once it has been split in one-sorted parts...
        Tuple right_nof = rt.withoutFilter();
        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> hom_filters = And.equalityMap(filter); //we assume that in the filter there are just (in)equalities!!
        compositions = new LinkedList<>();
        hom_filters.entrySet().forEach(entry -> { 
            compositions.add(new TupleComposition(reduceGuard(entry.getKey(), entry.getValue()), right_nof) );
        });
        // residual sub-tuples not having any associated filter ....
        my_parts.keySet().forEach( col -> {
            if (hom_filters.get(col) == null) {
                List<? extends SetFunction> st = my_parts.get(col); // the sub-tuple of cc_low_case cc_name
                compositions.add(new TupleComposition(new Tuple (null, new Domain(col,st.size()),  Util.singleSortedMap(col, st) , null, getDomain()), right_nof));
            }
        });
        //System.out.println("basecompose ->\n"+res); //debug
        return TupleBagJuxtaposition.factory(compositions);
    }
    
    public FunctionTupleBag onesortedTupleComposeAsBag (Tuple right) {
       //System.out.println("onesortedTupleCompose: "+Expressions.toStringDetailed(this)+" . "+right); //debug
        if ( ! SetFunction.differentFromZero(right.getComponents() ) ) 
               return null; // right tuple'e components don't have a fixed card ...
        
        ColorClass cc = getSort();
        List<FunctionTuple> basic_comps = new ArrayList<>(); // the list of basic compositions    
        boolean succeded = false;
        for (Map.Entry<Tuple, Integer> ht : toIndexSeparatedMap().entrySet() ) {
            Tuple t = ht.getKey();
            int i = ht.getValue(); // subtuple's index
            FunctionTuple ft;
            if (i == 0) { // t is constant 
                ft = new Tuple(null, t.getCodomain(), t.getHomSubTuples(), right.guard(), right.getDomain());
                succeded = true; //new!
            }
            else {
                SetFunction f_i = right.getComponent(i,cc);
                if ( (ft = t.tupleBaseCompose(f_i, right.guard(), right.getDomain() )) != null) 
                    succeded = true;
                else {
                    List<? extends SetFunction> comps = t.getComponents();
                    if (i != 1)
                        comps = ClassFunction.setIndex(1, comps);
                    Tuple l = new Tuple(null, getCodomain(), Util.singleSortedMap(cc, comps) , null, new Domain(cc)), //a copy of this tuple with index 1
                          r = new Tuple(f_i, right.guard(), right.getDomain());
                    r.setSimplified(true); //optimizations
                    ft = new TupleComposition(l, r);
                }
            }
            basic_comps.add(ft); 
        }
        //System.out.println("onesortedTupleCompose: -> \n"+basic_comps); //debug
        return succeded ? TupleIntersection.factory(basic_comps) : null; //this way tuples' merging is forced (necessary when there are projections)
    }*/
   
    
    /** 
     * core of the function-tuples' composition algorithm:
     * the cases in which the right function is a (guarded) tuple are treated;
     * IMPORTANT: assumes that both the left and the right tuples have already been simplified
    *  @return the composition between <code>this</code> tuple and the <code>right</code> tuple;
     * <code>null</code> if either <code>right</code> is not a <code>Tuple</code> or some further reduction is required
     * @throws wncalculus.expr.IllegalDomain if <tt>this</tt> function's domain and <tt>right</tt>'s codomain
     * are different
     */
    @Override
    public FunctionTuple baseCompose (FunctionTuple right)  {
        //System.out.println("baseCompose:\n"+/*Expressions.toStringDetailed(*/this/*)*/+" . "+right); //debug
        if ( ! (right instanceof Tuple || right instanceof AllTuple ) ) 
            return null;
        
        Tuple rt =  right instanceof AllTuple ? ((AllTuple) right).asTuple() : (Tuple) right ;
        if (  filter() != null )   
            return new FilteredTuple(  filter(), new TupleComposition( withoutFilter(), rt));
        // the external filter is trivial
        Guard guard = guard(), filter =rt.filter() ;
        SortedMap<ColorClass, List<? extends SetFunction>> my_parts = getHomSubTuples();
        int left_parts = my_parts.size(); // the number of parts of this
        if (guard == null && filter == null) { // base case: there is no inner filter
             if (left_parts < 2) 
                 return onesortedTupleCompose(rt); 

             List<FunctionTuple> compositions  = new ArrayList<>();
             my_parts.entrySet().forEach( h_part -> {
                 ColorClass c = h_part.getKey();
                 List<? extends SetFunction> list = h_part.getValue();
                 compositions.add(new TupleComposition( new Tuple (null, new Domain(c , list.size()), Util.singleSortedMap(c,list), null, getDomain()),  rt));
            });
             
             return TupleJuxtaposition.factory(compositions);
        }
        // the inner filter is not trivial (!= null)
        //System.out.println("left:\n"+this+"\nright:\n"+rt); //debug
        if (guard != null)  // we move the left's tuple guard ... to the right
            return new TupleComposition( withoutGuard(), rt.build(filter != null ? (Guard) And.factory(filter, guard) : guard, rt.guard()));
        //the right function's filter cannot be "absorbed" : we try to reduce it by possibly "expanding" the left tuple
        //either the filter is fully absorbed or the left tuple is expanded .. once it has been split in one-sorted parts...
        Tuple rt_nof = rt.withoutFilter();
        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> hom_filters = And.equalityMap(filter); //we assume that in the filter there are just (in)equalities!!
        List<FunctionTuple> compositions = new LinkedList<>();
        hom_filters.entrySet().forEach(entry -> { 
            compositions.add(new TupleComposition(reduceGuard(entry.getKey(), entry.getValue()), rt_nof) );
        });
        // residual sub-tuples not having any associated filter ....
        my_parts.keySet().forEach( col -> {
            if (hom_filters.get(col) == null) {
                List<? extends SetFunction> st = my_parts.get(col); // the sub-tuple of cc_low_case cc_name
                compositions.add(new TupleComposition(new Tuple (null, new Domain(col,st.size()),  Util.singleSortedMap(col, st) , null, getDomain()), rt_nof));
            }
        });
        //System.out.println("basecompose ->\n"+res); //debug
        return TupleJuxtaposition.factory(compositions);
    }
    
    //pressochè equivalente a baseCompose solo che come parametro ha una tupla
    public FunctionTuple compose (final Tuple right)  {
        //System.out.println("baseCompose:\n"+/*Expressions.toStringDetailed(*/this/*)*/+" . "+right); //debug
        if (  filter() != null )   
            return new FilteredTuple(  filter(), new TupleComposition( withoutFilter(), right));
        // the external filter is trivial
        Guard guard = guard(), filter =right.filter() ;
        SortedMap<ColorClass, List<? extends SetFunction>> my_parts = getHomSubTuples();
        int left_parts = my_parts.size(); // the number of parts of this
        if (guard == null && filter == null) { // base case: there is no inner filter
             if (left_parts < 2) 
                 return onesortedTupleCompose(right); //may return null 

             List<FunctionTuple> compositions  = new ArrayList<>();
             my_parts.entrySet().forEach( h_part -> {
                 ColorClass c = h_part.getKey();
                 List<? extends SetFunction> list = h_part.getValue();
                 compositions.add(new TupleComposition( new Tuple (null, new Domain(c , list.size()), Util.singleSortedMap(c,list), null, getDomain()),  right));
            });
             
             return TupleJuxtaposition.factory(compositions);
        }
        // the inner filter is not trivial (!= null)
        //System.out.println("left:\n"+this+"\nright:\n"+rt); //debug
        if (guard != null)  // we move the left's tuple guard ... to the right
            return new TupleComposition( withoutGuard(), right.build(filter != null ? (Guard) And.factory(filter, guard) : guard, right.guard()));
        //the right function's filter cannot be "absorbed" : we try to reduce it by possibly "expanding" the left tuple
        //either the filter is fully absorbed or the left tuple is expanded .. once it has been split in one-sorted parts...
        Tuple right_nof = right.withoutFilter();
        Map<ColorClass, Map<Boolean, SortedSet<Equality>>> hom_filters = And.equalityMap(filter); //we assume that in the filter there are just (in)equalities!!
        List<FunctionTuple> compositions = new LinkedList<>();
        hom_filters.entrySet().forEach(entry -> { 
            compositions.add(new TupleComposition(reduceGuard(entry.getKey(), entry.getValue()), right_nof) );
        });
        // residual sub-tuples not having any associated filter ....
        my_parts.keySet().forEach(col -> {
            if (hom_filters.get(col) == null) {
                List<? extends SetFunction> st = my_parts.get(col); // the sub-tuple of cc_low_case cc_name
                compositions.add(new TupleComposition(new Tuple (null, new Domain(col,st.size()),  Util.singleSortedMap(col, st) , null, getDomain()), right_nof));
            }
        });
        //System.out.println("basecompose ->\n"+res); //debug
        return TupleJuxtaposition.factory(compositions);
    }
            
    
    /** *  performs the composition between this tuple (the left one, assumed one-sorted), 
        and the specified right tuple (assuming that both inner and outside filters -not the guard- are trivial),
        after checking that the right tuple is either "empty" or in a composable form (all of tuple'fc components
        have (fixed) cardLb &gt; 0, but for cc_low_case-argument making the tuple null..)
        in order to work correctly, it should be called after toRightComposableForm() (and a renaming of equal proj.)
        @param right the tuple to be right-composed with this
        @return the function-tuple resulting from composition; <tt>null</tt>
        if the right tuple is not empty and have any components with not fixed card
     */
    public FunctionTuple onesortedTupleCompose (Tuple right) {
       //System.out.println("onesortedTupleCompose: "+Expressions.toStringDetailed(this)+" . "+right); //debug
        if ( SetFunction.differentFromZero(right.getComponents() ) ) { // right tuple'e components don't have a fixed card ...
            boolean succeded = false;
            ColorClass cc = getSort();
            List<FunctionTuple> basic_comps = new ArrayList<>(); // the list of basic compositions    
            for (Map.Entry<Tuple, Integer> ht : toIndexSeparatedMap().entrySet() ) {
                Tuple t = ht.getKey();
                int i = ht.getValue(); // subtuple's index
                FunctionTuple ft;
                if (i == 0) { // t is constant 
                    ft = new Tuple(null, t.getCodomain(), t.getHomSubTuples(), right.guard(), right.getDomain());
                    succeded = true; //new!
                }
                else {
                    SetFunction f_i = right.getComponent(i,cc);
                    if ( (ft = t.tupleBaseCompose(f_i, right.guard(), right.getDomain() )) != null) 
                        succeded = true;
                    else {
                        List<? extends SetFunction> comps = t.getComponents();
                        if (i != 1)
                            comps = ClassFunction.setDefaultIndex( comps );
                        Tuple l = new Tuple(null, getCodomain(), Util.singleSortedMap(cc, comps) , null, new Domain(cc)), //a copy of this tuple with index 1
                              r = new Tuple(f_i, right.guard(), right.getDomain());
                        r.setSimplified(true); //optimizations
                        ft = new TupleComposition(l, r);
                    }
                }
                basic_comps.add(ft); 
            }
            //System.out.println("onesortedTupleCompose: -> \n"+basic_comps); //debug
            if (succeded) 
                return TupleIntersection.factory(basic_comps); //this way tuples' merging is forced (necessary when there are projections)
            }
  
        return null;
    }
        
    
    /**
     * performs a basic composition between a tuple and a class-function (without considering tuple's filter)
     * the left (i.e., <code>this</code>) tuple is assumed one-sorted and single-index (no matter which one)
     * the right tuple is assumed elementary (i.e., of one elemeent) and in a composable (i.e., constant k) form;
     * (new version)
     * @param rx the classfunction of the right-most tuple referred to by the left-tuple
     * @param rg the right tuple's guard
     * @param rd the right tuple's domain
     * @return the (possibly filtered!) tuple resulting from the composition;
    */
    public FunctionTuple tupleBaseCompose (SetFunction rx, Guard rg, Domain rd)  {
        ColorClass cc = getSort(); // left tuple'fc colour
        List<? extends SetFunction> lcomps = getHomSubTuple(cc); // left-tuple'fc components
        Domain cd = getCodomain();
        Map<Integer, Integer> projections = new HashMap<>(); // stores projection repetitions (postions and succ args)
        Map<Integer, Collection<Integer>> complements = new HashMap<>(); // stores complements repetitions 
        checkLeftCompForm(projections, complements, lcomps );
        int npr = projections.size(), ncmp = complements.size();
        if (npr + ncmp < 2 || rx.card().singleValue(1) )  // base case: |rx| == 1 or no repetitions 
            return new Tuple(null, cd, cc, getCompositions(lcomps, rx),  rg, rd);
        //System.out.println("tupleBaseCompose:\nleft"+this+"\nright"+right); //debug 
        // the right function's card is > 1 and there are repetitions of X^1 on the left tuple
        List<? extends SetFunction> newcomps = lcomps; // left-tuple components (possibly extended with an ending proj)
        Domain ncd = npr != 0 ? cd : cd.set(cc, cd.mult(cc)+1 );
        if (npr == 0) {
            projections.put(lcomps.size() + 1, 0); // the projection is inserted at position 1 ...
            List<SetFunction> extended = new ArrayList<>(lcomps); // left-tuple copy with additional projection at the end
            extended.add(Projection.builder(1, cc)); //X^1 addded at the end (the index doesn't matter)
            newcomps = extended;
        }
        int i_1, succ_1;
        List<Guard> filters = new ArrayList<>();
        Iterator<Integer> ite = projections.keySet().iterator();
        succ_1 = projections.get( i_1 = ite.next() );
        Projection p_1 = Projection.builder(i_1, cc); // the first X^1 occurring and its position..
        while ( ite.hasNext() ) {
            int i = ite.next();
            filters.add(Equality.builder(p_1, Projection.builder(i, succ_1 -projections.get(i), cc), true, ncd));
        }
        //we build the set of inequalities (it is sufficiente to consider the first projection occurrence..)
        complements.keySet().forEach(j -> {
            complements.get(j).forEach(j_exp -> { 
                filters.add(Equality.builder(p_1, Projection.builder(j, succ_1 - j_exp, cc), false, ncd));
            });
        });
        Tuple t  = new Tuple(And.buildAndForm(filters), ncd, cc, getCompositions(newcomps, rx), rg, rd);
        
        return npr != 0 ? t : new TupleProjection (t, lcomps.size());
    }
            
    /**
     * computes the difference between tuples in an optimized way; the tuples are assumed
 to have sameName (co)domains and already simplified|
     * @param other the tuple to be "subtracted" to this
     * @return the difference between this and other (if it results in an "OR" form then
     * the terms are pairwise disjoint)
     */
    public FunctionTuple diff (Tuple other)  {   
        if ( disjoined(other) )  //optimization 
            return this;     
        
        if ( isTrue() ) 
            return other.complement(); //an optimized ad hoc version is invoked
        
        Guard othf = other.filter(), f_and_1_2 = join(filter() , othf), // the "AND" between tuples' filters
              myg = guard(), othg = other.guard(), g_and_1_2 = join(myg, othg);  // the "AND" between tuples' guards
        List<FunctionTuple> diff_list = new ArrayList<>();
        List<SetFunction> diff_tuple_args, head = new ArrayList<>();
        SetFunction t1_i, t2_i , inter;
        List<? extends SetFunction> comps = getComponents(), others = other.getComponents();
        for (int i = 0, tsize = comps.size(); i < tsize ; i++ , head.add(inter) ) {
            t1_i  = comps.get(i);
            t2_i  = others.get(i); 
            inter = (SetFunction) t1_i.andFactory(t1_i,t2_i). normalize();
            if (inter instanceof Empty) 
                return this; // optimization
            
            if (!inter.equals(t1_i)) { //optimization: t1_i not included in t2_i ...
                diff_tuple_args = new ArrayList<>();
                diff_tuple_args.addAll(head); // the first 1,2,..,i-1 (intersection) components..
                diff_tuple_args.add( differ(t1_i , t2_i )); // f_i is set equal to t1_i - t1_2
                diff_tuple_args.addAll(comps.subList(i+1, tsize)); // the remaining i+1,i+2,.., components
                diff_list.add( build(f_and_1_2, diff_tuple_args, g_and_1_2 ));
            }
        }
        if (othf != null) 
            diff_list.add( build ( subtr(filter(), othf), myg));
        if (othg != null) 
            diff_list.add( build (f_and_1_2, subtr(myg, othg) ));
            
        return diff_list.isEmpty() ? getFalse() : TupleSum.factory(diff_list, true);
    }
        
    
    /** computes the difference tS - this in an optimized way
     * @return  the complement of <code>this</code> tuple
     * VERIFICARE SE SI PUO ULTERIORMENTE OTTIMIZzARE
     */
    public FunctionTuple complement ()  {   
        if ( isTrue()) 
            return getFalse();
        
        if (isFalse()) 
            return getTrue();
        
        Domain cd = getCodomain();
        SortedMap<ColorClass, List<? extends SetFunction>> mS = AllTuple.toMap(cd);
        List<SetFunction> tS = new ArrayList<>();
        mS.values().forEach(tS::addAll);
        Set<FunctionTuple> diff_list = new HashSet<>();
        int tsize = size();
        Guard f2 = filter(), g2 = guard();
        List<? extends SetFunction> comps = getComponents();
        for (int i = 0 ; i < tsize ; i++) {
            SetFunction t_i = comps.get(i);
            if (! t_i.isTrue()) { //optimization: t2_i different from tS...
                List<SetFunction> d_tuple_args = new ArrayList<>();
                d_tuple_args.addAll(comps.subList(0,i)); // the curr_t 1,2,..,i-1 components of the intersection (= other)..
                d_tuple_args.add(Complement.factory(t_i) ); // f_i is set equal to tS - t1_2
                d_tuple_args.addAll(tS.subList(i + 1, tsize)); // the remaining i+1,i+2,.., components
                diff_list.add( build (f2, d_tuple_args, g2));
            }
        }
        if (f2 != null) 
            diff_list.add(new Tuple(Neg.factory(f2), null,  mS, null, getDomain()));
        if (g2 != null) 
            diff_list.add(new Tuple(f2, cd, mS, Neg.factory(g2), null));
        
        return diff_list.isEmpty() ? getFalse() : TupleSum.factory(diff_list, true); //disjoined form
    }
        
    /** infers the co-domain from a list of class-functions */
    private static Domain buildCodomain(List<? extends SetFunction> l)  {
        List<Sort> codom_list = new ArrayList<>();
        l.forEach((f) -> { codom_list.add(f.getSort()); });
        
        return new Domain(codom_list);
    }
    
    
    /** 
     * try to merge this tuple with another one; merging succeeds if and only if
     * <tt>this</tt> has the form <code>&lt;f_1,f_2,..,f_i, f_{i+1},f_{i+2},.. &gt;</code> and other
     * has the form <code>&lt;f_1,f_2,..,f_i', f_{i+1},f_{i+2},.. &gt;</code>, or either this
     * has the form <code>[f]T[eg]</code> and other has the form <code>[f']T[eg]</code>
     * or this has the form <code>[f]T[eg]</code> and other has the form <code>[f]T[eg']</code>;
     * the resulting tuple is <code>&lt;f_1,f_2,..,f_i+f_i', f_{i+1},f_{i+2},.. &gt;</code> or
     * <code>[f or f']T[eg]</code>, and <code>[f]T[eg or eg']</code>, respectively
     * @param  other the tuple to be merged with <tt>this</tt>
     * @return the tuple resulting form merge, or <tt>null</tt> if no merge has been done
     */
    public Tuple merge (Tuple other) {
        int tsize = size();
        Guard myg = guard(), othg = other.guard(), myf = filter(), othf = other.filter();
        boolean equal_g = Objects.equals(myg, othg);
        if ( equal_g  &&  Objects.equals(myf, othf) ) {
            List<? extends SetFunction> args = getComponents(), others = other.getComponents();
            int i = 0;
            while (i < tsize-1 && args.get(i).equals(others.get(i))) ++i;
            List<? extends SetFunction> tail = args.subList(i+1, tsize);
            if ( tail.equals (others.subList(i+1, tsize) )) { //in particular, is true if tail is empty
                List<SetFunction> newlist = new ArrayList<>( args.subList(0, i)); //the first 0..i-1 components ..
                newlist.add ((SetFunction) Union.factory(false, args.get(i), others.get(i)). normalize( ));
                newlist.addAll(tail);

                return build(newlist);
            }
        }
        else if ( (equal_g || Objects.equals(myf, othf) ) && getComponents().equals(other.getComponents())){
            Guard g;
            if (equal_g) {
                if ( (g = disjoin(myf, othf)) != null )
                    g = (Guard) g.normalize();

                return build (g,  myg);
            }
            if ( (g =  disjoin(myg,  othg))  != null )
                g = (Guard) g.normalize();

            return build (myf,  g);
        }
        
        return null;
    }
    
     /**
     * implements the transpose algorithm for a Tuple, assumed to be in a normal-and-form,
     * and not containing the "empty" class-function
     * @return the transpose of <tt>this</tt> tuple
     * @see isNormalAndForm
     */
    public Tuple transpose () {
        if ( ! LogicalExprs.isNormalAndForm( getComponents() ) ) 
            return null;
        
        Domain d = getDomain(), cd = getCodomain(); 
        SortedMap<ColorClass, List<? extends SetFunction>> tr_templ = AllTuple.toMap( d ); //considers this.domain as codomain..
        List<List<SetFunction>> tr_hpart_factors; //the list of factors of a homogeneous part of the transpose 
        Set<Guard> tr_guard = new HashSet<>();
        if (filter() != null)
            tr_guard.add(filter());//the guard of the transpose contains the tuple' filter
        for (Map.Entry<ColorClass, List<? extends SetFunction>> entry : getHomSubTuples().entrySet()) {
            ColorClass cc = entry.getKey();
            tr_hpart_factors = new ArrayList<>(); //we buildOp the list of list factors of the corresponding h. part of the transpose 
            if (tr_templ.get(cc) != null)  // cc_low_case is present in the transpose codomain     
                for (SetFunction f : tr_templ.get(cc)) 
                    tr_hpart_factors.add(Util.singletonList(f));
            for (ListIterator<? extends SetFunction> it = entry.getValue(). listIterator() ; it.hasNext(); ) { // an homeogeneous sub-tuple of this is considered
                SetFunction c = it.next();
                int i = it.nextIndex() ; // the current position on the tuple and the position on the transpose, respectively
                for (SetFunction f : c instanceof Intersection ? ((Intersection)c).getArgs() : Collections.singleton(c)) {
                    if (f instanceof ProjectionBased) {
                        ProjectionBased p = (ProjectionBased) f,
                                        tr_p = Projection.builder(i, - p.getSucc(), cc); // the transposed class-function
                        if (p instanceof ProjectionComp) 
                            tr_p = ProjectionComp.factory((Projection) tr_p);
                        //tr_p is moved in the suitable position (corresponding to its or_index) on the transpose ..
                        tr_hpart_factors.get(p.getIndex() -1).add((SetFunction) tr_p);
                    }
                    else if (f instanceof Subcl) 
                        tr_guard.add(Membership.build( Projection.builder(i,cc), (Subcl) f, cd));
                }
            }
            if (!tr_hpart_factors.isEmpty()) {
                List<SetFunction>  final_list = new ArrayList<>(); //.clear()
                for (List<SetFunction> l : tr_hpart_factors)  // the corresponding transpose part is coherently built
                    final_list.add(Intersection.factory(l));
                tr_templ.put(cc, final_list);
            }
        }
        Guard g = guard(), tr_filter = g != null ? g.clone(d) : null;
        //System.out.println("transposed tuple (by cc_low_case): "+tr_templ.values()); //debug
        return new Tuple(tr_filter, d, tr_templ, tr_guard.isEmpty() ? null : And.factory(tr_guard), cd);
    }
    
    /**
     * 
     * @return the tuple'fc cardLb lower-bound, meant as product of tuple's components
     * cardinalities; <code>null</code> if, for any reason, the cardLb cannot be computed
     * REMARK the possible filter is ignored
     * @throws ArithmeticException in the event of either overflow of an empty tuple
     */
    public Integer tupleCard () {
        Integer card = 1;
        for (List<? extends SetFunction> l : getHomSubTuples().values() ) 
            for (SetFunction f : l)  {
                Interval fc = f.card();
                if (fc == null )
                    return null;
                
                card = Math.multiplyExact(card, fc.lb());
            }
        
        return card;
    }
    
    /**
     * computes the lower-bound of <code>this</code> tuple's cardinality, by considering not only
     * tuple's components, but also the filter; the tuple is assumed to be simplified
     * @return the lower-bound of <code>this</code> tuple'fc cardinality; <code>null</code> if, for any reason, the
     * cardinality cannot be computed (e.eg., because the filter contains more symbols with the same index)
     * @throws ClassCastException if the filter is not in the expected form
     * @throws ArithmeticException in the event of overflow
     */
    @Override
    public Integer cardLb () {
        Guard myf = filter();
        if (myf == null)
            return tupleCard();
        
        Integer card = 1;
        Map<ColorClass, ? extends Map<Boolean, ? extends Set<Equality>>> filtermap = And.equalityMap(myf);
        for (Map.Entry<ColorClass, List<? extends SetFunction>> e : getHomSubTuples().entrySet()) { // for each C-component of the tuple
            ColorClass c = e.getKey();
            Map<Boolean, ? extends Set<Equality>> mc = filtermap.get(c);
            if (mc != null) {
               Integer comp_card = homComponentCard(mc.get(true), mc.get(false), e.getValue());
               if (comp_card == null)
                   return null;
               
               card =  Math.multiplyExact(card, comp_card );
            }
            else
                for (SetFunction f : e.getValue()) {
                    Interval cf = f.card();
                    if (cf == null)
                        return null;
                    
                    card = Math.multiplyExact(card, cf.lb());
                }
        }
        
        return card;
    }
    
    /**
    * @return  the (lower-bound of) the cardinality of a color-component of a tuple
    * associated with a corresponding filter; <tt>null</tt> if, for any reason,
    * it cannot be computed
    */
  private static Integer homComponentCard(Set<? extends Equality> equalities, Set<? extends Equality> inequalities, List <? extends SetFunction> homtuple) {
        boolean alliset[] = new boolean[homtuple.size() + 1]; // the tuple'fc index set
        Map<Projection, Set<Projection>> eq_map = new HashMap<>(); //maps equivalence classes of equalities through their representative element (the filter is assumed canonical)
        Integer card = 1;
        if (equalities != null)
            equalities.forEach(e -> {
                Projection pi ;
                Set<Projection> iset = eq_map.get(pi = e.getArg1());
                if (iset== null)
                    eq_map.put(pi, iset = new HashSet<>());
                iset.add(e.getArg2());
            });
        try {
            if ( inequalities != null && !inequalities.isEmpty() ) {
                InequalityGraph g = new InequalityGraph(inequalities);
                for (Set<? extends Projection> component : g.connectedComponents()) {
                    int  lambda = homtuple.get( component.iterator().next().getIndex() -1).card().lb(); // the cardLb of a tuple comp. referred to by component
                    int chrval  = g.subGraph(component).chromPolynomial(lambda); // the chromatic polynomial value
                    if (chrval < 0)
                        return null;

                    card = Math.multiplyExact(card, chrval );
                    setConsidered( alliset, component); // the corresponding tuple'fc component are as marked "already considered"
                    Iterator<Map.Entry<Projection, Set<Projection>>> iterator = eq_map.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Projection, Set<Projection>> e = iterator.next(); // the next equality class
                        if (component.contains(e.getKey())) {
                            iterator.remove(); //optimization
                            setConsidered( alliset, e.getValue());
                            break; //optimization
                        }
                    }
                }
            }
            //we consider left equality classes
            for (Map.Entry<Projection, Set<Projection>> e : eq_map.entrySet()) {
                int i = e.getKey().getIndex();
                card = Math.multiplyExact(card, homtuple.get(i-1).card().lb() );
                alliset[i] = true;
                setConsidered( alliset, e.getValue());
            }
            //we consider left (i.e., "still to be considered") tuple'fc components
            for (int i = 1; i < alliset.length ; ++i)
                if ( !alliset[i] )
                    card = Math.multiplyExact(card, homtuple.get(i-1).card().lb() );   
        }
        catch (Exception e) {
            card = null;
        }
        
        return card;
    }
    
    private static void setConsidered(boolean[] b, Set<? extends Projection> s) {
        s.forEach(p -> { b[p.getIndex()] = true; });
    }
    
    @Override
    public void printCard() {
        System.out.println("cardinality of "+ this + " : " + cardLb());
    } 

    /**
     * 
     * @return a compact representation used in SODE computation 
     */
    @Override
    public  String toStringAbstract () {
            StringBuilder s = new StringBuilder();
            Guard g = guard();
            //Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = And.equalityMap(g);
            Map<ColorClass, Map<Boolean, Set<Membership>>> membMap = And.membMap(g);
            //System.out.println("memb map: "+membMap); //debug
            for (Map.Entry<ColorClass, List<? extends SetFunction>> e : getHomSubTuples().entrySet()) {
                ColorClass cc = e.getKey();
                String cc_name = cc.name();
                String cc_low_case = cc_name.toLowerCase();
                List<? extends SetFunction> list = e.getValue();
                for (int i = 0 ; i < list.size() ; ++i) {
                    s.append(cc_low_case).append(i+1);
                    Map<Boolean, Set<Membership>> ccMembMap = membMap.getOrDefault(cc, Collections.emptyMap());
                    Set<Membership> in   = ccMembMap.getOrDefault(true, Collections.EMPTY_SET),
                                    notin= ccMembMap.getOrDefault(false, Collections.EMPTY_SET);
                    SetFunction f = list.get(i);
                    //System.out.println("in map: "+in);
                    //System.out.println("notin map: "+notin);    
                    if (f instanceof Projection) {
                        Projection p = (Projection) f;
                        boolean go_on = true;
                        for (Membership m : in)
                            if (m.getArg1().equals(p)) {
                                s.append('_').append(cc_name).append(m.index());
                                go_on = false;
                                break;
                            } 
                        if (go_on) {
                            boolean first = true;
                            for (Membership m : notin)
                                if (m.getArg1().equals(p)) {
                                    s.append(first ? "_not" : "_").append(cc_name).append(m.index());
                                    go_on = first = false;
                                }
                        }
                        if (go_on)
                            s.append('_').append(cc_name);
                    }
                    else if (f instanceof Intersection) {
                        Intersection inter = (Intersection) f;
                        Subcl sc = Util.find(inter.getArgs(), Subcl.class);
                        s.append('_').append(cc_name);
                        if (sc != null) 
                            s.append(sc.index());
                    } 
                    else if (f instanceof Subcl) {
                        s.append('_').append(cc_name).append(((Subcl)f).index());
                    }
                    else
                        s.append('_').append(cc_name);
                    
                    s.append('_');
                }
            }    
            s.deleteCharAt(s.length()-1);
            
            return s.toString();
    }

    @Override
    public Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }
    
    @Override
    public Tuple asTuple() {
        return this;
    }
    
    @Override
    public boolean isTuple() {
        return true;
    }
       
}