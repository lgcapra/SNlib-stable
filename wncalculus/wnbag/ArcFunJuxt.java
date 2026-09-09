package wnbag;

import java.util.*;
import java.util.Map.Entry;

import color.Sort;
import expr.*;
import color.ColorClass;
import guard.Guard;
import util.Util;
import wncolorfunction.ColorFunction;

/**
 * This class defines a kind of tuple juxtaposition operator.
 * Arguments must all have the same domain
 * @author Lorenzo Capra
 */

public final class ArcFunJuxt  implements ArcFunction, N_aryOp<ArcFunction >  {
    
    private final Map<ColorClass, ArcFunction> ccomponents;
    private final Guard guard;
    private final Domain  codom;
    private boolean simplified;
    
    /*
    base constructor
    */
    private ArcFunJuxt(final Map<ColorClass, ? extends ArcFunction> ctuples, final Guard guard, final boolean check) {
        final Collection<? extends ArcFunction> afunctions = ctuples.values();
        if (check) 
            Expressions.checkDomain(afunctions);
        
        this.ccomponents = Collections.unmodifiableMap(ctuples);
        this.codom  =  buildCodomain(afunctions );
        this.guard = guard;
    }

    /**
     * 
     * @param ctuples
     * @param guard
     * @param check
     * @return
     */
    public static ArcFunction factory (final Map<ColorClass, ? extends ArcFunction> ctuples, final Guard guard, final boolean check) {
        if (ctuples.size() == 1) {
            return ctuples.values().iterator().next();
        }
        return new ArcFunJuxt(ctuples, guard, check);
    }

    public static ArcFunction factory (final Map<ColorClass, ? extends ArcFunction> ctuples, final Guard guard) {
        return factory(ctuples, guard, false);
    }
 
    
    @Override
    public ArcFunction buildOp(final Collection<? extends ArcFunction > args) {
       TreeMap<ColorClass, ArcFunction> map = new TreeMap<>();
       for (ArcFunction f : args)
         map.put((ColorClass)f.getDomain().asMap().keySet().iterator().next(), f);

       return ArcFunJuxt.factory(map, this.guard);
    }
    
    /** 
     *infers the juxtaposition co-domain from its argument list
     */
    private Domain buildCodomain(final Collection<? extends ArcFunction> tuples) {
        final HashMap<Sort,Integer> d = new HashMap<>();
        tuples.forEach( ft -> { 
            final Map<? extends Sort, Integer> cd = ft.getCodomain().asMap();
            if (cd.size() == 1) {
                final Map.Entry<? extends Sort, Integer> e = cd.entrySet().iterator().next();
                if (d.putIfAbsent(e.getKey(), e.getValue()) != null) 
                    throw new IllegalDomain(tuples+": many tuples of the same colour are present!");
            }
            else
                 throw new IllegalDomain(tuples+": non mono-coloured tuples are present!");
        });
        
        return new Domain(d);
    }

    @Override
    public Domain getCodomain() {
        return this.codom;
    }
    

     @Override
    /** the simplification algorithm treats two cases; a juxtaposition formed only by 
        Tuples; a juxtaposition containing a n-ary operator*/
    public ArcFunction specSimplify() { 
       final var components = this.ccomponents.values(); 
       if ( Util.checkAny(components, comp -> comp instanceof TupleBag cbag && cbag.size() == 0) )
          return build(); //optimization
       
       if ( Util.checkAll(components, comp -> comp instanceof WNtuple) ) 
        { // there are only tuples ..
           final SortedMap<ColorClass, List <? extends ColorFunction>> map = new TreeMap<>();
           for (Entry<ColorClass, ArcFunction> ccomp : this.ccomponents.entrySet()) {
              WNtuple tc = (WNtuple) ccomp.getValue();
              map.put(ccomp.getKey(), Collections.unmodifiableList(tc.getComponents()));
           }
           
           return new WNtuple(map, guard);
       }
       
       //completare
       /*final int t_pos;
       if ((t_pos = Util.getType(components, TupleBag.class)) >= 0) {
            final TupleSum t_op = (TupleSum) this.tuples.get(t_pos);
            final Set<FunctionTuple> t_jxtp_list = new HashSet<>(); 
            for (FunctionTuple x : t_op.getArgs() ) {
                final List<FunctionTuple> simp_list = new ArrayList<>(this.tuples);
                simp_list.set(t_pos, x );
                t_jxtp_list.add(TupleJuxtaposition.factory(simp_list,false));
            }
            return TupleSum.factory(t_jxtp_list, t_op.disjoined()); // juxtaposition preserves disjoinedness
       }*/
       //System.out.println("res: " + this);
       return this ;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public String symb() {
        return "::";
    }

    @Override
    public Collection</*? extends*/ ArcFunction> getArgs() {
        return this.ccomponents.values();
    }
    
     @Override
    public final boolean equals(Object o) {
        ArcFunJuxt other;
        return o != null && this.getClass() == o.getClass() && Objects.equals(this.ccomponents, (other = (ArcFunJuxt) o).ccomponents)
           && Objects.equals(this.guard, other.guard);
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 47 * hash + this.ccomponents.hashCode();
        return hash;
    }
    
    @Override
    public String toString () {
        String res = symb();
        res = this.ccomponents.values().stream().map(x -> x.toString() + ';').reduce(res, String::concat);
    
        return res.substring(0,res.length()-1)+ symb();
    }

    @Override
    public ArcFunction getIde() {
        return null;
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
    public Class<ArcFunction> type() {
        return ArcFunction.class;
    }

    @Override
    public Integer cardLb() {
        return null;
    }
    
    /**
     * defined to avoid compiler issues
     */
    public ArcFunction clone(Map<Sort, Sort> split_map) {
        return ArcFunction.super.clone(split_map).cast();
    }
}

