package org.iutools.webservice;

import ca.nrc.testing.Asserter;

import javax.servlet.http.HttpServletResponse;

public class AssertResponse extends Asserter<ServiceResponse> {
	public AssertResponse(ServiceResponse _gotObject) {
		super(_gotObject);
	}

	public AssertResponse(ServiceResponse _gotObject, String mess) {
		super(_gotObject, mess);
	}
}
