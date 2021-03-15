package org.iutools.webservice.morphexamples;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

public class MorphemeExamplesEndpointTest extends EndpointTest {

	@Override
	public Endpoint makeEndpoint() {
		return new MorphemeExamplesEndpoint();
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__MorphemeExamplesEndpoint__HappyPath() throws Exception {
		String[] corpusWords = new String[] {
			"ujaraqsiurnirmik", "aanniasiuqtiit", "iglumik", "tuktusiuqti"
		};

		MorphemeExamplesInputs examplesInputs =
			new MorphemeExamplesInputs("siuq","compiled_corpus","2");

		EndpointResult epResponse = endPoint.execute(examplesInputs);
		new AssertMorphemeExamplesResponse(epResponse)
			.exampleScoredExamplesAre(
				new Pair[] {
					Pair.of("ammuumajuqsiuqtutik", 10004.0),
					Pair.of("ittuqsiutitaaqpattut", 10002.0)});
	}
}