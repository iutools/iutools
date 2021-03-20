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
}
