package org.iutools.webservice;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import ca.nrc.ui.web.testing.MockHttpServletRequest;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.json.Mapper;
import org.iutools.webservice.gist.GistPrepareContentResult;
import org.iutools.webservice.logaction.LogActionResult;
import org.iutools.webservice.morphexamples.MorphemeExamplesResult;
import org.iutools.webservice.search.ExpandQueryResult;
import org.iutools.webservice.spell.CheckWordResult;
import org.iutools.webservice.tokenize.TokenizeResult;
import org.iutools.webservice.worddict.WordDictResult;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;


public class EndpointDispatcherTest {

	protected static Case[] nonLogEndpointCases = new Case[] {
		new Case("expandquery",
			new JSONObject().put("origQuery", "inuksuk"),
			"iutools/srv2/search/expandquery",
			ExpandQueryResult.class),
		new Case("morpheme_dictionary",
			new JSONObject()
				.put("wordPattern", "siuq"),
			"iutools/srv2/morpheme_dictionary",
			MorphemeExamplesResult.class),
		new Case("preparecontent",
			new JSONObject()
				.put("textOrUrl", "inuksuk"),
			"iutools/srv2/gist/preparecontent",
			GistPrepareContentResult.class),
		new Case("spell - misspelled word",
			new JSONObject().put("text", "inukkksuk"),
			"iutools/srv2/spell",
			CheckWordResult.class),
		new Case("spell - correctly spelled word",
			new JSONObject().put("text", "inukkksuk"),
			"iutools/srv2/spell",
			CheckWordResult.class),
		new Case("tokenize",
			new JSONObject().put("text", "hello wor"),
			"iutools/srv2/tokenize",
			TokenizeResult.class),
		new Case("worddict",
			new JSONObject().put("word", "inuksuk"),
			"iutools/srv2/worddict",
			WordDictResult.class),
//		new Case("config",
//			new JSONObject().put("propNames", new String[] {"org.iutools.apps.feedkback_emails"}),
//			"iutools/srv2/config",
//			ConfigResult.class),
	};

	protected static Case[] logEndpointCases = new Case[] {
		new Case("DICTIONARY_SEARCH",
			"DICTIONARY_SEARCH",
			new JSONObject()
				.put("taskData", new JSONObject()
					.put("word", "inuk"))
		),
		new Case("MORPHEME_SEARCH",
			"MORPHEME_SEARCH",
			new JSONObject()
				.put("taskData", new JSONObject()
					.put("wordPattern", "gaq"))
		),
		new Case("SEARCH_WEB",
			"SEARCH_WEB",
			new JSONObject()
				.put("taskData", new JSONObject()
					.put("origQuery", "inuksuk"))
		),
	};


	///////////////////////////////////////////////
	// VERIFICATION TEST
	///////////////////////////////////////////////

	@Test
	public void test__doPost__nonLogEndpoints() throws Exception {
		Consumer<Case> runner = (aCase) -> {
			try {
				JSONObject json = (JSONObject)aCase.data[0];
				String uri = (String)aCase.data[1];
				Class<? extends EndpointResult> resultsClass =
					(Class<? extends EndpointResult>)aCase.data[2];
				MockHttpServletResponse response  =
					doPost(uri, json);

				new AssertServletResponse(response, resultsClass)
					.reportsNoException()
					;

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(nonLogEndpointCases, runner)
//			.onlyCaseNums(4)
			.run();
	}

	@Test
	public void test__doPost__LogEndpoint() throws Exception {
		Consumer<Case> runner = (aCase) -> {
			try {
				String action = (String)aCase.data[0];
				JSONObject json = (JSONObject)aCase.data[1];
				json.put("_action", action);
				String uri = "iutools/srv2/log_action";

				// Log the action START
				json.put("phase", "START");
				MockHttpServletResponse response  = doPost(uri, json);
				new AssertServletResponse(response, LogActionResult.class)
					.reportsNoException()
					;

				// Get the TaskID that was generated by th
				LogActionResult result =
					new Mapper().readValue(response.getOutput(), LogActionResult.class);
				String taskID = result.taskID;

				// Log the action END
				json.put("phase", "END");
				json.put("_taskID", taskID);
				response  = doPost(uri, json);
					new AssertServletResponse(response, LogActionResult.class)
					.reportsNoException()
					// Check that the task ID for the end is the same as for the start
					.taskIDequals(taskID, "Task ID of END was not the same as START");
				;

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(logEndpointCases, runner)
//			.onlyCaseNums(7)
			.run();
	}

	@Test
	public void test__doPost__InputAlreadyHasTaskID__ResultUsesSameID() throws Exception {
		String id = "someid";
		JSONObject json = new JSONObject()
			.put("wordPattern", "siuq")
			.put("_taskID", id);
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
//				"Service raised exception\n\n" +
//				"JSON inputs did not have the structure of class 'org.iutools.webservice.morphexamples.MorphemeExamplesInputs'.\n" +
//				"JSON was: '{\"wordPattern\":\"siuq\",\"unknownField\":\"blah\"}'")
				"Service raised exception\n\n"
				+ "org.iutools.webservice.ServiceException: JSON inputs did not have the structure of class 'org.iutools.webservice.morphexamples.MorphemeExamplesInputs'.\n"
				+ "JSON was: '{\"wordPattern\":\"siuq\",\"unknownField\":\"blah\"}'"
			);
		return;
	}

	@Test
	public void test__doPost__OutputContainsSomeSyllabicChars() throws Exception {
		JSONObject json = new JSONObject()
			.put("origQuery", "inuksuk");
		String uri = "iutools/srv2/search/expandquery";
		MockHttpServletResponse response  = doPost(uri, json);

		String expRegex = "\\((inuk?s[^\\s]*( OR )?)+\\)";

		new AssertServletResponse(response, ExpandQueryResult.class)
			.jsonContains(expRegex, true)
		;
		return;
	}

	@Test
	public void test__endpointName__HappyPaht() throws Exception {
		for (String endpoint: new String[] {
			"gist/preparecontent",
			"log_action",
			"morpheme_dictionary",
			"search/expandquery",
			"spell",
			"tokenize",
			"worddict"}) {
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

	@Test
	public void test__doPost__TaskIDOfResponseIsSameAsThatOfInputs() throws Exception {
		Consumer<Case> runner = (aCase) -> {
			try {
				JSONObject jsonInputs = (JSONObject) aCase.data[0];
				String uri = (String)aCase.data[1];
				jsonInputs.put("_taskID", "someid");
				MockHttpServletResponse gotResponse =  doPost(uri, jsonInputs);
				new AssertHttpServletResponse(gotResponse)
					.assertFieldEquals("taskID", "someid");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(nonLogEndpointCases, runner)
			.run();
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