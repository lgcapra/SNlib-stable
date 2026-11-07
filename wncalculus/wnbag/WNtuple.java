package wnbag;

import java.util.*;
import java.util.Map.Entry;

import classfunction.*;
import color.ColorClass;
import expr.*;
import guard.*;
import org.jetbrains.annotations.NotNull;
import tuple.AbstractTuple;
import util.Pair;
import util.Util;
import wncolorfunction.ColorFunction;
import wncolorfunction.LinearComb;
import wncolorfunction.LinearCombScalar;

/**
 * this class defines generic SN function-tuples, composed of linear combinations of elementary SN class-functions
 * (this implementation is not complete)
 * @author lorenzo capra
 */
public final class WNtuple extends AbstractTuple<ColorFunction>  implements ArcFunction {
    
    //cache fields
    private Boolean simple = null, 
                    elementary = null;
    private Integer card = null;
  

    /**
     * base constructor: creates a <tt>WNtuple</tt> from a list of linear comb. of class-functions
     * @param f the tuple's filter
     * @param l the tuple's components
     * @param g the tuple's guard
     * @param check domain-check flag
     */
    public WNtuple(Guard f, List<? extends ColorFunction> l, Guard g, boolean check) {
        super(f, l, g, check);
    }
    
    /**
     * creates a <tt>WNtuple</tt> with a deafult filter from a list of linear comb. of class-functions
     * @param l the tuple's components
     * @param g the tuple's guard
     * @param check domain-check flag
     */
    public WNtuple(List<? extends ColorFunction> l, final Guard g, boolean check) {
        super(l, g, check);
    }
    
    /**
     * efficiently creates a <tt>WNtuple</tt> from a map of colors to class-functions;
     * no check is done
     * @param f the tuple's filter
     * @param m a (sorted) map of colors to corresponding lists of functions
     * @param g the tuple's guard
     */
    public WNtuple(Guard f, SortedMap<ColorClass, List <? extends ColorFunction>> m, Guard g) {
        super(f, m, g, false);
    }
    
    /**
     * creates an ordinary <tt>WNtuple</tt> from a map of colors to class-functions;
     * no check is done
     * @param m a (sorted) map of colors to corresponding lists of functions
     * @param g the tuple's guard
     */
    public WNtuple(SortedMap<ColorClass, List <? extends ColorFunction>> m, Guard g) {
        super(m, g, false);
    }

    public WNtuple(final List<? extends ColorFunction> args, final Domain d) {
        super(args, d, true);
    }

    public WNtuple(Domain dom, ColorFunction ... comps) {
        this(Arrays.asList(comps), dom);
    }

    public WNtuple(final Guard g, ColorFunction ... comps) {
        this(Arrays.asList(comps), g, false);
    }

    @Override
    public WNtuple build(Guard filter, Guard guard) {
        return new WNtuple(filter, getHomSubTuples(), guard);
    }

    /**
     * 
     * @return <code>true</code> if and only if each component is a <code>LinearComb</code> or
     *         an <code>ElementaryFunction</code>
     */
    public boolean simple() {
        if (elementaryTuple()) {
            return true;
        }
        if (simple == null) {
          simple = Util.checkAll(getComponents(), c ->
                  c instanceof LinearComb || c instanceof ElementaryFunction);
        }
        return simple;
    }

    /**   
     * @return <code>true</code> if and only if each component is an <code>ElementaryFunction</code>
     */
    public boolean elementaryTuple() {
        if (elementary == null) {
          elementary = Util.checkAll(getComponents(), c -> c instanceof ElementaryFunction);
        }

        return elementary;
    }


    @Override
    public Integer cardLb() {
        if (card == null) {
            card = 1;
            for (ColorFunction o : getComponents()) {
                Integer lb = o.cardLb();
                if (lb == null)
                    return lb;
                card *= lb;
            }
        }

        return card;
    }

    
    /**
     * separates <tt>this</tt> tuple into its independent sub-tuples, given a partition of variable indices
     * modeling the independent parts of the associated guard (if any)
     * should be invoked on (single-color) tuples whose components hold at most one (projection) index 
     * (even if it works more generally)
     * @param connected the (possibly empty) pre-calculated set of connected components of tuple's guard
     * @return a map whose keys are sets of independent variable indices and whose values are corresponding
     * sub-tuples of <tt>this</tt> tuple, computed according the method's parameter;
     * each independent sub-tuple is expressed in turn as a map between tuple's positions (starting from 0) and components
     */
    public @NotNull Map<Set<Integer>, LinkedHashMap<Integer,ColorFunction> > independentComponents (@NotNull Set<? extends Set<Integer>> connected) {
    	List<? extends ColorFunction> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashMap<Integer, ColorFunction>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashMap<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			ColorFunction l = components.get(i);
			Set<Integer> indexSet = l.indexSet();
			LinkedHashMap<Integer, ColorFunction> m = imap.get( indexSet ); //optimization
			if (m == null)
				for (Entry<Set<Integer>, LinkedHashMap<Integer, ColorFunction>> entry :  imap.entrySet() )
					if (entry.getKey().containsAll( indexSet ) ) { // indexset should be a singleton or empty
                                            m = entry.getValue();
                                            break;
		    		}
			if ( m ==  null ) 
				imap.put(l.indexSet(), m = new LinkedHashMap<>());
			m.put(i, l);
		}
			
    	return imap;
    }
    
    /**
     * overloaded version of <tt>independentComponents</tt>, which assumes <tt>this</tt> tuple single-color,
     * and with and and-type guard
     * @return a map whose keys are sets of independent variable indices and whose values are corresponding
     */
    public Map<Set<Integer>, LinkedHashMap<Integer,ColorFunction> > independentComponents () {
    	Guard g = guard();
    	return independentComponents( !g.isTrivial()  ? ((And) g).igraph().get( getSort()).connectedIndices(): Collections.emptySet());
    }
    
    /*
     * 2nd version of independentComponents, which only returns the positions of independent subtuple elements
     */
    public @NotNull Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 (@NotNull Set<? extends Set<Integer>> connected) {
    	List<? extends ColorFunction> components = getHomSubTuple(getSort());
    	Map<Set<Integer>, LinkedHashSet<Integer>>  imap = new HashMap<>();
    	for (Set<Integer> c : connected) 
    		imap.put(c, new LinkedHashSet<>()); //imap is initialized according to connected partition
		for (int i=0, tsize = components.size() ; i< tsize ; ++i) {
			ColorFunction l = components.get(i);
			Set<Integer> indexSet = l.indexSet();
			LinkedHashSet<Integer> m = imap.get( indexSet ); //optimization
			if (m == null)
				for (Entry<Set<Integer>, LinkedHashSet<Integer>> entry :  imap.entrySet() ) 
					if (entry.getKey().containsAll( indexSet ) ) { // indexset should be a singleton or empty
		    			 m = entry.getValue();
		    			 break;
		    		}
			if ( m ==  null ) 
				imap.put(l.indexSet(), m = new LinkedHashSet<>());
			
			m.add(i);
		}
			
    	return imap;
    }
    
    /**
     * overloaded version of <tt>independentComponentsV2</tt>, which assumes <tt>this</tt> tuple single-color,
     * and with and and-type guard
     * @return a map whose keys are sets of independent variable indices and whose values are corresponding
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 () {
    	Guard g = guard();
    	return independentComponentsV2(!g.isTrivial() ? ((And) g).igraph().get( getSort()).connectedIndices(): Collections.emptySet());
    }

    @Override
    public ArcFunction specSimplify() {
        //System.out.println("specSimplify("+this.toStringDetailed()+")"); //debug
        ArcFunction res = super.specSimplify().cast();
        //System.out.println("specSimplify: "+this.toStringDetailed()+" -> "+res.toStringDetailed()); //debug
        if (! (res instanceof WNtuple tres))
            return res;

        // Split reduction: if the filter or the guard refer to some tuple's positions,
        // we can try to split the tuple at those positions, separately for each color.
        Map<ColorClass, Set<Integer>> targetPositions = GuardAndFilterIdx(tres);
        //System.out.println("\n"+this + "\n" + targetPositions + "\n");
        if (!targetPositions.isEmpty()) {
            ArcFunction splitTuple = tres.splitAtPositions(targetPositions);
            if (splitTuple != tres) {
                //System.out.println("\nSplit tuple: " + splitTuple);
                // If the tuple was split, we recursively simplify the resulting sum of tuples
                return (ArcFunction) splitTuple.specSimplify();
            }
        }

        // Filter reduction: if the filter is non-trivial, we can try to reduce the tuple by applying the filter's equalities and memberships
        if (tres.filter().isElemAndForm()) {
            final ArcFunction reduced = tres.baseFilterReduction(tres.filter().equalityMap(), tres.filter().membMap());
            if (reduced != tres) {
//                System.out.println("\nspecSimplify: "+tres.toStringDetailed()); //debug
//                System.out.println("specSimplify: reduced by filter to "+reduced.toStringDetailed());
                return reduced;
            }
        }

        // for each color class, simplify the corresponding subtuple
        SortedMap<ColorClass, List<? extends ColorFunction>> subtuples = new TreeMap<>();
        int coeff = 1;
        var modified = false;
        for (Map.Entry<ColorClass, List<? extends ColorFunction>> entry : tres.getHomSubTuples().entrySet()) {
            final ColorClass cc = entry.getKey();
            final List<? extends ColorFunction> subtuple = entry.getValue();
            final List<ColorFunction> simplifiedSubtuple = new ArrayList<>();
            for (ColorFunction cf : subtuple) {
                if (cf instanceof LinearComb lc && lc.size() == 1) {
                    ColorFunction simplifiedCf = lc.asMap().keySet().iterator().next().cast();
                    coeff *= lc.asMap().values().iterator().next();
                    simplifiedSubtuple.add(simplifiedCf);
                    modified = true;
                } else if (cf instanceof LinearCombScalar sc) {
                    coeff *= sc.k();
                    simplifiedSubtuple.add(sc.getArg().cast());
                    modified = true;
                }
                else {
                    simplifiedSubtuple.add(cf);
                }
            }
            subtuples.put(cc, simplifiedSubtuple);
        }
        //if (modified) System.out.println("simplified Sub Tuple: "+ new ArcFunScalar(new WNtuple(tres.filter(), subtuples, tres.guard()), coeff));
        return !modified ? tres : new ArcFunScalar(new WNtuple(tres.filter(), subtuples, tres.guard()), coeff);
    }

    /**
     * Returns the set of positions (1-based) mentioned in the guard and filter of the given tuple.
     *
     * @param tres the tuple to analyze
     * @return the map of positions (1-based) mentioned in the guard and filter
     */
    private @NotNull Map<ColorClass, Set<Integer>> GuardAndFilterIdx(@NotNull WNtuple tres) {
        Map<ColorClass, Set<Integer>> targetPositions = new HashMap<>();

        // Collect positions referenced by the filter.
        if (tres.filter().isElemAndForm()) {
            var equalityMap = tres.filter().equalityMap();
            var membMap = tres.filter().membMap();

            for (ColorClass cc : equalityMap.keySet()) {
                Map<Boolean, SortedSet<Equality>> equalities = equalityMap.get(cc);
                if (equalities != null) {
                    for (SortedSet<Equality> set : equalities.values()) {
                        for (Equality eq : set) {
                            targetPositions.computeIfAbsent(cc, k -> new HashSet<>()).add(eq.firstIndex());
                            targetPositions.computeIfAbsent(cc, k -> new HashSet<>()).add(eq.secondIndex());
                        }
                    }
                }
            }

            for (ColorClass cc : membMap.keySet()) {
                Map<Boolean, Set<Membership>> memberships = membMap.get(cc);
                if (memberships != null) {
                    for (Set<Membership> set : memberships.values()) {
                        for (Membership mb : set) {
                            targetPositions.computeIfAbsent(cc, k -> new HashSet<>()).add(mb.firstIndex());
                        }
                    }
                }
            }
        }

        // Split the tuple and its guard by color.
        Map<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> splitMap = tres.splitColors(tres.guard());

        for (var entry : splitMap.entrySet()) {
            ColorClass cc = entry.getKey();

            List<? extends ColorFunction> components = entry.getValue().getKey();
            Set<? extends ElementaryGuard> guards = entry.getValue().getValue();

            Set<Integer> guardIndices = new HashSet<>();

            // Collect projection indices used by the guards of this color.
            for (ElementaryGuard guard : guards) {
                collectGuardIndices(guard, guardIndices);
            }

            // Convert projection indices into positions within this color.
            for (int pos = 0; pos < components.size(); pos++) {
                ColorFunction component = components.get(pos);

                if (!Collections.disjoint(component.indexSet(), guardIndices)) {
                    targetPositions.computeIfAbsent(cc, k -> new HashSet<>()).add(pos + 1);
                }
            }
        }

        // Remove colors for which no position needs to be expanded.
        targetPositions.entrySet().removeIf(entry -> entry.getValue().isEmpty());


        return targetPositions;
    }

    /**
     * Collects projection indices occurring in a guard.
     */
    private void collectGuardIndices(@NotNull Guard guard, @NotNull Set<Integer> indices) {
        switch (guard) {
            case And and -> {
                for (Guard clause : and.getArgs()) {
                    collectGuardIndices(clause, indices);
                }
            }
            case Equality eq -> {
                indices.add(eq.firstIndex());
                indices.add(eq.secondIndex());
            }
            case Membership mb -> indices.add(mb.firstIndex());
            default -> {
            }
        }
    }

    /**
     * splits <tt>this</tt> tuple at the given positions, if possible, and returns a sum of resulting tuples;
     * if no split is possible, returns <tt>this</tt>
     *
     * @param targetPositions the positions at which to split the tuple (1-based)
     * @return a sum of tuples resulting from the split, or <tt>this</tt> if no split is possible
     */
    public @NotNull ArcFunction splitAtPositions(@NotNull Map<ColorClass, Set<Integer>> targetPositions) {

        // Retrieve all tuple components, preserving their original order.
        List<? extends ColorFunction> comps = getComponents();

        // For each component, store the possible alternatives obtained by expanding
        // its linear combination. Each alternative is paired with its coefficient.
        List<Set<Map.Entry<ColorFunction, Integer>>> choices = new ArrayList<>();
        boolean expandable = false;

        // Keep track of the current position within each color class.
        // Positions are counted independently for each color class.
        Map<ColorClass, Integer> colorPositions = new HashMap<>();

        for (ColorFunction cf : comps) {
            ColorClass cc = cf.getSort();
            int pos = colorPositions.merge(cc, 1, Integer::sum);

            Set<Map.Entry<ColorFunction, Integer>> alternatives = new HashSet<>();

            // Expand the component only if:
            // - its color class has target positions;
            // - its position within that color class is among the targets;
            // - it is a linear combination containing more than one term.
            if (targetPositions.containsKey(cc)
                    && targetPositions.get(cc).contains(pos)
                    && cf instanceof LinearComb lc
                    && lc.size() > 1) {

                // Create one alternative for each term of the linear combination,
                // preserving the coefficient associated with that term.
                for (var entry : lc.asMap().entrySet()) {
                    alternatives.add(new AbstractMap.SimpleEntry<>(new LinearComb(entry.getKey()), entry.getValue()));
                }

                expandable = true;

            } else {
                // Keep the original component unchanged, with coefficient 1.
                alternatives.add(new AbstractMap.SimpleEntry<>(cf, 1));
            }

            choices.add(alternatives);
        }

        // If no component can be expanded, return the original tuple unchanged.
        if (!expandable) {
            return this;
        }

        List<ArcFunction> tupleAddends = new ArrayList<>();

        // Generate all possible combinations of component alternatives using the Cartesian product of the previously collected choices.
        for (Collection<Map.Entry<ColorFunction, Integer>> prod : Util.cartesianProd(choices)) {

            List<ColorFunction> newComps = new ArrayList<>(comps.size());
            int mult = 1;

            // Reconstruct the tuple using the selected alternative for each component.
            // The coefficient of the resulting tuple is the product of the coefficients of its selected component alternatives.
            for (Map.Entry<ColorFunction, Integer> choice : prod) {
                newComps.add(choice.getKey());
                mult *= choice.getValue();
            }

            // Build a new tuple preserving the original filter and guard.
            WNtuple t = new WNtuple(filter(), newComps, guard(), false);

            // If the resulting coefficient differs from 1, represent it by wrapping the tuple in an ArcFunScalar.
            tupleAddends.add(mult != 1 ? new ArcFunScalar(t, mult) : t);
        }

        // Combine all generated tuples into a sum of arc functions.
        return ArcFunSum.factory(tupleAddends);
    }

    /**
     * Move the guard clauses (Guard) to act as a filter (Filter) for the tuple
     * if all variables involved in the clauses appear as projections
     * within the components of the tuple.
     *
     * @return a new WNtuple with the updated filter and the reduced guard,
     * or 'this' if no transformation has been applied.
     */
    public @NotNull WNtuple baseGuardReduction() {
        Guard currentGuard = guard();

        // If the guard is trivial, there's nothing to reduce
        if (currentGuard.isTrivial()) {
            return this;
        }

        // Map from projection indices to their corresponding positions in the tuple (1-based)
        // Es: <c1, c3, c2> -> {1: pos 1, 3: pos 2, 2: pos 3}
        final ColorClass cc = getSort();
        if (cc == null) {
            return this; // this is not a 1-sorted tuple: this global expansion cannot be performed
        }

        final List<? extends ColorFunction> comps = getHomSubTuple(cc);
        final Map<Integer, Integer> projToPosMap = new HashMap<>();

        for (int pos = 0; pos < comps.size(); pos++) {
            ColorFunction cf = comps.get(pos);
            if (cf instanceof Projection p) {
                projToPosMap.put(p.getIndex(), pos + 1);
            } else if (cf instanceof LinearComb lc && lc.size() == 1) {
                ElementaryFunction ef = lc.asMap().keySet().iterator().next();
                if (ef instanceof Projection p) {
                    projToPosMap.put(p.getIndex(), pos + 1);
                }
            }
        }

        // Extract the guard clauses (And)
        final Collection<Guard> guardClauses = (Collection<Guard>) And.getArgs(currentGuard);
        final Collection<Guard> clausesToMoveToFilter = new LinkedList<>();
        final Collection<Guard> newFilterClauses = new LinkedList<>();

        for (Guard clause : guardClauses) {
            if (clause instanceof Equality eq) {
                int idx1 = eq.getArg1().getIndex();
                int idx2 = eq.getArg2().getIndex();

                // Check if both indices are present in the projection-to-position map
                Integer pos1 = projToPosMap.get(idx1);
                Integer pos2 = projToPosMap.get(idx2);

                if (pos1 != null && pos2 != null) {
                    // Create a new Equality clause for the filter using the positions instead of the original indices
                    Projection filterProj1 = Projection.builder(pos1, cc);
                    Projection filterProj2 = Projection.builder(pos2, cc);

                    Equality filterEq = (Equality) Equality.builder(filterProj1, filterProj2, eq.isEquality(), getCodomain());

                    clausesToMoveToFilter.add(eq);
                    newFilterClauses.add(filterEq);
                }
            }
        }

        // If no clause has been moved, return the tuple unchanged
        if (clausesToMoveToFilter.isEmpty()) {
            return this;
        }

        // Rebuild the new filter by combining the existing filter with the new filter clauses
        Guard newFilter = filter();
        if (!newFilterClauses.isEmpty()) {
            final Set<Guard> args = new LinkedHashSet<>();

            if (!newFilter.isTrivial()) {
                if (newFilter instanceof ElementaryGuard || newFilter instanceof And) {
                    args.addAll(And.getArgs(newFilter));
                } else {
                    args.add(newFilter);
                }
            }

            args.addAll(newFilterClauses);
            newFilter = args.size() == 1 ? args.iterator().next()
                       : And.buildAndFormWithDom(args, getCodomain());
        }

        // Rebuild the new guard by removing the moved clauses from the original guard
        Guard newGuard;
        final Set<Guard> remainingGuardArgs = new LinkedHashSet<>(guardClauses);
        clausesToMoveToFilter.forEach(remainingGuardArgs::remove);

        if (remainingGuardArgs.isEmpty()) {
            newGuard = guard.True.getInstance(getDomain());
        } else if (remainingGuardArgs.size() == 1) {
            newGuard = remainingGuardArgs.iterator().next();
        } else {
            newGuard = And.buildAndFormWithDom(remainingGuardArgs, getDomain());
        }

        return new WNtuple(newFilter, getHomSubTuples(), newGuard);
    }

    /**
     * Performs the tuple's filter reduction for WNtuple, following the same logic
     * as Tuple.baseFilterReduction(...), but using multiset operations instead of
     * set operations.
     *
     * @param equalityMap the filter's precomputed equality map
     * @param membMap    the filter's precomputed membership map
     * @return an equivalent reduced tuple, or this if no reduction happened
     */
    public @NotNull WNtuple baseFilterReduction(@NotNull Map<ColorClass, @NotNull Map<Boolean, SortedSet<Equality>>> equalityMap, @NotNull Map<ColorClass, @NotNull Map<Boolean, Set<Membership>>> membMap) {
        final Collection<Guard> to_remove = new LinkedList<>();
        final SortedMap<ColorClass, List<? extends ColorFunction>> tuple_copy = new TreeMap<>();
        boolean reduced = false;

        // membership reductions
        for (var mx : membMap.entrySet()) {
            ColorClass cc = mx.getKey();
            List<ColorFunction> h_tuple = new ArrayList<>(getHomSubTuple(cc));
            for (var my : mx.getValue().entrySet()) {
                for (var m : my.getValue()) {
                    int i = m.firstIndex() - 1;
                    if (i >= 0 && i < h_tuple.size()) {
                        ColorFunction oldCf = h_tuple.get(i);
                        ColorFunction constraint = multisetConstraint(m);
                        ColorFunction newCf = my.getKey() ? interMulti(oldCf, constraint) : differMulti(oldCf, constraint);

                        if (!newCf.equals(oldCf)) {
                            h_tuple.set(i, newCf);
                            reduced = true;
                        }
                        to_remove.add(m);
                    }
                }
            }
            tuple_copy.put(cc, h_tuple);
        }

        // equality/inequality reductions
        for (var ex : equalityMap.entrySet()) {
            ColorClass cc = ex.getKey();
            List<ColorFunction> h_tuple = (List<ColorFunction>) tuple_copy.get(cc);
            if (h_tuple == null) {
                h_tuple = new ArrayList<>(getHomSubTuple(cc));
            }

            for (var ey : ex.getValue().entrySet()) {
                boolean op = ey.getKey();
                for (var eq : ey.getValue()) {
                    int i = eq.firstIndex() - 1;
                    int j = eq.secondIndex() - 1;

                    if (i < 0 || j < 0 || i >= h_tuple.size() || j >= h_tuple.size()) {
                        continue;
                    }

                    final ColorFunction f_i = h_tuple.get(i);
                    final ColorFunction f_j = h_tuple.get(j);
                    ColorFunction succ_fi = f_i;
                    ColorFunction succ_fj = f_j;

                    int exp_diff = eq.getArg2().getSucc() - eq.getArg1().getSucc();
                    if (cc.isOrdered() && exp_diff != 0) {
                        succ_fi = succMulti(-exp_diff, f_i);
                        succ_fj = succMulti(exp_diff, f_j);
                    }

                    if (op) {
                        // Equality
                        boolean fi_card_leq_1 = cardLeq1(f_i);
                        boolean fj_card_leq_1 = cardLeq1(f_j);

                        if (fi_card_leq_1 || fj_card_leq_1) {
                            if (fi_card_leq_1 && isSetFunction(f_j)) {
                                // S_C2 at position j restricted to the pointwise projection f_i
                                h_tuple.set(j, succ_fi);
                                to_remove.add(eq);
                                reduced = true;
                            } else if (fj_card_leq_1 && isSetFunction(f_i)) {
                                // S_C2 at position i restricted to the pointwise projection f_j
                                h_tuple.set(i, succ_fj);
                                to_remove.add(eq);
                                reduced = true;
                            } else if (fi_card_leq_1 && fj_card_leq_1) {
                                // Either both are singletons or both are projections; if they are different projections, we can substitute one with the other
                                if (f_i instanceof Projection p1 && f_j instanceof Projection p2 && p1.getIndex() != p2.getIndex()) {
                                    Projection target = Projection.builder(p1.getIndex(), cc);
                                    for (int k = 0; k < h_tuple.size(); k++) {
                                        h_tuple.set(k, substituteProjection(h_tuple.get(k), p2.getIndex(), target));
                                    }
                                    to_remove.add(eq);
                                    reduced = true;
                                } else if (f_i.equals(succ_fj)) {
                                    to_remove.add(eq); // Tautology: c_i = c_i
                                    reduced = true;
                                }
                            }
                        }
                    } else {
                        // Inequality
                        if (f_i.equals(f_j) && cardLeq1(f_i)) {
                            // If both components are equal and have cardinality <= 1, the inequality is unsatisfiable
                            return build().cast();
                        }
                        // If one component is a set function and the other has cardinality <= 1, we can restrict the set function by removing the singleton
                        if (isSetFunction(f_i) && cardLeq1(f_j)) {
                            ColorFunction restricted = differMulti(f_i, succ_fj);
                            if (!restricted.equals(f_i)) {
                                h_tuple.set(i, restricted);
                                to_remove.add(eq);
                                reduced = true;
                            }
                        } else if (isSetFunction(f_j) && cardLeq1(f_i)) {
                            ColorFunction restricted = differMulti(f_j, succ_fi);
                            if (!restricted.equals(f_j)) {
                                h_tuple.set(j, restricted);
                                to_remove.add(eq);
                                reduced = true;
                            }
                        }
                    }
                }
            }

            tuple_copy.put(cc, h_tuple);
        }

        if (!reduced) {
            return this;
        }

        Guard newFilter = rebuildFilterAfterReduction(filter(), to_remove);
        SortedMap<ColorClass, List<? extends ColorFunction>> rebuilt = rebuildHomParts(getHomSubTuples(), tuple_copy);

        return new WNtuple(newFilter, rebuilt, guard());
    }

    // Helper for identifying set functions S_C
    private boolean isSetFunction(@NotNull ColorFunction cf) {
        return switch (cf) {
            case SetFunction setFunction -> true;
            case LinearComb lc -> lc.asMap().keySet().stream().anyMatch(Objects::nonNull);
            default -> false;
        };
    }

    /**
     * Sostituisce ricorsivamente un indice di proiezione target con la nuova proiezione all'interno di una ColorFunction.
     */
    private @NotNull ColorFunction substituteProjection(@NotNull ColorFunction cf, int targetIndex, @NotNull Projection replacement) {
        if (cf instanceof Projection p) {
            if (p.getIndex() == targetIndex) {
                return replacement;
            }
            return p;
        }
        if (cf instanceof LinearComb lc) {
            final Map<ElementaryFunction, Integer> newMap = new LinkedHashMap<>();
            boolean changed = false;
            for (var entry : lc.asMap().entrySet()) {
                ElementaryFunction elem = entry.getKey();
                if (elem instanceof Projection p && p.getIndex() == targetIndex) {
                    newMap.put(replacement, entry.getValue());
                    changed = true;
                } else {
                    newMap.put(elem, entry.getValue());
                }
            }
            return changed ? new LinearComb(newMap, false) : lc;
        }
        return cf;
    }

    /**
     * For a membership clause m = X_i in S (or !in S), the corresponding multiset
     * restriction is just the intersection with the subclass support.
     */
    private @NotNull ColorFunction multisetConstraint(@NotNull Membership m) {
        // In this library a Membership is a guard on a projection; for a multiset
        // component the effect is just a restriction of the support.
        // We keep it conservative and return the component unchanged when the
        // restriction is not explicit at this level.
        return new LinearComb(m.getSort());
    }

    /**
     * Computes the intersection of two ColorFunctions, treating them as multisets.
     * The intersection is defined as the multiset containing each term present in both functions,
     * with multiplicity equal to the minimum multiplicity of that term in the two functions.
     *
     * @param f1 the first ColorFunction
     * @param f2 the second ColorFunction
     * @return a new ColorFunction representing the intersection of f1 and f2 as multisets
     */
    private @NotNull ColorFunction interMulti(@NotNull ColorFunction f1, @NotNull ColorFunction f2) {
        if (f1.equals(f2)) {
            return f1;
        }

        // Converted to LinearComb for easier multiset operations
        LinearComb lc1 = toLinearComb(f1);
        LinearComb lc2 = toLinearComb(f2);

        // We compute the intersection of the two multisets by taking the minimum multiplicity for each term present in both
        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>();
        for (var entry : lc1.asMap().entrySet()) {
            final ElementaryFunction key = entry.getKey();
            final Integer v2 = lc2.asMap().get(key); // Get the multiplicity of the same term in the second multiset
            if (v2 != null) {
                // Take the minimum multiplicity between the two multisets for this term
                final int min = Math.min(entry.getValue(), v2);
                if (min > 0) {
                    out.put(key, min);
                }
            }
        }

        //System.out.println("interMulti: " + f1 + " inter " + f2 + " = " + out); // debug
        // If the intersection is empty, return an empty LinearComb (the null multiset)
        return out.isEmpty() ? new LinearComb(f1.getSort()) : new LinearComb(out, false);
    }

    /**
     * Computes the difference of two ColorFunctions, treating them as multisets.
     * The difference is defined as the multiset containing each term present in the first function,
     * with multiplicity equal to the multiplicity in the first function minus the multiplicity in the second function.
     * If a term's multiplicity becomes zero or negative, it is removed from the resulting multiset.
     *
     * @param f1 the first ColorFunction
     * @param f2 the second ColorFunction
     * @return a new ColorFunction representing the difference of f1 and f2 as multisets
     */
    private @NotNull ColorFunction differMulti(@NotNull ColorFunction f1, @NotNull ColorFunction f2) {
        if (f1.equals(f2)) {
            return new LinearComb(f1.getSort()); // f1 - f1 = 0 (empty multiset)
        }

        LinearComb lc1 = toLinearComb(f1);
        LinearComb lc2 = toLinearComb(f2);

        // We compute the difference of the two multisets by subtracting the multiplicity of each term in the second multiset from that in the first.
        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>(lc1.asMap());

        // For each term in the second multiset, subtract its multiplicity from the first.
        for (var e : lc2.asMap().entrySet()) {
            final int newVal = out.getOrDefault(e.getKey(), 0) - e.getValue();
            if (newVal == 0) {
                out.remove(e.getKey()); // If the new multiplicity is zero, remove the term from the result
            } else {
                out.put(e.getKey(), newVal); // Otherwise, update the multiplicity in the result
            }
        }

        //System.out.println("differMulti: " + f1 + " differ " + f2 + " = " + out); // debug
        return out.isEmpty() ? new LinearComb(f1.getSort()) : new LinearComb(out, false);
    }

    /**
     * Converts a ColorFunction to a LinearComb representation.
     * If the ColorFunction is already a LinearComb, it returns it as is.
     * If it is an ElementaryFunction, it creates a LinearComb with that function and multiplicity 1.
     * If it is neither, it creates an empty LinearComb with the same sort as the original ColorFunction.
     *
     * @param cf the ColorFunction to convert
     * @return the LinearComb representation of the ColorFunction
     */
    private @NotNull LinearComb toLinearComb(@NotNull ColorFunction cf) {
        if (cf instanceof LinearComb lc) {
            return lc;
        }
        // Convert the single elementary function to a LinearComb with multiplicity 1
        if (cf instanceof ElementaryFunction ef) {
            return new LinearComb(ef);
        }
        return new LinearComb(cf.getSort());
    }

    /**
     * Shifts the successor exponent of each elementary function in the multiset by k.
     * If the function is not a LinearComb, it returns the function unchanged.
     *
     * @param k the amount to shift the successor exponent
     * @param f the ColorFunction to shift
     * @return a new ColorFunction with shifted successor exponents, or the original function if it is not a LinearComb
     */
    private @NotNull ColorFunction succMulti(int k, @NotNull ColorFunction f) {
        if (k == 0) {
            return f; // Offset 0: no change needed
        }

        if (!(f instanceof LinearComb lc)) {
            return f;
        }

        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>();
        for (var entry : lc.asMap().entrySet()) {
            final SetFunction x = entry.getKey();
            final SetFunction shifted;

            // Shift the successor exponent based on the type of the elementary function
            if (x instanceof Projection p) {
                shifted = p.setExp(p.getSucc() + k);
            } else if (x instanceof Successor s) {
                shifted = Successor.factory(k + s.getExp(), s.getArg());
            } else {
                // For other types of elementary functions, we create a new Successor with the shifted exponent
                shifted = Successor.factory(k, x);
            }

            // If the shifted function is an ElementaryFunction, we add it to the output map with the same multiplicity
            if (shifted instanceof ElementaryFunction ef) {
                out.merge(ef, entry.getValue(), Integer::sum);
            }
        }

        return out.isEmpty() ? new LinearComb(f.getSort()) : new LinearComb(out, false);
    }

    /**
     * Cardinality <= 1 test for multiset components.
     */
    private boolean cardLeq1(@NotNull ColorFunction f) {
        switch (f) {
            // If the function is a Projection, it is considered to have cardinality <= 1
            case Projection projection -> {
                return true;
            }
            // If the function is a LinearComb, we check its map representation
            case LinearComb lc -> {
                var map = lc.asMap();
                if (map.isEmpty()) {
                    return true; // empty: <= 1
                }
                if(map.size() == 1) {
                    var entry = map.entrySet().iterator().next();
                    // If the multiplicity is 1 and the key has cardinality <= 1, then the whole function has cardinality <= 1
                    return (entry.getValue() == 1 && cardLeq1(entry.getKey()));
                }
            }
            default -> {
                return false;
            }
        }
        return false;
    }

    @Override
    public WNtuple build(final Guard filter, final SortedMap<ColorClass, List<? extends ColorFunction>> map,
            final Guard guard) {
        return new WNtuple(filter,map, guard);
    }

    /**
     * base composition between <code>this</code> tuple (assumed single-color, simple, and without constants inside)
     * and the <code>other</code> tuple (assumed entirely projected by <code>this</code>) 
     * @param other the right tuple
     * @return the composed tuple
     */
    public @NotNull ArcFunction baseCompose(@NotNull WNtuple other) {
        //System.out.println("Base Compose: " + this + " . " + other);
        final List<? extends ColorFunction> leftComps = getHomSubTuple(getSort());
        final List<? extends ColorFunction> rightComps = other.getHomSubTuple(other.getSort());

        // Projection index -> component of the right tuple
        final Map<Integer, ColorFunction> subst = new HashMap<>();
        for (int i = 0; i < rightComps.size(); i++) {
            subst.put(i + 1, rightComps.get(i));
        }

        final List<ColorFunction> resultComps = new ArrayList<>(leftComps.size());

        for (ColorFunction leftCf : leftComps) {
            final Map<ElementaryFunction, Integer> newMap = new HashMap<>();

            if (leftCf instanceof LinearComb lc) {
                for (Map.Entry<? extends ElementaryFunction, Integer> e : lc.asMap().entrySet()) {
                    final ElementaryFunction f = e.getKey();
                    final int mult = e.getValue();

                    if (f instanceof Projection p) {
                        composeProjection(p, subst, newMap, mult);
                    } else {
                        newMap.merge(f, mult, Integer::sum);
                    }
                }

                if (newMap.isEmpty()) {
                    resultComps.add(new LinearComb(lc.getSort()));
                } else {
                    resultComps.add(new LinearComb(newMap, false));
                }

            } else if (leftCf instanceof Projection p) {
                composeProjection(p, subst, newMap, 1);

                if (newMap.isEmpty()) {
                    resultComps.add(new LinearComb(p.getSort()));
                } else if (newMap.size() == 1 && newMap.values().iterator().next() == 1) {
                    resultComps.add(newMap.keySet().iterator().next());
                } else {
                    resultComps.add(new LinearComb(newMap, false));
                }

            } else {
                throw new IllegalStateException("Unsupported component in baseCompose: " + leftCf.getClass().getName() + " -> " + leftCf);
            }
        }

        //System.out.println("Compose Result: " + resultComps);
        return new WNtuple(resultComps, other.guard(), true);
    }

    /**
     * Composes a projection with the right tuple's components based on the substitution map.
     *
     * @param p the projection to compose
     * @param subst the substitution map from projection indices to right tuple components
     * @param result the map to accumulate the resulting elementary functions and their multiplicities
     * @param multiplicity the multiplicity of the projection in the left tuple
     */
    private void composeProjection(@NotNull Projection p, @NotNull Map<Integer, ColorFunction> subst, @NotNull Map<ElementaryFunction, Integer> result, int multiplicity) {

        ColorFunction replacement = subst.get(p.getIndex());

        switch (replacement) {
            case null -> throw new IllegalStateException("Missing substitution for projection " + p);
            case LinearComb lc -> {
                for (var entry : lc.asMap().entrySet()) {
                    Pair<SetFunction, Integer> composition =
                            p.baseCompose(entry.getKey());

                    SetFunction composed = composition.getKey();

                    if (!(composed instanceof ElementaryFunction elementary)) {
                        throw new IllegalStateException("Composition did not produce an ElementaryFunction: " + composed);
                    }

                    result.merge(elementary, multiplicity * entry.getValue(), Integer::sum);
                }
            }
            case ElementaryFunction elementary -> {
                Pair<SetFunction, Integer> composition = p.baseCompose(elementary);

                SetFunction composed = composition.getKey();

                if (!(composed instanceof ElementaryFunction composedElementary)) {
                    throw new IllegalStateException("Composition did not produce an ElementaryFunction: " + composed);
                }

                result.merge(composedElementary, multiplicity, Integer::sum);
            }
            default -> throw new IllegalStateException("Unsupported replacement for projection " + p + ": " + replacement.getClass().getName());
        }

    }

    /**
     * Performs the composition of this tuple with another tuple, taking into account repeated indices.
     * If the result of the basic composition is a WNtuple, it applies a filter to enforce equality constraints
     * for repeated indices based on the positions of the components in this tuple.
     *
     * @param other the right tuple to compose with
     * @return the composed ArcFunction, potentially with a filter for repeated indices
     */
    public @NotNull ArcFunction repetedIndexCompose(@NotNull WNtuple other) {

        //System.out.println("\n\n " + this + " " + other + "\n\n");
        // First, perform the basic composition
        ArcFunction composed = baseCompose(other);

        // If the result is not a WNtuple, return it as is
        if (!(composed instanceof WNtuple wnTuple)) {
            return composed;
        }

        // Get the repeated index positions from the left tuple
        List<Set<Integer>> indexPositions = projectionIndexPositions();

        // Build the repeated index filter using the RESULT tuple's CODOMAIN (which is the filter's domain)
        Guard repeatedIndexFilter = buildRepeatedIndexFilter(indexPositions, wnTuple.getCodomain());

        // If the filter is trivial (True), return the result as is
        if (repeatedIndexFilter.isTrivial()) {
            return composed;
        }

        //WNtuple out = wnTuple.build(repeatedIndexFilter, wnTuple.guard());
        return wnTuple.joinFilter(repeatedIndexFilter).<WNtuple>cast();

        // Create a new WNtuple with the new filter (replacing the old one)
//        return wnTuple.build(repeatedIndexFilter, wnTuple.guard());
    }

    /**
     * Builds an equality filter for repeated indices based on the position map.
     *
     * @param indexPositions the map returned by projectionIndexPositions()
     * @param filterDomain the domain of the filter
     * @return a Guard representing the equality constraints for repeated indices
     */
    public @NotNull Guard buildRepeatedIndexFilter(List<Set<Integer>> indexPositions, @NotNull Domain filterDomain) {
        final ColorClass cc = getSort();
        final Collection<Guard> equalityGuards = new ArrayList<>();

        // For each index that appears in multiple positions
        for (Set<Integer> positions : indexPositions) {

            // If index appears more than once, create equality guards
            if (positions.size() > 1) {
                final List<Integer> posList = new ArrayList<>(positions);

                // Create equality between the first position and all other positions
                final int firstPos = posList.getFirst();
                final Projection firstProj = Projection.builder(firstPos, cc);

                for (int i = 1; i < posList.size(); i++) {
                    final int pos = posList.get(i);
                    final Projection otherProj = Projection.builder(pos, cc);

                    // Add equality: component at firstPos == component at pos
                    equalityGuards.add(Equality.builder(firstProj, otherProj, true, filterDomain));
                }
            }
        }
        return equalityGuards.isEmpty() ?
                guard.True.getInstance(filterDomain) :
                guard.And.factory(equalityGuards);
    }

    /**
     * Returns the positions of the tuple components grouped by projection index.
     * The result is a list where the element at position {@code i - 1}
     * contains the positions of the tuple components that contain projection
     * index {@code i}.
     * <p>
     * Positions are 1-based.
     *
     * @return a list containing, for each projection index, the positions
     *         of the tuple components containing that index
     */
    public @NotNull List<Set<Integer>> projectionIndexPositions() {
        final ColorClass cc = getSort();
        if (cc == null) {
            throw new IllegalStateException("projectionIndexPositions() requires a single-color tuple");
        }

        final List<? extends ColorFunction> comps = getHomSubTuple(cc);
        final List<Set<Integer>> result = new ArrayList<>();

        for (int pos = 0; pos < comps.size(); pos++) {
            final ColorFunction cf = comps.get(pos);
            final Set<Integer> idxs = cf.indexSet();

            for (int idx : idxs) {
                while (result.size() < idx) {
                    result.add(new LinkedHashSet<>());
                }

                result.get(idx - 1).add(pos + 1);
            }
        }

        return result;
    }

    @Override
    public TupleBag getEmpty() {
      return build();
    }

}
