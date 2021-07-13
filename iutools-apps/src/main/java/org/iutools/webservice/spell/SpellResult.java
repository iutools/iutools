package org.iutools.webservice.spell;

import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.EndpointResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellResult extends EndpointResult {

	public List<SpellingCorrection> correction;

	public SpellResult() {
		super();
	}

	@Override
	public JSONObject resultLogEntry() {
		JSONObject entry = null;
		JSONArray misspelledWords = new JSONArray();
		if (correction != null && correction.size() > 0) {
			for (SpellingCorrection aCorrection: correction) {
				if (aCorrection == null || !aCorrection.wasMispelled) {
					continue;
				}
				List<String> suggestions = aCorrection.getPossibleSpellings();
				if (suggestions != null && suggestions.size() > 0) {
					String[] suggestionsArr = suggestions.toArray(new String[0]);
					misspelledWords.put(
						new JSONObject()
							.put("origWord", aCorrection.orig)
							.put("suggestedSpellings", suggestionsArr)
					);
				}
			}
			if (misspelledWords.length() > 0) {
				entry = new JSONObject()
					.put("misspelledWords", misspelledWords)
				;
			}
		}
		return entry;
	}
}