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
	public void test__MorphemeExamplesEndpoint__JustOneMatchingMorpheme() throws Exception {
		String[] corpusWords = new String[] {
			"ujaraqsiurnirmik", "aanniasiuqtiit", "iglumik", "tuktusiuqti"
		};

		MorphemeExamplesInputs examplesInputs =
			new MorphemeExamplesInputs("siuq","compiled_corpus","2");

		EndpointResult epResponse = endPoint.execute(examplesInputs);
		new AssertMorphemeExamplesResult(epResponse)
			.matchingMorphemesAre("siuq/1nv")
			.exampleScoredExamplesAre(
				new Pair[] {
					Pair.of("ammuumajuqsiuqtutik", 10004.0),
					Pair.of("ittuqsiutitaaqpattut", 10002.0)
				}
			);
	}

	@Test
	public void test__MorphemeExamplesEndpoint__SeveralMatchingMorphemes() throws Exception {
		String[] corpusWords = new String[] {
			"ujaraqsiurnirmik", "aanniasiuqtiit", "iglumik", "tuktusiuqti"
		};

		MorphemeExamplesInputs examplesInputs =
			new MorphemeExamplesInputs("tit","compiled_corpus","2");

		EndpointResult epResponse = endPoint.execute(examplesInputs);
		new AssertMorphemeExamplesResult(epResponse)
			.matchingMorphemesAre(
				"ilinniaqtit/1v",
				"katit/1v",
				"tit/1vv",
				"tit/tn-nom-p-2s",
				"titaq/1v",
				"titiq/1v",
				"titiraq/1v"
			)
		;
	}
}