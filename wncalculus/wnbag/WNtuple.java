package wnbag;

import java.util.*;
import java.util.Map.Entry;
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
    
    //cache fields
    private Boolean simple = null;
    private Integer card = null;

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
     * @param m a (sorted) map of colors to corresponding lists of functions
     * @param g the tuple's guard
     */
    public WNtuple(Guard f, SortedMap<ColorClass, List <? extends ColorFunction>> m, Guard g) {
        super(f, m, g, false);
    }
    
    /**
     * creates an ordinary <tt>WNtuple</tt> from a map of colors to class-functions;
     * no check is done
     * @param m a (sorted) map of colors to corresponding lists of functions
     * @param g the tuple's guard
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

    /**
     * 
     * @return <code>true</code> if and only if this tuple is built of <code>LinearComb</code>s
     */
    public boolean simple() {
        if (simple == null)
          simple = Util.checkAll(getComponents(), LinearComb.class::isInstance);

        return simple;
    }

    @Override
    public Integer cardLb() {
        if (card == null) {
            card = 1;
            for (ColorFunction o : getComponents()) {
                Integer lb = o.cardLb();
                if (lb == null)
                    return lb;
                card *= lb;
            }
        }

        return card;
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
        ColorClass cc = getSort();
        if (cc == null) {
            return this;
        }

        List<? extends ColorFunction> comps = getComponents();
        List<Set<Map.Entry<ColorFunction, Integer>>> choices = new ArrayList<>();
        boolean expandable = false;

        for (ColorFunction cf : comps) {
            Set<Map.Entry<ColorFunction, Integer>> c = expandChoices(cf);
            if (c.size() > 1) {
                expandable = true;
            }
            choices.add(c);
        }

        if (!expandable) {
            return this;
        }

        HashMap<WNtuple, Integer> result = new HashMap<>();

        for (Collection<Map.Entry<ColorFunction, Integer>> prod : Util.cartesianProd(choices)) {
            List<ColorFunction> newComps = new ArrayList<>(comps.size());
            int mult = 1;

            for (Map.Entry<ColorFunction, Integer> choice : prod) {
                newComps.add(choice.getKey());
                mult *= choice.getValue();
            }

            WNtuple t = new WNtuple(Util.singleSortedMap(cc, newComps), guard());
            result.merge(t, mult, Integer::sum);
        }

        return new TupleBag(result);
    }

    private Set<Map.Entry<ColorFunction, Integer>> expandChoices(ColorFunction cf) {
        if (cf instanceof LinearComb lc && lc.size() > 1) {
            Set<Map.Entry<ColorFunction, Integer>> s = new HashSet<>();
            lc.asMap().forEach((f, k) -> s.add(new AbstractMap.SimpleEntry<>(new LinearComb(f), k)));
            return s;
        }

        return Collections.singleton(new AbstractMap.SimpleEntry<>(cf, 1));
    }

    @Override
    public ArcFunction specSimplify() {
        //System.out.println("specSimplify("+this.toStringDetailed()+")"); //debug
        ArcFunction res = super.specSimplify().cast();
        if (! (res instanceof WNtuple tres))
            return res;
        // complete!
        return tres;
    }

    @Override
    public WNtuple build(final Guard filter, final SortedMap<ColorClass, List<? extends ColorFunction>> map,
            final Guard guard) {
        return new WNtuple(filter,map, guard);
    }

    /**
     * base composition between <code>this</code> tuple (assumed single-color, simple, and without constants inside)
     * and the <code>other</code> tuple (assumed entirely projected by <code>this</code>) 
     * @param other
     * @return
     */
    ArcFunction baseCompose(WNtuple other) {
        //dummy implementation
        return new ArcFunComp.BaseComp(this, other);
    }


}
