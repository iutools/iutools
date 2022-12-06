package org.iutools.webservice.spell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckWordResult extends EndpointResult {

	public SpellingCorrection correction = null;
	public Boolean providesSuggestions = true;

	public CheckWordResult() {
		super();
	}

	public CheckWordResult setCorrection(String orig, String... suggestions) {
		boolean wasMisspelled = false;
		if (suggestions.length > 1 ||
			suggestions.length == 1 && !suggestions[0].equals(orig)) {
			wasMisspelled = true;
		}
		correction =
			new SpellingCorrection(orig, suggestions, wasMisspelled);
		return this;

	}

	@Override
	public JSONObject resultLogEntry() throws ServiceException {
		JSONObject entry = null;
		if (correction != null && correction.wasMispelled) {
			Map<String,Object> entryMap = new HashMap<String,Object>();
			entryMap.put("misspelledWord", correction.orig);
			try {
				String jsonStr = new ObjectMapper().writeValueAsString(entryMap);
				entry = new JSONObject(jsonStr);
			} catch (JsonProcessingException e) {
				throw new ServiceException(e);
			}
		}
		return entry;
	}
}