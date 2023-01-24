package org.iutools.webservice.morphdict;

import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.apache.commons.lang3.tuple.Pair;
import static org.iutools.script.TransCoder.Script;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
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
		CaseMorphemeDictEndpoint[] cases = new CaseMorphemeDictEndpoint[] {

			new CaseMorphemeDictEndpoint(
				"Just canonical form; form has exact matches and more",
				"tut", null, null)
				.morphIDsAre("tut/1v", "tut/tn-sim-s", "tutaq/1n", "tuti/1v", "tutik/1v")
				.morphCanonicalsAre("tut", "tut", "tutaq", "tuti", "tutik"),

			new CaseMorphemeDictEndpoint(
				"Just canonical form; form only has matches that start with that form",
				"titi", null, null)
				.morphIDsAre("titiq/1v", "titiqqartalik/1n")
				.morphCanonicalsAre("titiq", "titiqqartalik"),

			new CaseMorphemeDictEndpoint("canonical form + grammar",
				"tut", "verb", null)
				.morphIDsAre("tut/1v", "tuti/1v", "tutik/1v")
				.morphCanonicalsAre("tut", "tuti", "tutik"),


			new CaseMorphemeDictEndpoint("canonical form + meaning",
				"tut", null, "step")
				.morphIDsAre("tuti/1v")
				.morphCanonicalsAre("tuti"),

			new CaseMorphemeDictEndpoint("grammar + meaning",
				null, "verb", "step")
				.morphIDsAre("abluq/1v", "tuti/1v")
				.morphCanonicalsAre("abluq", "tuti"),

			new CaseMorphemeDictEndpoint("canonical + grammar + meaning",
				"tut", "verb", "step")
				.morphIDsAre("tuti/1v")
				.morphCanonicalsAre("tuti"),

			new CaseMorphemeDictEndpoint("Output in Syll",
				"tut", null, null)
				.useScript(Script.SYLLABIC)
				.morphIDsAre("tut/1v", "tut/tn-sim-s", "tutaq/1n",
					"tuti/1v", "tutik/1v")
				.morphCanonicalsAre("ᑐᑦ", "ᑐᑦ", "ᑐᑕᖅ", "ᑐᑎ", "ᑐᑎᒃ"),

			new CaseMorphemeDictEndpoint("Input in Syll, output in ROMAN",
				"ᑐᑦ", null, null)
				.useScript(Script.ROMAN)
				.morphIDsAre("tut/1v", "tut/tn-sim-s", "tutaq/1n", "tuti/1v", "tutik/1v")
				.morphCanonicalsAre("tut", "tutaq", "tuti", "tutik"),

			new CaseMorphemeDictEndpoint("Input not IU text",
				"hello", null, null),
		};

		Consumer<Case> runner = (caseUncast) -> {
			CaseMorphemeDictEndpoint caze = (CaseMorphemeDictEndpoint)caseUncast;
			try {
				MorphemeDictInputs inputs =
					new MorphemeDictInputs(caze.canonicalForm, caze.grammar, caze.meaning,
						"compiled_corpus", "2");
				inputs.setIUAlphabet(caze.outputScript);

  				EndpointResult epResponse = endPoint.execute(inputs);
				AssertMorphemeDictResult asserter = new AssertMorphemeDictResult(epResponse)
					.matchingMorphIDsAre(caze.expMorphIDs)
					.matchingMorphCanonicalsAre(caze.expMorphCanonicals)
					.examplesAreInScript(caze.outputScript);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(7)
//			.onlyCasesWithDescr("Input in Syll, output in ROMAN")
			.run();
	}

	//////////////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////////////

	public static class CaseMorphemeDictEndpoint extends Case {

		public String canonicalForm = null;
		public String grammar = null;
		public String meaning = null;
		public Script outputScript = Script.ROMAN;

		public String[] expMorphIDs = new String[0];
		public String[] expMorphCanonicals = new String[0];

		public Map<String,String[]> expExamplesIndex = new HashMap<>();

		public CaseMorphemeDictEndpoint(String _descr, String _canonicalForm, String _grammar, String _meaning) {
			super(_descr, null);
			this.canonicalForm = _canonicalForm;
			this.grammar = _grammar;
			this.meaning = _meaning;
		}

		public CaseMorphemeDictEndpoint useScript(Script _outputScript) {
			this.outputScript = _outputScript;
			return this;
		}

		public CaseMorphemeDictEndpoint morphIDsAre(String... _expMorphIDs) {
			this.expMorphIDs = _expMorphIDs;
			return this;
		}
		public CaseMorphemeDictEndpoint morphCanonicalsAre(String... _expMorphCanonicals) {
			this.expMorphCanonicals = _expMorphCanonicals;
			return this;
		}

		public CaseMorphemeDictEndpoint providesExamples(String morphID, String... expExamples) {
			expExamplesIndex.put(morphID, expExamples);
			return this;
		}

	}
}