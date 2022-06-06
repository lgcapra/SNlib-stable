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
    
    public  final int k ; // the Projection bound
    private final ColorClass cc; // the Projection's color class
    private final FunctionTuple ftuple; // the function argument 
    
    private boolean simplified;
    
    final static String OPSYMB =  "Prj_";//"\u220F_"; 
    
    //cache
    private Domain  codomain; 
    private Integer monBound, splitdelim;
    
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
        if (this.splitdelim != null) 
                ColorClass.setDelim(delimiters, this.cc, this.splitdelim);
        //System.out.println("ecco delim di "+this+": "+delimiters); //debug
        return delimiters;   
    }
        
    
    @Override
    public FunctionTuple specSimplify( ) {
        //System.out.println("TupleProjection:\n"+this); //debug
        if (this.k == this.ftuple.size())  // the Projection is the identity
            return this.ftuple;
        if (this.ftuple instanceof AllTuple) 
            return AllTuple.getInstance(getCodomain(), getDomain());
        if (this.ftuple instanceof EmptyTuple ) 
            return getFalse();
        if (this.ftuple instanceof TupleProjection) 
            return new TupleProjection(((TupleProjection) this.ftuple).ftuple , this.k);
        // main case
        if ( this.ftuple instanceof Tuple ){
            // VERY IMPORTANT: we assume that the inequation set corresponding to the filter has been shown "satisfiable"...
            var tuple = (Tuple) this.ftuple ; 
            var components = tuple.getHomSubTuple(this.cc);
            //System.out.println(this); //debug
            if ( SetFunction.differentFromZero(components.subList(this.k, tuple.size() )) ) {
                Guard filter = tuple.filter() , guard = tuple.guard(); 
                SortedMap<ColorClass,List<? extends SetFunction>> projection = Util.singleSortedMap(this.cc, components.subList(0, this.k ));
                Domain codom = getCodomain(); 
                if (filter.isTrivial())  // rule 3 (simplest case)
                    return new Tuple (filter.clone(codom), projection, guard); 
                
                var equalityMap = filter.equalityMap();
                if (equalityMap.isEmpty() || ! filter.membMap().isEmpty() )
                    return this;
                // the filter is built of (in)equalities
                // equalities should only refer to i) "equal" (mod-succ) components ii) of cardlb > 1, as for inequalities condition i) may hold for single forms
                Set<Equality> equalities   = equalityMap.get(this.cc).getOrDefault(true,  Collections.emptySortedSet()), 
                              inequalities = equalityMap.get(this.cc).getOrDefault(false, Collections.emptySortedSet());
                Set<Guard> eq_restr = Guard.restriction(equalities, this.k), ieq_restr;
                if (eq_restr.size() != equalities.size() ) { // Lemma 11: some equalities refer to the tuple's extended part ...
                    eq_restr.addAll(inequalities); // inequalities are added
                    return new TupleProjection(new Tuple (filter.andFactory(eq_restr), tuple.getHomSubTuples(), guard), this.k);
                }
                // equalities (if any) refer to the original part of the tuple
                ieq_restr = Guard.restriction(inequalities, this.k);
                if (ieq_restr.size()== inequalities.size() ) {
                    return new Tuple (filter.clone(codom), projection, guard); // rule 3
                }
                // some inequality refers to the tuple's extension
                final Interval bounds = cardBounds(inequalities,  components, this.k) ; // the interval [minlb, maxub] of cardinality's bounds
                final int minlb  = bounds.lb();
                if ( minlb > 1) {   
                    final InequalityGraph igraph = new InequalityGraph(inequalities);
                    if (this.monBound == null)
                        this.monBound = monoBound(igraph);
                    //System.out.println(this +" mon_bound: "+this.monBound+ " (minlb="+minlb+ ") (maxub="+maxub+')'); //debug
                    //corollary 14 + lemma 4.10: either minlb > proj's mon_bound or g[T] is f.p and the inequalities' eq_restr is a clique
                    if ( minlb > this.monBound  ) {
                        eq_restr.addAll(ieq_restr); // the k-restriction of the filter
                        return new Tuple( And.buildAndFormWithD(eq_restr, codom), projection, guard); // k-restr of the whole tuple
                    }
                    
                    if (igraph.isSimpleForm()) {
                        final boolean clique_k = igraph.isClique(this.k);
                        if (clique_k && minlb >= igraph.chromaticNumber() ) {
                            eq_restr.addAll(ieq_restr); 
                            return new Tuple( And.buildAndFormWithD(eq_restr, codom), projection, guard); // k-restr of the whole tuple
                        }                        
                        // minlb <= mon_bound and either g is not a single-form or the inequality graph's eq_restr is not a clique, or some extra components has cardlb minlb < X
                        FunctionTuple ft = tuple.reduceFilterClassIneqs(inequalities, this.cc);
                        if (ft != tuple)  // g single-form but [g]T not a fixed-point (we may drop this condition)
                            return new TupleProjection(ft, this.k); 
                        // if the graph is not a clique, we consider the projection monotonicity-bound
                        if (!clique_k && bounds.ub() >= 0 && bounds.ub() <= this.monBound ) { // the cardinalities u.b. <= mon_bound (otherwise a split would be needed)     
                            codom = tuple.getCodomain();
                            // [g]T is a f.p.: there should be (assumption) a pair of independent nodes X_i, X_j, i <=k , j <= k - DOES THE CHECK MAY BE REMOVED?
                            final Projection[] i_nodes = igraph.getIndependentNodesLe(this.k); 
                            //System.out.println(toStringDetailed()+": added constraint: "+i_nodes[0]+","+i_nodes[1]); //debug
                            Set<Equality> f_args = new HashSet<>(inequalities); // a copy of the filter 
                            f_args.addAll(equalities);
                            final var args_1 = new HashSet<Guard>(f_args);
                            final var args_2 = new HashSet<Guard>(f_args);
                            args_1.add(Equality.builder(i_nodes[0],i_nodes[1],true, codom ));
                            args_2.add(Equality.builder(i_nodes[0],i_nodes[1],false,codom));
                            TupleProjection tp_1 = new TupleProjection(new Tuple (And.factory(args_1), tuple.getHomSubTuples(), guard), this.k),
                                            tp_2 = new TupleProjection(new Tuple (And.factory(args_2), tuple.getHomSubTuples(), guard), this.k);
                            return TupleSum.factory(true, tp_1, tp_2);                        
                        }
                    } else { // the filter is not a simple form
                        final var ccard = this.cc.fixedSize();
                        if (ccard > 0) { // fixed-size color class 
                            Iterator<Set<Equality>> ite = Util.mapFeature(inequalities, e -> new Pair<>(e.firstIndex(), e.secondIndex())). values().iterator();
                            Set<Equality> maxsim = ite.next(), next;
                            while (ite.hasNext())
                                if ( (next = ite.next() ).size() > maxsim.size() )
                                    maxsim = next;
                            //the greatest sub-list of similar ineqs is replaced by a cooresponding sum of equalities 
                            var args = new HashSet<>(inequalities); 
                            args.removeAll(maxsim);
                            args.addAll(equalities);
                            Or nested = (Or) Or.factory(Equality.missingOppEqs(maxsim, ccard), true);
                            return new TupleProjection(new Tuple ( ((And)And.factory(args)).distribute(nested), tuple.getHomSubTuples(), guard), this.k);
                        }
                    }
                     // new! we may direcltly set the split offset
                     this.splitdelim = this.cc.setDelim(this.monBound - minlb + 1) ; // this quantity is >= 1
                }
            }
        }
        return this; 
    }
    
            
    /**
     * find a minimal upper bound (monotonicity bound) for the the (k-)projection of [g']T to be
     * equivalent to the syntactical restriction of the [g']T.
     * tuple's components cardinalities are taken into account
     * @param g the inequation graph associated with the filter
     * @return a minimal upper bound for the k-projection to be equivalent to the k-(syntactical) eq_restr
     */
    private int monoBound (InequalityGraph g) {   
        if (g.isEmpty() || g.indexSetGt(this.k).isEmpty()) 
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
     * {i}, i > k and degree(v_i)+gap_i == minlb({degree(v_i)+gap(i)} );
     * gap(i) is the constant "gap" associated to the domain represented by the application of the i-th component
     * of the specified (extended) tuple; k is the tuple's Projection bound;
     * @return minlb({degree(i)+gap(i)}, i > k)
     * @throws NoSuchElementException if the set of vertices with index > k is empty
     */
    private int min_degree_vset(InequalityGraph g, Set<Integer> sd) {
        List<? extends ClassFunction> t = ((Tuple) this.ftuple).getHomSubTuple(this.cc);
        Iterator<Integer> it = g.indexSetGt(this.k).iterator();
        int i   = it.next(), min = g.degree(i) + ((SetFunction)t.get(i-1)).gap(), d_i;
        sd.add(i);
        while ( it.hasNext() ) 
            if ( ( d_i = g.degree(i = it.next()) + ((SetFunction)t.get(i-1)).gap() ) <= min) {
                if (d_i < min) {
                    min = d_i;
                    sd.clear();
                }
                sd.add(i);
            }
        
        return min;
    }
    
        
    /**
     * @return the interval [minlb, maxub] of cardinality's bounds of tuple functions (meant as domains)
     * referred to by a inequalities with indices greater than a given value;
     * <code>[0,0]</code>, if the cardinality of some functions is undefined
     */
    private static Interval cardBounds(Collection<? extends Equality> eqs, List<? extends SetFunction> components, int k) {
        int minlb = Integer.MAX_VALUE, maxub = 0;
        for (int x : Guard.indexSet(eqs)) {
            if (x > k) {
                Interval card = components.get(x -1).card();
                if ( card == null )
                    return new Interval(0,0);
                
                if (minlb > card.lb())
                    minlb = card.lb();
                if (maxub >= 0 && ( card.ub() < 0 || maxub < card.ub() ))
                    maxub = card.ub(); 
            }
        }
        return maxub < 0 ? new Interval(minlb) : new Interval(minlb, maxub);
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