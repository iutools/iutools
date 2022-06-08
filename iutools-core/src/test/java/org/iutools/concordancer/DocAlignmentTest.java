package org.iutools.concordancer;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
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
		// At the very minimum, you must provide the list of languages that the
		// document is available in (English, French and Inuktitut in the example
		// below.
		DocAlignment docAl =
			// The document is available in English, French and Inuktitut
			new DocAlignment("en", "fr", "iu");

		// At this point, you can find out the list of avalaible language
		//
		Set<String> langs = docAl.languages();

		// Next, you can optionally provide the unique ID of the document in the various
		// languages. For example, if the document is a web page, the ID could
		// be the page's URL.
		//
		docAl
			.setPageID("en", "http://www.lexiconsRus.org/en/helloworld")
			.setPageID("fr", "http://www.lexiconsRus.org/fr/bonjourlemonde")
			.setPageID("iu", "http://www.lexiconsRus.org/iu/ainunavut")
		;

		// At this point, you can ask for the ID of the doc in the various
		// languages
		for (String lang: docAl.languages()) {
			String id = docAl.getPageID(lang);
			// Note: This will throw an Exception if the doc ID is not a valid
			//  URL
			URL url = docAl.getPageURL(lang);
		}

		// Next, you can optionally provide the "raw" content of the document
		// in the various languages. The raw content might be HTML, XML,
		// or some binary format like PDF or MS Word.
		//
		docAl
			.setPageRawContent(
				"en",
				"<html><body><div><img src=\"logo.png\">LexiconsRus</div>Hello world. Good morning.</body></html>")
			.setPageRawContent(
				"en",
				"<html><body><div><img src=\"logo.png\">LexiconsRus</div>Bonjour le monde. Bonjour.</body></html>")
			.setPageRawContent(
				"en",
				"<html><body><div><img src=\"logo.png\">LexiconsRus</div>ai nunavut, ulaakut.</body></html>")
			;

		// At this point, you can get the raw content each language
		for (String lang: docAl.languages()) {
			String html = docAl.getPageRawContent(lang);
		}

		// Next, you can optionally provide the plain text for the document.
		// You can provide the plain text for the whole document, as well the
		// plain text for the "main" part of the document (i.e. the part that
		// excludes boilerplate text).
		//
		docAl
			.setPageText("en", "LexiconsRus\nHello world. Good morning.")
			.setPageText("en", "Hello world. Good morning.")
			.setPageText("fr", "LexiconsRus\nBonjour le monde. Bonjour.")
			.setPageText("fr", "Bonjour le monde. Bonjour.")
			.setPageText("iu", "LexiconsRus\nai nunavut, ulaakut.")
			.setPageText("iu", "ai nunavut, ulaakut.")
			;

		// At this point, you can get the plain text for the whole document and
		// its main part, for the various languages
		for (String lang: docAl.languages()) {
			String wholeText = docAl.getPageText(lang);
			String mainText = docAl.getPageMainText(lang);
		}

		// Next, you can optionally provide segmentation of the plain texts into
		// sentences
		docAl
			.setPageSentences("en",
				"LexiconsRus", "Hello world.", "Good morning.")
			.setPageMainSentences("en",
				"Hello world.", "Good morning.")

			.setPageSentences("fr",
				"LexiconsRus", "Bonjour le monde.", "Bonjour.")
			.setPageMainSentences("fr",
				"Bonjour le monde.", "Bonjour.")

			.setPageSentences("fr",
				"LexiconsRus", "ai nunavut, ulaakut.")
			.setPageMainSentences("fr",
				"ai nunavut, ulaakut.")
			;

		// At this point, you can obtain the sentences in the various languages
		//
		for (String lang: docAl.languages()) {
			List<String> wholeSentences = docAl.getPageSentences(lang);
			List<String> mainSentences = docAl.getPageMainSentences(lang);
		}

		// Next, you can optionally provide the alignment information
		// for some language pairs
		//
		// For example, here is how you provide the alignments for the "main"
		// sentence of the en-iu pair, at the sentence AND token level
		//
		docAl.addAlignment(
			DocAlignment.PageSection.MAIN,
			new AlignmentSpec("en", "iu", "0-1:0")
				.setTokenAlignment(
					// Tokenization in each language
					new String[] {"hello", "world", ".", "Good", "morning", "."},
					new String[] {"ai", "nunavut", "ulaakut", "."},
					// SentencePair of the tokens
					"0:0 1-2:1 3-4:2 5:3")
			);

		// If you don't care about word-level alignment, you can omit it.
		// For example, here is how you would provide sentence-level ONLY
		// alignment for the en-fr pair.
		//
		docAl
			.addAlignment(
				DocAlignment.PageSection.MAIN,
				new AlignmentSpec("en", "fr", "0:0"))
			.addAlignment(
				DocAlignment.PageSection.MAIN,
				new AlignmentSpec("en", "fr", "1:1-2"))
			.addAlignment(
				DocAlignment.PageSection.MAIN,
				new AlignmentSpec("en", "fr", "2:3"))
			.addAlignment(
				DocAlignment.PageSection.MAIN,
				new AlignmentSpec("en", "fr", "3-4:4"))
			.addAlignment(
				DocAlignment.PageSection.MAIN,
				new AlignmentSpec("en", "fr", "5:5"))
			;

		// At this point, you can obtain sentence-level aligned pairs for
		// pairs of languages
		for (String lang: docAl.languages()) {
			if (lang.equals("en")) continue;
			List<SentencePair> sentAlign = docAl.getAligments("en", lang);
			List<SentencePair> sentAlignMain =
				docAl.getAligments("en", lang, DocAlignment.PageSection.MAIN);
		}
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
