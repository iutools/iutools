package org.iutools.webservice;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.iutools.morphrelatives.MorphologicalRelative;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
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
		
		SearchInputs searchInputs = new SearchInputs("nunavut").setHitsPerPage(10);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SEARCH,
					searchInputs
				);
		
		String expExpandedQuery = "(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ OR ᓄᓇᕗᑦ)";		
		String[] queryWords = new String[] {"ᓄᓇᕗ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕘᑉ", "ᓄᓇᕗᒻᒥᐅᑦ", "ᓄᓇᕗᑦ"};
		double badHitsTolerance = 0.55;
		long minTotalHits = 7500;
		long minHitsRetrieved = 100;
		IUTServiceTestHelpers.assertSearchResponseIsOK(response, expExpandedQuery, queryWords, badHitsTolerance, 
				minTotalHits, minHitsRetrieved);
	}
	
	@Test
	public void test__SearchEndpoint__QueryWithLessThanOnePageOfHits() throws Exception {
		
		// This query (= 'religion') returns less than 10 hits (i.e. less than 
		// a full page of hits).
		SearchInputs searchInputs = new SearchInputs("ᐃᓂᓕᐅ").setHitsPerPage(10);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SEARCH,
					searchInputs
				);
		
		String expExpandedQuery = "(ᐃᓂᓕᐅᕐᑐᑦ OR ᐃᓂᓕᐅᕈᕕᒃ OR ᐃᓂᓕᐅᕆᓂᕐᓗ OR ᐃᓂᓕᐅᕆᓂᕐᒥᒃ OR ᐃᓂᓕᐅᕐᑕᐅᓗᓂ OR ᐃᓂᓕᐅ)";
		String[] queryWords = new String[] {"ᐃᓂᓕᐅᕐᑐᑦ", "ᐃᓂᓕᐅᕈᕕᒃ", "ᐃᓂᓕᐅᕆᓂᕐᓗ", "ᐃᓂᓕᐅᕆᓂᕐᒥᒃ", "ᐃᓂᓕᐅᕐᑕᐅᓗᓂ", "ᐃᓂᓕᐅ"};
		double badHitsTolerance = 0.70;
		long minTotalHits = 1;
		long minHitsRetrieved = 1;
		IUTServiceTestHelpers.assertSearchResponseIsOK(response, expExpandedQuery, queryWords, badHitsTolerance, 
				minTotalHits, minHitsRetrieved);
	}

	@Test
	public void test__SearchEndpoint__QueryIsAlreadyExpanded__DoesNotTryToExpandAgain() throws Exception {
		
		SearchInputs searchInputs = new SearchInputs("(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ)").setHitsPerPage(10);
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SEARCH,
					searchInputs
				);
		
		String expExpandedQuery = "(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ)";
		String[] queryWords = new String[] {"ᓄᓇᕗ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕘᑉ", "ᓄᓇᕗᒻᒥᐅᑦ"};
		double badHitsTolerance = 0.55;
		long minTotalHits = 1000;
		long minHitsRetrieved = 100;
		IUTServiceTestHelpers.assertSearchResponseIsOK(response, expExpandedQuery, queryWords, badHitsTolerance, 
				minTotalHits, minHitsRetrieved);
	}	

	@Test
	public void test__expandQuery__QueryIsNotAnalyzable() throws Exception {
		String nonAnalyzableWord = "oewrmweriorfgqer";
		SearchResponse results = new SearchResponse();
		endPoint.expandQuery(nonAnalyzableWord, results);
		
		List<String> gotQueryWords = results.expandedQueryWords;
		String[] expQueryWords = new String[] {nonAnalyzableWord};
		AssertObject.assertDeepEquals("", expQueryWords, gotQueryWords);
		AssertString.assertStringEquals("("+nonAnalyzableWord+")", results.expandedQuery);
	}
	

	private void assertExpansionWordsAre(String[] expExpansionWords, MorphologicalRelative[] gotExpansions) throws IOException {
		List<String> gotExpansionWords = new ArrayList<String>();
		if (gotExpansions == null) {
			gotExpansionWords = null;
		} else {
			for (MorphologicalRelative exp: gotExpansions) {
				gotExpansionWords.add(exp.getWord());
			}
		}
		AssertObject.assertDeepEquals("", expExpansionWords, gotExpansionWords);
	}
	
}
