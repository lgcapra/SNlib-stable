package wncalculus.classfunction;

import java.util.Set;
import wncalculus.expr.UnaryOp;
import wncalculus.color.ColorClass;

/**
 *
 * @author Lorenzo Capra
 */
public abstract class UnaryClassOp extends SetFunction implements UnaryOp<SetFunction> {
    
    private final SetFunction arg;//argument
    
    UnaryClassOp (SetFunction arg) {
        this.arg = arg;
    }
    
    @Override
    public final SetFunction setDefaultIndex() {
        SetFunction narg = this.arg.setDefaultIndex();
        
        return narg != this.arg ? buildOp(narg) : this;
    }
    
    @Override
    public final SetFunction getArg() {
       return this.arg;
    }
   
    @Override
    public final ColorClass getSort() {
        return this.arg.getSort();
    }
   
   @Override
    public final int splitDelim() {
        return this.arg.splitDelim();
    }
   
    @Override
   public final Set<Integer> indexSet() { 
         return this.arg.indexSet();
     }
    
    
    @Override
    public final String toString() {
        return UnaryOp.super.toStringOp();
    }
    
     @Override
    public final SetFunction copy(ColorClass newcc) {
        return buildOp(this.arg.copy(newcc));
    }
    
    /*@Override
    public final SetFunction clone (final Domain newdom, final Domain newcd) {
        return (SetFunction) super.clone(newdom, newcd);
    }*/
    
}
