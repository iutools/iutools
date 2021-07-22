package org.iutools.concordancer;

import ca.nrc.datastructure.Cloner;

public class WordAlignmentException extends Exception {
	public WordAlignmentException(String mess, Exception e) {
		super(mess, e);
	}
	public WordAlignmentException(String mess) {
		super(mess);
	}
	public WordAlignmentException(Exception e) {
		super(e);
	}
}