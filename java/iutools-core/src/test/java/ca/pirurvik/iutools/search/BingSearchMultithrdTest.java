package ca.pirurvik.iutools.search;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.testing.IUTTestHelpers;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class BingSearchMultithrdTest {

	/*******************************
	 * DOCUMENTATION TESTS
	 *******************************/
	
	@Test
	public void test__BingSearchMultithrd__Synopsis() throws Exception {
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
		String query = "(ᖃᕋᓴᐅᔭᒃᑯᑦ OR ᖃᐅᔨᓴᖅᑎᒃᑯ OR ᐃᓕᓐᓂᐊᖅᑐᒧᑦ)";
		
		//
		// First, you would create a BingSearchMultithrd...
		//
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		
		// 
		// Then you run the search
		//
		int maxHits = 10;
		PageOfHits results = searcher.search(query);
		Long totalEstHits = results.estTotalHits;
		List<SearchHit> hits = results.hitsCurrPage;
	}
	
	/*******************************
	 * VERIFICATION TESTS
	 *******************************/
	
	@Test
	public void test__BingSearchMultithrd__HappyPath() throws Exception {
		String [] terms = new String[] {"ᖃᕋᓴᐅᔭᒃᑯᑦ", "ᖃᐅᔨᓴᖅᑎᒃᑯ", "ᐃᓕᓐᓂᐊᖅᑐᒧᑦ"};
		String query = "(ᖃᕋᓴᐅᔭᒃᑯᑦ OR ᖃᐅᔨᓴᖅᑎᒃᑯ OR ᐃᓕᓐᓂᐊᖅᑐᒧᑦ)";
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		PageOfHits results = searcher.search(query);
		Long totalEstHits = results.estTotalHits;
		List<SearchHit> hits = results.hitsCurrPage;
		
		Assert.assertTrue("Total number of hits was lower than expected: "+totalEstHits, totalEstHits > 100);
		IUTTestHelpers.assertMostHitsMatchWords(terms, hits, 0.79);
	}	

	@Test
	public void test__BingSearchMultithrd__FirstAndSecondPagesOfHitsAreDifferent() throws Exception {
		String [] terms = new String[] {"ᖃᕋᓴᐅᔭᒃᑯᑦ", "ᖃᐅᔨᓴᖅᑎᒃᑯ", "ᐃᓕᓐᓂᐊᖅᑐᒧᑦ"};
		String query = "(ᖃᕋᓴᐅᔭᒃᑯᑦ OR ᖃᐅᔨᓴᖅᑎᒃᑯ OR ᐃᓕᓐᓂᐊᖅᑐᒧᑦ)";
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		
		PageOfHits results = searcher.search(query);
		List<SearchHit> hitsPage1 = results.hitsCurrPage;
		
		Set<String> urlsPage1 = new HashSet<String>();
		for (SearchHit hit: hitsPage1) urlsPage1.add(hit.url);
		
		results = searcher.retrieveNextPage(results);
		List<SearchHit> hitsPage2 = results.hitsCurrPage;
		
		double minDiffRatio = 0.8;
		IUTTestHelpers.assertHitsPagesDifferByAtLeast("Hits from pages 1 and 2 should have been different", 
				hitsPage1, hitsPage2, minDiffRatio);
	}	

	@Test
	public void test__BingSearchMultithrd__FollowPagesUntilTheEnd() throws Exception  {
		
		// term = 'nanivara'
		// This term does not have any hits beyond the second page
		//
//		String [] terms = new String[] {"ᓇᓂᕙᕋ", "ᓇᓂᓯᔪᓐᓇᕈᑦᑕ", "ᓇᓂᔭᐅᓯᒪᔪᑦ", "ᓇᓂᓯᔾᔪᒻᒧ", "ᓇᓂᓯᔪᓐᓇᕈᒪ"};
		String query = "(ᓇᓂᕙᕋ OR OR ᓇᓂᓯᔪᓐᓇᕈᑦᑕ OR ᓇᓂᔭᐅᓯᒪᔪᑦ OR ᓇᓂᓯᔾᔪᒻᒧ OR ᓇᓂᓯᔪᓐᓇᕈᒪ)";
		
		
		// Get the second page of hits
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		PageOfHits gotPage1 = searcher.search(query, 5);
		Assert.assertTrue("First page of hits should have had a next page", gotPage1.hasNext);
		
		
		// Get the second and last page of hits
		PageOfHits gotPage2 = searcher.retrieveNextPage(gotPage1);
		Assert.assertFalse("Second page of hits should have been the last", gotPage2.hasNext);
		Long expTotalHits = new Long(gotPage2.hitsCurrPage.size() + gotPage2.urlsAllPreviousHits.size());
		Assert.assertEquals("By the last page, the estimated total hits should have been equal to the total number of hits displayed", 
				expTotalHits, gotPage2.estTotalHits);
	}	

	@Test @Ignore
	public void test__BingSearchMultithrd__SearchWithOnlyPDFResults() throws Exception {
		String [] terms = new String[] {"ᖃᐅᔨᓴᕈᓐᓇᕐᒪᖔᑕ"};
		String query = "ᖃᐅᔨᓴᕈᓐᓇᕐᒪᖔᑕ";
		BingSearchMultithrd searcher = new BingSearchMultithrd();
		PageOfHits results = searcher.search(query);
		Long totalEstHits = results.estTotalHits;
		List<SearchHit> hits = results.hitsCurrPage;
		
		Assert.assertTrue("Total number of hits was lower than expected: "+totalEstHits, totalEstHits > 100);
		IUTTestHelpers.assertMostHitsMatchWords(terms, hits, 0.79);
	}	
	
}
