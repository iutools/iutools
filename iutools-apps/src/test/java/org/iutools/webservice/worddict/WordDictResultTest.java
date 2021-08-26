package org.iutools.webservice.worddict;

import org.iutools.webservice.AssertEndpointResult;
import org.junit.jupiter.api.Test;

public class WordDictResultTest {

	@Test
	public void test__WordDictResult__jsonification() throws Exception {
		WordDictResult result = new WordDictResult();

		new AssertWordDictResult(result)
			.jsonEquals("{\"errorMessage\":null,\"failingInputs\":null,\"matchingWords\":null,\"queryWordEntry\":null,\"stackTrace\":null,\"status\":null,\"taskID\":null,\"totalWords\":null}");
	}

}
