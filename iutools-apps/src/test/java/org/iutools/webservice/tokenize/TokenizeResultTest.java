package org.iutools.webservice.tokenize;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.text.segmentation.Token;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import org.iutools.webservice.AssertEndpointResult;

public class TokenizeResultTest {

	@Test
	public void test__TokenizeResult__jsonification() throws Exception {
		TokenizeResult result = new TokenizeResult();
		List<Token> tokens = new ArrayList<Token>();
		tokens.add(new Token("inuksuk", true));
		tokens.add(new Token(", ", false));
		result.tokens = tokens;

		new AssertTokenizeResult(result)
			.jsonEquals("{\"errorMessage\":null,\"failingInputs\":null,\"stackTrace\":null,\"status\":null,\"taskElapsedMsecs\":null,\"taskID\":null,\"taskStartTime\":null,\"tokens\":[{\"isWord\":true,\"text\":\"inuksuk\"},{\"isWord\":false,\"text\":\", \"}]}");
	}
}
