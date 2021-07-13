package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import ca.nrc.ui.web.testing.MockHttpServletRequest;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.gist.GistPrepareContentResult;
import org.iutools.webservice.gist.GistWordResult;
import org.iutools.webservice.logaction.LogActionInputs;
import org.iutools.webservice.morphexamples.MorphemeExamplesResult;
import org.iutools.webservice.search.ExpandQueryResult;
import org.iutools.webservice.spell.SpellResult;
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
		String uri = "iutools/srv2/morpheme_dictionary";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, MorphemeExamplesResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__InputAlreadyHasTaskID__ResultUsesSameID() throws Exception {
		String id = "someid";
		JSONObject json = new JSONObject()
			.put("wordPattern", "siuq")
			.put("taskID", id);
		String uri = "iutools/srv2/morpheme_dictionary";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, MorphemeExamplesResult.class)
			.reportsNoException()
			.taskIDequals(id)
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
		String uri = "iutools/srv2/morpheme_dictionary";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, MorphemeExamplesResult.class)
			.reportsException(
				"JSON inputs did not have the structure of class org.iutools.webservice.morphexamples.MorphemeExamplesInputs")
			;
		return;
	}

	@Test
	public void test__doPost__OutputContainsSomeSyllabicChars() throws Exception {
		JSONObject json = new JSONObject()
			.put("origQuery", "inuksuk");
		String uri = "iutools/srv2/search/expandquery";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, ExpandQueryResult.class)
			.jsonContains("(ᐃᓄᒃᓱᒃ OR ᐃᓄᔅᓱᒻᒥᒃ OR ᐃᓄᒃᓱᙳᐊᑦ OR ᐃᓄᒃᓱᐃ OR ᐃᓄᒃᓲᑉ OR ᐃᓄᒃᓱᒻᒥ)")
		;
		return;
	}

	@Test
	public void test__doPost__expandquery__HappyPath() throws Exception {
		JSONObject json = new JSONObject()
			.put("origQuery", "inuksuk");
		String uri = "iutools/srv2/search/expandquery";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, ExpandQueryResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__log_action__SEARCH_WEB() throws Exception {

		JSONObject json = new JSONObject()
			.put("action", LogActionInputs.Action.SEARCH_WEB)
			.put("taskID", JSONObject.NULL)
			.put("taskData", new JSONObject()
				.put("origQuery", "inuksuk")
			);
		String uri = "iutools/srv2/log_action";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, ExpandQueryResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__log_action__MORPHEME_EXAMPLES() throws Exception {

		JSONObject json = new JSONObject()
			.put("action", LogActionInputs.Action.MORPHEME_EXAMPLES)
			.put("taskID", JSONObject.NULL)
			.put("taskData", new JSONObject()
				.put("wordPattern", "gaq")
			);
		String uri = "iutools/srv2/log_action";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, ExpandQueryResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__morpheme_dictionary__HappyPath() throws Exception {
		JSONObject json = new JSONObject()
			.put("wordPattern", "siuq");
		String uri = "iutools/srv2/morpheme_dictionary";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, MorphemeExamplesResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__preparecontent__HappyPath() throws Exception {
		JSONObject json = new JSONObject()
			.put("textOrUrl", "inuksuk");
		String uri = "iutools/srv2/gist/preparecontent";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, GistPrepareContentResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__gistword__HappyPath() throws Exception {
		JSONObject json = new JSONObject()
			.put("word", "inuksuk");
		String uri = "iutools/srv2/gist/gistword";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, GistWordResult.class)
			.reportsNoException()
			;
		return;
	}

	@Test
	public void test__doPost__spell__HappyPath() throws Exception {
		JSONObject json = new JSONObject()
			.put("text", "inukkksuk");
		String uri = "iutools/srv2/spell";
		MockHttpServletResponse response  = doPost(uri, json);

		new AssertServletResponse(response, SpellResult.class)
			.reportsNoException()
			;
		return;
	}


	@Test
	public void test__endpointName__HappyPaht() throws Exception {
		for (String endpoint: new String[] {
			"gist/gistword",
			"gist/preparecontent",
			"log_action",
			"morpheme_dictionary",
			"search/expandquery",
			"spell",
			"tokenize"
			}) {
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