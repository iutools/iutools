package org.iutools.webservice;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

public abstract class ServiceInputsTest {

	protected abstract ServiceInputs makeInputs() throws Exception;
	protected boolean shouldGenerateNullSummary() {return false;}

	ServiceInputs inputs = null;

	@BeforeEach
	public void setUp() throws Exception {
		inputs = makeInputs();
	}

	@Test
	public void test__summarizeForLogging__SanityCheck() throws Exception {
		ServiceInputs input = makeInputs();
		// Note: Normally the action would differ for each type of input, but for
		//   the purpose of this test, we always set it to WORD_LOOKUP.
		input._action = "WORD_LOOKUP";
		Map<String, Object> gotSummary = input.summarizeForLogging();
		String gotSummaryPP = PrettyPrinter.print(gotSummary);
		if (shouldGenerateNullSummary()) {
			Assertions.assertTrue(
				null == gotSummary,
				"Summary should have been null\n"+gotSummaryPP);
		} else {
			String gotAction = (String) gotSummary.get("_action");
			AssertString.assertStringEquals(
				"Action not properly set in the summary.\nSummary was:\n" +
				gotSummaryPP,
				"WORD_LOOKUP", gotAction);
			typeSpecificSummarySanityCheck(gotSummary);
		}
	}

	protected void typeSpecificSummarySanityCheck(Map<String, Object> gotSummary)  {
		// By default, we don't do any further sanit checkts
	}
}
