package org.iutools.webservice;

import ca.nrc.testing.Asserter;

public class AssertEndpointResult extends Asserter<EndpointResult> {
	public AssertEndpointResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertEndpointResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}
}