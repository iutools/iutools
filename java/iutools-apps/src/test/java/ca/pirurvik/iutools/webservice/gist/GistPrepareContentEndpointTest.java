package ca.pirurvik.iutools.webservice.gist;

import org.junit.Test;

import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.concordancer.Alignment;
import ca.pirurvik.iutools.webservice.IUTServiceTestHelpers;
import ca.pirurvik.iutools.webservice.tokenize.GistPrepareContentInputs;

public class GistPrepareContentEndpointTest {

	/***********************
	 * VERIFICATION TESTS
	 ***********************/
	
	@Test
	public void test__GistPrepareContentEndpoint__InputIsContent() throws Exception {
		
		String text = "ᒪᓕᒐᓕᐅᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᒃᑲᓐᓂᓚᐅᖅᐳᖅ";
		GistPrepareContentInputs prepareInputs = 
				new GistPrepareContentInputs(text);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST_PREPARE_CONTENT,
					prepareInputs
				);
		
		String[][] expIUSentences = new String[][] {
			new String[] {
				"maligaliuqti", " ",  "juu",  " ",  "savikataaq",  " ",
				"niruaqtaukkannilauqpuq"}		
		};
		
		GistPrepareContentAsserter.assertThat(response, 
			"Content not prepared as expecte")
			.inputWasActualContent(true)
			.iuSentencesEquals(expIUSentences)
			.enSentencesEquals(null)
		;
	}

	@Test
	public void test__GistPrepareContentEndpoint__InputIsEnURL() throws Exception {
		
		String url = "https://www.gov.nu.ca/";
		GistPrepareContentInputs prepareInputs = 
				new GistPrepareContentInputs(url);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST_PREPARE_CONTENT,
					prepareInputs
				);
		
		GistPrepareContentAsserter.assertThat(response, 
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.containsAlignment(
				new Alignment("iu", "nunavut gavamanga |", "en", "Government of Nunavut |"))
		;
	}
	@Test
	public void test__GistPrepareContentEndpoint__InputIsIuURL() throws Exception {
		
		String url = "https://www.gov.nu.ca/iu/";
		GistPrepareContentInputs prepareInputs = 
				new GistPrepareContentInputs(url);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.GIST_PREPARE_CONTENT,
					prepareInputs
				);
		
		GistPrepareContentAsserter.assertThat(response, 
			"Content not prepared as expected")
			.inputWasActualContent(false)
			.containsAlignment(
				new Alignment("iu", "nunavut gavamanga |", "en", "Government of Nunavut |"))
		;
	}

}
