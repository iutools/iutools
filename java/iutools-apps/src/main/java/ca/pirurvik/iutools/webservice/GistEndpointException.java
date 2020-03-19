package ca.pirurvik.iutools.webservice;

public class GistEndpointException extends Exception {
	public GistEndpointException(String mess, Exception e) {
		super(mess, e);
	}
	public GistEndpointException(Exception e) {
		super(e);
	}
	public GistEndpointException(String mess) {
		super(mess);
	}
}
