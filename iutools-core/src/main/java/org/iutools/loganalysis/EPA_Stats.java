package org.iutools.loganalysis;

public class EPA_Stats extends ActionOrEndpointStats {
	long frequency = 0;
	long totalElapsedMSecs = 0;
	long totalExceptions = 0;

	public double avgMsecs() {
		double avg = 0.0;
		if (frequency != 0) {
			avg = 1.0 * totalElapsedMSecs / frequency;
		}
		return avg;
	}

	public double exceptionsRate() {
		double rate = 0.0;
		if (frequency != 0) {
			rate = 1.0 * totalExceptions / frequency;
		}
		return rate;
	}
}
