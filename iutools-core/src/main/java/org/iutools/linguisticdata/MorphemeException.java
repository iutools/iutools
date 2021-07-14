package org.iutools.linguisticdata;

public class MorphemeException extends Exception {
	public MorphemeException(String mess, Exception e) {
		super(mess, e);
	}
	public MorphemeException(String mess) {
		super(mess);
	}
	public MorphemeException(Exception e) {
		super(e);
	}
}
