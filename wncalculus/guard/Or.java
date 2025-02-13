package guard;

import java.util.*;
import logexpr.OrOp;
import expr.Domain;
import util.Util;

/**
 * this class implements the boolean OR operator
 * @author Lorenzo Capra
 */
public final class Or extends NaryGuardOperator implements OrOp<Guard> {
     
    private boolean disjoined; // deafult is false
    
    private Or (Set< ? extends Guard> guards, boolean check, boolean disjoined) {
        super(guards,check);
        this.disjoined = disjoined;
    }
     /**
      * build an <tt>Or</tt> guard from a collection of operands; if the collection is
      * a singleton, extracts and return its only element
     * @param arglist the list of operands
     * @param check domain checking flag
     * @param disjoined disjointness flag
     * @return the newly built guard
     */
    public static Guard factory(Collection<? extends Guard> arglist, boolean check, boolean disjoined) {
        Set<? extends Guard> asSet = Util.asSet(arglist);
        return asSet.size() < 2 ?  asSet.iterator().next() : new Or (asSet, check, disjoined);
    }
     
    /**
     * build an <tt>Or</tt> guard from a collection of operands, by checking the
     * domains of operands
     * @param arglist a collection of guards
     * @param disjoined disjointness flag
     * @return the newly built guard
     */
    public static Guard factory(Collection<? extends Guard > arglist, boolean disjoined)  {
        return factory(arglist, true, disjoined);
    }
    
    /**
     * build an <tt>Or</tt> guard from a list operands expressed as a varargs
     * @param check  domain-check flag
     * @param disjoined  disjointness flag
     * @param args a list of operands
     * @return the newly built guard
     */
    public static Guard factory (boolean check, boolean disjoined, Guard ... args)  {
        return factory(Arrays.asList(args), check, disjoined); 
    }
    
    /**
     * build an <tt>Or</tt> guard from a list operands expressed as a varargs,
     * by checking domains
     * @param disjoined  disjointness flag
     * @param args a list of operands
     * @return the newly built guard
     */
    public static Guard factory(boolean disjoined, Guard ... args)  {
        return factory(true, disjoined, args);
    }
    
    @Override
    public Guard buildOp(Collection <? extends Guard> args)  {
        return factory(args, false, false);
    }
    
    @Override
    public String symb() {
        return "+";
    }
  
  
    @Override
    public Or clone(Domain new_dom)  {
        return new_dom.equals( getDomain()) ? this : new Or(cloneArgs(new_dom), false, this.disjoined);
    }

    @Override
    public Guard merge() {
        return this;
    }

    @Override
    public boolean disjoined() {
        return this.disjoined;
    }
    
    @Override
    public Guard specSimplify() { //new
        //System.out.println("(Or.specSimplify)\n"+this);
        return simple() ? super.specSimplify() : this ;
        //System.out.println("--> \n"+red);
    }

    @Override
    public void setDisjoint() {
        this.disjoined = true;
    }
    
}
