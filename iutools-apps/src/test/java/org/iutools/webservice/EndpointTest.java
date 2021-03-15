package org.iutools.webservice;

import ca.nrc.ui.web.testing.MockHttpServletRequest;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;

import javax.servlet.http.HttpServletRequest;

public abstract class EndpointTest {

	public abstract Endpoint makeEndpoint();

	private ObjectMapper mapper = new ObjectMapper();

	protected Endpoint endPoint = null;

	@BeforeEach
	public void setUp() throws Exception {
		endPoint = makeEndpoint();
	}
}