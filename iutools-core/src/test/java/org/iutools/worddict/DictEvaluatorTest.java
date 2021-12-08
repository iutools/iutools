package org.iutools.worddict;


import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.iutools.concordancer.tm.TMEvaluator.*;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class DictEvaluatorTest {

	@Test
	public void test__onNewGlossaryEntry__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("nunavut",
				// IU term in ROMAN
				"nunavut",
				// EN translation
				"nunavut",
				// Whether the ORIGINAL IU term is present in the TM
				true,
				// The "mode" in which the EN term could be SPOTTED in an alignment
				// of the ORIGINAL IU term
				MatchType.STRICT,
				// Whether one of the RELATED IU terms was present in the TM
				true,
				// The "mode" in which the EN term could be SPOTTED in an alignment
				// of one of the RELATED IU terms
				MatchType.STRICT
			),
		};

		Consumer<Case> runner = (aCase) -> {
			String iuTerm_roman = (String) aCase.data[0];
			String enTerm = (String) aCase.data[1];
			Boolean origPresent = (Boolean) aCase.data[2];
			MatchType enSpotted4Orig = (MatchType) aCase.data[3];
			Boolean relatedPresent = (Boolean) aCase.data[4];
			MatchType enSpotted4Related = (MatchType) aCase.data[5];

			GlossaryEntry entry = new GlossaryEntry()
				.setTermInLang("iu_roman", iuTerm_roman)
				.setTermInLang("en", enTerm);

			DictEvaluationResults results = new DictEvaluationResults();
			try {
				new DictEvaluator().onNewGlossaryEntry(entry, results);
				new AssertDictEvaluationResults(results)
					.totalGlossaryEntries(1)
					;
			} catch (MultilingualDictException e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
			.run();

	}
}
