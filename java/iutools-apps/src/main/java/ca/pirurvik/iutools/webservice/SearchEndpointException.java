package ca.pirurvik.iutools.webservice;

import ca.nrc.config.ConfigException;

public class SearchEndpointException extends Exception {
	
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
