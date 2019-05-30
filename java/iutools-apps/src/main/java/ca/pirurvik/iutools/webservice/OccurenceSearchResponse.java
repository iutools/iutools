package ca.pirurvik.iutools.webservice;

import java.util.HashMap;

import ca.nrc.datastructure.Pair;

public class OccurenceSearchResponse extends ServiceResponse {
	public HashMap<String,Pair<String,Pair<String,Long>[]>> matchingWords;
	public HashMap<String,Object> exampleWord;
}
