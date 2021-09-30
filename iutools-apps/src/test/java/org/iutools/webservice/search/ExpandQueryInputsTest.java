package org.iutools.webservice.search;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class ExpandQueryInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new ExpandQueryInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new ExpandQueryInputs("inuksuk");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"origQuery\":\"inuksuk\",\"taskElapsedMsecs\":null}");
			;
	}
}