package org.iutools.webservice.gist;

import com.fasterxml.jackson.databind.ObjectMapper;
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
	public void test__DELETEME() throws Exception {
		String jsonStr = "{\"_taskID\":\"2021-09-30T12:48:51.863Z\"}";
		new ObjectMapper().readValue(jsonStr, GistPrepareContentInputs.class);
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new GistPrepareContentInputs("inuksuk, nunavut");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"taskElapsedMsecs\":null,\"totalWords\":2,\"type\":\"text\"}");
			;

		inputs =
			new GistPrepareContentInputs("http://www.somewhere.com/hello.html");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":null,\"_taskID\":null,\"_taskStartTime\":null,\"address\":\"http://www.somewhere.com/hello.html\",\"host\":\"www.somewhere.com\",\"taskElapsedMsecs\":null,\"type\":\"url\"}");
	}
}