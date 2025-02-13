package tuple;

import java.util.*;
import expr.*;
import graph.InequalityGraph;
import classfunction.*;
import color.ColorClass;
import guard.*;
import util.*;

/**
 * this class defines the Projection on the first k components of a one-sorted WN function-tuple F 
 its precise semantics is &lang;X_i,..,X_k&rang;  \circle F ;
 * @author Lorenzo Capra
 */
public final class TupleProjection implements FunctionTuple, UnaryOp<FunctionTuple> {
    
    public  final int k ; // the Projection size
    private final ColorClass cc; // the Projection's color class
    private final FunctionTuple ftuple; // the function argument 
    private final Domain  codomain; 
    private boolean simplified;
    //cache
    private Integer mboundoff; // the offset to the get the projection monotonicity bound
    private InequalityGraph graph;
    
    final static String OPSYMB =  "Prj_";//"\u220F_"; 
    
    
    /** base constructor
     * @param f the Projection function-tuple
     * @param size the Projection's size
     * @throws IllegalArgumentException if the specified function is not one-sorted 
     */
    public TupleProjection (final FunctionTuple f, final int size) {
        if (( this.cc = (ColorClass) f.oneSorted() ) == null) 
            throw new IllegalArgumentException("Many-sorted funtion-tuple to project!");
        
        this.k = checkSize(size , f.size() );
        this.ftuple = f;
        this.codomain = new Domain(this.cc, this.k);
    }
    
    private static int checkSize (int k , int arity) {
        if (k <  1  || k >  arity ) 
            throw new IndexOutOfBoundsException("inconsistent projection bound ("+k+")");
        
        return k;
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
        return this.codomain;
    }
    
    private InequalityGraph getGraph(final Set<Equality> inequalities) {
        if (this.graph == null) {
            this.graph = new InequalityGraph(inequalities); 
        }
        return this.graph;
    }
    
    private int getBound(final Set<Equality> inequalities) {
        if (this.mboundoff == null) {
            this.mboundoff = getGraph(inequalities).monoBound(this.k, ((Tuple)this.ftuple).getHomSubTuple(this.cc));
        }
        return this.mboundoff;
    }
        
    @Override
    public Map<Sort, Integer> splitDelimiters() {
        Map<Sort, Integer> delimiters  = this.ftuple.splitDelimiters();
        if (this.mboundoff != null && this.mboundoff > 0) {
            ColorClass.setDelim(delimiters, this.cc, this.cc.setDelim(this.mboundoff));
        }
        return delimiters;   
    }
            
    @Override
    public FunctionTuple specSimplify( ) {
        //System.out.println("TupleProjection:\n"+toStringDetailed()); //debug
        if (this.k == this.ftuple.size())  // the Projection is the identity
            return this.ftuple;
        if (this.ftuple instanceof AllTuple) 
            return AllTuple.getInstance(getCodomain(), getDomain());
        if (this.ftuple instanceof EmptyTuple ) 
            return getFalse();
        if (this.ftuple instanceof TupleProjection) 
            return new TupleProjection(((TupleProjection) this.ftuple).ftuple , this.k);
        // main case
        if ( this.ftuple instanceof Tuple && this.ftuple.simplified()){ // VERY IMPORTANT: we assume that the inequation set corresponding to the filter has been shown "satisfiable"...
            final var tuple = (Tuple) this.ftuple ; 
            final var components = tuple.getHomSubTuple(this.cc);
            //System.out.println(this); //debug
            if ( SetFunction.differentFromZero(components.subList(this.k, tuple.size() )) ) {
                final Guard filter = tuple.filter() , guard = tuple.guard(); 
                final SortedMap<ColorClass,List<? extends SetFunction>> projected = Util.singleSortedMap(this.cc, components.subList(0, this.k ));
                var codom = getCodomain(); 
                if (filter.isTrivial())  // rule 3 (simplest case)
                    return new Tuple (filter.clone(codom), projected, guard); 
                else {
                    final var equalityMap = filter.equalityMap();
                    if (!equalityMap.isEmpty() && filter.membMap().isEmpty() ) { // the filter is built of (in)equalities that should only refer to i) "equal" (mod-succ) components ii) of cardlb > 1, as for inequalities condition i) may hold for single forms
                        Set<Equality> equalities   = equalityMap.get(this.cc).getOrDefault(true,  Collections.emptySortedSet()), 
                                      inequalities = equalityMap.get(this.cc).getOrDefault(false, Collections.emptySortedSet());
                        final Set<Guard> eq_restr = Guard.restriction(equalities, this.k), ieq_restr;
                        if (eq_restr.size() != equalities.size() ) { // Lemma 11: some equalities refer to the tuple's extended part ...
                            eq_restr.addAll(inequalities); // inequalities are added
                            return new TupleProjection(new Tuple (filter.andFactory(eq_restr), tuple.getHomSubTuples(), guard), this.k);
                        }
                        // equalities (if any) refer to the original part of the tuple
                        ieq_restr = Guard.restriction(inequalities, this.k);
                        if (ieq_restr.size()== inequalities.size() ) {
                            return new Tuple (filter.clone(codom), projected, guard); // rule 3
                        }
                        // some inequality refers to the extended part of the tuple
                        getBound(inequalities); // we use the cache
                        System.out.println("mon_bound: "+this.mboundoff); //debug
                        if (this.mboundoff == 0) { // corollary 14: the tuple's cardinality lower bounds satisfy the projection mon. bound
                            eq_restr.addAll(ieq_restr); // the k-restriction of the filter
                            return new Tuple( And.buildAndFormWithD(eq_restr, codom), projected, guard); // k-restr of the whole tuple
                        }
                        // the tuple's lower bounds do not satisfy the projection mon. bound
                        if (this.graph.isSimpleForm()) {
                            final boolean clique_k = this.graph.isClique(this.k);
                            if (clique_k && minLb(inequalities, components, this.k) >= this.graph.chromaticNumber() ) { //Lemmaa 10: g[T] is simple, the inequalities restriction is a clique, and the lower bounds are at least as the chromatic numb.
                                eq_restr.addAll(ieq_restr); 
                                return new Tuple( And.buildAndFormWithD(eq_restr, codom), projected, guard); // k-restr of the tuple
                            } 
                            else { // either the inequalities restriction is not a clique or some extended component (that ..) has card. lb  < X
                                FunctionTuple ft = tuple.reduceFilterClassIneqs(inequalities, this.cc);
                                if (ft != tuple) {  // [g]T is not a fixed-point (we may drop this condition)
                                    return new TupleProjection(ft, this.k); 
                                }
                                if (! clique_k ) { // the cardinalities u.b. <= mon_bound (otherwise a split would be needed)     
                                    codom = tuple.getCodomain();
                                    // [g]T is a f.p.: there should be (assumption) a pair of independent nodes X_i, X_j, i <=k , j <= k - DOES THE CHECK MAY BE REMOVED?
                                    final Projection[] i_nodes = this.graph.getIndependentNodesLe(this.k); 
                                    //System.out.println(toStringDetailed()+": added constraint: "+i_nodes[0]+","+i_nodes[1]); //debug
                                    final Set<Equality> f_args = new HashSet<>(inequalities); // a copy of the filter 
                                    f_args.addAll(equalities);
                                    final var args_1 = new HashSet<Guard>(f_args);
                                    final var args_2 = new HashSet<Guard>(f_args);
                                    args_1.add(Equality.builder(i_nodes[0],i_nodes[1],true, codom ));
                                    args_2.add(Equality.builder(i_nodes[0],i_nodes[1],false,codom));
                                    TupleProjection tp_1 = new TupleProjection(new Tuple (And.factory(args_1), tuple.getHomSubTuples(), guard), this.k),
                                                    tp_2 = new TupleProjection(new Tuple (And.factory(args_2), tuple.getHomSubTuples(), guard), this.k);
                                    return TupleSum.factory(true, tp_1, tp_2);                        
                                }
                            }
                        } 
                        else { // the filter is not a simple form
                            final var ccard = this.cc.fixedSize();
                            if (ccard > 0) { // fixed-size color class 
                                final Iterator<Set<Equality>> ite = Util.mapFeature(inequalities, e -> new Pair<>(e.firstIndex(), e.secondIndex())). values().iterator();
                                Set<Equality> maxsim = ite.next(), next;
                                while (ite.hasNext())
                                    if ( (next = ite.next() ).size() > maxsim.size() )
                                        maxsim = next;
                                //the greatest sub-list of similar ineqs is replaced by a cooresponding sum of equalities 
                                final var args = new HashSet<>(inequalities); 
                                args.removeAll(maxsim);
                                args.addAll(equalities);
                                final Or nested = (Or) Or.factory(Equality.missingOppEqs(maxsim, ccard), true);
                                return new TupleProjection(new Tuple ( ((And)And.factory(args)).distribute(nested), tuple.getHomSubTuples(), guard), this.k);
                            }
                        }
                    }
                }
            }
        }
        return this; 
    }
        
    /**
     * @return the minimun of cardinality lower bounds of tuple functions (meant as domains)
     * referred to by inequalities with indices greater than a given value;
     * <code>0</code>, if the cardinality of some functions is undefined
     */
    private static int minLb(Collection<? extends Equality> eqs, List<? extends SetFunction> components, int k) {
        var minlb = Integer.MAX_VALUE;
        for (int x : Guard.indexSet(eqs)) {
            if (x > k) {
                Interval card = components.get(x -1).card();
                if ( card == null )
                    return 0;
                if (minlb > card.lb())
                    minlb = card.lb(); 
            }
        }
        return minlb;
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