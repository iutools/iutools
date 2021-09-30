package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.json.Mapper;
import org.junit.jupiter.api.Assertions;

import javax.servlet.http.HttpServletResponse;

public class AssertServletResponse extends Asserter<HttpServletResponse> {

	Class<? extends EndpointResult> responseClass = null;

	private Mapper mapper = new Mapper();

	public AssertServletResponse(HttpServletResponse _gotObject) throws Exception {
		super(_gotObject);
		init_AssertServletResponse((Class)null);
	}

	public AssertServletResponse(HttpServletResponse _gotObject, String mess) throws Exception {
		super(_gotObject, mess);
		init_AssertServletResponse((Class)null);
	}

	public AssertServletResponse(MockHttpServletResponse _gotResponse,
		Class<? extends EndpointResult> _responseClass) throws Exception {
		super(_gotResponse);
		init_AssertServletResponse(_responseClass);
	}

	private void init_AssertServletResponse(
		Class<? extends EndpointResult> _responseClass) throws Exception {
		this.responseClass = _responseClass;
		assertSanityCheck();
	}

	private void assertSanityCheck() throws Exception {
		EndpointResult result = endpointResult();
		if (result.errorMessage == null) {
			// Note: If an error message is reported, then it may be that we
			// stopped execution before we were done setting fields of the
			// EnpointResult. So don't do a sanity check on the result in that case.
			Assertions.assertNotNull(result.taskID,
				baseMessage + "\nTask ID of response was not set");
		}
	}

	public MockHttpServletResponse servletResponse() {
		return (MockHttpServletResponse) gotObject;
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

	public AssertServletResponse taskIDequals(String expID) throws Exception {
		return taskIDequals(expID, (String)null);
	}

	public AssertServletResponse taskIDequals(String expID, String mess) throws Exception {
		if (mess == null) {
			mess = "";
		} else {
			mess += "\n";
		}
		String gotID = endpointResult().taskID;
		AssertString.assertStringEquals(
			baseMessage+mess+"\nTask ID was not as expected",
			expID, gotID
		);
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

	public AssertServletResponse jsonContains(String expSubstring) throws Exception {
		String gotJson = servletResponse().getOutput();
		AssertString.assertStringContains(
			baseMessage+"\nResponse output did not contain the expected substring",
			gotJson, expSubstring
		);
		return this;
	}
}