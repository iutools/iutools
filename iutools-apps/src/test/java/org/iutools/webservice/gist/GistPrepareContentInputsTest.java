package org.iutools.webservice.gist;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class GistPrepareContentInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws ServiceException {
		return new GistPrepareContentInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new GistPrepareContentInputs("inuksuk, nunavut");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"totalWords\":2,\"type\":\"text\"}");
			;

		inputs =
			new GistPrepareContentInputs("http://www.somewhere.com/hello.html");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"address\":\"http://www.somewhere.com/hello.html\",\"host\":\"www.somewhere.com\",\"type\":\"url\"}");
	}
}