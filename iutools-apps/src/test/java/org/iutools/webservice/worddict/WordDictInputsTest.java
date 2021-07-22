package org.iutools.webservice.worddict;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class WordDictInputsTest extends ServiceInputsTest {
	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new WordDictInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		WordDictInputs inputs =
			new WordDictInputs("inuksuk");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"taskID\":null,\"word\":\"inuksuk\"}");
			;
	}

}
