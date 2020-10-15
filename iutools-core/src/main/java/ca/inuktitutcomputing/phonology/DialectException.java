package ca.inuktitutcomputing.phonology;

import ca.inuktitutcomputing.script.TransCoderException;

public class DialectException extends Exception {

    public DialectException(String mess, Exception e) {
        super(mess, e);
    }
    public DialectException(Exception e) {
        super(e);
    }
    public DialectException(String mess) {
        super(mess);
    }
}
