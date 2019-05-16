package ca.pirurvik.iutools.testing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.search.SearchHit;

public class IUTTestHelpers {

	public static void assertMostHitsMatchWords(String[] queryWords, List<SearchHit> gotHits, double tolerance) {
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
		int unmatchedPercent = (int)(Math.round(unmatchedRatio * 100));
		Assert.assertTrue(
				"There were too many urls "+unmatchedPercent+"%) that did not match the  query words '"+regex+".\n"
			  + "Unmatched URLs were:\n  "
			  + String.join("\n  ", unmatchedURLs),
			  unmatchedRatio <= tolerance
			);	
		}

	public static void assertHitsPagesDifferByAtLeast(String message, List<SearchHit> hitsPage1, List<SearchHit> hitsPage2,
			double minDiffRatio) {
		
		Assert.assertFalse("The second page of hits did not find any URL that was not already in the first page. ", 
						   hitsPage2.size() == 0);
		
		
		Set<String> urls1 = new HashSet<String>();
		for (SearchHit hit: hitsPage1) {
			urls1.add(hit.url);
		}
		
		Set<String> urls2 = new HashSet<String>();
		for (SearchHit hit: hitsPage2) {
			urls2.add(hit.url);
		}
		
		Set<String> intersection = new HashSet<String>(urls1);
		intersection.retainAll(urls2);
		
		
		double gotDiffRatio = 1 - (1.0 * intersection.size()) / Math.max(urls1.size(), urls2.size());
		
		message += 
				 "\nThe difference ratio between hits of the first and second page ("+gotDiffRatio+") was too low. Here are the list of URLs for each list of hits.\n\n*** hits1:\n"
				+PrettyPrinter.print(urls1)
				+"\n\n*** hits2:\n"
				+PrettyPrinter.print(urls2);
		
		Assert.assertTrue(message, gotDiffRatio >= minDiffRatio);
	}

}
