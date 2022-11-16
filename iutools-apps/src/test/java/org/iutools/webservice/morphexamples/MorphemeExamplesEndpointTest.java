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
			.matchingMorphemesAre(
				Pair.of("siuq/1nv",
					"searching, looking for s.t. (trans.: of, for, about s.o.); travelling through space or time (spend); feasting, celebrating")
			)
			.examplesForMorphemeAre("siuq/1nv",
				new String[] {
					"isumassaqsiurningit", "angiqtauniqsiuqtuq"
				});
		;
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
				Pair.of("tit/1vv",
					"to cause s.o. (refl.: oneself) to do s.t."),
				Pair.of("tit/tn-gen-p-2s",
					"genitive: of your (many things to one person) "),
				Pair.of("tit/tn-nom-p-2s",
					"nominative: your (many things to one person) "),
				Pair.of("titaq/1v",
					"to make music, esp. on the accordion"),
				Pair.of("titiq/1v",
					"to mark something with a stroke, a sign, etc. with an instrument"),
				Pair.of("titiqqartalik/1n",
					"one of Harbour Islands (Repulse Bay area)"),
				Pair.of("titulaaq/1v",
				"to whistle or to blow, as a horn, a whistle, etc."),
				Pair.of("titunaq/tpd-sim-p",
					"like"),
				Pair.of("titut/tn-sim-p",
					"similaris: like many; the (many)")
			)
			.examplesForMorphemeAre("tit/tn-nom-p-2s",
				new String[] {
					"akitujuutit",
					"iluunnatit"
				});
		;
	}
}