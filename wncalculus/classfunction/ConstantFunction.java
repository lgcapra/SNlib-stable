package wncalculus.classfunction;

import wncalculus.color.ColorClass;
import wncalculus.expr.Interval;

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
    public SetFunction baseCompose(SetFunction right) {
        Interval card = right.card();
        
        return card != null && right.card().lb() > 0 ? this : null;
       
    }
  
   /*@Override
   public final Integer getIndex() {
    	return 0;
   }*/
   
   
   @Override
   public final ElementaryFunction setDefaultIndex() {
       return this;
   }

    @Override
    public int splitDelim() {
        return 0;
    }
    
   
}
