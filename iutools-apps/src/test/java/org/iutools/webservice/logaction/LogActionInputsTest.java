package org.iutools.webservice.logaction;

import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class LogActionInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new LogActionInputs();
	}

	@Test
	public void test__summarizeForLogging__SeveralCases() throws Exception {
		Case[] cases = new Case[] {
			new Case(
				"DICTIONARY_SEARCH",
				new LogActionInputs(
					"DICTIONARY_SEARCH",
					new JSONObject()
						.put("lang", "iu")
						.put("word", "inu")
				),
				"{\"_action\":\"DICTIONARY_SEARCH\",\"_phase\":\"START\",\"_taskID\":null,\"_taskStartTime\":null,\"lang\":\"iu\",\"word\":\"inu\"}"
			),
			new Case(
				"GIST_TEXT_texttype",
				new LogActionInputs(
					"GIST_TEXT",
					new JSONObject()
						.put("textOrUrl", "inuksuk")
				),
				"{\"_action\":\"GIST_TEXT\",\"_phase\":\"START\",\"_taskID\":null,\"_taskStartTime\":null,\"taskElapsedMsecs\":null,\"totalWords\":1,\"type\":\"text\"}"
			),
			new Case(
				"GIST_TEXT_urltype",
				new LogActionInputs(
					"GIST_TEXT",
					new JSONObject()
						.put("textOrUrl", "http://www.somewhere.com/hello.html")
				),
				"{\"_action\":\"GIST_TEXT\",\"_phase\":\"START\",\"_taskID\":null,\"_taskStartTime\":null,\"address\":\"http://www.somewhere.com/hello.html\",\"host\":\"www.somewhere.com\",\"taskElapsedMsecs\":null,\"type\":\"url\"}"
			),
			new Case(
				"MORPHEME_SEARCH",
				new LogActionInputs(
					"MORPHEME_SEARCH",
					new JSONObject()
						.put("wordPattern", "siuq")
						.put("corpusName", JSONObject.NULL)
						.put("nbExamples", "50")
				),
				"{\"_action\":\"MORPHEME_SEARCH\",\"_phase\":\"START\",\"_taskID\":null,\"_taskStartTime\":null,\"corpusName\":null,\"nbExamples\":\"50\",\"wordPattern\":\"siuq\"}"
			),
			new Case(
				"SEARCH_WEB",
				new LogActionInputs(
					"SEARCH_WEB",
					new JSONObject()
						.put("origQuery", "inuksuk")
				),
				"{\"_action\":\"SEARCH_WEB\",\"_phase\":\"START\",\"_taskID\":null,\"_taskStartTime\":null,\"origQuery\":\"inuksuk\"}"
			),
			new Case(
				"SPELL",
				new LogActionInputs(
					"SPELL",
					new JSONObject()
						.put("text", "inuksuk iglu nunavut")
				),
				"{\"_action\":\"SPELL\",\"_phase\":\"START\",\"_taskID\":null,\"_taskStartTime\":null,\"taskElapsedMsecs\":null,\"totalWords\":3}"
			),
			new Case(
				"WORD_LOOKUP",
				new LogActionInputs(
					"DICTIONARY_SEARCH",
					new JSONObject()
						.put("lang", "iu")
						.put("word", "inuksanganut")
				),
				"{\"_action\":\"DICTIONARY_SEARCH\",\"_phase\":\"START\",\"_taskID\":null,\"_taskStartTime\":null,\"lang\":\"iu\",\"word\":\"inuksanganut\"}"
			),

		};

		Consumer<Case> runner = (aCase) -> {
			LogActionInputs inputs = (LogActionInputs)aCase.data[0];
			String expJson = (String)aCase.data[1];
			try {
				new AssertServiceInputs(inputs, aCase.descr)
				.logSummaryIs(expJson);
				;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
			.run();
	}
}
