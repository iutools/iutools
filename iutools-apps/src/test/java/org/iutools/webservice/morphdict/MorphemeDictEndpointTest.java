package org.iutools.webservice.morphdict;

import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class MorphemeDictEndpointTest extends EndpointTest {

	@Override
	public Endpoint makeEndpoint() {
		return new MorphemeDictEndpoint();
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__MorphemeExamplesEndpoint__JustOneMatchingMorpheme() throws Exception {
		String[] corpusWords = new String[] {
			"ujaraqsiurnirmik", "aanniasiuqtiit", "iglumik", "tuktusiuqti"
		};

		MorphemeDictInputs examplesInputs =
			new MorphemeDictInputs("siuq","compiled_corpus","2");

		EndpointResult epResponse = endPoint.execute(examplesInputs);
		new AssertMorphemeDictResult(epResponse)
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

		MorphemeDictInputs examplesInputs =
			new MorphemeDictInputs("tit","compiled_corpus","2");

		EndpointResult epResponse = endPoint.execute(examplesInputs);
		new AssertMorphemeDictResult(epResponse)
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

	@Test
	public void test__MorphemeExamplesEndpoint__VariousCases() throws Exception {
		Case[] cases = new RunOnCases.Case[] {

			new Case("Just canonical form; form has exact matches and more", "tut", null, null,
				"tut/1v", "tut/tn-sim-s", "tutaq/1n", "tuti/1v", "tutik/1v"),

			new Case("Just canonical form; form only has matches that start with that form",
		"titi", null, null,
				"titiq/1v", "titiqqartalik/1n"),

			new Case("canonical form + grammar", "tut", "verb", null,
				"tut/1v", "tuti/1v", "tutik/1v"),

			new Case("canonical form + meaning", "tut", null, "step",
				"tuti/1v"),

			new Case("grammar + meaning", null, "verb", "step",
				"abluq/1v", "tuti/1v"),

			new Case("canonical + grammar + meaning", "tut", "verb", "step",
				"tuti/1v"),
		};

		Consumer<Case> runner = (caze) -> {
			String canonical = (String)caze.data[0];
			String grammar = (String)caze.data[1];
			String meaning = (String)caze.data[2];
			String[] expMorphemes = new String[caze.data.length-3];
			for (int ii=3; ii < caze.data.length; ii++) {
				expMorphemes[ii-3] = (String)caze.data[ii];
			}

			try {
				MorphemeDictInputs examplesInputs =
					new MorphemeDictInputs(canonical, grammar, meaning, "compiled_corpus", "2");

  				EndpointResult epResponse = endPoint.execute(examplesInputs);
				new AssertMorphemeDictResult(epResponse)
					.matchingMorphIDsAre(expMorphemes);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(5)
			.run();
	}
}