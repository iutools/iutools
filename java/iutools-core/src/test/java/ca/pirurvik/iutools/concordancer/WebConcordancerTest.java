package ca.pirurvik.iutools.concordancer;

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.concordancer.Alignment;
import ca.pirurvik.iutools.concordancer.DocAlignment;
import ca.pirurvik.iutools.concordancer.WebConcordancer;

public class WebConcordancerTest {
	
	WebConcordancer concordancer = null;
	
	@Before
	public void setUp() {
		concordancer = new WebConcordancer();
	}

	//////////////////////////////////
	// DOCUMENTATION TEST
	//////////////////////////////////
		
	@Test
	public void test__WebConcordancer__Synopsis() throws Exception {
		//
		// Use this class to fetch aligned sentences from a multilingual 
		// web page.
		// 
		// For example, say you are looking at the home page of the 
		// government of Nunavut web site, which is available in 
		// many different languages.
		// 
		URL url = new URL("https://www.gov.nu.ca/");
		
		// You want to get aligned sentences for the en-iu (Inuktut) language 
		// pair. You do this as follows.
		//
		DocAlignment pageAligment = 
				new WebConcordancer().alignPage(url, new String[] {"en", "iu"});
		
		// You can then check if the alignment was successful or not
		if (!pageAligment.success) {
			// These will provide details about what went wrong
			List<String> problems = pageAligment.problemsEncountered;
		} else {
			// Alignment was successful.
			// Loop the aligned bit of text.
			//
			// Note: getAligments() provides the alignments in the exact order in 
			//   which they appeared in the two pages.
			//
			for (Alignment alignment: pageAligment.getAligments()) {
				// This is the text for each of the two languages
				//
				String iuText = alignment.getText("iu");
				String enText = alignment.getText("en");			
				
				if (alignment.misaligned) {
					// This means that the text on the two sides does not actually 
					// correspond to each other. The aligner just got confused 
					// and it knows it.
				}
			}
		}
	}
	
	//////////////////////////////////
	// VERIFICATION TEST
	//////////////////////////////////
	
	@Test
	public void test__alignPage__HappyPath() throws Exception {
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment = 
					new WebConcordancer().alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
			.wasSuccessful()
			.contentIsPlainText("en", "iu")
			.urlForLangEquals("en", new URL("https://www.gov.nu.ca/"))
			.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu"))
			.pageInLangContains("en", "Premier of Nunavut")
			.pageInLangContains("iu", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ")
			.containsAlignment(new Alignment("en", "Government of Nunavut |", "iu", "ᓄᓇᕗᑦ ᒐᕙᒪᖓ |"))
			;
	}
	
	@Test
	public void test__langPairUnfilledSecond__HappyPath() throws Exception {
		DocAlignment alignment = 
				new DocAlignment("en", "iu").setPageContent("en", "Hello");
		
		Pair<String,String> gotLangPair = 
				concordancer.langPairUnfilledSecond(alignment);
		Pair<String,String> expLangPair = Pair.of("en", "iu");
		AssertObject.assertDeepEquals("Language pair not as expected", 
				expLangPair, gotLangPair);
	}

	@Test(expected=WebConcordancerException.class)
	public void test__langPairUnfilledSecond__BothLangFilled__RaisesException() 
			throws Exception {
		DocAlignment alignment = 
				new DocAlignment("en", "iu")
					.setPageContent("en", "Nunavut")
					.setPageContent("iu", "ᓄᓇᕗᑦ");
		
		Pair<String,String> gotLangPair = 
				concordancer.langPairUnfilledSecond(alignment);
	}
	
	@Test(expected=WebConcordancerException.class)
	public void test__langPairUnfilledSecond__NeitherLangFilled__RaisesException() 
			throws Exception {
		DocAlignment alignment = new DocAlignment("en", "iu");
		
		Pair<String,String> gotLangPair = 
				concordancer.langPairUnfilledSecond(alignment);
	}

	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////

}
