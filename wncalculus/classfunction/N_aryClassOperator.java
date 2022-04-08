package wncalculus.classfunction;

import java.util.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.*;
import wncalculus.guard.Equality;
import wncalculus.logexpr.AndOp;
import wncalculus.util.Util;

/**
 * this abstract class defines the super-type for n-ary (n greater than one) class-function associative operators
 * @author lorenzo capra
 */
public abstract class N_aryClassOperator  extends SetFunction implements N_aryOp<SetFunction> {
    
    private final Set<SetFunction> args;//the (ordered) operand's list
    // the following fields are for the sake of efficiency
    private Map<Boolean, Set<ProjectionBased>  > proj_map; //false -> non congruent; true -> congruent 
    private Set<Subcl> subcls;
    
    
    /**
    * basic constructor
    * @param functions the list of operands
    * @param check domain-check flag
    * @throws IllegalDomain if the functions' color-classes are different
    */
    protected N_aryClassOperator(Set<? extends SetFunction> functions, boolean check) {
        if (check)
            Expressions.checkDomain(functions);
        this.args = Collections.unmodifiableSet( functions );
        if (this.args.size() < 2) {
           System.err.println("built n-ary class op with less than two operands");
        }
    }
    
    /**
     * @return the fixedSize of the operation
     */
    public final int size () {
        return this.args.size();
    }

    @Override
    public final Set<? extends SetFunction> getArgs() {
        return this.args;
    }
    
    /**
     * 
     * @param congr congruency flag
     * @return either the set of Projection's or the set of ProjectionComp's, depending on the flaf and
     * the type of <code>this</code> operator
     */
    public final Set<ProjectionBased> congruent(boolean congr) {
        if ( this.proj_map == null) {
            HashMap<Boolean, Set<ProjectionBased>> prmap = new HashMap<>();
            this.args.forEach( f -> {
                 if (f instanceof ProjectionBased) 
                     Util.addElem(f instanceof Projection, (ProjectionBased) f, prmap);
             });
             this.proj_map = Collections.unmodifiableMap(prmap);
        }
        
        return  this.proj_map.getOrDefault(congr == this instanceof AndOp, Collections.EMPTY_SET);
    }
                
    
    /**
     * @return the subclass functions in the operand list
     */
    public final Set<Subcl> subclasses () {
        if (this.subcls == null) 
            this.subcls = getSort().isSplit() ? Collections.unmodifiableSet(Util.getType(this.args, Subcl.class) ) : Collections.EMPTY_SET;
        
        return this.subcls;    
    }
            
    Map<Integer, SortedSet<ProjectionBased>> similarCongrMap(boolean congr)  {
        return Util.mapFeature(congruent(congr), ProjectionBased::getIndex, (f1,f2)-> ((Integer)f1.getSucc()).compareTo(f2.getSucc()) );
    }
     
    /**
     * @return the number of subclass operands
     */
    public int subclSize() {
        return getSort().isSplit() ? subclasses().size() : 0;
    }     
 
    @Override
    public final ColorClass getSort() {
        return this.args.iterator().next().getSort();
    }
    
    @Override
    public final boolean equals (Object o) {
        return N_aryOp.super.isEqual(o);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        
        return  97 * hash + Objects.hashCode(this.args);
    }
    
    @Override
    public int splitDelim () {
        return ClassFunction.splitDelim(this.args, getSort());
    }
    
    
    /** performs the simplification of the operand list of either a Union or an Intersection term
     *  that is assumed to be simple (simplified), ORDERED, and repetition-less; e.g.:
     *  1.  if the intersection contains two static subclasses returns Empty
     *  2.  X_i * !X_i .. &rarr; empty
     *  2.  S - X_i * !X_i .. &rarr; !X_i
     *  4.  S-X_i * S-!X_i * S -!^nX_i.. &rarr; Empty (n: color class cardinality) ..
     * 5. if the union contains all static subclass returns All
     * 6.  S-X_i U S-!X_i .. &rarr; All
     * 7. if the union contains all possible projection succ. returns All (card dep.).
     *  @return the simplified term; <tt>this</tt> if no reduction has been cariied out;
     */
    @Override
    public SetFunction specSimplify() {
        //System.out.println("specSimp:\n"+this);
        int n = subclasses().size();
        final boolean isAndOp = this instanceof AndOp;
        final ColorClass cc = getSort();
        
        if (n > 1  && ( isAndOp ||  n == cc.subclasses() ) )
            return (SetFunction) getZero() ;
        //we check for one_missing operands
        Set<ProjectionBased> s1 = congruent(true), s2 = congruent(false);
        if (s1.size() > s2.size() ? Util.checkAny(s2, t -> s1.contains(t.opposite())) : Util.checkAny(s1, t -> s2.contains(t.opposite())))
            return (SetFunction) getZero();
        
        return cc.isOrdered() ? reduceProjections(isAndOp, cc) : this; //it may return null: means the terms should be split
    }
    
    /**
     * reduces projection-based terms of the operator; logically identical to 
     * <code>N_aryGuardOperator.reduceEqualities</code>
     * should be invoked only if the operator's color-class is ordered
     * CAREFUL: doesn't check for one_missing operands (assumes that this check was done before)
     * builds on some assumptions:
     * 1) operands have been previously simplified;
     * 2) no duplicates are present!
     * @param andop a flag denoting the kind of the operator
     * @param cc the operator's color class
     */
    private SetFunction reduceProjections(final boolean andop, final ColorClass cc) {
        final int lb  = cc.lb();
        // we first check for the presence of non-singleton, similar congruents lists, and (in case of singleton), that of one_missing non.congruent
        Map<Integer, SortedSet<ProjectionBased>> cmap = similarCongrMap(true);
        boolean to_split = false;
        for (SortedSet<ProjectionBased> similar : cmap.values())  
            if ( similar.size() > 1) {
                if ( similar.last().getSucc() - similar.first().getSucc() < lb)   
                    return (SetFunction) getZero() ; // if this.args is returned it means the term should be split ..
                
                to_split = true;
            }
        if ( to_split )
            return this;
        //similar congruents sets are singletons: we finally consider non congruent sets of similar terms
        final var fixed_size = cc.hasFixedSize();
        Set<SetFunction> argscopy = null;
        for (Map.Entry<Integer, SortedSet<ProjectionBased>> sim_nc : similarCongrMap(false).entrySet()) {
            SortedSet<ProjectionBased> poset = sim_nc.getValue(); // a set of non-congruent, simila terms
            final var posize = poset.size();
            if ( fixed_size && posize == lb)  
                return (SetFunction) getZero(); // the outcome is either 0 or S ...
            else { 
                final int key = sim_nc.getKey(), exp, min, max;
                SortedSet<ProjectionBased> singlet; // the corresponding congruent singleton
                final var one_missing = fixed_size && posize == lb -1;
                if ( one_missing || ( singlet = cmap.get( key )) != null && 
                        (max = poset.last().getSucc()) - (min = poset.first().getSucc()) < lb && Math.abs((exp = singlet.first().getSucc()) - min) < lb && Math.abs(exp  - max) < lb) {
                    argscopy = Util.lightCopy(argscopy, this.args);
                    argscopy.removeAll(poset);
                    if (one_missing) {
                    	Projection p = Projection.builder(key, Util.missingNext(poset, ProjectionBased::getSucc, 0), cc);
                        argscopy.add(andop ? p : ProjectionComp.factory(p).cast());
                    }
                }
            }
        }
        
        return argscopy == null ? this : (SetFunction) buildOp(argscopy);
    }
    
     @Override
     public final Set<Integer> indexSet() {
         return ClassFunction.indexSet(this.args);
     }
  
     
    @Override
    public final String toString() {
        return N_aryOp.super.toStringOp();
    }
    
    @Override
    public final SetFunction replace (Equality e) {
        HashMap<SetFunction,SetFunction> replacements = new HashMap<>(); 
        getArgs().forEach( f -> {
            SetFunction pf ;
            if (f.getSort().equals(e.getSort()) && (pf  = f.replace(e) ) != f) 
                replacements.put(f, pf);
        });
        if (replacements.isEmpty())
            return this;
        
        Set<SetFunction> args_copy = new HashSet<>(getArgs());
        args_copy.removeAll(replacements.keySet());
        args_copy.addAll(replacements.values());
        
        return (SetFunction) buildOp(args_copy);
    }
    
     @Override
     public final SetFunction copy(ColorClass newcc) {
        return buildOp((ClassFunction.copy(this.args, newcc)));
     }
     
}