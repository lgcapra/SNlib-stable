package wncalculus.tuple;

import java.util.*;
import wncalculus.expr.IllegalDomain;
import wncalculus.logexpr.OrOp;
import wncalculus.util.Util;


/**
 * this class defines the sum of WN function-tuples  mapping on sets
 * @author Lorenzo Capra
 */
public final class TupleSum extends TupleNaryOp implements OrOp<FunctionTuple> {
    
    private boolean disjoined; // default is false
    
    private TupleSum (Set<? extends FunctionTuple> tuples, boolean check, boolean disjoined) {
        super(tuples, check);
        this.disjoined = disjoined;
    }
  
    /** main factory method: builds a sum of tuples from a given collection of operands; if the collection
     * (possibly translated to a Set) is a singleton then the contained element is returned 
     * @param arglist the collection of operands
     * @param check the arity control-flag
     * @param disjoined pair-wise disjointness flag
     * @return either a TupleSum corresponding to the passed collection, or the
     * only contained element, if the collection is size-one
     * @throws IllegalDomain if the operands have different arity
     * @throws NoSuchElementException if the collection is empty
     */
    public static FunctionTuple factory(Collection<? extends FunctionTuple> arglist, boolean check, boolean disjoined) {
        Set<? extends FunctionTuple> aSet = Util.asSet(arglist);
        return aSet.size() < 2 ?  aSet.iterator().next() : new TupleSum (aSet, check, disjoined);
    }
    
    /**
     * builds a sum of tuples from a given collection of operands, without any check
     * @param arglist  the collection of operands
     * @param disjoined pair-wise disjointness flag
     * @return either a TupleSum corresponding to the passed collection, or the
     * only contained element, if the collection is size-one
     */
    public static FunctionTuple factory(Collection<? extends FunctionTuple> arglist, boolean disjoined)  {
        return factory(arglist,true,disjoined);
    }
    
    /**
     * builds a sum of tuples from a given list (varargs) of operands
     * @param check  the arity control-flag
     * @param disjoined pair-wise disjointness flag
     * @param args the list of tuples
     * @return either a TupleSum corresponding to the passed list, or the
     * only contained element, if the list is size-one
     */
    public static FunctionTuple factory (boolean check, boolean disjoined, FunctionTuple ... args)  {
        return factory(Arrays.asList(args), check , disjoined); 
    }
    
    /**
     * builds a sum of tuples from a given list (varargs) of operands, without
     * any check
     * @param disjoined pair-wise disjointness flag
     * @param args the list of tuples
     * @return either a TupleSum corresponding to the passed list, or the
     * only contained element, if the list is size-one
     */
    public static FunctionTuple factory(boolean disjoined, FunctionTuple ... args)  {
        return factory(true, disjoined, args);
    }
    
    /**
    * build a sum of tuples preserving disjointness of <code>this</code>
    */
    @Override
    public FunctionTuple buildOp(Collection <? extends FunctionTuple> args)  {
        return factory(args, this.disjoined);
    }

    @Override
    public boolean differentFromZero() {
        return getArgs().stream().anyMatch(f -> (f.differentFromZero()));
    }
    
    /**
     * performs the difference between <tt>this</tt> sum and a given function
     * @param other the function-tuple to be subtracted from <tt>this</tt> sum
     * @return the difference between <tt>this</tt> sum and a given function
     * @throws IllegalDomain if <tt>this</tt> and <tt>other</tt> have different arity
     */
    public FunctionTuple diff (FunctionTuple other)  {
        //System.out.println("ecco\n"+this + " - " + '('+other+')');
        Collection<FunctionTuple> diff_res = new ArrayList<>();
        getArgs().forEach( t -> { diff_res.add( TupleDiff.factory(t , other) ); });
        
        return TupleSum.factory(diff_res, this.disjoined);
    }
    
    /**
     * 
     * @return <code>true</code> if and only if is a sum of tuples 
     */
    public boolean isElementary() {
        return Util.checkAll(getArgs(), Tuple.class::isInstance);
    }
    /** 
     tries to merge this sum (assumed to be a sum of tuples) in order to obtain a more
     compact representation;
     WARNING: it should never be invoked from inside the f.p. simplification algorithm!
     @return a folded equivalent term; <tt>this</tt>, if no merge has been done
     @throws ClassCastException if the sum is not elementary
    */
    @Override
    public FunctionTuple merge () {
        Collection<? extends FunctionTuple> targs = getArgs();
        List<Tuple > pt = Util.copyAndCast(targs, Tuple.class );  
        FunctionTuple.fold(pt);
        return pt.size() < targs.size() ? TupleSum.factory( pt, this.disjoined) : this; //optimization
    }

    
    @Override
    public String symb() {
        return " + ";
    }
    
    @Override
    public boolean disjoined() {
        return this.disjoined;
    }
    
     @Override
    public void setDisjoint() {
        this.disjoined = true;
    }
    
     @Override
    public void printCard() {
        System.out.println("cardinality of sum's elements (BEGIN)");
        getArgs().forEach(s -> { s.printCard(); });
        System.out.println("cardinality of sum's elements (END)");
    }
    
    @Override
    public final Integer cardLb() {
        if (! this.disjoined ) {
            System.err.println("non disjoined: "+this);
            return null;
        }
        
        int card = 0;
        for (FunctionTuple x : getArgs() ) {
            Integer n = x.cardLb();
            if (n == null)
                return null;
            
            card += n;
        }
        
        return card;
    }
    
    @Override
    public  String toStringAbstract () {
        StringBuilder s = new StringBuilder();
        String op = symb();
        getArgs().forEach(t -> { s.append(t.toStringAbstract()).append(op); });
        int l = s.length();
        s.delete(l - op.length(), l);
        
        return s.toString();
    }
    
}
