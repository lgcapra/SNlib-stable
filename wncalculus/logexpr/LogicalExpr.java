package wncalculus.logexpr;

import java.util.*;
import wncalculus.expr.*;

/**
 * this interface defines the super-type for any sorted expression (term) of a "logical" domain (set, boolean, etc.)
 * @author Lorenzo Capra
 */
public interface LogicalExpr extends ParametricExpr  {
      
     /**
      * 
      * @return a normalized expression equivalent to <code>this</code> on which
      * a cast is done
      */
     @Override
     default LogicalExpr normalize() {
         return (LogicalExpr) ParametricExpr.super.normalize();
     }
     /**
      * invokes the normalization method and, if the parameter flag is set
      * and the normalization result is a "sum", puts it in a normalized/disjoint form
      * @param todisjoin a flag for (possibly) disjoining the terms of the result
      * @return a normalised expression (with pair-wise disjoint terms) equivalent to <code>this</code>
      */
     default LogicalExpr normalize(boolean todisjoin) {
        LogicalExpr e = normalize(); 
        //System.out.println("normalize(true) -- res intermedio ->\n"+e);
        return todisjoin && e instanceof OrOp ? ((OrOp) e).disjoinAndNormalize() : e;
     }
     
    /**
      * @return <code>true</code> if and only if <code>this</code> expression is
      * an "And" form
    */
    default boolean isAndForm( ) {
        return terminal() ;
    }
    
     /**
     * @return <code>true</code> if and only if this expression is a normal "and form"
     */
      default boolean isNormalAndForm( ) {
        return terminal() ;
      }
      
      /**
     * checks whether <code>this</code> and a given term are disjoint: general version
     * if used during the f.p. algorithm it should be invoked just on normal "AND" forms
     * @param e a term
     * @return <code>this</code> AND e == FALSE; null otherwise
     */
    default boolean disjoined( LogicalExpr e)  {
         //System.out.println("disjoned");
         LogicalExpr in = andFactory(this, e). normalize();
         boolean d = in. isFalse();
         //if (!d)  //debug
             //System.out.println(this + "*" + e +": -> "+in);
         
         return d;
    }
  
       /**
        * @return the "zero" constant of the monoid
       */
       LogicalExpr getFalse();
       
       
      /**
       * @return the "true" (universe) constant for <code>this</code> domain
      */  
      LogicalExpr  getTrue();
      
      /**
       * @return the "zero" trems for a logical n-ary op; <code>null</code> if <code>this</code> expression
       * is not a n-ary operator
       */
      default LogicalExpr getZero() {
          return null;
      }
      
      
     /** checks (mostly at syntax level) whether <code>this</code> expression corresponds to the
       * <code>false</code> - it implements a sufficient condition
       * @return <code>true</code> if this expression is equivalent to the logical constant false  
     */
      default boolean isFalse() {
          return false;
      }
      
       /** 
        * sufficient condition for <code>this</code> term to never map to <code>false</code>
        * @return <code>true</code> if <code>this</code> never maps to <code>false</code>
        */
      boolean differentFromZero();
    
      
      /** 
       * checks (mostly at syntax level) whether <code>this</code> expression corresponds to the
       * <code>true</code> - it implements a sufficient condition
       * @return <code>true</code> if this expression is equivalent to the logical constant true  
       */
      default boolean isTrue() {
          return false;
      }
      
    /** 
     * checks the equivalence of this term to the "truth" constant;
     * WARNING: this method should never be invoked from inside the fixed-point simplification algorithm!!
     * @return <code>true</code> if and only if <code>this</code> is equivalent to TRUE  
     */
     default boolean truthEquivalent() {
        return getTrue().diff(this).normalize( ).equals( getFalse() );
     }
     
     /**
     * "syntactical" version of <code>implies</code>, working just on terms that are "normal and forms";
     * this version can be safely invoked from inside the f.p. simplification algorithm
     * @param e a logical term
     * @return <code>true</code> if and only if both <code>this</code> and e are "normal and forms"
     * and <code>this</code> implies (i,e, is included in) e
     * @throws wncalculus.expr.IllegalDomain if the expressions' domains are different
     */
     default boolean implies (LogicalExpr e) {
        return equals( andFactory(this, e).normalize() );
     } 
     
     /**
     * performs the standard algorithm of logical "difference" between <code>this</code> and 
     * another term
     * @param e a logical term
     * @return t1 and not(e)
     * @throws wncalculus.expr.IllegalDomain if the expressions' domains are different
     */
    default  LogicalExpr  diff(LogicalExpr e)  {
        return andFactory(this, notFactory(e));
    }
     
     /**
      * build a "NOT" operator
     * @param arg the operand
     * @return a "NOT" operator with the specified operand
      */
      LogicalExpr notFactory(LogicalExpr arg);
      
      
      //the following methods return the passed argument, in the case the collection is a singleton
      //<E extends LogicalExpr> E andFactory(Collection<? extends E> args) ;

     /**
     * build an "AND" operator
     * @param args a collection of operands
     * @return an "AND" operator for the collection of operands
     * @throws wncalculus.expr.IllegalDomain if the operands' domain are different
     */  
       LogicalExpr andFactory(Collection<? extends LogicalExpr> args);
      
     
    /**
     * build an "AND" operator
     * @param args a "list" of operands (expressed as a varargs)
     * @return an "AND" operator for the list of operands
     * @throws wncalculus.expr.IllegalDomain if the operands' domain are different
     */       
      default  LogicalExpr andFactory(LogicalExpr ... args) {
          return andFactory(Arrays.asList(args));
      }
      
    /**
     * build an "OR" operator
     * @param args a collection of operands
     * @param disjoined a flag telling if the operands have to be assumed
     * pair-wise disjoint
     * @return an "OR" operator for the collection of operands
     * @throws wncalculus.expr.IllegalDomain if the operands' domain are different
     */
    LogicalExpr orFactory(Collection<? extends LogicalExpr> args, boolean disjoined);
      
    
    /**
     * build an "OR" operator
     * @param args a "list" of operands (a varargs parameter)
     * @param disjoined a flag telling if the operands have to be assumed
     * pair-wise disjoint
     * @return an "OR" operator for the list of operands
     * @throws wncalculus.expr.IllegalDomain if the operands' domain are different
     */      
      default LogicalExpr orFactory(boolean disjoined, LogicalExpr ... args) {
          return orFactory(Arrays.asList(args), disjoined);
      }
          
    /**
     * simplifies and (possibly) split a <code>this</code> expression according to the f.p.algorithm,
     * tries to "merge" (possible) resulting "sums" of terms, checking their "truth"-equivalence;
     * if the flag is set <code>true</code> puts the result into a disjoint form
     * @param todisjoin flag indicating to put the result(s) in a disjoint form
     * @param verbose verbose output flag
     * @return the resulting list of equivalent (split) terms
     */
    default Set<LogicalExpr> simplify (boolean todisjoin, boolean verbose) {
        long startTime = System.currentTimeMillis();
        Set<LogicalExpr> terms = new LinkedHashSet<>();
        for (ParametricExpr e : simplify(/*false*/true) ) {
            LogicalExpr tx = (LogicalExpr) e;
            if (tx instanceof OrOp ) {
                OrOp or = (OrOp) tx;    
                //if ( or. truthEquivalent() ) 
                    //tx = tx.getTrue();
                //else 
                if (todisjoin && ( tx  = or.disjoinAndNormalize() ) instanceof OrOp )
                    tx = ( (OrOp)tx). merge();
            }
            terms.add(tx);
        }    
        Expressions.mergeResults(terms);
        
        if (verbose) {
            long endTime = System.currentTimeMillis();
            long seconds = endTime - startTime;
            System.out.println("overall simplification time (including truth-equivalence test): " + seconds + " ms");
        }
        
        return terms;
    }
    
    /**
     * default version of simplify
     * @return  the resulting list of equivalent (split) terms
     */
    @Override
    default Set<LogicalExpr> simplify() {
        return simplify(true/*false*/, true);
    }
    
      
}
