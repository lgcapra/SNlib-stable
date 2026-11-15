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
        //System.out.println("specSimplify("+this.toStringDetailed()+")");
        ArcFunction res = super.specSimplify().cast();
        //System.out.println("specSimplify: "+this.toStringDetailed()+" -> "+res.toStringDetailed());
        if (! (res instanceof WNtuple tres))
            return res;

        // Split reduction: if the filter refer to some tuple's positions,
        // we can try to split the tuple at those positions, separately for each color.
        Map<ColorClass, Set<Integer>> targetPositions = filterIdx(tres);
        //System.out.println("\n"+this + "\n" + targetPositions + "\n");
        if (!targetPositions.isEmpty()) {
            ArcFunction splitTuple = tres.splitAtPositions(targetPositions);
            if (splitTuple != tres) {
                //System.out.println("\nSplit tuple: " + splitTuple);
                // If the tuple was split, recursively simplify the resulting sum of tuples.
                return (ArcFunction) splitTuple.specSimplify();
            }
        }

        // Extract scalar coefficients from the tuple components.
        SortedMap<ColorClass, List<? extends ColorFunction>> subtuples = new TreeMap<>();
        int coeff = 1;
        boolean modified = false;

        for (Map.Entry<ColorClass, List<? extends ColorFunction>> entry : tres.getHomSubTuples().entrySet()) {

            ColorClass cc = entry.getKey();
            List<ColorFunction> simplifiedSubtuple = new ArrayList<>();

            for (ColorFunction cf : entry.getValue()) {
                if (cf instanceof LinearComb lc && lc.size() == 1) {
                    ColorFunction simplifiedCf = lc.asMap().keySet().iterator().next().cast();

                    coeff *= lc.asMap().values().iterator().next();
                    simplifiedSubtuple.add(simplifiedCf);
                    modified = true;

                } else if (cf instanceof LinearCombScalar sc) {
                    coeff *= sc.k();
                    simplifiedSubtuple.add(sc.getArg().cast());
                    modified = true;

                } else {
                    simplifiedSubtuple.add(cf);
                }
            }
            subtuples.put(cc, simplifiedSubtuple);
        }

        // If scalar coefficients were extracted, return the simplified tuple.
        if (modified) {
            WNtuple simplifiedTuple = new WNtuple(tres.filter(), subtuples, tres.guard());
            return new ArcFunScalar(simplifiedTuple, coeff);
        }

        // Filter reduction: if the filter is non-trivial, we can try to reduce the tuple by applying the filter's equalities and memberships.
        if (tres.filter().isElemAndForm()) {
            final ArcFunction reduced = tres.baseFilterReduction(tres.filter().equalityMap(), tres.filter().membMap());
            if (reduced != tres) {
                //System.out.println("\nspecSimplify: " + tres.toStringDetailed());
                //System.out.println("specSimplify: reduced by filter to " + reduced.toStringDetailed());
                return reduced;
            }
        }
        return tres;
    }


    /**
     * Returns the set of positions (1-based) mentioned in the filter of the given tuple.
     *
     * @param tres the tuple to analyze
     * @return the map of positions (1-based) mentioned in the filter
     */
    private @NotNull Map<ColorClass, Set<Integer>> filterIdx(@NotNull WNtuple tres) {
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
        }

        // Remove colors for which no position needs to be expanded.
        targetPositions.entrySet().removeIf(entry -> entry.getValue().isEmpty());


        return targetPositions;
    }

    /**
     * splits <tt>this</tt> tuple at the given positions, if possible, and returns a sum of resulting tuples;
     * if no split is possible, returns <tt>this</tt>
     *
     * @param targetPositions the positions at which to split the tuple (1-based)
     * @return a sum of tuples resulting from the split, or <tt>this</tt> if no split is possible
     */
    private @NotNull ArcFunction splitAtPositions(@NotNull Map<ColorClass, Set<Integer>> targetPositions) {

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
    @NotNull WNtuple baseGuardReduction() {
        final Guard currentGuard = guard();

        // If the guard is trivial, there is nothing to reduce.
        if (currentGuard.isTrivial()) {
            return this;
        }

        // This reduction currently supports only one-sorted tuples.
        final ColorClass cc = getSort();
        if (cc == null) {
            return this;
        }

        final List<? extends ColorFunction> comps = getHomSubTuple(cc);

        // Projection index -> representative position in the tuple (1-based).
        final Map<Integer, Integer> projToPosMap = new HashMap<>();

        // Projection index -> projection occurring at the representative position.
        final Map<Integer, Projection> projToComponentMap = new HashMap<>();

        for (int pos = 0; pos < comps.size(); pos++) {
            ColorFunction cf = comps.get(pos);
            Projection projection = null;

            if (cf instanceof Projection p) {
                projection = p;
            } else if (cf instanceof LinearComb lc && lc.size() == 1) {
                ElementaryFunction ef =
                        lc.asMap().keySet().iterator().next();

                if (ef instanceof Projection p) {
                    projection = p;
                }
            }

            if (projection != null) {
                int idx = projection.getIndex();

                // Keep the first occurrence and its associated successor.
                if (!projToPosMap.containsKey(idx)) {
                    projToPosMap.put(idx, pos + 1);
                    projToComponentMap.put(idx, projection);
                }
            }
        }

        // Only extract conjuncts; other guard forms are kept as a whole.
        final Collection<Guard> guardClauses = new LinkedList<>();
        if (currentGuard instanceof And) {
            guardClauses.addAll(And.getArgs(currentGuard));
        } else {
            guardClauses.add(currentGuard);
        }

        final Collection<Guard> clausesToMoveToFilter = new LinkedList<>();
        final Collection<Guard> newFilterClauses = new LinkedList<>();

        for (Guard clause : guardClauses) {
            if (!(clause instanceof Equality eq)) {
                continue;
            }

            Projection guardProj1 = eq.getArg1();
            Projection guardProj2 = eq.getArg2();

            int idx1 = guardProj1.getIndex();
            int idx2 = guardProj2.getIndex();

            Integer pos1 = projToPosMap.get(idx1);
            Integer pos2 = projToPosMap.get(idx2);

            // Both variables must be represented by projection components.
            if (pos1 == null || pos2 == null) {
                continue;
            }

            Projection component1 = projToComponentMap.get(idx1);
            Projection component2 = projToComponentMap.get(idx2);

            // If y_pos = succ^a(x_idx), then
            // succ^u(x_idx) = succ^(u-a)(y_pos).
            Projection filterProj1 = Projection.builder(
                    pos1,
                    guardProj1.getSucc() - component1.getSucc(),
                    cc
            );

            Projection filterProj2 = Projection.builder(
                    pos2,
                    guardProj2.getSucc() - component2.getSucc(),
                    cc
            );

            // The builder may also return True or False.
            Guard filterClause = Equality.builder(
                    filterProj1,
                    filterProj2,
                    eq.isEquality(),
                    getCodomain()
            );

            clausesToMoveToFilter.add(clause);
            newFilterClauses.add(filterClause);
        }

        if (clausesToMoveToFilter.isEmpty()) {
            return this;
        }

        // Combine the existing filter with the transferred clauses.
        // Keeping the existing filter also preserves a possible False filter.
        final Set<Guard> filterArgs = new LinkedHashSet<>();
        Guard currentFilter = filter();

        if (currentFilter instanceof And) {
            filterArgs.addAll(And.getArgs(currentFilter));
        } else {
            filterArgs.add(currentFilter);
        }

        filterArgs.addAll(newFilterClauses);

        Guard newFilter = filterArgs.size() == 1
                ? filterArgs.iterator().next()
                : And.buildAndFormWithDom(filterArgs, getCodomain());

        // Remove the transferred clauses from the guard.
        final Set<Guard> remainingGuardArgs =
                new LinkedHashSet<>(guardClauses);

        remainingGuardArgs.removeAll(clausesToMoveToFilter);

        Guard newGuard;
        if (remainingGuardArgs.isEmpty()) {
            newGuard = guard.True.getInstance(getDomain());
        } else if (remainingGuardArgs.size() == 1) {
            newGuard = remainingGuardArgs.iterator().next();
        } else {
            newGuard = And.buildAndFormWithDom(
                    remainingGuardArgs,
                    getDomain()
            );
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

    private @NotNull ArcFunction baseFilterReduction(@NotNull Map<ColorClass, @NotNull Map<Boolean, SortedSet<Equality>>> equalityMap, @NotNull Map<ColorClass, @NotNull Map<Boolean, Set<Membership>>> membMap) {

        final Collection<Guard> to_remove = new LinkedList<>();
        final SortedMap<ColorClass, List<? extends ColorFunction>> tuple_copy = new TreeMap<>();
        boolean reduced = false;

        // Membership reductions
        for (var mx : membMap.entrySet()) {
            ColorClass cc = mx.getKey();
            List<ColorFunction> h_tuple = new ArrayList<>(getHomSubTuple(cc));

            for (var my : mx.getValue().entrySet()) {
                for (var m : my.getValue()) {
                    int i = m.firstIndex() - 1;

                    if (i >= 0 && i < h_tuple.size()) {
                        ColorFunction oldCf = h_tuple.get(i);

                        if (oldCf instanceof ConstantFunction cf) {
                            ColorFunction newCf = restrictConstantByMembership(cf, m);

                            if (!newCf.equals(oldCf)) {
                                h_tuple.set(i, newCf);
                                reduced = true;
                            }

                            to_remove.add(m);
                        }
                    }
                }
            }

            tuple_copy.put(cc, h_tuple);
        }

        // Equality / inequality reductions
        for (var ex : equalityMap.entrySet()) {
            ColorClass cc = ex.getKey();

            List<ColorFunction> h_tuple = tuple_copy.containsKey(cc) ? new ArrayList<>(tuple_copy.get(cc)) : new ArrayList<>(getHomSubTuple(cc));

            for (var ey : ex.getValue().entrySet()) {
                boolean equality = ey.getKey();

                for (var eq : ey.getValue()) {
                    int i = eq.firstIndex() - 1;
                    int j = eq.secondIndex() - 1;

                    if (i < 0 || j < 0 || i >= h_tuple.size() || j >= h_tuple.size()) {
                        continue;
                    }

                    final ColorFunction f_i = h_tuple.get(i);
                    final ColorFunction f_j = h_tuple.get(j);

                    // Align successor exponents for ordered color classes.
                    int exp_diff = eq.getArg2().getSucc() - eq.getArg1().getSucc();

                    ColorFunction succ_fi = f_i;
                    ColorFunction succ_fj = f_j;

                    if (cc.isOrdered() && exp_diff != 0) {
                        succ_fi = succMulti(-exp_diff, f_i);
                        succ_fj = succMulti(exp_diff, f_j);
                    }

                    boolean fi_card_leq_1 = cardLeq1(f_i);
                    boolean fj_card_leq_1 = cardLeq1(f_j);

                    // Both components have cardinality at most 1.
                    if (fi_card_leq_1 && fj_card_leq_1) {

                        if (f_i.specSimplify().equals(succ_fj.specSimplify())) {
                            if (equality) {// c = c is always true.
                                to_remove.add(eq);
                                reduced = true;
                            } else {// c != c is impossible.
                                return getEmpty().cast();
                            }
                        }
                        if (equality) {
                            // Distinct symbolic singleton components: preserve the filter because their values are unknown.
                            continue;
                        } else {
                            ColorFunction diff_i = differMulti(f_i, succ_fj);
                            h_tuple.set(i, diff_i);
                            to_remove.add(eq);
                            reduced = true;
                        }
                    }

                    if (!fi_card_leq_1 && !fj_card_leq_1) {
                        continue;
                    }

                    // Compute intersection only after handling singleton cases.
                    ColorFunction inter = interMulti(f_i, succ_fj);

                    if (isEmptyMulti(inter)) {
                        if (!equality) {
                            // Disjoint components always satisfy the inequality.
                            to_remove.add(eq);
                            reduced = true;
                        }
                        continue;
                    }

                    if (fi_card_leq_1) {
                        // Restrict the second component.
                        ColorFunction restricted = equality ? interMulti(f_j, succ_fi) : differMulti(f_j, succ_fi);

                        h_tuple.set(j, restricted);

                    } else {
                        // Restrict the first component.
                        ColorFunction restricted = equality ? inter : differMulti(f_i, succ_fj);

                        h_tuple.set(i, restricted);

                    }

                    to_remove.add(eq);
                    reduced = true;
                }
            }

            tuple_copy.put(cc, h_tuple);
        }

        if (!reduced) {
            return this;
        }

        Guard newFilter = rebuildFilterAfterReduction(filter(), to_remove);

        SortedMap<ColorClass, List<? extends ColorFunction>> rebuilt =
                rebuildHomParts(getHomSubTuples(), tuple_copy);

        return new WNtuple(newFilter, rebuilt, guard());
    }


    /**
     * Checks if the given ColorFunction is empty when treated as a multiset.
     * @param f the ColorFunction to check
     * @return true if the ColorFunction is empty as a multiset, false otherwise
     */
    private boolean isEmptyMulti(@NotNull ColorFunction f) {
        return f instanceof LinearComb lc && lc.asMap().isEmpty();
    }

    /**
     * Computes the intersection of two ColorFunctions, treating them as multisets.
     * The intersection is defined as the multiset containing each term present in both functions,
     * with multiplicity equal to the minimum of the multiplicities in the two functions.
     * @param f1 the first ColorFunction
     * @param f2 the second ColorFunction
     * @return the intersection of the two ColorFunctions as a multiset
     */
    private @NotNull ColorFunction interMulti(@NotNull ColorFunction f1, @NotNull ColorFunction f2) {
        if (f1.equals(f2)) {
            return f1;
        }
        if (f1 instanceof All) {
            return f2;
        }
        if (f2 instanceof All) {
            return f1;
        }
        LinearComb lc1 = toLinearComb(f1);
        LinearComb lc2 = toLinearComb(f2);
        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>();
        for (var entry : lc1.asMap().entrySet()) {
            ElementaryFunction key = entry.getKey();
            if (lc2.asMap().containsKey(key)) {
                out.put(key, 1);
            }
        }
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
            return new LinearComb(f1.getSort());
        }
        LinearComb lc1 = toLinearComb(f1);
        LinearComb lc2 = toLinearComb(f2);
        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>(lc1.asMap());
        for (var entry : lc2.asMap().entrySet()) {
            ElementaryFunction key = entry.getKey();
            int newValue = out.getOrDefault(key, 0) - entry.getValue();
            if (newValue == 0) {
                out.remove(key);
            } else {
                out.put(key, newValue);
            }
        }
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
     * Shifts the successor exponent of a ColorFunction by a given amount.
     *
     * @param k the amount to shift the successor exponent
     * @param f the ColorFunction to shift
     * @return a new ColorFunction with shifted successor exponents, or the original function if it is not a LinearComb
     */
    private @NotNull ColorFunction succMulti(int k, @NotNull ColorFunction f) {

        if (k == 0) return f;

        if (f instanceof LinearComb lc) {
            final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>();

            for (var entry : lc.asMap().entrySet()) {
                SetFunction shifted = Successor.factory(k, entry.getKey());

                if (!(shifted instanceof ElementaryFunction elementary)) {
                    throw new IllegalStateException("Successor did not produce an elementary function: " + shifted);
                }

                out.merge(elementary, entry.getValue(), Integer::sum);
            }

            return out.isEmpty() ? new LinearComb(lc.getSort()) : new LinearComb(out, false);
        }

        if (f instanceof ElementaryFunction ef) {
            SetFunction shifted = Successor.factory(k, ef);

            if (shifted instanceof ElementaryFunction elementary) {
                return elementary;
            }

            throw new IllegalStateException("Successor did not produce an elementary function: " + shifted);
        }

        throw new IllegalArgumentException("Unsupported function in succMulti: " + f.getClass().getName());
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
    public WNtuple build(final Guard filter, final SortedMap<ColorClass, List<? extends ColorFunction>> map, final Guard guard) {
        return new WNtuple(filter,map, guard);
    }

    /**
     * base composition between <code>this</code> tuple (assumed single-color, simple, and without constants inside)
     * and the <code>other</code> tuple (assumed entirely projected by <code>this</code>) 
     * @param other the right tuple
     * @return the composed tuple
     */
    @NotNull
    public ArcFunction baseCompose(@NotNull WNtuple other) {
        if (!this.guard().isTrivial()) {
            throw new IllegalStateException("baseCompose requires a trivial guard in the left tuple: " + this.guard());
        }
        if (!other.filter().isTrivial()) {
            throw new IllegalStateException("baseCompose requires a trivial filter in the right tuple: " + other.filter());
        }

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
    @NotNull ArcFunction repetedIndexCompose(@NotNull WNtuple other) {
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
    }

    /**
     * Builds an equality filter for repeated indices based on the position map.
     *
     * @param indexPositions the map returned by projectionIndexPositions()
     * @param filterDomain the domain of the filter
     * @return a Guard representing the equality constraints for repeated indices
     */
    private @NotNull Guard buildRepeatedIndexFilter(@NotNull List<Set<Integer>> indexPositions, @NotNull Domain filterDomain) {

        final ColorClass cc = getSort();
        final List<? extends ColorFunction> comps = getHomSubTuple(cc);
        final Collection<Guard> equalityGuards = new ArrayList<>();

        for (int index = 0; index < indexPositions.size(); index++) {
            Set<Integer> positions = indexPositions.get(index);

            if (positions.size() <= 1) {
                continue;
            }

            // indexPositions uses index - 1 as the list position.
            final int projectionIndex = index + 1;
            Projection firstFilterProjection = null;

            for (int pos : positions) {
                ColorFunction component = comps.get(pos - 1);
                Projection projection = null;

                if (component instanceof Projection p) {
                    projection = p;
                } else if (component instanceof LinearComb lc && lc.size() == 1) {
                    ElementaryFunction term = lc.asMap().keySet().iterator().next();

                    if (term instanceof Projection p) {
                        projection = p;
                    }
                }

                // A single exponent must be identifiable for this position.
                if (projection == null || projection.getIndex() != projectionIndex) {
                    throw new IllegalStateException("Cannot build a repeated-index filter for component " + component + " at position " + pos + ": expected a single projection term");
                }

                // If y_pos = succ^a(x_index), then
                // x_index = succ^-a(y_pos).
                Projection filterProjection = Projection.builder(pos, -projection.getSucc(), cc);

                if (firstFilterProjection == null) {
                    firstFilterProjection = filterProjection;
                } else {
                    equalityGuards.add(Equality.builder(firstFilterProjection, filterProjection, true, filterDomain));
                }
            }
        }

        return equalityGuards.isEmpty() ? True.getInstance(filterDomain) : And.factory(equalityGuards);
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
    @NotNull List<Set<Integer>> projectionIndexPositions() {
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

    private @NotNull ColorFunction restrictConstantByMembership(@NotNull ConstantFunction cf, @NotNull Membership m) {

        ColorClass cc = cf.getSort();

        // The empty multiset remains empty.
        switch (cf) {
            case Empty empty -> {
                return Empty.getInstance(cc);
            }

            // Restrict the universal multiset.
            case All all -> {
                if (m.sign()) {
                    // Positive membership: keep only the selected subclass.
                    return m.getArg2();
                }

                // Negative membership: sum all subclasses except the selected one.
                Map<ElementaryFunction, Integer> terms = new LinkedHashMap<>();
                for (int i = 1; i <= cc.subclasses(); i++) {
                    if (i != m.getArg2().index()) {
                        terms.put(Subcl.factory(i, cc), 1);
                    }
                }
                return terms.isEmpty() ? new LinearComb(cc) : new LinearComb(terms, false);
            }

            // Restrict a multiset already belonging to one subclass.
            case Subcl sc -> {
                boolean belongsToSelectedSubclass =
                        sc.index() == m.getArg2().index();

                boolean keep = m.sign() == belongsToSelectedSubclass;

                return keep ? sc : Empty.getInstance(cc);
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Unsupported constant function: " + cf.getClass().getSimpleName());
    }

    @Override
    public TupleBag getEmpty() {
      return build();
    }

}
