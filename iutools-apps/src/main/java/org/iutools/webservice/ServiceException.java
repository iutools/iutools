package org.iutools.webservice;

public class ServiceException extends Exception {
	
	public ServiceException(String mess) {
		super(mess);
	}

	public ServiceException(String mess, Exception e) {
		super(mess, e);
	}

	public ServiceException(Throwable e) {
		super(e);
	}

}
