package tuple;

import java.util.*;
import expr.*;
import color.ColorClass;
import guard.And;
import guard.Guard;
import util.Util;
import classfunction.SetFunction;

/**
 * This class defines a kind of tuple juxtaposition operator.
 * Tuple-arguments must all have the same domain, single-coloured and pairwise-disjoint codomains.
 * @author Lorenzo Capra
 */
public final class TupleJuxtaposition  implements FunctionTuple, N_aryOp<FunctionTuple >  {
    
    private final List<FunctionTuple>  tuples;
    private final Domain               codom;
    private boolean simplified;
    
    /*
    base constructor
    */
    private TupleJuxtaposition (List<? extends FunctionTuple> tuples, boolean check)  {
        if (check) 
            Expressions.checkDomain(tuples);
        this.codom  =  buildCodomain( tuples );
        this.tuples =  Collections.unmodifiableList(tuples);
    }
    
    /** 
     * main factory method; creates juxtaposition form a (non-empty) list of Tuples
     * @param tuples a list of tuples
     * @param check check domain flag
     * @return the result of tuples juxtaposition
     * @throws IllegalDomain  if the tuple color domains are different;  many tuples of the same colour
     * or non mono-coloured tuples are present in the passed list
     */
    public static FunctionTuple factory (List<? extends FunctionTuple> tuples, boolean check)  {
        return tuples.size() == 1 ? tuples.get(0) : new TupleJuxtaposition (tuples, check); 
    } 
    
    /**
     * build a tuple juxtaposition without any check on tuples domains 
     * @param tuples a list of tuples to juxtapose
     * @return the tuples' juxtaposition
     * @throws IllegalDomain  if the tuples have different domains
     */
    public static FunctionTuple factory (List<? extends FunctionTuple> tuples)  {
        return factory(tuples, false);
    }
    
    /**
     * build a tuple juxtaposition from a varargs list of tuples
     * @param check_colors domain-check flag
     * @param tuples a list of tuples to juxtapose
     * @return  the tuples' juxtaposition
     */
    public static FunctionTuple factory (boolean check_colors, FunctionTuple ... tuples) {
        return factory(Arrays.asList(tuples),  check_colors);
    }
    
    /**
     * build a tuple juxtaposition from a varargs list of tuples
     * without any check
     * @param tuples a list of tuples to juxtapose
     * @return  the tuples' juxtaposition
     */
    public static FunctionTuple factory (FunctionTuple ... tuples) {
        return factory(false, tuples);
    }
    
    @Override
    public FunctionTuple buildOp(Collection<? extends FunctionTuple > args) {
        return factory((List<? extends FunctionTuple>) args, false);
    }
    
    /** 
     *infers the juxtaposition co-domain from its argument list
     */
    private Domain buildCodomain( List<? extends FunctionTuple> tuples) {
        HashMap<Sort,Integer> d = new HashMap<>();
        tuples.forEach( ft -> { 
            Map<? extends Sort, Integer> cd = ft.getCodomain().asMap();
            if (cd.size() == 1) {
                Map.Entry<? extends Sort, Integer> e = cd.entrySet().iterator().next();
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
    public Map<Sort, Integer> splitDelimiters() {
        return ColorClass.mergeSplitDelimiters(getArgs());
    }

    @Override
    public boolean differentFromZero() {
        return this.tuples.stream().allMatch(t -> t.differentFromZero());
    }

    
    /**
     * overrides the super-type method because the operands codomains are restrictions
     * of <tt>this</tt> term's codomain
     * @param newdom the new domain
     * @param newcd  the new codomain
     * @return a clone of <tt>this</tt> with the specified co-domains
     */
    /*@Override
    public TupleJuxtaposition clone (final Domain newdom, final Domain newcd) {
        List<FunctionTuple> cloned_tuples = new ArrayList<>();
        this.tuples.forEach((var tuple) -> {
            Domain old_cd = tuple.getCodomain();
            Map.Entry<? extends Sort, Integer> e = old_cd.asMap().entrySet().iterator().next();
            var cc = e.getKey(); //the sub-tuple is mono-coloured
            cloned_tuples.add((FunctionTuple)tuple.clone(newdom, newcd.mult(cc) != 0 ? old_cd : new Domain(newcd.getSort(cc.name()), e.getValue()) ));
        });
        
        return new TupleJuxtaposition(cloned_tuples, true);
    }*/
    
    @Override
    public TupleJuxtaposition clone (final  Map<Sort, Sort> split_map) {
        List<FunctionTuple> cloned_tuples = new ArrayList<>();
        this.tuples.forEach((var tuple) -> { cloned_tuples.add(tuple.clone(split_map ). cast() ); });
        
        return new TupleJuxtaposition(cloned_tuples, true);
        
    }

    @Override
    /** the simplification algorithm treats two cases; a juxtaposition formed only by 
        Tuples; a juxtaposition containing a n-ary operator*/
    public FunctionTuple specSimplify() { 
       int t_pos;
       FunctionTuple res = this;
       Domain cd = getCodomain(), d = getDomain();
       
       if ( Util.find(this.tuples, EmptyTuple.class ) != null)
          res = EmptyTuple.getInstance(cd, d); //optimization
       else if (Util.checkAll(this.tuples , (e -> e instanceof Tuple || e instanceof AllTuple) ) ) { // there are only (constant) Tuples (non extended) ..
           List<SetFunction> t_list = new ArrayList<>();
           Set<Guard> g = new HashSet<>(), f = new HashSet<>();
           for (FunctionTuple x : this.tuples ) {
               Tuple t = x instanceof AllTuple ? ((AllTuple)x).asTuple() : (Tuple) x;
               t_list.addAll( t.getComponents());
               f.add(t.filter().clone(cd));  // the domains of the inner filters are different from the terms's codomain...
               g.add(t.guard());
           }
           res = new Tuple(And.factory(f), t_list, And.buildAndFormWithD(g, d));
       }
       else if ((t_pos = Util.indexOf(this.tuples, TupleSum.class)) >= 0) {
            TupleSum t_op = (TupleSum) this.tuples.get(t_pos);
            Set<FunctionTuple> t_jxtp_list = new HashSet<>(); 
            for (FunctionTuple x : t_op.getArgs() ) {
                List<FunctionTuple> simp_list = new ArrayList<>(this.tuples);
                simp_list.set(t_pos, x );
                t_jxtp_list.add(TupleJuxtaposition.factory(simp_list,false));
            }
            res = TupleSum.factory(t_jxtp_list, t_op.disjoined()); // juxtaposition preserves disjoinedness
       }
       //System.out.println("res: " + res);
       return res ;
    }

    @Override
    public boolean isConstant() {
        return this.tuples.stream().noneMatch(t -> !t.isConstant());
    }

    @Override
    public String symb() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List</*? extends*/ FunctionTuple> getArgs() {
        return this.tuples;
    }
    
     @Override
    public final boolean equals(Object o) {
        return N_aryOp.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.tuples.hashCode();
        return hash;
    }
    
    @Override
    public String toString () {
        String res = "<";
        res = this.tuples.stream().map(x -> x.toString() + ';').reduce(res, String::concat);
        
        return res.substring(0,res.length()-1)+'>';
    }

    @Override
    public FunctionTuple getIde() {
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
    public Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }
    
}
