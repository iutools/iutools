package org.iutools.morph;

import org.iutools.linguisticdata.LinguisticDataException;

public class DecompositionException extends Exception {
	public DecompositionException(String mess, Exception e) {
		super(mess, e);
	}

	public DecompositionException(Exception e) {
		super(e);
	}

	public DecompositionException(String mess) {
		super(mess);
	}
}
