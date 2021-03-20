package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class EndpointTest {

	public abstract Endpoint makeEndpoint();

	private ObjectMapper mapper = new ObjectMapper();

	protected Endpoint endPoint = null;

	@BeforeEach
	public void setUp() throws Exception {
		endPoint = makeEndpoint();
	}
}