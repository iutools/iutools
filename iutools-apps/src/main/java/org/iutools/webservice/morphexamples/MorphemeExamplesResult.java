package org.iutools.webservice.morphexamples;

import org.iutools.webservice.EndpointResult;

import java.util.HashMap;
import java.util.Map;

public class MorphemeExamplesResult<MorphemeSearchResult> extends EndpointResult {

	public Map<String, MorphemeSearchResult> matchingWords =
	new HashMap<String, MorphemeSearchResult>();
}