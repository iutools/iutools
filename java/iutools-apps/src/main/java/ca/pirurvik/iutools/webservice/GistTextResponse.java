package ca.pirurvik.iutools.webservice;

import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.pirurvik.iutools.concordancer.AlignmentResult;


public class GistTextResponse {

	public DecompositionExpression[] decompositions;
	public Alignment[] sentencePairs;
	AlignmentResult bilingualAlignments = new AlignmentResult().setSuccess(false);
	
	public GistTextResponse() {}
}
