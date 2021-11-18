package org.iutools.webservice.morphexamples;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class MorphemeExamplesInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws ServiceException {
		return new MorphemeExamplesInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new MorphemeExamplesInputs("siuq", (String)null, "50");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"corpusName\":null,\"iuAlphabet\":null,\"nbExamples\":\"50\",\"taskElapsedMsecs\":null,\"wordPattern\":\"siuq\"}");
			;
	}
}