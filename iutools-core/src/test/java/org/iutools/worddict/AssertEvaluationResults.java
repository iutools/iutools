package org.iutools.worddict;

import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

public class AssertEvaluationResults extends Asserter<EvaluationResults> {
	public AssertEvaluationResults(EvaluationResults _gotObject) {
		super(_gotObject);
	}

	public AssertEvaluationResults(EvaluationResults _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertEvaluationResults totalEntries(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalEntries,
			baseMessage + "\nTotal number of glossary entries evalauted was wrong"
		);
		return this;
	}

	public AssertEvaluationResults totalEntriesWithIU(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalIUPresent_Orig,
			baseMessage + "\nWrong number of glossary entries that contain the IU term"
		);

		return this;
	}

	public AssertEvaluationResults totalEntriesWithRelatedIU(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalRelatedIUPresent,
			baseMessage + "\nWrong number of glossary entries that contained a RELATED IU term"
		);

		return this;
	}

	public AssertEvaluationResults totalExactSpotOrig(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalENSpotted_Strict,
			baseMessage + "\nWrong number of glossary entries for which we found a PERFECT match for the translation of the ORIGINAL IU term "
		);

		return this;
	}

	public AssertEvaluationResults totalPartialSpotOrig(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalENSpotted_Lenient,
			baseMessage + "\nWrong number of glossary entries for which we found a PARTIAL match for the translation of the ORIGINAL IU term "
		);

		return this;
	}

	public AssertEvaluationResults totalExactSpotRelated(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalExactSpotRelated,
			baseMessage + "\nWrong number of glossary entries for which we found a PERFECT match for the translation of a RELATED IU term "
		);

		return this;
	}

	public AssertEvaluationResults totalPartialSpotRelated(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalPartialSpotRelated,
			baseMessage + "\nWrong number of glossary entries for which we found a PARTIAL match for the translation of a RELATED IU term "
		);

		return this;
	}

	protected EvaluationResults results() {
		return (EvaluationResults)gotObject;
	}

}
