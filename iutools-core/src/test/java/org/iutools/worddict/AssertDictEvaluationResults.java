package org.iutools.worddict;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;
import org.iutools.concordancer.tm.TMEvaluator;
import org.iutools.worddict.MultilingualDict.*;
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
		WhatTerm where, int expTotal) {
		long gotTotal = results().totalIUPresent(where);
		Assertions.assertEquals(
			expTotal, gotTotal,
			baseMessage+"Wrong number of glossary entries that contain the IU term in "+ where
		);
		return this;
	}

	public DictEvaluationResults results() {
		return (DictEvaluationResults)gotObject;
	}

	public AssertDictEvaluationResults iuPresentHistogramEquals(
		FrequencyHistogram<MultilingualDict.WhatTerm> expIUPresent_hist)
	throws Exception {

		AssertObject.assertDeepEquals(
			baseMessage+"\nWrong histogram for IU term presence",
			expIUPresent_hist, results().iuPresent_hist
		);
		return this;
	}

	public AssertDictEvaluationResults iuSpottedHistogramEquals(
		FrequencyHistogram<TMEvaluator.MatchType> expIUSpotted_hist)
	throws Exception {

		AssertObject.assertDeepEquals(
			baseMessage+"\nWrong histogram for EN translation spotted",
			expIUSpotted_hist, results().iuSpotted_hist
		);
		return this;
	}

}
