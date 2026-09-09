package wnbag;

import java.util.*;
import java.util.Map.Entry;
import bagexpr.BagExpr;
import bagexpr.UnaryBagOp;
import color.ColorClass;
import expr.Domain;
import util.Util;
import wncolorfunction.ColorFunction;

/**
 * This class represents an expansion of an arc function (bag of WNtuple) based on specified positions of color functions to be expanded.
* The arc function is supposed to be single color (no check done).
*  It is a kind of a unary operator that takes an arc function and a map of positions to expand, and produces a new arc function with the specified expansions.
* @author lorenzo capra
 */

public final class ArcFunExpansion extends UnaryBagOp<WNtuple> implements  ArcFunction {

    private final Map<Integer, ColorFunction> mapExpand; // positions of the class functions to be expanded
    private Domain codomain = null; // cache for the codomain, initialized to null
    
    private ArcFunExpansion(ArcFunction bagexpr, Map<Integer, ColorFunction> mapPositions) {
        super(bagexpr);
        this.mapExpand = Collections.unmodifiableMap(mapPositions);
    }


    public static ArcFunction factory(ArcFunction bagexpr, Map<Integer, ColorFunction> mapPositions) {
        if (mapPositions.isEmpty()) {
            return bagexpr; // if there are no positions to expand, return the wrapped bag expression
        }
        return new ArcFunExpansion(bagexpr, mapPositions);
    }
    
    
    @Override
    public Domain getCodomain() {
        if (this.codomain == null) {
            final Domain codom = getArg().getCodomain();
            final ColorClass cc = (ColorClass) codom.support().iterator().next(); // assuming single color class for the arc function
            this.codomain = new Domain(cc, codom.mult(cc) + mapExpand.size()); // cache the computed codomain
            }

        return this.codomain;
    }

    public String toString() {
        return symb() + getArg().toString() + ", " + mapExpand.toString() + symb();
    }

    public ArcFunction specSimplify() {
        final var arg = getArg(); 
        
        if (arg instanceof WNtuple tuple) {
             ColorClass cc = tuple.getSort();
             List<? extends ColorFunction> copy = tuple.getHomSubTuple(cc);
             final List<? extends ColorFunction> expanded = Util.expandPositions(copy, this.mapExpand); // expand the positions in the copy of the homogenous sub-tuples
             return new WNtuple(Util.singleSortedMap(cc, expanded), tuple.guard()); // return a new WNtuple with the expanded positions
        }

        if (arg instanceof TupleBag tb) {
            final List<ArcFunction> expandedTuples = new ArrayList<>();
            for (Entry<? extends WNtuple, Integer> entry : tb.asMap().entrySet()) {
                ArcFunction scalar = ArcFunScalar.factory(new ArcFunExpansion(entry.getKey(), this.mapExpand), entry.getValue());
                expandedTuples.add(scalar);
            }            
            return ArcFunSum.factory(expandedTuples);
        }
           
        if (arg instanceof ArcFunScalar sc) { // mettiamo fuori il coefficiente
            return ArcFunScalar.factory(new ArcFunExpansion(sc.getArg().cast(), this.mapExpand), sc.k());
        }

        return this; // otherwise, no simplification is performed
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ArcFunExpansion other = (ArcFunExpansion) obj;
        return Objects.equals(getArg(), other.getArg()) && Objects.equals(mapExpand, other.mapExpand);
    }

    public int hashCode() {
        return Objects.hash(getArg(), mapExpand);
    }


    @Override
    public boolean isInvolution() {
        return false;
    }


    @Override
    public BagExpr<WNtuple> buildOp(BagExpr<WNtuple> arg) {
        return new ArcFunExpansion(arg.cast(), this.mapExpand);
    }


    @Override
    public String symb() {
        return "$";
    }

}
