package org.iutools.webservice;

public class SearchEndpointException extends ServiceException {
	
	public SearchEndpointException(String mess) {
		super(mess);
	}

	public SearchEndpointException(Exception e) {
		super(e);
	}

	public SearchEndpointException(String mess, Exception e) {
		super(mess, e);
	}
}
