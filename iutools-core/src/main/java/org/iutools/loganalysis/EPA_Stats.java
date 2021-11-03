package org.iutools.loganalysis;

public class EPA_Stats extends ActionOrEndpointStats {
	public String category;
	private long frequency = 0;
	long totalElapsedMSecs = 0;
	long totalExceptions = 0;
	long totalStarted = 0;
	long totalEnded = 0;

	public EPA_Stats(String _category) {
		this.category = _category;
	}

	public long getFrequency() {
		return frequency;
	}

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

	public void onStart() {
		frequency++;
		totalStarted++;
	}

	public void onEnd(Integer elapsedMSecs) {
		if (elapsedMSecs != null) {
			totalElapsedMSecs += elapsedMSecs;
		}

	}
}
