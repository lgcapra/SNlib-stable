package wncalculus.tuple;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import wncalculus.classfunction.SetFunction;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.expr.MultiArgs;
import wncalculus.expr.Sort;
import wncalculus.expr.UnaryOp;
import wncalculus.guard.And;
import wncalculus.guard.Equality;
import wncalculus.guard.Guard;
import wncalculus.guard.NaryGuardOperator;
import wncalculus.guard.True;

public final class ColorRestriction implements FunctionTuple, UnaryOp<FunctionTuple> {

    private final FunctionTuple arg;
    private boolean simplified;
    private final Set<? extends ColorClass> ccset;
    private final Domain cod;
    
    /**
     * constructor: creates the color restriction of a function-tuple 
     * semantically the restriction is the projection on the other colors of the codomain
     * @param t a function-tuple
     * @param sc a set of sorts
     */
    private ColorRestriction(FunctionTuple t, Set<? extends ColorClass> sc, Domain d) {
        this.arg = t;
        this.ccset = sc;
        this.cod = d;
    }

    /**
     * @param t a function-tuple
     * @param sc a set of sorts
     * @return the color restriction of t, i.e., the projection of t on the other colors of the codomain
     * (<code>this</code>, in the event).
     */
    public static FunctionTuple factory(FunctionTuple t, Set<? extends ColorClass> sc) {
        if (sc.isEmpty())
           return t;

        {
        Domain cd = t.getCodomain(), r = cd.restriction(sc);
        return cd.size() > r.size() ?  new ColorRestriction(t, sc, r) : t;
        }
    }

    @Override
    public Domain getDomain() {
        return this.arg.getDomain();
    }
    
    @Override
    public Domain getCodomain() {
        return cod;
    }
    
    @Override
    public boolean differentFromZero() {
        return this.arg.differentFromZero();
    }

    @Override
    public Map<Sort, Integer> splitDelimiters() {
        return this.arg.splitDelimiters();
    }

    @Override
    public boolean simplified() {
        return this.simplified;
    }

    @Override
    public void setSimplified(boolean simplified) {
        this.simplified = true;
    }

    @Override
    public Class<? extends FunctionTuple> type() {
        return ColorRestriction.class;
    }

    @Override
    public FunctionTuple getArg() {
        return this.arg;
    }

    @Override
    public FunctionTuple buildOp(FunctionTuple arg) {
        return ColorRestriction.factory(arg, this.ccset);
    }

    @Override
    public String symb() {
        return "R";
    }

   @Override
    public boolean isDistributive (Class<? extends MultiArgs> optk) {
        return  optk.equals(TupleSum.class);
    }

    @Override
    public boolean isInvolution() {
        return false;
    }

    @Override
    public FunctionTuple specSimplify() {
        if (this.arg.getClass() == Tuple.class) {
            final Tuple t = (Tuple) this.arg;
            final Guard ft = t.filter();
            if (ft.isAndForm() && ft.membMap().isEmpty()) {
                final Map<ColorClass, Map<Boolean, SortedSet<Equality>>> eqMap = ft.equalityMap();
                final HashSet<Equality> eqset = new HashSet<>();    
                final SortedMap<ColorClass, List<? extends SetFunction>> m = new TreeMap<>();
                for (final Entry<ColorClass, List<? extends SetFunction>> x : t.getHomSubTuples().entrySet()) {
                    if (!this.ccset.contains(x.getKey())) {
                        m.put(x.getKey(), x.getValue());
                        final Map<Boolean, SortedSet<Equality>> eq = eqMap.get(x.getKey());
                        if (eq != null) {
                            eqset.addAll(eq.getOrDefault(true, Collections.emptySortedSet()));
                            eqset.addAll(eq.getOrDefault(false, Collections.emptySortedSet()));
                        }
                }
            }

            return  new Tuple(eqset.isEmpty() ? True.getInstance(cod) : And.factory(NaryGuardOperator.cloneArgs(eqset, cod)), m, t.guard());
            }
        }
        
        return this;
    }

    @Override
    public String toString() {
        return UnaryOp.super.toStringOp() + "_{" + this.ccset + "}";
    }

}
