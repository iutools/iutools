package org.iutools.webservice.gist;

import org.iutools.concordancer.Alignment;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

public class GistPrepareContentTest extends EndpointTest {
	@Override
	public Endpoint makeEndpoint() {
		return new GistPrepareContentEndpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__GistPrepareContentEndpoint__InputIsContent() throws Exception {
		
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
			.containsAlignment(
				new Alignment(
					"iu", "nunavut gavamanga",
				"en", "Government of Nunavut"))
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
				new Alignment(
					"iu", "nunavut gavamanga",
				"en", "Government of Nunavut"))
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

		GistPrepareContentInputs inputs =
			new GistPrepareContentInputs(url);

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
}
