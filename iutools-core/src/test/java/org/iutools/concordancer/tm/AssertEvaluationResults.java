package org.iutools.concordancer.tm;

import ca.nrc.testing.Asserter;
import org.iutools.worddict.EvaluationResults;
import org.junit.jupiter.api.Assertions;

public class AssertEvaluationResults extends Asserter<EvaluationResults> {

	public AssertEvaluationResults(EvaluationResults _gotObject) {
		super(_gotObject);
	}

	public AssertEvaluationResults(EvaluationResults _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertEvaluationResults totalGlossaryEntries(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalEntries,
			baseMessage+"\nWrong number of glossary entries");
		return this;
	}

	public AssertEvaluationResults totaIUPresent_Orig(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalIUPresent_Orig,
			baseMessage+"\nWrong number of IU terms that were found in the hansard");
		return this;
	}


	public AssertEvaluationResults totalENPresent_Orig_Strict(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalENPresent_Orig_Strict,
			baseMessage+"\nWrong number of IU terms for which we found alignments with *BOTH* IU and EN sides present (in the STRICT sense)");
		return this;
	}

	public AssertEvaluationResults totalENPresent_Orig_Lenient(int expTotal) {
		Assertions.assertEquals(
		expTotal, results().totalENPresent_Orig_Lenient,
		baseMessage+"\nWrong number of IU terms for which we found alignments with *BOTH* IU and EN sides present (in the LENIENT sense)");
		return this;
	}

	public AssertEvaluationResults totalOnlyIUPresent_Orig(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalOnlyIUPresentOrig(),
			baseMessage+"\nWrong number of IU terms for which we found alignments with *ONLY* IU side present");
		return this;
	}


	protected EvaluationResults results() {
		return (EvaluationResults) gotObject;
	}
}
