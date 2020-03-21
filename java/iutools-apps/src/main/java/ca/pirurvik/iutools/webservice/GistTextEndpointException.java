package ca.pirurvik.iutools.webservice;

public class GistTextEndpointException extends Exception {
	public GistTextEndpointException(String mess, Exception e) {
		super(mess, e);
	}
	public GistTextEndpointException(Exception e) {
		super(e);
	}
	public GistTextEndpointException(String mess) {
		super(mess);
	}
}
