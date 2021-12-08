package org.iutools.worddict;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import org.iutools.concordancer.tm.TMEvaluator;
import org.iutools.concordancer.tm.TMEvaluator.*;
import org.iutools.worddict.MultilingualDict.WhatTerm;
import org.junit.platform.engine.support.discovery.SelectorResolver;

public class DictEvaluationResults  {
	int totalGlossaryEntries = 0;

	FrequencyHistogram<WhatTerm> iuPresent_hist =
		new FrequencyHistogram<WhatTerm>();

	FrequencyHistogram<MatchType> iuSpotted_hist =
		new FrequencyHistogram<MatchType>();

	public long totalIUPresent(WhatTerm where) {
		return iuPresent_hist.frequency(where);
	}
	public long totalIUPresent() {
		long total =
			iuPresent_hist.frequency(WhatTerm.ORIGINAL) +
			iuPresent_hist.frequency(WhatTerm.RELATED);
		return total;
	}

	public long totalIUSpotted(MatchType inSense) {
		return iuSpotted_hist.frequency(inSense);
	}

	public long totalIUSpotted_atLeastInSense(MatchType inSense) {
		long total = 0;
		if (inSense != null) {
			for (MatchType aSense : TMEvaluator.matchTypes()) {
				total += totalIUSpotted(aSense);
				if (aSense == inSense) {
					break;
				}
			}
		}
		return total;
	}


	public void onIUPresent(WhatTerm whatTerm) {
		iuPresent_hist.updateFreq(whatTerm);
	}

	public void onENSpotting(MatchType inSense) {
		iuSpotted_hist.updateFreq(inSense);
	}

	public double rateENSpotted(MatchType inSense) {
		double rate = 0.0;
		long outOfTotal = totalIUPresent();
		if (outOfTotal > 0) {
			rate =
				1.0 * totalIUSpotted_atLeastInSense(inSense) /
				outOfTotal;
		}

		return rate;
	}
}
