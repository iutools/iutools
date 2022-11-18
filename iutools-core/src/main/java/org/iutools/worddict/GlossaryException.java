package org.iutools.worddict;

public class GlossaryException extends Exception {
	public GlossaryException(String mess) {
		super(mess);
	}
	public GlossaryException(String mess, Exception e) {
		super(mess, e);
	}
	public GlossaryException(Exception e) {
		super(e);
	}
}
