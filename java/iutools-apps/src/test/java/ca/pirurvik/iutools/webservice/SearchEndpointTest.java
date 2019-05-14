package ca.pirurvik.iutools.webservice;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;
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
		
//		Assert.fail("This test currently fails because of the bug in Bing search engine. Reactivate it once we have answer from MS.");
		
		SearchInputs searchInputs = new SearchInputs().setHitsPerPage(20);
		searchInputs.query = "nunavut";

				
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
