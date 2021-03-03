package org.iutools.morphrelatives;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.junit.Assert;

import java.util.List;

public class AssertWordOutcome extends Asserter<WordOutcome> {
	public AssertWordOutcome(WordOutcome _gotObject) {
		super(_gotObject);
	}

	public AssertWordOutcome(WordOutcome _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public WordOutcome outcome() {
		return this.gotObject;
	}

	public AssertWordOutcome relativesProducedAre(String... expProduced)
		throws Exception {
		String[] gotProduced = outcome().relsProduced;
		AssertObject.assertDeepEquals(
			baseMessage+"\nList of produced relatives was not as expected",
			expProduced, gotProduced);
		return this;
	}

	public AssertWordOutcome correctRelativesAre(
		String[] gsRelatives, String... expCorrect)
		throws Exception {
		List<String> gotCorrect = outcome().correctRelatives(gsRelatives);
		AssertObject.assertDeepEquals(
			baseMessage+"\nList of correct relatives was not as expected",
			expCorrect, gotCorrect);

		return this;
	}

	public AssertWordOutcome incorrectRelativesAre(
	String[] gsRelatives, String... expIncorrect)
		throws Exception {
		List<String> gotIncorrect = outcome().incorrectRelatives(gsRelatives);
		AssertObject.assertDeepEquals(
			baseMessage+"\nList of incorrect relatives was not as expected",
			expIncorrect, gotIncorrect);

		return this;
	}

	public AssertWordOutcome prettyPrintIs(
	String[] gsRelatives, String expPPrint)
		throws Exception {
		String gotPPrint = outcome().fitnessToGoldStandard(gsRelatives);
		AssertString.assertStringEquals(
			baseMessage+"\nPretty print not as expected",
			expPPrint, gotPPrint);
		return this;
	}

	public AssertWordOutcome precisionIs(
		String[] gsRelatives, double expPrec) {
		double gotPrec = outcome().precision(gsRelatives);
		Assert.assertEquals(
		baseMessage+"\nPrecision not as expected",
			expPrec, gotPrec, 0.01);
		return this;
	}

	public AssertWordOutcome recallIs(
		String[] gsRelatives, double expRec) {
		double gotRec = outcome().recall(gsRelatives);
		Assert.assertEquals(
		baseMessage+"\nRecall not as expected",
			expRec, gotRec, 0.01);
		return this;
	}
}
