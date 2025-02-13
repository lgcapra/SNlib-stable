package util;

import java.util.Arrays;

/**
 * this class represents structured keys composed of arbitrary elements, that can
 * be safely used as keys in hash structures
 * @author Lorenzo Capra
 */
public final class ComplexKey {
    
    private final Object [] keys;
    
    /**
     * builds a structured key from a variable list of arguments
     * @param keys a varargs list of objects 
     */
    public ComplexKey(Object ... keys) {
        this.keys = keys;
    }
    
    /**
     * return a given component of the key
     * @param i a give position
     * @return  the i-th component of this key (positions start from 1 ...)
     * @throws IndexOutOfBoundsException if the argument is out of range
     */
    public Object key (int i) {
        return this.keys[i - 1];
    }

   
    @Override
    public boolean equals (Object o) {
        return o instanceof ComplexKey && Arrays.equals(this.keys, ((ComplexKey) o).keys);
    }

    @Override
    public int hashCode() {
        return 119 + Arrays.hashCode(this.keys);
    }
    
    
    @Override
    public String toString() {
        return Arrays.toString(this.keys);
    }
    
    //for debugging

    /**
     *
     * @return
     */
    public String hashCodes() {
        String res = "";
        for (Object x :this.keys)
            res+= x +"("+x.hashCode()+") ";
        return res;
    }
    
}
