package org.iutools.webservice.worddict;

import ca.nrc.config.ConfigException;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
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
		WordDictResult epResult = (WordDictResult) endPoint.execute(inputs);

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
			epResult = (WordDictResult) endPoint.execute(inputs);
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

		WordDictResult epResult = (WordDictResult) endPoint.execute(inputs);

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
		WordDictResult epResult = (WordDictResult) endPoint.execute(inputs);

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
	public void test__WordDictEndpoint__EnglishInputWord() throws Exception {
		String query = "housing";
		WordDictInputs inputs = new WordDictInputs(query, "en");
		WordDictResult epResult = (WordDictResult) endPoint.execute(inputs);

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

			new Case("iu-ᐊᒻᒨᒪᔪᖅ",
				// Query language
				"iu",
				// Query word
				"ᐊᒻᒨᒪᔪᖅ",
				// Expected min matching words
				10,
				// Expected Decomposition for query word.
				// Set to empty array for an English query word because English words are never
				// decomposed.
				new String[] {"iglu/1n", "ga/tn-nom-s-1s"},
				// Expected translations
				new String[]{"house", "home", "rent"}
			),

			new Case("iu-igluga",
				// Query language
				"iu",
				// Query word
				"igluga",
				// Expected min matching words
				10,
				// Expected Decomposition for query word.
				// Set to empty array for an English query word because English words are never
				// decomposed.
				new String[] {"iglu/1n", "ga/tn-nom-s-1s"},
				// Expected translations
				new String[]{"house", "home", "rent"}
			),

			new Case("en-housing",
				"en", "housing", 10,
				new String[0],
				new String[]{"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ", "ᐃᒡᓗᓕᕆᓂᕐᒥ",
					"ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ", "ᐃᒡᓗᖏᓐᓄᑦ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ"}
			),

			// This IU word is not found in the hansard, but it DOES decompose.
			// So, we ARE able to show any meaningful information about it
			new Case("iu-iqqanaijaqtulirijikkut",
				"iu", "iqqanaijaqtulirijikkut", 15,
				new String[] {"iqqanaijaq/1v", "juq/1vn", "liri/1nv", "ji/1vn",
					"kkut/1nn"},
				new String[]{"hiring", "human resources",
					"human resources ... personnel", "branch summary", "resources"}
			),

			// This IU word is not found in the hansard, AND it DOES NOT decompose.
			// So no hits should be found.
			new Case("iu-iqqanakkk", "iu", "iqqanakkk", 0),
		};

		Consumer<Case> runner =
			(aCase) ->
			{
				try {
					String lang = (String) aCase.data[0];
					String otherLang = MultilingualDictEntry.otherLang(lang);
					String query = (String) aCase.data[1];
					Integer expMinHits = (Integer) aCase.data[2];

					String[] expDecomp = null;
					String[] expTranslations = null;
					if (aCase.data.length > 3) {
						expDecomp = (String[]) aCase.data[3];
						expTranslations = (String[]) aCase.data[4];
					}
					WordDictInputs inputs = new WordDictInputs(query, lang);
					WordDictResult epResult =
						(WordDictResult) endPoint.execute(inputs);

					AssertEndpointResult hitsAsserter =
						new AssertWordDictResult(epResult, aCase.descr)
						.raisesNoError()
						.foundAtLeastNWords(expMinHits);

					if (expMinHits > 0) {
						new AssertMultilingualDictEntry(epResult.queryWordEntry, aCase.descr)
							.isForWord(query)
							.definitionEquals(null)
							.decompositionIs(expDecomp)
							.atLeastNExamples(expMinHits)
							.highlightsAreSubsetOf(lang, true, query)
							.highlightsAreSubsetOf(otherLang, true, expTranslations)
							;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(3)
			.run();
	}
}
