package ca.pirurvik.iutools.webservice;

import java.util.HashMap;

public class OccurenceSearchResponse extends ServiceResponse {
	public HashMap<String,MorphemeSearchResult> matchingWords;
	public HashMap<String,Object> exampleWord;
}
