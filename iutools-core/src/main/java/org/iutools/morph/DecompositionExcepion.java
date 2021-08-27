package org.iutools.morph;

import org.iutools.linguisticdata.LinguisticDataException;

public class DecompositionExcepion extends Exception {
	public DecompositionExcepion(String mess, Exception e) {
		super(mess, e);
	}

	public DecompositionExcepion(Exception e) {
		super(e);
	}

	public DecompositionExcepion(String mess) {
		super(mess);
	}
}
