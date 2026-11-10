package wnbag;

import bagexpr.Bag;
import bagexpr.BagComp;
import bagexpr.BagExpr;
import classfunction.ClassFunction;
import classfunction.ElementaryFunction;
import classfunction.Projection;
import color.ColorClass;
import expr.Domain;
import graph.InequalityGraph;
import guard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Pair;
import util.Util;
import wncolorfunction.ColorFunction;
import wncolorfunction.LinearComb;

import java.util.*;
import java.util.Map.Entry;

import static guard.Guard.scaleGuardIndex;


public final class ArcFunComp extends BagComp<WNtuple> implements ArcFunction {
       
    public ArcFunComp(@NotNull ArcFunction l, @NotNull ArcFunction r) {
        super(l, r);
    }

    @Override
    public ArcFunComp buildOp(BagExpr<WNtuple> left, BagExpr<WNtuple> right) {
           return new ArcFunComp((ArcFunction) left, (ArcFunction) right);
    }

    @Override
    public ArcFunction specSimplify() {
        ArcFunction res = (ArcFunction) super.specSimplify();

        if (res instanceof ArcFunComp comp) {
            // caso base: composizione tra WNtuple (tuple di class-functions) dove sx contiene solo LinearCombs
            if (comp.left() instanceof WNtuple left && left.simple()){
                // Preliminary step: we make tuples' inner component exclusively contain constant or non-constant functions
                //we split the tuple so that components are homogeneous (only constants or non-constants)
                List<WNtuple> homogeneousTuples = splitHomogeneousTuples(left);

                if (homogeneousTuples.size() > 1) {
                    return new ArcFunComp(ArcFunSum.factory(homogeneousTuples), comp.right().cast());
                }

                if (!(left.filter().isTrivial())) {
                    //to do
                    return res;
                }

                if (!(comp.right() instanceof WNtuple right))
                    return res;

                if (!left.guard().isTrivial()) { // the left tuple's guard is moved on the right
                    return new ArcFunComp(left.withoutGuard().cast(), right.joinFilter(left.guard()).cast());
                }

                // left tuple, with no constant components
                // Lemma 1
                final Guard rf = right.filter();
                if (! (rf.isTrivial() || rf.isElemAndForm()) )
                    return res;

                int card = 1; // the cardinality of the right-tuple components non-projected
                final Map<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> splitColors = left.splitColors(rf);
                //System.out.println("DEBUG (a):\n"+left+'\n'+splitColors+'\n'+right);
                final Map<ColorClass, ArcFunction> results = new HashMap<>(); // the divided-by color outcomes
                for (Entry<ColorClass, Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>>> cleft : splitColors.entrySet()) {
                    final Pair<List<? extends ColorFunction>, Set<? extends ElementaryGuard>> pair = cleft.getValue();
                    List<? extends ColorFunction> cleftComps = pair.getKey();
                    Set<? extends ElementaryGuard> crightF=  pair.getValue();
                    final ColorClass cc = cleft.getKey();

                    //System.out.println("DEBUG (a):\n"+cleftComps+'\n'+crightF+'\n'+cc+'\n'+right);
                    // Simple-form prerequisite
                    if (!prerequisite(crightF, right, cc)) {
                        return res;
                    }

                    // new Property 2: we extract constant components from the left tuple, if any
                    final Map<Integer, ColorFunction> constants = Util.select(cleftComps, ColorFunction::isConstant);
                    ArcFunction cc_res = property2(cleftComps, crightF, right, constants, cc, card);
                    if (cc_res == null) {
                        return res;
                    }
                    //System.out.println("property 2: " + cc_res);

                    // Property 15
                    ArcFunScalar ccr = (ArcFunScalar) cc_res;
                    card = ccr.k();
                    if (!(ccr.getArg() instanceof ArcFunComp)) {
                        results.put(cc, cc_res);
                        continue;
                    }
                    cc_res = property15((ArcFunComp)ccr.getArg(), card);
                    //System.out.println("property 15: " + cc_res);

                    // Lemma 4
                    ccr = (ArcFunScalar) cc_res;
                    card = ccr.k();
                    cc_res = lemma4((ArcFunComp)ccr.getArg(), card);
                    //System.out.println("lemma 4: " + cc_res);
                    if (cc_res == null) {
                        throw new IllegalStateException("Lemma 4 could not be applied to the composition: " + ccr);
                    }
                    ccr = (ArcFunScalar) cc_res;
                    card = ccr.k();
                    if (card == 0) {
                        results.put(cc, cc_res);
                        continue;
                    }
                    //System.out.println("lemma 4: " + cc_res);
                    // If there is still something in the right filter, it is moved to the left guard position so that it can then be shifted into the left filter.
                    if(!(((WNtuple)(((ArcFunComp)ccr.getArg()).right())).filter().isTrivial())) {
                        WNtuple tleft = (WNtuple) ((WNtuple)(((ArcFunComp)ccr.getArg()).left())).joinGuard(((WNtuple)(((ArcFunComp)ccr.getArg()).right())).filter().cast());
                        tleft=tleft.baseGuardReduction().specSimplify().cast();
                        WNtuple tright = ((WNtuple)(((ArcFunComp)ccr.getArg()).right())).withoutFilter().cast();
                        cc_res = new ArcFunScalar(new ArcFunComp(tleft, tright), card);
                        //System.out.println("after moving right filter to left: " + cc_res);
                    }

                    ccr = (ArcFunScalar) cc_res;
                    card = Objects.requireNonNull(ccr).k();
                    cc_res = composeColorTuples((ArcFunComp)ccr.getArg());

                    cc_res = ArcFunExpansion.factory(cc_res, constants);
                    //System.out.println("DEBUG (c):\n"+ card + " " + cc_res+ "\n") ;
                    results.put(cc, cc_res);
                } // for each color class

                // we build the scalar product of juxtaposition of results
                // System.out.println("DEBUG (b):\n"+results);
                res = new ArcFunScalar(ArcFunJuxt.factory(results, right.guard()), card); // k⋅(Tr∘TX′);
            }
        }
        //System.out.println("(ArcFunComp) simplified to: " + res + ", class: " + res.getClass()); //debug
        return res;
    }

    /**
     * Checks if the prerequisites for applying certain properties are met, based on the guards and the right tuple.
     * @param guards the set of elementary guards
     * @param right the right tuple
     * @param cc the color class
     * @return true if the prerequisites are met, false otherwise
     */
    private boolean prerequisite(@NotNull Set<? extends ElementaryGuard> guards, @NotNull WNtuple right, @NotNull ColorClass cc) {
        for (ElementaryGuard guard : guards) {
            if (guard.isMembership()) {
                return false;
            }
            Equality eq = (Equality) guard;
            var rc1 = right.getComponent(eq.getArg1().getIndex(), cc);
            var rc2 = right.getComponent(eq.getArg2().getIndex(), cc);
            // Continue only if the equality concerns equivalent components in the right tuple and their cardinality is greater than one.
            if (!rc1.equals(rc2) || rc1.cardLb() <= 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Applies property 2 to the given components and guards (cRightF).
     * Removes constant components from the left tuple and adjusts the right tuple accordingly.
     * Projects the right tuple onto the indices of the left tuple and the guards.
     * Rescales the indices of the left tuple and guards if necessary.
     *
     * @param cleftComps the components of the left tuple
     * @param crightF the filter of the right tuple
     * @param right the right tuple
     * @param constants the constant components in the left tuple
     * @param cc the color class of the left tuple
     * @param card the cardinality of the right tuple's non-projected components
     * @return an ArcFunScalar representing the result of applying property 2, or null if the property cannot be applied
     */
    private @Nullable ArcFunScalar property2(@NotNull List<? extends ColorFunction> cleftComps, @NotNull Set<? extends ElementaryGuard> crightF, @NotNull WNtuple right, @NotNull Map<Integer, ColorFunction> constants, @NotNull ColorClass cc, int card){
        final Guard rf = right.filter();
        cleftComps = Util.remove(cleftComps, constants.keySet()); // we remove (in the event) constant components from the left tuple's components
        final SortedSet<Integer> leftindx = new TreeSet<>(ClassFunction.indexSet(cleftComps));
        leftindx.addAll(Guard.indexSet(crightF)); // includes the guard indices too
        final List<? extends ColorFunction> rightComps = right.getHomSubTuple(cc),
                redRightComps = Util.projection(rightComps, leftindx);
        //Guard rightFilter = newGuard(crightF, rtx.getCodomain());
        if (redRightComps != rightComps) { // some components of the right tuple are projected out
            for (int i = 1; i <= rightComps.size(); i++) { // we check the cardinality of the non-projected components of the right tuple
                if (! leftindx.contains(i) ) {
                    final Integer c = rightComps.get(i -1).cardLb();
                    if (c == null)
                        return null; // the cardinality of a non-projected component is not known, so we cannot apply Lemma 1
                    else
                        card *= rightComps.get(i -1).cardLb();
                }
            }
            // we rescale (in the event) rojection indices in the tuple sx
            Map<Integer, Integer> scaled = Util.scaledIndex(leftindx);
            if (leftindx.last() > leftindx.size()) {
                cleftComps = ClassFunction.scaleIndex(cleftComps, scaled);
                crightF = scaleGuardIndex(crightF, scaled, rf.getDomain());
            }
        }

        WNtuple rtx = new WNtuple(redRightComps, right.getDomain());
        Guard rightFilter = newGuard(crightF, rtx.getCodomain());

        // If the left tuple consists only of constants, then I do not perform the composition.
        if (cleftComps.isEmpty()) {
            List<ColorFunction> originalLeftComps = new ArrayList<>(
                    constants.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(Map.Entry::getValue).toList());
            WNtuple leftTuple = new WNtuple(originalLeftComps, right.getDomain());
            // The guard must have the same domain as the left tuple.
            Guard leftGuard = newGuard(crightF, right.getDomain());
            return new ArcFunScalar((ArcFunction) leftTuple.joinGuard(leftGuard), card);
        }

        return new ArcFunScalar(new ArcFunComp(new WNtuple(cleftComps, rightFilter.getDomain()), rtx.joinFilter(rightFilter).cast()), card);

    }

    /**
     * Removes equality clauses from an intermediate filter/guard in a composition expression.
     *
     * @param ccr the ArcFunComp to which property 15 is applied
     * @param card the cardinality of the ArcFunComp
     * @return an ArcFunScalar representing the result of applying property 15
     */
    private @NotNull ArcFunScalar property15(@NotNull ArcFunComp ccr, int card){
        WNtuple rtx = ccr.right().cast();
        Guard rightFilter = rtx.filter();
        var rightComps = rtx.getComponents();

        // Keep only the non-equality guards from the right filter.
        Set<Equality> crightF = new HashSet<>();
        collectIneq(rightFilter, crightF);

        WNtuple left = ccr.left().cast();
        var cleftComps = left.getComponents();

        // Collect the indices of the components occurring in the left tuple.
        final SortedSet<Integer> lidx = new TreeSet<>(ClassFunction.indexSet(cleftComps));

        // Each set represents the indices belonging to an equality class.
        Set<Set<Integer>> eqClass = extractEqualityClasses(rightFilter);
        //System.out.println(eqClass);

        // Include the indices required by the remaining guards.
        lidx.addAll(Guard.indexSet(crightF));
        // Project the right tuple onto the components whose indices are in lidx.
        var redRightComps = Util.projection(rightComps, lidx);

        if (redRightComps != rightComps) { // Some right-tuple components have been removed by the projection.
            for (Set<Integer> eqc : eqClass) {
                // If an equality class has no index among the retained components,
                // its contribution must be accounted for in the cardinality.
                if (Collections.disjoint(eqc, lidx)) {
                    // Use one representative index from the equality class.
                    int i = Collections.min(eqc);

                    // Multiply the cardinality by the lower-bound cardinality of the corresponding component in the original right tuple.
                    card *= rightComps.get(i - 1).cardLb();
                }
            }

            // Compute the new indices after projecting out unused components.
            Map<Integer, Integer> scaled = Util.scaledIndex(lidx);

            // If the retained indices are no longer consecutive starting from 1,
            // update the indices in the left tuple and in the remaining guards.
            if (lidx.last() > lidx.size()) {
                cleftComps = ClassFunction.scaleIndex(cleftComps, scaled);

                crightF = (Set<Equality>) scaleGuardIndex(crightF, scaled, rightFilter.getDomain());
            }
        }

        rtx = new WNtuple(redRightComps, rtx.getDomain());
        rightFilter = newGuard(crightF, rtx.getCodomain());
        var tleft = new WNtuple(cleftComps, rightFilter.getDomain());
        rtx = (WNtuple) rtx.joinFilter(rightFilter);
        return new ArcFunScalar(new ArcFunComp(tleft, rtx), card);
    }

    /**
     * Applies Lemma 4 to the given ArcFunComp and cardinality.
     * Eliminates extra variables from a set of inequality constraints,
     * transforming the combinatorial count of their instantiations into a parametric scalar coefficient.
     *
     * @param comp the ArcFunComp to which Lemma 4 is applied
     * @param card the cardinality of the ArcFunComp
     * @return an ArcFunction representing the result of applying Lemma 4, or null if the conditions for applying Lemma 4 are not met
     */
    private @Nullable ArcFunction lemma4(@NotNull ArcFunComp comp, int card) {
        WNtuple left = comp.left().cast();
        WNtuple right = comp.right().cast();
        Guard rightFilter = right.filter();

        if (rightFilter == null || rightFilter.isTrivial()) {
            return new ArcFunScalar(comp, card);
        }

        var cleftComps = left.getComponents();
        final SortedSet<Integer> leftidx = new TreeSet<>(ClassFunction.indexSet(cleftComps));

        // I gather the members of the filter
        final Set<Equality> ineqs = new HashSet<>();
        collectIneq(rightFilter, ineqs);
        if (ineqs.isEmpty()) {
            return new ArcFunScalar(comp, card);
        }

        // Variables used in the filter
        final SortedSet<Integer> gIdx = new TreeSet<>();
        for (Equality e : ineqs) {
            gIdx.addAll(e.indexSet());
        }

        // Lemma 4 applies only if there are extra variables
        final SortedSet<Integer> extra = new TreeSet<>(gIdx);
        extra.removeAll(leftidx);
        if (extra.isEmpty()) {
            return new ArcFunScalar(comp, card);
        }

        // Lambda: domain available; we use the minimum lb known for the components involved
        final List<? extends ColorFunction> rightComps = right.getComponents();
        int lambda = Integer.MAX_VALUE;
        for (int i : gIdx) {
            if (i < 1 || i > rightComps.size()) {
                return null;
            }
            Integer lb = rightComps.get(i - 1).cardLb();
            if (lb == null) {
                return null; // Cannot reliably estimate the coefficient.
            }
            if (lb < lambda) {
                lambda = lb;
            }
        }

        // Construction of the complete guard graph, and extraction of its connected components
        final InequalityGraph fullGraph = new InequalityGraph(ineqs);
        final Set<HashSet<classfunction.Projection>> ccSet = fullGraph.connectedComponents();

        long kTot = 1L;
        Set<Equality> cRightF = new HashSet<>(); // reduced guard g_X

        for (Set<classfunction.Projection> cc : ccSet) {
            // Inequalities for the current component
            final Set<Equality> compEqs = new HashSet<>();
            for (Equality e : ineqs) {
                if (cc.contains(e.getArg1()) && cc.contains(e.getArg2())) {
                    compEqs.add(e);
                }
            }
            if (compEqs.isEmpty()) {
                continue;
            }

            final InequalityGraph compGraph = new InequalityGraph(compEqs);
            final long pComp = compGraph.chromPolynomial(lambda);

            // Component indices
            final SortedSet<Integer> compIdx = new TreeSet<>();
            for (Equality e : compEqs) {
                compIdx.addAll(e.indexSet());
            }

            final SortedSet<Integer> xComp = new TreeSet<>(compIdx);
            xComp.retainAll(leftidx);

            if (xComp.isEmpty()) {
                // Completely extra block: I multiply directly by P(comp,lambda)
                kTot = Math.multiplyExact(kTot, pComp);
                continue;
            }

            // Mixed block: I apply the relationship with the clique to X_comp
            int s = xComp.size();
            if (lambda < s) {
                return new ArcFunScalar(new ArcFunComp(left, right), 0);
            }

            long edgesInX = 0;
            for (Equality e : compEqs) {
                if (leftidx.containsAll(e.indexSet())) {
                    edgesInX++;
                    cRightF.add(e);// Inequalities between variables in leftidx remain in the reduced filter.
                }
            }
            if (edgesInX != ((long)s * (s - 1)) / 2) {
                return null; // It is not a clique: certain preconditions (reduction) are required
            }

            long pClique = 1L;
            for (int i = 0; i < s; i++) {
                pClique = Math.multiplyExact(pClique, (long)(lambda - i));
            }

            if (pClique == 0L) {
                return new ArcFunScalar(new ArcFunComp(left, right), 0);
            }

            // Indivisible
            if (pComp % pClique != 0L) { // preliminary conditions not met
                return null;
            }

            long kComp = pComp / pClique;
            kTot = Math.multiplyExact(kTot, kComp);
        }

        if (kTot == 0L) {
            return new ArcFunScalar(new ArcFunComp(left, right), 0);
        }

        // Rescaling of indices
        // Collect the indices of the components occurring in the left tuple.
        final SortedSet<Integer> lidx = new TreeSet<>(ClassFunction.indexSet(cleftComps));

        // Include the indices required by the remaining guards.
        lidx.addAll(Guard.indexSet(cRightF));
        // Project the right tuple onto the components whose indices are in lidx.
        var redRightComps = Util.projection(rightComps, lidx);

        if (redRightComps != rightComps) { // Some right-tuple components have been removed by the projection.
            // Compute the new indices after projecting out unused components.
            Map<Integer, Integer> scaled = Util.scaledIndex(lidx);

            // If the retained indices are no longer consecutive starting from 1,
            // update the indices in the left tuple and in the remaining guards.
            if (lidx.last() > lidx.size()) {
                cleftComps = ClassFunction.scaleIndex(cleftComps, scaled);
                cRightF = (Set<Equality>) scaleGuardIndex(cRightF, scaled, rightFilter.getDomain());
            }
        }

        WNtuple rtx = new WNtuple(redRightComps, right.getDomain());
        rightFilter = newGuard(cRightF, rtx.getCodomain());
        WNtuple tleft = new WNtuple(cleftComps, rightFilter.getDomain());
        rtx = (WNtuple) rtx.joinFilter(rightFilter);

        return new ArcFunScalar(new ArcFunComp(tleft, rtx), Math.multiplyExact(card, Math.toIntExact(kTot)));
    }

    /**
     * Collects the inequality clauses from a guard and adds them to the output set.
     *
     * @param g the guard to process
     * @param out the set to which the inequality clauses are added
     */
    private void collectIneq(@NotNull Guard g, @NotNull Set<Equality> out) {
        if (g.isTrivial()) {
            return;
        }
        if (g instanceof And and) {
            for (Guard a : and.getArgs()) {
                collectIneq(a, out);
            }
            return;
        }
        if (g instanceof Equality eq && !eq.sign()) {
            out.add(eq);
        }
    }

    /**
     * Extracts the equality classes from a guard.
     * For an AND guard, groups independent equalities; for a single equality, creates a group containing only that equality.
     *
     * @param rightFilter the guard from which to extract equality classes
     * @return a set of sets, where each inner set contains the indices of components involved in an equality class
     */
    private @NotNull Set<Set<Integer>> extractEqualityClasses(@NotNull Guard rightFilter) {
        // Extract the equalities from the right-side filter.
        // For an AND guard, group independent equalities;
        // for a single equality, create a group containing only that equality.
        final List<List<Equality>> equalities;
        if (rightFilter instanceof And and) {
            equalities = and.separateIndependentEqualities();
        } else if (rightFilter instanceof Equality equality) {
            equalities = List.of(List.of(equality));
        } else {
            equalities = Collections.emptyList();
        }

        final Set<Set<Integer>> eqClass = new HashSet<>();

        // Build the index set associated with each group of equalities.
        for (List<Equality> group : equalities) {
            Set<Integer> indices = new HashSet<>();
            for (Equality eq : group) {
                // Add the indices of both arguments of each equality.
                indices.addAll(eq.indexSet());
            }
            eqClass.add(indices);
        }

        return eqClass;
    }


    /**
     * Checks if the given tuple has any repeated indices in its components.
     *
     * @param tleft the tuple to check
     * @return true if there are repeated indices, false otherwise
     */
    private static boolean isRepeatedIndex(@NotNull WNtuple tleft) {
        for (Set<Integer> positions : tleft.projectionIndexPositions()) {
            if (positions.size() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Splits a tuple into homogeneous tuples based on the types of its components.
     * Each resulting tuple will contain either only constant components or only non-constant components.
     *
     * @param left the tuple to split
     * @return a list of homogeneous tuples
     */
    private @NotNull List<WNtuple> splitHomogeneousTuples(@NotNull WNtuple left) {
        final List<List<ColorFunction>> alternatives = new ArrayList<>();

        for (ColorFunction component : left.getComponents()) {
            if (component instanceof LinearComb lc) {
                final List<ColorFunction> separated = new ArrayList<>();

                for (Bag<?> bag : lc.separateConst()) {
                    separated.add((ColorFunction) bag);
                }

                alternatives.add(separated);

            } else if (component instanceof Projection || component.isConstant()) {
                alternatives.add(List.of(component));

            } else {
                throw new IllegalArgumentException("Unsupported component type: " + component.getClass());
            }
        }

        final Set<Collection<ColorFunction>> combinations = Util.cartesianProd(alternatives);

        final List<WNtuple> result = new ArrayList<>();

        for (Collection<ColorFunction> combination : combinations) {
            result.add(new WNtuple(new ArrayList<>(combination), left.guard(), false));
        }

        return result;
    }



    /**
     * Composes two color tuples, handling cases where the left tuple has repeated indices.
     *
     * @param comp the ArcFunComp containing the two tuples to compose
     * @return the composed ArcFunction
     */
    private @NotNull ArcFunction composeColorTuples(@NotNull ArcFunComp comp) {
        WNtuple tleft = comp.left().cast();
        WNtuple rtx = comp.right().cast();
        if (!isRepeatedIndex(tleft)) {
            return tleft.baseCompose(rtx);
        }

        // If the left tuple has repeated indices, we need to check if any of the components at those indices are LinearComb with more than one term.
        // If so, we split the left tuple and compose each split with the right tuple.
        List<Set<Integer>> idxPos = tleft.projectionIndexPositions();

        for (Set<Integer> idxPo : idxPos) {
            if (idxPo.size() <= 1) {
                continue;
            }

            for (Integer i : idxPo) {
                ColorFunction component = tleft.getComponent(i, tleft.getSort());

                if (component instanceof LinearComb lc && lc.asMap().size() > 1) {
                    return splitAndCompose(tleft, rtx, i);
                }
            }
        }

        return tleft.repetedIndexCompose(rtx);
    }

    /**
     * Splits the left tuple into homogeneous tuples and composes each with the right tuple.
     * @param left the left tuple
     * @param right the right tuple
     * @param i the index of the component to split (1-based index)
     * @return an ArcFunction representing the sum of compositions of each split tuple with the right tuple
     */
    private @NotNull ArcFunction splitAndCompose(@NotNull WNtuple left, @NotNull WNtuple right, int i) {

        List<LinearComb> tupleComps = (List<LinearComb>) Util.cast(left.getComponents(), LinearComb.class);
        ArcFunction result = null;

        for (List<LinearComb> splitComps : splitSum(tupleComps, i)) {
            WNtuple newTuple = new WNtuple(left.filter(), splitComps, left.guard(), false);
            ArcFunction newRes = new ArcFunComp(newTuple, right);
            result = result == null ? newRes : ArcFunSum.factory(result, newRes);
        }

        return Objects.requireNonNull(result);
    }

    /**
     * Splits the LinearComb at position p1 in the list into its constituent ElementaryFunctions, creating new lists for each combination.
     * @param list the original list of LinearComb
     * @param p1 the position of the LinearComb to split (1-based index)
     * @return a set of lists, each containing one of the ElementaryFunctions from the split LinearComb
     */
    private @NotNull Set<List<LinearComb>> splitSum(@NotNull List<LinearComb> list, int p1) {
        Set<List<LinearComb>> result = new HashSet<>();
        var lc = list.get(p1-1);
        for ( Map.Entry<? extends ElementaryFunction, Integer> l : lc.asMap().entrySet()) {
            var newList = new ArrayList<>(list);
            newList.set(p1 - 1, new LinearComb(l.getKey(), l.getValue()));
            result.add(newList);
        }
        return result;
    }

    /**
     * Creates a new Guard from a set of elementary guards and a domain.
     * If the set is empty, returns a trivial true guard for the given domain.
     * Otherwise, constructs an AND guard from the provided elementary guards.
     *
     * @param s the set of elementary guards
     * @param d the domain for the new guard
     * @return a new Guard representing the conjunction of the provided elementary guards, or a trivial true guard if the set is empty
     */
    private @NotNull Guard newGuard(@NotNull Set<? extends ElementaryGuard> s, @NotNull Domain d) {
        if (s.isEmpty())
            return True.getInstance(d);

        return And.factory(NaryGuardOperator.cloneArgs(s, d));
    }
}
