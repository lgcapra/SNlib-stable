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
import wncolorfunction.ColorFunction;
import wncolorfunction.LinearComb;

/**
 * this class defines generic SN function-tuples, composed of linear combinations of elementary SN class-functions
 * (this implementation is not complete)
 * @author lorenzo capra
 */
public final class WNtuple extends AbstractTuple<ColorFunction>  implements ArcFunction {

    /**
     * base constructor: creates a <tt>WNtuple</tt> from a list of linear comb. of class-functions
     * @param f the tuple's filter
     * @param l the tuple's components
     * @param g the tuple's guard
     * @param check domain-check flag
     */
    public WNtuple(Guard f, List<? extends ColorFunction> l, Guard g, boolean check) {
        super(f, l, g, check);
    }
    
    /**
     * creates a <tt>WNtuple</tt> with a deafult filter from a list of linear comb. of class-functions
     * @param l the tuple's components
     * @param g the tuple's guard
     * @param check domain-check flag
     */
    public WNtuple(List<? extends ColorFunction> l, final Guard g, boolean check) {
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
    public WNtuple(Guard f, SortedMap<ColorClass, List <? extends ColorFunction>> m, Guard g) {
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
    public WNtuple(SortedMap<ColorClass, List <? extends ColorFunction>> m, Guard g) {
        super(m, g, false);
    }

    public WNtuple(final List<? extends ColorFunction> args, final Domain d) {
        super(args, d, true);
    }

    public WNtuple(Domain dom, ColorFunction ... comps) {
        this(Arrays.asList(comps), dom);
    }

    @Override
    @SuppressWarnings("unchecked")
    public WNtuple build(Guard filter, Guard guard) {
        return new WNtuple(filter, getHomSubTuples(), guard);
    }

    @Override
    public Integer cardLb() {
        Integer card = 0;
        for (ColorFunction o : getComponents()) {
                Integer lb = ((LinearComb) o).cardLb();
                if (lb == null)
                    return lb;
                card += lb;
            }
        return card;
    }

    /**
     * 
     * @return a set (that is a sum) of list of entries matching tuple's components containing variables of the same index
     * it builds on <tt>Util.cartesianProd</tt>
     * should be invoked on single-color tuples
     */
    private Set<Collection<Entry<Integer, Map<ElementaryFunction, Integer>>>> separate( ) {
    	List<Set<Entry<Integer, Map<ElementaryFunction, Integer>>>> l = new ArrayList<>();
        getComponents().stream().map(cx -> ((ColorFunction) cx).components().entrySet()).forEachOrdered(l::add);
    	
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
    public Set<? extends WNtuple> singleIndexComponentsTuples() {
    	Set<Collection<Entry<Integer, Map<ElementaryFunction, Integer>>>> expansion = separate();
    	if (expansion.size() == 1)
            return Collections.singleton(this); //optimization
        //System.out.println("expansion:\n"+expansion);
        HashSet<WNtuple> tset = new HashSet<>();
        ColorClass cc = getSort(); // the tuple is assumed-single color
    	for (Collection<Entry<Integer, Map<ElementaryFunction, Integer>>> lx : expansion) {
            List<ColorFunction> lc = new ArrayList<>();
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
    public Map<Set<Integer>, LinkedHashMap<Integer,ColorFunction> > independentComponents (Set<? extends Set<Integer>> connected) {
    	List<? extends ColorFunction> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashMap<Integer, ColorFunction>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashMap<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			ColorFunction l = components.get(i);
			Set<Integer> indexSet = l.indexSet();
			LinkedHashMap<Integer, ColorFunction> m = imap.get( indexSet ); //optimization
			if (m == null)
				for (Entry<Set<Integer>, LinkedHashMap<Integer, ColorFunction>> entry :  imap.entrySet() )
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
    public Map<Set<Integer>, LinkedHashMap<Integer,ColorFunction> > independentComponents () {
    	Guard g = guard();
    	return independentComponents( !g.isTrivial()  ? ((And) g).igraph().get( getSort()).connectedIndices(): Collections.emptySet());
    }
    
    /*
     * 2nd version of independentComponents, which only returns the positions of independent subtuple elements
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 (Set<? extends Set<Integer>> connected) {
    	List<? extends ColorFunction> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashSet<Integer>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashSet<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			ColorFunction l = components.get(i);
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
    
    /**
     * if <code>this</code> tuple contains non trivial linear combinations, expand the tuple into a corresponding TupleBag
     * @return the bag corresponding to the expansion of inner linear combinations
     */
    public ArcFunction expand() {
        // to do
        return this;
    }
    
}
