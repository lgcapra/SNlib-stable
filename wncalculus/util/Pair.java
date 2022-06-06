package wncalculus.util;

import java.util.Map;
import java.util.Objects;

/**
 * this class implements pairs of arbitrary objects
 * the class is provided with "set" methods
 * @author Lorenzo Capra
 * @param <K> key component's type
 * @param <V> value component's type
 */
public class Pair<K,V> implements Map.Entry<K,V> {
    private K key;
    private V value;
    
    /**
     * base constructor: creates a pair key-value
     * @param k the key (i.e., the first element)
     * @param v the value (i.e., the second element)
     */
    public Pair (K k, V v) {
        this.key   = k;
        this.value = v;
    }
    
    @Override
    public final K getKey () {
        return this.key;
    }
    
    @Override
    public final V getValue () {
        return this.value;
    }
    
    /**
     * sets up the 1st element (called key) of <tt>this</tt> pair
     * @param el the 1st element's (new) value
     * @return the old value of 1st element 
     */
    public K setKey (K el) {
        K temp = this.key;
        this.key = el;
        return temp;
    }
    
    @Override
    public V setValue (V el) {
        V temp = this.value;
        this.value = el;
        return temp;
    }
    
    @Override
    public final boolean equals (Object o) {
        boolean res = o != null && o.getClass().equals(getClass());
        if (res) {
            Pair p = (Pair) o;
            res = Objects.equals(this.key,p.key) && Objects.equals(this.value, p.value);
        }
        
        return res;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        hash = 79 * hash + Objects.hashCode(this.value);
        return hash;
    }
    
    @Override
    public String toString() {
       return  "("+this.key + "," + this.value +')';
    }

} 
