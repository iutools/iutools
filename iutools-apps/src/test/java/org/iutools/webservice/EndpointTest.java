package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class EndpointTest {

	public abstract Endpoint makeEndpoint();

	// This abstract test serves as a reminder that we should test \
	// the logEntry() method for each subclass of Endpoint
	@Test
	public abstract void test__logEntry() throws Exception;

	private ObjectMapper mapper = new ObjectMapper();

	protected Endpoint endPoint = null;

	@BeforeEach
	public void setUp() throws Exception {
		endPoint = makeEndpoint();
	}

	protected void assertLogEntryEquals(ServiceInputs inputs,
		JSONObject expEntry) throws Exception {
		JSONObject gotEntry = endPoint.logEntry(inputs);

		AssertString.assertStringEquals(
			"Log entry not as expected",
			expEntry.toString(), gotEntry.toString()
		);

	}
}