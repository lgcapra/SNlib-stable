package wncalculus.guard;

import java.util.*;
import wncalculus.classfunction.*;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.expr.IllegalDomain;
import wncalculus.util.ComplexKey;
import wncalculus.util.Pair;

/**
 * this class represents predicates checking for the (in)equality of two
 * variables (i.e., projection functions)
 * @author Lorenzo Capra
 */
public final class Equality extends ElementaryGuard implements Comparable<Equality>  {
    
    /**
     *
     */
public  static final Map< ComplexKey, Equality> VALUES = new HashMap<>();

    /**
     * base constructor
     * @param p1 the first variable
     * @param p2 the second variable
     * @param op the equality's sign
     * @param dom the equality's domain
     */
    private Equality(Projection p1, Projection p2, boolean op, Domain dom)  { 
        super(p1, p2, op, dom);
    }
        
    /**
     * build an <tt>Equality</tt>; if the two variables (projections) have the same index it may result in
     * <tt>True</tt> or <tt>False</tt>
     * @param p1 a projection
     * @param p2 a projection
     * @param op a flag denoting the sign
     * @param dom a domain
     * @return a Guard corresponding to the (in)equality between @param {p1} and @parm {p2}
     * of the specified domain
     * @throws IllegalDomain if the colors of the variables do not match
     */
    public static Guard builder(Projection p1, Projection p2, boolean op, Domain dom)  {
        ColorClass cc = checkDomain(p1,p2,dom);
        int i1  = p1.getIndex(), i2 = p2.getIndex();
        if (i1 == i2) {
            Guard T = True.getInstance(dom), F = False.getInstance(dom);
            if ( p1.getSucc() == p2.getSucc() )  //the same as np1 ==np2
                return  op ? T : F;
            
            if (Math.abs(p2.getSucc() - p1.getSucc() ) < p1.getSort().lb() ) 
                return  op ? F : T;
        }
        //the variable indices are different
        if ( i1  > i2 ) { //swap p1 and p2
            Projection temp = p1;
            p1 = p2;
            p2 = temp;
        }
        checkIndex(p2, dom);
        
        int exp = p1.getSucc(), exp2 = p2.getSucc();
        if (!op && cc.isOrdered() && cc.ccSize( ) == 2) { // in this case we invert both the sign and the 2nd successor
            op = true;
            exp2 = exp2 == 0 ? 1 : 0;
        }
        
        p1 = p1.setExp(0);
        p2 = p2.setExp(exp2 - exp);
        
        Equality eq;
        ComplexKey k = new ComplexKey(p1, p2, op, dom);
        if ( (eq = VALUES.get(k)) == null) {
            VALUES.put(k,  eq = new Equality(p1, p2, op, dom)) ;
            //Util.checkBuilderOneStep(eq, VALUES); //debug
        }
        
        return eq;
    }
       
    /**
     * @return the 2nd operand, doing a cast
     */
    @Override
    public Projection getArg2() {
        return (Projection) super.getArg2();
    }

    /**
     *
     * @return the index of the 2nd variable
     */
    public Integer secondIndex () {
        return getArg2().getIndex();
    }
    
    /**
     * 
     * @return the successor (previously exponent) of the 2nd operand (the first is zero) 
     */
    public Integer getSucc () {
        return getArg2().getSucc();
    }

    
    @Override
    public int splitDelim () {
        return getArg2().splitDelim(); // the equality is in canonical form
    }
    
    @Override
    public String opSymb() {
        return sign() ? " = " : " != ";
    }

    @Override
    public Equality opposite() {
        return (Equality) Equality.builder(getArg1(), getArg2(), ! sign(), getDomain()); 
    }
    
    @Override
    public Guard replace(Equality eq) {
        Projection p1 = getArg1().replace(eq), p2 = getArg2().replace(eq);
        
        return p1 == getArg1() && p2 == getArg2() ? this : Equality.builder(p1, p2, sign(), getDomain() ); 
    }

    @Override
    public Set<Integer> indexSet() {
        return new HashSet<>(Arrays.asList(new Integer[] { firstIndex(), secondIndex()}));
    }
    
    @Override
    public String toString() {
        return getArg1()+opSymb()+getArg2();
    }
        
    /**
    @return the exponents of the second argument of the list of equalities which
    are missing in the range [0..<code>bound</code>-1]; two assumptions are made
    * 1) the color class of equalities is ordered and single value; 2) the passed list is
    * assumed to contain "similar" equalities
     */
    private static Set<Integer> missingExp(Collection<? extends Equality> l, int bound) {
        Set<Integer> exps = new HashSet<>();
        for (int i = 0; i < bound; i++) 
            exps.add(i);
        l.forEach(e -> { exps.remove(e.getArg2().getSucc()); });
        
        return exps;
    }
    
    /**
     * @param l a list of similar (in)equalities
     * @param succ_bound the successor upper bound
     * @return the list of opposite equalities which are missing in the specified list, considering
     * the successors in [0, <code>succ_bound</code>-1]; two assumptions are made
     * 1) the color class  of equalities is ordered and single-value; 2) the passed list is assumed
     * to contain "similar" (in)equalities of the same sign;
     * returned (in)equalities have opposite sign
     */
    public static List<Guard> missingOppEqs(Collection<? extends Equality> l, int succ_bound) {
        List<Guard> res = new ArrayList<>();
        Equality eq = l.iterator().next();
        Boolean sign = ! eq.sign();
        missingExp(l, succ_bound).forEach( n -> { res.add(Equality.builder(eq.getArg1(), eq.getArg2().setExp(n), sign, eq.getDomain())); });
        
        return res;
    }
    
    /**
     * builds a set of inequalities corresponding to the passed index set, color class,
     * and domain
     * @param s a set of indices
     * @param cc a color class
     * @param d the color domain of the guard
     * @return a corresponding guard (set of inequalities)
     */
    public static Guard inequalitySet(Set<Integer> s, ColorClass cc, Domain d) {
        List<Integer> l = new ArrayList<>(s);
        List<Guard> ineqs = new ArrayList<>();
        for (int i = 0; i < l.size() - 1; i++) 
            for (Integer j : l.subList(i+1, l.size()) ) 
                ineqs.add(Equality.builder(Projection.builder(l.get(i), cc), Projection.builder(j,cc), false, d));
        
        return And.factory(ineqs);
    }

    
    @Override
    public Intersection toSetfunction(Projection f) {
        int i = f.getIndex();
        SetFunction other = null;
        if ( i == firstIndex()) 
            other = Successor.factory(f.getSucc(), getArg2()); //the succ of 1st operand of the guard is 0
        else if (i == secondIndex()) 
            other = Successor.factory(f.getSucc() - getArg2().getSucc(), getArg1()); 
        
        return other == null ? null : (Intersection)Intersection.factory(f, sign() ? other : Complement.factory(other));
    }
    

    /**
     * 
     * @return <code>true</code> if and only if the variables have the same index
     * (the equality is equivalent to a logical constant but some split is still needed)
     */
    public boolean sameIndex () {
        return Objects.equals(firstIndex(), secondIndex());
    }
    
    
    @Override
    public boolean isEquality() {
        return sign();
    }
    
    /**
     * @return <tt>this</tt> if <tt>this</tt> is an equality of type X^i == X^j; @code{null} otherwise
     */
    @Override
    public boolean isInEquality() {
        return ! sign() ;
    }
    
    /**
     * given a collection of (in)equalities, assumed of the same (ordered) colour, computes
     * the maximal offset between successors for each projection index, considering that a symbol
     * X^i can appear both as left and right member of an (in)equality;
     * for example, if we have X^1 != !X^2, X^2!=!X^3 the offset, as for X^2, is 2 (1 - -1)
     * @param elist a collection of (in)equalities
     * @return the max offset between successors of each projection index in @param elist
     */
    public static int succOffset(Collection<? extends Equality> elist) {
        int offset = 0;
        for (Pair<Integer, Integer> p : succBounds(elist).values())
            offset = Math.max(offset, p.getValue() - p.getKey());
        //System.out.println("max succ offset of : " + elist +"\n"+offset); // debug
        return offset;
    }
    /**
     * given a collection of (in)equalities, assumed of the same (ordered) colour, computes
     * the minimal and maximal successors for each projection index, considering that a symbol
     * X^i can appear both as left and right member of an (in)equality
     * for example if we have X^1 != !X^2, X^2!=!X^3
     * the min and max, as for 2 (X^2), are -1 and +1
     * @param elist a collection of (in)equalities
     * @return a map of pairs holding the min and max successor of each projection index in @param elist
     */
    public static Map<Integer, Pair<Integer, Integer>> succBounds(Collection<? extends Equality> elist) {
        Map<Integer, Pair<Integer, Integer>> succmap = new HashMap<>();
        elist.forEach(eq -> {
            int succ = eq.getArg2().getSucc();
            Pair<Integer, Integer> s1, s2;
            if ( (s1 = succmap.putIfAbsent(eq.getArg1().getIndex(), new Pair<>(-succ, -succ))) != null)
                set_Min_Max(s1, -succ);
            if ((s2 = succmap.putIfAbsent(eq.getArg2().getIndex(), new Pair<>(succ, succ))) != null)  
                set_Min_Max(s2, succ);
        });
        
        return succmap;
    }
    
    /**
     * sets-up a pair of integer values, representing a minimum and a maximum, w.r.t.
     * a new value (if the current pair is not <code>null</code>
     * @param p a pair of values, representing (in the order) a minimum and a maximum
     * @param v a new (minimum/maximum) value
     */
    private static void set_Min_Max(Pair<Integer, Integer> p, int v) {
        if (p.getKey() > v) 
            p.setKey(v);
        if (p.getValue() < v) 
            p.setValue(v);
    }

    /**
     * compare <code>this</code> equality to another considering the variables indices first
     * then the successors (the color and the sign are ignored, i.e., the equalities
     * are assumed of the same type/color)
     * @param e an equality
     * @return  <code>this</code> -1,0,1 dependening on the comparison outcome
     */
    @Override
    public int compareTo(Equality e) {
       int c = firstIndex().compareTo(e.firstIndex());
       if (c !=0 )
           return c;
       
       if (( c = secondIndex().compareTo(e.secondIndex()) ) != 0)
           return c;
       
       return getSucc().compareTo(e.getSucc());
    }
    
    @Override
    public Guard clone (Domain newdom)  {
        return Equality.builder(getArg1(), getArg2(), sign(), newdom);
    }
    
    /**
     *
     * @param cc
     * @param newdom
     * @return
     */
    @Override
    public Guard copy(ColorClass cc, Domain newdom) {
        return Equality.builder(getArg1().copy(cc), getArg2().copy(cc), sign(), newdom);
    }

}
