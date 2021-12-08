package org.iutools.worddict;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import org.iutools.concordancer.tm.TMEvaluator.*;
import org.iutools.worddict.MultilingualDict.WhatTerm;

public class DictEvaluationResults  {
	int totalGlossaryEntries = 0;

	FrequencyHistogram<WhatTerm> iuPresent_hist =
		new FrequencyHistogram<WhatTerm>();

	FrequencyHistogram<MatchType> iuSpotted_hist =
		new FrequencyHistogram<MatchType>();

	public long totalIUPresent(WhatTerm where) {
		return iuPresent_hist.frequency(where);
	}

	public void onIUPresent(WhatTerm whatTerm) {
		iuPresent_hist.updateFreq(whatTerm);
	}

	public void onENSpotting(MatchType inSense) {
		iuSpotted_hist.updateFreq(inSense);
	}
}
