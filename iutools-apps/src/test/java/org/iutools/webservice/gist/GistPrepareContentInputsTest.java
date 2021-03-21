package org.iutools.webservice.gist;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class GistPrepareContentInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() {
		return new ServiceInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new GistPrepareContentInputs("inuksuk, nunavut");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"taskID\":null,\"totalWords\":2,\"type\":\"text\"}");
			;

		inputs =
			new GistPrepareContentInputs("http://www.somewhere.com/hello.html");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"address\":\"http://www.somewhere.com/hello.html\",\"host\":\"www.somewhere.com\",\"taskID\":null,\"type\":\"url\"}");
	}
}