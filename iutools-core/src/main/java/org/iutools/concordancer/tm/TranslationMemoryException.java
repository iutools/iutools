package org.iutools.concordancer.tm;

public class TranslationMemoryException extends Exception {
	public TranslationMemoryException(String mess, Exception e) {
		super(mess, e);
	}
	public TranslationMemoryException(String mess) {
		super(mess);
	}
	public TranslationMemoryException(Exception e) {
		super(e);
	}
}
