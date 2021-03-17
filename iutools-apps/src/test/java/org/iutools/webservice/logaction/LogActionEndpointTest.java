package org.iutools.webservice.logaction;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class LogActionEndpointTest extends EndpointTest  {

	@Override
	public Endpoint makeEndpoint() {
		return new LogActionEndpoint();
	}

	@Override @Test
	public void test__logEntry() throws Exception {
		JSONObject taskData =
			new JSONObject()
				.put("someField", "someValue");
		LogActionInputs inputs =
			new LogActionInputs(LogActionInputs.Action.SEARCH_WEB, taskData);
		JSONObject expEntry = new JSONObject()
			.put("taskData", taskData)
		;
		assertLogEntryEquals(inputs, expEntry);
	}
}
