package sosumijava;

/**
 *
 * @author tomasca
 */
public class SosumiException extends Exception {

    public SosumiException(String msg, Throwable t) {
        super(msg, t);
    }

    SosumiException(String msg) {
        this(msg, null);
    }
}
