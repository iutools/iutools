package org.iutools.webservice.morphexamples;

import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.webservice.EndpointResult;

import java.util.*;

import org.json.JSONObject;

public class MorphemeExamplesResult extends EndpointResult {

	public List<MorphemeHumanReadableDescr> matchingMorphemes =
		new ArrayList<MorphemeHumanReadableDescr>();

	public Map<String,String[]> examplesForMorpheme = new HashMap<String,String[]>();

	public Set<String> matchingMorphemeIDs() {
		return examplesForMorpheme.keySet();
	}

	public Set<MorphemeHumanReadableDescr> matchingMorphemesDescr() {
		Set<MorphemeHumanReadableDescr> matchingDescrs =
			new HashSet<MorphemeHumanReadableDescr>();
		matchingDescrs.addAll(matchingMorphemes);
		return matchingDescrs;
	}

	@Override
	public JSONObject resultLogEntry() {
		return null;
	}
}