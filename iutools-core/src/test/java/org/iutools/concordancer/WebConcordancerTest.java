package org.iutools.concordancer;

import java.net.URL;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;

public abstract class WebConcordancerTest {
	
	protected static WebConcordancer concordancer = null;

	protected abstract WebConcordancer makeConcordancer(WebConcordancer.AlignOptions... options);

	protected WebConcordancer makeConcordancer() {
		return makeConcordancer(new WebConcordancer.AlignOptions[0]);
	}


	@Before
	public void setUp() {
		if (concordancer == null) {
			concordancer = makeConcordancer(WebConcordancer.AlignOptions.MAIN_TEXT);
		}
	}
	
	//////////////////////////////////
	// DOCUMENTATION TEST
	//////////////////////////////////
		
	// This test started failing on 2020-06-03
	// It seems the content of the www.gov.nu.ca home pages has changed
	// in a way that makes Maligna crash.
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
			concordancer.alignPage(url, new String[] {"en", "iu"});
		
		// You can then check if the alignment was successful or not
		if (!pageAligment.success) {
			// These will provide details about what went wrong
			Map<DocAlignment.Problem, Exception> problems = 
				pageAligment.problemsEncountered;
		} else {
			// SentencePair was successful.
			// Loop the aligned bit of text.
			//
			// Note: getAligments() provides the alignments in the exact order in 
			//   which they appeared in the two pages.
			//
			for (SentencePair alignment: pageAligment.getAligments()) {
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

		// You can also fetch the content of the parallel pages without
		// aligning their sentences.
		//
		concordancer
			.alignPage(url, new String[] {"en", "iu"});
	}
	
	//////////////////////////////////
	// VERIFICATION TEST
	//////////////////////////////////
	
	// This test started failing on 2020-06-03
	// It seems the content of the www.gov.nu.ca home pages has changed
	// in a way that makes Maligna crash.
	//
	@Test
	public void test__alignPage__HappyPath() throws Exception {
		URL url = new URL("https://www.gov.nu.ca/");
		concordancer =
			makeConcordancer(WebConcordancer.AlignOptions.MAIN_TEXT, WebConcordancer.AlignOptions.ALL_TEXT);

		DocAlignment pageAligment = 
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.didNotEncounterProblems()
			.pageTextIsNotHtml("en", "iu")
			.urlForLangEquals("en", new URL("https://www.gov.nu.ca/"))
			.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu"))
			.completeTextContains("en", "Premier of Nunavut")
			.completeTextContains("iu", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ")
			;
	}
	
	@Test
	public void test__alignPage__URLWith_PageNotFound() throws Exception {
		// Server returns Page not found for this URL
		//
		URL url = new URL("https://www.gov.nu.ca/doesnotexist/iu");
		DocAlignment pageAligment = 
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.encounteredProblems(DocAlignment.Problem.FETCHING_INPUT_URL)
			;
	}
	
	@Test
	public void test__alignPage__NonExistantServer() throws Exception {
		URL url = new URL("https://nonexistantserver.nu.ca");
		DocAlignment pageAligment = 
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.encounteredProblems(DocAlignment.Problem.FETCHING_INPUT_URL)
			;
	}

	@Test
	public void test__alignPage__MAIN_TEXT() throws Exception {
		concordancer = makeConcordancer(WebConcordancer.AlignOptions.MAIN_TEXT);
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment =
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.didNotEncounterProblems()
			.providesValuesFor(WebConcordancer.AlignOptions.MAIN_TEXT)
			.doesNotProvideValuesFor(
				WebConcordancer.AlignOptions.ALL_TEXT,
				WebConcordancer.AlignOptions.HTML, WebConcordancer.AlignOptions.ALIGNED_SENTENCES)
			.pageTextIsNotHtml()
			;
	}

	@Test
	public void test__alignPage__COMPLETE_TEXT() throws Exception {
		concordancer = makeConcordancer(WebConcordancer.AlignOptions.ALL_TEXT);
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment =
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.didNotEncounterProblems()
			.providesValuesFor(WebConcordancer.AlignOptions.ALL_TEXT)
			.doesNotProvideValuesFor(
				WebConcordancer.AlignOptions.MAIN_TEXT,
				WebConcordancer.AlignOptions.HTML, WebConcordancer.AlignOptions.ALIGNED_SENTENCES)
			.pageTextIsNotHtml()
			;
	}

	@Test
	public void test__alignPage__MAIN_TEXT_and_COMPLETE_TEXT() throws Exception {
		concordancer =
			makeConcordancer(WebConcordancer.AlignOptions.MAIN_TEXT, WebConcordancer.AlignOptions.ALL_TEXT);
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment =
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.didNotEncounterProblems()
			.providesValuesFor(WebConcordancer.AlignOptions.MAIN_TEXT, WebConcordancer.AlignOptions.ALL_TEXT)
			.doesNotProvideValuesFor(
				WebConcordancer.AlignOptions.HTML, WebConcordancer.AlignOptions.ALIGNED_SENTENCES)
			.pageTextIsNotHtml()
			;
	}

	@Test
	public void test__alignPage__MAIN_TEXT_and_HTML() throws Exception {
		concordancer =
			makeConcordancer(WebConcordancer.AlignOptions.MAIN_TEXT, WebConcordancer.AlignOptions.HTML);
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment =
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.didNotEncounterProblems()
			.providesValuesFor(WebConcordancer.AlignOptions.MAIN_TEXT, WebConcordancer.AlignOptions.HTML)
			.doesNotProvideValuesFor(
				WebConcordancer.AlignOptions.ALIGNED_SENTENCES, WebConcordancer.AlignOptions.ALL_TEXT)
			.pageTextIsNotHtml()
			;
	}

	@Test
	public void test__alignPage__MAIN_TEXT_and_ALIGNED_SENTENCES() throws Exception {
		concordancer =
			makeConcordancer(WebConcordancer.AlignOptions.MAIN_TEXT, WebConcordancer.AlignOptions.ALIGNED_SENTENCES);
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment =
			concordancer.alignPage(url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.didNotEncounterProblems()
			.providesValuesFor(WebConcordancer.AlignOptions.MAIN_TEXT, WebConcordancer.AlignOptions.ALIGNED_SENTENCES)
			.doesNotProvideValuesFor(
				WebConcordancer.AlignOptions.HTML, WebConcordancer.AlignOptions.ALL_TEXT)
			.pageTextIsNotHtml()
			;
	}

	@Test
	public void test__alignPage__PageWhoseOtherPageCannotBeDeterminedThroughURLPatternRules() throws Exception {
		URL url = new URL("https://www.gov.nu.ca/honourable-joe-savikataaq-4");
		concordancer = makeConcordancer(WebConcordancer.AlignOptions.ALL_TEXT);
		DocAlignment pageAligment =
			concordancer.alignPage(
				url, new String[] {"en", "iu"});

		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for " + url + " were not as expected.")
			.didNotEncounterProblems()
			.urlForLangEquals("en", new URL("https://www.gov.nu.ca/honourable-joe-savikataaq-4"))
			// This URL auto forwards to https://www.gov.nu.ca/iu/juu-savikataaq-4
			.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu/juu-savikataaq-4"))

			.completeTextContains("en", "Premier of Nunavut")
			.completeTextContains("iu", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ")
		;
	}

	@Test
	public void test__fetchParallelPages__HappyPath() throws Exception {
		// The English URL for this IU url is:
		//
		//   https://www.gov.nu.ca/
		//
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment =
			new DocAlignment("en", "iu")
			.setPageURL("en", url);

		concordancer.fetchParallelPages(pageAligment);
		DocAlignmentAsserter.assertThat(pageAligment, "SentencePair results for "+url+" were not as expected.")
			.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu"))
			;
	}

	@Test
	public void test__langPairUnfilledSecond__HappyPath() throws Exception {
		DocAlignment alignment = 
				new DocAlignment("en", "iu")
					.setPageURL("en", new URL("http://somewhere.com/index_e.html"));

		Pair<String,String> gotLangPair = 
				concordancer.langAndOtherLang(alignment);
		Pair<String,String> expLangPair = Pair.of("en", "iu");
		AssertObject.assertDeepEquals("Language pair not as expected", 
				expLangPair, gotLangPair);
	}

	@Test
	public void test__langAndOtherLang__BothLangFilled__ReturnsPairOfNulls() 
			throws Exception {
		DocAlignment alignment = 
				new DocAlignment("en", "iu")
					.setPageText("en", "Nunavut")
					.setPageText("iu", "ᓄᓇᕗᑦ");
		
		Pair<String,String> gotLangPair = 
				concordancer.langAndOtherLang(alignment);
		Pair<String,String> expLangPair = Pair.of(null, null);
		AssertObject.assertDeepEquals(
			"", expLangPair, gotLangPair);
	}
	
	@Test
	public void test__langAndOtherLang__NeitherLangFilled__ReturnsPairOfNulls()
			throws Exception {
		DocAlignment alignment = new DocAlignment("en", "iu");
		
		Pair<String,String> gotLangPair = 
				concordancer.langAndOtherLang(alignment);
	}	

	//////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////

}
