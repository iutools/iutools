package org.iutools.morph.l2r;

@SuppressWarnings("serial")
public class WordAnalyzerException extends Exception {
	
	public WordAnalyzerException(String message) {
		super(message);
	}

	public WordAnalyzerException(Exception e) {
		super(e);
	}

}
