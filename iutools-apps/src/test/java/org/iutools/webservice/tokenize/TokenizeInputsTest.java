package org.iutools.webservice.tokenize;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class TokenizeInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new TokenizeInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		TokenizeInputs inputs =
			new TokenizeInputs("inukkksuk nunavut");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"maxWords\":null,\"totalWords\":2}");
			;
	}
}
