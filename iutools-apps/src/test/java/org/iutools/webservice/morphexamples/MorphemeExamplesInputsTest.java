package org.iutools.webservice.morphexamples;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class MorphemeExamplesInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() {
		return new ServiceInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new MorphemeExamplesInputs("siuq", (String)null, "50");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"corpusName\":null,\"nbExamples\":\"50\",\"taskID\":null,\"wordPattern\":\"siuq\"}");
			;
	}
}