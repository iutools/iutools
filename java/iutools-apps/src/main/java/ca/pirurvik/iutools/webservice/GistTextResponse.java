package ca.pirurvik.iutools.webservice;

import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.pirurvik.iutools.concordancer.DocAlignment;


public class GistTextResponse {

	public DecompositionExpression[] decompositions;
	public Alignment[] sentencePairs;
	DocAlignment bilingualAlignments = new DocAlignment().setSuccess(false);
	
	public GistTextResponse() {}
}
