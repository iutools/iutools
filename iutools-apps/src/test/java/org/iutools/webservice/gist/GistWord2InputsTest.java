package org.iutools.webservice.gist;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class GistWord2InputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new GistWord2Inputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new GistWord2Inputs("inuksuk");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"taskID\":null,\"word\":\"inuksuk\",\"wordRomanized\":\"inuksuk\"}");
			;

		inputs =
			new GistWord2Inputs("ᐃᓄᒃᓱᒃ");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"taskID\":null,\"word\":\"ᐃᓄᒃᓱᒃ\",\"wordRomanized\":\"inuksuk\"}");
			;
	}
}