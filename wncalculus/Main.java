package wncalculus;

import java.io.IOException;
import wncalculus.classfunction.*;
import wncalculus.tuple.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import wncalculus.bagexpr.*;
import wncalculus.expr.*;
import wncalculus.graph.Graph;
import wncalculus.graph.InequalityGraph;
import wncalculus.guard.*;
import wncalculus.color.ColorClass;
import wncalculus.logexpr.LogicalExprs;
import wncalculus.util.Util;
import wncalculus.wnbag.FunctionTupleBag;
import wncalculus.wnbag.LinearComb;
import wncalculus.wnbag.WNtuple;

/**
 * This class embeds some examples of use of library's type and methods
 * @author lorenzo capra
 */
public class Main {
   
   
   static final Interval
                   i1 = new Interval(2,2),
                   i2 = new Interval(1,1),
                   i3 = new Interval(2),
                   i4 = new Interval(3,6),
                   i5 = new Interval(3),
                   i6 = new Interval(4,4),
                   i7 = new Interval(1),
                   i = new Interval(3,3);

   static /*final*/ ColorClass 
                     c1 = new ColorClass(1,i3/*i5*/,true),
                     c1bis = new ColorClass(1,i4,true), //compatible with c1
                     c2 = new ColorClass(2,new Interval[] {i2,i2,i7}), // split
                     c0ord = new ColorClass(2,new Interval[] {i6,i2,i2}, true), // split and ordered
                     c3 = new ColorClass(3,i4),
                     c4 = new ColorClass(4,i5),
                     c5 = new ColorClass(5,i5,false),
                     c5bis = new ColorClass(5,i4,false), //compatible with c5
                     c2bis = new ColorClass(2,i3), // NON sameName with c2
                     c3bis = new ColorClass(3,i5), // sameName with c3
                     X = new ColorClass("X",i,true), 
                     c6 = new ColorClass(6,i6,true), // a fixed-k color class of card 4;
                     C = new ColorClass("C",new Interval[] {i,i}),
                     c_neutral = new ColorClass("N", new Interval[] {i2}); // neutral class
    
    
     /*
    a comparator for equalities (assumed homogenous and of the same sign) considering
    first the indices then the successors
    */
    private static final Comparator<Equality> COMPEQ = (e1, e2) -> {
        if (e1 == e2)
            return 0; //just for efficiency
        
        int c = e1.firstIndex().compareTo(e2.firstIndex());
        if (c!= 0)
            return c;
           
        if ( (c  = e1.secondIndex().compareTo(e2.secondIndex())) != 0 || ! e1.getSort().isOrdered())
            return c;
        
        return e1.getSucc().compareTo(e2.getSucc());
        
    };
    
    private static void test0 () {
        Projection f = Projection.builder (1,X) , 
                   sf = Projection.builder (1,1,X);
        SetFunction fc=ProjectionComp.factory(f).cast(), sfc = ProjectionComp.factory(sf).cast(),
                    succ = Successor.factory(2,sfc), un = Union.factory(false, fc, succ);
        Domain d = new Domain(X);
        Tuple t = new Tuple(d,un);
        System.out.println(un);
        System.out.println(un.normalize().toStringDetailed());
        //System.out.println(t.simplify());
        
    }
    
    /**
     * run some tests for the library's types and methods (especially, normalization ones)
     * @param args command line arguments (not used)
     */
    public static void main(String[] args)  {
       /*try {
           Runtime.getRuntime().exec(new String[] { "java", "-jar", "E:\\lorenzo\\CLISVN\\SNexpressions\\SNexCLI.jar" });
           return;
       } catch (IOException ex) {
           Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
       }*/
        
        /*int n = Integer.MAX_VALUE;
        for (int i = 0; i < 32; i++)
            System.out.print(Util.checkBit(n, i) ? "1" : "0");
        System.out.println();*/
        
        petriNets20();
        
        test0();
        
        testBag();
        //if (true) return;
        
        testGuard();
        Util.getChar();
        //test of new, possibly ordered, split classes
        /*Integer[] iset = {1};
        Subcl ordsc = (Subcl) Subcl.factory(new HashSet<>(Arrays.asList(iset)),  c0ord, new Interval(1,2));
    
        System.out.println(ordsc);
        Util.getChar();*/
        
        /*try {testExtra(); }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }*/
        //System.exit(0);
        
        testGraph();
        Util.getChar();
        SetFunction f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f14,f15,f16,f17,f18,f1c; 
        
        Collection<? extends ParametricExpr> cf1, cf2, cf3;
        
        //testGuard();
        //testInequalitygraph();
        testBasicComposition();
        //System.exit(0);
       //test of neutral class
        SetFunction xn /*= Projection.builder(2, c_neutral)*/; //raises an exception
        //System.out.println("normalizzo "+xn+':'+xn.normalize());
        xn = All.getInstance(c_neutral);
        System.out.println("normalizzo "+xn+':'+xn.normalize());
        xn = Complement.factory(xn);
        System.out.println("normalizzo "+xn+':'+xn.normalize());
        Util.getChar();
       
        f1 = Projection.builder (1,c1);
        f1c=Complement.factory(f1);
        System.out.println("ecco il complemento di "+f1);
        /*System.out.println(Util.toStringDetailed(*/cf1= f1c.simplify()/*))*/;
        f2 = (SetFunction) cf1.iterator().next();
        f18= (SetFunction) f1.orFactory(false, f1,f1c);
        System.out.println("SEMPLIFICO "+f18+':');
        System.out.println(Expressions.toStringDetailed(f18.simplify()));
        System.out.println("ecco il complemento di "+f2 +'\n'+Expressions.toStringDetailed((f2 = Complement.factory(f2)).simplify()));
        System.out.println("ecco il complemento di "+f2 +'\n'+Expressions.toStringDetailed(Complement.factory(f2).simplify()));  
        f2 = f1;
        f15= Successor.factory(1,(Projection)f2);
        System.out.println("SEMPLIFICO "+(f15=Successor.factory(-4,f15))+'\n'+ Expressions.toStringDetailed(cf3 = f15.simplify()));
        f3 = All.getInstance(c1);
        f4 = Empty.getInstance(c1);
        //f4 = ProjectionComp.factory(2,cc1);
        List<SetFunction> f_list = new ArrayList<>();
        f_list.add(ProjectionComp.factory(Projection.builder(1,2,c1)).cast());
        f_list.add(f1);
        f_list.add(f1);
        f_list.add(Successor.factory(3,f1));f_list.add(f3);
        f_list.add(Projection.builder (2,c1));
        //f_list.add(f18);
        f5 = Successor.factory(1,Intersection.factory(f_list));     
        System.out.println("semplifico *** "+f5.toStringDetailed());
        System.out.println(Expressions.toStringDetailed(f5.simplify()));
        
        f_list.add(f4);
        f5 = Intersection.factory(f_list);
        System.out.println("semplifico "+f5.toStringDetailed());
        System.out.println(Expressions.toStringDetailed(f5.simplify()));
        
        f6= Projection.builder(1,-4,c1);
        System.out.println("semplifico "+f6.toStringDetailed());
        System.out.println(Expressions.toStringDetailed(f6.simplify()));
        System.out.println("semplifico "+(f7=ProjectionComp.factory((Projection)f6).cast()));
        System.out.println(Expressions.toStringDetailed(f7.simplify()));

        System.out.println(f8= Successor.factory(2,f7));
        System.out.println(Expressions.toStringDetailed(f8.simplify()));
        System.out.println(f9=Complement.factory(f8));
        System.out.println(Expressions.toStringDetailed(f9.simplify()));
        System.out.println((f10=Complement.factory(f9)));
        System.out.println(Expressions.toStringDetailed(f10.simplify()));

        System.out.println(f11= Successor.factory(-1,f10));
        System.out.println(Expressions.toStringDetailed(f11.simplify()));
        System.out.println(f12= Successor.factory(1,f11));
        System.out.println(Expressions.toStringDetailed(f12.simplify()));
        System.out.println(f13=Complement.factory(f12));
        System.out.println(Expressions.toStringDetailed(f13.simplify())); 

        f14=Complement.factory(Union.factory(false, f12,f13));
        System.out.println(f14);
        System.out.println(Expressions.toStringDetailed(f14.simplify()));
        
        SetFunction e;
        System.out.println((e= Complement.factory(All.getInstance(c1)))+"\ntermine da semplificare");
        System.out.println(Expressions.toStringDetailed(e.simplify()));
        Subcl s2 = Subcl.factory(2, c2), s1 = Subcl.factory(1, c2), s3 = Subcl.factory(3, c2);
        System.out.println((f18=Complement.factory(s2))+"\n"+ Expressions.toStringDetailed(f18.simplify()));
        System.out.println((f1.orFactory(false,f18,s1))+"\n"+Expressions.toStringDetailed(f1.orFactory(false,f18,s1).simplify()));
        System.out.println((Union.factory(false,Projection.builder (2,c2),s1,s2,s3))+"\n"+Expressions.toStringDetailed(Union.factory(false,Projection.builder (2,c2),s1,s2,s3).simplify()));
        System.out.println((f1.andFactory(s1,f18))+"\n"+Expressions.toStringDetailed(f1.andFactory(s1,f18).simplify()));
        System.out.println((Intersection.factory(true,Projection.builder (2,c2),s1,s2))+"\n"+Expressions.toStringDetailed(Intersection.factory(true,Projection.builder (2,c2),s1,s2).simplify()));

        //System.out.println(f1+" intersez "+ f2 + " implies " + f1 + " :" + LogicalExprs.implies(f1.andFactory(f1, f2), f1));
        //System.out.println(f2 + " implies " + f1+" intersez "+ f2 + " :" + LogicalExprs.implies(f2,f1.andFactory(f1, f2)));

        f_list.remove(f4);
        f5 = f1.andFactory(f_list);
        System.out.println("splitdelim di " + f5+ ':'+f5.splitDelim());
        System.out.println("semplifico "+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));
        //System.out.println("f5 dopo sostituzione !X_2 -> X_1 : " + f5.facotory(Projection.factory(2,1,c1), Projection.factory(1,c1)));
        //nuovo
        f5 = Union.factory(false,f5,Complement.factory(Projection.builder (1,c1)),Complement.factory(Projection.builder (2,c1)));
        System.out.println(f5.getSort());
        //System.out.println("splitdelim di " + f5+ ':'+f5.splitDelim());
        System.out.println("semplifico "+ f5);
        System.out.println(Expressions.toStringDetailed(f5.simplify()));
        System.out.println("\nsemplifico "+ Complement.factory(f5));
        System.out.println(Complement.factory(f5).simplify());
        System.out.println("semplifico "+f1.andFactory(Complement.factory(f5), All.getInstance(f5.getSort())));
        System.out.println(Expressions.toStringDetailed(f1.andFactory(Complement.factory(f5), All.getInstance(f5.getSort())).simplify()));
        //System.out.println("f5 equiv a S : "+Util.equivalent(f5, new All (f5.getSort())));

        f14 = Projection.builder(1,1,c1);
        f_list.add(f14);
        f5 = f1.andFactory(f_list);
        //System.out.println("*** card di "+f5+" : "+f5.card());
        System.out.println("semplifico "+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));
             
        f_list.removeAll(Collections.singleton(f1));
        f_list.add(ProjectionComp.factory(Projection.builder (2,-1,c1)).cast());
        f_list.add(ProjectionComp.factory(Projection.builder (1,c1)).cast());
        f5 = (SetFunction) f1.orFactory( false, Projection.builder (1,c1), Projection.builder (1,1,c1));
        System.out.println("*** card di "+f5+" : "+f5.card());
        System.out.println("semplifico\n"+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));
        
        f5 = f1.andFactory(f_list);
        //System.out.println("somma dei max succ exp in " + f5+ ':'+((Intersection)f5).splitDelim());
        System.out.println("semplifico "+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));
        f_list.add(Projection.builder (2,c1));
        f5 = f1.andFactory(f_list);
        System.out.println("*** card di "+f5+" : "+f5.card());
        System.out.println("semplifico\n"+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));

        f_list.remove(f14);
        f_list.add(ProjectionComp.factory(Projection.builder(1,1,c1)).cast());
        f5 = f1.andFactory(f_list);
        //System.out.println("*** forma semplificata? " + ((Intersection)f5).hasSimplifiedForm() );
        //System.out.println("*** parti con lo stesso index-set: " +( (Intersection)f5).index_separated_args() );
        System.out.println("semplifico\n"+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));
        //System.out.println("constraint di "+ f5+ ": "+f5.card());
        //System.exit(0);
        
        f_list.add(Projection.builder(1,1,c1));
        f_list.add(ProjectionComp.factory(Projection.builder (2,c1)).cast());
        f5 = f1.andFactory(f_list);
        System.out.println("*** card di "+f5+" : "+f5.card());
        System.out.println("semplifico\n"+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));
        //System.out.println("constraint di "+ f5+ ": "+f5.card());

        f_list.clear();
        f2 = Projection.builder (2,c2); f1 = Projection.builder (1,c2);
        SetFunction pc1 = ProjectionComp.factory((Projection)f1).cast(),
                        pc2 = ProjectionComp.factory((Projection)f2).cast();
        
        f6 = (SetFunction) f1.andFactory(pc1,f2);
        f7 = Union.factory(false,f6,/*pc1*/f1);
        
        System.out.println("normalizzo\n"+ f7+ "\n"+Expressions.toStringDetailed(f7.simplify()));
        Tuple t1,t2,t3;
        Domain d2 = new Domain(c2,c2), d1;
        t1 = new Tuple(null, null, d2, f6); t2 = new Tuple(null, null, d2, f7);
        TupleSum ts = (TupleSum) TupleSum.factory(false,t1,t2);
        System.out.println("semplifico\n"+ ts+ "\n"+Expressions.toStringDetailed(ts.simplify()));
        t1 = new Tuple(null, null, d2,f1); t2 = new Tuple(null, null,d2,f2);
        
        ts = (TupleSum) TupleSum.factory(false,t1,t2);
        System.out.println("semplifico\n"+ ts.toStringDetailed()+ "\n"+Expressions.toStringDetailed(ts.simplify()));
        System.out.println("trasposta di "+ts+ "\n"+Expressions.toStringDetailed(new TupleTranspose(ts).simplify()));
        //WNtupleBag b1 = new WNtupleBag(t1, 2);
        BagExpr<FunctionTuple> b1 = new FunctionTupleBag(Util.singleMap(t1, 2));
        //WNtupleBag b2 = new WNtupleBag(t2, 1), b3;
        BagExpr<FunctionTuple> b2 = new FunctionTupleBag(Util.singleMap(t2, 2)), b3;
        BagExpr<FunctionTuple> s = BagSum.factory(b1,b2);
        System.out.println(s);
        System.out.println("normalizzo:");
        LogicalBag<FunctionTuple> tbag = (LogicalBag<FunctionTuple>) s.normalize();
        System.out.println(tbag);
        Map<Guard, Integer> m = tbag.mapGuardsToMaxCoefficients();
        System.out.println("map guardie-> coefficienti: "+m);
        System.out.println("prodotto Cartesiano m x m x m:\n"+LogicalBag.product(m,m,m));
        Util.getChar();
        
        System.out.println("trasposta\n:"+new BagTranspose(s).normalize());
        BagExpr<FunctionTuple> diff = BagDiff.build(s, b2);
        System.out.println("normalizzo "+diff+'\n'+diff.normalize());
        System.out.println("supporto di "+diff+'\n'+new TupleSupport(diff)./*simplify()*/normalize());
        Util.getChar();
        
        FunctionTuple ris = new TupleComposition(new TupleTranspose(new TupleSupport(diff)), new TupleSupport(b2), true);
        System.out.println("semplifico\n"+ ris);
        System.out.println(ris.simplify());
        
        BagComp<FunctionTuple> bc = new BagComp(new BagTranspose<>(diff), b2);
        System.out.println("normalizzo\n"+ bc);
        System.out.println(bc.normalize());
        Util.getChar();
        
        System.out.println("transposing: "+ new BagTranspose(s));
        LogicalBag bt = (LogicalBag) new BagTranspose(s).normalize();
        System.out.println(bt);
        System.out.println("map guardie-> max coeff: "+bt.mapGuardsToMaxCoefficients());
        System.out.println("diff between: "+b1+" and "+b2+'\n'+BagDiff.build(b1,b2).normalize());
        Util.getChar();
        
        t3 = new Tuple(null, null,d2, f6, f7);
        b3 = new FunctionTupleBag(Util.singleMap(t3, 2));
        BagComp<FunctionTuple> mset_c = new BagComp<>(b2,b3);
        System.out.println("compose: "+mset_c+"\nsemplifico:");
        BagExpr<FunctionTuple> b = (BagExpr<FunctionTuple>) mset_c.normalize();
        System.out.println(b);
        Util.getChar();
        
        Tuple t4 = new Tuple(null, null,d2, f2, pc1), t5 = new Tuple(null, null,d2, f1, f2), t6;
        //System.out.println("T4: "+t4);
        LogicalBag<FunctionTuple> b4 = (LogicalBag<FunctionTuple>) mset_c.build(t4, t4, t5);
        System.out.println("b4: "+b4);
        System.out.println("b4 normalizzato: "+b4.normalize());
        System.out.println("map guardie-> max coeff: "+b4.mapGuardsToMaxCoefficients());
        Equality e1 = (Equality) Equality.builder((Projection) f1, (Projection)f2, true, d2),
                 e2 = e1.opposite();
        LogicalBag<FunctionTuple> b5 = (LogicalBag<FunctionTuple>) mset_c.build(new FilteredTuple(e1,t4), new FilteredTuple(e1,t4), new FilteredTuple(e1,t5));
        System.out.println("b5: "+b5);
        System.out.println("b5 normalizzato: "+ (b5 = (LogicalBag<FunctionTuple>) b5.normalize()));
        System.out.println("map guardie-> max coeff: "+b5.mapGuardsToMaxCoefficients());
        t6 = new Tuple(null, null,d2, f2, f2);
        LogicalBag<FunctionTuple> b6 = (LogicalBag<FunctionTuple>) mset_c.build(t4, t4, t6);
        System.out.println("b6: "+b6);
        System.out.println("b6 normalizzato: "+b6.normalize());
        System.out.println("map guardie-> max coeff: "+b6.mapGuardsToMaxCoefficients());
        
        
        System.out.println("transpose: "+new BagTranspose(mset_c) .normalize());
        Util.getChar();
        
        f_list.add(pc1);
        f_list.add(pc2);
        f_list.add(Subcl.factory(1, c2));
        f5 = f1.andFactory(f_list);
        System.out.println("semplifico\n"+ f5.toStringDetailed()+ "\n");
        System.out.println(f5.simplify());
        //Util.getChar();
        Tuple tx = new Tuple(null, Collections.singletonList(f5), null, d2);
        System.out.println("semplifico\n"+ tx+ "\n"+Expressions.toStringDetailed(tx.simplify()));
        Util.getChar();
        
        f_list.clear();
        f1=Projection.builder (2,c1);
        //f_list.add(ProjectionComp.factory((Projection)f1));
        //f_list.add(/*ProjectionComp.factory(*/(Projection) f1)/*)*/;
        f_list.add(ProjectionComp.factory(Projection.builder (2,1,c1)).cast());
        f_list.add(ProjectionComp.factory(Projection.builder (1,c1)/*(Projection) f1)*/).cast());
        f_list.add(ProjectionComp.factory(Projection.builder (1,1,c1)).cast());
        //f_list.add(Projection.factory (3,c1));
        //f_list.add(f1);
        f5 = f1.andFactory(f_list);
        System.out.println("forma right composable di "+f5);
        //for (SetFunction.GuardedFunction gf : ((Intersection)(f5)).toGuardedForm(new True(f5.inferDomain())).toRightComposableSet()) 
            //System.out.println(gf);
        System.out.println("semplifico\n"+ f5+ "\n"+Expressions.toStringDetailed(f5.simplify()));
        //System.out.println("constraint di "+ f5+ ": "+f5.card());

        f1=Projection.builder (1,c1);
        f2= Successor.factory(1,(Projection)f1);
        f4=(SetFunction) f1.andFactory(f1,f2);
        System.out.println("semplifico\n"+f4+'\n'+Expressions.toStringDetailed(f4.simplify()));
        System.out.println("*** card di "+f4+" : "+f4.card());
        //System.out.println("constraint di "+ f1+ ": "+f1.card());

        f3= ProjectionComp.factory(Projection.builder(2,1,c1)).cast();//S-!X_2
        f6 = (SetFunction) f1.orFactory( false,f3,f2);
        f7 = (SetFunction) f1.andFactory(f3,f2);
        f4 = (SetFunction) f1.andFactory(f1, f6);
        f8= Projection.builder(2,c1);
        f9= (SetFunction) f1.orFactory(false, f8,f1 );
        f10 = (SetFunction) f1.andFactory(f9,f6);

        System.out.println("semplifico\n"+ f10);
        System.out.println(Expressions.toStringDetailed(f10.simplify()));
        System.out.println("semplifico\n"+ f4+"\n"+(cf3=f4.simplify()));
        System.out.println("semplifico compl. di \n"+ f6+"\n"+ Expressions.toStringDetailed(Complement.factory(f6).simplify()));
        //NEW
        System.out.println("semplifico compl. di \n"+ f7+"\n"+ Expressions.toStringDetailed(Complement.factory(f7).simplify()));

        f9 = (SetFunction) f1.orFactory(false,ProjectionComp.factory((Projection) (f1=Projection.builder (1,c1))).cast(),f1.andFactory(Projection.builder(2,1,c1),f1));
        System.out.println(f9);
        //System.out.println(f9  + " equivalente a " + (f10 = Complement.factory(f4)) + '\n'+Util.equivalent(f9,Complement.factory(f4)));

        System.out.println("ecco l'unione (semplificata) di "+f9 + " e " + f10);
        System.out.println(Expressions.toStringDetailed(f1.orFactory(false,f9,f10).simplify()));
        System.out.println("ecco l'intersezione (semplificata) di "+f9 + " e " + f10 +'\n'+Expressions.toStringDetailed(f1.andFactory(f9,f10).simplify()));

        f5 = Complement.factory(f4);
        System.out.println("semplifico\n"+ f5+"\n"+Expressions.toStringDetailed(f5.simplify()));
        System.out.println("ho semplificato\n"+ f5);
        f16 = (SetFunction) f1.andFactory (f10,f9);
        System.out.println("semplifico\n"+ f16+"\n"+f16.simplify());
        f17 = (SetFunction) f1.orFactory(false,f5,f16 );

        System.out.println("semplifico\n"+ f17);
        System.out.println("splitdelim: "+ f17.splitDelim()); 

        System.out.println(Expressions.toStringDetailed((cf2=f17.simplify())));//critical
        
        f11 = Complement.factory(f17);
        System.out.println("semplifico\n"+ f11+"\n"+Expressions.toStringDetailed(f11.simplify()));
        
        System.out.println("---testing bags---");
        testBag();
        
    }
    
    /**
     *
     */
    public static void testBasicComposition()  {
        Projection p1_0 = Projection.builder (1,c1),
                   p1_1 = Projection.builder (1,/*4*/1,c1),
                   p2 = Projection.builder (2,c1),
                   p3_0 = Projection.builder (3,c1);
        SetFunction /*p1c=Complement.factory(p1), p2c = Complement.factory(p2),*/  sp1c;
        SetFunction p1c = ProjectionComp.factory(p1_1).cast(),
                        p2c = ProjectionComp.factory(p2).cast(), p0c = ProjectionComp.factory(p1_0).cast();
       
        SetFunction sp1 = Successor.factory(1,p1_1);
        sp1c = Complement.factory(sp1);
        Intersection inter1 = (Intersection) p1_1.andFactory(p1_1,p0c);
        
        SetFunction f;
        ClassComposition bc = new ClassComposition(Successor.factory(1,p1_1),sp1,true);
        System.out.println("********composizione di class-functions********\n");
        System.out.println("SEMPLIFICO "+bc+':');
        System.out.println(Expressions.toStringDetailed(bc.simplify()));
        System.out.println("SEMPLIFICO "+bc+':');
        System.out.println(Expressions.toStringDetailed(bc.simplify()));
        
        f = (SetFunction) p1_1.andFactory(p1c, Successor.factory(2,p1c));
        bc = new ClassComposition(f, (SetFunction) p1_1.orFactory(false,f,inter1),true);
        System.out.println("SEMPLIFICO "+bc+':');
        System.out.println(Expressions.toStringDetailed(bc.simplify()));
        //Util.getChar();
        
        bc = new ClassComposition(bc,sp1c,true);
        System.out.println("SEMPLIFICO "+bc+':');
        System.out.println(Expressions.toStringDetailed(bc.simplify()));
        
        f = (SetFunction) p1_1.andFactory(p1c,sp1c);
        bc = new ClassComposition(f,sp1c,true);
        System.out.println("SEMPLIFICO "+bc+':');
        System.out.println(Expressions.toStringDetailed(bc.simplify()));
        
        Subcl sc3 = Subcl.factory(3, c2);
        Projection    p4  = Projection.builder (2,c2),  p3 = Projection.builder (3,c2), p5 = Projection.builder (3,c2);
        SetFunction p4c = ProjectionComp.factory(p4).cast(),
                   p3c =ProjectionComp.factory(p3).cast(), all = All.getInstance(p3.getSort()) ;
        bc = new ClassComposition(sc3,p4c,true);
        System.out.println("SEMPLIFICO "+bc+':');
        System.out.println(Expressions.toStringDetailed(bc.simplify()));
        f = (SetFunction) p1_1.andFactory(p3c,p4c);
        bc = new ClassComposition(p3c,f,true);
        System.out.println("SEMPLIFICO "+bc+':');
        System.out.println(Expressions.toStringDetailed(bc.simplify()));
        
        System.out.println("\n******** tuple ***********\n");
        Domain d1 = new Domain(c1,c1,c2,c2,c2) /*null*/;
        System.out.println("d1: "+d1);
        FunctionTuple in , f_t;
        TupleSum sum;
        Projection p1_bis  = Projection.builder (1,c2), p2_bis  = Projection.builder (2,c2);
        Tuple t1  = new Tuple(null, null,d1,inter1,sc3,p1_1,p2,Intersection.factory(false,p4c,p3,sc3),/*new Unionp3,*/p4/*)*/),
              t0_1 = new Tuple(null, null,d1, p3c, all, p3c), t0_2 = new Tuple(null, null,d1, p3,  all, p3c);
        System.out.println(t0_1 + "(codom) " + t0_1.getCodomain()+ " (dom) " + t0_1.getDomain());
        Equality eq = (Equality) Equality.builder(p1_bis/*p3*/,p2_bis,true, t0_1.getCodomain());
        Tuple t0_0 = new Tuple(/*f1.andFactory(*/eq/*,eq.opposite())*/,null,d1, p1_bis, p1_bis, p1_bis),
              t0_3 = new Tuple(eq.opposite(), null, d1, ProjectionComp.factory(p1_bis).cast() ,ProjectionComp.factory(p1_bis).cast(), (SetFunction) p1_1.andFactory(p4,p3)),
              t0_4 = new Tuple(/*eq.opposite()*/null, null, d1, all ,all, p3),
              t0_5 = new Tuple(null, null,t0_3.getCodomain(), ProjectionComp.factory(p1_bis).cast(),ProjectionComp.factory(p2_bis).cast());
        
        System.out.println("trasposta di\n"+t0_3.toStringDetailed());
        System.out.println(t0_3.transpose().simplify());
        //System.out.println("merge di "+t0_1+ " e "+t0_2+"? : "+t0_1.merge(t0_2));
       
        System.out.println("\nt0_0: "+t0_0);
        System.out.println("semplificazione: "+Expressions.toStringDetailed(t0_0.simplify()));
        //System.exit(0);
        
        sum = (TupleSum) TupleSum.factory(false,t0_1,t0_2,t0_3, t0_4);
        System.out.println("somma: "+sum);
        Util.getChar();
        
        System.out.println("equivalente a <S> ? "+ sum.truthEquivalent( ));
        System.out.println("merge: "+((TupleSum)sum).merge());
        System.out.println("semplifichiamo e compattiamo:\n"+sum.simplify());
        Util.getChar();
        //bag
        /*WNtupleBag*/ BagExpr<FunctionTuple> wnbag;
        Map<FunctionTuple,Integer> map = new HashMap<>();
        map.put(t0_1,2);
        map.put(t0_2, -1/*-3*/);
        map.put(t0_3, 1);
        map.put(t0_4, 1);
        wnbag =  new FunctionTupleBag(map, false);
        System.out.println("bag:\n"+wnbag);
        wnbag =   (BagExpr<FunctionTuple>) wnbag.specSimplify();
        System.out.println("bag spec-semplificato:"+wnbag);
        System.out.println("bag semplificato:"+wnbag.genSimplify());
        Util.getChar();
        LogicalBag<FunctionTuple> lb = (LogicalBag<FunctionTuple>) wnbag.normalize();
        System.out.println("bag normalizzato:"+lb);
        System.out.println("map guardie -> coefficienti: "+lb.mapGuardsToMaxCoefficients());
        
        System.out.println("bag+bag: (normalizzato)");
        lb = (LogicalBag<FunctionTuple>) BagSum.factory(wnbag, wnbag).normalize();
        System.out.println(lb);
        System.out.println("map guardie -> coefficienti: "+lb.mapGuardsToMaxCoefficients());
        Util.getChar();
        
        //System.exit(0);
        
        TupleComposition tc = new TupleComposition(t0_5,t0_3);
        System.out.println("\ncomposizione: "+tc);
        System.out.println("semplificazione:\n"+Expressions.toStringDetailed(tc.simplify()));
        
        t0_5 = new Tuple(null, null,t0_3.getCodomain(),ProjectionComp.factory(p1_bis).cast(),ProjectionComp.factory(p1_bis).cast(),ProjectionComp.factory(/*p3*/p1_bis).cast());
        tc = new TupleComposition(t0_5,t0_3);
        System.out.println("\ncomposizione: "+tc.toStringDetailed());
        System.out.println("semplificazione:\n"+Expressions.toStringDetailed(tc.simplify()));
        
        //System.exit(0);
        //System.out.println("parte costante di "+f+ ": "+ ((Intersection)f).getConstantTerms());
        System.out.println("\necco t1: "+t1);
        System.out.println("\ntrasposta non semplificata:\n"+t1.transpose()/*.verbNormalize()*/);
        //System.exit(0);
        
        System.out.println("ecco le parti \"omogenee\" (dello stesso colore) di t1:\n"+t1.getHomSubTuples());
        System.out.println("ecco codom di t1: "+t1.getCodomain());
        System.out.println("ecco dom di t1: "+t1.getDomain());
        System.out.println("ecco supporto di t1: "+t1.getSorts());
        //System.out.println("ecco split delimiters: "+t1.filterSplitDelims());
        //System.out.println("ecco t1 parzialmente semplificato: "+t1.specSimplify());
        System.out.println("ecco t1 semplificato:");
        System.out.println(t1.simplify());
        //System.out.println("ecco lo split di t1: "+Util.toStringDetailed(t1.split()));
        
        inter1 = (Intersection) p1_1.andFactory(p2c,p1c);
        Tuple t2 = new Tuple(null, null,d1,inter1,sc3,p1_1,p2,
                Intersection.factory(false,p4c,p3,sc3), (SetFunction)p1_1.andFactory(p3,p4));
        System.out.println("\necco t2\n"+t2);
        System.out.println("ecco t1 - t2");
        System.out.println(t1.diff(t2));
       
        in = (FunctionTuple) t1.andFactory(t1,t2);
        sum = (TupleSum) TupleSum.factory(false,t1,in);
        System.out.println("\necco somma\n"+sum);
        System.out.println("semplificazione:\n"+sum.simplify());
         
        Tuple gt1 = new Tuple(Equality.builder(p1_1,p2,true, t1.getCodomain()), t1.getComponents(),null, t1.getDomain()),
              gt2 = new Tuple (Equality.builder(p1_1,p2,false,t1.getCodomain()),t2.getComponents(), null, t1.getDomain()), gt3;
        System.out.println("\necco gt1\n"+gt1);
        System.out.println("ecco gt1 semplificata\n"+gt1.simplify());
        
        System.out.println("\necco gt2\n"+gt2);
        System.out.println("ecco gt2 semplificata\n"+gt2.simplify());
        
        
        FunctionTuple ft = (FunctionTuple) gt1.notFactory(gt2);
        System.out.println("ecco ft:\n"+ft);
        System.out.println("\ncomplemento di gt2 (semplificato)\n"+ft.simplify().iterator().next());
        
        TupleIntersection ti = (TupleIntersection) t1.andFactory(gt2,gt1);
        System.out.println("\nfold di\ngt1: "+gt1+"\ne di\ngt2: "+gt2+'\n'+ti.specSimplify());
        
        System.out.println("\necco t1\n"+t1);
        //System.out.println("ecco la forma \"right-composable\" equivalente: "+t1.toRightComposableForm());
        System.out.println("ecco t1 semplificato");
        System.out.println(t1.simplify());
        
        FunctionTuple cft = (FunctionTuple) t1.notFactory(t1).simplify().iterator().next(); 
        System.out.println("\necco il complemento di t1 (semplificato)\n"+cft);
        //System.exit(0);
        
        System.out.println("\necco t2: "+t2.toStringDetailed());
       //System.out.println("forma right-comp:\n"+t2.toRightComposableForm());
       t2.simplify().forEach((wnf) -> {
           System.out.println("\necco una forma semplificata di t2: "+wnf.toStringDetailed());
       });
        
        System.out.println("\n**** composizione fra tuple ***** ");
        
        Tuple t4 = new Tuple(null, null,d1,inter1,sc3,p1_1,Diff.factory(sc3,p4), (SetFunction) p1_1.andFactory(p4c,sc3),p3,p4),
              t3 = new Tuple(null, null,t4.getCodomain(), (SetFunction) p1_1.andFactory(p0c,p1c),p2c, (SetFunction) p1_1.andFactory(p1_1,p2),p4,p4);
        
        FunctionTuple comp_res, res;
        //for (WNFunctionTuple t : simplify(t4))
            //System.out.println(Util.toStringDetailed(t));
        System.out.println("\ncomposizione di "+t3.toStringDetailed()+" e\n"+ t4.toStringDetailed());
        //System.out.println("left semplificata: "+simplify(t3));
        //System.out.println("right semplificata: ");
        //System.out.println(simplify(t4)/*verbNormalize()*/); // semplificazione costosa!!
        
        System.out.println(comp_res = new TupleComposition(t3,t4, true));
        System.out.println("ora semplifichiamo ...");
        comp_res.simplify().forEach((wn) -> {
            // semplificazione costosa!!
            System.out.println(wn.toStringDetailed());
            //System.out.println("delim: "+wn.filterSplitDelims());
       }); //System.exit(0);
        
        System.out.println("\n********estensione di tuple  ********");
        Map<? extends ColorClass, List<? extends SetFunction>> th_list =  t3.getHomSubTuples(); // we separate color-homgenous parts of t3
        System.out.println("ecco le parti omogenee di t3 :" + th_list);
        Tuple t3_1 , t3_2 ;
        SetFunction[][] at = {null,null,null};
        int j = 0;
        for (ColorClass cc : th_list.keySet()) {
            at[j++] = th_list.get(cc).toArray(new SetFunction[0]);
        }
        t3_1 = new Tuple(null, Arrays.asList(at[0]), null, t3.getDomain()); 
        t3_2 = new Tuple(null, Arrays.asList(at[1]), null, t3.getDomain());
        ft = TupleJuxtaposition.factory (true, t3_1,t3_2);
        System.out.println("giustapposizione delle parti omogenee di t3\n"+ ft );
        System.out.println("ecco la semplificazione ..\n"+ ft.simplify() );
        Guard eq1 = Equality.builder(p1_0,p2,false,t3.getDomain()), eq2 = Equality.builder(p1_1,p2,false,t3.getDomain()),
              eq1_f = Equality.builder(p1_0,p2,false,t3_1.getCodomain()), eq2_f = Equality.builder(p1_1,p2,false,t3_1.getCodomain()),
              eq3_f , f3 = (Guard) eq1.andFactory(eq1_f, eq2_f/*,eq3_f*/);
        
        gt3 = new Tuple(f3, t3_1.getComponents(),eq1,null);
        TupleProjection tp = new TupleProjection (gt3,2);
        System.out.println("\necco tp\n"+tp);
        System.out.println("semplifichiamo:\n"+Expressions.toStringDetailed(tp.simplify()));
        //System.out.println("spec-semplifichiamo:\n"+pt.specSimplify());
        List<SetFunction> cf = new ArrayList<>(gt3.getComponents());
        cf.set(0, ProjectionComp.factory(p1_0).cast());
        cf.set(1, ProjectionComp.factory(p1_0).cast());
        cf.set(2, ProjectionComp.factory(p1_0).cast());
       
        gt3 = new Tuple(gt3.filter(), cf, gt3.guard(),null);
        System.out.println("\necco tp\n"+tp);
        System.out.println("semplifichiamo:\n"+Expressions.toStringDetailed(tp.simplify()));
        
        cf = new ArrayList<>(gt3.getComponents());
        cf.add(ProjectionComp.factory(p1_0).cast());
        Tuple t0 = new Tuple(null, cf, null, d1);
        eq1_f = Equality.builder(p1_0,p2,false,t0.getCodomain());
        eq2_f = Equality.builder(p1_0,p3_0,false,t0.getCodomain());
        eq3_f = Equality.builder(p3_0,p2,false,t0.getCodomain());
        f3 = And.factory(eq1_f,eq2_f,eq3_f);
        tp = new TupleProjection(new Tuple(f3, t0.getComponents(),null, t0.getDomain()),2);
        System.out.println("\necco tp\n"+tp);
        System.out.println("semplifichiamo:\n"+Expressions.toStringDetailed(tp.simplify()));
        System.out.println("semplifichiamo e compattiamo:\n"+tp.simplify());
    }
    
    /**
     *
     */
    public static void testExtra() {
        final Interval in = new Interval(3,5/*5*/);
        ColorClass A = new ColorClass("A",in,true);
        final Projection a1,a1_4,a2,a2_1,a2_4,a3,a3_1,a3_2,a3_3,a4;
        a1= Projection.builder(1,A);
        a2= Projection.builder(2,A);
        a3 = Projection.builder(3,A);
        a4 = Projection.builder(4,A);
        a2_4= Projection.builder(2,4,A);
        a3_2 =Projection.builder(3,2,A);
        a3_3 =Projection.builder(3,3,A);
        a1_4 = Projection.builder(1,4,A);
        a2_1 = Projection.builder(2,1,A);
        a3_1 = Projection.builder(3,1,A);
        final SetFunction p1_4c = ProjectionComp.factory(a1_4).cast(),
                              p2c = ProjectionComp.factory(a2).cast(),
                              p1c = ProjectionComp.factory(a1).cast(),
                              p2_1c = ProjectionComp.factory(a2_1).cast();
        final Equality eq1, eq2, ineq1, ineq2,ineq3,ineq4, ineq5, ineq6, ineq7;
        final Intersection in1 = (Intersection) Intersection.factory(false,p1_4c, p2c),
                           in2 = (Intersection) Intersection.factory(false,p1c, p2_1c);
        final And guard, filter, guard2;
        Domain d3 = new Domain(A,A,A),
                d2  = new Domain(A,A);
        
        //filter
        eq1 = (Equality) Equality.builder(a1,a2_4,true,d3);
        ineq1 = (Equality) Equality.builder(a1,a3,false,d3);
        ineq2 = (Equality) Equality.builder(a1,a3_2,false,d3);
        ineq3 = (Equality) Equality.builder(a1,a3_3,false,d3);
        
        //guard
        eq2 = (Equality) Equality.builder(a2,a3,true,d3);
        ineq4 = (Equality) Equality.builder(a1,a2_1,false,d3);
        
        filter = (And) And.factory(eq1, ineq1, ineq2, ineq3 );
        guard  = (And) And.factory(eq2,ineq4);
        Tuple t = new Tuple(filter,guard,d3,in1,in2,in1);
        TupleProjection tp = new TupleProjection(t,2);
        
        System.out.println(tp);
        //System.out.println("semplifico:\n"+Expressions.verbNormalize(tp,LogExprSimplifier.builder(false)));
        System.out.println();
        ineq5 =  (Equality) Equality.builder(a1,a2_1,false,d3);
        ineq6 = (Equality) Equality.builder(a1,a3_1,false,d3);
        ineq7 = (Equality) Equality.builder(a2,a3,false,d3);
        guard2  = (And) And.factory(ineq5 ,ineq6 ,ineq7);
        
        //System.out.println(gf);
        //System.out.println("semplifico:\n"+Expressions.verbNormalize(gf,LogExprSimplifier.builder(false))+'\n');
        
        //FunctionTuple ts = TupleSum.factory(false,gf,tp);
        //System.out.println(ts);
        //System.out.println("semplifico:\n"+Expressions.verbNormalize(ts,LogExprSimplifier.builder(false))+'\n');
        
        List<SetFunction> args = Collections.nCopies(5, (SetFunction)p1c);
        args.set(1, Intersection.factory(false,p1c,ProjectionComp.factory(a1.setExp(1)).cast()));
        Tuple sx = new Tuple(null,args,null, new Domain(A));
        //sx.setComponent(1, a1);
        //sx.setComponent(2, Intersection.factory(false,p1c,ProjectionComp.factory(a1.setExp(1))));
        //sx.setComponent(3, Intersection.factory(false,p1c,ProjectionComp.factory(a1.setExp(1))));
        //sx.setComponent(4, a1);
        System.out.println("\nsx\n"+sx);
        System.out.println(new TupleComposition(sx, new Tuple(null, Collections.singletonList(p1c),null,new Domain(A))).simplify());
        //System.exit(0);
    }
    
    /**
     *
     */
    public static void testGuard()  {
        Projection p1,p2,p3,p4,p5;
        Subcl sc1,sc2,sc3,sc4;
        Guard g0,g1,g2,g3,g4,g5,g6,g7,g8,g9,g10,g11,g12;
        p1= Projection.builder(1,-2,c1);
        p2 = Projection.builder(2,1,c1);
        g1 = Equality.builder(p1,p2,false,new Domain(c1,c1));
        g2 = Equality.builder(p2,p1,false,new Domain(c1,c1));
        if (!g1.equals(g2)) {
            System.out.println("problema");
            throw new Error();
        }
        
        System.out.println("ecco g1\n"+g1+" "+g1.getDomain());
        System.out.println("ecco g1 semplificato\n"+Expressions.toStringDetailed(g1.simplify()));
        p4 = Projection.builder(2,c1);
        g2 =  Equality.builder(p1,p4,true,new Domain(c1,c1));
        //g11 = And.factory(g1,g2,new True(g1.getDomain()));
        //System.out.println("ecco g11\n"+g11);
        //System.out.println("ecco g11 semplificato\n"+Expressions.toStringDetailed(simplify(g11)));
        //System.out.println("ecco g11 semplificato (solo specSimplify)\n"+g11.specSimplify());
        
        sc1 = Subcl.factory(1, c2);
        sc2 = Subcl.factory(2, c2);
        sc3 = Subcl.factory(3, c2);
        Projection np1 = Projection.builder(1,c2), np2 = Projection.builder(2,c2),
                   np3 = Projection.builder(3, c2), np4 = Projection.builder(4,c2),
                   np5 = Projection.builder(5,c2);
        Membership m1 = (Membership) Membership.build(np4,sc2,true,new Domain(c2,c2,c2,c2)),
                   m2 = (Membership) Membership.build(np4,sc1,false,new Domain(c2,c2,c2,c2));
        g0 = (Guard) g2.andFactory(m1, m2/*, false*/);
        System.out.println("ecco g0\n"+g0);
        System.out.println("ecco g0 semplificato\n"+Expressions.toStringDetailed(g0.simplify()));
        
        Domain d1 = new Domain(c1,c1,c1,c1,c2,c2,c2,c2,c2);
        System.out.println("d1: "+d1);
        Equality eq1 = (Equality) Equality.builder(np2,np3,false,d1), 
                 eq2 = (Equality) Equality.builder(np3,np4,false,d1), 
                 eq3 = (Equality) Equality.builder(np4,np2,false,d1),
                 eq4 = (Equality) Equality.builder(np1,np3,true,d1),
                 eq5 = (Equality) Equality.builder(np3,np5,true,d1);
        //And and = (And) And.factory(eq1,eq2,eq3);
        //System.out.println("parti indipendenti di "+and+'\n'+And.independentParts(Arrays.asList(eq1,eq2,eq3)));
        g2 = And.factory(eq1, Membership.build(np4,sc1,true,d1), eq2, Membership.build(np3,sc1,true,d1));
        //g2 = f1.andFactory(eq1, Membership.factory(p4_1,sc1,true), Membership.factory(p4_1,(Subcl)f1.orFactory(sc2,sc1).specSimplify(),true));
        System.out.println("ecco g2\n"+g2);
        //System.out.println("ecco g2 diviso in parti\n"+Arrays.asList(Guard.separateIntoParts(((N_aryGuardOperator)g2).getArgs())));
        System.out.println("ecco g2 semplificato\n"+Expressions.toStringDetailed(g2.simplify()));
        //System.exit(0);
        
        g1 = Equality.builder(p1,p2,true,d1);
        g6 = Equality.builder(p1,p4,true,d1);
        System.out.println("ecco g1\n"+g1);
        System.out.println("ecco g1 semplificato\n"+Expressions.toStringDetailed(g1.simplify()));
        System.out.println("ecco g3\n"+ (g3 = Neg.factory(g1)));
        System.out.println("ecco g3 semplificato\n"+Expressions.toStringDetailed(g3.simplify()));
        System.out.println("ecco g4\n"+ (g4 = Neg.factory(g3)));
        System.out.println("ecco g4 semplificato\n"+Expressions.toStringDetailed(g4.simplify()));
        System.out.println("ecco g5\n"+ /*Expressions.toStringDetailed*/(g5 = (Guard) g2.andFactory(g1,g6)));
        System.out.println("ecco g5 semplificato\n"+Expressions.toStringDetailed(g5.simplify()));
        g7 = Membership.build(/*p3*/np2,sc2,/*false*/true,d1);
        g8 = (Guard) g2.orFactory(false,g2,g7);
        System.out.println("ecco g8\n"+ g8.toStringDetailed());
        System.out.println("ecco g8 semplificato\n"+Expressions.toStringDetailed(g8.simplify()));
        System.out.println("ecco not(g8) semplificato\n"+Expressions.toStringDetailed(Neg.factory(g8).simplify()));
        
        //parte per testare nuovi metodi di canonizzazione
        TreeSet<Equality> es = new TreeSet<>(COMPEQ), ies = new TreeSet<>(COMPEQ);
        es.addAll(Arrays.asList(new Equality[] {eq1.opposite(),eq2.opposite(),eq3.opposite(),eq4,eq5}));
        System.out.println(es);
        And.toCanonicalForm(es);
        System.out.println("forma canonica ->\n"+es);
        
        ies.addAll(Arrays.asList(new Equality[] {eq1,(Equality)Equality.builder(np3,np5,false,d1)}));
        System.out.println(ies);
        And.replaceEq(ies, es);
        System.out.println("dopo aver sostituito: "+es+"\n-> \n"+ies);
        Util.getChar();
        
        p1= Projection.builder(1,c1);
        Projection p1_1 = Projection.builder(1,1, c1);
        p2 = Projection.builder(2,-1,c1);
        p3 = Projection.builder(3,c1);
        p4 = Projection.builder(2,c1);
        p5 = Projection.builder(4,c1);
        
        g1 = Equality.builder(p1_1,p2,true,d1);
        g3 = Equality.builder(p3,p4,true,d1);
        g2 = Equality.builder(p1_1,p3,true,d1);
        g5 = Equality.builder(p4,p5,true,d1);
        es.clear();
        es.addAll(Arrays.asList(new Equality[] {(Equality)g1,(Equality)g2,(Equality)g3,(Equality)g5}));
        System.out.println(es);
        //And.toCanonicalForm(es);
        System.out.println(es);
        
        Set<Equality> l = new HashSet<>(ies);
        l.addAll(es);
        System.out.println(l);
        LogicalExprs.disjoin/*New*/(l);
        System.out.println(l);
        //System.exit(0);
 
        g4 = (Guard) g2.orFactory(false,g1,g3);
        System.out.println("ecco g3\n"+ g4.toStringDetailed());
        System.out.println("ecco g3 semplificato\n"+Expressions.toStringDetailed(g4.simplify()));
        
        g8 = Membership.build(np2,sc3,false,d1);
        g9 = (Guard) g2.andFactory(g2.orFactory(g8,g7), Membership.build(np2,sc1,false,d1));
        System.out.println("ecco g9\n"+ g9.toStringDetailed());
        System.out.println("ecco g9 semplificato\n"+Expressions.toStringDetailed(g9.simplify()));
        g10 = (Guard) g2.andFactory(g1,Neg.factory(g1));
        System.out.println("ecco g10\n"+ g10.toStringDetailed());
        System.out.println("ecco g10 semplificato\n"+Expressions.toStringDetailed(g10.simplify()));
        
        p3 = Projection.builder(3,c1);
        p2 = Projection.builder(2,c1);
        Projection p2bis = Projection.builder(2,1,c1), p4bis = Projection.builder(4,1,c1);
        p4 = Projection.builder(4,c1);
       
        g11 = And.factory(Equality.builder(p1,p4bis,false,d1), Equality.builder(p1,p2bis,false,d1), Equality.builder(p2,p3,false,d1),Equality.builder(p2,p4,false,d1),Equality.builder(p3,p4,false,d1));
        InequalityGraph ig = ((And)g11).igraph().get(g11.getSorts().iterator().next());
        System.out.println("ecco g11\n"+ g11.toStringDetailed());
        System.out.println("ecco il grafo di g11\n"+ ig);
        System.out.println("componenti di g11\n" + ig.connectedComponents());
        System.out.println("ecco g11 semplificato\n\n"+Expressions.toStringDetailed(g11.simplify()));
        
        
        g12 = (Guard) g2.andFactory(g11,g9);
        System.out.println("ecco g12\n"+ g12.toStringDetailed());
        System.out.println("ecco g12 semplificato\n\n"+Expressions.toStringDetailed(g12.simplify()));
    }
    
    /**
     *
     */
    public static void testGraph() {
        System.out.println("**** graphs ****");
        Graph<Integer> g = new Graph<>();
        g.addVertex(1);g.addVertex(2); 
        g.addVertex(3);g.addVertex(4);
        g.addVertex(5);
        
        g.addEdge(1,2);
        g.addEdge(2,3);
        g.addEdge(5,4);
        g.addEdge(1,3);
        //g.addEdge(3,4);
        System.out.println("g: "+g);
        int lambda = 3;
        System.out.println("P(g,"+lambda+"): "+ g.chromPolynomial (lambda));
        //Util.getChar();
        
        System.out.println("components of " +g + '\n'+g.connectedComponents());
        //Util.getChar();
    }
    
    /**
     *
     */
    public static void testBag() {
        Projection c_1 = Projection.builder(1, C),
                   c_2 = Projection.builder(2, C),
                   c_3 = Projection.builder(3, C),
                   c_4 = Projection.builder(4, C);
        Subcl S_1 = Subcl.factory(1, C),
              S_2 = Subcl.factory(2, C) ; 
        Domain d1 = new Domain(C),
               d2 = new Domain(C,C); // C x C
        Guard g1 = Membership.build(c_1, S_1, true, d2),
              g2 = Membership.build(c_2, S_1, true, d2),
              g1_1 = Membership.build(c_1, S_1, true, d1),
              g1_2 = Membership.build(c_1, S_2, true, d1);
        Tuple t, tc1 , tc2;
        t =  new Tuple(null, And.factory(g1,g2),null,c_2, c_1);
        tc1 =new Tuple(g1_1, g1, null, c_1);
        tc2 =new Tuple(g1_1, g1, null, c_2);
        LogicalBag<FunctionTuple> I, t_bag;
        FunctionTuple[] a = new FunctionTuple[]{tc1,tc1,tc2};
        I = new FunctionTupleBag(Util.asMap(Arrays.asList(a)));
        t_bag = new FunctionTupleBag(Util.singleMap(t, 1));
        BagComp<FunctionTuple> I_comp_t = new BagComp(I,t_bag);
        System.out.println("normalizzo:\n"+I_comp_t);
        t_bag = (LogicalBag<FunctionTuple>) I_comp_t.normalize(); //possiamo usare sia normalize sia simplify
        System.out.println(t_bag.toStringDetailed());
        System.out.println("disgiunto & normalizzato:\n");
        System.out.println(t_bag.normalize(/*true*/));
        
        System.out.println("map guardie -> coefficienti:");
        System.out.println(t_bag.mapGuardsToMaxCoefficients());
        Util.getChar();
        
        tc1 = new Tuple(g1_2, g1, null,  c_1);
        tc2 = new Tuple(g1_2, g1, null, c_2);
        a = new FunctionTuple[]{tc1,tc1,tc2};
        I = new FunctionTupleBag(Util.asMap(Arrays.asList(a)));
        I_comp_t = new BagComp<>(I,new FunctionTupleBag(Util.singleMap(t, 1)));
        System.out.println("normalizzo:\n"+I_comp_t);
        System.out.println("-->\n"+I_comp_t.normalize());
        
        BagComp<FunctionTuple> transp = new BagComp<>(new BagTranspose<>(new FunctionTupleBag(Util.singleMap(t, 1))), new BagTranspose<>(I));
        System.out.println("normalizzo:\n"+transp);
        System.out.println("-->\n"+transp.normalize());
        Util.getChar();
        
        tc2 = new Tuple(g1_1, g1, null, c_2);
        I = new FunctionTupleBag(Util.singleMap(tc2, 1));
        I_comp_t = new BagComp<>(I,new FunctionTupleBag(Util.singleMap(t, 1)));
        System.out.println("normalizzo:\n"+I_comp_t);
        System.out.println(I_comp_t.normalize());
        System.out.println("trasposta di :"+I_comp_t);
        System.out.println(t_bag = (LogicalBag<FunctionTuple>) new BagTranspose<>(I_comp_t).normalize());
        Util.getChar();
        
        System.out.println("map guardie -> coefficienti: "+t_bag.mapGuardsToMaxCoefficients());
        System.out.println(t_bag);
        Util.getChar();
        
                
        //testiamo LinearComb
        System.out.println("+++ BagFunction +++");
        LinearComb l = new LinearComb(c_1, c_2, c_2, c_1, c_1, S_1, S_1, S_2);
        LinearComb l1,l2,l3;
        System.out.println(l);
        l1 = new LinearComb(c_2,c_1,c_1,S_1,S_1,S_2,S_1);
        System.out.println(l1+" -> "+l1.components());
        BagExpr<ElementaryFunction> d = BagDiff.build(l, l1);
        System.out.println("diff:\n"+d);
        d = (BagExpr<ElementaryFunction>) d.normalize();
        System.out.println("diff semplificata:\n"+d);
        d = ((LogicalBag)d).disjoin();
        System.out.println("disgiunta:\n"+d);
        l = new LinearComb(c_1,c_2,c_2,c_1,c_1,S_1,S_1,S_2);
        System.out.println(l + " -> "+l.components());
        l1 = new LinearComb(c_2,c_1,c_1,S_1,S_1,S_2,S_1);
        System.out.println(l1+" -> "+l1.components());
        d = BagDiff.build(l, l1);
        System.out.println("semplifico:\n"+d);
        System.out.println(d.normalize());
        l = new LinearComb(c_1,c_1,c_1,S_1,S_1,S_2);
        l2 = new LinearComb(c_2,c_4,c_1,S_1,c_4,S_2);
        l3 = new LinearComb(c_3,c_1,c_1,S_1,c_4,S_2,S_1);
        System.out.println(l+" -> "+l.components());
        Domain d4 = new Domain(C,C,C,C);
        WNtuple wnt;
        List<LinearComb> lb = new ArrayList<>(Arrays.asList(new LinearComb[] {l1,l2,l3}));
        Equality eq1 = (Equality) Equality.builder(c_2,c_3,false,d4), 
                 eq2 = (Equality) Equality.builder(c_2,c_4,false,d4);
        And ng1 = (And) And.factory(eq1,eq2);
        System.out.println(ng1.getSort());
        wnt = new WNtuple(null,lb,ng1,d4, true);
        System.out.println("expansion of "+wnt);
        for (WNtuple tx : wnt.singleIndexComponentsTuples()) {
        	System.out.println("->\n"+tx);
        	System.out.println("independent subtuples of"+tx+"->\n"+tx.independentComponentsV2());
            
        }
        /*BagComp<ElementaryFunction> bc = new BagComp<>(l,l1);
        System.out.println("semplifico:\n"+bc);
        /*System.out.println(bc.normalize());
        d = BagDiff.build(new LinearComb(S_1, 2), new LinearComb(c_1));
        System.out.println(bc.normalize());
        /*d = BagDiff.build(new LinearComb(S_1, 2), new LinearComb(c_1));
        bc = new BagComp<>(d,l1);
        System.out.println("semplifico:\n"+bc);
        System.out.println(bc.normalize());*/
        

    }
    
    /*
    example used in PN2020 paper
    */
    static void petriNets20() {
        ColorClass C = new ColorClass("C", true); //ordered class C s.t. |C| > 1
        Interval i1 = new Interval(3,8), i2 = new Interval(2,2); // [3,8] and [2,2] (constraints)
        ColorClass D = new ColorClass("D", new Interval[] {i1, i2}); // partitioned class D
        Projection c_1= Projection.builder(1, C), // c_1
                   c_2= Projection.builder(2, 1, C), // !c_2
                   d_1= Projection.builder(1, D); // d_1
        SetFunction comp_c_1, comp_d_1;
        comp_c_1 = ProjectionComp.factory(c_1).cast(); // S - c_1
        comp_d_1 = ProjectionComp.factory(d_1).cast(); // S - d_1
        Subcl sd2 = Subcl.factory(2, D); // constant D{2}
        SetFunction inter;
        inter = Intersection.factory(comp_d_1, sd2); // D{2} \cap S -d1 
        Domain dom = new Domain(C,C,D); // domain C^2 x D
        Guard g1 = Membership.build(d_1, sd2, true, dom); // guard d_1 \in D{2}
        Tuple t1 , t2, t3;
        t1 = new Tuple(dom, c_1, comp_c_1, c_2, d_1); // <c_1,S-c_1,!c_2,d_1>
        t2 = new Tuple(null, g1, dom, comp_c_1, c_2 , inter); // <S-c_1,!c_2,(S-d_1 * S_D{2})>[d_1 in D{2}]
        t3 = new Tuple(null, g1, dom, comp_c_1, c_2 , d_1); // <S-c_1,!c_2,d_1 >[d_1 in D{2}]
        System.out.println(t1+"\n (abstract) "+t1.toStringAbstract());
        System.out.println(t2+"\n (abstract) "+t2.toStringAbstract());
        System.out.println(t3+"\n (abstract) "+t3.toStringAbstract());
        TupleComposition tcom = new TupleComposition(t1, t2); // t1 /circ t2
        TupleTranspose tcom_trans = new TupleTranspose(tcom); // tcom^t
        System.out.println(tcom_trans+"\n-->");
        Expressions.printResults(tcom_trans.simplify());
        tcom_trans = new TupleTranspose(tcom_trans);
        System.out.println(tcom_trans+"\n-->");
        Expressions.printResults(tcom_trans.simplify());
        
        
    }

}
