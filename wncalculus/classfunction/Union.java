package classfunction;

import java.util.*;
import logexpr.OrOp;
import expr.Interval;
import util.Util;

/**
 * this class defines unions over class functions; union is  an "OR" operator
 * @author lorenzo capra
 */
public class Union extends N_aryClassOperator implements OrOp<SetFunction>  {
    
    private final static String OPSYMB = " + "/*" U "*/;//the union op. symbol
    private boolean disjoined; // default is false
    
    private Union (Set<? extends  SetFunction> guards, boolean check, boolean disjoined) {
        super(guards,check);
        this.disjoined = disjoined;
    }
   
    /**
     * build a <code>Union</code> of class-functions mapping to sets from a collection;
     * if the collection is a singleton, its only element is returned
     * @param arglist a collection of functions
     * @param check the functions' color-class check flag
     * @param disjoined disjointness flag (if set, the operands are assumed pair-wise disjoint)
     * @return the function resulting from the union of the operands
     */
    public static SetFunction factory(Collection<? extends SetFunction> arglist, boolean check, boolean disjoined) {
        Set<? extends SetFunction> asSet = Util.asSet(arglist);
        return asSet.size() < 2 ?  asSet.iterator().next() : new Union (asSet, check, disjoined);
    }
    
  /**
     * build a <code>Union</code> of class-functions mapping to sets from a collection,
     * checking the operands' color-classes
     * @param arglist a collection of functions
     * @param disjoined disjointness flag (if set, the operands are assumed pair-wise disjoint)
     * @return the function resulting from the union of the operands
     */
    public static SetFunction factory(Collection<? extends SetFunction> arglist, boolean disjoined)  {
        return factory(arglist, false, disjoined);
    }
    
    /**
     * build a <code>Union</code> of class-functions mapping to sets from a list (varargs);
     * if the list is a singleton, its only element is returned
     * @param args a list (varargs) of functions
     * @param check the functions' color-class check flag
     * @param disjoined disjointness flag (if set, the operands are assumed pair-wise disjoint)
     * @return the function resulting from the union of the operands
     */
    public static SetFunction factory (boolean check, boolean disjoined, SetFunction ... args)  {
        return factory(Arrays.asList(args), check, disjoined); 
    }
    
   /**
     * build a <code>Union</code> of class-functions mapping to sets from a list (varargs),
     * checking the operands' color-classes
     * @param args a list (varargs) of functions
     * @param disjoined disjointness flag (if set, the operands are assumed pair-wise disjoint)
     * @return the function resulting from the union of the operands
     */
    public static SetFunction factory(boolean disjoined, SetFunction ... args)  {
        return factory(false, disjoined, args);
    }
    
    @Override
    public SetFunction buildOp(Collection <? extends SetFunction> args) {
        return factory(args, this.disjoined);
    }

    @Override
    public String symb() {
        return Union.OPSYMB;
    }    
    
    @Override
    /**
     * optimized version: checks whether the union is disjoint form
     * @return the "sum" of cardinalities of operands, null if the check fails
     * or the cardinality of some terms cannot be computed
     */
    public Interval card() {   
        Interval card, sum = null;
        if ( this.disjoined  ) {
            sum =  new Interval();
            for (SetFunction f : getArgs()) {
                if ( (card = f.card()) == null ) {
                     sum = null;
                     break;
                 }
                 sum = sum.sum(card);
            }
        }  
        return sum;
    }

     /**
     *  first invokes the simplifyArgs method, then checks for the
     *  presence of all sublcasses or 
     *  tries to reduce possible "extended complements", as in the example below:
     * (S-X_1 \cap S -X_2 ) + X_1 + ..&rarr;  S-X_2 + X_1 + .. 
     * careful!! this reduction doesn't preserve disjointness
     * @return a reduced term or <code>this</code> if no reduction has been done
     */
    @Override
    public SetFunction specSimplify() {
       //System.out.println(this+ " -> "); //debug
        final SetFunction red = super.specSimplify();
        if ( red instanceof Union un ) {
            final Set<? extends SetFunction> args = un.getArgs();
            final int nsubcl = getSort().subclasses();
            if (nsubcl > 1 && countSubclasses(args) == nsubcl)
               return All.getInstance(getSort()); //just an optimization
            // non preserva disgiunzione
            /*Set<ProjectionBased> projections = un.congruent(false); // the set of projections of the union
            if (! projections.isEmpty() ) { 
               Set<SetFunction> new_args = null;
               for (Intersection in : Util.getType(args, Intersection.class) ) { 
                    Set<SetFunction> in_args = null; // (usually) light inefficiency (ok)
                    for (ProjectionBased p : projections) {
                        SetFunction opp = (SetFunction) p.opposite();
                        if ( in.getArgs().contains( opp ) ) { 
                            in_args = Util.lightCopy(in_args, in.getArgs());
                            if (in_args.remove(opp) && in_args.isEmpty() )
                               return All.getInstance(getSort()); //for example (S - X^1 - X^2) + X^1 + X^2
                        }
                    }
                    if (in_args != null) { // if any change done we replace in with a new one without removed factors
                        new_args = Util.lightCopy(new_args, args);
                        new_args.remove(in);
                        new_args.add(Intersection.factory(in_args));
                    }
               }
               if ( new_args != null )
                   red = Union.factory(new_args, false);
            }*/           
        }
       
       return red;
    }
    
    /*
    computes the set of indices of subclasses in a given collection (assumed of the same colour) 
    */
    private static int countSubclasses(final Collection<? extends ClassFunction> c) {
        int res = 0;
        res = c.stream().filter(f -> (f instanceof Subcl)).map((_item) -> 1).reduce(res, Integer::sum);
        return res;
    } 
            
    @Override
    public SetFunction merge() {
        return this;
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
    public final SetFunction setDefaultIndex( ) {
        return Union.factory(ClassFunction.setDefaultIndex( getArgs() ), this.disjoined); 
    }

}
