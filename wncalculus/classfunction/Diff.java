package classfunction;

import java.util.Map;
import java.util.Set;
import color.ColorClass;
import expr.*;

/**
 * this class implements the difference between class-functions mapping to sets
 * @author lorenzo capra
 */
//classe marker: il metodo factory mappa Diff in una altro oggetto
//non dovrebbe essere nell gerarchia
//oppure: definire come deafult in ClassFunction is metodi
public final class Diff extends SetFunction  {

    /**
     * factory method: builds a <tt>SetFuncton</tt> which is equivalent to the difference between the operands,
     * @param min the minuend
     * @param subtr the subtrahend
     * @param check domain-check flag
     * @return the <tt>SetFunction</tt> a function equivalent to the difference
     * @throws IllegalDomain if the arity of operands is not the same
     */
    public static SetFunction factory (SetFunction min , SetFunction subtr , boolean check) { // anticipates specific reductions
       if (check && !min.getSort().equals(subtr.getSort())) 
            throw new IllegalDomain(min+" ("+min.getSort()+") "+subtr+" ("+subtr.getSort()+")");
        
        if (min.isFalse() || subtr.isTrue()) 
            return min.getFalse(); // might be a general rule 
        
        if ( subtr.isFalse()) 
            return min; //as above
         
        if (min.isTrue())
            return Complement.factory(subtr);  

        return Intersection.factory(min , Complement.factory(subtr)) ;
    }
    
     /** overloaded constructor not checking color' compliance
     * @param min the minuend
     * @param subtr the subtrahend
     * @return the <tt>SetFunction</tt> which is the difference between the operands
     */
    public static SetFunction factory (SetFunction min , SetFunction subtr )  {
        return factory (min, subtr, false);
    }

    @Override
    public Interval card() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Integer> indexSet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <E extends ClassFunction> E copy(ColorClass newcc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <E extends ClassFunction> E setDefaultIndex( ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int splitDelim() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    

    @Override
    public ColorClass getSort() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ParametricExpr clone(final  Map<Sort, Sort> split_map) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected final void checkNeutral() {
        if (getSort().neutral()) {
            throw new IllegalArgumentException("Only All an Empty may have a neutral colour");
        }
    }


}
