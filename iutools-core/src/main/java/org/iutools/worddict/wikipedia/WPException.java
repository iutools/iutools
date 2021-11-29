package org.iutools.worddict.wikipedia;

public class WPException extends Exception {
	public WPException(String mess, Exception exc) {
		super(mess, exc);
	}
	public WPException(Exception exc) {
		super(exc);
	}
	public WPException(String mess) {
		super(mess);
	}
}
