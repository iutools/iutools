package org.iutools.webservice.morphdict;

import org.iutools.webservice.ServiceException;

public class MorphemeDictException extends ServiceException {
	public MorphemeDictException(String mess, Exception e) {
		super(mess, e);
	}

	public MorphemeDictException(Exception e) {
		super(e);
	}

	public MorphemeDictException(String mess) {
		super(mess);
	}
}
