package org.iutools.webservice.tokenize;

import ca.nrc.config.ConfigException;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.iutools.webservice.ServiceException;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

public class TokenizeEndpointTest extends EndpointTest {

	@Override
	public Endpoint makeEndpoint() throws SpellCheckerException, FileNotFoundException, ConfigException, ServiceException {
		return new TokenizeEndpoint();
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__TokenizeEndpoint__HappyPath() throws Exception {

		TokenizeInputs inputs = new TokenizeInputs("nunavut, inuktut");
		EndpointResult epResult = endPoint.execute(inputs);

		Pair<String,Boolean>[] expTokens = new Pair[] {
			Pair.of("nunavut", true),
			Pair.of(",", false),
			Pair.of(" ", false),
			Pair.of("inuktut", true)
		};

		new AssertTokenizeResult(epResult)
			.raisesNoError()
			.producesTokens(expTokens)
		;
	}
}
