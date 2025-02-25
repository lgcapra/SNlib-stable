package classfunction;

import color.ColorClass;
import expr.Interval;
import util.Pair;

/**
 * supertype of elementary constant functions
 * @author Lorenzo Capra
 */
public abstract class ConstantFunction extends ElementaryFunction   {
    
    /**
     * build a constant class-function
     * @param cc the function's color-class
     */
    public ConstantFunction(final ColorClass cc)  {
        super(cc);
    }
    
    @Override
    public final boolean isConstant() {
        return true;
    }
    
    @Override
    public Pair<SetFunction,Integer> baseCompose(final SetFunction right) {
        Interval card = right.card();
        return card != null && right.card().lb() > 0 ? new Pair<>(this,null) :
                      super.baseCompose(right);     
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
