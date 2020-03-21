package ca.pirurvik.iutools.webservice;

import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;
import ca.inuktitutcomputing.utilities.Alignment;


public class GistTextResponse {

	public DecompositionExpression[] decompositions;
	public Alignment[] sentencePairs;
	
	public GistTextResponse() {}
}
