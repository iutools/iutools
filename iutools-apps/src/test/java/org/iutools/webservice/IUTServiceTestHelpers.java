package org.iutools.webservice;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.web.Http;
import ca.nrc.ui.web.testing.MockHttpServletRequest;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.logaction.LogActionEndpoint;
import org.iutools.webservice.relatedwords.RelatedWordsEndpoint;
import org.iutools.webservice.relatedwords.RelatedWordsResponse;

public class IUTServiceTestHelpers {
	public static final long SHORT_WAIT = 2*1000;
	public static final long MEDIUM_WAIT = 2*SHORT_WAIT;
	public static final long LONG_WAIT = 2*MEDIUM_WAIT;

	public enum EndpointNames {
		GIST_PREPARE_CONTENT, GIST_WORD, LOG, MORPHEME,
		RELATED_WORDS, TOKENIZE, SPELL};

	public static MockHttpServletResponse postEndpointDirectly(EndpointNames eptName, Object inputs) throws Exception {
		return postEndpointDirectly(eptName, inputs, false);
	}

	public static void invokeEndpointThroughServer(
		Http.Method method, String endpointPath, ServiceInputs inputs)
		throws Exception {

		String jsonBody = inputs.toString();
		URL url = new URL("http://localhost:8080/iutools/srv/"+endpointPath);
		Http.doRequest(method, url, jsonBody);
	}

	public static MockHttpServletResponse postEndpointDirectly(
		EndpointNames eptName, Object inputs, boolean expectServiceError) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		String jsonBody = new ObjectMapper().writeValueAsString(inputs);
		request.setReaderContent(jsonBody);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		if (eptName == EndpointNames.LOG) {
			new LogActionEndpoint().doPost(request, response);
		} else if (eptName == EndpointNames.RELATED_WORDS) {
			new RelatedWordsEndpoint().doPost(request, response);
		}
		
		String srvErr = ServiceResponse.jsonErrorMessage(response.getOutput());
		if (srvErr != null && ! expectServiceError) {
			throw new Exception("Did not expect the service to return an error message but it did.\nerrorMessage: "+srvErr);
		} else if (srvErr == null && expectServiceError) {
			throw new Exception("Expected the service to return an error message but it did not.");
		}
		
		return response;
	}

	public static RelatedWordsResponse toRelatedWordsResponse(
		MockHttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		RelatedWordsResponse response =
			new ObjectMapper().readValue(responseStr, RelatedWordsResponse.class);
		return response;
	}
}
