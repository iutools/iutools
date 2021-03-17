package org.iutools.webservice.log;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LogEndpointTest extends EndpointTest  {

	@Override
	public Endpoint makeEndpoint() {
		return new LogEndpoint();
	}

	@Override @Test
	public void test__logEntry() throws Exception {
		JSONObject taskData =
			new JSONObject()
				.put("someField", "someValue");
		LogInputs inputs =
			new LogInputs(LogInputs.Action.SEARCH_WEB, taskData);
		JSONObject expEntry = new JSONObject()
			.put("taskData", taskData)
		;
		assertLogEntryEquals(inputs, expEntry);
		;

	}
}
