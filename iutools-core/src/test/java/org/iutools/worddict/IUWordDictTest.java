package org.iutools.worddict;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.junit.jupiter.api.Test;

import java.util.List;

public class IUWordDictTest {

	//////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////

	@Test
	public void test__IUWordDict__Synopsis() throws Exception {
		// The dictionary is a singleton
		IUWordDict dict = IUWordDict.getInstance();

		// Given an inuktitut word, you can get its dictionary entry
		IUWordDictEntry entry = dict.entry4word("inuksuk");

		// The input word can be in latin or syllabic alphabet
		entry = dict.entry4word("ᐃᓄᒃᓱᒃ");

		// The entry contains a bunch of information about the word

		// Definition (may be null)
		String definition = entry.definition;

		// Morphological decomposition in human-readable form
		List<MorphemeHumanReadableDescr> decomp = entry.morphDecomp;

		// List of possible English translations with scores.
		for (Pair<String,Double> scoredTranslations: entry.enTranslations()) {
			String translation = scoredTranslations.getLeft();
			Double score = scoredTranslations.getRight();

			// For each possible translation, you can get a list of bilingual
			// sentences that use that particular translation
			List<String[]> examples = entry.bilingualExamplesOfUse(translation);
		}

		// You can also get bilingual examples of use for all of the possible
		// translations
		List<String[]> examples = entry.bilingualExamplesOfUse();
	}


	//////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////

	@Test
	public void test__entry4word__Roman() throws Exception {
		IUWordDictEntry entry = IUWordDict.getInstance().entry4word("inuksuk");
		new AssertIUWordDictEntry(entry)
			.definitionEquals(null)
			.decompositionIs("inuksuk/1n")
			.bilingualExamplesStartWith(
				new String[] {"sitiivan <strong>inuksuk</strong>","Stephen Innuksuk"},
				new String[] {"lui <strong>inuksuk</strong>","Louis Inukshuk"},
				new String[] {"sitipirin <strong>inuksuk</strong>","Stephen Innuksuk"})
//			.possibleTranslationsAre("blah", "blob")
		;
	}

	@Test
	public void test__entry4word__Syllabics() throws Exception {
		IUWordDictEntry entry = IUWordDict.getInstance().entry4word("ᐃᓄᒃᓱᒃ");
		new AssertIUWordDictEntry(entry)
			.definitionEquals(null)
			.decompositionIs("inuksuk/1n")
			.bilingualExamplesStartWith(
				new String[] {"ᓯᑏᕙᓐ <strong>ᐃᓄᒃᓱᒃ</strong>","Stephen Innuksuk"},
				new String[] {"ᓗᐃ <strong>ᐃᓄᒃᓱᒃ</strong>","Louis Inukshuk"},
				new String[] {"ᓯᑎᐱᕆᓐ <strong>ᐃᓄᒃᓱᒃ</strong>","Stephen Innuksuk"})
//			.possibleTranslationsAre("blah", "blob")
		;
	}

	@Test
	public void test__entry4word__OutOfCorpusWord() throws Exception {
		String misspelledWord = "inuksssuk";
		IUWordDictEntry entry = IUWordDict.getInstance().entry4word(misspelledWord);
		new AssertIUWordDictEntry(entry)
			.definitionEquals(null)
			.decompositionIs(null)
			.bilingualExamplesStartWith()
			.possibleTranslationsAre()
		;
	}

}
