package org.iutools.worddict;

public class TermNormalizerException extends Exception {
	public TermNormalizerException(String mess) {
		super(mess);
	}
	public TermNormalizerException(Exception e) {
		super(e);
	}
}
