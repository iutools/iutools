package org.iutools.nlp;

public class StopWordsException extends Exception {
	StopWordsException(String mess, Exception e) {
		super(mess, e);
	}
	StopWordsException(String mess) {
		super(mess);
	}
	StopWordsException(Exception e) {
		super(e);
	}
}
