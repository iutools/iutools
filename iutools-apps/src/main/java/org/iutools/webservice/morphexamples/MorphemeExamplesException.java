package org.iutools.webservice.morphexamples;

import org.iutools.webservice.ServiceException;

public class MorphemeExamplesException extends ServiceException {
	public MorphemeExamplesException(String mess, Exception e) {
		super(mess, e);
	}

	public MorphemeExamplesException(Exception e) {
		super(e);
	}

	public MorphemeExamplesException(String mess) {
		super(mess);
	}
}
