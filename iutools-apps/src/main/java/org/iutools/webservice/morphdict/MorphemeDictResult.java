package org.iutools.webservice.morphdict;

import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.webservice.EndpointResult;

import java.util.*;

public class MorphemeDictResult extends EndpointResult {

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
}