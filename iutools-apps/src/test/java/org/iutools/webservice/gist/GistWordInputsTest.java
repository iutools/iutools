package org.iutools.webservice.gist;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class GistWordInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new GistWordInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new GistWordInputs("inuksuk");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"taskID\":null,\"word\":\"inuksuk\",\"wordRomanized\":\"inuksuk\"}");
			;

		inputs =
			new GistWordInputs("ᐃᓄᒃᓱᒃ");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"taskID\":null,\"word\":\"ᐃᓄᒃᓱᒃ\",\"wordRomanized\":\"inuksuk\"}");
			;
	}
}