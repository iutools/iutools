package org.iutools.worddict;

import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

public class AssertDictEvaluationResults extends Asserter<DictEvaluationResults> {
	public AssertDictEvaluationResults(DictEvaluationResults _gotObject) {
		super(_gotObject);
	}

	public AssertDictEvaluationResults(DictEvaluationResults _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertDictEvaluationResults totalGlossaryEntries(int expTotal) {
		Assertions.assertEquals(
			expTotal, results().totalGlossaryEntries,
			"Wrong number of glossary entries evaluated"
		);
		return this;
	}

	public AssertDictEvaluationResults totalIUPresent(
		boolean includingRelatedTerms, int expTotal) {
		int gotTotal = results().totalIUPresent(includingRelatedTerms);
		Assertions.assertEquals(
			expTotal, gotTotal,
			baseMessage+"Wrong number of glossary entries that contain the IU term (includingRelatedTerms="+includingRelatedTerms+")"
		);
		return this;
	}

	public DictEvaluationResults results() {
		return (DictEvaluationResults)gotObject;
	}
}
