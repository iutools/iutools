package org.iutools.webservice;


import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;

import javax.servlet.http.HttpServletResponse;

public class AssertServletResponse extends Asserter<HttpServletResponse> {

	Class<? extends EndpointResult> responseClass = null;

	private ObjectMapper mapper = new ObjectMapper();

	public AssertServletResponse(HttpServletResponse _gotObject) {
		super(_gotObject);
	}

	public AssertServletResponse(HttpServletResponse _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertServletResponse(MockHttpServletResponse _gotResponse,
		Class<? extends EndpointResult> _responseClass) {
		super(_gotResponse);
		this.responseClass = _responseClass;
	}

	public HttpServletResponse servletResponse() {
		return (HttpServletResponse) gotObject;
	}

	protected EndpointResult endpointResult() throws Exception {
		String respJson = servletResponse().getOutputStream().toString();
		EndpointResult result = mapper.readValue(respJson, responseClass);
		return result;
	}

	public AssertServletResponse reportsNoException() throws Exception {
		EndpointResult result = endpointResult();
		Assertions.assertNull(result.errorMessage,
			baseMessage+"\nUnexpected exception: "+result.errorMessage);
		return this;
	}

	public AssertServletResponse reportsException(String expMess)
		throws Exception {

		EndpointResult result = endpointResult();
		AssertString.assertStringContains(
		baseMessage + "\nException was not as expected",
			result.errorMessage, expMess);
		return this;
	}
}