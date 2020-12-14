package org.iutools.search;



import org.junit.Test;

import ca.nrc.data.harvesting.SearchEngine.Query;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchResults;
import org.iutools.testing.IUTTestHelpers;

public class IUSearchEngineTest {

	@Test
	public void test__IUSearchEngine__HappyPath() throws Exception {
		
		String [] expandedTerm = new String[] {
				"ᓄᓇᕗ", "ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕘᑉ", "ᓄᓇᕗᒻᒥᐅᑦ", "ᓄᓇᕗᑦ"};
		Query query = new Query(expandedTerm).setLang("iu");		
		IUSearchEngine searcher = new IUSearchEngine();
		SearchResults results = searcher.search(query);
		IUTTestHelpers.assertSufficientHitsFound(results, 20);		
//		IUTTestHelpers.assertMostHitsMatchWords(expandedTerm, hits, 0.80);
//		IUTTestHelpers.assertMostHitsAreInuktut(hits, 0.95, 0.8);
	}
	
	@Test(expected=SearchEngineException.class)
	public void test__IUSearchEngine__NonIUQuery__RaisesException() throws Exception {
		Query query = new Query("ᓄᓇᕗ").setLang("fr");		
		IUSearchEngine searcher = new IUSearchEngine();
		searcher.search(query);
	}

}
