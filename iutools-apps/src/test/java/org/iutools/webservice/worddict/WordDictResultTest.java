package org.iutools.webservice.worddict;

import org.iutools.webservice.AssertEndpointResult;
import org.junit.jupiter.api.Test;

public class WordDictResultTest {

	@Test
	public void test__WordDictResult__jsonification() throws Exception {
		WordDictResult result = new WordDictResult();

		new AssertWordDictResult(result)
			.jsonEquals("{\"entry\":null,\"errorMessage\":null,\"failingInputs\":null,\"stackTrace\":null,\"status\":null,\"taskID\":null}");
	}

}
