package org.iutools.webservice.tokenize;

import ca.nrc.datastructure.Pair;
import ca.nrc.testing.AssertObject;
import org.iutools.webservice.AssertEndpointResult;
import org.iutools.webservice.EndpointResult;
import org.junit.jupiter.api.Assertions;

public class AssertTokenizeResult extends AssertEndpointResult {
	public AssertTokenizeResult(EndpointResult _gotObject) {
		super(_gotObject);
	}

	public AssertTokenizeResult(EndpointResult _gotObject, String mess) {
		super(_gotObject, mess);
	}

	TokenizeResult result() {
		return (TokenizeResult) gotObject;
	}

	public AssertTokenizeResult raisesNoError() {
		Assertions.assertEquals(
			null, result().errorMessage,
			baseMessage+"\nResponse raised error");
		return this;
	}

	public AssertTokenizeResult producesTokens(
		Pair<String, Boolean>[] expTokens) throws Exception {

		AssertObject.assertDeepEquals(
				baseMessage+"\nTokens were not as expected",
				expTokens, result().tokens);
		return this;
	}

}
