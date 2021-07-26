package org.iutools.concordancer.tm;

public class WordSpotterException extends Exception {
	public WordSpotterException(String mess, Exception e) {
		super(mess, e);
	}
	public WordSpotterException(String mess) {
		super(mess);
	}
	public WordSpotterException(Exception e) {
		super(e);
	}
}
