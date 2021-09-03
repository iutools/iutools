package org.iutools.worddict;

public class MultilingualDictException extends Exception {
	public MultilingualDictException(String mess, Exception e) {
		super(mess, e);
	}
	public MultilingualDictException(String mess) {
		super(mess);
	}
	public MultilingualDictException(Exception e) {
		super(e);
	}
}
