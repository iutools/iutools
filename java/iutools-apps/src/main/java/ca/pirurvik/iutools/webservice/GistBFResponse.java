package ca.pirurvik.iutools.webservice;

import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;;

public class GistBFResponse extends ServiceResponse {
	public DecompositionExpression[] decompositions;
	public String[] meanings;
	public String[] alignments;
}
