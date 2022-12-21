package org.iutools.webservice.spell;

import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class ShallowCheckTextEndpointTest extends EndpointTest {
	@Override
	public Endpoint makeEndpoint() throws Exception {
		return new ShallowCheckTextEndpoint();
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__ShallowCheckTextEndpoint__VariousCases() throws Exception {

		ShallowCheckCase[] cases = new ShallowCheckCase[] {
			new ShallowCheckCase(
				"Syllabic text that violates a rule for which ther are no fixes",
				"ᑯᐊᐳᕇᓴᓐᑯᓐᓂ",
				"ᑯᐊᐳᕇᓴ[ᓐᑯ]ᓐᓂ",
				Pair.of("ᑯᐊᐳᕇᓴᓐᑯᓐᓂ", "ᑯᐊᐳᕇᓴ[ᓐᑯ]ᓐᓂ")
			),
			new ShallowCheckCase(
				"Syllabic text with 'ᕿ' incorrectly spelled as two chars ᕐ+ᑭ",
				"ᐃᓕᓐᓂᐊᕐᑭᑎ",
				// This looks the same as above, but instead of ᓐ+ᑭ, we have a single
				// character that looks the same, i.e.
				"ᐃᓕᓐᓂᐊᕿᑎ",
				Pair.of("ᐃᓕᓐᓂᐊᕐᑭᑎ", "ᐃᓕᓐᓂᐊᕿᑎ")
			),

			new ShallowCheckCase(
				"Syllabic text that PASSES SHALLOW check eventhough it would FAIL DEEP check",
				"ᐸᐅᓗᓯ, ᐊᕿᐊᕈᖅ ᓂᕈᐊᖅᑕᐅᓚᐅᖅᑐᖅ ᓂᕈᐊᕕᔾᔪᐊᕐᓇᐅᑎᓪᓗᒍ",
				"ᐸᐅᓗᓯ, ᐊᕿᐊᕈᖅ ᓂᕈᐊᖅᑕᐅᓚᐅᖅᑐᖅ ᓂᕈᐊᕕᔾᔪᐊᕐᓇᐅᑎᓪᓗᒍ"
			),

		};

		Consumer<Case> runner = (caseNoCasting) -> {
			ShallowCheckCase aCase = (ShallowCheckCase) caseNoCasting;
			String origText = aCase.origText;
			try {
				ShallowCheckTextInputs inputs = new ShallowCheckTextInputs(origText);
				EndpointResult epResult = endPoint.execute(inputs);
				new AssertShallowCheckTextResult(epResult)
					.correctedTextEquals(aCase.expCorrText)
					.badWordWere(aCase.expBadWords);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(2)
			.run()
			;

		return;
	}

	/////////////////////////////////
	// TEST HELPERS
	/////////////////////////////////

	public static class ShallowCheckCase extends RunOnCases.Case {

		public String origText = null;
		public String expCorrText = null;
		Pair<String,String>[] expBadWords = null;

		public ShallowCheckCase(String _descr, String _origText,
			String _expCorrText, Pair<String,String>... _badWords) {
			super(_descr, null);
			origText = _origText;
			expCorrText = _expCorrText;
			expBadWords = _badWords;
		}
	}

}
