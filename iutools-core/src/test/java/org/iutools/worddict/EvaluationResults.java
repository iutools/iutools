package org.iutools.worddict;

public class EvaluationResults {
	/** Number of glossary entries evaluated */
	public int totalEntries = 0;

	/** Number of glossary entries for which the ORIGINAL IU term was found in
	   the translation memory */
	public int totalIUPresent = 0;

	/** Number of glossary entries for which at least one of the RELATED IU
		term was found in the translation memory */
	public int totalRelatedIUPresent = 0;

//	public int totalEntriesPresent = 0;

	/** Number of glossary entries for which we were able to find an EXACT match
	 *  for the translation of the ORIGINAL IU term */
	public double totalExactSpotOrig =0 ;

	/** Number of glossary entries for which we were able to find a PARTIAL match
	 *  for the translation of the ORIGINAL IU term */
	public double totalPartialSpotOrig = 0;


	/** Number of glossary entries for which we were able to find an EXACT match
	 *  for the translation of the RELATED IU terms */
	public double totalExactSpotRelated =0 ;

	/** Number of glossary entries for which we were able to find a PARTIAL match
	 *  for the translation of the RELATED IU terms */
	public double totalPartialSpotRelated = 0;

}
