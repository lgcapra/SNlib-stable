package wncalculus.wnbag;

import java.util.*;
import wncalculus.bagexpr.LogicalBag;
import wncalculus.classfunction.ClassFunction;
import wncalculus.classfunction.ElementaryFunction;
import wncalculus.classfunction.Projection;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.util.Util;
import wncalculus.expr.IllegalDomain;
import wncalculus.expr.Sort;
import wncalculus.guard.Equality;

/**
 * this class defines linear combination of basic class-funtions
 * @author lorenzo capra
 */
public final class LinearComb extends LogicalBag<ElementaryFunction> implements ClassFunction {
    
	HashMap<Integer, Map<ElementaryFunction, Integer> > components; //cache: the components, separated by index (0 is for constants)
	
    /**
     * base constructor: creates a linear-combination (i.e., a bag) of (elementary) class-functions
     * @param m a map corresponding to the bag
     * @param disjoint disjointness flag (if set, terms are assumed pair-wise disjoint)
     */
    public LinearComb(Map<ElementaryFunction, Integer> m, boolean disjoint) {
        super(m, disjoint);
    }
     /**
     * base constructor: creates a linear-combination (i.e., a bag) of (elementary) class-functions,
     * which are not assumed pair-wise disjoint
     * @param m a map corresponding to the bag
     */
    public LinearComb(Map<ElementaryFunction, Integer> m) {
        this(m, false);
    }
    
    /**
     * creates a linear-combination (i.e., a bag) of (elementary) class-functions,
     * from a collection
     * @param c a collection of class-functions
     */
    public LinearComb(Collection<? extends ElementaryFunction> c) {
        this(Util.asMap(c));
    }
    
    /**
     * creates a linear-combination (i.e., a bag) of (elementary) class-functions,
     * from a varargs list of functions
     * @param f a list of functions
     */
    public LinearComb(ElementaryFunction ... f) {
        this(Arrays.asList(f));
    }
    
    /**
     * creates an elementary (i.e., single-element) linear-combination
     * @param f the only function
     * @param k its multiplicity
     */
    public LinearComb(ElementaryFunction f , int k) {
        super (f, k);
    }
    
    /**
     * creates an empty linear combination
     * @param cc the bag's colour
     */
    public LinearComb(ColorClass cc) {
        super( new Domain(Util.singleMap(cc, 1)));
    }
    
    
    @Override
    public LinearComb build(Domain dom, Domain codom) {
        Sort c;
        if (dom.size() == 1 && dom.mult(c = dom.support().iterator().next() ) == 1 && dom.equals(codom))
            return new LinearComb((ColorClass) c);
        
        throw new IllegalDomain("the co-domain of a linear combination must be a singleton");
    }
    
    
    @Override
    public ColorClass getSort() {
        return (ColorClass) getDomain().support().iterator().next();
    }

    @Override
    public LinearComb setDefaultIndex() {
        Set<Integer> s = indexSet();
        if (s.isEmpty() || s.size() == 1 && s.iterator().next() == 1)
            return this;
        
        Map<ElementaryFunction, Integer> m = new HashMap<>();
        asMap().entrySet().forEach( x -> { m.put(x.getKey().setDefaultIndex(), x.getValue()); });
        
        return new LinearComb(m);
    }
    
    
    @Override
    public int splitDelim() {
        return ClassFunction.splitDelim(support(), getSort());
    }
    
    
    /**
     * extracts the components of <tt>this</tt> l.c. composed of terms with the same index
     * @return a map between indices and corresponding (sub-)bags ; possibly, constant functions
     * are bring together to functions of a given index (that is, if the key 0
     * is present at the end, then it is the only key present in the returned map, meaning
     * that the linear combination is made up of constants)
     */
    public HashMap<Integer, Map<ElementaryFunction, Integer>> components () {
    	if (this.components == null) {
    		this.components = new HashMap<>();
                support().forEach(f -> {
	            Integer i = f instanceof Projection ? ((Projection)f).getIndex() : 0;
	            Map<ElementaryFunction, Integer> b = this.components.get(i);
	            if (b == null) 
	            	this.components.put(i, b = new HashMap<>());
	            b.put(f, mult(f)); 
	        });
                Map<ElementaryFunction, Integer> b;
                if (this.components.size() > 1 && (b = this.components.get(0)) != null ) {
                    this.components.remove(0);
                    this.components.get(this.components.keySet().iterator().next()).putAll(b);
                }
    	}
        
    	return this.components;
    }
    
    /**
     * @return the index set of <code>this</code> function
     */
    @Override
    public Set<Integer> indexSet() {
         Set<Integer> s = components().keySet();
    	 
    	 return s.contains(0) ? Collections.emptySet() : s;
    }

    @Override
    public LinearComb replace(Equality eq) {
        HashMap<ElementaryFunction, Integer> copy = new HashMap<>();
        asMap().entrySet().forEach(x -> {
            ElementaryFunction k = x.getKey().replace(eq);
            Integer m = copy.get(k);
            copy.put(k, x.getValue() + (m == null ? 0 : m));
        });
        
        return new LinearComb(copy);
    }
    
    @Override
    public LinearComb copy(ColorClass newcc) {
        HashMap<ElementaryFunction, Integer>  newmap = new HashMap<>();
        asMap().entrySet().forEach(e -> { newmap.put(e.getKey().copy(newcc), e.getValue()); });
        
        return new LinearComb(newmap);
    }
    

    @Override
    public LogicalBag<ElementaryFunction> build(Map<ElementaryFunction, Integer> smap, boolean disj) {
        return new LinearComb(smap, disj);
    }
    
    @Override
    public LinearComb clone (final Domain newdom) {
        return (LinearComb) ClassFunction.super.clone(newdom);
    }
   
}
