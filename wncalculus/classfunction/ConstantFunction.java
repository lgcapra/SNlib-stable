package wncalculus.classfunction;

import wncalculus.color.ColorClass;
import wncalculus.expr.Interval;
import wncalculus.util.Pair;

/**
 *
 * @author Lorenzo Capra
 */
public abstract class ConstantFunction extends ElementaryFunction   {
    
    /**
     * build a constant class-function
     * @param cc the function's color-class
     */
    public ConstantFunction(ColorClass cc)  {
        super(cc);
    }
    
    @Override
    public final boolean isConstant() {
        return true;
    }
    
    @Override
    public Pair<SetFunction,Integer> baseCompose(SetFunction right) {
        Interval card = right.card();
        if (card != null && right.card().lb() > 0)
            return new Pair<>(this,null);
        
        return super.baseCompose(right);     
    }
   
   @Override
   public final ElementaryFunction setDefaultIndex() {
       return this;
   }

    @Override
    public int splitDelim() {
        return 0;
    }
    
   
}
