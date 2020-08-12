package ca.nrc.json;

public class TypePreservingMapperException extends Exception {
    public TypePreservingMapperException(String mess, Exception e) {
        super(mess, e);
    }
    public TypePreservingMapperException(String mess) {
        super(mess);
    }
    public TypePreservingMapperException(Exception e) {
        super(e);
    }
}
