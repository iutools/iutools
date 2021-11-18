package org.iutools.webservice.worddict;

import ca.nrc.config.ConfigException;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.iutools.script.TransCoder;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointTest;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.AssertMultilingualDictEntry;
import org.iutools.worddict.MultilingualDictEntry;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.function.Consumer;

public class WordDictEndpointTest extends EndpointTest {
	@Override
	public WordDictEndpoint makeEndpoint() throws SpellCheckerException, FileNotFoundException, ConfigException, ServiceException {
		return new WordDictEndpoint();
	}

	/***********************
	 * DOCUMENTATION TESTS
	 ***********************/

	@Test
	public void test__WordDictEndpoint__Synopsis() throws Exception {
		// Use this endpoint to retrieve information about a word

		// Feed a word pattern to the endpoint inputs.
		// This can be a complete word, or the start of a word
		String queryPattern = "inuksuk";
		WordDictInputs inputs = new WordDictInputs(queryPattern);
		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		// This give you the list of words that start with the word pattern
		List<String> hits = epResult.matchingWords;


		// If the query pattern matched a word exactly, then you can get its
		// dictionary entry.
		// Note that the entry will be available even if the query pattern matched
		// other words as well.
		MultilingualDictEntry wordEntry = epResult.queryWordEntry;

		// By default, the dictionary assumes the word is in Inuktitut.
		// But you can also specify that the word is in English.
		// At that point, information will be displayed about that English word,
		// Including list of Inuktitut translations and bilingual examples of
		// use.
		//
		// This can be useful for finding the different ways in which an English
		// word can be rendered into Inuktitut.
		{
			inputs.word = "housing";
			inputs.lang = "en";
			epResult = (WordDictResult) endPoint.executeThenConvert(inputs);
		}
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__WordDictEndpoint__QueryMatchesWordExactlyAndNothingMore() throws Exception {

		// There is only one word in the dictionary that matches this
		// pattern
		String query = "inuksuk";
		WordDictInputs inputs = new WordDictInputs(query);

		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("inuksuk");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("inuksuk")
			.definitionEquals(null)
			.decompositionIs("inuksuk/1n")
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
				"innuksuk", "inukshuk", "inuksuk",
				// Why are these considered a translations of "inuksuk"?
				"from", "at", "held at", "held", "innuksuk ... lives",
				"iqaluit’s"
			)
			.highlightsAreSubsetOf("iu", "inuksuk")
		;
	}

	@Test
	public void test__WordDictEndpoint__QueryMatchesManyWords_NoneOfWhichIsTheQueryItself() throws Exception {

		// There are several words that match this
		// pattern
		String query = "inuk";
		WordDictInputs inputs = new WordDictInputs(query);
		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("inuk", "inuksuk", "inukku", "inuktut");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("inuk")
			.definitionEquals(null)
			.decompositionIs()
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
				"innuksuk", "inukshuk", "inuksuk",
				// Why are these considered a translations of "inuksuk"?
				"held", "(interpretation",
				"name", "individuals", "person"
			)
			.highlightsAreSubsetOf("iu", "inuksuk", "inuk", "inuk ... inuk")
		;
	}


	@Test
	public void test__WordDictEndpoint__InputRomanButSyllabicOutputRequested() throws Exception {

		// There are several words that match this
		// pattern
		String query = "inuk";
		WordDictInputs inputs = new WordDictInputs(query);
		inputs.iuAlphabet = TransCoder.Script.SYLLABIC;
		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("ᐃᓄᒃ", "ᐃᓄᒃᓱᒃ", "ᐃᓄᒃᑯ", "ᐃᓄᒃᑐᑦ");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("ᐃᓄᒃ")
			.definitionEquals(null)
			.decompositionIs()
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
				"innuksuk", "inukshuk", "inuksuk",
			// Why are these considered a translations of "inuksuk"?
				"held", "(interpretation",
				"name", "individuals", "person"
			)
			.highlightsAreSubsetOf("iu", "ᐃᓄᒃ")
			;
	}

	@Test
	public void test__WordDictEndpoint__EnglishInputWord() throws Exception {
		String query = "housing";
		WordDictInputs inputs = new WordDictInputs(query, "en");
		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("housing");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("housing")
			.definitionEquals(null)
			.decompositionIs()
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en", true, "housing")
			.highlightsAreSubsetOf("iu", true,
				"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ", "ᐃᒡᓗᓕᕆᓂᕐᒥ", "ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ",
				"ᐃᒡᓗᖏᓐᓄᑦ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ")
		;
	}

	@Test
	public void test__WordDictEndpoint__VariousCases() throws Exception {

		Case[] cases = new Case[]{
			new Case("iu-igluga",
				// Query language
				"iu",
				// Query word
				"igluga",
				// Output alphabet (null means leave as is)
				null,
				// Expected min matching words
				10,
				// Expected words found
				new String[] {"igluga", "iglugalait"},
				// Expected Decomposition for query word.
				// Set to empty array for an English query word because English words are never
				// decomposed.
				new String[] {"iglu/1n", "ga/tn-nom-s-1s"},
				// Expected related words
				new String[] {
					"illuit", "illulirinirmut", "illumut", "illunik", "illunut"},
				// Expected translations
				new String[]{"house", "home", "rent"},
				// Expected min total bilingual examples (for both orig and related words)
				1000
			),

			new Case("en-housing",
				"en", "housing", null, 10,
				new String[] {"housing"},
				new String[0],
				new String[0],
				new String[]{"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ", "ᐃᒡᓗᓕᕆᓂᕐᒥ",
					"ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ", "ᐃᒡᓗᖏᓐᓄᑦ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ"},
				1000
			),

			// This IU word is not found in the hansard, but it DOES decompose.
			// So, we ARE able to show any meaningful information about it
			new Case("iu-iqqanaijaqtulirijikkut",
				"iu", "iqqanaijaqtulirijikkut", null, 15,
				new String[] {"iqqanaijaqtulirijikkut"},
				new String[] {"iqqanaijaq/1v", "juq/1vn", "liri/1nv", "ji/1vn",
					"kkut/1nn"},
				new String[] {
					"iqanaijaqtuliriji", "iqanaijaqtulirijikkunni", "iqanaijartulirijiit",
					"iqanaijartulirijikkunnut", "iqanaijartulirijikkut"},
				new String[]{"hiring", "human resources",
					"human resources ... personnel", "branch summary", "resources"},
				1000
			),

			// This IU word is not found in the hansard, AND it DOES NOT decompose.
			// So no hits should be found.
			new Case("iu-iqqanakkk", "iu", "iqqanakkk", null, 0),

			// In this case, the input is roman, but we request the results in
			// syllabics
			new Case("iu-igluga-roman2syll",
				"iu", "igluga", TransCoder.Script.SYLLABIC, 10,
				new String[] {"ᐃᒡᓗᒐ", "ᐃᒡᓗᒐᓚᐃᑦ"},
				new String[] {"iglu/1n", "ga/tn-nom-s-1s"},
				new String[] {
					"ᐃᓪᓗᐃᑦ", "ᐃᓪᓗᒧᑦ", "ᐃᓪᓗᓂᒃ", "ᐃᓪᓗᓄᑦ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ"},
				new String[]{"house", "home", "rent"},
				1000
			),

			// In this case, the input is syllabics, but we request the results in
			// roman
			new Case("iu-igluga-syll2roman",
				"iu", "ᐃᒡᓗᒐ", TransCoder.Script.ROMAN, 10,
				new String[] {"igluga", "igluga"},
				new String[] {"iglu/1n", "ga/tn-nom-s-1s"},
				new String[] {
					"illuit", "illulirinirmut", "illumut", "illunik", "illunut"},
				new String[]{"house", "home", "rent"},
				1000
			),
		};

		Consumer<Case> runner =
			(aCase) ->
			{
				try {
					String lang = (String) aCase.data[0];
					String otherLang = MultilingualDictEntry.otherLang(lang);
					String query = (String) aCase.data[1];
					TransCoder.Script iuAlphabet = (TransCoder.Script) aCase.data[2];
					Integer expMinHits = (Integer) aCase.data[3];
					String[] expWords = null;
					String[] expDecomp = null;
					String[] expRelatedWords = null;
					String[] expTranslations = null;
					Integer expMinExamples = null;
					if (aCase.data.length > 4) {
						expWords = (String[]) aCase.data[4];
						expDecomp = (String[]) aCase.data[5];
						expRelatedWords = (String[]) aCase.data[6];
						expTranslations = (String[]) aCase.data[7];
					}
					WordDictInputs inputs = new WordDictInputs(query, lang);
					inputs.iuAlphabet = iuAlphabet;
					WordDictResult epResult =
						(WordDictResult) endPoint.executeThenConvert(inputs);
					AssertEndpointResult hitsAsserter =
						new AssertWordDictResult(epResult, aCase.descr)
						.raisesNoError()
						.foundAtLeastNWords(expMinHits)
						;

					if (expWords != null) {
						hitsAsserter.foundWords(expWords);
					}

					if (expMinHits > 0) {
						new AssertMultilingualDictEntry(epResult.queryWordEntry, aCase.descr)
							.isForWord(epResult.convertedQuery)
							.definitionEquals(null)
							.decompositionIs(expDecomp)
							.relatedWordsIsSubsetOf(expRelatedWords)
							.atLeastNExamples(expMinHits)
							.highlightsAreSubsetOf(lang, true, epResult.convertedQuery)
							.highlightsAreSubsetOf(otherLang, true, expTranslations)
							;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(5)
//			.onlyCasesWithDescr("iu-igluga-syll2roman")
			.run();
	}
}
