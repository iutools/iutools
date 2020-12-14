package org.iutools.concordancer;

public class AlignerException extends Exception {

	AlignerException(String mess, Exception e) {
		super(mess, e);
	}

	AlignerException(String mess) {
		super(mess);
	}

	AlignerException(Exception e) {
		super(e);
	}
}
