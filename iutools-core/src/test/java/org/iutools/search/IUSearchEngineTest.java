package org.iutools.search;



import ca.nrc.config.Config;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.data.harvesting.SearchEngine.Query;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchResults;
import org.iutools.testing.IUTTestHelpers;

public class IUSearchEngineTest {

	private String bingTestKey;

	@Before
	public void setup() throws Exception {
		this.bingTestKey = assumeTestBingKeyIsDefined();
	}

	@Test
	public void test__IUSearchEngine__HappyPath() throws Exception {
		
		String [] expandedTerm = new String[] {
				"ᓄᓇᕗ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕘᑉ", "ᓄᓇᕗᒻᒥᐅᑦ", "ᓄᓇᕗᑦ"};
		Query query = new Query(expandedTerm).setLang("iu");		
		IUSearchEngine searcher = new IUSearchEngine(bingTestKey);
		SearchResults results = searcher.search(query);
		IUTTestHelpers.assertSufficientHitsFound(results, 20);		
//		IUTTestHelpers.assertMostHitsMatchWords(expandedTerm, hits, 0.80);
//		IUTTestHelpers.assertMostHitsAreInuktut(hits, 0.95, 0.8);
	}
	
	@Test(expected=SearchEngineException.class)
	public void test__IUSearchEngine__NonIUQuery__RaisesException() throws Exception {
		Query query = new Query("ᓄᓇᕗ").setLang("fr");		
		IUSearchEngine searcher = new IUSearchEngine(bingTestKey);
		searcher.search(query);
	}

	////////////////////////////////
	// TEST HELPERS
	////////////////////////////////

	protected static String assumeTestBingKeyIsDefined() throws Exception {
		String bingTestKeyPropName = 	"org.iutools.search.testBingKey";
		String key = Config.getConfigProperty(bingTestKeyPropName, false);
		Assume.assumeTrue(
		"No bing key defined. Skipping all tests in SearchEngine_BingTest." +
		"To run those tests, obtain a Bing key from Microsoft Azure and setup a config property "+bingTestKeyPropName+" with that value.",
		key != null);

		return key;
	}
}
