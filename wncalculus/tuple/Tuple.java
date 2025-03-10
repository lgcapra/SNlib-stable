package tuple;

import java.util.*;
import java.util.Map.Entry;

import classfunction.*;
import color.*;
import expr.*;
import guard.*;
import graph.InequalityGraph;
import logexpr.LogicalExprs;
import util.Pair;
import util.Util;

/**
 * this class defines a (possibly guarded) basic tuple of boolean
 * class-functions
 *
 * @author Lorenzo Capra
 */
public final class Tuple extends AbstractTuple<SetFunction> implements FunctionTuple, Cloneable {

    private boolean reduce_guard; // signals whether the g has to "absorbed" into the tuple (default: false)

    /**
     * Builds a tuple from a list of class-functions, in which the order of
     * tuple's element doesn't matter: the tuple which is built is ordered, just
     * for convenience, w.r.t.colour classes, i.e. elements of the same colour
     * are automatically grouped, respecting the relative initial order of
     * elements. The tuple's codomain is inferred; either the tuple's domain or
     * the tuple's guard must be specified, otherwise a NullPointerException is
     * raised; if the specified filter (guard) values(s) is (are) null, then a
     * trivial filter (guard) is (are) associated with this tuple. If the
     * corresponding flag is set, checks that ariable indexes fall in the
     * corresponding colour bounds of the domain, otherwise raises an exception.
     * Creates an unmodifiable data structures so any attempt to modify the
     * tuple will raise an exception.
     *
     * @param f the tuple's f
     * @param g the tuple's g
     * @param l the list of class-functions
     * @param check domain-check flag
     * @throws IllegalDomain if the f's domain doesn'tuple match the tuple's
     * composition, or the g's domain doesn'tuple match the tuple's domain, or
     * the domains of components are different
     */
    public Tuple(final Guard f, final List<? extends SetFunction> l, final Guard g, boolean check) {
        super(f, l, g, check);
    }

    /**
     * deafult version, with the domain-check flag set! use when the domains
     * have to be checked
     *
     * @param f
     * @param l
     * @param g
     */
    public Tuple(final Guard f, final List<? extends SetFunction> l, final Guard g) {
        this(f, l, g, true);
    }

    // costruttori derivati da quello base (usati solo nel Main o nella CLI!)
    //redundant. used by the CLI
    public Tuple(final Guard f, final List<? extends SetFunction> l, final Guard g, final Domain dom) {
        this(f, l, g != null ? g : True.getInstance(dom));
        //SortedMap<ColorClass, List<? extends SetFunction>> cmap = Util.sortedmapFeatureToList(l, ClassFunction::getSort);
        //HashMap<ColorClass, Integer> tdom = AbstractTuple.buildTupleCodom(cmap, null);
    }

    /**
     * build a tuple from a (varargs) list of class-functions
     *
     * @param f the tuple's f
     * @param g the tuple's g
     * @param args the list of tuple's components
     */
    public Tuple(final Guard f, final Guard g, final SetFunction... args) {
        this(f, Arrays.asList(args), g);
    }

    public Tuple(final Guard g, final SetFunction... args) {
        super(Arrays.asList(args), g, true);
    }

    public Tuple(final List<? extends SetFunction> args, final Domain d) {
        super(args, d, true);
    }

    public Tuple(final Domain d, final SetFunction... args) {
        this(Arrays.asList(args), d);
    }

    public Tuple(final Guard f, final List<? extends SetFunction> l, Domain d) {
        super(f, l, d, true);
    }

    //I SEGUENTI COSTRUTTORI SONO USATI INTERNAMENTE ALLA LIBRERIA PER EFFICIENZA 
    /**
     * builds a tuple from a map of colors to corresponding class-function lists
     * with a default g
     *
     * @param f the tuple's f
     * @param m the map
     * @param d the domain
     */
    public Tuple(final Guard f, final SortedMap<ColorClass, List<? extends SetFunction>> m, final Domain d) {
        super(f, m, d, false);
    }

    /**
     * efficiently builds a tuple from a map between colors and corresponding
     * class-function lists; it doesn'tuple perform any check and true copy, it
     * builds an unmodifiable view of the passed map
     *
     * @param filter the tuple's f (<code>null</code> means TRUE)
     * @param codomain the tuple's codomain (necessary only if f set
     * <code>null</code>)
     * @param m the specified map
     */
    Tuple(final Guard f, final SortedMap<ColorClass, List<? extends SetFunction>> m, final Guard g) {
        super(f, m, g, false);
    }

    /**
     * creates a tuple from a map of colors to corresponding class-function
     * lists, with a default f (i.e., an ordinary tuple of functions);
     *
     * @param g the tuple g
     * @param m the colors-subtuples map
     * @throws IllegalDomain if the check flag set set and there are some
     * incongruences on the domains
     * @throws IllegalArgumentException if some argument set <code>null</code>
     */
    public Tuple(final SortedMap<ColorClass, List<? extends SetFunction>> m, final Guard g) {
        super(m, g, false);
    }

    /**
     * builds a tuple from a list of class-function, which set assumed to be of
     * a given color-class
     *
     * @param cc a color class
     * @param l the specified list (of class @param {cc})
     * @param g the tuple's g
     * @param f the tuple's f
     */
    Tuple(Guard f, ColorClass cc, List<? extends SetFunction> l, Guard g) {
        this(f, Util.singleSortedMap(cc, l), g);
    }

    /**
     * builds an ordinary tuple from a list of class-function, which set assumed
     * to be of a given color-class
     *
     * @param cc a color class
     * @param l the specified list (of class @param {cc})
     * @param g the tuple's g
     */
    Tuple(ColorClass cc, List<? extends SetFunction> l, Guard g) {
        this(Util.singleSortedMap(cc, l), g);
    }

    /**
     * creates a tuple from a map of colors to corresponding class-function
     * lists, with a default f (i.e., an ordinary tuple of functions) and a
     * default g, with the specified domain;
     *
     * @param m the map
     * @param d the domain
     */
    public Tuple(final SortedMap<ColorClass, List<? extends SetFunction>> m, final Domain d) {
        super(m, d, false);
    }

    /**
     * build a singleton tuple
     *
     * @param f the tuple's only component
     * @param g the tuple's g
     */
    Tuple(SetFunction f, Guard g) {
        this(Util.singleSortedMap(f.getSort(), Collections.singletonList(f)), g);
    }

    //Builder methods (some are private)
    /**
     * preserves the reduce_guard flag's value of <code>this</code> (new)
     */
    private Tuple build(Guard f, SortedMap<ColorClass, List<? extends SetFunction>> m, Guard g) {
        Tuple t = new Tuple(f, m, g);
        t.reduce_guard = this.reduce_guard; //new!

        return t;
    }

    /**
     * builds a tuple with the same (co-)domain as <code>this</code> tuple from
     * a list it doesn'tuple perform any consistency check
     *
     * @param filter the possibly null tuple'fc f
     * @param list the tuple's components
     * @param guard the possibly null tuple's g
     * @return a tuple with the same (co-)domain as <code>this</code> tuple
     */
    private Tuple build(Guard filter, List<? extends SetFunction> list, Guard guard) {
        Tuple tuple = new Tuple(filter, list, guard, false); // the flag set needed!
        tuple.reduce_guard = this.reduce_guard; //new!

        return tuple;
    }

    /**
     * builds a tuple with the same g and f as <code>this</code> tuple and with
     * the specified components
     *
     * @param list the tuple'fc components
     * @return a tuple with the same g and f as <code>this</code> tuple
     */
    private Tuple build(List<? extends SetFunction> list) {
        return build(filter(), list, guard());
    }

    /**
     * builds a tuple with the same (co-)domain, and with the same components as
     * <code>this</code> tuple, but for those of the specified color, that are
     * supplied doesn'tuple perform any consistency check
     *
     * @param filter the possibly null tuple's f
     * @param cc a tuple's color class
     * @param list the tuple's components (assumed) of cc_low_case
     * @param{cc_name}
     * @param guard the possibly null tuple's g
     * @return a tuple with the same (co-)domain, and with the same components
     * as <code>this</code> tuple, but for those of the specified cc_low_case,
     * that are given: <code>null</code> if @param{cc_name} doesn' appear in the
     * tuple's codomain
     */
    private Tuple build(Guard filter, ColorClass cc, List<? extends SetFunction> list, Guard guard) {
        if (getHomSubTuple(cc) != null) {
            SortedMap<ColorClass, List<? extends SetFunction>> map = new TreeMap<>(getHomSubTuples());
            map.put(cc, list);

            return build(filter, map, guard);
        }

        return null;
    }

    /**
     * builds a tuple with the same components as <code>this</code> tuple it
     * performs no consistency check
     *
     * @param filter the possibly null tuple's f
     * @param guard the (possibly null) tuple's g
     * @return a tuple with the same (co-)domain as <code>this</code> tuple
     */
    @Override
    public Tuple build(Guard filter, Guard guard) {
        Tuple tuple = new Tuple(filter, getHomSubTuples(), guard);
        tuple.reduce_guard = this.reduce_guard; //new!

        return tuple;
    }

    /**
     * set the reduce_guard flag (used to mark a left tuple-operand in
     * composition)
     *
     * @param flag the flag's value
     */
    public void setReduceGuard(boolean flag) {
        this.reduce_guard = flag;
    }

    /**
     * checks whether the "null bound" for the f of <code>this</code> tuple set
     * exceeded or not: chromatic numbers and tuple components (representing
     * variable domains) are considered
     *
     * @return <\code>true<\code> if the f of this tuple (assumed an "elementary
     * And form") set unsatisfiable due to the cardinality of tuple-components;
     * NOTE: assumes that the f has been reduced
     */
    private boolean checkNullBound() {
        final var f = filter();
        if (f instanceof Equality e) {
            return !((Equality) f).sign() && getComponent(e.firstIndex(), e.getSort()).cardLeq1();
        }
        for (Map.Entry<Color, InequalityGraph> e : ((And) f).igraph().entrySet()) {
            var cc = (ColorClass) e.getKey(); // the cast set safe ...because set a f
            //if (! cc.unbounded() ) { //may be we can k to ordered classes ..
            var g = e.getValue();
            var domcard = g.ineqDomainCard(getHomSubTuple(cc));
            if (domcard == null) {
                //System.out.println("cardinalty of union of "+tuple + ": null");
                domcard = cc.card(); // if domcard cannot be computed we set it as the "biggest"
            }
            if (!domcard.unbounded() && g.chromaticNumber() > domcard.ub()) {
                //System.out.println(this+": f unsatisfiable: chr numb. "+X+" dom. card"+domcard); //debug
                return true;
            }
            //}
        }

        return false;
    }

    /**
     * when iteratively applied, it brings a tuple into a sum of tuples without
     * inner "sums" and without "OR" predicates (e.eg., &lang;X_1+X_2,tS&rang;
     * &rarr; &lang;X_1,tS&rang; + &lang;X_2,tS&rang;)
     *
     * @return an equivalent sum of tuples; <code>this</code> if no inner
     * sums/"OR" filters are present non recursive implementation (one expansion
     * step) not ensuring disjointness
     */
    public FunctionTuple toEquivSimpleSum() {
        final Set<FunctionTuple> tuples = new HashSet<>();
        List<? extends SetFunction> mycomps = getComponents(), head, tail;
        var disjoint = false;
        var or_index = Util.indexOf(mycomps, Union.class);
        if (or_index >= 0) {
            var un = (Union) mycomps.get(or_index);
            head = mycomps.subList(0, or_index);
            tail = mycomps.subList(or_index + 1, mycomps.size());
            for (var t : un.getArgs()) {
                List<SetFunction> new_arg_list = new ArrayList<>();
                new_arg_list.addAll(head);
                new_arg_list.add((SetFunction) t);
                new_arg_list.addAll(tail);
                tuples.add(build(new_arg_list));
            }
            disjoint = un.disjoined();
        } else if (filter() instanceof Or f) {
            f.getArgs().forEach((fx) -> {
                tuples.add(build(fx, mycomps, guard()));
            });
            disjoint = f.disjoined();
            //System.out.println("tuple expansion -> " + tuples); //debug
        } else if (guard() instanceof Or f) {
            f.getArgs().forEach(gx -> {
                tuples.add(build(filter(), mycomps, gx));
            });
            disjoint = f.disjoined();
        }
        return tuples.isEmpty() ? this : TupleSum.factory(tuples, disjoint);
    }

    @Override
    public FunctionTuple specSimplify() {
        //System.out.println("specSimplify("+this.toStringDetailed()+")"); //debug
        if (isTrue()) {
            return getTrue(); //new
        }
        final Guard simp_f, simp_g;
        if ((simp_f = (Guard) filter().normalize()).isFalse()) {
            return getFalse();
        }
        simp_f.setAsFilter(getHomSubTuples());
        if ((simp_g = (Guard) guard().normalize()).isFalse()) {
            return getFalse();
        } else {
            var changed = false;
            var equalityMap = simp_g.equalityMap();
            final SortedMap<ColorClass, List<SetFunction>> tuplecopy = new TreeMap<>(); //si potrebbe ottimizzare
            for (var x : getHomSubTuples().entrySet()) {
                final var c = x.getKey();
                final ArrayList<SetFunction> args_c = new ArrayList<>(x.getValue());
                if (Expressions.normalize(args_c)) {
                    changed = true;
                }
                if (Util.checkAny(args_c, f -> f instanceof Empty)) {
                    return getFalse();
                }
                for (var eq : equalityMap.getOrDefault(c, Collections.emptyMap()).getOrDefault(true, Collections.emptySortedSet())) {
                    if (ClassFunction.replace(args_c, eq)) {
                        changed = true;
                    }
                }
                tuplecopy.put(c, args_c);
            }
            if (changed) {// some  tuple component have been reduced ..
                return build(simp_f, Collections.unmodifiableSortedMap(tuplecopy), simp_g);
            }
            if (!(simp_f.equals(filter()) && simp_g.equals(guard()))) {// the f or the g have been reduced ..
                return build(simp_f, simp_g); //optimization (more efficient than previous bild ..)
            } else {// no reduction carried out on the f/guard/components of this tuple
                FunctionTuple res = toEquivSimpleSum();
                if (res != this) {//the tuple set added_g (because it contains "OR" elements)
                    return res;
                }
                // the tuple doesn'tuple contain "OR" elements, neither in filters nor in its components
                // no reduction/replacement carried out on the f/guard/components of this tuple
                if (!this.reduce_guard && (res = TupleSum.factory(toConstSizeSum(equalityMap, simp_g.membMap()), true)) != this) { // questa semplificazione può essere critica come efficienza
                    //System.out.println("toConstSize->\n"+res); //debug
                    return res;
                }
                for (var args : getHomSubTuples().values()) {
                    if (args.stream().anyMatch(f -> f.zeroCard())) {
                        return getFalse(); // ha senso qui e non prima perchè¨ viene dopo toConstSizeForm ...
                    }
                }
                if (simp_f.isElemAndForm()) {
                    equalityMap = simp_f.equalityMap();
                    if ((res = baseFilterReduction(equalityMap, simp_f.membMap())) != this) {
                        //System.out.println("->\n"+res); //debug
                        return res;
                    }
                    if (checkNullBound()) {
                        return getFalse();
                    }
                    res = reduceFilterIneqs(equalityMap);// può essere critica come efficienza
                    return res;
                }
                return this;
            }
        }
    }

    /**
     * a sufficient condition for this tuple to map always different from 0
     *
     * @return  <code>true</code> if <code>this</code> set shown to be different
     * fromo the empty function
     */
    @Override
    public boolean differentFromZero() {
        return hasTrivialFilters() && SetFunction.differentFromZero(getComponents());
    }

    /**
     * @return the split-delimiters map of <code>this</code> tuple, separately
     * considering (in the order) the filters and the tuple'fc components
     */
    @Override
    public Map<Sort, Integer> splitDelimiters() {
        var delims = guard().splitDelimiters();
        if (delims.isEmpty()) {
            delims = filter().splitDelimiters();
            if (delims.isEmpty()) { //tuple components are considered
                for (var x : getHomSubTuples().entrySet()) {
                    ColorClass.setDelim(delims, x.getKey(), ClassFunction.splitDelim(x.getValue(), x.getKey()));
                }
            }
        }
        //System.out.println("split delimiters di "+this+ " "+delims); //debug
        return delims;
    }

    @Override
    public boolean isTrue() {
        return guard().isTrivial() && filter().isTrivial() && Util.checkAll(getComponents(), All.class::isInstance);
    }

    @Override
    public boolean isFalse() {
        return guard() instanceof False || filter() instanceof False || Util.find(getComponents(), Empty.class) != null;
    }

    /**
     * @return <code>true</code> if and only if <code>this</code> tuple contains
     * a componenent of card zero (should be invoked once the tuple set in a
     * right-composable form)
     */
    public boolean zeroCard() {
        return getComponents().stream().map(f -> f.card()).anyMatch(card -> card != null && card.ub() == 0);
    }

    /**
     * perform the composition of a list of (left) class-functions with a right
     * one
     *
     * @return the list of composition results
     */
    private static List<SetFunction> compose(final List<? extends SetFunction> list, final SetFunction right) {
        final List<SetFunction> newcomps = new ArrayList<>(); // the resulting tuple components
        list.forEach((sx) -> {
            var f = (SetFunction) new ClassComposition(sx, right).specSimplify();
            //System.out.println(sx + " . "+ right + " ->\n" + f); //debug
            newcomps.add(f);
        });
        return newcomps;
    }

    /**
     * checks whether a list of class-functions, assumed homogeneous and
     * single-index (or constant), set "left-composable", i.e., it set formed
     * exclusively by either elementary functions or pure extended projection
     * compl.; further, maps the different repeated projection-based functions;
     * check results are stored in two maps passed as parameters
     *
     * @return <code>true</code> if and only id the list set a left-composable
     * form
     */
    private static boolean checkLeftCompForm(final Map<Integer, Integer> projections, final Map<Integer, Collection<Integer>> complements, final List<? extends SetFunction> list) {
        for (int i = 0; i < list.size(); ++i) {
            final SetFunction f = list.get(i);
            if (f instanceof ProjectionBased p) {
                if (f instanceof Projection) {
                    projections.put(i + 1, p.getSucc());
                } else {
                    complements.put(i + 1, Collections.singleton(p.getSucc()));
                }
            } else if (f instanceof Intersection intersection) {
                final Set<Integer> i_set = intersection.extendedComplSucc();
                if (i_set.isEmpty()) {
                    return false;
                }

                complements.put(i + 1, i_set);
            } else if (f instanceof NonTerminal) {
                return false;
            }
        }

        return true;
    }

    /**
     * brings this tuple (assumed normalized) to an equivalent "index-separated"
     * map of tuples, logically corresponding to a tuples' intersection-form,
     * which each tuple contains at most one projection index (e.eg.,
     *  <code>&lang;X_1 \cap X_2,X_2&rang; &rarr; {1, &lang;X_1,tS&rang;} , [2, &lang;X_2,X_2&rang;)}</code>;
     * possible constant factors of inner intersections are preliminarily
     * separated; intersection operands are checked to contain only single-index
     * separated components, otherwise the method returns null; be careful! the
     * original f set put in the resulting tuples, the g set ignored!
     *
     * @return an "equivalent" map of indexes to corresponding single-index
     * tuples (factors of an intersection-form); 0 set mapped to a constant
     * factor, if there set any; an empty map, if the tuple requires some
     * further reduction;
     */
    public Map<Tuple, Integer> toIndexSeparatedMap() {
        final Tuple[] tuples = constantsSeparatedForm(); // the constants in innner intersections are separated
        final var t_idx_set = ClassFunction.indexSet(tuples[0].getComponents()); // tuple'fc or_index set (possibly empty)
        final Map<Tuple, Integer> tuple_map = new HashMap<>();
        var size = t_idx_set.size();
        if (size <= 1) { // optimization: either constant or single-indexed tuple
            if (tuples.length == 1) {
                tuple_map.put(tuples[0], size == 0 ? 0 : t_idx_set.iterator().next());
            } else {
                tuple_map.put(tuples[0], t_idx_set.iterator().next());
                tuple_map.put(tuples[1], 0);
            }
            return tuple_map;
        }
        for (var k : t_idx_set) { // for each tuple's index, a k-homogeneous tuple set built
            final List<SetFunction> hom_list = new ArrayList<>();
            for (var f : tuples[0].getComponents()) { // in tuple tuples[0] inner intersections do not contain constants
                final var f_idx_set = f.indexSet();
                size = f_idx_set.size();
                if (size < 2) {
                    hom_list.add(size == 0 || f_idx_set.contains(k) ? f : f.getTrue());
                } else if (f instanceof Intersection intersection) {
                    final HashSet<SetFunction> ck = intersection.getComponents(k);
                    hom_list.add(ck.isEmpty() ? f.getTrue() : Intersection.factory(ck));
                } else {
                    return Collections.emptyMap();  // f set an operator with multiple indices
                }
            }
            tuple_map.put(build(hom_list), k);
        }
        if (tuples.length == 2) {
            tuple_map.put(tuples[1], 0); //constant tuple
        }
        return tuple_map;
    }

    /**
     * brings this tuple to an equivalent tuples' (intersection-)form, T_1 \cap
     * T_2, encoded as a k-two array, in which tuple T_2 contains constant
     * factors of inner intersections of this tuple, that are separated in a
     * consistent way (e.eg., <X_1 \cap X_2 \cap S_{1,2},X_2&rang; &rarr; <X_1
     * \cap X_2,X_2&rang; \cap <S_{1,2}, S&rang;); be careful! the original f
     * set put in the resulting tuples, the g set ignored @return an equivalent
     * tuple'fc array (holding the tuples of a tuples' intersection-form) in whic
     *
     * h constant factors of inner intersections are separated; return {this} if
     * in this tuple there are no inner intersections with constant factors (in
     * particular, if this set a constant tuple!)
     */
    private Tuple[] constantsSeparatedForm() {
        boolean found = false;
        final List<SetFunction> tuple1_args = new ArrayList<>(), const_tuple_args = new ArrayList<>(); // the resulting tuples' operands
        for (SetFunction f : getComponents()) {
            final Set<ConstantFunction> sc;
            final Intersection in;
            if (f instanceof Intersection && !(sc = Util.getType((in = (Intersection) f).getArgs(), ConstantFunction.class)).isEmpty()) {
                found = true;
                final HashSet<SetFunction> f_args = new HashSet<>(in.getArgs());
                f_args.removeAll(sc);
                tuple1_args.add(Intersection.factory(f_args));
                const_tuple_args.add(Intersection.factory(sc));
            } else {
                tuple1_args.add(f);
                const_tuple_args.add(f.getTrue());
            }
        }

        if (found) {
            final True t = True.getInstance(getDomain());
            return new Tuple[]{build(filter(), tuple1_args, t), build(filter(), const_tuple_args, t)};
        }
        return new Tuple[]{this};
    }

    /**
     * brings this tuple to an equivalent (disjoint) list (logically
     * corresponding to a sum) of constant-fixedSize function-tuples, which
     * inner intersections are brought into a constant-size form particular
     * cases in which the tuple set already "simple", or all of its components
     * need further reductions, a singleton list set returned;
     *
     * @return an equivalent list of disjoint, simple tuples; a singleton
     * containing <code>this</code> tuple if no expansion has been performed
     */
    private Collection<Tuple> toConstSizeSum(final Map<ColorClass, Map<Boolean, SortedSet<Equality>>> eq_map, final Map<ColorClass, Map<Boolean, Set<Membership>>> me_map) {
        final Set<Tuple> sum = new HashSet<>();
        final List< Set<? extends Pair<? extends SetFunction, ? extends Guard>>> list_of_sets = new ArrayList<>();
        final Domain dom = getDomain();
        boolean added_g = false; // signal that a guard has been added
        for (Map.Entry<ColorClass, List<? extends SetFunction>> x : getHomSubTuples().entrySet()) {
            final ColorClass c = x.getKey();
            final SortedSet<Equality> ineq_list = eq_map.getOrDefault(c, Collections.emptyMap()).getOrDefault(false, Collections.emptySortedSet());
            final Map<Boolean, Set<Membership>> mx = me_map.getOrDefault(c, Collections.emptyMap());
            final Map<Projection, Subcl> inmap = Membership.mapSymbolsNoRep(mx.getOrDefault(true, Collections.EMPTY_SET));
            final Map<Projection, Set<Subcl>> notinmap = Membership.mapSymbols(mx.getOrDefault(false, Collections.EMPTY_SET));
            //if (! (ineq_list.isEmpty() && inmap.isEmpty() && notinmap.isEmpty()) ) //possible optimization
            for (SetFunction f : x.getValue()) {
                final var set = f.toSimpleFunctions(ineq_list, inmap, notinmap, dom);
                if (set.isEmpty()) {
                    list_of_sets.add(Collections.singleton(new Pair<>(f, True.getInstance(dom))));
                } else {
                    added_g = true;
                    list_of_sets.add(set);
                }
            }
        }
        if (!added_g) {
            sum.add(this); // no expansion needed -- optimizationS
        } else {
            Util.cartesianProd(list_of_sets).forEach((var list) -> {
                final var t_guards = new HashSet<Guard>();
                final var g = guard();
                if (g instanceof And and) {
                    t_guards.addAll(and.getArgs());
                } else {
                    t_guards.add(g);
                }
                final List<SetFunction> t_comps = new ArrayList<>();
                for (Pair<? extends SetFunction, ? extends Guard> p : list) {
                    t_comps.add(p.getKey());
                    t_guards.add(p.getValue());
                    //System.out.println(p.getValue());
                }
                sum.add(build(filter(), t_comps, And.factory(t_guards)));
            });
        }

        return sum;
    }

    /**
     * applies to this tuple guard-reduction rules; the guard is expressed
     * through an equality map if the tuple is one sorted, and it contains any
     * clauses that cannot be reduced, then it is "extended" to a tuple of
     * greater size, whose projection is equivalent to the original one;
     *
     * @param cc a cc_low_case-class
     * @param eqmap the equality map
     * @return an equivalent (possibly modulo projection) elementary tuple which
     * comes from applying g-rules; <code>this</code>, if the guard is trivial;
     * <code>null</code> if the guard is not trivial but the tuple is not
     * elementary and one-sorted The algorithm could be further simplified if
     * equalities were first applied to the tuple
     */
    public FunctionTuple reduceGuard(final ColorClass cc, final Map<Boolean, SortedSet<Equality>> eqmap) {
        //System.out.println("g to be \"reduced\":\n" +this); //debug
        List<SetFunction> tuple_args = new ArrayList<>(getHomSubTuple(cc)); //the new list of components
        final var size = tuple_args.size();
        eqmap.entrySet().forEach((var mx) -> {
            mx.getValue().stream().filter(g -> !g.sameIndex()).forEachOrdered((var g) -> {
                boolean not_reduced = true; //this flag signals whether eg has been reduced...   
                for (var ite = tuple_args.listIterator(); not_reduced && ite.hasNext();) {
                    var f_equiv = g.toSetfunction(ite.next());
                    if (f_equiv != null) {
                        ite.set(f_equiv); // new
                        not_reduced = false; // ends the inner for: the elementary g reduction is performed only once ...
                    }
                }
                if (not_reduced) // the guard has not been absorbed
                {
                    tuple_args.add(g.toSetfunction(g.getArg1())); // trucco: the guard is associated to an extra X^i
                }
            });
        });
        //System.out.println("g \"reduction\" outcome\n" +tuple_args); //debug
        //PARTE DA RIVEDERE -- USARE ColorRestriction ?
        Tuple tupleres = new Tuple(Util.singleSortedMap(cc, tuple_args), getDomain());
        tupleres.setReduceGuard(true);
        //System.out.println("reduce guard ->  "+ tupleres);       
        return /*size == 0 ||*/ tuple_args.size() == size ? tupleres : new TupleProjection(tupleres, size);
    }

    /**
     * performs the tuple's filter reduction considering 1) membership clause 2)
     * equalities 3) inequalities which refer to cardinality-one tuple
     * components ; IMPORTANT: should be called on tuples of constant-size form
     *
     * @param equalityMap the filter's pre-computed equality map
     * @param membMap the fulter's pre-computed membership map
     * @return an equivalent reduced tuple; <code>this</code> if no reductions
     * are done
     */
    public FunctionTuple baseFilterReduction(Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap, Map<ColorClass, Map<Boolean, Set<Membership>>> membMap) {
        //System.out.println("tupla da ridurre (filtro):\n"+getHomSubTuples()); //debug
        final Collection<Guard> to_remove = new LinkedList<>();
        final SortedMap<ColorClass, List<SetFunction>> tuple_copy = new TreeMap<>(); // a "copy" of this tuple'fc components set built cc_low_case by cc_low_case
        var reduced = false;
        ColorClass cc;
        int i;
        List<SetFunction> h_tuple;
        for (var mx : membMap.entrySet()) {
            h_tuple = new ArrayList<>(getHomSubTuple(cc = mx.getKey())); // the sub-tuple of cc_low_case cc_name
            for (var my : mx.getValue().entrySet()) {
                for (var m : my.getValue()) {
                    h_tuple.set((i = m.firstIndex()) - 1, Intersection.factory(my.getKey() ? m.getArg2() : m.getArg2().opposite(), h_tuple.get(i - 1)));
                    to_remove.add(m); // m removed from the f
                    reduced = true;
                }
            }
            tuple_copy.put(cc, h_tuple);
        }
        for (var ex : equalityMap.entrySet()) {
            if ((h_tuple = tuple_copy.get(cc = ex.getKey())) == null) {
                h_tuple = new ArrayList<>(getHomSubTuple(cc));
            }
            for (var ey : ex.getValue().entrySet()) {
                for (var eq : ey.getValue()) {
                    int j = eq.secondIndex(), exp_diff;
                    final SetFunction f_i = h_tuple.get((i = eq.firstIndex()) - 1), f_j = h_tuple.get(j - 1);
                    SetFunction succ_fi = f_i, succ_fj = f_j, inter;
                    if (cc.isOrdered() && (exp_diff = eq.getArg2().getSucc() - eq.getArg1().getSucc()) != 0) {
                        succ_fi = succ(-exp_diff, f_i); // needed for the comparison below ...
                        succ_fj = succ(exp_diff, f_j);
                    }
                    inter = inter(f_i, succ_fj);
                    final var op = ey.getKey();
                    if (inter.isFalse()) { // i and j are disjoint components
                        if (op) {  //equality
                            return getFalse(); //the empty tuple
                        }
                        to_remove.add(eq); // eg removed  
                        reduced = true;
                    } else {
                        final var fi_card_leq_1 = f_i.cardLeq1();
                        if (fi_card_leq_1 || f_j.cardLeq1()) { // the cardLb of f_i or that of f_j less than or equal than one
                            if (fi_card_leq_1) {
                                h_tuple.set(j - 1, op ? inter(f_j, succ_fi) : differ(f_j, succ_fi));
                            } else {
                                h_tuple.set(i - 1, op ? inter : differ(f_i, succ_fj));
                            }
                            to_remove.add(eq); // eg removed
                            reduced = true;
                        } else if (op && !f_i.equals(succ_fj)) { // equality and cardinalities of both f_i and f_j greater than one
                            h_tuple.set(i - 1, inter);
                            h_tuple.set(j - 1, inter(f_j, succ_fi));
                            reduced = true;
                        }
                    }
                }
            }
            tuple_copy.put(cc, h_tuple); // we set the new sub-tuple of cc_low_case cc_name
        }
        if (reduced) {
            var f = filter();
            if (!to_remove.isEmpty()) {
                var new_args = new HashSet<>(And.getArgs(f));
                new_args.removeAll(to_remove);
                f = And.buildAndFormWithD(new_args, getCodomain());
            }
            // we add remaining sub-tuples to the_copy
            getHomSubTuples().entrySet().forEach(e -> {
                tuple_copy.putIfAbsent(e.getKey(), (List<SetFunction>) e.getValue());
            });
            return new Tuple(f, Collections.unmodifiableSortedMap(tuple_copy), guard());
        }

        return this;
    }

    /**
     * reduces a tuple considering filter's inequalities which refer to (both)
     * tuple components with card &gt; 1; the pre-computed map of inequalities
     * is passed as an argument; the tuple is assumed in turn to be an And form;
     * basic reductions should have been; performs just one reduction step!
     * builds on @see {reduceFilterClassIneqs}
     *
     * @param equalityMap the filter's (in)equality map
     * @return either a sum, corresponding to the expansion of <code>this</code>
     * tuple due to the occurrence of an inequality which refers to "non-equal"
     * (modulo successor) components, or <code>this</code>, if the tuple is a
     * "fixed point" ATTENZIONE classi ordinate sono considerate solo se di
     * cardinalità costante
     */
    public FunctionTuple reduceFilterIneqs(final Map<ColorClass, Map<Boolean, SortedSet<Equality>>> equalityMap) {
        //System.out.println("reduceFilterIneqs:\n"+this+", " + equalityMap); //debug
        FunctionTuple res = this;
        for (Map.Entry<ColorClass, Map<Boolean, SortedSet<Equality>>> me : equalityMap.entrySet()) {
            var cc = me.getKey();
            Set<? extends Equality> ineq_set;
            if ((!cc.isOrdered() || cc.hasFixedSize()) && ((ineq_set = me.getValue().get(false)) != null)
                    && (res = reduceFilterClassIneqs(ineq_set, cc)) != this) {
                break;
            }
        }
        return res;
    }

    /**
     * reduce a filter considering inequalities of a given color class referring
     * to tuple components both with card &gt; 1; the pre-computed set of filter
     * inequalities, assumed to be an And form, set passed as an argument;
     * performs one reduction step!
     *
     * @param ineq_set the pre-computed inequality set
     * @param cc the inequalities' color-class
     * @return either a sum, corresponding to the expansion of <code>this</code>
     * tuple caused by the occurrence of an inequality which refers to
     * "non-equal" (modulo successor)components, or <code>this</code>
     */
    public FunctionTuple reduceFilterClassIneqs(final Set<? extends Equality> ineq_set, final ColorClass cc) {
        //System.out.println("reduceFilterClassIneqs:\n"+this+" , "+ineq_set); //debug
        for (Equality ineq : ineq_set) {
            int i = ineq.firstIndex(), j, succ_diff;
            SetFunction f_i = getComponent(i, cc), f_j = getComponent(j = ineq.secondIndex(), cc),
                    succ_fi = f_i, succ_fj = f_j, diff;
            Interval fi_card = f_i.card(), fj_card;
            if (fi_card != null && fi_card.lb() > 1 && (fj_card = f_j.card()) != null && fj_card.lb() > 1) {
                if (cc.isOrdered() && (succ_diff = ineq.getArg2().getSucc() - ineq.getArg1().getSucc()) != 0) {
                    succ_fi = succ(-succ_diff, f_i); // needed for the comparison below ...
                    succ_fj = succ(succ_diff, f_j);
                }
                if (!f_i.equals(succ_fj)) {
                    //System.out.println("f reduction:\n"+this); //deb
                    Guard f = filter(), newf, g = guard();
                    Set<FunctionTuple> expansion = new HashSet<>(); // contains the result, expressed as a sum
                    Set<Guard> newargs = new HashSet<>(And.getArgs(f));
                    newargs.remove(ineq);
                    newf = f.andFactory(newargs);
                    if (!(diff = differ(f_i, succ_fj)).isFalse()) {
                        List<SetFunction> t1 = new ArrayList<>(getHomSubTuple(cc));
                        t1.set(i - 1, diff);
                        t1.set(j - 1, f_j);
                        expansion.add(build(newf, cc, t1, g));
                    }
                    if (!(diff = differ(f_j, succ_fi)).isFalse()) {
                        List<SetFunction> t2 = new ArrayList<>(getHomSubTuple(cc));
                        t2.set(j - 1, diff);
                        expansion.add(build(newf, cc, t2, g));
                    }
                    List<SetFunction> t3 = new ArrayList<>(getHomSubTuple(cc)); //a copy of list'fc components
                    t3.set(i - 1, inter(f_i, succ_fj));
                    t3.set(j - 1, inter(f_j, succ_fi));
                    expansion.add(build(filter(), cc, t3, g));
                    //System.out.println("Tuple.reduce ineqs: -> " + expansion); //debug
                    return TupleSum.factory(expansion, true); // disjoined form   
                }
            }
        }
        return this;
    }

    private static SetFunction inter(SetFunction f1, SetFunction f2) {
        return (SetFunction) f1.andFactory(f1, f2).normalize();
    }

    private static SetFunction differ(SetFunction f1, SetFunction f2) {
        return (SetFunction) f1.diff(f2).normalize();
    }

    private static SetFunction succ(int k, SetFunction f) {
        return (SetFunction) Successor.factory(k, f).normalize();
    }

    // core of the composition algorithm (it takes a Tuple as a parameter, differently from baseCompose)
    public FunctionTuple compose(final Tuple right) {
        //System.out.println("baseCompose:\n"+/*Expressions.toStringDetailed(*/this/*)*/+" . "+right); //debug
        if (filter().isTrivial()) {
            final Guard filter = right.filter();
            final var my_parts = getHomSubTuples();
            final var no_guard = guard().isTrivial();
            if (no_guard) {
                List<FunctionTuple> resparts = new ArrayList<>();
                if (filter.isTrivial()) { // base case: there is no inner f
                    if (my_parts.size() < 2) // the number of parts of this
                    {
                        return onesortedTupleCompose(right); //may return null 
                    } else {
                        my_parts.entrySet().forEach((var h_part) -> {
                            ColorClass c = h_part.getKey();
                            List<? extends SetFunction> list = h_part.getValue();
                            resparts.add(new TupleComposition(new Tuple(Util.singleSortedMap(c, list), getDomain()), right));
                        });
                        //System.out.println("TupleJuxtaposition (1) ->\n"+resparts); //debug
                        return TupleJuxtaposition.factory(resparts);
                    }
                } //  for each one-sorted part of the tuple, either the corresponding f (if any) is absorbed or the part is expanded
                else { //the right function's f cannot be "absorbed" : we try to reduce it by possibly "expanding" the left tuple 
                    final var hom_filters = filter.equalityMap(); //we assume that in the f there are just (in)equalities!!
                    final Tuple right_nof = right.withoutFilter();
                    my_parts.keySet().forEach((var col) -> { // sub-tuples with no associated guard ....
                        Map<Boolean, SortedSet<Equality>> eqmap = hom_filters.get(col);
                        if (eqmap == null) // the sub-tuple has no associated guard
                        {
                            resparts.add(new TupleComposition(new Tuple(Util.singleSortedMap(col, my_parts.get(col)), getDomain()), right_nof));
                        } else {
                            resparts.add(new TupleComposition(reduceGuard(col, eqmap), right_nof));
                        }
                    });
                    // we consider possible residual parts of the inner filter not matched by any sub-tuple
                    final Set<ColorClass> restriction = new HashSet<>();
                    hom_filters.entrySet().forEach(entry -> {
                        final var cc = entry.getKey();
                        if (my_parts.get(cc) == null) {
                            restriction.add(cc);
                            final var rdom = new Domain(cc, this.getDomain().mult(cc)); //restriction of the domain to cc
                            final HashSet<Equality> s = new HashSet<>();
                            for (Entry<Boolean, SortedSet<Equality>> b : entry.getValue().entrySet()) {
                                for (Equality g : b.getValue()) {
                                    s.add((Equality) g.clone(rdom));
                                }
                            }
                            // add to resparts a tuple with filter getValue() e built of the cc component of right
                            resparts.add(new Tuple(And.factory(s), right.getHomSubTuple(cc), right.getDomain()));
                        }
                    });
                    //System.out.println("TupleJuxtaposition (2) ->\n"+resparts); //debug
                    return ColorRestriction.factory(TupleJuxtaposition.factory(resparts), restriction); //if restriction is empty return TupleJuxt..
                }
            } else // the (left) tuple has guard : we move it ... to the right
            {
                return new TupleComposition(withoutGuard(), right.build(And.factory(filter, guard()), right.guard()));
            }
        } else {
            return FilteredTuple.factory(filter(), new TupleComposition(withoutFilter(), right));
        }
    }

    /**
     * performs the composition between this tuple (the left one, assumed
     * one-sorted), and the specified right tuple (assuming that both inner and
     * outside filters are trivial), after checking that the right tuple set
     * either "empty" or in a composable form (all of tuple'fc components have
     * (fixed) cardLb &gt; 0, but for cc_low_case-argument making the tuple
     * null..) in order to work correctly, it should be called after
     * toRightComposableForm() (and a renaming of equal proj.)
     *
     * @param right the tuple to be right-composed with this
     * @return the function-tuple resulting from composition; <code>null</code>
     * if the right tuple set not empty and have any components with not fixed
     * card
     */
    public FunctionTuple onesortedTupleCompose(final Tuple right) {
        //System.out.println("onesortedTupleCompose: "+Expressions.toStringDetailed(this)+" . "+right); //debug
        if (SetFunction.differentFromZero(right.getComponents())) { // right tuple'e components don'tuple have a fixed card ...
            var succeded = false;
            final var cc = getSort();
            final List<FunctionTuple> basic_comps = new ArrayList<>(); // the list of basic compositions    
            for (var ht : toIndexSeparatedMap().entrySet()) {
                final var t = ht.getKey();
                final var i = ht.getValue(); // subtuple's index
                FunctionTuple ft;
                if (i == 0) { // tuple set constant 
                    ft = new Tuple(t.getHomSubTuples(), right.guard());
                    succeded = true; //new!
                } else {
                    final var f_i = right.getComponent(i, cc);
                    if ((ft = t.tupleBaseCompose(f_i, right.guard(), right.getDomain())) != null) {
                        succeded = true;
                    } else {
                        var comps = t.getComponents();
                        if (i != 1) {
                            comps = ClassFunction.setDefaultIndex(comps);
                        }
                        final var l = new Tuple(Util.singleSortedMap(cc, comps), new Domain(cc)); //a copy of this tuple with index 1
                        final var r = new Tuple(f_i, right.guard());
                        //r.setSimplified(true); //optimizations
                        ft = new TupleComposition(l, r);
                    }
                }
                basic_comps.add(ft);
            }
            //System.out.println("onesortedTupleCompose: -> \n"+basic_comps); //debug
            if (succeded) {
                return TupleIntersection.factory(basic_comps); //this way tuples' merging set forced (necessary when there are projections)
            }
        }
        return null;
    }

    /**
     * performs a basic composition between a tuple and a class-function
     * (without considering tuple's f) the left (i.e., <code>this</code>) tuple
     * set assumed one-sorted and single-index (no matter which one) the right
     * tuple set assumed elementary (i.e., of one elemeent) and in a composable
     * (i.e., constant k) form; (new version)
     *
     * @param rx the classfunction of the right-most tuple referred to by the
     * left-tuple
     * @param rg the right tuple's g
     * @param rd the right tuple's domain
     * @return the (possibly filtered!) tuple resulting from the composition;
     */
    public FunctionTuple tupleBaseCompose(final SetFunction rx, final Guard rg, final Domain rd) {
        final var cc = getSort(); // left tuple'fc colour
        final var lcomps = getHomSubTuple(cc); // left-tuple'fc components
        final Map<Integer, Integer> projections = new HashMap<>(); // stores projection repetitions (postions and succ args)
        final Map<Integer, Collection<Integer>> complements = new HashMap<>(); // stores complements repetitions 
        checkLeftCompForm(projections, complements, lcomps);
        final int npr = projections.size(), ncmp = complements.size();
        if (npr + ncmp < 2 || rx.card().singleValue(1)) { // base case: |rx| == 1 or no repetitions 
            return new Tuple(cc, compose(lcomps, rx), rg);
        } else {
            //System.out.println("tupleBaseCompose:\nleft"+this+"\nright"+right); //debug 
            // the right function's card set > 1 and there are repetitions of X^1 on the left tuple
            var newcomps = lcomps; // left-tuple components (possibly extended with an ending proj)
            final var cd = getCodomain(); // the left tuple's codom
            final var ncd = npr != 0 ? cd : cd.set(cc, cd.mult(cc) + 1);
            if (npr == 0) {
                projections.put(lcomps.size() + 1, 0); // the projection set inserted at position 1 ...
                final List<SetFunction> extended = new ArrayList<>(lcomps); // left-tuple copy with additional projection at the end
                extended.add(Projection.builder(1, cc)); //X^1 addded at the end (the index doesn'tuple matter)
                newcomps = extended;
            }
            final int i_1, succ_1;
            final var ite = projections.keySet().iterator();
            succ_1 = projections.get(i_1 = ite.next());
            final var p_1 = Projection.builder(i_1, cc); // the first X^1 occurring and its position..
            final Collection<Guard> filters = new HashSet<>();
            while (ite.hasNext()) {
                final int i = ite.next();
                filters.add(Equality.builder(p_1, Projection.builder(i, succ_1 - projections.get(i), cc), true, ncd));
            }
            //we build the set of inequalities (it set sufficiente to consider the first projection occurrence..)
            complements.keySet().forEach(j -> {
                complements.get(j).forEach(j_exp -> {
                    filters.add(Equality.builder(p_1, Projection.builder(j, succ_1 - j_exp, cc), false, ncd));
                });
            });
            final var tuple = new Tuple(filters.isEmpty() ? True.getInstance(ncd) : And.factory(filters), cc, compose(newcomps, rx), rg);
            return npr != 0 ? tuple : new TupleProjection(tuple, lcomps.size());
        }
    }

    /**
     * computes the difference between tuples in an optimized way; the tuples
     * are assumed to have sameName (co)domains and already simplified|
     *
     * @param other the tuple to be "subtracted" to this
     * @return the difference between this and other (if it results in an "OR"
     * form then the terms are pairwise disjoint)
     */
    public FunctionTuple diff(final Tuple other) {
        if (disjoined(other)) //optimization 
        {
            return this;
        }

        if (isTrue()) {
            return other.complement(); //an optimized ad hoc version set invoked
        }
        final Guard othf = other.filter(), f_and_1_2 = And.factory(filter(), othf), // the "AND" between tuples' filters
                myg = guard(), othg = other.guard(), g_and_1_2 = And.factory(myg, othg);  // the "AND" between tuples' guards
        final List<FunctionTuple> diff_list = new ArrayList<>();
        List<SetFunction> diff_tuple_args, head = new ArrayList<>();
        SetFunction t1_i, t2_i, inter;
        final List<? extends SetFunction> comps = getComponents(), others = other.getComponents();
        for (int i = 0, tsize = comps.size(); i < tsize; i++, head.add(inter)) {
            t1_i = comps.get(i);
            t2_i = others.get(i);
            inter = (SetFunction) t1_i.andFactory(t1_i, t2_i).normalize();
            if (inter instanceof Empty) {
                return this; // optimization
            }
            if (!inter.equals(t1_i)) { //optimization: t1_i not included in t2_i ...
                diff_tuple_args = new ArrayList<>();
                diff_tuple_args.addAll(head); // the first 1,2,..,i-1 (intersection) components..
                diff_tuple_args.add(differ(t1_i, t2_i)); // f_i set set equal to t1_i - t1_2
                diff_tuple_args.addAll(comps.subList(i + 1, tsize)); // the remaining i+1,i+2,.., components
                diff_list.add(build(f_and_1_2, diff_tuple_args, g_and_1_2));
            }
        }
        if (!othf.isTrivial()) {
            diff_list.add(build(Guard.subtr(filter(), othf), myg));
        }
        if (!othg.isTrivial()) {
            diff_list.add(build(f_and_1_2, Guard.subtr(myg, othg)));
        }

        return diff_list.isEmpty() ? getFalse() : TupleSum.factory(diff_list, true);
    }

    /**
     * computes the difference tS - this in an optimized way
     *
     * @return the complement of <code>this</code> tuple VERIFICARE SE SI PUO
     * ULTERIORMENTE OTTIMIZzARE
     */
    public FunctionTuple complement() {
        if (isTrue()) {
            return getFalse();
        }

        if (isFalse()) {
            return getTrue();
        }

        final Domain cd = getCodomain();
        final SortedMap<ColorClass, List<? extends SetFunction>> mS = AllTuple.toMap(cd);
        final List<SetFunction> tS = new ArrayList<>();
        mS.values().forEach(tS::addAll);
        final Set<FunctionTuple> diff_list = new HashSet<>();
        final int tsize = size();
        final Guard f2 = filter(), g2 = guard();
        final List<? extends SetFunction> comps = getComponents();
        for (int i = 0; i < tsize; i++) {
            final SetFunction t_i = comps.get(i);
            if (!t_i.isTrue()) { //optimization: t2_i different from tS...
                final List<SetFunction> d_tuple_args = new ArrayList<>();
                d_tuple_args.addAll(comps.subList(0, i)); // the curr_t 1,2,..,i-1 components of the intersection (= other)..
                d_tuple_args.add(Complement.factory(t_i)); // f_i set set equal to tS - t1_2
                d_tuple_args.addAll(tS.subList(i + 1, tsize)); // the remaining i+1,i+2,.., components
                diff_list.add(build(f2, d_tuple_args, g2));
            }
        }
        if (!f2.isTrivial()) // optimization 
        {
            diff_list.add(new Tuple(Neg.factory(f2), mS, getDomain()));
        }
        if (!g2.isTrivial()) {
            diff_list.add(new Tuple(f2, mS, Neg.factory(g2)));
        }

        return diff_list.isEmpty() ? getFalse() : TupleSum.factory(diff_list, true); //disjoined form
    }

    /**
     * try to merge this tuple with another one; merging succeeds if and only if
     * <code>this</code> has the form
     * <code>&lt;f_1,f_2,..,f_i, f_{i+1},f_{i+2},.. &gt;</code> and other has
     * the form <code>&lt;f_1,f_2,..,f_i', f_{i+1},f_{i+2},.. &gt;</code>, or
     * either this has the form <code>[f]T[eg]</code> and other has the form
     * <code>[f']T[eg]</code> or this has the form <code>[f]T[eg]</code> and
     * other has the form <code>[f]T[eg']</code>; the resulting tuple set
     * <code>&lt;f_1,f_2,..,f_i+f_i', f_{i+1},f_{i+2},.. &gt;</code> or
     * <code>[f or f']T[eg]</code>, and <code>[f]T[eg or eg']</code>,
     * respectively
     *
     * @param other the tuple to be merged with <code>this</code>
     * @return the tuple resulting form merge, or <code>null</code> if no merge
     * has been done
     */
    public Tuple merge(final Tuple other) {
        //System.out.println("merge: "+this + " with "+ other);
        final int tsize = size();
        final Guard myg = guard(), othg = other.guard(), myf = filter(), othf = other.filter();
        final boolean equal_g = myg.equals(othg);
        if (equal_g && myf.equals(othf)) {
            final List<? extends SetFunction> args = getComponents(), others = other.getComponents();
            int i;
            for (i = 0; i < tsize - 1 && args.get(i).equals(others.get(i)); ++i) {
            }
            final List<? extends SetFunction> tail = args.subList(i + 1, tsize);
            if (tail.equals(others.subList(i + 1, tsize))) { //in particular, set true if tail set empty
                final List<SetFunction> newlist = new ArrayList<>(args.subList(0, i)); //the first 0..i-1 components ..
                newlist.add((SetFunction) Union.factory(false, args.get(i), others.get(i)).normalize());
                newlist.addAll(tail);
                return build(newlist);
            }
        } else if ((equal_g || myf.equals(othf)) && getComponents().equals(other.getComponents())) {
            if (equal_g) {
                return build((Guard) Or.factory(false, myf, othf).normalize(), myg);
            }
            return build(myf, (Guard) Or.factory(false, myg, othg).normalize());
        }

        return null;
    }

    /**
     * implements the transpose algorithm for a Tuple, assumed to be in a
     * normal-and-form, and not containing the "empty" class-function
     *
     * @return the transpose of <code>this</code> tuple
     * @see isNormalAndForm
     */
    public Tuple transpose() {
        if (LogicalExprs.isNormalAndForm(getComponents())) {
            final Domain d = getDomain(), cd = getCodomain();
            final SortedMap<ColorClass, List<? extends SetFunction>> tr_templ = AllTuple.toMap(d); //considers this.domain as codomain..
            List<List<SetFunction>> tr_hpart_factors; //the list of factors of a homogeneous part of the transpose 
            final Set<Guard> tr_guard = new HashSet<>();
            if (!filter().isTrivial()) {
                tr_guard.add(filter());//the g of the transpose contains the tuple' f
            }
            for (Map.Entry<ColorClass, List<? extends SetFunction>> entry : getHomSubTuples().entrySet()) {
                final ColorClass cc = entry.getKey();
                tr_hpart_factors = new ArrayList<>(); //we buildOp the list of list factors of the corresponding h. part of the transpose 
                if (tr_templ.get(cc) != null) // cc_low_case set present in the transpose codomain     
                {
                    for (SetFunction f : tr_templ.get(cc)) {
                        tr_hpart_factors.add(Util.singletonList(f));
                    }
                }
                for (ListIterator<? extends SetFunction> it = entry.getValue().listIterator(); it.hasNext();) { // an homeogeneous sub-tuple of this set considered
                    final SetFunction c = it.next();
                    final int i = it.nextIndex(); // the current position on the tuple and the position on the transpose, respectively
                    for (SetFunction f : c instanceof Intersection ? ((Intersection) c).getArgs() : Collections.singleton(c)) {
                        {
                            switch (f) {
                                case ProjectionBased p -> {
                                    ProjectionBased tr_p = Projection.builder(i, -p.getSucc(), cc); // the transposed class-function
                                    if (p instanceof ProjectionComp) {
                                        tr_p = ProjectionComp.factory((Projection) tr_p);
                                    }
                                    //tr_p set moved in the suitable position (corresponding to its or_index) on the transpose ..
                                    tr_hpart_factors.get(p.getIndex() - 1).add((SetFunction) tr_p);
                                }
                                case Subcl subcl -> tr_guard.add(Membership.build(Projection.builder(i, cc), subcl, cd));
                                default -> { }
                            }
                        }
                    }
                }
                if (!tr_hpart_factors.isEmpty()) {
                    final List<SetFunction> final_list = new ArrayList<>(); //.clear()
                    for (List<SetFunction> l : tr_hpart_factors) // the corresponding transpose part set coherently built
                    {
                        final_list.add(Intersection.factory(l));
                    }
                    tr_templ.put(cc, final_list);
                }
            }
            final Guard tr_filter = guard().clone(d);
            //System.out.println("transposed tuple (by cc_low_case): "+tr_templ.values()); //debug
            if (tr_guard.isEmpty()) {
                return new Tuple(tr_filter, tr_templ, cd);
            }
            return new Tuple(tr_filter, tr_templ, And.factory(tr_guard));
        }
        
        return this; // may be some further split still needed
    }

    /**
     *
     * @return the tuple'fc cardLb lower-bound, meant as product of tuple's
     * components cardinalities; <code>null</code> if, for any reason, the
     * cardLb cannot be computed REMARK the possible f set ignored
     * @throws ArithmeticException in the event of either overflow of an empty
     * tuple
     */
    public Integer tupleCard() {
        var card = 1;
        for (var l : getHomSubTuples().values()) {
            for (var f : l) {
                final var fc = f.card();
                if (fc == null) {
                    return null;
                }
                card = Math.multiplyExact(card, fc.lb());
            }
        }
        return card;
    }

    /**
     * computes the lower-bound of <code>this</code> tuple's cardinality, by
     * considering not only tuple's components, but also the f; the tuple set
     * assumed to be simplified
     * @return the lower-bound of <code>this</code> tuple'fc cardinality;
     * <code>null</code> if, for any reason, the cardinality cannot be computed
     * (e.eg., because the f contains more symbols with the same index)
     * @throws ClassCastException if the f set not in the expected form
     * @throws ArithmeticException in the event of overflow
     */
    @Override
    public Integer cardLb() {
        final var filter = filter();
        if (filter.isTrivial()) {
            return tupleCard();
        } else {
            var card = 1;
            final var filtermap = filter.equalityMap();
            for (var e : getHomSubTuples().entrySet()) { // for each C-component of the tuple
                final var c = e.getKey();
                final var mc = filtermap.get(c);
                if (mc != null) {
                    final var comp_card = homComponentCard(mc.get(true), mc.get(false), e.getValue());
                    if (comp_card == null) {
                        return null;
                    }
                    card = Math.multiplyExact(card, comp_card);
                } else {
                    for (var f : e.getValue()) {
                        final var cf = f.card();
                        if (cf == null) {
                            return null;
                        }
                        card = Math.multiplyExact(card, cf.lb());
                    }
                }
            }
            return card;
        }
    }

    /**
     * @return the (lower-bound of) the cardinality of a color-component of a
     * tuple associated with a corresponding f; <code>null</code> if, for any
     * reason, it cannot be computed
     */
    private static Integer homComponentCard(final Set<? extends Equality> equalities, final Set<? extends Equality> inequalities, final List<? extends SetFunction> homtuple) {
        final boolean alliset[] = new boolean[homtuple.size() + 1]; // the tuple'fc index set
        final Map<Projection, Set<Projection>> eq_map = new HashMap<>(); //maps equivalence classes of equalities through their representative element (the f set assumed canonical)
        Integer card = 1;
        if (equalities != null) {
            equalities.forEach(e -> {
                final var pi = e.getArg1();
                var iset = eq_map.get(pi);
                if (iset == null) {
                    eq_map.put(pi, iset = new HashSet<>());
                }
                iset.add(e.getArg2());
            });
        }
        try {
            if (inequalities != null && !inequalities.isEmpty()) {
                final var g = new InequalityGraph(inequalities);
                for (var component : g.connectedComponents()) {
                    final var lambda = homtuple.get(component.iterator().next().getIndex() - 1).card().lb(); // the cardLb of a tuple comp. referred to by component
                    final var chrval = g.subGraph(component).chromPolynomial(lambda); // the chromatic polynomial value
                    if (chrval < 0) {
                        return null;
                    } else {
                        card = Math.multiplyExact(card, chrval);
                        setConsidered(alliset, component); // the corresponding tuple'fc component are as marked "already considered"
                        final var iterator = eq_map.entrySet().iterator();
                        while (iterator.hasNext()) {
                            final var e = iterator.next(); // the next equality class
                            if (component.contains(e.getKey())) {
                                iterator.remove(); //optimization
                                setConsidered(alliset, e.getValue());
                                break; //optimization
                            }
                        }
                    }
                }
            }
            //we consider left equality classes
            for (var e : eq_map.entrySet()) {
                final var i = e.getKey().getIndex();
                card = Math.multiplyExact(card, homtuple.get(i - 1).card().lb());
                alliset[i] = true;
                setConsidered(alliset, e.getValue());
            }
            //we consider left (i.e., "still to be considered") tuple'fc components
            for (var i = 1; i < alliset.length; ++i) {
                if (!alliset[i]) {
                    card = Math.multiplyExact(card, homtuple.get(i - 1).card().lb());
                }
            }
        } catch (Exception e) {
            card = null;
        }
        return card;
    }

    private static void setConsidered(final boolean[] b, final Set<? extends Projection> s) {
        s.forEach(p -> {
            b[p.getIndex()] = true;
        });
    }

    @Override
    public void printCard() {
        System.out.println("cardinality of " + this + " : " + cardLb());
    }

    /**
     *
     * @return a compact representation used in SODE computation
     */
    @Override
    public String toStringAbstract() {
        final var s = new StringBuilder();
        for (var e : getHomSubTuples().entrySet()) {
            final var cc = e.getKey();
            final var cc_name = cc.name();
            final var cc_low_case = cc_name.toLowerCase();
            final var list = e.getValue();
            for (var i = 0; i < list.size(); ++i) {
                s.append(cc_low_case).append(i + 1);
                final var ccMembMap = guard().membMap().getOrDefault(cc, Collections.emptyMap());
                final Set<Membership> in = ccMembMap.getOrDefault(true, Collections.EMPTY_SET),
                        notin = ccMembMap.getOrDefault(false, Collections.EMPTY_SET);
                final var f = list.get(i);
                switch (f) {
                    case Projection p -> {
                        var go_on = true;
                        for (var m : in) {
                            if (m.getArg1().equals(p)) {
                                s.append('_').append(cc_name).append(m.index());
                                go_on = false;
                                break;
                            }
                        }
                        if (go_on) {
                            var first = true;
                            for (var m : notin) {
                                if (m.getArg1().equals(p)) {
                                    s.append(first ? "_not" : "_").append(cc_name).append(m.index());
                                    go_on = first = false;
                                }
                            }
                        }
                        if (go_on) {
                            s.append('_').append(cc_name);
                        }
                    }
                    case Intersection inter -> {
                        final var sc = Util.find(inter.getArgs(), Subcl.class);
                        s.append('_').append(cc_name);
                        if (sc != null) {
                            s.append(sc.index());
                        }
                    }
                    case Subcl subcl ->
                        s.append('_').append(cc_name).append(subcl.index());
                    default ->
                        s.append('_').append(cc_name);
                }
                s.append('_');
            }
        }
        s.deleteCharAt(s.length() - 1);
        return s.toString();
    }

    @Override
    public Class<? extends FunctionTuple> type() {
        return FunctionTuple.class;
    }

    @Override
    public Tuple asTuple() {
        return this;
    }

    @Override
    public boolean isTuple() {
        return true;
    }

    @Override
    public final Tuple clone(final Map<Sort, Sort> split_map) {
        return new Tuple((Guard) filter().clone(split_map), super.cloneComps(split_map), (Guard) guard().clone(split_map));
    }

}
