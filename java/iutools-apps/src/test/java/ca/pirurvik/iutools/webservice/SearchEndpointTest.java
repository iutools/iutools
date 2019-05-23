package ca.pirurvik.iutools.webservice;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;
import ca.pirurvik.iutools.search.SearchHit;
import ca.pirurvik.iutools.testing.IUTTestHelpers;
import ca.pirurvik.iutools.webservice.SearchEndpoint;
import ca.pirurvik.iutools.webservice.SearchInputs;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.ui.web.testing.MockHttpServletResponse;


public class SearchEndpointTest {

	SearchEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new SearchEndpoint();
	}

	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__SearchEndpoint__HappyPath() throws Exception {
		
		SearchInputs searchInputs = new SearchInputs("nunavut").setHitsPerPage(20);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SEARCH,
					searchInputs
				);
		
		IUTServiceTestHelpers.assertExpandedQueryEquals(
				"(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ)", 
				response);
		
		String[] queryWords = new String[] {"ᓄᓇᕗ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕘᑉ", "ᓄᓇᕗᒻᒥᐅᑦ"};
		double tolerance = 0.75;
		SearchResponse srchResponse = IUTServiceTestHelpers.toSearchResponse(response);
		IUTServiceTestHelpers.assertMostHitsMatchWords(queryWords, response, tolerance);
		Assert.assertTrue(srchResponse.totalHits > 10);
	}
	
	@Test
	public void test__SearchEndpoint__FirstAndSecondPagesOfHitsDiffer() throws Exception {
		
		SearchInputs searchInputs = new SearchInputs("nunavut").setHitsPerPage(10);

		// Get the first page of hits
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SEARCH,
					searchInputs
				);
		List<SearchHit> hits1 = IUTServiceTestHelpers.toSearchResponse(response).hits;
		
		// Get the second page of hits
		searchInputs.setPageNum(1);
		response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SEARCH,
					searchInputs
				);
		List<SearchHit> hits2 = IUTServiceTestHelpers.toSearchResponse(response).hits;
		
		double diffRatio = 0.80;
		IUTTestHelpers.assertHitsPagesDifferByAtLeast("Hits from pages 1 and 2 should have been different", 
				hits1, hits2, diffRatio);
	}	
	
	@Test
	public void test__SearchEndpoint__QueryIsAlreadyExpanded__DoesNotTryToExpandAgain() throws Exception {
		
		SearchInputs searchInputs = new SearchInputs("(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ)").setHitsPerPage(20);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SEARCH,
					searchInputs
				);
		
		IUTServiceTestHelpers.assertExpandedQueryEquals(
				"(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ)", 
				response);
		
		String[] queryWords = new String[] {"ᓄᓇᕗ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕘᑉ", "ᓄᓇᕗᒻᒥᐅᑦ"};
		double tolerance = 0.7;
		SearchResponse srchResponse = IUTServiceTestHelpers.toSearchResponse(response);
		IUTServiceTestHelpers.assertMostHitsMatchWords(queryWords, response, tolerance);
		Assert.assertTrue(srchResponse.totalHits > 10);
	}	
	

	private void assertExpansionWordsAre(String[] expExpansionWords, QueryExpansion[] gotExpansions) throws IOException {
		List<String> gotExpansionWords = new ArrayList<String>();
		if (gotExpansions == null) {
			gotExpansionWords = null;
		} else {
			for (QueryExpansion exp: gotExpansions) {
				gotExpansionWords.add(exp.word);
			}
		}
		AssertHelpers.assertDeepEquals("", expExpansionWords, gotExpansionWords);
	}
	
}
