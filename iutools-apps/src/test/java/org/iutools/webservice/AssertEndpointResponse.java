package org.iutools.webservice;

import ca.nrc.testing.Asserter;

public class AssertEndpointResponse extends Asserter<EndpointResult> {
	public AssertEndpointResponse(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertEndpointResponse(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}
}