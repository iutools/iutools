package ca.pirurvik.iutools.webservice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.testing.IUTTestHelpers;

public class SpellEndpointTest {

	SpellEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new SpellEndpoint();
	}

	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__SpellEndpoint__Syllabic__HappyPath() throws Exception {
		
		SpellInputs spellInputs = new SpellInputs("ᐃᓄᑦᒧᑦ ᑕᑯᔪᖅ");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SPELL,
					spellInputs
				);
		
		SpellCheckerAssertion.assertThat(response, "")
			.raisedNoError()
			.nthCorrectionIs(0, true,
				new String[] {
					"ᐃᓄᒻᒧᑦ",
					"ᐃᓄᑐᐊᒧᑦ",
					"ᐃᓄᑐᐊᑦ",
					"ᐃᓄᒃᑐᑦ",
					"ᐃᓄᕗᑦ",
					"ᐃᓄᒻᒧᑦ",
					"ᐃᓄᑐᐊᒧᑦ",
					"ᐃᓄᑐᐊᑦ",
					"ᐃᓄᒃᑐᑦ",
					"ᐃᓄᕗᑦ"
				}
			)
			.nthCorrectionIs(1, false)
			;
		
		return;	
	}

	@Test
	public void test__SpellEndpoint__WordWithSyllCharsThatAreExpressedAsTwoRomanChars() 
			throws Exception {
		
		SpellInputs spellInputs = new SpellInputs("ᒐᕙᒪᒃᑯᑎᒍᑦ");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SPELL,
					spellInputs
				);
		
//		SpellCheckerAssertion.assertThat(response, "")
//			.raisedNoError()
//			.nthCorrectionIs(0, true, 
//					new String[] {
//					  "ᐃᓄᒻᒧᑦ",
//					  "ᐃᓄᑐᐊᒧᑦ",
//					  "ᐃᓄᑐᐊᕐᒧᑦ",
//					  "ᐃᓄᖕᒧᑦ",
//					  "ᐃᓄᑐᖃᕐᒧᑦ",
//					  "ᐃᓄᒻᒧᑦ",
//					  "ᐃᓄᑐᐊᒧᑦ",
//					  "ᐃᓄᑐᐊᕐᒧᑦ",
//					  "ᐃᓄᖕᒧᑦ",
//					  "ᐃᓄᑐᖃᕐᒧᑦ"
//					})
//			.nthCorrectionIs(1, false)
//			;
		
		return;	
	}

//	
//	@Test
//	public void test__SearchEndpoint__FirstAndSecondPagesOfHitsDiffer() throws Exception {
//		
//		SearchInputs searchInputs = new SearchInputs("nunavut").setHitsPerPage(10);
//
//		// Get the first page of hits
//		MockHttpServletResponse response = 
//				IUTServiceTestHelpers.postEndpointDirectly(
//					IUTServiceTestHelpers.EndpointNames.SEARCH,
//					searchInputs
//				);
//		List<SearchHit> hits1 = IUTServiceTestHelpers.toSearchResponse(response).hits;
//		
//		// Get the second page of hits
//		searchInputs.setPageNum(1);
//		response = 
//				IUTServiceTestHelpers.postEndpointDirectly(
//					IUTServiceTestHelpers.EndpointNames.SEARCH,
//					searchInputs
//				);
//		List<SearchHit> hits2 = IUTServiceTestHelpers.toSearchResponse(response).hits;
//		
//		double diffRatio = 0.80;
//		IUTTestHelpers.assertHitsPagesDifferByAtLeast("Hits from pages 1 and 2 should have been different", 
//				hits1, hits2, diffRatio);
//	}	
//	
//	@Test
//	public void test__SearchEndpoint__QueryIsAlreadyExpanded__DoesNotTryToExpandAgain() throws Exception {
//		
//		SearchInputs searchInputs = new SearchInputs("(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ)").setHitsPerPage(20);
//				
//		MockHttpServletResponse response = 
//				IUTServiceTestHelpers.postEndpointDirectly(
//					IUTServiceTestHelpers.EndpointNames.SEARCH,
//					searchInputs
//				);
//		
//		IUTServiceTestHelpers.assertExpandedQueryEquals(
//				"(ᓄᓇᕗ OR ᓄᓇᕗᒻᒥ OR ᓄᓇᕘᒥ OR ᓄᓇᕘᑉ OR ᓄᓇᕗᒻᒥᐅᑦ)", 
//				response);
//		
//		String[] queryWords = new String[] {"ᓄᓇᕗ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕘᑉ", "ᓄᓇᕗᒻᒥᐅᑦ"};
//		double tolerance = 0.7;
//		SearchResponse srchResponse = IUTServiceTestHelpers.toSearchResponse(response);
//		IUTServiceTestHelpers.assertMostHitsMatchWords(queryWords, response, tolerance);
//		Assert.assertTrue(srchResponse.totalHits > 10);
//	}	
//
//	@Test
//	public void test__expandQuery__QueryIsNotAnalyzable() throws Exception {
//		String nonAnalyzableWord = "oewrmweriorfgqer";
//		SearchResponse results = new SearchResponse();
//		endPoint.expandQuery(nonAnalyzableWord, results);
//		
//		List<String> gotQueryWords = results.expandedQueryWords;
//		String[] expQueryWords = new String[] {nonAnalyzableWord};
//		AssertHelpers.assertDeepEquals("", expQueryWords, gotQueryWords);
//		AssertHelpers.assertStringEquals("("+nonAnalyzableWord+")", results.expandedQuery);
//	}
//	
//
//	private void assertExpansionWordsAre(String[] expExpansionWords, QueryExpansion[] gotExpansions) throws IOException {
//		List<String> gotExpansionWords = new ArrayList<String>();
//		if (gotExpansions == null) {
//			gotExpansionWords = null;
//		} else {
//			for (QueryExpansion exp: gotExpansions) {
//				gotExpansionWords.add(exp.word);
//			}
//		}
//		AssertHelpers.assertDeepEquals("", expExpansionWords, gotExpansionWords);
//	}
	
	
}
