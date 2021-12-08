package org.iutools.worddict;


import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.iutools.concordancer.tm.TMEvaluator.*;
import org.iutools.worddict.MultilingualDict.WhatTerm;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class DictEvaluatorTest {

	@Test
	public void test__onNewGlossaryEntry__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("nunavut - Spotted in ORIG word alignments",
				// IU term in ROMAN
				"nunavut",
				// EN translation
				"nunavut",
				// Whether we found the ORIGINAL IU term, or had to look
				// for the RELATED terms
				WhatTerm.ORIGINAL,
				// The "mode" in which the EN term could be SPOTTED in an alignment
				// of either the ORIGINAL or RELATED IU terms
				MatchType.STRICT
			),
			new Case("uikipitia - ORIG and RELATED terms absent",
				// IU term in ROMAN
				"uikipitia", "astronomical object",
				null, null
			),
			new Case("tukiliuqpaa - RELATED term found, but no SPOTTING",
				// IU term in ROMAN
				"uikipitia", "astronomical object",
				null, null
			),
		};

		Consumer<Case> runner = (aCase) -> {
			String iuTerm_roman = (String) aCase.data[0];
			String enTerm = (String) aCase.data[1];
			WhatTerm expWhatTerm = (WhatTerm) aCase.data[2];
			MatchType expEnSpotted = (MatchType) aCase.data[3];

			GlossaryEntry entry = new GlossaryEntry()
				.setTermInLang("iu_roman", iuTerm_roman)
				.setTermInLang("en", enTerm);

			DictEvaluationResults results = new DictEvaluationResults();
			try {
				new DictEvaluator().onNewGlossaryEntry(entry, results);
				FrequencyHistogram<WhatTerm> expIUPresent_hist =
					new FrequencyHistogram<WhatTerm>();
				if (expWhatTerm != null) {
					expIUPresent_hist.updateFreq(expWhatTerm);
				}


				new AssertDictEvaluationResults(results)
					.totalGlossaryEntries(1)
					.iuPresentHistogramEquals(expIUPresent_hist)
					;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
			.run();

	}


}
