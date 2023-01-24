package org.iutools.webservice.morphdict;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class MorphemeDictInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws ServiceException {
		return new MorphemeDictInputs("tut", "verb", "step");
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new MorphemeDictInputs("siuq", (String)null, "50");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"canonicalForm\":\"siuq\",\"grammar\":null,\"iuAlphabet\":\"ROMAN\",\"meaning\":null,\"nbExamples\":\"50\",\"taskElapsedMsecs\":null}");
			;
	}
}