package org.iutools.webservice.tokenize;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.junit.jupiter.api.Assertions;

public class AssertTokenize2Result extends AssertEndpointResult {
	public AssertTokenize2Result(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertTokenize2Result(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	Tokenize2Result result() {
		return (Tokenize2Result) gotObject;
	}

	public AssertTokenize2Result raisesNoError() {
		Assertions.assertEquals(
			null, result().errorMessage,
			baseMessage+"\nResponse raised error");
		return this;
	}

	public AssertTokenize2Result producesTokens(
		Pair<String, Boolean>[] expTokens) throws Exception {

		AssertObject.assertDeepEquals(
				baseMessage+"\nTokens were not as expected",
				expTokens, result().tokens);
		return this;
	}

}
