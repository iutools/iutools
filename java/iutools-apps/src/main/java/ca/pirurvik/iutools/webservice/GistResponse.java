package ca.pirurvik.iutools.webservice;

import ca.inuktitutcomputing.morph.Decomposition;
import ca.nrc.datastructure.Pair;

public class GistResponse {

	public Decomposition[] decomps;
	public Pair<String,String>[] sentencePairs;
	
	public GistResponse() {}
}
