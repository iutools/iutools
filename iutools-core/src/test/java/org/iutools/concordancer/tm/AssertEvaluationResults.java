package org.iutools.concordancer.tm;

import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;
import org.iutools.concordancer.tm.TMEvaluator.MatchType;

public class AssertEvaluationResults extends Asserter<EvaluationResults> {

	public AssertEvaluationResults(EvaluationResults _gotObject) {
		super(_gotObject);
	}

	public AssertEvaluationResults(EvaluationResults _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertEvaluationResults totalSingleIUTermEntries(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalSingleIUWordEntries,
			baseMessage+"\nWrong number of single IU word glossary entries");
		return this;
	}

	public AssertEvaluationResults totalGlossaryEntries(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalEntries,
			baseMessage+"\nWrong number of glossary entries");
		return this;
	}

	public AssertEvaluationResults totaIUPresent(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalIUPresent_Orig,
			baseMessage+"\nWrong number of IU terms that were found in the hansard");
		return this;
	}


	public AssertEvaluationResults totalENPresent_Strict(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalENPresent_Strict,
			baseMessage+"\nWrong number of IU terms for which we found alignments with *BOTH* IU and EN sides present (in the STRICT sense)");
		return this;
	}

	public AssertEvaluationResults totalENPresent_Lenient(int expTotal) {
		Assertions.assertEquals(
		expTotal, results().totalENPresent_Lenient,
		baseMessage+"\nWrong number of IU terms for which we found alignments with *BOTH* IU and EN sides present (in the LENIENT sense)");
		return this;
	}


	public AssertEvaluationResults totalENSpotted_Strict(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalENSpotted_Strict,
			baseMessage+"\nWrong number of IU terms for which the EN translation was SPOTTED in the STRICT sense");
		return this;
	}

	public AssertEvaluationResults totalENSpotted_Lenient(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalENSpotted_Lenient,
			baseMessage+"\nWrong number of IU terms for which the EN translation was SPOTTED in the LENIENT sense");
		return this;
	}

	public AssertEvaluationResults totalENSpotted_LenientOverlap(int i) {
//		Assertions.fail("Implement this assertion");
		return this;
	}

	public AssertEvaluationResults rateENSpotted_Strict(double expPerc) {
		AssertNumber.assertEquals(
			baseMessage+"\nWrong perecentage of IU terms for which the EN translation was SPOTTED in the STRICT sense",
			expPerc, results().rateENSpotted_Strict(), 0.001);
		return this;
	}

	public AssertEvaluationResults rateENSpotted_Lenient(double expPerc) {
		AssertNumber.assertEquals(
			baseMessage+"\nWrong perecentage of IU terms for which the EN translation was SPOTTED in the LENIENT sense",
			expPerc, results().rateENSpotted_Lenient(), 0.01);
		return this;
	}

	public AssertEvaluationResults rateENSpotted_LenientOverlap(int i) {
//		Assertions.fail("Implement this assertion");
		return this;
	}

	public AssertEvaluationResults totalENPresent_inSense(MatchType matchType, int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalEnPresent_inSense(matchType),
			baseMessage+"\nWrong total for glossary entries for which the EN term was PRESENT in sense "+matchType
		);
		return this;
	}

	public AssertEvaluationResults totalENPresent_atLeastInSense(MatchType matchType, int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalEnPresent_atLeastInSense(matchType),
			baseMessage+"\nWrong total for glossary entries for which the EN term was PRESENT in AT LEAST sense "+matchType
		);
		return this;
	}
	public AssertEvaluationResults totalENSpotted_inSense(MatchType matchType, int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalEnSpotted_inSense(matchType),
			baseMessage+"\nWrong total for glossary entries for which the EN translation was SPOTTED in sense "+matchType
		);
		return this;
	}

	public AssertEvaluationResults totalENSpotted_atLeastInSense(MatchType sense, int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalEnSpotted_atLeastInSense(sense),
			baseMessage+"\nWrong total for glossary entries for which the EN term was SPOTTED in AT LEAST sense "+sense
		);
		return this;
	}

	public AssertEvaluationResults rateENSpotted_inSense(MatchType sense, double expRate) {
		AssertNumber.assertEquals(
			baseMessage+"\nWrong perecentage of IU terms for which the EN translation was SPOTTED in the STRICT sense",
			expRate, results().rateENSpotted_inSense(sense), 0.001);
		return this;
	}


	protected EvaluationResults results() {
		return (EvaluationResults) gotObject;
	}

}
