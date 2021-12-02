package org.iutools.worddict;

public class EvaluationResults {
	/** Number of glossary entries evaluated */
	public int totalEntries = 0;

	/** Number of glossary entries for which the ORIGINAL IU term was FOUND in
	   the translation memory */
	public int totalIUPresent_Orig = 0;

	/** Number of glossary entries for which the EN term was found in
	 the translation memory for an alignemtn with the ORIGINAL IU term
	 on the left side (in the STRICT sense) */
	public int totalENPresent_Strict = 0;


	/** Number of glossary entries for which the EN term was found in
	 the translation memory for an alignemtn with the ORIGINAL IU term
	 on the left side (in the LENIENT sense) */
	public int totalENPresent_Lenient = 0;

	/** Number of glossary entries for which at least one of the RELATED IU
		term was found in the translation memory */
	public int totalRelatedIUPresent = 0;

	/** Number of glossary entries for which we were able to find an EXACT
	 *  translation of the ORIGINAL IU term */
	public int totalENSpotted_Strict =0 ;

	/** Number of glossary entries for which we were able to find a PARTIAL
	 *  translation of the ORIGINAL IU term */
	public int totalENSpotted_Lenient = 0;


	/** Number of glossary entries for which we were able to find an EXACT
	 *  translation of the RELATED IU terms */
	public int totalExactSpotRelated =0 ;

	/** Number of glossary entries for which we were able to find a PARTIAL
	 *  translation of the RELATED IU terms */
	public int totalPartialSpotRelated = 0;

	/** Number of glossary entries for which we were able to find an EXACT
	 *  translation of EITHER the orig or related IU terms */
	public int totalExactSpotAny =0 ;

	/** Number of glossary entries for which we were able to find a PARTIAL
	 *  translation of EITHER the orig or related IU terms  */
	public int totalPartialSpotAny = 0;

	public int totalOnlyIUPresentOrig() {
		return totalIUPresent_Orig - totalENPresent_Strict;
	}

	/** Number of glossary entries for which the ORIGINAL IU term was ABSENT in
	 the translation memory */
	public int totalIUAbsentOrig() {
		return totalEntries - totalIUPresent_Orig;
	}

	public int totalENAbsent_Orig_Strict() {
		return totalIUPresent_Orig - totalENPresent_Strict;
	}

	public int totalENAbsent_Orig_Lenient() {
		return totalIUPresent_Orig - totalENPresent_Lenient;
	}

	public double rateENSpotted_Strict() {
		double rate = 0.0;
		if (totalENPresent_Strict > 0) {
			rate = 1.0 * totalENSpotted_Strict / totalENPresent_Strict;
		}
		return rate;
	}

	public double rateENSpotted_Lenient() {
		double rate = 0.0;
		if (totalENPresent_Lenient > 0) {
			rate = 1.0 * totalENSpotted_Lenient / totalENPresent_Lenient;
		}
		return rate;
	}

}
