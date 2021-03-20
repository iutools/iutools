package org.iutools.webservice.gist;

import org.iutools.concordancer.Alignment;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class GistPrepareContent2Test extends EndpointTest {
	@Override
	public Endpoint makeEndpoint() {
		return new GistPrepareContent2Endpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__GistPrepareContentEndpoint__InputIsContent() throws Exception {
		
		String text = "ᒪᓕᒐᓕᐅᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᒃᑲᓐᓂᓚᐅᖅᐳᖅ";
		GistPrepareContent2Inputs inputs =
				new GistPrepareContent2Inputs(text);

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

		GistPrepareContent2Inputs inputs =
				new GistPrepareContent2Inputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.containsAlignment(
				new Alignment(
					"iu", "nunavut gavamanga",
				"en", "The Government of Nunavut"))
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsIuURL() throws Exception {

		String url = "https://www.gov.nu.ca/iu/";

		GistPrepareContent2Inputs inputs =
			new GistPrepareContent2Inputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.containsAlignment(
				new Alignment(
					"iu", "nunavut gavamanga",
				"en", "The Government of Nunavut"))
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsURL_WhoseTranslationCannotBeDeduced() throws Exception {
		// The English URL for this IU url is:
		//
		//   https://www.gov.nu.ca/community-and-government-services
		//
		// which currently cannot be deduced by the concordancer.
		//
		String url = "https://www.gov.nu.ca/iu/cgs-iu";

		GistPrepareContent2Inputs inputs =
			new GistPrepareContent2Inputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.hasContentForLang("en")
			.hasContentForLang("iu")
			.hasSomeAlignments()
			.containsIUSentenceStartingWith("nunalingni gavamakkunnillu pijittiraqtikkut")
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsURL_NonExistantURL() throws Exception {

		String url = "https://www.gov.nu.ca/doesnotexist/iu";

		GistPrepareContent2Inputs inputs =
			new GistPrepareContent2Inputs(url);

		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistPrepareContentResult(epResult,
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.raisesError("Unable to download the input page")
			.couldNotFetchIUContent()
			.couldNotFetchEnContent()
		;
	}
}
