package expr;

import java.util.*;
import util.Util;

/**
 * this class is a collector of generic algorithms for the
 * manipulation of (possibly parametric) expressions
 * @author Lorenzo Capra
 */
public class Expressions {
          
    /**
     * simplifies a list of expressions (preserving the order) operating in a destructive way:
     * the normalized expressions type is forced to the specified one
     * @param <E> the expressions domain
     * @param arglist the list of expressions 
     * @return  <code>true</code> if and only if the list has been changed
     * @throws NoSuchElementException if the list is empty
     */
    public static <E extends Expression> boolean normalize(List<E> arglist) {
        //System.out.println("normalize "+iset + " -> "); //debug
        boolean changed = false;
        Class<E> type = arglist.get(0).type();
        for (ListIterator<E> ite = arglist.listIterator() ; ite.hasNext(); ) {
            E e = ite.next(), normal = type.cast( e.normalize() );
            if ( ! e.equals(normal) ) { 
                ite.set( normal );
                changed = true;
            }
        }
        //System.out.println(iset); //debug    
        return changed;
    }
    
      /**
     * simplifies a collection of expressions, operating in a destructive way:
     * the normalized expressions type is forced to the specified one
     * @param <E> the expressions domain
     * @param arglist the list of expressions 
     * @return  <code>true</code> if and only if the list has been changed
     */
    public static <E extends Expression> boolean normalize(Collection<E> arglist) {
        //System.out.println("normalize "+arglist + " -> "); //debug
        boolean normalized = false;
        List<E> nargs = new ArrayList<>();
        for (Iterator<E> ite = arglist.iterator() ; ite.hasNext() ; ) {
            E  e = ite.next(), normal = e. normalize().cast();
            if ( ! e.equals(normal) ) {
                ite.remove();
                nargs.add(normal) ;
                normalized = true;
            }
        }
        //System.out.println(nargs); //debug
        arglist.addAll(nargs);
        
        return normalized;
    }    
    
    /**
     * provides a detailed textual description for a collection of logical terms
     * @param c the terms' collection
     * @return the terms's description, one for each row
     */
    public static String toStringDetailed(Collection<? extends Expression> c) {
        String out = "";
        out = c.stream().map(f -> f.toStringDetailed() + '\n').reduce(out, String::concat);
        
        return out;
    }
    
    /**
     * print a collection of expressions, where each  term may be followed by
     * information about its cardinality, depending on the implementation of <code>printCard</code>
     * @param result the collection to be printed
    */
    public static void printResults (Collection<? extends Expression> result) {
        System.out.println("normalization result:");
        int i = 1;
        for (Expression e : result) {
            System.out.printf("%-2d) %s",i++, e.toStringDetailed()+'\n');
        }
    }


    /**
     * checks the domains of a collection of expressions
     * @param terms a collection of expressions
     * @throws IllegalDomain if the domains of the expressions are not congruent
     */
    public static void checkDomain(Collection<? extends Expression> terms) {
        Util.checkProperty(terms, Expression::sameDomain, e-> "dom: "+e.getDomain(), IllegalDomain.class);
    }
    
   /**
    * checks the co-domains of a collection of expressions; in the case the flag <code>fixedSize</code> is set,
    * checks also the co-domains
    * @param terms a collection of expressions
    * @throws IllegalDomain if the co-domains  of the expressions are not congruent
    */
    public static void checkArity(Collection<? extends Expression> terms) {
        Util.checkProperty(terms, Expression::sameArity, e-> ("dom: "+e.getDomain()+"\ncodom: "+e.getCodomain()), IllegalDomain.class);
    }
     
}   
