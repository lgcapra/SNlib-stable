package classfunction;

import java.util.*;
import logexpr.AndOp;
import expr.*;
import color.ColorClass;
import guard.*;
import util.Pair;
import util.Util;

/**
 * this class defines intersections over class functions; intersection is an "AND" operator
 *
 * @author lorenzo capra
 */
public final class Intersection extends N_aryClassOperator implements AndOp<SetFunction> {

    private final static String OPSYMB = " * ";//the intersection op. symbol
    //hashing 
    private Interval card;
    private Integer extended_compl;

    private Intersection(Set<? extends SetFunction> args, boolean check) {
        super(args, check);
    }

    /**
     * build an <code>Intersection</code> from a collection of class-functions;
     * if the collection is a singleton the only function is returned
     *
     * @param arglist the collection of operands
     * @param check the color check flag
     * @return a either an intersection or a class-function, depending on
     * whether the collection's fixedSize is greater than or equal to one
     * @throws IllegalDomain if the functions have different colors
     * @throws NoSuchElementException if the passed collection is empty
     */
    public static SetFunction factory(Collection<? extends SetFunction> arglist, boolean check) {
        Set<? extends SetFunction> asSet = Util.asSet(arglist);
        return asSet.size() < 2 ? asSet.iterator().next() : new Intersection(asSet, /*check*/ true);
    }

    /**
     * build an <code>Intersection</code> from a collection of class-functions,
     * checking the function's color-classes
     *
     * @param arglist the collection of operands
     * @return a either an intersection or a class-function, depending on
     * whether the collection's fixedSize is greater than or equal to one
     */
    public static SetFunction factory(Collection<? extends SetFunction> arglist) {
        return factory(arglist, true);
    }

    /**
     * build an <code>Intersection</code> from a list (varargs) of
     * class-functions,
     *
     * @param check the color check flag
     * @param args the list of functions
     * @return a either an intersection or a class-function, depending on
     * whether the list's fixedSize is greater than or equal to one
     */
    public static SetFunction factory(boolean check, SetFunction... args) {
        return factory(Arrays.asList(args), check);
    }

    /**
     * build an <code>Intersection</code> from a list (varargs) of
     * class-functions, checking the function's color-classes
     *
     * @param args the list of functions
     * @return a either an intersection or a class-function, depending on
     * whether the list's fixedSize is greater than or equal to one
     */
    public static SetFunction factory(SetFunction... args) {
        return factory(Arrays.asList(args), false);
    }

    @Override
    public SetFunction buildOp(Collection<? extends SetFunction> args) {
        return factory(args, true);
    }

    @Override
    public String symb() {
        return Intersection.OPSYMB;
    }

    
    /**
     * checks whether this intersection represents an "extended projection complement";
     * also considers the lower bound of the constraints and the min and max succ 
     * assumes that different projections map to different colours and no duplicates are present
     *
     * @return the number of proj. complements if and only if the check
     * succeeds; @code {0} otherwise
     */
    public Integer extendedCompl() {
        if (this.extended_compl == null) { // never computed before ...
            this.extended_compl = 0; //we assume it is not a proper ext compl ...
            ColorClass cc = getSort();
            Set<ProjectionBased> ncset = congruent(false);
            int size = size(), ncsize = ncset.size();
            if (cc.isOrdered()) {
                if (size == ncsize) {
                    Iterator<ProjectionBased> ite = ncset.iterator();
                    int min = ite.next().getSucc(), max = min;
                    while (ite.hasNext()) {
                        int s = ite.next().getSucc();
                        if (s < min) {
                            min = s;
                        } else if (s > max) {
                            max = s;
                        }
                    }
                    if (max - min < cc.lb()) {
                        this.extended_compl = ncsize;
                    }
                }
            } else {
                int sc = subclasses().size();
                if (sc < 2 && ncsize + sc == size) {
                    this.extended_compl = ncsize;
                }
            }
        }

        return this.extended_compl;
    }

    
    /**
     * @return the "cardinality" of this intersection term, if it is formed
     * exclusively by ProjectionComp(s) and possibly one Subcl 
     * CAREFUL assumes that the intersection-form is "simple" (constant-fixedSize), i.e., for
     * each pair S - X_i, S- X_j, i != j, X_i != X_j; it also checks the
     * presence of the Empty function; if card cannot be computed returns
     * <code>null</code>
     */
    @Override
    public Interval card() {
        Integer k = extendedCompl();
        if (0 != k) {
            if (this.card == null) {
                Interval in = subclasses().isEmpty() ? getSort().card() : subclasses().iterator().next().card();
                int ub = in.ub(), lb;
                boolean bounded = !in.unbounded();
                if (bounded && ub <= k) {
                    this.card = new Interval(0, 0); // optimization
                }
                else if ((lb = in.lb()) >= k) //new: in the previous version the cardinality 0 was equipared to null
                {
                    this.card = bounded ? new Interval(lb - k, ub - k) : new Interval(lb - k);
                }
            }
        }

        return this.card;
    }


    /**
     * @return a set with the the successor-arguments appearing on @code {this},
     * which is assumed to be an extended complement; an empty set if (for any
     * reasons) @code {this} is not an extended complement
     */
    public Set extendedComplSucc() {
        if (0 != extendedCompl()) {
            HashSet<Integer> succset = new HashSet<>();
            congruent(false).forEach((var x) -> {
                succset.add(x.getSucc());
            });
            return succset;
        }

        return Collections.EMPTY_SET;
    }

    /**
     * gets the index-separated operands of <code>this</code> intersection,
     *
     * @param i an index
     * @return the succset of i-indexed factors of <code>this</code>
     * intersection
     */
    public HashSet<SetFunction> getComponents(int i) {
        return Util.filter(getArgs(), f -> f instanceof ProjectionBased && ((ProjectionBased) f).getIndex() == i);
    }

    /**
     * this version assumes that the left operand (this) is a simplified
     * single-indexed, pure generalised complement (defined on an ordered
     * class);
     *
     * @param right the right operand
     * @return the composition result between <code>this</code> (assumed a unary
     * function) and <code>right</code> IMPORTANT NOTE the case S-X_1 \cap
     * S_{1,k} is not considered given that, when separating a left-composed
     * tuple in an intersection of single-index tuples, constants factors are
     * separated too (otherwise also this case should be considered here...)
     */
    @Override
    public Pair<SetFunction, Integer> baseCompose(SetFunction right) {
        Interval rcard = right.card();
        if (rcard != null) {
            //System.out.println("right card:" + rcard); //debug
            if (rcard.singleValue(1)) { // base case: right's cardinality is one...
                List<SetFunction> res_args = new ArrayList<>();
                getArgs().forEach(f -> {
                    res_args.add(new ClassComposition(f, right));
                });
                return new Pair<>(Intersection.factory(res_args), null);
            } else {
                ColorClass cc = getSort();
                if (cc.isOrdered()) {
                    Set<Integer> succargs = extendedComplSucc(); // the successor arguments...
                    final var ecsize = succargs.size();
                    if (ecsize > 0) {
                        if (rcard.lb() > ecsize) {
                            return new Pair<>(All.getInstance(cc), null);
                        }
                        if (cc.fixedSize() > 0) { // fixed card: the intersection of complements becomes a sum of projection succesors ...    
                            Set<SetFunction> newargs = new HashSet<>();
                            Util.missing(succargs, 0, cc.fixedSize()).forEach(h -> {
                                newargs.add(Successor.factory(h, right));
                            }); // the composition outcome is directly computed ... we may further optimize using a closed formula? may be at tuple level
                            return new Pair<>(Union.factory(newargs, false), null); // the obtained sum may not be disjoint
                        }
                        //else: a split should be done (new)
                        return new Pair<>(null, ecsize - rcard.lb() + 1);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public int splitDelim() { //new (partial solution)
        Interval mycard = card();
        return mycard != null && !mycard.singleton() && mycard.lb() <= 1 ? 1 : super.splitDelim();
    }

    /**
     * efficiently builds an Intersection obtained by joining this and the
     * specified function (in particular, if the former is another Intersection)
     *
     * @param other the specified function to be
     * @param check joined
     * @return a new Intersection resulting from joining this and other Note:
     * this operation could be embedded in the constructor by using the
     * associative red. rule
     */
    public SetFunction join(SetFunction other, boolean check) {
        if (check && !this.getSort().equals(other.getSort())) {
            throw new IllegalDomain("cannot join intersections of different colors!");
        }

        List<SetFunction> newargs = new ArrayList<>(getArgs());
        if (other instanceof Intersection intersection) {
            newargs.addAll(intersection.getArgs());
        } else {
            newargs.add(other);
        }

        return Intersection.factory(newargs);
    }

    @Override
    public final SetFunction setDefaultIndex() {
        return Intersection.factory(ClassFunction.setDefaultIndex(getArgs()));
    }

    @Override
    public Set<? extends Pair<? extends SetFunction, ? extends Guard>> toSimpleFunctions(final Set<? extends Equality> ineqlist, final Map<Projection, Subcl> inmap, final Map<Projection, Set<Subcl>> notinmap, Domain domain) {
        //System.out.println("Intersection.toSimpleFunctions (1)\n"+this);
        Set< Pair<SetFunction, Guard>> res = new HashSet<>();
        Projection pr;
        Set<? extends SetFunction> operands = getArgs();
        if (congruent(true).isEmpty()) {  // no Projection in the operands' list - only ProjectionComp(s) and (at most) one Subcl
            HashSet<SetFunction> red_args;
            boolean reduced = false;
            Set<Subcl> subclasses = subclasses();
            if (subclasses.size() > 1) {
                throw new IllegalArgumentException("intersection with more subclasses! (should be empty)"); //added for safety reasons!
            }
            if (!subclasses.isEmpty()) {  // there is one subclass
                final Subcl subc = subclasses.iterator().next();
                for (SetFunction x : operands) {
                    Subcl sc; // the (possiby null) subclass associated with the arg X_i (pr) of pc by an "in" clause
                    if (subc.equals(x) || subc.equals(sc = inmap.get(pr = ((ProjectionComp) x).getArg()))) // the i-th sublist contains only ProjectionComp
                    {
                        continue;
                    }
                    //System.out.println(this+", "+inmap+ ", "+notinmap); //  there is no the clause X_i in subc
                    red_args = new HashSet<>(operands);
                    red_args.remove(x); // S - X_i  (pc) is erased from the copy of args of subc
                    SetFunction redf = Intersection.factory(red_args);
                    if (sc != null || notinmap.getOrDefault(pr, Collections.EMPTY_SET).contains(subc)) // there is either X_i in sc, sc != subc, or X_i notin subc 
                    {
                        res.add(new Pair<>(redf, True.getInstance(domain)));  // the pc's arg refers to a subclass other than subc ...
                    } else { // neither the pc's arg X_i belongs to any subclass != subc nor there is X_i notin subc
                        if (subc.card().ub() != 1) // subc is not a singleton subclass
                        {
                            res.add(new Pair<>(this, Membership.build(pr, subc, domain))); // S - X_i \cap subc [X_i in subc] -> 0 if |subc| = 1 (optimization)
                        }
                        res.add(new Pair<>(redf, Membership.build(pr, subc, false, domain)));
                    }
                    reduced = true;
                    break;
                }
            }
            if (!reduced) { // we consider projection complements (with different indices)
                search:
                for (Map<Integer, Set<ProjectionBased>> ncm = Util.mapFeature(congruent(false), ProjectionBased::getIndex); ncm.size() > 1;) {
                    Iterator<Map.Entry<Integer, Set<ProjectionBased>>> mit = ncm.entrySet().iterator();
                    Map.Entry<Integer, Set<ProjectionBased>> e1 = mit.next(); //safe
                    do {
                        Map.Entry<Integer, Set<ProjectionBased>> e2 = mit.next(); //safe
                        for (ProjectionComp pc1 : Util.cast(e1.getValue(), ProjectionComp.class)) {
                            for (ProjectionComp pc2 : Util.cast(e2.getValue(), ProjectionComp.class)) {
                                Projection p1 = pc1.getArg(), p2 = pc2.getArg();
                                Equality eq = (Equality) Equality.builder(p1, p2, false, domain);
                                if (!(ineqlist.contains(eq) || differentDom(p1, p2, inmap, notinmap))) { // in g there is¨no pc.arg != pc2.arg or pc in sc1,pc in sc2, or ... :
                                    res.add(new Pair(this, eq)); // the equality pc.arg <> pc2.arg is added
                                    red_args = new HashSet<>(operands); //optimization
                                    red_args.remove(pc2);
                                    res.add(new Pair(Intersection.factory(red_args), eq.opposite()));// the corresponding equality is added
                                    break search; //exits all loops
                                }
                            }
                        }
                    } while (mit.hasNext());

                    ncm.remove(e1.getKey());
                }
            }
        } else { // the intersection contains a projection
            Set<Guard> new_guards = new HashSet<>();
            Iterator<ProjectionBased> ite = congruent(true).iterator(); // one projection is taken
            for (pr = (Projection) ite.next(); ite.hasNext();) // new equalities
            {
                new_guards.add(Equality.builder(pr, (Projection) ite.next(), true, domain));
            }
            for (ProjectionBased pc : congruent(false)) // new inequalities
            {
                new_guards.add(Equality.builder(pr, (Projection) pc.opposite(), false, domain));
            }
            for (Subcl s : subclasses()) // new memberships
            {
                new_guards.add(Membership.build(pr, s, domain));
            }

            if (!new_guards.isEmpty()) // means that further rewriting needed
            {
                res.add(new Pair<>(pr, And.factory(new_guards)));
            }
        }
        //System.out.println("ecco torightcompset: "+res);
        return res;
    }

    /*
    checks whether two variables have different color-sets
     */
    private static boolean differentDom(Projection p1, Projection p2, Map<Projection, Subcl> inmap, Map<Projection, Set<Subcl>> notinmap) {
        Subcl sc1 = inmap.get(p1), sc2 = inmap.get(p2);
        if (sc1 != null && sc2 != null) {
            return sc1 != sc2;
        }

        return notinmap.getOrDefault(p2, Collections.EMPTY_SET).contains(sc1) || notinmap.getOrDefault(p1, Collections.EMPTY_SET).contains(sc2);
    }

    @Override
    public boolean elementary() {
        return getArgs().stream().allMatch(e -> elementary());
    }

}
