package org.iutools.webservice.morphexamples;

import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.webservice.EndpointResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.iutools.webservice.MorphemeSearchResult;
import org.json.JSONObject;

public class MorphemeExamplesResult extends EndpointResult {

	public Map<String, MorphemeSearchResult> matchingWords = new HashMap<String, MorphemeSearchResult>();

	public Set<String> matchingMorphemeIDs() {
		return matchingWords.keySet();
	}

	public Set<MorphemeHumanReadableDescr> matchingMorphemesDescr() {
		Set<MorphemeHumanReadableDescr> matchingDescrs =
			new HashSet<MorphemeHumanReadableDescr>();
		for (String morph: matchingMorphemeIDs()) {
			matchingDescrs.add(matchingWords.get(morph).morphDescr);
		}
		return matchingDescrs;
	}

	@Override
	public JSONObject resultLogEntry() {
		return null;
	}
}