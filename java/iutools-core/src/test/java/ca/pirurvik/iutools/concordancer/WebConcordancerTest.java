package ca.pirurvik.iutools.concordancer;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
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
	
	
	// For now, alignPage returns an empty set of alignments, EXCEPT for 
	// when we ask for pages on mocksite.nu.ca. 
	//
	// In that case, it returns some hard coded results
	//
	@Test
	public void test__alignPage__MockSite() throws Exception {
		URL url = new URL("http://mocksite.nu.ca/en");
		DocAlignment pageAligment = 
					new WebConcordancer().alignPage(url, new String[] {"en", "iu"});

		Pair<String,String>[] expAlignments = new Pair[] {
				Pair.of("As of today, there are no known cases of COVID-19 in the territory.", 
						"ᐅᓪᓗᒥᒧᑦ ᑎᑭᖦᖢᒍ, ᖃᐅᔨᒪᔭᐅᔪᓂᒃ ᓄᕙᒡᔪᐊᕐᓇᖅ-19−ᒧᑦ ᐱᑕᖃᙱᓚᖅ ᑕᒫᓂ ᐅᑭᐅᖅᑕᖅᑐᒥ ᐊᕕᒃᓯᒪᓂᕆᔭᐅᔪᒥ."),
				Pair.of("The public health communicable disease team is following approximately 70 persons under investigation.", 
						"ᑭᒃᑯᑐᐃᓐᓇᓂᒃ ᐋᓐᓂᐊᖃᖅᑕᐃᓕᑎᑦᑎᓂᕐᒧᑦ ᐊᐃᑦᑐᕐᓘᑕᐅᔪᓐᓇᖅᑐᓄᑦ ᖃᓂᒪᑖᕆᔭᐅᔪᓐᓇᖅᑐᓄᑦ ᐱᓕᕆᖃᑎᒌᑦ ᒪᓕᒃᓯᕗᖅ 50-60−ᐸᓗᖕᓂᒃ ᐃᓄᖕᓂᒃ ᑖᒃᑯᐊ ᖃᐅᔨᓴᖅᑕᐅᕙᓪᓕᐊᓪᓗᑎᒃ.")
		};
		assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
			.urlForLangEquals("en", new URL("http://mocksite.nu.ca/en"))
			.urlForLangEquals("iu", new URL("http://mocksite.nu.ca/iu"))
			.pageInLangContains("en", "COVID-19")
			.pageInLangContains("iu", "ᓄᕙᒡᔪᐊᕐᓇᖅ-19−ᒧᑦ")
			.alignmentsEqual(
					"Mock Alignments were not as expected",
					"en", "iu", expAlignments);
			;

	}
	
	@Test
	public void test__alignPage__HappyPath() throws Exception {
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment = 
					new WebConcordancer().alignPage(url, new String[] {"en", "iu"});

		assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
			.wasSuccessful()
			.urlForLangEquals("en", new URL("https://www.gov.nu.ca/"))
			.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu"))
			.pageInLangContains("en", "Premier of Nunavut")
			.pageInLangContains("iu", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ")
//			.containsAlignment(new Alignment("en", "BLAH", "iu", "BLOB"))
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
	
	
	private AlignmentResultAssertion assertThat(DocAlignment pageAligment, 
			String message) {
		AlignmentResultAssertion assertion = 
				new AlignmentResultAssertion(pageAligment);
		return assertion;
	}
	
	public static class AlignmentResultAssertion {

		String baseMessage = "";
		
		DocAlignment gotAlignmentResult = null;
		
		public AlignmentResultAssertion(DocAlignment pageAligment) {
			this.gotAlignmentResult = pageAligment;
		}

		public void containsAlignment(Alignment expAlignment) {
			
			boolean found = false;
			
			Assert.assertTrue("Could not find aligment", found);			
		}

		public AlignmentResultAssertion wasSuccessful() {
			Assert.assertTrue(
					baseMessage+"\nAlignment should have been successful.",
					gotAlignmentResult.success);
			return this;
		}

		public void alignmentsEqual(String mess, String lang1, String lang2, 
				Pair<String, String>[] expAlPairs) throws Exception {

			String[] expAlStrs = new String[expAlPairs.length];
			for (int ii=0; ii < expAlPairs.length; ii++) {
				expAlStrs[ii] = 
						"(" +
						lang1 + ":" + expAlPairs[ii].getFirst() + 
						" <--> " +
						lang2 + ":" + expAlPairs[ii].getSecond() + 
						")";
			}
			List<Alignment> gotAlList = this.gotAlignmentResult.getAligments();
			String[] gotAlStrs = new String[gotAlList.size()];
			for (int ii=0; ii < gotAlList.size(); ii++) {
				gotAlStrs[ii] = gotAlList.get(ii).toString();
			}
			
			AssertObject.assertDeepEquals(
					"Alignments texts were not as expected.", 
					expAlStrs, gotAlStrs);
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