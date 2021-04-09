package wncalculus.logexpr;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import wncalculus.expr.Expression;
import wncalculus.expr.N_aryOp;
import static wncalculus.logexpr.LogicalExprs.checkComplementary;
import static wncalculus.logexpr.LogicalExprs.contained;
import wncalculus.tuple.FunctionTuple;
import wncalculus.util.Util;

/**
 * this interface defines the super-type of boolean operators that can be viewed as "disjunctions"
 * @author lorenzo capra
 * @param <E> the operands type
 */
public interface OrOp<E extends LogicalExpr> extends LogicalExpr, N_aryOp<E>  {
    
    /** 
     * @return a compact representation of the operator; it should be called after <code>this</code>
     * term  has been normalized
     */
    LogicalExpr merge ();
    
    
    /** 
     * checks for any sufficient condition for disjointness
     * @return <code>true</code> if and only if <code>this</code> has been shown
     * to be pair-wise disjoint
     */
    boolean disjoined();
    
    /**
     * marks <code>this</code> as disjoint, without doing anything! 
     */
    void setDisjoint();
    
    /**
     * provides a sufficient conditions for an "Or" expression to be "false"
     * @return  <code>true</code> if the operator is "false"
     */
    @Override
    default boolean isFalse() {
        LogicalExpr zero = getFalse();
        
        return getArgs().stream().allMatch( t -> zero.equals(t) );
    }
    
    /**
     * provides a sufficient condition for an "Or" expressions to be "true";
     * @return  <code>true</code> if the operator is "true"
     */
    @Override
    default boolean isTrue( ) {
        return getArgs().contains( getTrue());
    }
    
    @Override
    default E getIde() {
        Class<E> type = type();
        return type.cast(getFalse());
    }
    
    @Override
    default LogicalExpr getZero() {
        return getTrue();
    }
    
    /**
     * 
     * checks the equivalence of this term to the "truth" constant in an optimized way
     * WARNING: this method should never be invoked from inside the fixed-point simplification algorithm!!
     * @return <code>true</code> if and only if <code>this</code> is equivalent to TRUE  
     */
    @Override
    default boolean truthEquivalent ( ) {
        List<? extends E> args = Util.asList( getArgs() ); //
        //System.out.println("thruth equivalence: term size "+(args) ); //profiling
        LogicalExpr t = args.get(0), compl_t = (LogicalExpr) t.getTrue().diff(t). normalize(); // we take the complement of one term
        
        return compl_t instanceof OrOp ? contained( ((OrOp)compl_t).getArgs()  , args.subList(1, args.size()) )
                         : contained (compl_t, args.subList(1, args.size()));
      }
    
    @Override
    default E genSimplify ( ) {
        //System.out.println("OrOp (86)\n"+this);//debug*/
        E res = N_aryOp.super.genSimplify();
        if (res instanceof OrOp ) {
            E True = getTrue().cast();
            OrOp<E> orop = (OrOp<E>) res;
            Collection<E> args = orop. getArgs(); 
            if ( args. contains(True) || ! type().equals( FunctionTuple.class) && checkComplementary(args) )
                return True ;
            //new
            if (!orop.disjoined() && Util.checkAll(args, Expression::terminal)) //forse meglio elementare che terminal
                return orop.disjoin().cast();
        }
        
        //System.out.println("---> :\n"+res);//debug*/
        return res;
    }
    
    /**
     * disjoin <code>this</code> without doing (directly) any normalization
     * @return either an equivalent disjoint form of <code>this</code> expression;
     * or <code>this</code> if the term is marked as, or comes to be, already disjoint
     * (in the latter case it is marked as disjoint)
     */
    default LogicalExpr disjoin ( ) {
        if (! disjoined() ) {
             Collection<E> args = LogicalExprs.disjoin( getArgs());
             if (! args.isEmpty() ) 
                 return orFactory(args, true);
             
             setDisjoint(); // needed
        }
        
        return this;
    }
    
    /**
     * disjoin and (if the procedure is effective) normalizes <code>this</code> term
     * @return either an equivalent disjoint-normalized term; or <code>this</code>,
     * if it is already (or marked as) disjoint
     */
    default LogicalExpr disjoinAndNormalize() {
        LogicalExpr de = disjoin();
        //System.out.println("disgiunto:\n"+de);
        return equals(de) ? this : de.normalize();
    }
    
}
