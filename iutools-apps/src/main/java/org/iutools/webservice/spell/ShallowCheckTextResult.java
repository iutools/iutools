package org.iutools.webservice.spell;

import org.iutools.webservice.EndpointResult;

import java.util.Map;

public class ShallowCheckTextResult extends EndpointResult {

	public String origText = null;
	public String correctedText = null;
	public Map<String,String> misspelledWords = null;

	public ShallowCheckTextResult(String _orig, String _correctedText,
		Map<String,String> _correctionsMade) {
		origText = _orig;
		correctedText = _correctedText;
		misspelledWords = _correctionsMade;
	}
}
