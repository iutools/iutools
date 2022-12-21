package org.iutools.webservice.spell;

import ca.nrc.testing.AssertObject;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.junit.jupiter.api.Assertions;

public class AssertCheckWordResult extends AssertEndpointResult {

	@Override
	protected CheckWordResult result() {
		return (CheckWordResult)gotObject;
	}

	public AssertCheckWordResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertCheckWordResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}
	
	public AssertCheckWordResult correctionIs(Boolean expMisspelled) throws Exception {
		return correctionIs(expMisspelled, null);
	}
	
	public AssertCheckWordResult correctionIs(Boolean expMisspelled,
		String[] expSuggestions) throws Exception {
		
		SpellingCorrection correction = result().correction;

		String wordDescr = "word "+correction.orig+"'";
		
		Assertions.assertEquals(
			expMisspelled, correction.wasMispelled,
			baseMessage+"\nMisspelled status was wrong for "+wordDescr);
		
		if (correction.wasMispelled) {
			AssertObject.assertDeepEquals(
				baseMessage+"\nSuggestions were wrong for"+wordDescr,
				expSuggestions, correction.getAllSuggestions());
		}
		
		return this;
	}
}