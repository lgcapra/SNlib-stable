package wncalculus.expr;

/**
  this class defines possibly unbounded positive integer intervals;
  the "empty" interval is [0,0]
  intervals are implemented as "data-objects"
 */
  public final class Interval implements Comparable<Interval> {
        private final int lb, ub;//interval bounds
        
     /**
     * creates a bounded interval
     * @param lb the interval's lower bound
     * @param ub the interval's upper bound
     */    
        public Interval (int lb, int ub) {
           if (lb < 0) 
                throw new IllegalArgumentException("negative lb!");
           
           if (lb > ub ) 
                throw new IllegalArgumentException("incorrect bounds: " + lb +','+ub);
           
           this.lb=lb;
           this.ub=ub;
        }
        
        /** creates a superiorly unbounded interval
         * @param lb the interval's lower bound
         */
        public Interval (int lb) {
           if (lb < 0) 
            throw new IllegalArgumentException("incorrect interval bounds!");
           
           this.lb = lb;
           this.ub = -1; 
        }
        
        
        /** creates the "empty" interval [0,0] */
        public Interval () {
            this.lb = this.ub = 0;
        }
        
        //accessor methods

    /**
     *
     * @return the interval's lower bound
     */
        public int lb () {
            return this.lb;
        }
        
    /**
     *
     * @return the interval's upper bound (-1 if the interval is unbounded)
     */
    public int ub () {
            return this.ub ;
        }
        
        /**
         * @return <code>true</code> if and only the interval is unbounded 
         */
        public boolean unbounded () {
            return this.ub == -1;
        }
              
        /**
         * @return a non-null <code>Integer</code> if and only the interval is size-1
         */
        public Integer singleValue () {
            return this.lb == this.ub ? this.lb : null;
        }
        
        /**
         * @param k a value
         * @return <code>true</code> if and only the interval is composed of the given element
         */
        public boolean singleValue (int k) {
            return this.lb == this.ub && this.lb == k;
        }
        
        /**
         * 
         * @return <code>true</code> if and only if is single-valueS
         */
        public boolean singleton() {
            return this.lb == this.ub;
        }
        
        /**
         * @return the size of the interval; -1 is the interval is unbounded 
         */
        public int size () {
            return unbounded() ? -1 : this.ub - this.lb +1 ;
        }
        
        @Override
        public boolean equals (Object other) {
            Interval two;
            
            return other instanceof Interval && this.lb == (two=(Interval)other).lb && this.ub == two.ub;
        }
        
        /**
         * checks whether this contains 
         * @param other another interval
         * @return <code>true</code> if and only if <code>this</code> includes other
         */
        public boolean contains (Interval other) {
            return  lb <= other.lb && (ub == -1 || other.ub!= -1 && ub >= other.ub ) ;
        }

        /**
         * performs the "and" (intersection) between this and other intervals;
         * @param other the interval to be intersected with this
         * @return the (possibly null) "and" between this and interval
         */
        public Interval intersect (Interval other) {
            if (! unbounded() && this.ub < other.lb  || !other.unbounded() && other.ub < this.lb()) 
                return null;
            
            int newlb = Math.max(this.lb, other.lb);
            if ( unbounded() && other.unbounded()) 
                return new Interval(newlb);
            
            return new Interval(newlb, this.unbounded() ? other.ub : (other.unbounded() ? this.ub : Math.min(this.ub, other.ub)));
        }
        
    /**
     * performs the bound-wise sum of this and other intervals
     * @param other the interval to be summed up with this
     * @return the bound-wise sum between this and interval
     */
    public Interval sum (Interval other) {
        return unbounded() || other.unbounded() ? new Interval(lb + other.lb) : new Interval(lb + other.lb, ub + other.ub);
    }
                
    /**
     *
     * @param delim the split delimiter, i.e., an offset from the interval lower bound
     * @return a size-two array of intervals resulting from split; size-zero array,
     * if no split is done (e.g., if lb &gt; delim)
     * @throws IllegalArgumentException is the argument is negative
     */
    public Interval[] split (final int delim) {
        if (delim < 0)
            throw new IllegalArgumentException("negative offset");

        final int nb = delim+this.lb;
        if (delim == 0 || this.lb == this.ub || this.ub > 0 && this.ub < nb  ) 
            return new Interval[0];
        // lb < nb <= ub
        return new Interval[]{new Interval(this.lb, nb-1), unbounded() ? new Interval(nb) : new Interval(nb, this.ub)};
    }
        
    @Override
        public String toString() {
            return "["+lb+','+(ub==-1 ? '\u221E' : ""+ub)+']';
        }
        
    /**
     * overloaded version of toString that can be used to obtain an output consistent
     * with the parser
     * @param color the color class name
     * @return a string like |C| = k, or k_1 &le; |C| &le; k_2, or k_1 &le; |C| &le; n 
     */
    public String toString( String color) {
            String card = '|' + color + '|';
            
            return this.lb == this.ub ? card + " = " + this.lb : this.lb + " <= "+card + (this.unbounded() ? "<= n" : "<= "+this.ub);
        }
    
    @Override
        public int hashCode() {
            return 11*this.lb + 7*this.ub;
        }
        
        /**
         * try to merge <tt>this</tt> interval with another, for example: [2,3].merge ([4,*]) &rarr; [2,*]
         * in that order
         * @param other the interval to be merged with this
         * @return the interval resulting from merging, or <code>null</code>
         * if the intervals are not "adjacent"
         */
        public Interval merge (Interval other) {
            if (other.lb == this.ub + 1) 
                  return other.unbounded() ? new Interval(this.lb) : new Interval(this.lb, other.ub);
            
            return null;
        }

    @Override
    /**
     * makes a comparison between intervals based first on the lower bound then
     * on the upper bound
     */
    public int compareTo(Interval o) {
        Integer b = this.lb;
        int cmp;
        if ((cmp = b.compareTo(o.lb) ) != 0)
            return cmp;
        
        if (this.ub == o.ub)
            return 0;
        
        if ( unbounded() )
            return 1;
        
        return o.unbounded() ? -1 : this.ub < o.ub ? -1 : 1;
    }
    
    public boolean fit(final int offset) {
        return unbounded() || offset + this.lb  <= this.ub;
    }
  
  }
