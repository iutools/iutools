package org.iutools.webservice.logaction;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointTest;

public class LogActionEndpointTest extends EndpointTest  {

	@Override
	public Endpoint makeEndpoint() {
		return new LogActionEndpoint();
	}
}
