package wnbag;

import java.util.*;
import java.util.Map.Entry;

import classfunction.*;
import color.ColorClass;
import expr.*;
import guard.*;
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
    @SuppressWarnings("unchecked")
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
                  LinearComb.class.isInstance(c) || ElementaryFunction.class.isInstance(c));
        }
        return simple;
    }

    /**   
     * @return <code>true</code> if and only if each component is an <code>ElementaryFunction</code>
     */
    public boolean elementaryTuple() {
        if (elementary == null) {
          elementary = Util.checkAll(getComponents(), c -> ElementaryFunction.class.isInstance(c));
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
    public Map<Set<Integer>, LinkedHashMap<Integer,ColorFunction> > independentComponents (Set<? extends Set<Integer>> connected) {
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
     * @return
     */
    public Map<Set<Integer>, LinkedHashMap<Integer,ColorFunction> > independentComponents () {
    	Guard g = guard();
    	return independentComponents( !g.isTrivial()  ? ((And) g).igraph().get( getSort()).connectedIndices(): Collections.emptySet());
    }
    
    /*
     * 2nd version of independentComponents, which only returns the positions of independent subtuple elements
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 (Set<? extends Set<Integer>> connected) {
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
     * @return
     */
    public Map<Set<Integer>, LinkedHashSet<Integer>> independentComponentsV2 () {
    	Guard g = guard();
    	return independentComponentsV2(!g.isTrivial() ? ((And) g).igraph().get( getSort()).connectedIndices(): Collections.emptySet());
    }
    
    /**
     * if <code>this</code> tuple contains non trivial linear combinations, expand the tuple into a corresponding TupleBag
     * @return the bag corresponding to the expansion of inner linear combinations
     */
    public ArcFunction expand() {
        ColorClass cc = getSort();
        if (cc == null) {
            return this;
        }

        List<? extends ColorFunction> comps = getComponents();
        List<Set<Map.Entry<ColorFunction, Integer>>> choices = new ArrayList<>();
        boolean expandable = false;

        for (ColorFunction cf : comps) {
            Set<Map.Entry<ColorFunction, Integer>> c = expandChoices(cf);
            if (c.size() > 1) {
                expandable = true;
            }
            choices.add(c);
        }

        if (!expandable) {
            return this;
        }

        HashMap<WNtuple, Integer> result = new HashMap<>();

        for (Collection<Map.Entry<ColorFunction, Integer>> prod : Util.cartesianProd(choices)) {
            List<ColorFunction> newComps = new ArrayList<>(comps.size());
            int mult = 1;

            for (Map.Entry<ColorFunction, Integer> choice : prod) {
                newComps.add(choice.getKey());
                mult *= choice.getValue();
            }

            WNtuple t = new WNtuple(Util.singleSortedMap(cc, newComps), guard());
            result.merge(t, mult, Integer::sum);
        }

        return new TupleBag(result);
    }

    private Set<Map.Entry<ColorFunction, Integer>> expandChoices(ColorFunction cf) {
        if (cf instanceof LinearComb lc && lc.size() > 1) {
            Set<Map.Entry<ColorFunction, Integer>> s = new HashSet<>();
            lc.asMap().forEach((f, k) -> s.add(new AbstractMap.SimpleEntry<>(new LinearComb(f), k)));
            return s;
        }

        return Collections.singleton(new AbstractMap.SimpleEntry<>(cf, 1));
    }

    @Override
    public ArcFunction specSimplify() {
        //System.out.println("specSimplify("+this.toStringDetailed()+")"); //debug
        ArcFunction res = super.specSimplify().cast();
        //System.out.println("specSimplify: "+this.toStringDetailed()+" -> "+res.toStringDetailed()); //debug
        if (! (res instanceof WNtuple tres))
            return res;

        // if the tuple can be split into a sum of tuples, do it
        ArcFunction split = tres.toEquivSimpleSum();
        if (split != tres) {
            return split;
        }

        // Filter reduction: if the filter is non-trivial, we can try to reduce the tuple by applying the filter's equalities and memberships
        if (tres.filter().isElemAndForm()) {
            final var equalityMap = tres.filter().equalityMap();
            final var membMap = tres.filter().membMap();
            final ArcFunction reduced = tres.baseFilterReduction(equalityMap, membMap);
            if (reduced != tres) {
//                System.out.println("specSimplify: "+tres.toStringDetailed()); //debug
//                System.out.println("specSimplify: reduced by filter to "+reduced.toStringDetailed());
                return reduced;
            }
        }

        // Guard reduction: if the guard is non-trivial, we can try to switch some guard clauses to the filter if all variables involved in the guard clauses are projected in the tuple's components
        if (tres.guard().isElemAndForm()) {
            final ArcFunction reduced = tres.baseGuardReduction();
            if (reduced != tres) {
//                System.out.println("specSimplify: "+tres.toStringDetailed()); //debug
//                System.out.println("specSimplify: reduced by guard to "+reduced.toStringDetailed());
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
        return coeff == 1 || !modified ? tres : new ArcFunScalar(new WNtuple(tres.filter(), subtuples, tres.guard()), coeff);
    }

    /**
     * Expands each component that is a non-trivial LinearComb into a sum of tuples.
     * This is the multiset analogue of Tuple.toEquivSimpleSum().
     *
     * If a component is a LinearComb with support size > 1, the tuple is rewritten
     * as a sum of tuples, each selecting one term of the LinearComb.
     */
    public ArcFunction toEquivSimpleSum() {
        final ColorClass cc = getSort();
        if (cc == null) {
            return this; // this is not a 1-sorted tuple: this global expansion cannot be performed
        }
        final List<? extends ColorFunction> comps = getComponents();
        final List<List<Map.Entry<ColorFunction, Integer>>> choices = new ArrayList<>();
        boolean expandable = false;

        for (ColorFunction cf : comps) {
            if (cf instanceof LinearComb lc && lc.size() > 1) {
                final List<Map.Entry<ColorFunction, Integer>> c = new ArrayList<>();
                lc.asMap().forEach((f, k) -> c.add(new AbstractMap.SimpleEntry<>(new LinearComb(f), k)));
                choices.add(c);
                expandable = true;
            } else {
                final List<Map.Entry<ColorFunction, Integer>> c = new ArrayList<>();
                c.add(new AbstractMap.SimpleEntry<>(cf, 1));
                choices.add(c);
            }
        }

        if (!expandable) {
            return this;
        }

        final Map<WNtuple, Integer> result = new HashMap<>();

        for (Collection<Map.Entry<ColorFunction, Integer>> prod : Util.cartesianProd(choices)) {
            final List<ColorFunction> newComps = new ArrayList<>(comps.size());
            int mult = 1;

            for (Map.Entry<ColorFunction, Integer> choice : prod) {
                newComps.add(choice.getKey());
                mult *= choice.getValue();
            }

            final WNtuple t = new WNtuple(Util.singleSortedMap(getSort(), newComps), guard());
            result.merge(t, mult, Integer::sum);
        }

        return new TupleBag(result);
    }

    /**
     * Sposta le clausole della guardia (Guard) come filtro (Filter) della tupla
     * se tutte le variabili coinvolte nelle clausole compaiono come proiezioni
     * all'interno delle componenti della tupla.
     *
     * @return una nuova WNtuple con il filtro aggiornato e la guardia ridotta,
     *         oppure 'this' se non è stata applicata alcuna trasformazione.
     */
    public WNtuple baseGuardReduction() {
        Guard currentGuard = guard();

        // Se la guardia è banale (True), non c'è nulla da spostare
        if (currentGuard.isTrivial()) {
            return this;
        }

        // Mappa degli indici di proiezione presenti nella tupla verso le loro posizioni (1-based)
        // Es: <c1, c3, c2> -> {1: pos 1, 3: pos 2, 2: pos 3}
        final ColorClass cc = getSort();
        if (cc == null) {
            return this; // Operiamo su tuple omogenee/single-color
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

        // Estraiamo le clausole della guardia (And)
        final Collection<Guard> guardClauses = (Collection<Guard>) And.getArgs(currentGuard);
        final Collection<Guard> clausesToMoveToFilter = new LinkedList<>();
        final Collection<Guard> newFilterClauses = new LinkedList<>();

        for (Guard clause : guardClauses) {
            if (clause instanceof Equality eq) {
                int idx1 = eq.getArg1().getIndex();
                int idx2 = eq.getArg2().getIndex();

                // Verifichiamo se entrambi gli indici delle variabili compaiono nella tupla
                Integer pos1 = projToPosMap.get(idx1);
                Integer pos2 = projToPosMap.get(idx2);

                if (pos1 != null && pos2 != null) {
                    // Creiamo la corrispondente clausola per il Filtro usando le posizioni della tupla!
                    Projection filterProj1 = Projection.builder(pos1, cc);
                    Projection filterProj2 = Projection.builder(pos2, cc);

                    Equality filterEq = (Equality) Equality.builder(filterProj1, filterProj2, eq.isEquality(), getCodomain());

                    clausesToMoveToFilter.add(eq);
                    newFilterClauses.add(filterEq);
                }
            }
        }

        // Se nessuna clausola è stata spostata, restituisce la tupla inalterata
        if (clausesToMoveToFilter.isEmpty()) {
            return this;
        }

        // Ricostruzione del nuovo Filtro
        Guard newFilter = filter();
        if (!newFilterClauses.isEmpty()) {
            final Set<Guard> args = new LinkedHashSet<>();

            if (newFilter.isTrivial()) {
                // niente da aggiungere: il filtro era true
            } else if (newFilter instanceof ElementaryGuard || newFilter instanceof And) {
                args.addAll(And.getArgs(newFilter));
            } else {
                args.add(newFilter);
            }

            args.addAll(newFilterClauses);
            newFilter = args.size() == 1 ? args.iterator().next()
                       : And.buildAndFormWithDom(args, getCodomain());
        }

        // Ricostruzione della nuova Guardia (rimuovendo le clausole spostate)
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
    public WNtuple baseFilterReduction(Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap, Map<ColorClass, Map<Boolean, Set<Membership>>> membMap) {

        final Collection<Guard> to_remove = new LinkedList<>();
        final SortedMap<ColorClass, List<? extends ColorFunction>> tuple_copy = new TreeMap<>();
        var reduced = false;
        ColorClass cc;
        int i;
        List<ColorFunction> h_tuple;

        // Riduzione per Membership
        for (var mx : membMap.entrySet()) {
            h_tuple = new ArrayList<>(getHomSubTuple(cc = mx.getKey()));
            for (var my : mx.getValue().entrySet()) {
                for (var m : my.getValue()) {
                    i = m.firstIndex() - 1;
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

        // Riduzione per Uguaglianze (=) e Disuguaglianze (!=)
        for (var ex : equalityMap.entrySet()) {
            if ((h_tuple = (List<ColorFunction>) tuple_copy.get(cc = ex.getKey())) == null) {
                h_tuple = new ArrayList<>(getHomSubTuple(cc));
            }

            for (var ey : ex.getValue().entrySet()) {
                final boolean op = ey.getKey(); // true = '=', false = '!='

                for (var eq : ey.getValue()) {
                    i = eq.firstIndex() - 1;
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
                        // UGUAGLIANZA (=)
                        final boolean fi_card_leq_1 = cardLeq1(f_i);
                        final boolean fj_card_leq_1 = cardLeq1(f_j);

                        if (fi_card_leq_1 || fj_card_leq_1) {
                            if (fi_card_leq_1 && isSetFunction(f_j)) {
                                // S_C2 alla posizione j ristretto alla proiezione puntuale f_i
                                h_tuple.set(j, succ_fi);
                                to_remove.add(eq);
                                reduced = true;
                            } else if (fj_card_leq_1 && isSetFunction(f_i)) {
                                // S_C2 alla posizione i ristretto alla proiezione puntuale f_j
                                h_tuple.set(i, succ_fj);
                                to_remove.add(eq);
                                reduced = true;
                            } else if (fi_card_leq_1 && fj_card_leq_1) {
                                // Entrambe proiezioni puntuali: sostituzione simbolica diretta
                                if (f_i instanceof Projection p1 && f_j instanceof Projection p2 && p1.getIndex() != p2.getIndex()) {
                                    final Projection target = Projection.builder(p1.getIndex(), cc);
                                    for (int k = 0; k < h_tuple.size(); k++) {
                                        h_tuple.set(k, substituteProjection(h_tuple.get(k), p2.getIndex(), target));
                                    }
                                    to_remove.add(eq);
                                    reduced = true;
                                } else if (f_i.equals(succ_fj)) {
                                    to_remove.add(eq); // Tautologia c_i = c_i
                                    reduced = true;
                                }
                            }
                        }
                    } else {
                        // DISUGUAGLIANZA (!=)
                        if (f_i.equals(f_j) && cardLeq1(f_i)) {
                            // Incompatibilità c_i != c_i su elemento singolo -> tupla vuota
                            return build().cast();
                        }

                        // Se una componente è un supporto globale S_C e l'altra è una proiezione puntuale c:
                        if (isSetFunction(f_i) && cardLeq1(f_j)) {
                            ColorFunction restricted = differMulti(f_i, succ_fj);
                            if (!restricted.equals(f_i)) {
                                h_tuple.set(i, restricted);
                                to_remove.add(eq); // Clausola assorbita nella componente come S_C - c
                                reduced = true;
                            }
                        } else if (isSetFunction(f_j) && cardLeq1(f_i)) {
                            ColorFunction restricted = differMulti(f_j, succ_fi);
                            if (!restricted.equals(f_j)) {
                                h_tuple.set(j, restricted);
                                to_remove.add(eq); // Clausola assorbita nella componente come S_C - c
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

        // Ricostruzione del filtro pulito
        var f = filter();
        if (!to_remove.isEmpty()) {
            var args = new HashSet<>(And.getArgs(f));
            to_remove.forEach(args::remove);

            if (args.isEmpty()) {
                f = guard.True.getInstance(getCodomain());
            } else if (args.size() == 1) {
                f = args.iterator().next();
            } else {
                f = And.buildAndFormWithDom(args, getCodomain());
            }
        }

        final SortedMap<ColorClass, List<? extends ColorFunction>> rebuilt = new TreeMap<>();
        for (var e : getHomSubTuples().entrySet()) {
            rebuilt.putIfAbsent(e.getKey(), new ArrayList<>(e.getValue()));
        }
        rebuilt.putAll(tuple_copy);

        return new WNtuple(f, rebuilt, guard());
    }

    // Helper for identifying set functions S_C
    private static boolean isSetFunction(ColorFunction cf) {
        return switch (cf) {
            case null -> false;
            case SetFunction setFunction -> true;
            case LinearComb lc -> lc.asMap().keySet().stream().anyMatch(Objects::nonNull);
            default -> false;
        };
    }

    /**
     * Sostituisce ricorsivamente un indice di proiezione target con la nuova proiezione all'interno di una ColorFunction.
     */
    private static ColorFunction substituteProjection(ColorFunction cf, int targetIndex, Projection replacement) {
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
    private static ColorFunction multisetConstraint(Membership m) {
        // In this library a Membership is a guard on a projection; for a multiset
        // component the effect is just a restriction of the support.
        // We keep it conservative and return the component unchanged when the
        // restriction is not explicit at this level.
        return new LinearComb(m.getSort());
    }


    private static ColorFunction interMulti(ColorFunction f1, ColorFunction f2) {
        if (f1.equals(f2)) {
            return f1;
        }

        // Convertiamo f1 e f2 in LinearComb per poter accedere alla mappa (ElementaryFunction -> Coefficiente)
        LinearComb lc1 = toLinearComb(f1);
        LinearComb lc2 = toLinearComb(f2);

        // Scorriamo ciascun termine del primo multiset
        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>();
        for (var entry : lc1.asMap().entrySet()) {
            final ElementaryFunction key = entry.getKey();
            final Integer v2 = lc2.asMap().get(key); // Cerchiamo lo stesso termine nel secondo multiset
            if (v2 != null) {
                // Nei multiset l'intersezione sceglie il MINIMO dei coefficienti
                final int min = Math.min(entry.getValue(), v2);
                if (min > 0) {
                    out.put(key, min);
                }
            }
        }

        //System.out.println("interMulti: " + f1 + " inter " + f2 + " = " + out); // debug
        // Se l'intersezione è vuota, restituisce una LinearComb vuota (il multiset nullo)
        return out.isEmpty() ? new LinearComb(f1.getSort()) : new LinearComb(out, false);
    }

    private static ColorFunction differMulti(ColorFunction f1, ColorFunction f2) {
        if (f1.equals(f2)) {
            return new LinearComb(f1.getSort()); // f1 - f1 = 0 (multiset vuoto)
        }

        LinearComb lc1 = toLinearComb(f1);
        LinearComb lc2 = toLinearComb(f2);

        // Copiamo tutti i termini e i loro coefficienti dal primo multiset
        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>(lc1.asMap());

        // Sottraggiamo i coefficienti del secondo multiset
        for (var e : lc2.asMap().entrySet()) {
            final int newVal = out.getOrDefault(e.getKey(), 0) - e.getValue();
            if (newVal == 0) {
                out.remove(e.getKey()); // Se il coefficiente scende a 0, il termine scompare
            } else {
                out.put(e.getKey(), newVal); // Altrimenti aggiorna il coefficiente (anche negativo)
            }
        }

        //System.out.println("differMulti: " + f1 + " differ " + f2 + " = " + out); // debug
        return out.isEmpty() ? new LinearComb(f1.getSort()) : new LinearComb(out, false);
    }

    private static LinearComb toLinearComb(ColorFunction cf) {
        if (cf instanceof LinearComb lc) {
            return lc;
        }
        // Avvolgiamo la singola funzione elementare in una LinearComb con molteplicità 1
        if (cf instanceof ElementaryFunction ef) {
            return new LinearComb(ef);
        }
        return new LinearComb(cf.getSort());
    }

    /**
     * Applica l'operatore successore con offset k a una ColorFunction (es. LinearComb).
     *
     * @param k l'offset del successore (può essere positivo o negativo)
     * @param f la funzione di colore / combinazione lineare di partenza
     * @return una nuova ColorFunction con gli indici o i successori shiftati di k
     */
    private static ColorFunction succMulti(int k, ColorFunction f) {
        if (k == 0 || f == null) {
            return f; // Offset 0: non cambia nulla
        }

        if (!(f instanceof LinearComb lc)) {
            return f;
        }

        final Map<ElementaryFunction, Integer> out = new LinkedHashMap<>();
        for (var entry : lc.asMap().entrySet()) {
            final SetFunction x = entry.getKey();
            final SetFunction shifted;

            // Se è una Proiezione, incrementa direttamente il suo successore
            if (x instanceof Projection p) {
                shifted = p.setExp(p.getSucc() + k);
            } else if (x instanceof Successor s) {
                shifted = Successor.factory(k + s.getExp(), s.getArg());
            } else {
                // Altrimenti crea un nuovo oggetto Successor mediante la factory
                shifted = Successor.factory(k, x);
            }

            // Fonde il nuovo termine nel multiset di output sommando i coefficienti
            if (shifted instanceof ElementaryFunction ef) {
                out.merge(ef, entry.getValue(), Integer::sum);
            }
        }

        return out.isEmpty() ? new LinearComb(f.getSort()) : new LinearComb(out, false);
    }

    /**
     * Cardinality <= 1 test for multiset components.
     */
    private static boolean cardLeq1(ColorFunction f) {
        Integer lb = f.cardLb();
        return lb != null && lb <= 1;
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
    public ArcFunction baseCompose(WNtuple other) {
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

        return new WNtuple(resultComps, other.guard(), true);
    }


    private void composeProjection(Projection p, Map<Integer, ColorFunction> subst, Map<ElementaryFunction, Integer> result, int multiplicity) {

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

    public ArcFunction repetedIndexCompose(WNtuple other) {

        //System.out.println(this + " " + other);
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
    public Guard buildRepeatedIndexFilter(List<Set<Integer>> indexPositions, Domain filterDomain) {
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
    public List<Set<Integer>> projectionIndexPositions() {
        final ColorClass cc = getSort();
        if (cc == null) {
            throw new IllegalStateException(
                    "projectionIndexPositions() requires a single-color tuple"
            );
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
