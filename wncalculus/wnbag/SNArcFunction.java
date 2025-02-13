package wnbag;

import guard.Guard;

/**
 * the super type of SNArcFunction
 * @author lorenzo
 */
public interface SNArcFunction {
    
    
    <E extends SNArcFunction> E applyFilter(Guard f);
    
    
}
