package org.iutools.loganalysis;

import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

public class AssertEPA_Sats extends Asserter<EPA_Stats> {
	public AssertEPA_Sats(EPA_Stats _stats) {
		super(_stats);
	}

	public AssertEPA_Sats(EPA_Stats _stats, String _mess) {
		super(_stats, _mess);
	}

	public EPA_Stats stats() {
		return (EPA_Stats)gotObject;
	}

	public AssertEPA_Sats frequencyIs(long expFreq) {
		Assertions.assertEquals(expFreq, stats().frequency,
			baseMessage+"\nFrequency was not as expected");
		return this;
	}

	public AssertEPA_Sats avgMSecsIs(double expAvgMsecs) {
		Assertions.assertEquals(expAvgMsecs, stats().avgMsecs(),
			baseMessage+"\nFrequency was not as expected");
		return this;
	}
}
