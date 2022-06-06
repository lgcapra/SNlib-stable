package wncalculus.logexpr;

import java.util.*;
import wncalculus.expr.*;

/**
 * LogicalExprs is a collector of generic algorithms for the
 * manipulation of logical expressions
 * @author Lorenzo Capra
 */
public class LogicalExprs {
    
    
    /**
     * syntactically checks for the presence of complementary terms in a set of expressions
     * very efficient (O(n)) if the passed collection is an @code {HashSet}
     * @param argset a set of logical expressions    
     * @return <code>true</code> if and only if the passed list contains complementary terms
     */
    public static boolean checkComplementary(Collection<? extends LogicalExpr> argset) {
        return argset.stream().anyMatch(x -> ( x instanceof NotOp && argset.contains(((NotOp)x). getArg()) ));
    }
   
          
    /**
     * checks whether each logical term of a source list is "contained" in (implies)
     * the destination list, meant as a sum
     * @param <E> the type of list elements
     * @param l1 the first list
     * @param l2 the second list
     * @return <code>true</code> if and only if the first list is contained in the second list
     */
    public static <E extends LogicalExpr> boolean contained (Collection< ? extends E > l1, List<? extends E> l2) {
        return l1.stream().allMatch( e -> contained( e, l2));
    }
    
    /**
     * checks whether a logical term is "contained" in (implies) a list of terms
     * @param <E> the type of list elements
     * @param t a logical term
     * @param list a list of logical terms
     * @return <code>true</code> if and only if t is contained in the list
     */
    public static <E extends LogicalExpr> boolean contained (E t, List<? extends E> list) {
        if ((t = (E) t.normalize()). isFalse())
            return true; //new
        
        boolean included = false;
        //String s1 = ct.toString(); //debug
        for (ListIterator<? extends LogicalExpr> it = list.listIterator(); it.hasNext();) {
            LogicalExpr and = t.andFactory(t, it.next()). normalize( );
            if (! and.isFalse() ) { //ct and ot not disjoint
                included = t. equals( and ) || contained ( t.diff(and).normalize(), list.subList(it.nextIndex() , list.size()));
                break;
            }
        }
        
        return included;
    }
    

    /**
     * version of <code>isNormalAndForm</code> operating on a collection of boolean terms
     * @param c the specified collection
     * @return <code>true</code> if and only if the collection's elements are normal AND forms
     */
    public static boolean isNormalAndForm(Collection<? extends LogicalExpr > c) {
        return c.stream().allMatch(arg -> arg.isNormalAndForm() );
    }

    /**
     * @param c a collection of boolean terms
     * @return <code>true</code> if and only if the collection contains neither non-terminals
     * nor boolean constants
     */
     public static boolean simple(Collection<? extends LogicalExpr > c) {
        return c.stream().noneMatch( t -> t instanceof NonTerminal || t.isFalse() || t.isTrue());
    }
    
   public static boolean print;  
   /**
     * disjoin algorithm (new version, built on @see subtractFrom):
     * rewrites a given set of terms (assumed in normal and-form) into an equivalent,
     * pairwise-disjoint form, operating in a destructive way; logically it corresponds to rewrite t1 + t2 + t3 + ...tn
     * into t1 + (t2 + t3 + ... tn) &rarr; ((t1-t2)+t2) + (t3 + ... tn) &rarr; (((t1-t2)-t3)+(t2-t3) +t3) + (... tn) &rarr; ... 
     * in other words the list of disjoint terms is incrementally built, at each step the element t of the list is considered
     * (which is elementary, what is important, on the basis of the initial assumption), subtracted to each element of the disjoint list and finally
     * added to the list, thus preserving the list's disjointness;
     * optimizations: only non disjoint terms are substracted, if t is included in any e then t is not added to the list, finally
     * if t includes e then e is removed from the list
     * @param <E> the terms' domain
     * @param terms the collection of terms to be put into a disjoint form
     * @return a list of pair-wise disjoint terms; an empty list in no disjoining
     * has taken place 
     */
   public static <E extends LogicalExpr> List<E> disjoin(Collection<? extends E> terms) {
        if (terms.size() > 1 ) {
            boolean done = false;
            List<E> disjoint = new LinkedList<>();
            for (E t : terms) {
                Boolean changed = subtractFrom(disjoint, t);
                if (changed != null)
                    disjoint.add(t);
                if (changed == null || changed)
                    done = true;
            }
            if (print) System.out.println("disjoint:" + disjoint);//debug
        
            if (done)
                return disjoint;
        }
        
        return Collections.emptyList(); 
   }
   
    
    /**
     * "substracts" the head element from every element e of the passed list;
     * it does nothing if e and head are disjoint or head is included in some
     * e (in this case a return value signals that); if head includes e then e is removed
     * @param <E> the type of boolean expressions
     * @param l the list of expressions
     * @param head the element to be subtracted
     * @return <code>null</code> if some elements including head is found; otherwise, <code>true</code>
     * iff the list is modified 
     */
    private static <E extends LogicalExpr> Boolean subtractFrom(List<E> tail , E head)  {
        Boolean changed = false, not_included = true;
        Class<E> type = head.type();
        for (ListIterator<E> it = tail.listIterator(); it.hasNext() ; ) {
            E curr = it.next();
            LogicalExpr in = head.andFactory(head, curr).normalize();
            if (not_included && ! in.isFalse() ) {
                changed = true;
                if (curr.equals(in)) // head including curr
                    it.remove();
                else if ( head.equals( in ) )  // head included in curr
                    not_included = false; 
                else 
                    it.set(type.cast(curr.diff(head).normalize()) );
            }
        }
        if (! not_included )
           return null;

        return changed;
    }
    
    /**
     * checks whether a list of terms are pairwise disjoint
     * @param args the specified list
     * @return <tt>true</tt> if and only if the terms of the specified list are pairwise disjoint
    */
    public static boolean disjoined(Collection<? extends LogicalExpr> args)  {
        int size = args.size();
        List<? extends LogicalExpr> list_of_args = args instanceof List<?> ? (List<? extends LogicalExpr>) args : new ArrayList<>(args);
        for (int i = 0; i < size - 1; i++) 
            for (int j = i + 1; j < size; j++) 
                if (! list_of_args.get(i).disjoined( list_of_args.get(j) )) { 
                    return false;
                }
                
        return true;
    }

   
}
