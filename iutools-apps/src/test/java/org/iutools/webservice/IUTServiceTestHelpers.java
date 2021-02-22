package org.iutools.webservice;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.iutools.morph.Gist;
import org.iutools.utilities.Alignment;
import ca.nrc.web.Http;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertObject;
import ca.nrc.ui.web.testing.MockHttpServletRequest;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.search.SearchHit;
import org.iutools.testing.IUTTestHelpers;
import org.iutools.webservice.gist.GistPrepareContentEndpoint;
import org.iutools.webservice.gist.GistPrepareContentResponse;
import org.iutools.webservice.gist.GistWordEndpoint;
import org.iutools.webservice.gist.GistWordResponse;
import org.iutools.webservice.relatedwords.RelatedWordsEndpoint;
import org.iutools.webservice.relatedwords.RelatedWordsResponse;
import org.iutools.webservice.search.ExpandQueryEndpoint;
import org.iutools.webservice.search.ExpandQueryResponse;
import org.iutools.webservice.tokenize.TokenizeEndpoint;
import org.iutools.webservice.tokenize.TokenizeResponse;

import org.junit.*;

public class IUTServiceTestHelpers {
	public static final long SHORT_WAIT = 2*1000;
	public static final long MEDIUM_WAIT = 2*SHORT_WAIT;
	public static final long LONG_WAIT = 2*MEDIUM_WAIT;

	public enum EndpointNames {
		GIST, GIST_PREPARE_CONTENT, EXPAND_QUERY, GIST_WORD, MORPHEME, MORPHEMEEXAMPLE,
		RELATED_WORDS, SEARCH, TOKENIZE, SPELL};

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
		
		if (eptName == EndpointNames.GIST_WORD) {
			new GistWordEndpoint().doPost(request, response);
		} else if (eptName == EndpointNames.EXPAND_QUERY) {
			new ExpandQueryEndpoint().doPost(request, response);
		} else if (eptName == EndpointNames.GIST_PREPARE_CONTENT) {
			new GistPrepareContentEndpoint().doPost(request, response);
		} else if (eptName == EndpointNames.MORPHEME) {
			new OccurenceSearchEndpoint().doPost(request, response);
		} else if (eptName == EndpointNames.RELATED_WORDS) {
			new RelatedWordsEndpoint().doPost(request, response);
		} else if (eptName == EndpointNames.SEARCH) {
			new SearchEndpoint().doPost(request, response);
		} else if (eptName == EndpointNames.SPELL) {
			new SpellEndpoint().doPost(request, response);	
		} else if (eptName == EndpointNames.TOKENIZE) {
			new TokenizeEndpoint().doPost(request, response);	
		}
		
		String srvErr = ServiceResponse.jsonErrorMessage(response.getOutput());
		if (srvErr != null && ! expectServiceError) {
			throw new Exception("Did not expect the service to return an error message but it did.\nerrorMessage: "+srvErr);
		} else if (srvErr == null && expectServiceError) {
			throw new Exception("Expected the service to return an error message but it did not.");
		}
		
		return response;
	}
	
	public static GistPrepareContentResponse toGistPrepareContentResponse(
			MockHttpServletResponse gotResponse) throws IOException {
		String responseStr = gotResponse.getOutputStream().toString();
		GistPrepareContentResponse response = 
				new ObjectMapper().readValue(responseStr, 
						GistPrepareContentResponse.class);
		return response;
	}
	
	public static GistWordResponse toGistWordResponse(
			MockHttpServletResponse gotResponse) throws IOException {
		String responseStr = gotResponse.getOutputStream().toString();
		GistWordResponse response = 
				new ObjectMapper().readValue(responseStr, GistWordResponse.class);
		return response;
	}

	public static SpellResponse toSpellResponse(
			HttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		SpellResponse response = 
				new ObjectMapper().readValue(responseStr, SpellResponse.class);
		return response;
	}

	
	public static SearchResponse toSearchResponse(
			HttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		SearchResponse response = new ObjectMapper().readValue(responseStr, SearchResponse.class);
		return response;
	}

	public static RelatedWordsResponse toRelatedWordsResponse(
		MockHttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		RelatedWordsResponse response =
			new ObjectMapper().readValue(responseStr, RelatedWordsResponse.class);
		return response;
	}

	public static ExpandQueryResponse toExpandQueryResponse(
		MockHttpServletResponse servletResp) throws Exception {
		String responseStr = servletResp.getOutputStream().toString();
		ExpandQueryResponse response =
			new ObjectMapper().readValue(responseStr, ExpandQueryResponse.class);
		return response;
	}

	public static TokenizeResponse toTokenizeResponse(
			MockHttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		TokenizeResponse response = 
				new ObjectMapper().readValue(responseStr, TokenizeResponse.class);
		return response;
	}
	
	private static OccurenceSearchResponse toOccurenceSearchResponse(
			MockHttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		OccurenceSearchResponse response = 
				new ObjectMapper().readValue(responseStr, OccurenceSearchResponse.class);
		return response;
	}
	private static OccurenceExampleResponse toOccurenceExampleResponse(
			MockHttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		OccurenceExampleResponse response = 
				new ObjectMapper().readValue(responseStr, OccurenceExampleResponse.class);
		return response;
	}
	


	public static void assertExpandedQueryEquals(String expQuery, MockHttpServletResponse gotResponse) throws JsonParseException, JsonMappingException, IOException {
		SearchResponse gotResult = new ObjectMapper().readValue(gotResponse.getOutput(), SearchResponse.class);
		AssertHelpers.assertStringEquals("Expanded query was not as expected.", expQuery.trim(), gotResult.expandedQuery.trim());
	}

	public static void assertTotalHitsLessThan(String mess,
		MockHttpServletResponse gotResponse, int expMaxHits) throws Exception {
		SearchResponse gotResult =
			new ObjectMapper()
				.readValue(gotResponse.getOutput(), SearchResponse.class);
		AssertNumber.isLessOrEqualTo(
			mess, gotResult.totalHits, expMaxHits);
	}

	public static void assertMostHitsMatchWords(String[] queryWords, MockHttpServletResponse gotResponse,
								double tolerance) throws JsonParseException, JsonMappingException, IOException {
		SearchResponse gotResult = new ObjectMapper().readValue(gotResponse.getOutput(), SearchResponse.class);
		List<SearchHit> gotHits = gotResult.hits;
		
		IUTTestHelpers.assertMostHitsMatchWords(queryWords, gotHits, tolerance);
		
		
		String regex = "(We would like to show you a description here but the site won’t allow us";
		for (String aWord: queryWords) {
			if (regex == null) {
				regex = "(";
			} else {
				regex += "|";
			}
			regex += aWord.toLowerCase();
		}
		regex += ")";
		
		Pattern patt = Pattern.compile(regex);
		int  hitNum = 1;
		Set<String> unmatchedURLs = new HashSet<String>();
		for (SearchHit aHit: gotHits) {
			Matcher matcher = patt.matcher(aHit.snippet);
			if (!matcher.find()) {
				unmatchedURLs.add(aHit.url);
			}
			hitNum++;
		}
		
		double unmatchedRatio = 1.0 * unmatchedURLs.size() / hitNum;
		Assert.assertTrue(
				"There were too many urls that did not match the  query words '"+regex+".\n"
			  + "Unmatched URLs were:\n  "
			  + String.join("\n  ", unmatchedURLs),
			  unmatchedRatio <= tolerance
			);
	}

	public static void assertSearchResponseIsOK(MockHttpServletResponse response, String expExpandedQuery,
			String[] queryWords, double badHitsTolerance, long minTotalHits, long minHitsRetrieved) 
			throws Exception {
		IUTServiceTestHelpers.assertExpandedQueryEquals(expExpandedQuery, response);
		
		SearchResponse srchResponse = IUTServiceTestHelpers.toSearchResponse(response);
		IUTServiceTestHelpers.assertMostHitsMatchWords(queryWords, response, badHitsTolerance);
		AssertNumber.isGreaterOrEqualTo("The total number of potential hits was too low", srchResponse.totalHits, minTotalHits);
		AssertNumber.isGreaterOrEqualTo("The number of hits actually retrieved was too low", new Long(srchResponse.hits.size()), minHitsRetrieved);
	}

	public static void assertOccurenceSearchResponseIsOK(
			MockHttpServletResponse response, Map<String,MorphemeSearchResult> expected) throws Exception {
		
		OccurenceSearchResponse occurenceSearchResponse = 
				IUTServiceTestHelpers.toOccurenceSearchResponse(response);
		
		Map<String,MorphemeSearchResult> got = occurenceSearchResponse.matchingWords;
		AssertObject.assertDeepEquals(
	"The list of morphemes was not as expected",
			expected, got, new Integer(1));
	}

	public static void assertOccurenceExampleResponseIsOK(
			MockHttpServletResponse response, ExampleWordWithMorpheme expected) throws Exception {
		
		OccurenceExampleResponse occurenceExampleResponse = 
				IUTServiceTestHelpers.toOccurenceExampleResponse(response);
		
		ExampleWordWithMorpheme gotExampleWord = occurenceExampleResponse.exampleWord;
		Gist gotGist = gotExampleWord.gist;
		Gist expectedGist = expected.gist;
		AssertObject.assertDeepEquals("The gists are not equal.",expectedGist, gotGist);
		Assert.assertEquals("The word of the gist is not as expected.",  expectedGist.word, gotGist.word);
		Alignment[] gotAlignments = gotExampleWord.alignments;
		Alignment[] expectedAlignments = expected.alignments;
		Assert.assertEquals("The alignments are not equal.",
				expectedAlignments[0].get("iu").substring(0,100), 
				gotAlignments[0].get("iu").substring(0,100));
		Assert.assertEquals("The alignments are not equal.",
				expectedAlignments[0].get("en").substring(0,100), 
				gotAlignments[0].get("en").substring(0,100));
	}
}
