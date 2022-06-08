package org.iutools.webservice.gist;

import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.iutools.concordancer.SentencePair;
import org.iutools.script.TransCoder.*;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class GistPrepareContentTest extends EndpointTest {
	@Override
	public Endpoint makeEndpoint() {
		return new GistPrepareContentEndpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__GistPrepareContentEndpoint__InputIsSyllabicContent() throws Exception {
		
		String text = "ᒪᓕᒐᓕᐅᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᒃᑲᓐᓂᓚᐅᖅᐳᖅ";
		GistPrepareContentInputs inputs =
				new GistPrepareContentInputs(text);

		EndpointResult epResult = endPoint.execute(inputs);

		String[][] expIUSentences = new String[][] {
			new String[] {
				"maligaliuqti", " ",  "juu",  " ",  "savikataaq",  " ",
				"niruaqtaukkannilauqpuq"}		
		};

		new AssertGistPrepareContentResult(epResult,
		"Content not prepared as expected")
			.inputWasActualContent(true)
			.iuSentencesEquals(expIUSentences)
			.enSentencesEquals(null)
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsEnURL() throws Exception {

		String url = "https://www.gov.nu.ca/";

		GistPrepareContentInputs inputs =
				new GistPrepareContentInputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.containsENSentenceStartingWith("Premier of Nunavut")
			.containsIUSentenceStartingWith("sivuliqti nunavummi")
			.containsAlignment(
				new SentencePair(
					"iu", "nunavut gavamanga\n",
					"en", "Government of Nunavut\n"))
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsURLOnNonGovIUSite() throws Exception {

		String url = "https://travelnunavut.ca/";

		GistPrepareContentInputs inputs =
			new GistPrepareContentInputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected\n(NOTE: THIS TEST MAY FAIL INTERMITENTLY BECAUSE travelnunavut.ca IS A BIT FLAKY)")
			.inputWasActualContent(false)
			.containsENSentenceStartingWith("WELCOME TO NUNAVUT")
			// Note: Eventhough the travelnunavut.ca web site has an INUKTITUT
			// language link, that link returns the same content as the English version.
			.hasNoIUSentences()
		;
	}


	@Test
	public void test__GistPrepareContentEndpoint__InputIsIuURL() throws Exception {

		String url = "https://www.gov.nu.ca/iu/";

		GistPrepareContentInputs inputs =
			new GistPrepareContentInputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.containsAlignment(
				new SentencePair(
					"iu", "nunavut gavamanga\n",
				"en", "Government of Nunavut\n"))
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsURL_WhoseTranslationCannotBeDeduced() throws Exception {
		// We use the Google home URL
		//
		//   https://www.google.com/
		//
		// which does not have a version in Inuktitut
		//
		String url = "https://www.google.com/";

		GistPrepareContentInputs inputs =
			new GistPrepareContentInputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected for url="+url)
			.inputWasActualContent(false)
			.hasContentForLang("en")
			.containsENSentenceStartingWith("Search Images Maps Play YouTube News Gmail Drive More »")
			.hasNoContentForLang("iu")
			.hasNoAlignments()
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsURL_NonExistantURL() throws Exception {

		String url = "https://www.gov.nu.ca/doesnotexist/iu";

		GistPrepareContentInputs inputs =
			new GistPrepareContentInputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.raisesError("Unable to download the input page")
			.couldNotFetchIUContent()
			.couldNotFetchEnContent()
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("text-roman2roman",
				"inuksuk nunavut. ammuumajuq.",
				Script.ROMAN,
				new String[] {
					 "inuksuk nunavut.",
					 " ammuumajuq."},
				"text"),

			new Case("text-roman2syll",
				"inuksuk nunavut. ammuumajuq.",
				Script.SYLLABIC,
				new String[]{
					"ᐃᓄᒃᓱᒃ ᓄᓇᕗᑦ.",
					" ᐊᒻᒨᒪᔪᖅ."
				},
				"text"),

			new Case("text-syll2roman",
				"ᐃᓄᒃᓱᒃ ᓄᓇᕗᑦ. ᐊᒻᒨᒪᔪᖅ.",
				Script.ROMAN,
				new String[]{
					"inuksuk nunavut.",
					" ammuumajuq.",
				},
				"text"),

			new Case("text-syll2syll",
				"ᐃᓄᒃᓱᒃ ᓄᓇᕗᑦ. ᐊᒻᒨᒪᔪᖅ.",
				Script.SYLLABIC,
				new String[]{
					"ᐃᓄᒃᓱᒃ ᓄᓇᕗᑦ.",
					" ᐊᒻᒨᒪᔪᖅ."
				},
				"text"),


			new Case("url-syll2roman",
				"https://www.gov.nu.ca/",
				Script.ROMAN,
				new String[] {"nunavut gavamanga"},
				"url"),

			new Case("url-syll2syll",
				"https://www.gov.nu.ca/",
				Script.SYLLABIC,
				new String[] {
				 	"ᓄᓇᕗᑦ ᒐᕙᒪᖓ"
				},
				"url"),
		};


		Consumer<Case> runner = (aCase) -> {
			String textOrUrl = (String)aCase.data[0];

			Script requestedScript = (Script)aCase.data[1];
			String[]expIUSentences = (String[])aCase.data[2];
			String inputType = (String)aCase.data[3];
			boolean isText = (inputType.equals("text"));
			String[] expEnSentences = null;
			if (aCase.data.length > 4) {
				expEnSentences = (String[])aCase.data[4];
			}

			try {
				GistPrepareContentInputs inputs =
					new GistPrepareContentInputs(textOrUrl);
				inputs.iuAlphabet = requestedScript;

				EndpointResult epResult = endPoint.executeThenConvert(inputs);

				AssertGistPrepareContentResult asserter =
					new AssertGistPrepareContentResult(epResult,
					"Content not prepared as expected");

				asserter
					.iuSentencesContains(expIUSentences)
					.enSentencesContain(expEnSentences)
					;

				asserter .inputWasActualContent(isText);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(6)
			.run();
	}
}
