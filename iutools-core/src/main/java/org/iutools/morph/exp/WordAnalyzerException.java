package org.iutools.morph.exp;

import ca.inuktitutcomputing.data.LinguisticDataException;

@SuppressWarnings("serial")
public class WordAnalyzerException extends Exception {
	
	public WordAnalyzerException(String message) {
		super(message);
	}

	public WordAnalyzerException(Exception e) {
		super(e);
	}

}
