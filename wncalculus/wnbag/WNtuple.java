package wnbag;

import java.util.*;
import java.util.Map.Entry;

import classfunction.*;
import color.ColorClass;
import expr.*;
import guard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private static @NotNull Map<? extends ElementaryFunction, Integer> elementaryTerms(@NotNull ColorFunction function) {
        if (function instanceof LinearComb combination) {
            return combination.asMap();
        }
        if (function instanceof ElementaryFunction elementary) {
            return Map.of(elementary, 1);
        }
        throw new IllegalArgumentException("Unsupported linear component: " + function);
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
        if (currentGuard.isTrivial()) return this;

        // This reduction currently supports only one-sorted tuples.
        final ColorClass cc = getSort();
        if (cc == null) return this;

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
                ElementaryFunction ef = lc.asMap().keySet().iterator().next();

                if (ef instanceof Projection p)
                    projection = p;
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
            if (!(clause instanceof Equality eq)) continue;

            Projection guardProj1 = eq.getArg1();
            Projection guardProj2 = eq.getArg2();

            int idx1 = guardProj1.getIndex();
            int idx2 = guardProj2.getIndex();

            Integer pos1 = projToPosMap.get(idx1);
            Integer pos2 = projToPosMap.get(idx2);

            // Both variables must be represented by projection components.
            if (pos1 == null || pos2 == null) continue;

            Projection component1 = projToComponentMap.get(idx1);
            Projection component2 = projToComponentMap.get(idx2);

            // If y_pos = succ^a(x_idx), then
            // succ^u(x_idx) = succ^(u-a)(y_pos).
            Projection filterProj1 = Projection.builder(pos1, guardProj1.getSucc() - component1.getSucc(), cc);
            Projection filterProj2 = Projection.builder(pos2, guardProj2.getSucc() - component2.getSucc(), cc);

            // The builder may also return True or False.
            Guard filterClause = Equality.builder(filterProj1, filterProj2, eq.isEquality(), getCodomain());

            clausesToMoveToFilter.add(clause);
            newFilterClauses.add(filterClause);
        }

        if (clausesToMoveToFilter.isEmpty()) return this;

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
        final Set<Guard> remainingGuardArgs = new LinkedHashSet<>(guardClauses);

        remainingGuardArgs.removeAll(clausesToMoveToFilter);

        Guard newGuard;
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
     * as Tuple.baseFilterReduction(...), reusing SetFunction operation where possible.
     *
     * @param equalityMap the filter's precomputed equality map
     * @param membMap the filter's precomputed membership map
     * @return an equivalent reduced tuple, or this if no reduction happened
     */
    private @NotNull ArcFunction baseFilterReduction(@NotNull Map<ColorClass, @NotNull Map<Boolean, SortedSet<Equality>>> equalityMap, @NotNull Map<ColorClass, @NotNull Map<Boolean, Set<Membership>>> membMap) {
        // Assert that the tuple is type-set
        for (var entry : membMap.entrySet()) {
            for (var signed : entry.getValue().entrySet()) {
                for (Membership membership : signed.getValue()) {
                    int position = membership.firstIndex() - 1;
                    var parts = getHomSubTuple(entry.getKey());
                    if (position < 0 || position >= parts.size()) continue;
                    ColorFunction component = parts.get(position);
                    if (!(component instanceof ElementaryFunction || component instanceof LinearComb)) continue;
                    List<FilterTerm> selected = new ArrayList<>();
                    for (var term : elementaryTerms(component).entrySet()) {
                        if (term.getValue() == 0) continue;
                        SetFunction intersection = normalizedIntersection(term.getKey(), membership.getArg2());
                        for (FilterTerm restricted : setIntersectionTerms(term.getKey(), membership.getArg2(), intersection))
                            selected.add(new FilterTerm(restricted.function(), restricted.condition(), Math.multiplyExact(term.getValue(), restricted.coefficient())));
                    }
                    return replaceFilteredComponent(entry.getKey(), position, membership, selected, !signed.getKey());
                }
            }
        }

        for (var entry : equalityMap.entrySet()) {
            ColorClass cc = entry.getKey();
            var parts = getHomSubTuple(cc);
            for (var signed : entry.getValue().entrySet()) {
                for (Equality equality : signed.getValue()) {
                    int i = equality.firstIndex() - 1;
                    int j = equality.secondIndex() - 1;
                    if (i < 0 || j < 0 || i >= parts.size() || j >= parts.size()) continue;
                    if (i == j) continue; // normalized by the guard implementation
                    ElementaryFunction fi = unitSetFunction(parts.get(i));
                    ElementaryFunction fj = unitSetFunction(parts.get(j));
                    if (fi == null || fj == null) continue;
                    if (fi.isFalse() || fj.isFalse()) return getEmpty();
                    int shift = equality.getArg2().getSucc() - equality.getArg1().getSucc();
                    SetFunction shiftedJ = (SetFunction) Successor.factory(shift, fj).normalize();
                    SetFunction intersection = normalizedIntersection(fi, shiftedJ);
                    // Disjointness must be checked even when both cardinalities exceed one.
                    if (intersection.isFalse()) {
                        return signed.getKey() ? getEmpty() : build(rebuildFilterAfterReduction(filter(), List.of(equality)), guard());
                    }
                    if (!fi.cardLeq1() && !fj.cardLeq1()) {
                        if (!signed.getKey() || fi.equals(shiftedJ)) continue;
                        SetFunction shiftedI = (SetFunction) Successor.factory(-shift, fi).normalize();
                        SetFunction intersectionJ = normalizedIntersection(fj, shiftedI);
                        if (intersection.equals(fi) && intersectionJ.equals(fj)) continue;
                        return restrictEqualComponents(cc, i, j, setIntersectionTerms(fi, shiftedJ, intersection), setIntersectionTerms(fj, shiftedI, intersectionJ));
                    }
                    int target;
                    ElementaryFunction original;
                    SetFunction singleton;
                    if (fi.cardLeq1()) {
                        target = j;
                        original = fj;
                        singleton = (SetFunction) Successor.factory(-shift, fi).normalize();
                        intersection = normalizedIntersection(original, singleton);
                    } else {
                        target = i;
                        original = fi;
                        singleton = shiftedJ;
                    }
                    // If the remaining component is a projection, the set rewrite
                    // can represent the difference directly by an input guard.
                    if (!signed.getKey() && original instanceof Projection && singleton instanceof ProjectionBased projectionBased) {
                        SetFunction complement = projectionBased.opposite();
                        return replaceFilteredComponent(cc, target, equality,
                                setIntersectionTerms(original, complement,
                                        normalizedIntersection(original, complement)), false);
                    }
                    // Difference of characteristic functions: A - (A intersect B).
                    // Subtracting B directly would be wrong when B is outside A.
                    return replaceFilteredComponent(cc, target, equality,
                            setIntersectionTerms(original, singleton, intersection), !signed.getKey());
                }
            }
        }
        return this;
    }

    /**
     * Restricts both components while retaining the equality in the output filter.
     *
     * @param cc the color class
     * @param i the index of the first component
     * @param j the index of the second component
     * @param leftTerms the terms for the left component
     * @param rightTerms the terms for the right component
     * @return the restricted arc function
     */
    private @NotNull ArcFunction restrictEqualComponents(@NotNull ColorClass cc, int i, int j, @NotNull List<FilterTerm> leftTerms, @NotNull List<FilterTerm> rightTerms) {
        List<ArcFunction> result = new ArrayList<>();
        for (FilterTerm left : leftTerms) {
            for (FilterTerm right : rightTerms) {
                Guard newGuard = (Guard) And.factory(guard(), left.condition(), right.condition()).normalize();
                if (newGuard.isFalse()) {
                    continue;
                }
                int coefficient = Math.multiplyExact(left.coefficient(), right.coefficient());
                if (coefficient == 0) continue;
                SortedMap<ColorClass, List<? extends ColorFunction>> updated = new TreeMap<>(getHomSubTuples());
                List<ColorFunction> parts = new ArrayList<>(getHomSubTuple(cc));
                parts.set(i, left.function());
                parts.set(j, right.function());
                updated.put(cc, parts);
                WNtuple restricted = new WNtuple(filter(), updated, newGuard);
                result.add(coefficient == 1 ? restricted : new ArcFunScalar(restricted, coefficient));
            }
        }
        return result.isEmpty() ? getEmpty() : ArcFunSum.factory(result);
    }

    /**
     * Returns the elementary function if the given function is an elementary function or a linear combination with a single term; otherwise, returns null.
     *
     * @param function the color function to check
     * @return the elementary function or null
     */
    private @Nullable ElementaryFunction unitSetFunction(@NotNull ColorFunction function) {
        if (function instanceof ElementaryFunction elementary) return elementary;
        if (function instanceof LinearComb combination) {
            if (combination.asMap().isEmpty()) return Empty.getInstance(function.getSort());
            if (combination.size() == 1) {
                var term = combination.asMap().entrySet().iterator().next();
                if (term.getValue() == 1) return term.getKey();
            }
        }
        return null;
    }

    /**
     * Computes the normalized intersection of two set functions.
     *
     * @param left  the left set function
     * @param right the right set function
     * @return the normalized intersection of the two set functions
     */
    private @NotNull SetFunction normalizedIntersection(@NotNull SetFunction left, @NotNull SetFunction right) {
        return (SetFunction) Intersection.factory(left, right).normalize();
    }

    private record FilterTerm(ElementaryFunction function, Guard condition, int coefficient) { }

    /** Reuses the set implementation, including its projection-to-guard rewrite.
     *
     *  @param left the left set function
     *  @param right the right set function
     *  @param intersection the intersection of the two set functions
     *  @return the list of filter terms
     */
    private @NotNull List<FilterTerm> setIntersectionTerms(@NotNull SetFunction left, @NotNull SetFunction right, @NotNull SetFunction intersection) {
        if (intersection.isFalse()) return List.of();
        if (intersection instanceof ElementaryFunction elementary) {
            return List.of(new FilterTerm(elementary, True.getInstance(getDomain()), 1));
        }
        if (intersection instanceof Intersection product
                && product.getArgs().stream().anyMatch(Projection.class::isInstance)) {
            var simple = product.toSimpleFunctions(Collections.emptySet(),
                    Collections.emptyMap(), Collections.emptyMap(), getDomain());
            List<FilterTerm> result = new ArrayList<>();
            for (var term : simple) {
                if (!(term.getKey() instanceof ElementaryFunction elementary)) {
                    throw new IllegalStateException("Non-elementary projection intersection: " + term);
                }
                result.add(new FilterTerm(elementary, term.getValue(), 1));
            }
            if (result.isEmpty()) throw new IllegalStateException("Untranslated intersection: " + product);
            return result;
        }
        // A complement is the signed characteristic function All - projection.
        // This also covers a singleton subclass intersected with a complement.
        if (left instanceof ProjectionComp complement) {
            List<FilterTerm> result = new ArrayList<>();
            if (!(right instanceof ElementaryFunction elementary)) {
                throw new IllegalStateException("Non-elementary type-set operand: " + right);
            }
            if (elementary instanceof ProjectionComp other) {
                result.add(new FilterTerm(All.getInstance(other.getSort()), True.getInstance(getDomain()), 1));
                result.add(new FilterTerm(other.getArg(), True.getInstance(getDomain()), -1));
            } else {
                result.add(new FilterTerm(elementary, True.getInstance(getDomain()), 1));
            }
            for (FilterTerm term : setIntersectionTerms(complement.getArg(), right,
                    normalizedIntersection(complement.getArg(), right))) {
                result.add(new FilterTerm(term.function(), term.condition(), -term.coefficient()));
            }
            return result;
        }
        if (right instanceof ProjectionComp) return setIntersectionTerms(right, left, intersection);
        throw new IllegalStateException("Unsupported type-set intersection: " + intersection);
    }

    /** Applies one rewrite to the whole tuple: generated conditions belong to its input domain.
     *
     *  @param cc the color class
     *  @param position the position of the component to be replaced
     *  @param removed the guard to be removed
     *  @param intersection the intersection of the guards
     *  @param subtract whether to subtract or add the coefficients
     *  @return the resulting arc function
     */
    private @NotNull ArcFunction replaceFilteredComponent(@NotNull ColorClass cc, int position, @NotNull Guard removed, @NotNull List<FilterTerm> intersection, boolean subtract) {
        Guard newFilter = rebuildFilterAfterReduction(filter(), List.of(removed));
        List<FilterTerm> terms = new ArrayList<>();
        if (subtract) {
            ColorFunction original = getHomSubTuple(cc).get(position);
            for (var term : elementaryTerms(original).entrySet())
                terms.add(new FilterTerm(term.getKey(), True.getInstance(getDomain()), term.getValue()));
        }
        for (FilterTerm term : intersection)
            terms.add(new FilterTerm(term.function(), term.condition(),
                    subtract ? -term.coefficient() : term.coefficient()));

        Map<Guard, Map<ElementaryFunction, Integer>> grouped = new LinkedHashMap<>();
        for (FilterTerm term : terms) {
            Guard condition = (Guard) And.factory(guard(), term.condition()).normalize();
            if (!condition.isFalse())
                grouped.computeIfAbsent(condition, key -> new LinkedHashMap<>())
                        .merge(term.function(), term.coefficient(), Integer::sum);
        }
        List<ArcFunction> result = new ArrayList<>();
        for (var group : grouped.entrySet()) {
            var coefficients = group.getValue();
            coefficients.entrySet().removeIf(term -> term.getValue() == 0 || term.getKey().isFalse());
            if (coefficients.isEmpty()) continue;
            ColorFunction component = coefficients.size() == 1
                    && coefficients.values().iterator().next() == 1
                    ? coefficients.keySet().iterator().next() : new LinearComb(coefficients, false);
            SortedMap<ColorClass, List<? extends ColorFunction>> updated = new TreeMap<>(getHomSubTuples());
            List<ColorFunction> parts = new ArrayList<>(getHomSubTuple(cc));
            parts.set(position, component);
            updated.put(cc, parts);
            result.add(new WNtuple(newFilter, updated, group.getKey()));
        }
        return result.isEmpty() ? getEmpty() : ArcFunSum.factory(result);
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
        return new WNtuple(this.filter(), resultComps, other.guard(), true);
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

    @Override
    public TupleBag getEmpty() {
      return build();
    }

}
