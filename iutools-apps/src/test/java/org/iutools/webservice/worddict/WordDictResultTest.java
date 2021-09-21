package org.iutools.webservice.worddict;

import org.junit.jupiter.api.Test;

public class WordDictResultTest {

	@Test
	public void test__WordDictResult__jsonification() throws Exception {
		WordDictResult result = new WordDictResult();

		new AssertWordDictResult(result)
			.jsonEquals("{\"errorMessage\":null,\"failingInputs\":null,\"lang\":null,\"matchingWords\":null,\"otherLang\":null,\"queryWordEntry\":null,\"stackTrace\":null,\"status\":null,\"taskID\":null,\"taskStartTime\":null,\"totalWords\":null}");
	}

}
