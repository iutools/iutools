package ca.pirurvik.iutools.search;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.testing.IUTTestHelpers;

import org.junit.Assert;
import org.junit.Test;

public class BingSearchMultithrdTest {

	/*******************************
	 * DOCUMENTATION TESTS
	 *******************************/
	
	@Test
	public void test__BingSearchMultithrd__Synopsis() {
		//
		// This class implements a workaround to a Bing Problem for Inuktut search.
		// Essentially, if you do a Bing search with multiple Inuktut words, it
		// tends to return mostly pages that are written in asian languages like
		// Chinese, Hindi, Korean, etc...
		// 
		// Alain has contacted the Bing team to report the problem, but they don't
		// seem to be moving towards a solution fast.
		//
		// So instead, we perform multi-term searches as a number of single-word
		// searches that run in separate threads.
		//
		// For example, say you want to do an inuktitut search for the following words:
		//
		String [] terms = new String[] {"ᖃᕋᓴᐅᔭᒃᑯᑦ", "ᖃᐅᔨᓴᖅᑎᒃᑯ", "ᐃᓕᓐᓂᐊᖅᑐᒧᑦ"};
		
		//
		// First, you would create a BingSearchMultithrd...
		//
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		
		// 
		// Then you run the search
		//
		int maxHits = 10;
		Pair<Long,List<SearchHit>> results = searcher.search(terms);
		Long totalEstHits = results.getFirst();
		List<SearchHit> hits = results.getSecond();
	}
	
	/*******************************
	 * VERIFICATION TESTS
	 *******************************/
	
	@Test
	public void test__BingSearchMultithrd__HappyPath() {
		String [] terms = new String[] {"ᖃᕋᓴᐅᔭᒃᑯᑦ", "ᖃᐅᔨᓴᖅᑎᒃᑯ", "ᐃᓕᓐᓂᐊᖅᑐᒧᑦ"};
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		Pair<Long,List<SearchHit>> results = searcher.search(terms);
		Long totalEstHits = results.getFirst();
		List<SearchHit> hits = results.getSecond();
		
		Assert.assertTrue("Total number of hits was lower than expected: "+totalEstHits, totalEstHits > 100);
		IUTTestHelpers.assertMostHitsMatchWords(terms, hits, 0.75);
	}	

	@Test
	public void test__BingSearchMultithrd__FirstAndSecondPagesOfHitsAreDifferent() {
		String [] terms = new String[] {"ᖃᕋᓴᐅᔭᒃᑯᑦ", "ᖃᐅᔨᓴᖅᑎᒃᑯ", "ᐃᓕᓐᓂᐊᖅᑐᒧᑦ"};
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		
		Pair<Long,List<SearchHit>> results = searcher.search(terms, 0);
		List<SearchHit> hitsPage1 = results.getSecond();
		
		results = searcher.search(terms, 1);
		List<SearchHit> hitsPage2 = results.getSecond();
		
		AssertHelpers.assertDeepNotEqual("Hits from first and second page should have been different", hitsPage1, hitsPage2);
	}	
}
