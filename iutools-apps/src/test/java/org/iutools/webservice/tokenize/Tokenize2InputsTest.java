package org.iutools.webservice.tokenize;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class Tokenize2InputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new Tokenize2Inputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		Tokenize2Inputs inputs =
			new Tokenize2Inputs("inukkksuk nunavut");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"taskID\":null,\"text\":\"inukkksuk nunavut\"}");
			;
	}

}
