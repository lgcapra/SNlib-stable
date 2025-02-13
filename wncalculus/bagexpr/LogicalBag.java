package bagexpr;

import java.util.*;
import java.util.function.BiFunction;
import color.ColorClass;
import guard.*;
import expr.Domain;
import expr.Sort;
import logexpr.LogicalExprs;
import logexpr.OrOp;
import logexpr.SetExpr;
import tuple.AllTuple;
import tuple.Tuple;
import util.Pair;
import util.Util;

/**
 * This class provides an implementation for bags of boolean expressions
 * @author Lorenzo Capra
 * @param <E> the bag's domain
*/
public abstract class LogicalBag<E extends SetExpr> extends AbstractBag<E>  {
    private boolean  disjoined;
    
   
    /**
     * main constructor: builds a <tt>LogicalBag</tt> from a <tt>Map</tt>, which is
     * assumed non-empty, not considering elements with value zero
     * @param m a map
     * @param disjoint flag denoting whether the bag has to be assumed in a disjoint form (no check done!)
     * @throws NoSuchElementException if the map is empty
     */
    public LogicalBag(Map<E, Integer> m, boolean disjoint) {
        super(m); // it may remove 0-value elements
        this.disjoined = m.size() < 2 || disjoint;
    }
   
    
    /**
     * build an empty bag
     * @param dom the bag's domain
     * @param codom the bag's codomain
     */
    public LogicalBag(Domain dom, Domain codom) {
        super(dom, codom);
        this.disjoined = true;
    }
    
    /**
     * build an empty bag
     * @param dom the bag's (co-)domain
     */ 
    public LogicalBag(Domain dom) {
         this(dom,dom);
     }
    
    //costruttori secondari (si potrebbero togliere: sono già definiti metodi corrispondenti in BagBuilder)

    /**
     * build a singleton bag
     * @param f the bag's term
     * @param k the term's multiplicity
     */
    
    public LogicalBag(E f , int k) {
        this (Util.singleMap(f, k), false);
    }
    
    /**
     * build a default, singleton bag (with multiplicity one)
     * @param f the bag's term
     */
    public LogicalBag(E f) {
        this(f, 1);
    }
    
    /**
     * build a bag from a collection of terms
     * @param c a collection of terms
     */
    public LogicalBag (Collection<? extends E> c) {
         this (Util.asMap(c), false);
     }
     
    /**
     * build a bag from a list (varargs) of terms
     * @param c a list of terms
     */
    public LogicalBag  (E ... c) {
         this (Arrays.asList(c));
     }
     
   /**
     * simplifies this "boolean" bag by normalizing its terms (resulting "false" terms are erased)
     * and possibly expanding "OR" terms
     * @return a "simplified" equivalent bag
     */
    @Override
    public final LogicalBag<E> specSimplify () {
       if ( isEmpty() ) 
           return this;
       
       Map<E,Integer> smap = new HashMap<>();
       Class<E> type = bagType();
       asMap().entrySet().forEach( s -> {
           E key = s.getKey(), e = type.cast(key.normalize());
           if (! e.isFalse()) 
               smap.put( e, s.getValue());
       });
       boolean disj = expand(smap) && this.disjoined; // OR terms are expanded (new)

       return  smap.isEmpty() ? build(getDomain(), getCodomain()) : build(smap, disj);
    }
    
    @Override
    public final LogicalBag<E> genSimplify() {
        return disjoin();
    }
    
    @Override
    public final boolean terminal () {
        return support().stream().allMatch(t -> t.terminal() );
    } 
    
    /**
     *
     * @return <tt>true</tt> if <tt>this</tt> bag's terms are pair-wise disjoint
     */
    public final boolean disjoined() {
        return this.disjoined;
    }
    
    /** pair-wise disjoin <code>this</code> bag's elements, operating in a non-destructive way
     *  should never be invoked from inside normalization 
     *  @return an "equivalent" pair-wise disjoint bag, or <code>this</code> if the
     *  bag is marked as disjoined
    */
    public final LogicalBag<E> disjoin() {
        return this.disjoined ? this : build( disjoinAsMap(), true);
    }
    
    /**
     * @return a normalized, pair-wise disjoint form equivalent to <code>this</code> bag;
     * <code>this</code> if, for any reason, disjoining does't produce any effect
     */
    public BagExpr<E> disjoinAndNormalize() {
        LogicalBag<E> db = disjoin();
        //System.out.println("(LogicalBag.normalize(boolean)\n"+db); //debug
        return db != this ? db.normalize() : db;
    }
    
    //ATTENZIONE: RIVEDERE (x EFFICIENZA) SULLA BASE DI QUELLO x LogExpr
    //creates a copy of the map corresponding to this bag with pair-wise dijoint keys
    //the map is assumed non empty
    private Map<E, Integer> disjoinAsMap () {
        Map<E, Integer> disjoint_m = new HashMap<>();
        Class<E> type = asMap().keySet().iterator().next().type();
        asMap().entrySet().forEach( x -> { addAndDisjoin(x.getKey(), x.getValue(), disjoint_m, type); });
        
        return disjoint_m;
    }
    
    /**
     * given a multi-set (i.e., a map), whose elements are assumed to be "pair-wise" disjoint,
     * adds an element with an associated multiplicity (k.e) preserving disjointness.
     * Let m: k1.e1 + k2.e2 + ...+ k_n.e_n; the resulting map is
     * k.(e - \cup e_i) + sum_i (k+k_i).(e \cap e_i) + sum_i k_i.(e_i - e)
     * @param <E> the map's domain
     * @param e the element to be added
     * @param k the element's multiplicity
     * @param m the given multi-set
     * @param token the type of bag's elements
     */
    private static <E extends SetExpr> void addAndDisjoin (final E e, final int k, final Map<E, Integer> m, final Class<E> token) {    
        Map<E, Integer> to_replace = new HashMap<>();
        Collection<E> subtraends = new ArrayList<>(); 
        if (m.isEmpty()) 
            m.put(e, k);
        else {
            Integer k1 = m.get(e);
            if ( k1  != null ) 
                m.put(e, k + k1);
            else {
                for (Iterator<E> it = m.keySet().iterator(); it.hasNext(); ) {
                    E f = it.next(), inter = token.cast(f.andFactory(e, f). normalize()); //normalization needed here     
                    if (!inter.isFalse() ) { // e and f are not disjoint 
                        to_replace.put( inter, ( k1 = m.get(f) ) + k );
                        if (!f.equals(inter))  // f is not contained in e
                            to_replace.put( token.cast(f.diff(e)/*. normalize()*/), k1 ); //normalization optional here 
                        it.remove(); // f is removed from m
                        if (e.equals(inter))  // e is contained in f
                            break; //because the partial sum is disjoint!

                        subtraends.add( f );
                    }
                }
                if (!to_replace.isEmpty()) {
                    m.putAll(to_replace);
                    if (!subtraends.isEmpty()) 
                        m.put(token.cast(e.diff( e.orFactory(subtraends, true))/*. normalize()*/) , k); //normalization optional here 
                }
                else 
                    m.put(e, k);
            }
        }
    }
       
   /** 
    * expand possible "OR" terms present in a map (representing a bag), in a destructive way
    * @param m a map (representing a bag)
    * @return <code>true</code> if and only if all "OR" terms are disjoint 
    */
   private boolean expand(Map<E, Integer> m) {
        boolean disjoint = true;
        ArrayList<OrOp<E>> orTerms = new ArrayList<>();
        for (E t : m.keySet()) // we first collect possible OR terms 
            if ( t instanceof OrOp ) {
                OrOp op = (OrOp) t;
                orTerms.add(op);
                disjoint &= op.disjoined();
            }
        orTerms.forEach( op -> { // for each OR term we expand its operands
            int k = m.get(op); //molteplicità dell'operatore in m
            op.getArgs().forEach( e -> {
                Integer mult = m.get(e);
                m.put(e, (mult == null ? 0 : mult) + k);
            });
        });
        m.keySet().removeAll(orTerms); // OR terms are finally removed from the map
        
        return disjoint;
    }
   

    @Override
    public LogicalBag<E> build(int k, E e) {
        LogicalBag<E> b = (LogicalBag<E>) super.build(k, e);
        b.disjoined = true;
        
        return b;
    }
    
    @Override
    public abstract LogicalBag<E> build(Domain dom, Domain codom);
    
    public abstract LogicalBag<E> build(Map<E, Integer> smap, boolean disj);

    @Override
    public LogicalBag<E> build(Map<E, Integer> m) {
        return m.isEmpty() ? (LogicalBag<E>)build() : build(m, false);
    }    
    
    /**
     * @return the sum of cardinalities of <tt>this</tt> bag's terms weighted by the
     * corresponding multiplicities (it makes sense only if these are positive);
     * <tt>null</tt> if, for any reasons, some of them cannot be computed
    */
    @Override
    public Integer card() {
        int card = 0;
        for (Map.Entry<? extends E, Integer> x : asMap().entrySet()) {
            Integer k = x.getKey().cardLb();
            if (k == null)
                return null;
            
            card += k * x.getValue();
        }
        return card;    
    }
    
    /**
     * computes (efficiently) the support of <tt>this</tt> bag,
     * considering only terms with positive coefficients;
     * @return <tt>this</tt> bag's support if proper, or <tt>null</tt> if the bag is not in a
     * disjoint form and contains some terms with negative coefficients
     */
    @Override
    public final Set<? extends E> properSupport() {
        if ( isProperBag() )
            return support();
        
        return this.disjoined ? super.properSupport() : null;
    }
    
    /**
     * given a bag of tuples, estracts a map:
     * {guards &rarr; coefficients}, which corresponds to a partition of bag's domain
     * obtained by applying a given function} (e.g., max, sum, etc.) to the coefficients
     * of terms with the same guards
     * (map's keys) are put in a pairwise mutex form (see @disjoinMapGuardsToCoeff)
     * the bag is normalized and put in a disjoint form, if it is not
     * NEW: add to the map the implicit guard given by the "negation" of the "sum" of the guards of the bag,
     * with an associated coefficient 0 (if the resulting expression is other than false)
     * due to the initial assumption, this implementation disregards the bag's terms, just considers their guards
     * @param f the function to apply
     * @return a map {guards &rarr; computed coefficients} of terms with the same guard
     * different guards in the map are mutually exclusive; <code>null</code> if the bag contains terms other than tuples.
     * @throws ClassCastException if some key is not a <code>Tuple</code>
    */
    public Map<Guard, Integer> mapGuardsToCoefficients (BiFunction<Integer,Integer,Integer> f) {
        Map<Guard, Integer> m = new HashMap<>();
        //System.out.println("mapGuardsToCoefficients: "+this); //debug
        LogicalBag<E> b =  (LogicalBag<E>) disjoinAndNormalize(); //the bag is in disjoint form
        if (b.isEmpty()) {
            m.put(True.getInstance(getDomain()), 0);
            return m;
        }
        //System.out.println("mapGuardsToCoefficients: mormalizzato:\n"+db); //debug
        Set<? extends E> keys = b.support();
        if (keys.size() == 1) { //elementary bag
            E k = keys.iterator().next();
            m.put(k instanceof AllTuple ? True.getInstance(getDomain()) : ((Tuple)k).guard(), b.mult(k));
        }
        else {
            keys.forEach( x -> { setVal(m, ((Tuple) x).guard(), b.mult(x), f); });
            if (m.size() > 1) //ottimizzazione
                 disjoinMapOfGuards(m, f);
        }
        if (!LogicalExprs.disjoined(m.keySet())) {
            System.out.println("le guardie dovrebbero essere disgiunte (1): "+m.keySet()); //debug
            throw new Error();
        }
        Integer truecoeff = m.get(null); //null represents "true": we replace it on the map
        if (truecoeff != null) {
            m.remove(null);
            m.put(True.getInstance(getDomain()), truecoeff);
        }
        // the implicit, complementary guard is built with coefficient 0
        Guard implicit = Neg.factory(Or.factory(m.keySet(), true));
        //System.out.println("guardia implicita:\n"+implicit);
        implicit = (Guard) implicit. normalize(true);
        //Util.unSetLog();
        
        if ( !implicit.isFalse()  ) {
            m.put(implicit, 0);
            //if (!LogicalExprs.disjoined(m.keySet())) {
                //System.out.println("le guardie dovrebbero essere disgiunte (2): "+/*mcopy*/m.keySet()); //debug
                //System.out.println("guardia implicita (normalizzata):\n"+implicit);
                //throw new Error();
            //}
        }
        //System.out.println("mapGuardsToCoefficients: finito"); //debug
        
        return m; // we could merge the obtained map!
    }
    
    /**
     * given a bag of tuples, assumed normalized (and disjoint), estracts a map:
     * {guards &rarr; max coefficients}, which corresponds to a partition of bag's domain
     * @return the map {guards &rarr; max coefficients}
     */
    public Map<Guard, Integer> mapGuardsToMaxCoefficients () {
        return mapGuardsToCoefficients((Integer x, Integer y) -> Math.max(x, y));
    }
    
    /**
     * given a bag of tuples, assumed normalized (and disjoint), estracts a map:
     * {guards &rarr; sum of coefficients}, which corresponds to a partition of bag's domain
     * @return the map {guards &rarr; sum of coefficients}
     */
    public Map<Guard, Integer> mapGuardsToSumCoefficients () {
        return mapGuardsToCoefficients((Integer x, Integer y) -> x + y );
    }
        
    /*
    given a map {guards &rarr; coefficients}, checks mutex for each pair g_i, g_j
    if mutex doesn't hold, it associates the (g_i and g_j) with the application of function f,
    replaces g_i with (g_i and not g_j) and g_j with (not g_i and g_j).
    WARNING:  a <code>null</code> guard corrisponds to true!!
    */
    private static void disjoinMapOfGuards(Map<Guard, Integer> m, BiFunction<Integer,Integer,Integer> f) {
        Set<Guard> guards = new HashSet<>(m.keySet()); //contiene l'insieme (aggiornato) delle guardie ancora da considerare
        while ( ! guards.isEmpty() ) {
            Iterator<Guard> ite = guards.iterator();
            Guard g1 = ite.next(); //prendiamo una guardia dal set
            ite.remove(); // la rimuoviamo
            for (int k1 = m.get(g1); ite.hasNext() ; ) { //verifichiamo se tra le guardie restanti ve n'è una non esclusiva a g1
                Guard g2 = ite.next();
                Guard[] parts = disjoinParts(g1,g2);
                if (parts != null) { //g1 e g2 non sono esclusive
                    Integer k2 = m.get(g2);
                    m.remove(g1); //g1 e g2 sono sostituiti con g1 and g2, g1 and not g2, not g1 and g2
                    m.remove(g2);
                    ite.remove(); // g2 rimosso anche da guards
                    setVal(m, parts[0], f.apply(k1, k2), f); // 0 è l'intersezione (è sicuramente != false)
                    guards.add(parts[0]);
                    if (!parts[1].isFalse()) {
                        setVal(m, parts[1], k1, f);
                        guards.add(parts[1]);
                    }
                    if (!parts[2].isFalse()) {
                        setVal(m, parts[2], k2, f);
                        guards.add(parts[2]);
                    }
                    break; // il loop interno!
                }
            }
        }
    }
    
    
    /*
    more general version, that applies a given function
    */
    private static void setVal (Map<Guard, Integer> m, Guard g, Integer v, BiFunction<Integer,Integer,Integer> f) {
        Integer k = m.get(g);
        m.put(g, k == null ? v : f.apply(k, v));
    }
    
    /*
    given two guards, returns a size-3 array holding g1 and g2 (0), g1 and not g2 (1), not g1 and g2 (2)
    if  g1, g2 are mutex returns <code>null</code>
    (assumes that only one between g1,g2 can be <code>null</code> ,i.e, true)
    */
    private static Guard[] disjoinParts(Guard g1, Guard g2) {
        Guard g; 
        Guard []res = new Guard[3];
        if (g1 == null) {// g1 == true oppure g2 == true
            res[0] = g2;
            res[1] = (Guard) (Neg.factory(g2)).normalize(true);
            res[2] = False.getInstance(g2.getDomain());
        }
        else if (g2 == null) {
            res[0] = g1;
            res[1] = False.getInstance(g1.getDomain());
            res[2] = (Guard) (Neg.factory(g1)).normalize(true);
            
        }
        else if ( ! (g = (Guard)(And.factory(g1,g2).normalize(true)) ).isFalse() ) {
            res[0] = g;
            res[1] = (Guard) g1.diff(g2).normalize(true);
            res[2] = (Guard) g2.diff(g1).normalize(true);
        }
        else
            res = null;
        
        return res;
    }
        
     /**
     * given a list of maps (i.e., set of pairs) guards to coefficients performs a kind of Cartesian product
     * building a map where each key is the "And" of a combination of keys on the list,
     * and the associated value is the corresponding list coefficients
     * @param lm a list of maps guards to coefficients
     * @return the map corresponding to the Cartesian product of the list of maps (seen as sets),
     * obtained by composing guards in an "And" form and considering the list of coefficients
     * "false" guards are erased from the resulting map
     * @throws IllegalArgumentException if the obtained guards are not unique (should be impossible
     * if they were mutually exlusive in the source maps)
     * SI POTREBBE OTTIMIZZARE NON CONSIDERANDO LA COMBINAZIONE DELLE GUARDIE IMPLICITE
     * this version replaces empty maps (denoting empty input arc functions) in the input list
     * with singletons {null=0}, where null means "true"
    */
    public static Map<Guard, List<Integer>> product (List<Map<Guard, Integer> > lm) {
        for (ListIterator<Map<Guard, Integer>> ite = lm.listIterator(); ite.hasNext() ; ) 
            if ( ite.next().isEmpty() )
                ite.set(Collections.singletonMap(null, 0)); // null stands for "true" (null input function)
        
        List<Set<Map.Entry<Guard, Integer>>> c = new ArrayList<>();
        lm.forEach( m -> { c.add( m.entrySet() ); }); // build the list of sets corresponding to the list of maps
        HashMap<Guard, List<Integer>> res = new HashMap<>();
        Util.cartesianProd(c).stream().map( l -> combineGuardCoeff((List<? extends Map.Entry<Guard, Integer>>) l)).forEachOrdered(entry -> {
            Guard g = entry.getKey();
            if ( g != null && ! g.isFalse() && res.putIfAbsent(g, entry.getValue()) != null)
                throw new IllegalArgumentException("identical combinations of guards found! ->\n"+g+ "\nhere is the initial list of maps:\n"+lm);
        });
                    
        return mergeGuards(res); // optimization (final merge)
    }
    
    /**
     * varargs version of product
     * @param list_of_entries a list of maps (i.e., set of paits) guards &rarr; coefficients
     * @return the map corresponding to the Cartesian product of the list of maps
     */
    public static Map<Guard, List<Integer> > product ( Map<Guard, Integer> ... list_of_entries) {
         return product (Arrays.asList(list_of_entries));
    }
    
    // given a list of entries {guard,coeff} builds a corresponding pair composed of
    // the "And" of guards and the associated list of coefficients
    // if the latter just contains value 0, return (null,null)
    // this version considers null guards as equivalento to "true"
    private static Pair<Guard, List<Integer>> combineGuardCoeff (List<? extends Map.Entry<Guard, Integer>> entry_list) {
        List<Guard> lg = new ArrayList<>();
        List<Integer> li = new ArrayList<>();
        entry_list.forEach( e -> {
            Guard g = e.getKey();
            if (g != null) // a null value corresponds to "true"
                lg.add(g);
            li.add(e.getValue()); // must be added also for null guards!
        });
        if (lg.isEmpty())
            throw new IllegalArgumentException("the list just contains null guards (representing empty arc functions!\n");
        
        return Collections.frequency(li, 0) == li.size() ?  new Pair<>(null, null) : 
                new Pair<>((Guard) And.factory(lg).normalize(true), li);
    }
    
    /**
     * given a map having Guards as keys, "merge" keys with the same associated values
     * replacing them with the corresponding "OR" term
     * @param <E> the type of values
     * @param m the given map
     * @return a compact map where keys with the same associated values
     * replacing them with a corresponding "OR" term; the map itself if no
     * "merge" is performed
     */
    public static <E> Map<Guard, E > mergeGuards (Map<Guard, E > m) {
        if ( Util.injective(m) ) // m is injective
            return m;
        
        Map<E, Set<Guard>> im = Util.invert(m); // the inverted map
        HashMap<Guard, E> mm = new HashMap<>();
        im.entrySet().forEach( e -> { mm.put((Guard) Or.factory(e.getValue(), true).normalize(), e.getKey()); });
        
        return mm;
    }

    @Override
    public final Map<Sort, Integer> splitDelimiters() {
        return ColorClass.mergeSplitDelimiters(support());
    }

    
}
