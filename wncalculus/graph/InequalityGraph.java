package wncalculus.graph;

import java.util.*;
import java.util.function.Function;
import wncalculus.classfunction.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.Equality;
import wncalculus.util.Util;
import static wncalculus.classfunction.Projection.*;
import wncalculus.util.Pair;

/**
 *
 * @author Lorenzo Capra
 this class provides a simple-graph representation for a color-homogenous set of inequalities
 the corresponding graphs are connected
 */
public final class InequalityGraph extends Graph<Projection> {

    private ColorClass cc; //the inequations' color class    
    private HashMap<Integer, HashSet<Projection>> imap; // the "index" map of this graph ("hashing")
    
    /**
     * builds an empty graph of inequalities (private)
     */
    /*private*/public InequalityGraph () {
    }
    
    private static InequalityGraph Empty; //empty 
    
    /**
     * @param cc a color class
     * @return an empty inequality graph
     */
    public static InequalityGraph Empty(ColorClass cc) {
        if (Empty == null) {
            Empty = new InequalityGraph();
            Empty.cc = cc;
        } 
        return Empty;
    }
    
    /** builds an inequation graph from a (non-empty) set of inequalities (assumed of the same colour)
     *  by adding (if needed) the implicit ones
     *  the resulting graph has a "minimal" form, possibly different from the "canonical" form of inequations
     *  @param c a collection of inequalities 
     *  @throws NoSuchElementException in the case of an empty set
     *  @throws Error if the graph'X building fails
     */
    public InequalityGraph (Set<? extends Equality> c)  {
        this.cc = c.iterator().next().getSort();
        this.imap = new HashMap<>();
        Function<Equality,Boolean> addEdge = this.cc.isOrdered() ?  e -> addOrdIneq(e) : e -> addIneq(e);
        c.stream().filter(eq -> (! addEdge.apply(eq) )).forEachOrdered(_item -> {
            throw new Error("inequality graph building failed\n:"+c);
        });
    }
    
    
    /** @return  the inequations' color class */
    public ColorClass getColorClass () {
        return this.cc;
    }
    
    /** @return  the inequations' color constraint lower bound */
    public int lb() {
        return this.cc.lb();
    }
                
    /**
     * add implicit inequalities (edges) between a vertex and each one in the specified list
     * @param p a vertex
     * @param vlist a list vertexes
     * @throws IllegalArgumentException if for any reasons the operation fails
     */
    private void addImplicitIneqs(Projection p , Collection <? extends Projection> vlist) {
        vlist.stream().filter(v ->  p.equals(v) || ! addEdge(p, v) ).forEachOrdered(_item -> {
            throw new IllegalArgumentException("failed adding implicit inequalities!"); // for debug purposes: could be eliminated 
        });
    }
    
   
    /**
     * add an arc to <code>this</code> graph preserving node minimality (it means that,
     * in the case of an ordered class, projection'X successors may be rearranged)
     * and adding possible implicit inequalities between nodes with the same index
     * (conjecture: calculates a vertex-minimal graph)
     * @param v1 first vertex
     * @param v2 second vertex
     * @return <code>true</code> if the operation succeeds
     */
    private boolean addOrdIneq (Equality ineq) {
        Projection v1 = ineq.getArg1(), v2 = ineq.getArg2();
        final int i1 = v1.getIndex(), i2 = v2.getIndex(), succ2 = v2.getSucc() ;
        final Set<Projection> prset1 = this.imap.get(i1), prset2 = this.imap.get(i2); // we consider the "current" vertex-set
        if ( prset1 == null || prset2 == null ) {
        	if ( prset1 != null && ! prset1.contains (v1)) { // there exists a vertex with index 1 but not with index 2
                    v1 = prset1.iterator().next(); // we take one with index 1 ..
                    v2 = v2.setExp( v1.getSucc() + succ2 );
             }
             else if (prset2 != null && ! prset2.contains (v2)) { // there exists a vertex with index 2 but not with index 1
                    v2 = prset2.iterator().next() ; // we take one with index 2 ..
                    v1 = v1.setExp( v2.getSucc() - succ2);
                }
        } else if (! (prset1.contains (v1) && prset2.contains (v2)) )  { // there exist both a vertex with index 1 and one with index 2
                boolean found = false;
                search: for (Projection n1 : prset1) {
                           for (Projection n2: prset2)
                               if (offset(n2.getSucc(), n1.getSucc()) == succ2) {
                                   found = true;
                                   v1 = n1; v2 = n2;
                                   break search;
                               }
                }
                if (!found) {
                    v1 = prset1.iterator().next(); // we take one with index 1 ..
                    v2 = v2.setExp( v1.getSucc() + succ2 );
                }	
        }
        
        if ( super.addVertex(v1) ){ // v1 is new 
            if ( prset1 != null ) // there were other vertices with the same index
                addImplicitIneqs(v1, prset1); // implicit inequalities between v1 and existing vertices with the same index
            setImap(v1);     
        }
        if ( super.addVertex(v2) ){ // v2 is new (as above) .. 
            if ( prset2 != null ) 
                addImplicitIneqs(v2, prset2); 
            setImap(v2);
        }
        //System.out.println("aggiunto "+ v1+','+v2);
        return super.addEdge(v1, v2); // the new arc is built; 
    }
    
    
    /**
     * computes the difference mod-n (considering the colour class constant size in the non-parametric case)
     * @param a a value
     * @param b a value
     * @return computes the difference (a-b) mod-n (considering the graph's colour class)
     */
    private int offset(int a, int b) {
        int size = this.cc.fixedSize(), diff = a - b;
        if (size > 0) // the color class is non-parametric
            diff = Util.valueModN(diff, size);
        
        return diff;
    }
    
    /**
     * add a new edge (optimized version for unordered color classes) 
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @return <code>true</code> if and only if the graph is modified
     */
    private boolean addIneq (Equality ineq) {
        Projection v1 = ineq.getArg1(), v2 = ineq.getArg2();
        if ( super.addVertex(v1) )
            setImap(v1);
        if ( super.addVertex(v2) )
            setImap(v2);
        
        return super.addEdge(v1, v2);
    }
    
    /**
     * "updates" the index-map of the graph (assuming it already exists) w.r.t. a given Projection
     * which is added to the set mapped by the Projection's index 
     * if there is no entry with that index it is first created
     * @param v a Projection (vertex)
     */
     private void setImap (Projection v) {
        int i = v.getIndex();
        HashSet<Projection> iset = this.imap.get(i);
        if (iset == null) // index i not yet mapped
            this.imap.put(i, iset = new HashSet<>()); 
        iset.add(v);   
     }
     
         
    /**
     * @param i a vertex index
     * @return the set (list) of vertexes of the graph with that index;
     * <code>null</code> if there are no such vertices
     */
    public Set<? extends Projection> vertexSet (int i) {
        return this.imap.get(i);
    }
        
    /**
     * calculates the set of vertices with indexes &le; k 
     * @param k a bound
     * @return the set of vertexes of the graph with indexes &le; k 
     */
    public Set<Projection> vertexSetLe(int k) {
        HashSet<Projection> vset = new HashSet<>();
        this.imap.entrySet().stream().filter(v -> v.getKey() <= k).forEachOrdered(v -> {  vset.addAll(v.getValue()); });
            
        return vset;
    }
    
    /**     
     * destructively removes a set of nodes from this inequation graph
     * builds on <code>Graph.removeVertices</code>
     * @param vlist a set of nodes to be removed
     * @return <code>this</code>
     */
    public InequalityGraph removeAll (Collection<? extends Projection> vlist) {
        removeVertices(vlist);
        vlist.forEach( v -> {
            int k = v.getIndex();
            Set<Projection> x = this.imap.get(k);
            if (x != null && x.remove(v) && x.isEmpty()) 
                this.imap.remove(k);
        });
        
        return this;
    }
    
    /**
     * 
    * @return the index-set of graph'X vertices  
     */
    public Set<? extends Integer> indexSet () {
        return this.imap.keySet();
    }
    
    /**
     * 
     * @param k a specified position (it usually denotes the original fixedSize of an extended tuple..)
     * @return the set of graph'X vertex indexes greater than k  
     */
    public Set<Integer> indexSetGt (int k) {
        Set<Integer> iset = new HashSet<>();
        this.imap.keySet().stream().filter( i -> i > k).forEachOrdered( i -> { iset.add(i); });

        return iset;
    }
        
    
    /**
     * computes the cardinality of the sum of tuple'X components (representing variable domains
     * referred to by inequalities
     * @param t the right-tuple, in the form of a list
     * @return the cardinality of the sum of components; <code>null</code> if it cannot be computed.
     * @throws ClassCastException if any term in t is not a SetFunction
     */
    public Interval ineqDomainCard (List<? extends SetFunction> t)  {
       Set<SetFunction> comps = new HashSet<>(); //tuple components corresponding to inequalities
       vertexSet().forEach((var p) -> { comps.add (Successor.factory( p.getSucc() , t.get( p.getIndex() - 1 )) ); });
       SetFunction u = Union.factory(comps,false);
       if (u instanceof NonTerminal) //optimization
           u =  (SetFunction) u.normalize( );
       //System.out.println("card di: "+comps + " ->"+ u); //debug
       return u.card();
    }
   
    /**
     * return the "cumulative" degree of this graph'X vertexes with the specified
     * index (disregarding by the way implicit relations between similar vertices)
     * @param i the vertices' index
     * @return the cumulative degree of vertexes with index <code>i</code>
     * @throws NullPointerException if there are no such vertices
     */
    public int degree(int i) {
        Collection<? extends Projection> vset = vertexSet(i);
        int di = 0, auto = vset.size(); //the "auto-degree"
        if (auto < 2) 
            di = degree (vset.iterator().next());
        else {
            di = vset.stream().map( v -> degree(v)).reduce(di, Integer::sum);
            di -= auto * (auto -1) ; // >= 0
        }
        
        return di ; 
    }
    
    /**
     @return <code>true</code> if and only if the corresponding guard is a simple form
    */
    public boolean isSimpleForm() {
        return ! this.cc.isOrdered() || this.imap.values().stream().allMatch( e -> e.size() == 1 ) ;
    }
    
    /**
     * 
     * @param k an index bound
     * @return <code>true</code> if and only if the subgraph composed by vertexes whose index is less or equal than k is a clique 
     */
    public boolean isClique (int k) {
        return subGraph(vertexSetLe(k)).isClique();
    }
    
    /**
     * computes two  non-adjacent vertices v_i, v_j, i != j, i, j with indexes &le; k
     * @param k a given bound
     * @return an array with two non adjacent nodes with indices &le; k; <tt>null</tt>
     * if there are not two such vertices
     */
    public Projection[] getIndependentNodesLe(int k) {
        Set<Projection> vset_k = vertexSetLe(k);
        Projection[] a = null;
        for (Projection v : vset_k ) {
            if ( degree(v) < order() -1) {// the vertex degree is not max
                Set<Projection> vset = new HashSet<>(vset_k);
                vset.removeAll( adjiacent(v) ); // the nodes v_j, j > k, not adjacent to v
                if ( vset.size() > 1 ) {
                    vset.remove(v); // v is contained in vset
                    a = new Projection[2];
                    a[0] = v;
                    a[1] = vset.iterator().next();
                    break;
                }
            }
        }
        
        return a;
    }
    
    /**
     * 
     * @return a deep copy of <code>this</code> graph 
     */
    @Override
    public InequalityGraph clone () {
        InequalityGraph copy = (InequalityGraph) super.clone(); // deep copy of the graph structure
        copy.cc = this.cc;
        copy.imap = new HashMap<>(); // deep copy
        this.imap.entrySet().forEach( e -> { copy.imap.put(e.getKey(), new HashSet<>(e.getValue())); });
        
        return copy;
    }
        
    @Override
    public String toString () {
        return super.toString() +'\n'+this.cc+ (isSimpleForm() ? "\nsingle form" : "")+'\n'+this.imap;
    }
    
    /**
     * 
     * @param l the (possibly null) associated tuple
     * @return the split-delimiter of the corresponding inequality-set, that is,
     * the maximal offset between successors (with the same index ?)
     */
    public int splitDelimiter(List<? extends SetFunction> l) {
        int delim = isSimpleForm() ? 0 : succDelim(maxSuccOffset(vertexSet(), this.cc), this.cc);
        if (delim == 0 && l != null) { //may we restrict to ordered classes?
           Interval ineqCard = ineqDomainCard(l);
           if (ineqCard == null)
               ineqCard = this.cc.card(); // if the variables "domains" cannot be computed we consider the "worst" case
           var X = chromaticNumber() - ineqCard.lb();
           if ( X  > 0)
               delim = X;
        }
        //System.out.println("splitdelim di "+this+": "+delim);
        return delim;
    }
    
    /**
     * 
     * @return the index-sets of connected components of <tt>this</tt> graph
     * (it builds on the superclass method)
     */
    public Set<HashSet<Integer>> connectedIndices () {
    	Set<HashSet<Integer>> i_set = new HashSet<>();
        connectedComponents().stream().map((var x) -> {
            HashSet<Integer> i_x = new HashSet<>();
            x.forEach(p -> { i_x.add(p.getIndex()); });
            return i_x;
        }).forEachOrdered(i_x -> {
            i_set.add(i_x);
        });
   
    	return i_set;
    }
    
    /**
     * find a minimal upper bound (monotonicity bound) for the the (k-)projection of [g']T to be
     * equivalent to the syntactical restriction of the [g']T.
     * tuple's components cardinalities are taken into account
     * @param g the inequation graph associated with the filter
     * @return a minimal upper bound for the k-projection to be equivalent to the (syntactical) k-restriction
     */
    public int prMonoBound (final int k, final List<? extends SetFunction> t) {
        return clone().prMonoBoundDestr(t, k);
    }
    
    /**
     * auxiliary method operating in a destructivr way (for efficiency)
     * @param t the tuple to project
     * @param k the projection's size
     * @return the projection's monotonicity bound (@see prMonoBound)
     */
    private int prMonoBoundDestr (final List<? extends SetFunction> t, final int k) {   
        Set<Integer> iset ;
        if (isEmpty() || (iset = indexSetGt(k)). isEmpty()) 
            return 0;
        else{
            var p = min_degree_vset(t, iset);
            var to_remove   = new HashSet<Projection>();
            p.getKey().forEach(i -> { to_remove.addAll(vertexSet(i)); });
            return Math.max(p.getValue(), removeAll(to_remove). prMonoBoundDestr(t, k) );
        }
    }
    
    
    /**
     * calculates the set of indices {i}, s.t., i > k and degree(v_i)+gap_i == minlb({degree(v_i)+gap(i)} );
     * gap(i) is the constant "gap" associated to the domain represented by the application of the i-th component
     * of the specified (extended) tuple; k is the tuple's Projection bound;
     * @param sd the (initially empty) set to fill with those vertices i such that i > k and degree(i)+gap(i) is min
     * @param isetgt_k the pre-calculated set of vertices with index > k
     * @return a pair containing the set minlb({degree(i)+gap(i)}, i > k) and (for convenience) 
     * @throws NoSuchElementException if <code>isetgt_k</code> is empty
     */
    public Pair<Set<Integer>,Integer> min_degree_vset(final List<? extends SetFunction> t, final Set<? extends Integer> isetgt_k) {
        var sd = new HashSet<Integer>();
        var it = isetgt_k.iterator();
        int i = it.next(), min = degreePlusGap(i,t);
        for (sd.add(i); it.hasNext(); i = it.next()) { 
            var d_i = degreePlusGap(i,t);
            if ( d_i <= min) {
                if (d_i < min) {
                    min = d_i;
                    sd.clear();
                }
                sd.add(i);
            }
        }
        return new Pair<>(sd, min);
    }
    
    /*
    calculates the sum of i-th vertex's degree and the gap of the corresponding tuple's component
    */
    private int degreePlusGap(final int i,  final List<? extends SetFunction> t) {
        return degree(i) + t.get(i-1).gap();
    }
}
