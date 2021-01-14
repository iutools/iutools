package org.iutools.concordancer;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class DocAlignmentTest {

	//////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////

	@Test
	public void test__DocAlignment__Synopsis() throws Exception {
		// This class is used to store information about the various language
		// version of a same document.
		//
		// Here is how you typically construct a DocAlignment object.
		// First, you specify the languages that the document is available in
		//
		DocAlignment docAl =
			// The document is available in English, French and Inuktitut
			new DocAlignment("en", "fr", "iu");

		// At this point, you can find out the list of avalaible language
		//
		Set<String> langs = docAl.getLanguages();

		// Next, you might provide the unique ID of the document in the various
		// languages. For example, if the document is a web page, the ID could
		// be the page's URL.
		//
		docAl
			.setPageID("en", "http://www.lexicon.org/en/helloworld")
			.setPageID("fr", "http://www.lexicon.org/fr/bonjourlemonde")
			.setPageID("iu", "http://www.lexicon.org/iu/ainunavut")
		;

		// At this point, you can

		// Next, you you might provide the document's sentences in those languages.
		// You can provide sentence for the complete document AND for
		// the "main" part of the document (i.e. the part that EXCLUDES
		// boilerplate text).
		//
		// Complete document...
		docAl
			.setPageSentences("en",
			"Hello world.", "Good morning.")
			.setPageSentences("fr",
			"Bonjour le monde.", "Bonjour.")
			.setPageSentences("fr",
			"ai nunavut, ulaakut.")
			;

		// Main part of the document. In this case it's identical to
		// the whole document....
		docAl
			.setPageMainSentences("en",
			"Hello world.", "Good morning.")
			.setPageMainSentences("fr",
			"Bonjour le monde.", "Bonjour.")
			.setPageMainSentences("fr",
			"ai nunavut, ulaakut.")
			;

		// At this point, you can obtain the sentences and/or content of the
		// alignment in various languages
		//
		for (String aLang: docAl.getLanguages()) {
			String wholeText = docAl.getPageText(aLang);
			String mainText = docAl.getPageMainText(aLang);
			List<String> wholeSentences = docAl.getPageSentences(aLang);
			List<String> mainSentences = docAl.getPageMainSentences(aLang);
		}

		// Next, you can provide the sentence level alignment information
		// for some language pairs
		//
		docAl
			.setAlignments(
				"en", "fr",
				new String[]{"0-0", "1-1"})

			// Both en sentences align to the 1st iu sentence
			.setAlignments(
				"en", "iu",
				new String[]{"0-0", "1-0"})
		;

		// At this point, you
	}

	//////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////

	@Test
	public void test__hasContentForBothLanguages__HasContent() throws Exception {
		DocAlignment alignment = new DocAlignment("en", "fr")
			.setPageText("en", "Hello world")
			.setPageText("fr", "Bonjour le monde");
		
		Assert.assertTrue(alignment.hasTextForBothLanguages(DocAlignment.PageSection.ALL));
	}

	@Test
	public void test__hasContentForBothLanguages__OnlyHasContentForOneLang()
	throws Exception {
		DocAlignment alignment = new DocAlignment("en")
			.setPageText("en", "Hello world");
		
		Assert.assertFalse(alignment.hasTextForBothLanguages(DocAlignment.PageSection.MAIN));
	}

	@Test
	public void test__hasContentForBothLanguages__HasNoContentAtAll() {
		DocAlignment alignment = new DocAlignment();
		
		Assert.assertFalse(alignment.hasTextForBothLanguages(DocAlignment.PageSection.MAIN));
	}
}
