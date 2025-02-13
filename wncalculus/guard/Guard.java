package guard;

import logexpr.LogicalExpr;
import java.util.*;
import classfunction.SetFunction;
import color.ColorClass;
import expr.Domain;
import util.Util;

/**
 * This abstract class represents the root of the hierarchy describing SN guards/filters.
 * The class and its subclasses meet the interpreter pattern.
 * @author Lorenzo Capra
 */
public abstract class Guard implements LogicalExpr/*, ComparableStep<Guard>*/ {

    
    private boolean simplified;
    private Map<? extends ColorClass , List<? extends SetFunction>> right_tuple; // if other than null, marks this guard as a filter of a tuple
    
    /**
     * 
     * @return <code>true</code> if and only if the guard is the constant True 
     */
    public boolean isTrivial() {
        return false;
    } 
    
    @Override
    public final boolean simplified () {
        return this.simplified;
    }
    
    @Override
    public void setSimplified(boolean simplified) {
       this.simplified = simplified;
    }
    
    /**
     * set <code>this</code> guard as a filter associated to a given tuple, expressed by
     * a map of colors to corresponding class-function lists
     * @param m a map between colors and corresponding class-function lists, representing
     * a typle
     */
    public final void setAsFilter(Map<? extends ColorClass , List<? extends SetFunction>> m) {
        this.right_tuple = m;
    }
    
    /**
     *
     * @return the tuple (expressed as a map) which is associated with <code>this</code> guard,
     * seen as a filter; <code>null</code> if no tuple is associayed with <code>this</code> guard
     */
    public Map<? extends ColorClass , List<? extends SetFunction>> getRightTuple() {
        return this.right_tuple;
    }
    
    
    @Override
    /**
     * @return the guard's codomain, which is equal (by definition) to the domain
     */
    public final Domain getCodomain() {
         return getDomain(); 
    }
    
    
    /** static version of indexSet working on a collection
     * @param c a given collection of guards
     * @return  the set of projection indices in <code>c</code>
     */
    public static Set<Integer> indexSet(Collection<? extends Guard> c) {
        Set<Integer> idxset = new HashSet<>();
        c.forEach( f -> { idxset.addAll(f.indexSet()); });
        
        return idxset;
    } 
    
    /** 
     * @return  the set of indexes of projections occurring on this guard
     */
    public abstract Set<Integer> indexSet();

    
    /** this trivial implementation is the only possible for guards*/
    @Override
    public  boolean differentFromZero() {
       return isTrue() ;
    }
    
    
      @Override
      public final Guard notFactory(LogicalExpr arg) {
          return Neg.factory((Guard)arg);
      }
      
      //the following methods return the passed argument, in the case the collection is a singleton

      /**
       * safely builds an "AND" corresponding to the arg list
       * if the list is empty builds a constant True with the same domain as <code>this</code>
       * @param args
       * @return an "AND" form corresponding to a (possibly empty) collection of guards  */
      @Override
      public final Guard andFactory(Collection<? extends LogicalExpr> args)  {
          return args.isEmpty() ? True.getInstance(getDomain()) : And.factory(Util.cast(args, Guard.class), false);
      }
      
      /**
       * builds an "OR" corresponding to the arg list
       * @param args
       * @return an "OR" form corresponding to a (non empty) collection of guards 
       */
      @Override
      public final Guard orFactory(Collection<? extends LogicalExpr> args, boolean disjoined)  {
          return Or.factory(Util.cast(args, Guard.class), disjoined);
      }
      
    /**
     * builds an "OR" guard from a pair of operands, assumed not disjoint
     * @param arg1 the first operand
     * @param arg2 the second operand
     * @return the newly built guard
     */
    public final Guard orFactory(LogicalExpr arg1, LogicalExpr arg2)  {
        return Or.factory(false, (Guard)arg1, (Guard)arg2);
     }
    
    /**
     * performs the "difference" between guards
     * @param p1 a guard
     * @param p2 another guard
     * @return the difference between guards
     */
    public final static Guard subtr(Guard p1, Guard p2) {
        return And.factory(p1, Neg.factory(p2));
    }
    
     
    /**
     * @param g a specified guard
     * @return <code>true</code> if and only this guard and  <code>g</code> have
     * the same color domain
     */
    final boolean sameDomainAs(Guard g) {
        return g.getDomain().equals( getDomain() );
     }

    
    /** the two methods below buildOp a constant with the same domain as the current term */
    @Override
    public final True getTrue() {    
        return True.getInstance(getDomain());
    }

    @Override
    public final False getFalse() {
       return False.getInstance(getDomain());
    }
    
    /**
     * given a list of guards, returns its (possibly empty) restriction (also called "projection") to those that refer
     * to projection indexes less than or equal to k; if the list contains any terms, with an empty index-set,
     * an exception is raised
     * @param <E> the type parameter: must be a type of guard
     * @param args the collection of guards
     * @param k the projection index used as restriction bound
     * @return a sub-list of guards, of the same type as the input list, which contains the restriction
     */
    public static <E extends Guard> Set<E> restriction (Collection<? extends E> args, int k) {
        Set<E> res = new HashSet<>();
        args.stream().filter(g ->  Collections.max( g.indexSet() ) <= k ).forEachOrdered(g -> { res.add(g); });
        return res;
    }
    
    //collection of static methods for the manipulation of list of guards (almost always assumed color-homogeneous and ordered)
   
    /**
     * checks whether a collection of guards contains an (in)equality betweeen terms of given indexes
     * @param cg the specified collection
     * @param i the firts index
     * @param j the second index
     * @param op the sign of the (in)equality
     * @return true if and only if cg contains an inequality between f(X_i) and f(X_j) of the appropriate sign 
     */
     public static boolean contains (Collection<? extends Guard > cg, int i, int j, boolean op) {
        return cg.stream().filter(g -> g instanceof Equality).map(g -> (Equality) g).anyMatch(eq -> eq.sign() == op && eq.firstIndex() ==i && eq.secondIndex() == j);
    }
    
    /**
     * @return true if and only if this guard is an "and form" which only contains elementary guards 
     * default implementation
     */
    public boolean isElemAndForm() {
        return false;
    }
    
    @Override
    public final Class<? extends Guard> type() {
        return Guard.class;
    }
    
    //convenience method (to be redefined)
    
    /**
     * @return <code>true</code> if and only <code>this</code> is an equality of type X^i == X^j
     */
    public boolean isEquality() {
        return false;
    }
    
    /**
     * @return <code>true</code> if and only if @code{this} is an equality of type X^i != X^j
     */
    public boolean isInEquality() {
        return false;
    }
    
    /**
     * @return <code>true</code> if and only if @code{this} is a membership of type X^i in C_j
     */
    public boolean isMembership() {
        return false;
    }
    
    /**
     * @return  <code>true</code> if and only if  @code{this} is a membership of type X^i notin C_j
     */
    public boolean isNotinMembership() {
        return false;
    }
    
     
    @Override
    public abstract Guard clone(Domain nd);
    
    /**
     * 
     * @return the equalities' map associated to a guard
     * this default implementation has to be redefined 
     */
    public Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap() {
        return Collections.emptyMap();
    }
    
    /**
     * 
     * @return the memberships' map associated to a guard
     * this default implementation has to be redefined 
     */
    public Map<ColorClass, Map<Boolean, Set<Membership>>> membMap() {
        return Collections.emptyMap();
    }


    /**
     * Sould work on and-forms only
     * @param cc a color class
     * @return the entire set of (in)equalities of that color class
     */
    Set<Equality> in_equality(ColorClass cc) {
        final var eqm = equalityMap().get(cc);
        if (eqm != null) {
            HashSet<Equality> s = new HashSet<Equality>(eqm.getOrDefault(true, Collections.emptySortedSet()));
            s.addAll(eqm.getOrDefault(false, Collections.emptySortedSet()));
        }
        
        return null;
    }
    
}
