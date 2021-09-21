package org.iutools.webservice.logaction;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class LogActionInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new LogActionInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new LogActionInputs(
				LogActionInputs.Action.GIST_TEXT,
				new JSONObject()
					.put("textOrUrl", "inuksuk")
			);
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":\"GIST_TEXT\",\"taskID\":null,\"taskStartTime\":null,\"totalWords\":1,\"type\":\"text\"}");
			;

		inputs =
			new LogActionInputs(
				LogActionInputs.Action.GIST_TEXT,
				new JSONObject()
					.put("textOrUrl", "http://www.somewhere.com/hello.html")
			);
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":\"GIST_TEXT\",\"address\":\"http://www.somewhere.com/hello.html\",\"host\":\"www.somewhere.com\",\"taskID\":null,\"taskStartTime\":null,\"type\":\"url\"}");
			;

		inputs =
			new LogActionInputs(
				LogActionInputs.Action.SEARCH_WEB,
				new JSONObject()
					.put("origQuery", "inuksuk")
			);
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":\"SEARCH_WEB\",\"origQuery\":\"inuksuk\"}");
			;

		inputs =
			new LogActionInputs(
				LogActionInputs.Action.MORPHEME_SEARCH,
				new JSONObject()
					.put("wordPattern", "siuq")
					.put("corpusName", JSONObject.NULL)
					.put("nbExamples", "50")
			);
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"_action\":\"MORPHEME_SEARCH\",\"corpusName\":null,\"nbExamples\":\"50\",\"wordPattern\":\"siuq\"}");
			;
	}
}
