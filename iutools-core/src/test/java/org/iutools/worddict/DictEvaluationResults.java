package org.iutools.worddict;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import org.iutools.worddict.MultilingualDict.WhatTerm;

public class DictEvaluationResults  {
	int totalGlossaryEntries = 0;
	FrequencyHistogram<WhatTerm> iuPresent_hist =
		new FrequencyHistogram<WhatTerm>();

	public int totalIUPresent(boolean includingRelatedTerms) {
		return -1;
	}

	public void onIUPresent(WhatTerm whatTerm) {
		iuPresent_hist.updateFreq(whatTerm);
	}

}
