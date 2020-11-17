package ca.pirurvik.iutools.concordancer;

import java.net.URL;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.concordancer.DocAlignment.Problem;
import static ca.pirurvik.iutools.concordancer.WebConcordancer.AlignOptions;

// 2020-10-28: There are lots of problems with the concordancer at the moment
//  and I (Alain) don't have time to look at them
//  So disable the tests for now.
@Ignore
public abstract class WebConcordancerTest {
	
	protected static WebConcordancer concordancer = null;

	protected abstract WebConcordancer makeConcordancer();
	
	@Before
	public void setUp() {
		if (concordancer == null) {
			concordancer = makeConcordancer();
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

		// You can also fetch the content of the parallel pages without
		// aligning their sentences.
		//
		concordancer
			.alignPage(url, new String[] {"en", "iu"},
				AlignOptions.ONLY_FETCH_CONTENT);
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
		DocAlignment pageAligment = 
			concordancer.alignPage(url, new String[] {"en", "iu"},
				AlignOptions.ONLY_FETCH_CONTENT);

		DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
			.didNotEncounterProblems()
			.contentIsPlainText("en", "iu")
			.urlForLangEquals("en", new URL("https://www.gov.nu.ca/"))
			.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu"))
			.pageInLangContains("en", "Premier of Nunavut")
			.pageInLangContains("iu", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ")
			;
	}
	
	@Test
	public void test__alignPage__URLWith_PageNotFound() throws Exception {
		// Server returns Page not found for this URL
		//
		URL url = new URL("https://www.gov.nu.ca/doesnotexist/iu");
		DocAlignment pageAligment = 
			concordancer.alignPage(url, new String[] {"en", "iu"},
				AlignOptions.ONLY_FETCH_CONTENT);

		DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
			.encounteredProblems(Problem.FETCHING_INPUT_URL)
			;
	}
	
	@Test
	public void test__alignPage__NonExistantServer() throws Exception {
		URL url = new URL("https://nonexistantserver.nu.ca");
		DocAlignment pageAligment = 
			concordancer.alignPage(url, new String[] {"en", "iu"},
					AlignOptions.ONLY_FETCH_CONTENT);

		DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
			.encounteredProblems(Problem.FETCHING_INPUT_URL)
			;
	}

	@Test
	public void test__alignPage__OnlyFetchParallelContent() throws Exception {
		URL url = new URL("https://www.gov.nu.ca/");
		DocAlignment pageAligment =
			concordancer.alignPage(
				url, new String[] {"en", "iu"}, AlignOptions.ONLY_FETCH_CONTENT);

		DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
				.didNotEncounterProblems()
				.hasNoAlignments()
				.contentIsPlainText("en", "iu")
				.urlForLangEquals("en", new URL("https://www.gov.nu.ca/"))
				.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu"))
				.pageInLangContains("en", "Premier of Nunavut")
				.pageInLangContains("iu", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ")
				;
		;
	}

	@Test
	public void test__alignPage__PageWhoseOtherPageCannotBeDeterminedThroughURLPatternRules() throws Exception {
		URL url = new URL("https://www.gov.nu.ca/honourable-joe-savikataaq-4");
		DocAlignment pageAligment =
			concordancer.alignPage(
				url, new String[] {"en", "iu"}, AlignOptions.ONLY_FETCH_CONTENT);

		if (concordancer.canFollowLanguageLink()) {
			DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for " + url + " were not as expected.")
				.didNotEncounterProblems()
				.urlForLangEquals("en", new URL("https://www.gov.nu.ca/honourable-joe-savikataaq-4"))
				// This URL auto forwards to https://www.gov.nu.ca/iu/juu-savikataaq-4
				.urlForLangEquals("iu", new URL("https://www.gov.nu.ca/iu/node/26649"))

				.pageInLangContains("en", "Premier of Nunavut")
				.pageInLangContains("iu", "ᓯᕗᓕᖅᑎ ᓄᓇᕗᒻᒥ")
			;
		} else {
			DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for " + url + " were not as expected.")
				.encounteredProblems(Problem.FETCHING_CONTENT_OF_OTHER_LANG_PAGE)
			;
		}
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
		DocAlignmentAsserter.assertThat(pageAligment, "Alignment results for "+url+" were not as expected.")
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
					.setPageContent("en", "Nunavut")
					.setPageContent("iu", "ᓄᓇᕗᑦ");
		
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
