package org.iutools.webservice.worddict;

import ca.nrc.config.ConfigException;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.webservice.EndpointTest;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.AssertIUWordDictEntry;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class WordDictEndpointTest extends EndpointTest {
	@Override
	public WordDictEndpoint makeEndpoint() throws SpellCheckerException, FileNotFoundException, ConfigException, ServiceException {
		return new WordDictEndpoint();
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__WordDictEndpoint__HappyPath() throws Exception {

		WordDictInputs inputs = new WordDictInputs("inuksuk");
		WordDictResult epResult = (WordDictResult) endPoint.execute(inputs);

		new AssertWordDictResult(epResult)
			.raisesNoError();

		new AssertIUWordDictEntry(epResult.entry)
			.isForWord("inuksuk")
			.definitionEquals(null)
			.decompositionIs("inuksuk/1n")
			.atLeastNExamples(10)
			.highlightsAreSubsetOf("en",
				// Why is this considered a translation of "inuksuk"?
				"from",
				"innuksuk", "inukshuk", "inuksuk")
			.highlightsAreSubsetOf("iu", "inuksuk")
		;
	}

}
