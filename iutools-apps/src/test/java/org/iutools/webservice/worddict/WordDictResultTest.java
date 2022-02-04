package org.iutools.webservice.worddict;

import org.junit.jupiter.api.Test;

public class WordDictResultTest {

	@Test
	public void test__WordDictResult__jsonification() throws Exception {
		WordDictResult result = new WordDictResult();

		new AssertWordDictResult(result)
			.jsonEquals("{\"convertedQuery\":null,\"errorMessage\":null,\"failingInputs\":null,\"lang\":\"iu\",\"matchingWords\":null,\"otherLang\":\"en\",\"queryWordEntry\":null,\"stackTrace\":null,\"status\":null,\"taskElapsedMsecs\":null,\"taskID\":null,\"taskStartTime\":null,\"totalWords\":null}");
	}

}
