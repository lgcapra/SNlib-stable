package wnbag;

import java.util.*;
import java.util.Map.Entry;
import classfunction.ElementaryFunction;
import color.ColorClass;
import expr.*;
import guard.And;
import guard.Guard;
import tuple.AbstractTuple;
import util.Util;

/**
 * this class defines generic SN function-tuples, possibly composed of linear combinations
 * of elementary SN class-functions
 * (this implementation is not complete)
 * @author lorenzo capra
 */
public final class WNtuple extends AbstractTuple<LinearComb>  {

    /**
     * base constructor: creates a <tt>WNtuple</tt> from a list of linear comb. of class-functions
     * @param f the tuple's filter
     * @param l the tuple's components
     * @param g the tuple's guard
     * @param check domain-check flag
     */
    public WNtuple(Guard f, List<? extends LinearComb> l, Guard g, boolean check) {
        super(f, l, g, check);
    }
    
    /**
     * creates a <tt>WNtuple</tt> with a deafult filter from a list of linear comb. of class-functions
     * @param l the tuple's components
     * @param g the tuple's guard
     * @param check domain-check flag
     */
    public WNtuple(List<? extends LinearComb> l, final Guard g, boolean check) {
        super(l, g, check);
    }
    
    /**
     * efficiently creates a <tt>WNtuple</tt> from a map of colors to class-functions;
     * no check is done
     * @param f the tuple's filter
     * @param codom the tuple's codomain
     * @param m a (sorted) map of colors to corresponding lists of functions
     * @param g the tuple's guard
     * @param dom  the tuple's domain
     */
    public WNtuple(Guard f, SortedMap<ColorClass, List <? extends LinearComb>> m, Guard g) {
        super(f, m, g, false);
    }
    
    /**
     * creates an ordinary <tt>WNtuple</tt> from a map of colors to class-functions;
     * no check is done
     * @param f the tuple's filter
     * @param codom the tuple's codomain
     * @param m a (sorted) map of colors to corresponding lists of functions
     * @param g the tuple's guard
     * @param dom  the tuple's domain
     */
    public WNtuple(SortedMap<ColorClass, List <? extends LinearComb>> m, Guard g) {
        super(m, g, false);
    }

    @Override
    public WNtuple build(Guard filter, Guard guard) {
        return new WNtuple(filter, getHomSubTuples(), guard);
    }

    
    @Override
    public ParametricExpr buildTransp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<WNtuple> type() {
        return WNtuple.class;
   }

    /*@Override
    public ParametricExpr clone(Domain newdom, Domain newcd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
    
    @Override
    public final WNtuple clone (final  Map<Sort, Sort> split_map) {
        return new WNtuple ((Guard)filter().clone(split_map), super.cloneComps(split_map), (Guard)guard().clone(split_map));
    }
    

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    /**
     * 
     * @return the tuple's expansion as a set of list of entries matching tuple's
     * components containing variables of the same index
     * it builds on <tt>Util.cartesianProd</tt>
     * should be invoked on single-color tuples
     */
    private Set<Collection<Entry<Integer, Map<ElementaryFunction, Integer>>>> expand( ) {
    	List<Set<Entry<Integer, Map<ElementaryFunction, Integer>>>> l = new ArrayList<>();
    	getComponents().stream().map(cx -> cx.components().entrySet()).forEachOrdered(l::add);
    	
    	return Util.cartesianProd(l);
    }
    
    /**
     * @return a tuple's expansion into a set (i.e., sum) of tuples whose components contain constants
     * or variables with the same index
     * should be invoked on single-color tuples, otherwise, it raises an exception
     * it builds on <tt>expand()</tt>
     * performs a kind of Cartesian product on <tt>this</tt> tuple, resulting in the set of
     * tuples with equal-index components
     * @return the tuple's expansion in a set of tuples whose components contain variables of the same index
     * should be invoked on single-color tuples, otherwise, it raises an exception
     * it build on <tt>expand()</tt>
     */
    // a cosa serve?
    public Set<? extends WNtuple> singleIndexComponentsTuples() {
    	HashSet<WNtuple> tset = new HashSet<>();
    	Set<Collection<Entry<Integer, Map<ElementaryFunction, Integer>>>> expansion = expand();
    	if (expansion.size() == 1)
            return Collections.singleton(this); //optimization
        
        //System.out.println("expansion:\n"+expansion);
        ColorClass cc = getSort(); // the tuple is assumed-single color
    	for (Collection<Entry<Integer, Map<ElementaryFunction, Integer>>> lx : expansion) {
            List<LinearComb> lc = new ArrayList<>();
            lx.forEach(x -> { lc.add(new LinearComb(x.getValue())); });
            tset.add(new WNtuple ( Util.singleSortedMap(cc, lc), guard() ));
    	}
    	//System.out.println("tset:\n"+tset); //debug
    	return tset;		
    }
    
    /**
     * separates <tt>this</tt> tuple into its independent sub-tuples, given a partition of variable indices
     * modeling the independent parts of the associated guard (if any)
     * should be invoked on (single-color) tuples whose components hold at most one (projection) index 
     * (even if it works more generally)
     * @param connected the (possibly empty) pre-calculated set of connected components of tuple's guard
     * @return a map whose keys are sets of independent variable indices and whose values are corresponding
     * sub-tuples of <tt>this</tt> tuple, computed according the method's parameter;
     * each independent sub-tuple is expressed in turn as a map between tuple's positions (starting from 0) and components
     */
    public Map<Set<Integer>, LinkedHashMap<Integer,LinearComb> > independentComponents (Set<? extends Set<Integer>> connected) {
    	List<? extends LinearComb> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashMap<Integer, LinearComb>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashMap<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			LinearComb l = components.get(i);
			Set<Integer> indexSet = l.indexSet();
			LinkedHashMap<Integer, LinearComb> m = imap.get( indexSet ); //optimization
			if (m == null)
				for (Entry<Set<Integer>, LinkedHashMap<Integer, LinearComb>> entry :  imap.entrySet() ) 
					if (entry.getKey().containsAll( indexSet ) ) { // indexset should be a singleton or empty
                                            m = entry.getValue();
                                            break;
		    		}
			if ( m ==  null ) 
				imap.put(l.indexSet(), m = new LinkedHashMap<>());
			m.put(i, l);
		}
			
    	return imap;
    }
    
    /**
     * overloaded version of <tt>independentComponents</tt>, which assumes <tt>this</tt> tuple single-color,
     * and with and and-type guard
     * @return
     */
    public Map<Set<Integer>, LinkedHashMap<Integer,LinearComb> > independentComponents () {
    	Guard g = guard();
    	return independentComponents( !g.isTrivial()  ? ((And) g).igraph().get( getSort()).connectedIndices(): Collections.emptySet());
    }
    
    /*
     * 2nd version of independentComponents, which only returns the positions of independent subtuple elements
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 (Set<? extends Set<Integer>> connected) {
    	List<? extends LinearComb> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashSet<Integer>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashSet<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			LinearComb l = components.get(i);
			Set<Integer> indexSet = l.indexSet();
			LinkedHashSet<Integer> m = imap.get( indexSet ); //optimization
			if (m == null)
				for (Entry<Set<Integer>, LinkedHashSet<Integer>> entry :  imap.entrySet() ) 
					if (entry.getKey().containsAll( indexSet ) ) { // indexset should be a singleton or empty
		    			 m = entry.getValue();
		    			 break;
		    		}
			if ( m ==  null ) 
				imap.put(l.indexSet(), m = new LinkedHashSet<>());
			
			m.add(i);
		}
			
    	return imap;
    }
    
    /**
     * overloaded version of <tt>independentComponentsV2</tt>, which assumes <tt>this</tt> tuple single-color,
     * and with and and-type guard
     * @return
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 () {
    	Guard g = guard();
    	return independentComponentsV2(!g.isTrivial() ? ((And) g).igraph().get( getSort()).connectedIndices(): Collections.emptySet());
    }
    
}
