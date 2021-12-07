package org.iutools.concordancer.tm;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import org.iutools.concordancer.tm.TMEvaluator.MatchType;

public class EvaluationResults {

	/** Frequency at which the EN term was found to be PRESENT in the tm,
	 *  in a given sense (STRICT, LENIENT, LENIENT_OVERLAP)
	 */
	FrequencyHistogram<MatchType> enPresent_Histogram =
		new FrequencyHistogram<MatchType>();

	/** Frequency at which the EN translation was SPOTTED in the tm,
	 *  in a given sense (STRICT, LENIENT, LENIENT_OVERLAP)
	 */
	FrequencyHistogram<MatchType> enSpotted_Histogram =
		new FrequencyHistogram<MatchType>();

	/** Number of glossary entries evaluated */
	public int totalEntries = 0;

	/** Number of glossary entries for which the ORIGINAL IU term was FOUND in
	   the translation memory */
	public int totalIUPresent_Orig = 0;

	/** Number of glossary entries for which we found an alignment that:
	 * - contain the ORIGINAL IU term
	 * - contains an EN translation that matches EN term (in the STRICT sense) */
	public int totalENPresent_Strict = 0;

	/** Number of glossary entries for which we found an alignment that:
	 * - contain the ORIGINAL IU term
	 * - contains an EN translation that matches EN term (in the LENIENT sense) */
	public int totalENPresent_Lenient = 0;

	/** Number of glossary entries for which we found an alignment that:
	 * - contain the ORIGINAL IU term
	 * - contains an EN translation that matches EN term (in the LENIENT OVERLAP sense) */
	public int totalENPresent_Lenientoverlap = 0;

	/** Number of glossary entries for which we were able to SPOT a
	 *  translation that matches the EN term in the STRICT sense  */
	public int totalENSpotted_Strict =0 ;

	/** Number of glossary entries for which we were able to SPOT a
	 *  translation that matches the EN term in the LENIENT sense  */
	public int totalENSpotted_Lenient = 0;

	/** Number of glossary entries for which we were able to SPOT a
	 *  translation that matches the EN term in the LENIENT OVERLAP sense  */
	public int totalENSpotted_Lenientoverlap = 0;

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

	public double rateENSpotted_inSense(MatchType sense) {
		double rate = 0.0;
		long total = totalEnPresent_atLeastInSense(sense);
		if (total > 0) {
			rate = 1.0 * totalEnSpotted_atLeastInSense(sense) / total;
		}
		return rate;
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

	public EvaluationResults onEnPresent(MatchType matchType) {
		enPresent_Histogram.updateFreq(matchType);
		return this;
	}

	public long totalEnPresent_inSense(MatchType matchType) {
		long total = enPresent_Histogram.frequency(matchType);
		return total;
	}

	public long totalEnPresent_atLeastInSense(MatchType sense) {
		long total = 0;
		for (MatchType otherSense: MatchType.values()) {
			if (otherSense == sense ||
				!TMEvaluator.isMoreLenient(otherSense, sense)) {
				total += totalEnPresent_inSense(otherSense);
			}
		}
		return total;
	}

	public EvaluationResults onEnSpotted(MatchType matchType) {
		enSpotted_Histogram.updateFreq(matchType);
		return this;
	}

	public long totalEnSpotted_inSense(MatchType matchType) {
		long total = enSpotted_Histogram.frequency(matchType);
		return total;
	}

	public long totalEnSpotted_atLeastInSense(MatchType sense) {
		long total = 0;
		for (MatchType otherSense: MatchType.values()) {
			if (otherSense == sense ||
				!TMEvaluator.isMoreLenient(otherSense, sense)) {
				total += totalEnSpotted_inSense(otherSense);
			}
		}
		return total;
	}


}
