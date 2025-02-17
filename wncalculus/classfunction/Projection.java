package classfunction;

import expr.*;
import java.util.*;
import color.ColorClass;
import guard.Equality;
import util.ComplexKey;
import util.Pair;
import util.Util;

/**
 * this class defines the projection function
 * @author lorenzo capra
 */
public final class Projection extends ElementaryFunction implements ProjectionBased {
    
     private final int index, succ ;//index and successor "argument" of a projection
     private static final Interval CARD =  new Interval(1,1); // the cardinality
     
    /**
     * basic constructor
     * creates a projection with an associated color constraint;
     * @param index the projection index
     * @param exp the successor exponent
     * @param cc the projection color class
     */
    private Projection (int index, int succ, ColorClass cc)  {
        super(cc);    
        this.index  = index;
        this.succ   = succ;
    }
       
    private static final Map<ComplexKey, Projection> VALUES = new HashMap<>();
     
    /**
     * creates a projection (successor) with an associated constraint
     * the successor argument (exp) may have to be simplified so that the following holds:
     * different keys map to different (according to equals) projections
     * @param index the projection index
     * @param succ the successor's argument
     * @param cc the specified color
     * @return a projection (successor) of the specified color class
     */
    public static final Projection builder (int index, int succ, ColorClass cc) {
        if (index < 1) 
            throw new IndexOutOfBoundsException("cannot create a projection with index < 1");
        if (succ != 0 && !cc.isOrdered()) 
            throw new IllegalDomain("cannot build a projection successor in case of unordered color class!");
        if ( cc.hasFixedSize()  ) 
            succ = Util.valueModN(succ , cc.lb()) ;    
        ComplexKey k = new ComplexKey(cc, index, succ);
        Projection p;
        if ( (p = VALUES.get(k)) == null) {
            VALUES.put(k, p = new Projection (index, succ, cc)) ;
            //Util.checkBuilderOneStep(p, VALUES); //debug
        }
        return p;
    }
    
     /**
     * creates a projection with an associated color constraint
     * @param index the projection index
     * @param cc the specified color
     * @return a projection of the specified color class
     */
     public static final Projection builder (int index, ColorClass cc) {
         return Projection.builder(index, 0, cc);
     }
     
    
    @Override
    public final int getIndex() {
        return this.index;
    }

 
    @Override
     public final int getSucc() {
        return this.succ;
    }
     
    @Override
    public Interval card() {
        return CARD;
    }
    
    @Override
    public Pair<SetFunction,Integer> baseCompose(SetFunction right) {
        return new Pair<>(Successor.factory(this.succ, right), null);
    }

    /**
     * 
     * @return a projection with the same color successor exponent as <code>this</code>,
     * and with the new specified index
     */
    @Override
    public Projection setDefaultIndex() {
        return 1 == this.index ? this : Projection.builder(1, this.succ, getSort());
    }
    
    /**
     * @param new_succ the new successor argument
     * @return a projection with the same index and color as <code>this</code>,
     * and the specified successor parameter
     */
    public Projection setExp(int new_succ) {   
        return new_succ == this.succ ? this : Projection.builder(this.index, new_succ, getSort());
    }
    
    /**
     * shift-up the projection of a given parameter
     * @param h a successor parameter
     * @return the projection obtained by shifting <code>this</code> of h positions
     */
    public Projection shift(int h) {
        return setExp(this.succ + h);
    }
       
    /**
     * @return the WN notation X_i^j
     */
    public String toStringWN() {
        return (this.succ==0 ? "" : "!"+ (this.succ != 1 ? "^"+this.succ : ""))+ "X_"+getSort()+'^'+this.index;
    }
    
    /** @return a parser-like notation of this projection */
    @Override
    public String toString() {
        return (this.succ==0 ? "" : "!"+ (this.succ != 1 ? this.succ : ""))+getSort().name().toLowerCase()+'_'+this.index;
    }
    

    @Override
    public ProjectionBased opposite() {
        return ProjectionComp.factory(this);
    }
    
    /**
     * replaces the variable of @code{this} function according to the specified equality
     * assuming that: 1) the color-class is the same, 2) the equality is not "trivial"
     * (the members have different indices)
     * @param eq an equality
     * @return the function corresponding to @code{this}, modulo the replacement induced by the
     * equality; @code{this} is the second index of the equality coincides with @code{this} index
     */
    @Override
    public Projection replace(Equality eq) {
        return eq.sameIndex() || eq.secondIndex() != getIndex() ? this :  builder(eq.firstIndex(), getSucc() - eq.getSucc(), getSort());
    }

    @Override
    public Projection copy(ColorClass s) {
      return Projection.builder(this.index, this.succ, s); 
    }
    
    /**
     * builds on @see succDelim
     * @return the split-delimiter, taking into account the successor
     */
    @Override
    public int splitDelim() {
       return succDelim(this.succ, getSort());
   }
    
   /**
    * @param s a (possibly negative) successor
    * @param c a color class
    * @return the split-delimiter, taking into account the successor and the color-class bounds
    */
    public static int succDelim (int s, ColorClass c) {
       return c.setDelim( Math.max(0, Math.abs(s)- c.lb() + 1) );
   }
   
    /**
     * 
     * @param l a list of projections
     * @param c the list color class 
     * @return the difference between max-succ and min-succS
     */
   public static int maxSuccOffset (Collection<? extends Projection> l, ColorClass c) {
       if ( c.isOrdered() ) {
            int min = 0, max = 0; //matters: we assume 0 present
            for (Projection v : l) {
                 min = Math.min(min, v.succ);
                 max = Math.max(max, v.succ);
             }
             return max -min;
       }
       return 0;
   }
   
    /** 
     * @return  the set of projection indexes occurring on this class-function
     */
     @Override
    public Set<Integer> indexSet() {
        return Collections.singleton(this.index);
     }

}
