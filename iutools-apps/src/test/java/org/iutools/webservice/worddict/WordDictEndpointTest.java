package org.iutools.webservice.worddict;

import ca.nrc.config.ConfigException;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.webservice.EndpointTest;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.AssertMultilingualDictEntry;
import org.iutools.worddict.MultilingualDictEntry;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

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
			inputs.wordIsEnglish = true;
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
				"from", "at ... at", "held at"
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
			.foundWords("inuksuk", "inukku", "inuktut");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("inuksuk")
			.definitionEquals(null)
			.decompositionIs("inuksuk/1n")
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
				"innuksuk", "inukshuk", "inuksuk",
				// Why are these considered a translations of "inuksuk"?
				"from", "at ... at", "held at"
			)
			.highlightsAreSubsetOf("iu", "inuksuk")
		;
	}

	@Test @Ignore
	public void test__WordDictEndpoint__EnglishInputWord() throws Exception {
		String query = "housing";
		WordDictInputs inputs = new WordDictInputs(query, true);
		WordDictResult epResult = (WordDictResult) endPoint.execute(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError()
			.foundWords("BLAH");

		new AssertMultilingualDictEntry(epResult.queryWordEntry)
			.isForWord("inuksuk")
			.definitionEquals(null)
			.decompositionIs("inuksuk/1n")
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
				"innuksuk", "inukshuk", "inuksuk",
				// Why are these considered a translations of "inuksuk"?
				"from", "at ... at", "held at"
			)
			.highlightsAreSubsetOf("iu", "inuksuk")
		;
	}
}
