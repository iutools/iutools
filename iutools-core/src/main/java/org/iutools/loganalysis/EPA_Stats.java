package org.iutools.loganalysis;

import ca.nrc.json.PrettyPrinter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EPA_Stats extends ActionOrEndpointStats {
	public String category;
	public long frequency = 0;
	public long totalElapsedMSecs = 0;
	public long totalExceptions = 0;
	public long totalStarted = 0;
	public long totalEnded = 0;

	public static void main(String[] args) {
		EPA_Stats stats = new EPA_Stats("someaction");
		stats.frequency = 10;
		stats.totalElapsedMSecs = 3931;

		System.out.println("stats="+ PrettyPrinter.print(stats));
	}

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

	public double avgSecs() {
		double avg = 0.0;
		if (frequency != 0) {
			avg = 1.0 * totalElapsedMSecs / (1000 * frequency);
		}
		return avg;
	}

	public double totalElapsedSecs() {
		double total = 0.0;
		if (frequency != 0) {
			total = totalElapsedMSecs / 1000;
		}
		return total;
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
