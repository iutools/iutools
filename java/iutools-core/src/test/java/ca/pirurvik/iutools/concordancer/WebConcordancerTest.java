package ca.pirurvik.iutools.concordancer;

import java.net.URL;
import java.util.List;

import org.junit.Test;

import ca.nrc.testing.AssertString;
import ca.pirurvik.iutools.concordancer.Alignment;
import ca.pirurvik.iutools.concordancer.AlignmentResult;
import ca.pirurvik.iutools.concordancer.WebConcordancer;

public class WebConcordancerTest {

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
		AlignmentResult pageAligment = 
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
		AlignmentResult pageAligment = 
					new WebConcordancer().alignPage(url, new String[] {"en", "iu"});

		assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
			.urlForLangEquals("en", new URL("https://www.gov.nu.ca/"))
//			.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/blobIU"))
			.pageInLangContains("en", "Government of Nunavut")
//			.pageInLangContains("iu", "BLOB")
			;
	}

	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////
	
	
	private AlignmentResultAssertion assertThat(AlignmentResult pageAligment, 
			String message) {
		AlignmentResultAssertion assertion = 
				new AlignmentResultAssertion(pageAligment);
		return assertion;
	}
	
	public static class AlignmentResultAssertion {

		String baseMessage = "";
		
		AlignmentResult gotAlignmentResult = null;
		
		public AlignmentResultAssertion(AlignmentResult pageAligment) {
			this.gotAlignmentResult = pageAligment;
		}

		public AlignmentResultAssertion urlForLangEquals(
					String lang, URL expURL) throws Exception {
			URL gotURL = gotAlignmentResult.getPageURL(lang);
			AssertString.assertStringEquals(
					"URL of the "+lang+" page was not as expected.",
					expURL.toString(), gotURL.toString());;
			return this;
		}

		public AlignmentResultAssertion pageInLangContains(String lang, String expText) {
			String gotText = gotAlignmentResult.getPageContent(lang);
			AssertString.assertStringContains(
					baseMessage+"\nContent of the "+lang+" page was not as expected", 
					gotText, expText);	
			return this;
		}
		
	}

}
