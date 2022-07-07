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
		WordDictInputs inputs = new WordDictInputs(query).setExactWordLookup(false);

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
				"iqaluit’s", "held ... inuksuk", "inuit", "inuit people",
				"mona ... lea ... innukshuk",
				"social", "inukshuk high", "inuksuk high", "nakasuk ... inuksuk"
			)
			.highlightsAreSubsetOf("iu", "inuksuk")
		;
	}

	@Test
	public void test__WordDictEndpoint__QueryMatchesManyWords_NoneOfWhichIsTheQueryItself() throws Exception {

		// There are several words that match this
		// pattern
		String query = "inuk";
		WordDictInputs inputs =
			new WordDictInputs(query)
			.setExactWordLookup(false);
		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("inuk", "inuksuk", "inuktituuqtut", "inuktituusuunit");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("inuk")
			.definitionEquals(null)
			.decompositionIs("inuk/1n")
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
					// These are English translations of the noun "inuk"
					"inuit", "individual", "individuals", "inuk",  "person",
					// These are English translations of the proper name "Inuk"
					"enook", "enuk ... pauloosie",
					"mr. enook", "mr. enook (interpretation", "name ... enook",
					"mr. enoki ... glenn",
					// Why are these considered a translations of "inuk"?
					"held", "(interpretation", "kenny ... jennifer",
					"name",

					// Those seem to be translations of "inuksuk" as opposed to
					// "inuk". Not sure why they are included.
					"innuksuk", "inukshuk", "inuksuk"

			)
			.highlightsAreSubsetOf("iu", "inuksuk", "inuk", "inuk ... inuk")
		;
	}


	@Test
	public void test__WordDictEndpoint__InputRomanButSyllabicOutputRequested() throws Exception {

		// There are several words that match this
		// pattern
		String query = "inuk";
		WordDictInputs inputs = new WordDictInputs(query).setExactWordLookup(false);
		inputs.iuAlphabet = TransCoder.Script.SYLLABIC;
		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("ᐃᓄᒃ", "ᐃᓄᒃᓱᒃ", "ᐃᓄᒃᑎᑑᖅᑐᑦ", "ᐃᓄᒃᑎᑑᓲᓂᑦ");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("ᐃᓄᒃ")
			.definitionEquals(null)
			.decompositionIs("inuk/1n")
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
				"inuit", "innuksuk", "inukshuk", "inuksuk",
			// Why are these considered a translations of "inuksuk"?
				"held", "(interpretation", "name", "individuals", "person",
				"enook", "enuk ... pauloosie", "mr. enook",
				"mr. enook (interpretation", "name ... enook", "individual",
				"inuk",
				"kenny ... jennifer",
				"mr. enoki ... glenn"
			)
			.highlightsAreSubsetOf("iu", "ᐃᓄᒃ")
			;
	}

	@Test
	public void test__WordDictEndpoint__EnglishInputWord() throws Exception {
		String query = "housing";
		WordDictInputs inputs = new WordDictInputs(query, "en").setExactWordLookup(false);
		WordDictResult epResult = (WordDictResult) endPoint.executeThenConvert(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("housing");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("housing")
			.definitionEquals(null)
			.decompositionIs()
			.atLeastNExamples(8)
			.highlightsAreSubsetOf("en", true, "housing")
			.highlightsAreSubsetOf("iu", true,
				"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ", "ᐃᒡᓗᓕᕆᓂᕐᒥ", "ᐃᒡᓗᓕᕆᓂᕐᓕ ... ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ",
				"ᐃᒡᓗᖏᓐᓄᑦ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ", "ᐃᓐᓇᑐᖃᓕᕆᓂᕐᒧᑦ", "ᐃᓪᓗᐃᑦ", "ᐃᓪᓗᓂᒃ",
				"ᐃᓪᓗᓕᕆᔨᒃᑯᑦ", "ᐃᓪᓗᓕᕆᔨᒃᑯᓐᓄᑦ", "ᐃᓪᓗᓕᕆᔨᓂ","ᐃᓪᓗᓕᕆᔨᓂᒃ")
		;
	}

	@Test
	public void test__WordDictEndpoint__VariousCases() throws Exception {

		WordDictEndpointCase[] cases = new WordDictEndpointCase[]{

		// This word is out-of-corpus and is not valid.
		// When searching for that exact word, we expect an entry that does not
		// have any information in it.
			new WordDictEndpointCase("iu-EXACT_WORD-out_of_corpus_invalid_word-")
				.query("ninuksuk")
				.exactWordLookup()
				.queryLang("iu")
				.givesEmptyWordEntry(),

			new WordDictEndpointCase("iu-EXACT_WORD-igluga")
				.queryLang("iu")
				.query("igluga")
				.exactWordLookup()
				.decomp(new String[] {"iglu/1n", "ga/tn-nom-s-1s"})
				.relatedWordsIncludedIn(
					new String[] {
						"illuit", "illulirinirmut", "illumut", "illunik", "illunut",
						"igluqaqtittinirmut", "illulirinirmi", "illuliriniup",
						"illunginnu", "illungit"})
				.translationsIncludedIn(
					new String[]{"house", "home", "rent"})
				.minExamples(5),

			new WordDictEndpointCase("iu-SEARCH-igluga")
				.queryLang("iu")
				.query("igluga")
				.minHits(10)
				.hitListStartsWithWords(new String[] {"igluga", "iglugalait"})
				.decomp(new String[] {"iglu/1n", "ga/tn-nom-s-1s"})
				.relatedWordsIncludedIn(
					new String[] {
						"illuit", "illulirinirmut", "illumut", "illunik", "illunut",
						"igluqaqtittinirmut", "illulirinirmi", "illuliriniup",
						"illunginnu", "illungit"})
				.translationsIncludedIn(new String[]{"house", "home", "rent"})
				.minExamples(5),

			new WordDictEndpointCase("en-SEARCH-housing")
				.queryLang("en")
				.query("housing")
				.iuAlphabet(TransCoder.Script.SYLLABIC)
				.minHits(10)
				.translationsIncludedIn(new String[]{
					"ᐃᓪᓗᓕᕆᔨᓂ", "ᐃᓪᓗᓕᕆᔨᓂᒃ",
					"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ", "ᐃᒡᓗᓕᕆᓂᕐᒥ",
					"ᐃᓪᓗᐃᑦ", "ᐃᓪᓗᓂᒃ", "ᐃᓪᓗᓕᕆᔨᒃᑯᓐᓄᑦ"
					})
				.minExamples(5),

//			// This IU word is not found in the corpus, but it DOES decompose.
//			// So, we ARE able to show some meaningful information about it
//			new WordDictEndpointCase("iu-SEARCH-iqqanaijaqtulirijikkut")
//				.queryLang("iu")
//				.query("iqqanaijaqtulirijikkut")
//				.minHits(12)
//				.hitListStartsWithWords(new String[] {"iqqanaijaqtulirijikkut", "BLAH"})
//				.decomp(
//					new String[] {"iqqanaijaq/1v", "juq/1vn", "liri/1nv", "ji/1vn",
//						"kkut/1nn"})
//				.relatedWordsIncludedIn(new String[] {
//					"iqanaijaqtuliriji", "iqanaijaqtulirijikkunni", "iqanaijartulirijiit",
//					"iqanaijartulirijikkunnut", "iqanaijartulirijikkut"})
//				.translationsIncludedIn(new String[] {
//					"department ... human resources", "hr", "human resource",
//					"human resources", "human resources. branch summary",
//					"human resources department", "human resources. department summary",
//					// The following are actually bad, but they may come up
//					"public works", "staffed"})
//				.minExamples(5),

			// This IU word is not found in the hansard, AND it DOES NOT decompose.
			// So no hits should be found.
			new WordDictEndpointCase("iu-SEARCH-iqqanakkk")
				.queryLang("iu")
				.query("iqqanakkk")
				.noHits(),

			// In this case, the input is roman, but we request the results in
			// syllabics
			new WordDictEndpointCase("iu-SEARCH-igluga-roman2syll")
				.queryLang("iu")
				.query("igluga")
				.iuAlphabet(TransCoder.Script.SYLLABIC)
				.minHits(10)
				.hitListStartsWithWords(new String[] {"ᐃᒡᓗᒐ", "ᐃᒡᓗᒐᓚᐃᑦ"})
				.decomp(new String[] {"iglu/1n", "ga/tn-nom-s-1s"})
				.relatedWordsIncludedIn(new String[] {
					"ᐃᓪᓗᐃᑦ", "ᐃᓪᓗᒧᑦ", "ᐃᓪᓗᓂᒃ", "ᐃᓪᓗᓄᑦ", "ᐃᓪᓗᓕᕆᓂᕐᒧᑦ"})
				.translationsIncludedIn(new String[]{"house", "home", "rent"})
				.minExamples(5),

			// In this case, the input is syllabics, but we request the results in
			// roman
			new WordDictEndpointCase("iu-SEARCH-igluga-syll2roman")
				.queryLang("iu")
				.query("ᐃᒡᓗᒐ")
				.iuAlphabet(TransCoder.Script.ROMAN)
				.minHits(10)
				.hitListStartsWithWords(new String[] {"igluga", "igluga"})
				.decomp(new String[] {"iglu/1n", "ga/tn-nom-s-1s"})
				.relatedWordsIncludedIn(new String[] {
					"illuit", "illulirinirmut", "illumut", "illunik", "illunut",
					"igluqaqtittinirmut", "illulirinirmi", "illuliriniup", "illungit"})
				.translationsIncludedIn(new String[]{"house", "home", "rent"})
				.minExamples(5),

			new WordDictEndpointCase("en-SEARCH-housing-roman")
				.queryLang("en")
				.query("housing")
				.iuAlphabet(TransCoder.Script.ROMAN)
				.translationsIncludedIn(new String[]{
					"illulirijini", "illulirijinik",
					"iglugijauvaktunik", "iglulirinirmi",
					"illuit", "illunik", "illulirijikkunnut"})
				.minExamples(5),

			new WordDictEndpointCase("en-SEARCH-housing-syll")
				.queryLang("en")
				.query("housing")
				.iuAlphabet(TransCoder.Script.SYLLABIC)
				.translationsIncludedIn(new String[]{
					"ᐃᓪᓗᓕᕆᔨᓂ", "ᐃᓪᓗᓕᕆᔨᓂᒃ",
					"ᐃᒡᓗᒋᔭᐅᕙᒃᑐᓂᒃ", "ᐃᒡᓗᓕᕆᓂᕐᒥ",
					"ᐃᓪᓗᐃᑦ", "ᐃᓪᓗᓂᒃ", "ᐃᓪᓗᓕᕆᔨᒃᑯᓐᓄᑦ"})
				.minExamples(5),

			new WordDictEndpointCase("iu-SEARCH-Igluga-capitalized")
				.queryLang("iu")
				.query("Igluga")
				.iuAlphabet(TransCoder.Script.ROMAN)
				.minHits(10)
				.decomp(new String[] {"iglu/1n", "ga/tn-nom-s-1s"})
				.relatedWordsIncludedIn(new String[] {
					"illuit", "illulirinirmut", "illumut", "illunik", "illunut"})
				.translationsIncludedIn(new String[]{"house", "home", "rent"})
				.minExamples(5),
		};

		Consumer<Case> runner =
			(uncastCase) ->
			{
				try {
					WordDictEndpointCase aCase = (WordDictEndpointCase)uncastCase;
					String otherLang = MultilingualDictEntry.otherLang(aCase.lang);
					if (aCase.lang.equals("en")) {
						aCase
							.noDecomp()
							.noRelatedWords()
							.minHits(1)
							.maxHits(1)
							.hitListStartsWithWords(aCase.query);
					}
					if (aCase.exactWordLookup) {
						aCase
							.minHits(0)
							.maxHits(0)
							.hitListStartsWithWords(aCase.query);
					}

					WordDictInputs inputs =
						new WordDictInputs(aCase.query, aCase.lang);
					if (aCase.exactWordLookup) {
						inputs.exactWordLookup = true;
					}
					inputs.iuAlphabet = aCase.iuAlphabet;
					WordDictResult epResult =
						(WordDictResult) endPoint.executeThenConvert(inputs);
					AssertEndpointResult hitsAsserter =
						new AssertWordDictResult(epResult, aCase.descr)
							.raisesNoError()
							.foundAtLeastNWords(aCase.expMinHits)
							.foundAtMostNWords(aCase.expMaxHits)
							.foundWords(aCase.expHitWords)
							;

					AssertMultilingualDictEntry entryAsserter =
						new AssertMultilingualDictEntry(epResult.queryWordEntry, aCase.descr);
					if (aCase.expectEmptyWordEntry) {
						entryAsserter
							.isForWord(epResult.convertedQuery)
							.gaveEmptyWordEntry();
					} else if (aCase.expMinHits != null && aCase.expMinHits > 0) {
						entryAsserter
							.isForWord(epResult.convertedQuery)
							.definitionEquals(null)
							.decompositionIs(aCase.expDecomp)
							.relatedWordsIsSubsetOf(aCase.expRelatedWords)
							.atLeastNExamples(aCase.expMinExamples)
							.translationsAreNonEmptySubsetOf(aCase.expTranslations)
							.highlightsAreSubsetOf(aCase.lang, true, epResult.convertedQuery)
							.highlightsAreSubsetOf(otherLang, true, aCase.expTranslations)
							;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(4)
//			.onlyCasesWithDescr("en-SEARCH-housing")
			.run();
	}

	public static class WordDictEndpointCase extends Case {

		public boolean exactWordLookup = false;
		public String lang = "iu";
		public String query = null;
		public TransCoder.Script iuAlphabet = TransCoder.Script.ROMAN;
		public String[] hitWords = null;
		public Integer expMinHits = null;
		public Integer expMaxHits = null;
		public String[] expHitWords = new String[0];
		public String[] expDecomp = null;
		public String 	expDefinition = null;
		public String[] expRelatedWords = null;
		public Integer maxRelatedWords = null;
		public Integer minRelatedWords = null;
		public String[] expTranslations = null;
		public Integer maxTranslations = null;
		public Integer minTranslations = null;
		public Integer expMinExamples = null;
		public Integer expMaxExamples = null;
		public boolean expectEmptyWordEntry = false;

		public WordDictEndpointCase(String _descr, Object... _data) {
			super(_descr, _data);
		}

		public WordDictEndpointCase(String _descr) {
			super(_descr, new Object[0]);
		}

		public WordDictEndpointCase queryLang(String _lang) {
			this.lang = _lang;
			return this;
		}

		public WordDictEndpointCase iuAlphabet(TransCoder.Script _alphabet) {
			this.iuAlphabet = _alphabet;
			return this;
		}

		public WordDictEndpointCase query(String _query) {
			this.query = _query;
			return this;
		}

		public WordDictEndpointCase exactWordLookup() {
			this.exactWordLookup = true;
			return this;
		}

		public WordDictEndpointCase hitListStartsWithWords(String... _hitWords) {
			this.hitWords = _hitWords;
			return this;
		}

		public WordDictEndpointCase noHits() {
			maxHits(0);
			expHitWords = new String[0];
			return this;
		}


		public WordDictEndpointCase minHits(int _expMinHits) {
			this.expMinHits = _expMinHits;
			return this;
		}

		public WordDictEndpointCase maxHits(int _expMaxHits) {
			this.expMaxHits = _expMaxHits;
			return this;
		}

		public WordDictEndpointCase setExpHitWords(String[] _expHitWords) {
			this.expHitWords = _expHitWords;
			return this;
		}

		public WordDictEndpointCase decomp(String[] _expDecomp) {
			this.expDecomp = _expDecomp;
			return this;
		}

		public WordDictEndpointCase noDecomp() {
			this.expDecomp = new String[0];
			return this;
		}

		public WordDictEndpointCase definition(String _def) {
			this.expDefinition = _def;
			return this;
		}

		public WordDictEndpointCase noDefinition() {
			this.expDefinition = null;
			return this;
		}

		public WordDictEndpointCase relatedWordsIncludedIn(String[] _expRelatedWords) {
			this.expRelatedWords = _expRelatedWords;
			return this;
		}

		public WordDictEndpointCase noRelatedWords() {
			this.maxRelatedWords = 0;
			this.expRelatedWords = new String[0];
			return this;
		}


		public WordDictEndpointCase translationsIncludedIn(String[] _expTranslations) {
			this.expTranslations = _expTranslations;
			return this;
		}

		public WordDictEndpointCase noTranslations() {
			this.maxTranslations = 0;
			this.translationsIncludedIn(null);
			this.minExamples(0);
			this.maxExamples(0);
			return this;
		}

		public WordDictEndpointCase minTranslations(int _minTranslations) {
			this.minTranslations = _minTranslations;
			return this;
		}

		public WordDictEndpointCase maxTranslations(int _maxTranslations) {
			this.maxTranslations = _maxTranslations;
			return this;
		}

		public WordDictEndpointCase minExamples(int _expMinExamples) {
			this.expMinExamples = _expMinExamples;
			return this;
		}

		public WordDictEndpointCase maxExamples(int _expMaxExamples) {
			this.expMaxExamples = _expMaxExamples;
			return this;
		}

		public WordDictEndpointCase givesEmptyWordEntry() {
			this.expectEmptyWordEntry = true;
			return this;
		}
	}
}
