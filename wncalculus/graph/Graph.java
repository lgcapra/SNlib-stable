package wncalculus.graph;

import java.util.*;

/**
 * This generic class implements (simple) undirected graphs  
 * @author Lorenzo Capra
 * @param <E> the type of graph's nodes
 */
public class Graph<E> {
    
    private final Map<E, HashSet<E>>  adjlist ; // the adjacency list of the graph
    
    
    // hashing fields
    private Integer chromaticNumber; // the chromatic number
    private Collection<HashSet<E>> cliques; //auxiliary variable storing the cliques of the graph
    private Set<HashSet<E>> connected; //the connected components
    
    /**
     * builds an empty graph
     */
    public Graph() {
        this.adjlist  =  new HashMap<>() ;
    }        
    
    /**
     * when invoked resets the "hashing" fields of the class
     */
    private void reset() {
        this.chromaticNumber = null;
        this.cliques = null;
    }
    
    
    /** add a new (isolated) vertex to this graph, if it is not yet present
     * @param v a (new) vertex
     * @return <code>true</code> if and only if the graph is modified 
    */
    public boolean addVertex(E v) {
        boolean changed = ! contains(v);
        if ( changed ) { // v yet not present
            this.adjlist.put(v, new HashSet<>());
            reset();
        }
        
        return changed;
    }    
    
    
    /** overloaded version of contains
     * @param v a vertex
     * @return <code>true</code> if and only if the graph contains the specified element
     */
    public final boolean contains (E v) {
        return this.adjlist.containsKey(v);
    }
    
    /**
     * 
     * @param v a vertex
     * @return <code>true</code> if and only if the specified eleemnt is an
     * isolated vertex of the graph
     */
    public boolean isolated(E v) {
        Set<E> s = this.adjlist.get(v);
        return s != null && s.isEmpty();
    }
    
    /** 
     * @param src a vertex
     * @param dest another vertex
     * @return <code>true</code> if and only if (first,second) is an edge of the graph
     */
    public final boolean contains (E src, E dest) {
        return contains(src) && this.adjlist.get(src).contains(dest);
    }
    
    /** 
     * @return the set of vertexes of the graph 
     */
    public final Set<? extends E> vertexSet() {    
        return this.adjlist.keySet();
    }
  
    /**
     * 
     * @param v a graph vertex
     * @return the set of nodes adjacent the specified vertex; <code>null</code> if v
     * is not a vertex of the graph
     */
    public Set<? extends E> adjiacent (E v) {
        return this.adjlist.get(v);
    }
      
    /**
     * @param v a vertex of <code>this</code> graph
     * @return the vertex degree
     * @throws NullPointerException if <code>v</code> is not a vertex of the graph
     */
    public final int degree(E v) {
        return adjiacent(v).size();
    }
        
    /**
     * 
     * @return the order of <code>this</code> graph 
     */
    public final int order() { 
        return this.adjlist.size();
    }
    
    
     /**
     * @return true if the graph is empty 
     */
    public final boolean isEmpty() {     
        return order() == 0;
    }
    
/**
 * 
 * @return the complement of <code>this</code> graph 
 */    
    public final Graph<E> complement () {
        Graph<E> complement = new Graph<> ();
        this.adjlist.entrySet().forEach(e -> {
            HashSet<E> adjv = new HashSet<>(vertexSet()); //copy of this.vertexsSet()
            adjv.removeAll(e.getValue() );
            adjv.remove(e.getKey() );
            complement.adjlist.put(e.getKey(), adjv);
        });
        //System.out.println("complementare: "+complement);//debug
        return complement;
     }
    
        
    /**
     * @param num a bitmap representation of a subset of vertexes of the graph
     * (must range between 0 and 2^{size} -1),  we assume vertexSet().size() <= 32
     * @return the corresponding subset of vertexes
     */
    private Set<? extends E> subSet (int num) {
        Set<E> subset = new HashSet<>(); 
        for (Iterator<? extends E> it = vertexSet().iterator(); num != 0 && it.hasNext() ; num >>= 1) {
            E v =it.next(); 
            if ((num & 1) == 1) // least significant bit of num is set
                subset.add(v);    
        }
        
        return subset;
    }
    
    /**
     * @param setv a (sub)set of vertexes of this graph
     * @return the corresponding bit-map representation.
     * Assumes set.size() <= 32 and set is a subset of this.vertexSet()
     */
    private int subSetIndex (HashSet<? extends E> set) {
        int res = 0, j = 0;
        for (E v : vertexSet()) { // j is the implicit position of a vertex
            if (set.isEmpty())
                break; // otpimization
            if ( set.contains( v ) ) { //efficient because an hashSet is used 
                res |= 1 << j; // "setta" il j-esimo bit di res
                set.remove(v); // for efficiency
            }
            ++j;
        }
            
        return res;    
    }
    
    /** povides the cardinality of the vertex power-set (should be private)*/
    private int powerSetCard () {
        return (int) Math.pow(2, vertexSet().size());
    }
    
     /**
     * builds the subgraph of this graph induced by a set of vertexes
     * @param vset the specified vertex set
     * @return the subgraph induced by vset (<code>this</code> if the set includes
     * all vertices)
     * @throws NullPointerException if some vertex of vset is not present in the graph
     */ 
    public Graph<E> subGraph(Set<? extends E> vset) {
       Graph<E> subgraph = this;
       if (! vset.equals(vertexSet())) {
           subgraph = new Graph<>(); // initially empty 
           for (E v : vset) {
               HashSet<E>  redadjv= new HashSet<>( adjiacent(v) );
               redadjv.retainAll(vset);
               subgraph.adjlist.put(v, redadjv);
           }
       }
       
       return subgraph;
    }
        
     /**
     * finds all maximal cliques of the graph.
     * @return a Collection of cliques (represented as set of vertices)
     */
    public Collection<? extends Set<? extends E>> getMaxCliques() {
        if (this.cliques == null) {
            this.cliques = new HashSet<>();
            findCliques(new HashSet<>(), new HashSet<>( vertexSet() ), new HashSet<>());
        }
        
        return this.cliques;
    }
        
    private void findCliques(Set<E> potential_clique, Set<? extends E> candidates, Set<E> already_found) {
        Set<E> candidates_array = new HashSet<>(candidates); 
        if (!end(candidates, already_found)) {
            candidates_array.stream().map( candidate -> {
                Set<E> new_candidates = new HashSet<>(),
                        new_already_found = new HashSet<>();
                // move candidate node to potential_clique
                potential_clique.add(candidate);
                candidates.remove(candidate);
                // create new_candidates by removing nodes in candidates not
                // connected to candidate node
                candidates.stream().filter(new_candidate ->  contains(candidate, new_candidate)).forEachOrdered(new_candidate -> {
                    new_candidates.add(new_candidate);
                });
                // create new_already_found by removing nodes in already_found
                // not connected to candidate node
                already_found.stream().filter(new_found ->  contains(candidate, new_found)).forEachOrdered(new_found -> {
                    new_already_found.add(new_found);
                });
                // if new_candidates and new_already_found are empty
                if (new_candidates.isEmpty() && new_already_found.isEmpty()) 
                    this.cliques.add(new HashSet<>(potential_clique)); // potential_clique is maximal_clique
                else 
                    findCliques(potential_clique, new_candidates, new_already_found); // recursive call
                // move candidate_node from potential_clique to already_found;
                already_found.add(candidate);
                return candidate;
            }).forEachOrdered( candidate -> { potential_clique.remove(candidate); }); 
        } 
    }
    
    private boolean end(Set<? extends E> candidates, Set<? extends E> already_found) {
        // if a node in already_found is connected to all nodes in candidates
        boolean end = false;
        int edgecounter;
        for (E found : already_found) {
            edgecounter = 0;
            for (E candidate : candidates) 
                if ( contains(found, candidate)) 
                    edgecounter++;
            if (edgecounter == candidates.size()) 
                end = true;
        }
        
        return end;
    }
    
    /**
     * 
     * @return true if and only if the graph is a clique 
     */
    public boolean isClique () {
        return getNotComplete() == null;
    }
    
     /**
     * finds all maximal independent sets of this graph.
     * @return the collection of independent sets of vertices 
     */
    public Collection<? extends Set<? extends E>> getMaxIndepSets() {
        return complement().getMaxCliques();
    }
    
    
    /**
     * @return the chromatic number of this graph 
     */
    public int chromaticNumber () {
        if (isEmpty()) 
            return 0; //by convention
        
        int size = order();
        if (size == 2)  //optimization
            return 2;
        
        if (this.chromaticNumber == null) { //caching
            if (isClique()) ////optimization
                this.chromaticNumber = size;
            else {
                int psetcard = powerSetCard();
                int [] X = new int[psetcard];
                for (int S = 1; S < psetcard ; ++S) {
                    //System.out.print("ecco S : " + S); //debug
                    Set<? extends E> S_set = subSet(S);
                    //System.out.println("; ecco S_set : " + S_set); //debug
                    X[S] = S_set.size(); //ok: è come inizializzare a valore max
                    Graph<E> sg = subGraph(S_set);
                    //System.out.println("ecco il sotto-grafo : " + sg); //debug
                    Collection<? extends Set<? extends E>> I_set = sg.getMaxIndepSets();
                    //System.out.println("ecco gli I-sets di sg: " + I_set); //debug
                    for (Set<? extends E>  I : I_set ) {
                        HashSet<E> S_minus_I = new HashSet<>(S_set);
                        S_minus_I.removeAll(I);
                        X[S] = Math.min(X[S], X[subSetIndex(S_minus_I)] + 1);
                    }
                }
                this.chromaticNumber = X[psetcard - 1];
            }
        }
        //System.out.println("X of "+this+" "+this.chromaticNumber); //debug
        return this.chromaticNumber;
    } 
           
     /**
     * makes a (deep) clone of <code>this</code> graph
     * builds on the default constructor, that each subclass of the <code>Graph</code>
     * class should be provided with
     * @return a (deep clone of <code>this</code> graph
     */ 
    @Override
    public  Graph<E> clone () {
        Class<? extends Graph> gtype = getClass();
        try {
            Graph<E> clone = gtype.newInstance();
            this.adjlist.entrySet().forEach( e -> { clone.adjlist.put(e.getKey(), new HashSet<>(e.getValue())); });
            return clone;
        } 
        catch (InstantiationException | IllegalAccessException ex) {
            throw new Error("class "+ gtype + " doesn not have a public default constructor");
        }
     }
    
    /**
     * glues two vertices of a given graph in a non-destructive way
     * @param v1 a vertex 
     * @param v2 a vertex
     * @return the graph obtained from <code>this</code> by glueing two vertices
     * (vertex v1 "survives"); <code>null</code> if v1 or v2 do not belong to the graph
     */
    public Graph<E>  glue (E v1, E v2)  {
        Graph<E> glued = null;
        if (contains(v1) && contains(v2)) {
            glued = clone(); //copy of g
            Set<E> v1_adj = glued.adjlist.get(v1);
            v1_adj.addAll(adjiacent(v2));
            v1_adj.remove(v1); //needed if v1v2 is an edge of g
            v1_adj.remove(v2); //needed if v1v2 is an edge of g
            glued.adjlist.remove(v2);
            for (Set<E> x : glued.adjlist.values()) // the left occurences of v2 are replaced with v1 
                if (x.remove(v2)) 
                    x.add(v1);
                
        }
        //System.out.println("glue("+v1+","+v2+") in "+g+'\n'+glued);
        return glued;
    }
        
    
    /**
     * add an edge between existing nodes to <code>this</code> graph in a destructive way
     * @param v1 a vertex
     * @param v2 a vertex (assumed different from v1)
     * @return <code>true</code> if and only if the new edge v1v2 is added to <code>this</code>
     * (<code>false</code> if such an edge already exists
     * @throws NullPointerException if v1 or v2 do not exist
     */
    public boolean addEdge (E v1, E v2) {
        boolean ret = this.adjlist.get(v1).add(v2) && this.adjlist.get(v2).add(v1);
        if (ret) 
            reset();
        
        return ret;
    }
    
    /**
     * remove an edge between existing nodes from <code>this</code> graph in a destructive way
     * @param v1 a vertex
     * @param v2 a vertex (assumed different from v1)
     * @return <code>true</code> if and only if the edge v1v2 is removed from <code>this</code>
     * (<code>false</code> if such an edge already exists
     * @throws NullPointerException if v1 or v2 do not exist
     */
    public boolean removeEdge (E v1, E v2) {
        boolean ret = this.adjlist.get(v1).remove(v2) && this.adjlist.get(v2).remove(v1);
        if (ret)
            reset();
        
        return ret;
    }
        
    /**
     * destructively removes the specified collection of nodes from <code>this</code> graph 
     * @param vlist a list of nodes
     * @return <code>true</code> if and only if the graph is modified
     */
    public boolean removeVertices (Collection<? extends E> vlist) {
        boolean changed = this.adjlist.keySet().removeAll(vlist);
        if (changed) {
            this.adjlist.values().forEach( x -> { x.removeAll(vlist); });
            reset();
        }
        
        return changed;
    }
    
    /**
     * add a new edge to <code>this</code> graph in a NON destructive way
     * @param v1 a vertex
     * @param v2 a vertex (assumed different from v1)
     * @return a graph derived from g with a new edge v1v2; <code>null</code> if the edge already exists
     */
    public  Graph<E> add (E v1, E v2) {
        Graph<E> ag = clone();
        if (! ag.addEdge (v1, v2) )
            ag = null;
        
        return ag;
    }
    
    /**
     * computes the value of chromatic polynomial of <code>this</code> graph, i.e., the number of
     * possible lambda-colouring of the graph (in a recursive way, using the FRT) 
     * @param lambda a positive value 
     * @return the value of chromatic polynomial of a graph-map for a given lambda
     */
    public int chromPolynomial (int lambda) {
        if (lambda == 0)
            return 0;
       
        E v, u = null;
        if ((v = getNotComplete()) == null)
            return lambda_K(lambda, order() ); //the chromatic polynomial of K_{(g.size)}
        // g is not a clique: we find u not adjacent to v
        Set<? extends E> adjv = adjiacent(v);
        for (E x : vertexSet())
            if (! (x.equals(v) || adjv.contains(x) ) ) {
                u = x;
                break;
            }
                
        return Math.addExact(glue(v,u).chromPolynomial(lambda), add(v,u).chromPolynomial(lambda));
    }
    
    /**
     * indirectly checks whether <code>this</code> graph is (not) complete 
     * @return a reference to a non-completely connected vertex, if there is any;
     * <code>null</code> if the graph is complete (clique)
     */
    public  E getNotComplete () {
        int d = order() -1; // the (potential) max degree of g
        if (d > 0) //optimization: the empty and singleton graphs are complete
            for (Map.Entry<? extends E, ? extends Set<? extends E>> v : this.adjlist.entrySet()) 
                if (v.getValue().size() < d ) 
                    return v.getKey(); // the graph is not complete
        
        return null; // the graph is complete
    }
    
    /**
     * computes  the chromatic.polynomial value (for a given lambda) of a order-i complete graph
     * @param lambda a (assumed positive) value (number of colouring)
     * @param i the (assumed positive) ordered of a complete graph
     * @return the number of lambda-colouring of a order-i complete graph
     */
    private static int lambda_K(int lambda, int i) {
        if (lambda < i)
            return 0; // efficient
        
        int lambda_i = lambda;
        while (--i > 0)
            lambda_i = Math.multiplyExact(lambda_i, lambda - i); // ok
        
        return lambda_i;
    }
    
    /**
     * computes the connected components of <tt>this</tt> graph
     * @return the set of connected components of g's vertices
     */
    public Set<HashSet<E>> connectedComponents () {
    	if (this.connected == null) {
    		this.connected = new HashSet<>();
	        Map<E,Boolean> visited = setAllNovisited ();
	        vertexSet().stream().filter(v -> !visited.get(v)).map(v -> {
	            HashSet<E> comp = new HashSet<>();
	            DFSvisit(v, comp, visited);
	            return comp;
	        }).forEachOrdered( comp -> { this.connected.add(comp); });
    	}
    	
        return this.connected;
    }
    
    /*
    * set up a map in which all the graph's vertices are set "not yet visited" 
    */
    private Map<E,Boolean> setAllNovisited () {
       Map<E,Boolean> visited = new HashMap<>();
       vertexSet().forEach( v -> { visited.put( v, Boolean.FALSE); });
       
       return visited;
    }
    
    /*
    recursively (DFS) vist a vertex of <code>this</code> graph and its adjacencies
    */
    private void DFSvisit(E v, Set<E> comp, Map<E, Boolean> visited) {
        visited.put(v, Boolean.TRUE); // v is set "visited"
        comp.add(v); // v added to the component
        adjiacent(v).stream().filter(u -> !visited.get(u) ).forEachOrdered(u -> {
            DFSvisit(u, comp, visited); // u's adjacencies are "recursively" visited
        }); // for each u adjacent v
    }
    
    @Override
    public String toString () {
        return this.adjlist.toString();
    }
    
}
