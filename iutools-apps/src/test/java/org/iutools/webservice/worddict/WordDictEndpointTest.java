package org.iutools.webservice.worddict;

import ca.nrc.config.ConfigException;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.webservice.EndpointTest;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.AssertIUWordDictEntry;
import org.iutools.worddict.IUWordDictEntry;
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
		IUWordDictEntry wordEntry = epResult.queryWordEntry;
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

		new AssertIUWordDictEntry(epResult.queryWordEntry)
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

		new AssertIUWordDictEntry(epResult.queryWordEntry)
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
