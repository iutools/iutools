package org.iutools.worddict;

public class IUWordDictException extends Exception {
	public IUWordDictException(String mess, Exception e) {
		super(mess, e);
	}
	public IUWordDictException(String mess) {
		super(mess);
	}
	public IUWordDictException(Exception e) {
		super(e);
	}
}
