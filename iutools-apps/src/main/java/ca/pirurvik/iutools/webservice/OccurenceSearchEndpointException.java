package ca.pirurvik.iutools.webservice;

public class OccurenceSearchEndpointException extends Exception {

    public OccurenceSearchEndpointException(String mess, Exception e) {
        super(mess, e);
    }
    public OccurenceSearchEndpointException(String mess) {
        super(mess);
    }
    public OccurenceSearchEndpointException(Exception e) {
        super(e);
    }
}
