package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.testing.AssertHelpers;
import ca.nrc.ui.web.testing.MockHttpServletRequest;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.search.SearchHit;
import ca.pirurvik.iutools.testing.IUTTestHelpers;
import ca.pirurvik.iutools.webservice.SearchEndpoint;
import ca.pirurvik.iutools.webservice.SearchResponse;

import org.junit.*;

public class IUTServiceTestHelpers {
	public static final long SHORT_WAIT = 2*1000;
	public static final long MEDIUM_WAIT = 2*SHORT_WAIT;
	public static final long LONG_WAIT = 2*MEDIUM_WAIT;
	
	enum EndpointNames {SEARCH};
	

	public static MockHttpServletResponse postEndpointDirectly(EndpointNames eptName, Object inputs) throws Exception {
		return postEndpointDirectly(eptName, inputs, false);
	}

	public static MockHttpServletResponse postEndpointDirectly(EndpointNames eptName, Object inputs, boolean expectServiceError) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		String jsonBody = new ObjectMapper().writeValueAsString(inputs);
		request.setReaderContent(jsonBody);
		
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		if (eptName == EndpointNames.SEARCH) {
			new SearchEndpoint().doPost(request, response);
		}
		
		String srvErr = ServiceResponse.jsonErrorMessage(response.getOutput());
		if (srvErr != null && ! expectServiceError) {
			throw new Exception("Did not expect the service to return an error message but it did.\nerrorMessage: "+srvErr);
		} else if (srvErr == null && expectServiceError) {
			throw new Exception("Expected the service to return an error message but it did not.");
		}
		
		return response;
	}
	
	public static SearchResponse toSearchResponse(HttpServletResponse servletResp) throws IOException {
		String responseStr = servletResp.getOutputStream().toString();
		SearchResponse response = new ObjectMapper().readValue(responseStr, SearchResponse.class);
		return response;
	}


	public static void assertExpandedQueryEquals(String expQuery, MockHttpServletResponse gotResponse) throws JsonParseException, JsonMappingException, IOException {
		SearchResponse gotResult = new ObjectMapper().readValue(gotResponse.getOutput(), SearchResponse.class);
		AssertHelpers.assertStringEquals("Expanded query was not as expected.", expQuery, gotResult.expandedQuery);
	}


	public static void assertMostHitsMatchWords(String[] queryWords, MockHttpServletResponse gotResponse,
								double tolerance) throws JsonParseException, JsonMappingException, IOException {
		SearchResponse gotResult = new ObjectMapper().readValue(gotResponse.getOutput(), SearchResponse.class);
		List<SearchHit> gotHits = gotResult.hits;
		
		IUTTestHelpers.assertMostHitsMatchWords(queryWords, gotHits, tolerance);
		
		
//		String regex = "(We would like to show you a description here but the site won’t allow us";
//		for (String aWord: queryWords) {
//			if (regex == null) {
//				regex = "(";
//			} else {
//				regex += "|";
//			}
//			regex += aWord.toLowerCase();
//		}
//		regex += ")";
//		
//		Pattern patt = Pattern.compile(regex);
//		int  hitNum = 1;
//		Set<String> unmatchedURLs = new HashSet<String>();
//		for (SearchHit aHit: gotHits) {
//			Matcher matcher = patt.matcher(aHit.snippet);
//			if (!matcher.find()) {
//				unmatchedURLs.add(aHit.url);
//			}
//			hitNum++;
//		}
//		
//		double unmatchedRatio = 1.0 * unmatchedURLs.size() / hitNum;
//		Assert.assertTrue(
//				"There were too many urls that did not match the  query words '"+regex+".\n"
//			  + "Unmatched URLs were:\n  "
//			  + String.join("\n  ", unmatchedURLs),
//			  unmatchedRatio <= tolerance
//			);
	}
	
	
}
