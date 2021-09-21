package org.iutools.webservice.tokenize;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import org.iutools.webservice.AssertEndpointResult;

public class TokenizeResultTest {

	@Test
	public void test__TokenizeResult__jsonification() throws Exception {
		TokenizeResult result = new TokenizeResult();
		List<Pair<String, Boolean>> tokens =
			new ArrayList<Pair<String, Boolean>>();
		tokens.add(Pair.of("inuksuk", true));
		tokens.add(Pair.of(", ", false));
		result.tokens = tokens;

		new AssertTokenizeResult(result)
			.jsonEquals("{\"errorMessage\":null,\"failingInputs\":null,\"stackTrace\":null,\"status\":null,\"taskID\":null,\"taskStartTime\":null,\"tokens\":[{\"inuksuk\":true},{\", \":false}]}");
	}
}
