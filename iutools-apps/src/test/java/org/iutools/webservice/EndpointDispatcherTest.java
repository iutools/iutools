package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import ca.nrc.ui.web.testing.MockHttpServletRequest;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.morphexamples.MorphemeExamplesResult;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EndpointDispatcherTest {

	///////////////////////////////////////////////
	// VERIFICATION TEST
	///////////////////////////////////////////////

	@Test
	public void test__doPost__HappyPath() throws Exception {
		JSONObject json = new JSONObject()
			.put("wordPattern", "siuq");
		String uri = "iutools/srv2/morpheme_examples";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, MorphemeExamplesResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__UnknownEndpoint__ReportsException() throws Exception {
		JSONObject json = new JSONObject()
			.put("wordPattern", "siuq");
		String uri = "iutools/srv2/unkown_point";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, MorphemeExamplesResult.class)
			.reportsException("No known endpoint for URI iutools/srv2/unkown_point")
			;
		return;
	}

	@Test
	public void test__doPost__InputJsonHasWrongStructure__ReportsException() throws Exception {
		JSONObject json = new JSONObject()
			.put("wordPattern", "siuq")
			.put("unknownField", "blah");
		String uri = "iutools/srv2/morpheme_examples";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, MorphemeExamplesResult.class)
			.reportsException(
				"JSON inputs did not have the structure of class org.iutools.webservice.morphexamples.MorphemeExamplesInputs")
			;
		return;
	}

	@Test
	public void test__endpointName__HappyPaht() throws Exception {
		for (String endpoint: new String[] {
			"expandquery", "gist/gistword", "gist/gistword",
			"gist/preparecontent", "morpheme_examples", "relatedwords", "spell",
			"tokenize"}) {
			String uri = "iutools/srv2/"+endpoint;
			String gotName = new EndpointDispatcher().endpointName(uri);
			AssertString.assertStringEquals(
			"Endpoint not as expected for uri: "+uri,
			endpoint, gotName);
		}
	}

	@Test
	public void test__endpointName__UnknownEndpoint() {
		Assertions.assertThrows(ServiceException.class, () -> {
			String uri = "iutols/srv2/unknown_point";
			String gotName = new EndpointDispatcher().endpointName(uri);
		});
	}

		///////////////////////////////////////////////
	// TEST HELPERS
	///////////////////////////////////////////////

	private MockHttpServletResponse doPost(String uri, JSONObject json)
		throws Exception {

		MockHttpServletRequest request =
			new MockHttpServletRequest().setURI(uri);
		request.setReaderContent(json.toString());

		MockHttpServletResponse response = new MockHttpServletResponse();
		new EndpointDispatcher().doPost(request, response);

		return response;
	}
}