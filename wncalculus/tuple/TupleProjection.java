package wncalculus.tuple;

import java.util.*;
import wncalculus.expr.*;
import wncalculus.graph.InequalityGraph;
import wncalculus.classfunction.ClassFunction;
import wncalculus.classfunction.Projection;
import wncalculus.classfunction.SetFunction;
import wncalculus.color.ColorClass;
import wncalculus.guard.*;
import wncalculus.util.Util;
import wncalculus.util.Pair;

/**
 * this class defines the Projection on the first k components of a one-sorted WN function-tuple F 
 its precise semantics is &lang;X_i,..,X_k&rang;  \circle F ;
 * @author Lorenzo Capra
 */
public final class TupleProjection implements FunctionTuple, UnaryOp<FunctionTuple> {

    
    public final int k ; // the Projection bound
    private final ColorClass cc; // the Projection's color class
    private final FunctionTuple ftuple; // the function argument 
    
    private boolean simplified;
    
    final static String OPSYMB =  "Prj_";//"\u220F_"; 
    
    private Domain codomain; //cache
    
    /** base constructor
     * @param f the Projection function-tuple
     * @param bound the Projection's bound
     * @throws IllegalArgumentException if the specified function is not one-sorted 
     */
    public TupleProjection (FunctionTuple f, int bound) {
        if (( this.cc = (ColorClass) f.oneSorted() ) == null) 
            throw new IllegalArgumentException("Many-sorted funtion-tuple to project!");
        
        this.k = checkSize( bound , f.size() );
        this.ftuple = f;
    }
    
    private static int checkSize (int k , int arity) {
        if (k <  1  || k >  arity ) 
            throw new IndexOutOfBoundsException("inconsistent projection bound ("+k+")");
        
        return k;
    }
    
    /**
     *
     * @return the projection's bound
     */
    public int bound() {
        return this.k;
    }
    
    /**
     *
     * @return the projected function's (assumed one-sorted) color
     */
    public ColorClass getSort () {
        return this.cc;
    }

    
    @Override
    public Domain getCodomain() {
        if (this.codomain == null)
            this.codomain = new Domain(this.cc, this.k);
        
        return this.codomain;
    }
        
    @Override
    public Map<Sort, Integer> splitDelimiters() {
        Map<Sort, Integer> delimiters  = this.ftuple.splitDelimiters();
        if (this.ftuple instanceof Tuple && delimiters.isEmpty() ) {
            Tuple tuple = (Tuple) this.ftuple;
            if (tuple.filter() instanceof And  ) {
                InequalityGraph igraph =  ( (And) tuple.filter() ).igraph().get(this.cc);
                if (igraph != null)  
                    ColorClass.setDelim(delimiters, this.cc, monoBound(igraph));
            }
        }
        //System.out.println("ecco delim di "+this+": "+delimiters); //debug
        return delimiters;   
    }
    
    @Override
    public FunctionTuple specSimplify( ) {
        FunctionTuple res = this;
        //System.out.println("TupleProjection:\n"+this); //debug
        if (this.ftuple.isFalse() ) 
            res = getFalse();
        else if (this.k == this.ftuple.size())  // the Projection is the identity
            res = this.ftuple;
        else if (this.ftuple instanceof TupleProjection) 
            res = new TupleProjection(((TupleProjection) this.ftuple).ftuple , this.k);
        else if (this.ftuple instanceof AllTuple) 
            res = AllTuple.getInstance(getCodomain(), getDomain());
        else if ( this.ftuple instanceof Tuple ) {//main case: the projected function is a tuple..   
            // VERY IMPORTANT: we assume that the inequation set corresponding to the filter has been shown "satisfiable"...
            Tuple tuple = (Tuple) this.ftuple ; 
            List<? extends SetFunction> components = tuple.getHomSubTuple(this.cc);
            //System.out.println(this); //debug
            if ( SetFunction.differentFromZero(components.subList(this.k, tuple.size() )) ) {
                Guard filter = tuple.filter() , guard = tuple.guard(); 
                Set<Integer> f_idxset = filter != null ? filter.indexSet() : Collections.EMPTY_SET;
                SortedMap<ColorClass,List<? extends SetFunction>> projection = Util.singleSortedMap(this.cc, components.subList(0, this.k ));
                Domain codom = getCodomain(), dom = getDomain(); //careful to the co-domain
                if (f_idxset.isEmpty() || Collections.max(f_idxset) <= this.k)  // rule 3 (simplest case)
                      res = new Tuple (filter != null ? filter.clone(codom) : null, codom, projection, guard, dom); 
                //the filter contains at least one variable with index > k ...; due to preliminary filter simplifications,
                //equalities should only refer to i) "equal" (mod-succ) components ii) of card > 1, as for inequalities condition i) omay hold for single forms
                else {
                    Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap = And.equalityMap(filter);
                    Set<Equality> equalities   = equalityMap.get(this.cc).getOrDefault(true,  Collections.emptySortedSet()), 
                                  inequalities = equalityMap.get(this.cc).getOrDefault(false, Collections.emptySortedSet());
                    if (! equalities.isEmpty() && Collections.max(Guard.indexSet(equalities) ) > this.k ) { // Lemma 11: there are any equalities that refer to the extended part...
                        Collection<Guard> restriction = Guard.restriction(equalities, this.k);
                        restriction.addAll(inequalities); // inequalities are added
                        res = new TupleProjection(new Tuple (restriction.isEmpty() ? null : And.factory(restriction), tuple.getCodomain(), tuple.getHomSubTuples(), guard, dom), this.k);
                    }
                    else if ( checkCardGe1(inequalities,  components, this.k) ) { // the cardinalities of inequality domains must be > 1 ...
                        // some inequality refers to the tuple's extension (otherwise we would have been fallen in one of the previous cases ..
                        InequalityGraph igraph = new InequalityGraph(inequalities);
                        int mon_bound =  monoBound(igraph), // the Projection monotonicity-bound ..
                            lb = this.cc.lb(), ub;
                        //System.out.println(this +" f_sat: "+f_sat+ " (lb="+lb+')'); //debug
                        //corollary 14 + lemma 4.10: either the constraint's lb > \pi_k (f_sat) or g[T] is f.p and the inequalities' restriction is a clique
                        boolean single_f;
                        FunctionTuple ft;
                        Set<? extends Guard> f_args = And.getArgs(filter);
                        if ( lb > mon_bound || (single_f = igraph.isSingleForm() ) && igraph.isClique(this.k) &&
                                       Util.checkAll(igraph.indexSet(), i ->  i <= this.k || components.get(i-1).cardLb() >= igraph.chromaticNumber()) ) 
                            res = new Tuple( And.buildAndForm(Guard.restriction( f_args, this.k), codom) , codom, projection, guard, getDomain()); // k-restriction of the whole filter
                        // lb <= f_sat and either g is not a single-form or the inequality graph's restriction is not a clique, or some extra components has card lb < X
                        else if (single_f && ( ft = tuple.reduceFilterClassIneqs(inequalities, this.cc) ) != tuple)  // g single-form but [g]T not a fixed-point
                            res = new TupleProjection(ft, this.k); 
                       // ... and either g is not a single-form or [g]T is a fixed-point
                        else if ( (ub = this.cc.ub() ) > 0 && ub  <= mon_bound ) { // the constraint's u.b. <= f_sat (otherwise some split needed)
                            Set<Guard> args_1, args_2; //
                            codom = tuple.getCodomain();
                            if (single_f ) { // [g]T is a f.p.: there should be (aasumption) a pair of independent nodes X_i, X_j, i <=k , j <= k
                                Projection[] i_nodes = igraph.getIndependentNodesLe(this.k); // null se la restrizione è clique (cioè -v. condiziobe if principale- se le componenti extra hanno card lb < X: in questo caso ce ne saremmo dovuti accorgere)
                                //System.out.println(toStringDetailed()+": aggiunto vincolo implicito: "+i_nodes[0]+","+i_nodes[1]); //debug
                                args_1 = new LinkedHashSet<>(f_args);
                                args_2 = new LinkedHashSet<>(f_args);
                                args_1.add(Equality.builder(i_nodes[0],i_nodes[1],true, codom )); //se solleva eccezione NullPointer significa .. (v. commento sopra)
                                args_2.add(Equality.builder(i_nodes[0],i_nodes[1],false,codom));
                                TupleProjection tp_1 = new TupleProjection(new Tuple (And.buildAndForm(args_1), codom, tuple.getHomSubTuples(), guard, dom), this.k),
                                                tp_2 = new TupleProjection(new Tuple (And.buildAndForm(args_2), codom, tuple.getHomSubTuples(), guard, dom), this.k);
                                res = TupleSum.factory(true, tp_1, tp_2);
                            }
                            else if (lb == ub ) { // not single-form: we assume that eventually the term becomes non parametric ...
                                Iterator<Set<Equality>> ite = Util.mapFeature(inequalities, e -> new Pair<>(e.firstIndex(), e.secondIndex())). values().iterator();
                                Set<Equality> maxsim = ite.next(), next;
                                while (ite.hasNext())
                                    if ( (next = ite.next() ).size() > maxsim.size() )
                                        maxsim = next;
                                //the greatest similar sub-list of ineqs is replaced in the filter by a cooresponding sum of eqs 
                                args_1 = new LinkedHashSet<>(f_args); 
                                args_1.removeAll(maxsim);
                                Or nested = (Or) Or.factory(Equality.missingOppEqs(maxsim, lb), true);
                                res = new TupleProjection(new Tuple ( ((And)And.factory(args_1)).distribute(nested), codom, tuple.getHomSubTuples(), guard, dom), this.k);
                            } 
                        }
                    }
                }
            }
            //System.out.println(this+"\n->\n"+res.toStringDetailed()); //debug
        }
       
        return res; 
    }
    /**
     * find a minimal upper bound (monotonicity bound) for the the (k-)projection of [g']T to be
     * equivalent to the syntactical restriction of the [g']T.
     * tuple's components cardinalities are taken into account
     * @param g the inequation graph associated with the filter
     * @return a minimal upper bound for the (k-)Projection to be equivalent to the (-k)syntactical restriction
     */
    private int monoBound (InequalityGraph g) {   
        if (g.indexSetGt(this.k).isEmpty()) 
            return 0;
        
        Set<Projection> to_be_removed   = new HashSet<>();
        Set<Integer>    min_degree_set  = new HashSet<>();
        int min_d = min_degree_vset (g, min_degree_set);
        min_degree_set.forEach( i -> { to_be_removed.addAll(g.vertexSet(i)); });
        //System.out.println(g); debug
        return Math.max(min_d , monoBound( g.clone().removeAll(to_be_removed)));
    }
    
    /**
     * brings in the specified set (which is cleared at each call) the set of indices
     * {i}, i > k and degree(v_i)+gap_i == min({degree(v_i)+gap(i)} );
     * gap(i) is the constant "gap" associated to the domain represented by the application of the i-th component
     * of the specified (extended) tuple; k is the tuple's Projection bound;
     * @return min({degree(i)+gap(i)}, i > k)
     * @throws NoSuchElementException if the set of vertices with index > k is empty
     */
    private int min_degree_vset(InequalityGraph g, Set<Integer> sd) {
        List<? extends ClassFunction> t = ((Tuple) this.ftuple).getHomSubTuple(this.cc);
        Iterator<Integer> it = g.indexSetGt(this.k).iterator();
        int i   = it.next(), min = g.degree(i) + Math.max(((SetFunction)t.get(i-1)).gap(), 0), d_i;
        sd.add(i);
        while ( it.hasNext() ) 
            if ( ( d_i = g.degree(i = it.next()) + Math.max(((SetFunction)t.get(i-1)).gap(), 0) ) <= min) {
                if (d_i < min) {
                    min = d_i;
                    sd.clear();
                }
                sd.add(i);
            }
        
        return min;
    }
    
    /**
     * checks whether the cardLb of functions (i.e., domains) referred to by a list of
     * in(equalities) is greater than one; only indices greater than a given threshold are considered
     * @param eqs a collection of equalities
     * @param components an associated list of domains 
     * @return <code>true</code> if and only if variables domains are greater than one 
     */
    private static boolean checkCardGe1(Collection<? extends Equality> eqs, List<? extends SetFunction> components, int k) {
        return Guard.indexSet(eqs).stream().filter(x -> x > k).map(x -> components.get(x -1).card()).noneMatch(card -> card == null || card.lb() < 2);
    }
    
    @Override
    public boolean equals(Object o) {
        TupleProjection tp;
        return this == o || o instanceof TupleProjection && this.k == (tp = (TupleProjection)o).k && this.ftuple.equals(tp.ftuple) ;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.k;
        hash = 23 * hash + Objects.hashCode(this.ftuple);
        
        return hash;
    }

  
    /**
     * overrides the super-type method because the operand's codomain is an extension
     * of <tt>this</tt> term's codomain
     * @param newdom the new domain
     * @param newcd  the new codomain
     * @param smap the color split-map
     * @return a clone of <tt>this</tt> with the specified co-domains
     */
    @Override
    public TupleProjection  clone (final Domain newdom, final Domain newcd) {
        return buildOp( getArg().clone(newdom, new Domain(newcd.support().iterator().next(), this.ftuple.size())). cast());
    }

 
    @Override
    public FunctionTuple baseCompose(FunctionTuple right) {
        //System.out.println("TupleProjection.baseCompose(" +toStringDetailed()+','+right+')'); //debug
        return new TupleProjection(new TupleComposition(this.ftuple, right) , this.k); // esiste regola generale equivalente   
    }

    @Override
    public boolean differentFromZero() {
        return this.ftuple.differentFromZero();
    }

    @Override
    public boolean isConstant() {
        return this.ftuple.isConstant();
    }

    @Override
    public String symb() {
        return TupleProjection.OPSYMB+this.k;
    }

    @Override
    public FunctionTuple getArg() {
        return this.ftuple;
    }

    @Override
    public TupleProjection buildOp(FunctionTuple arg) {
        return new TupleProjection(arg, this.k);
    }
    
    @Override
    public String toString() {
        return UnaryOp.super.toStringOp();
    }

    @Override
    public boolean isDistributive (Class<? extends MultiArgs> optk) {
        return  optk.equals(TupleSum.class);
    }

    @Override
    public boolean isInvolution() {
        return false;
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = simplified;
    }
    
    @Override
    public final Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }
    
}