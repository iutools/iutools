package ca.pirurvik.iutools.testing;

import ca.nrc.ui.web.testing.MockHttpServletResponse;

public class EndpointAssertion {
	
	protected String baseMessage = "";
	protected MockHttpServletResponse gotResponse = null;

	public EndpointAssertion(MockHttpServletResponse response, String mess) {
		gotResponse = response;
		baseMessage = mess;
	}	
}
