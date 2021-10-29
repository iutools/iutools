package org.iutools.loganalysis;

import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

public class AssertEPA_Stats extends Asserter<EPA_Stats> {
	public AssertEPA_Stats(EPA_Stats _stats) {
		super(_stats);
	}

	public AssertEPA_Stats(EPA_Stats _stats, String _mess) {
		super(_stats, _mess);
	}

	public EPA_Stats stats() {
		return (EPA_Stats)gotObject;
	}

	public AssertEPA_Stats frequencyIs(long expFreq) {
		Assertions.assertEquals(expFreq, stats().frequency,
			baseMessage+"\nFrequency was not as expected");
		return this;
	}

	public AssertEPA_Stats avgMSecsIs(double expAvgMsecs) {
		AssertNumber.assertEquals(
			baseMessage+"\nFrequency was not as expected",
			expAvgMsecs, stats().avgMsecs(), 0.1);

		return this;
	}
}
