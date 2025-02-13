package expr;

/**
 *
 * @author Lorenzo Capra
 * this exception is raised whenever, for any reasons, the co-domain
 * of an expression is violated or turns out to be erroneous
 */
public class IllegalDomain extends RuntimeException  {

    private final String msg;
    
    /**
     * creates an exception without an associate message
     */
    public IllegalDomain( ) {
        this.msg = "";
    }
    
    /**
     * creates an exception with an associate message
     * @param msg the message to prompt
     */
    public IllegalDomain(String msg) {
        this.msg = msg;
    }
    
    @Override
    public String toString() {
        return msg;
    }
    
}
