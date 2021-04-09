package wncalculus;

import java.util.*;
import wncalculus.bagexpr.LogicalBag;
import wncalculus.color.ColorClass;
import wncalculus.expr.Domain;
import wncalculus.expr.Interval;
import wncalculus.tuple.*;
import wncalculus.classfunction.*;
import wncalculus.guard.*;
import wncalculus.wnbag.FunctionTupleBag;


/**
 * this class collects some test sessions of library's methods involved
 * in the computation of SODE
 * @author lorenzo capra
 */
public class TestForSode {

    /**
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        Interval i1 = new Interval(3,3), //intervallo formato da 3 elementi
                 i2 = new Interval(4,4); //intervallo formato da 4 elementi
        //color-class
        ColorClass c1 = new ColorClass("X", new Interval[] {i1,i2}); // color class di nome "X" partizionata in due sottoclassi
                                                                     // ciascuna di card. 3
        //class-functions
        Projection x2 = Projection.builder (2,c1),
                   x1 = Projection.builder (1,c1),
                   x3 = Projection.builder (3,c1);
        SetFunction px1 = ProjectionComp.factory(x1).cast(); // (S - x1)
                        
        Subcl sc1 = Subcl.factory(1, c1); // S{C1}
   
        //domini
        Domain d1 = new Domain(c1,c1),    // d1 : c1 x c1
               d2 = new Domain(c1,c1,c1); // d2 : c1 x c1 x c1
        
        //guardie-filtri
        Equality e1 = (Equality) Equality.builder(x1, x2, true, d1), // [x1 = x2]
                 e2 = e1.opposite(), // [x1 != x2], con dom d1
                 e3 = (Equality) Equality.builder(x1, x2, false, d2); // [x1 != x2], con dom d2
                 
        Membership memb1 = Membership.build(x1, sc1, d1),  // [x1 in C{1}]
                   memb2 = (Membership) Membership.build(x2, sc1, false, d1); // [x1 notin C{1}]
        Guard g = And.factory(memb1, memb2); // [x1 in C{1}, x2 notin C{1}]
        
        SetFunction[] farr = new SetFunction[] {px1,px1};
        List<SetFunction> flist = new ArrayList<>(Arrays.asList(farr)); //lista di funzionis
        
// tuple di funzioni, tutte con dominio d2
        Tuple t1 = new Tuple(d2, x2, px1), // t1: <x2, S-x1>
              t2 = new Tuple(d2, x1, x2),  // t2: <x1, x2>
              t3 = new Tuple(d2, x2, x2),  // t3: <x2, x2>
              t4 = new Tuple(d2, sc1, x2), // t4: <S{C1}, x2>
              t5 = new Tuple(d2, x1, x3), // t5: <x1, x3>
              t6 = new Tuple(g, flist, e3,null); // t6: [x_1 in X{1},x_2 in X{2}]<S-x1,S-x1>[x_1 != x_2]
        FilteredTuple ft1 = new FilteredTuple(e1, t1); // ft1: [x1 = x2] (<x2, S-x1>)
        
        
        FilteredTuple ft2 = new FilteredTuple(e1,t2), // ft1: [x1 = x2] (<x2, S-x1>)
                      ft3 = new FilteredTuple(g,t4), // ft2: [x1 = x2] (<x1, x2>)
                      ft4 = new FilteredTuple(g,t5); // ft3: [x1 in C{1}, x2 notin C{1}](<S{C1}, x2>)
        // arc-functions (bags)
        FunctionTupleBag builder = new FunctionTupleBag(t1,1);
        LogicalBag<FunctionTuple> in1 = (LogicalBag<FunctionTuple>) builder.build(t1, t1, t2), // in1: 2<x2, S-x1> + 1<x1, x2>
                                  in2 = (LogicalBag<FunctionTuple>) builder.build(ft1, ft1, ft2), // in2: 2[x1 = x2](<x2, S-x1>) + 1[x1 = x2](<x1, x2>)
                                  in3 = (LogicalBag<FunctionTuple>) builder.build(t1, t1, t3), // in3: 2<x2, S-x1> + 1<x2, x2>
                                  in4 = (LogicalBag<FunctionTuple>) builder.build(t4, t5), // in4: 1<S{C1}, x2> + 1<x1, x3>
                                  in5 = (LogicalBag<FunctionTuple>) builder.build(ft3, ft4), // in5: 1[x1 in C{1}, x2 notin C{1}](<S{C1}, x2>) + 1[x1 in C{1}, x2 notin C{1}](<x1, x3>)
                                  in6 = (LogicalBag<FunctionTuple>) builder.build(t6, t6, t5, t5); // 2[x1 in X{1},x2 in X{2}]<S-x1,S-x1>[x_1 != x_2]+2<x_1,x_3>
        
        LogicalBag<?>[] input_array = {in1, in2, in3, in4, in5, in6}; // creiamo una lista di funzioni di input
        List<LogicalBag<FunctionTuple>> input_list = new ArrayList<>();
        for (LogicalBag<?> b : input_array)
            input_list.add((LogicalBag<FunctionTuple>) b);
        
        List < Map<Guard, Integer> > mapList = new ArrayList<>();
    
        // normalizziamo le funzioni e calcoliamo le mappe guardie -> coefficienti per ciascuna
        for (LogicalBag<FunctionTuple> f : input_list) {
            System.out.println("-----*****-----\ninput arc function: "+ f);
            f = (LogicalBag<FunctionTuple>) f.normalize();
            System.out.println("normalized form: "+ f);
            LogicalBag<FunctionTuple> fn = (LogicalBag<FunctionTuple>) f.normalize(/*true*/);
            System.out.println("normalized disjoint form: "+ fn);
            for (FunctionTuple tx : fn.support())
                if (tx instanceof Tuple)
                    System.out.println("cardinality of "+tx+": "+((Tuple)tx).cardLb());
            Map<Guard, Integer> map = f.mapGuardsToMaxCoefficients();
            System.out.println("corresponding map guards -> (max) coefficients: "+ map);
            mapList.add(map);
        }
        
        // calcoliamo il prodotto cartesiano delle mappe (i domini sono coerenti)
        Map<Guard, List<Integer>> prod = LogicalBag.product(mapList);
        System.out.println("-----*****-----\nCartesian product of maps:\n"+prod);
        FunctionTuple trt4 = (FunctionTuple) new TupleTranspose(t4).normalize(true); // trasposta di t4 (normalizzata)
        System.out.println("-----*****-----\nthe transpose of "+t4+" is: "+trt4);
        System.out.println("we apply the guards of the product to ttr4:");
        System.out.println(trt4.applyFilters(prod.keySet()));
    }
      
}
