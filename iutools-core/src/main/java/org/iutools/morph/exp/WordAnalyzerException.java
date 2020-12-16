package org.iutools.morph.exp;

@SuppressWarnings("serial")
public class WordAnalyzerException extends Exception {
	
	public WordAnalyzerException(String message) {
		super(message);
	}

	public WordAnalyzerException(Exception e) {
		super(e);
	}

}
