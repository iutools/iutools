package org.iutools.loganalysis;

public class EPA_Stats extends ActionOrEndpointStats {
	long frequency = 0;
	long totalElapsedMSecs = 0;

	public double avgMsecs() {
		double avg = 0.0;
		if (frequency != 0) {
			avg = 1.0 * totalElapsedMSecs / frequency;
		}
		return avg;
	}
}
