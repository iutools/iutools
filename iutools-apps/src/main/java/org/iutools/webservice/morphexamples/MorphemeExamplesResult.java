package org.iutools.webservice.morphexamples;

import org.iutools.webservice.EndpointResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.iutools.webservice.MorphemeSearchResult;
import org.json.JSONObject;

public class MorphemeExamplesResult extends EndpointResult {

	public Map<String, MorphemeSearchResult> matchingWords = new HashMap<String, MorphemeSearchResult>();

	public Set<String> matchingMorphemes() {
		return matchingWords.keySet();
	}

	public Set<String> matchingMorphemesDescr() {
		Set<String> matchingDescrs = new HashSet<String>();
		for (String morph: matchingMorphemes()) {
			matchingDescrs.add(matchingWords.get(morph).morphDescr);
		}
		return matchingDescrs;
	}

	@Override
	public JSONObject resultLogEntry() {
		return null;
	}
}