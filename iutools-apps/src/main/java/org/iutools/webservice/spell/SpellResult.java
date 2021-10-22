package org.iutools.webservice.spell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellResult extends EndpointResult {

	public List<SpellingCorrection> correction = new ArrayList<SpellingCorrection>();
	public Boolean providesSuggestions = true;

	public SpellResult() {
		super();
	}

	public SpellResult addCorrection(String orig, String... suggestions) {
		boolean wasMisspelled = false;
		if (suggestions.length > 1 ||
			suggestions.length == 1 && !suggestions[0].equals(orig)) {
			wasMisspelled = true;
		}
		SpellingCorrection corr =
			new SpellingCorrection(orig, suggestions, wasMisspelled);
		correction.add(corr);
		return this;
	}

	@Override
	public JSONObject resultLogEntry() throws ServiceException {
		JSONObject entry = null;
		if (correction != null && correction.size() > 0) {
			Map<String,Object> entryMap = new HashMap<String,Object>();
			List<Object> misspelledWords = new ArrayList<Object>();
			entryMap.put("misspelledWords", misspelledWords);
			for (SpellingCorrection aCorrection: correction) {
				if (aCorrection == null || !aCorrection.wasMispelled) {
					continue;
				}
				List<String> suggestions = aCorrection.getPossibleSpellings();
				if (suggestions != null && suggestions.size() > 0) {
					String[] suggestionsArr = suggestions.toArray(new String[0]);
					HashMap<String,Object> aCorrectionMap = new HashMap<String,Object>();
					aCorrectionMap.put("orig", aCorrection.orig);
					aCorrectionMap.put("suggestedSpellings", suggestionsArr);
					misspelledWords.add(aCorrectionMap);
				}
				try {
					String jsonStr = new ObjectMapper().writeValueAsString(entryMap);
					entry = new JSONObject(jsonStr);
				} catch (JsonProcessingException e) {
					throw new ServiceException(e);
				}
			}
		}
		return entry;
	}
}