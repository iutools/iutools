package org.iutools.sql;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;

/**
 * Check if we leaked some "managed" SQL resources between a start point and the current
 * time.
 *
 * By "managed", we mean resources that are created by classes in our SQL framework,
 * more precisely:
 *
 * - QueryProcessor
 * - ResultSetWrapper
 * - ResultSetIterator
 * - ResultSetColIterator
 *
 */
public class SQLLeakMonitor {

	private String mess = "";

	// Number of resources when we started monitoring
	int rsAtStart = 0;
	int stmtAtStart = 0;

	// Current number of resources
	int rsNow = 0;
	int stmtNow = 0;

	// Number of resources leaked
	int rsLeaked = 0;
	int stmtLeaked = 0;

	public boolean silent = false;

	public SQLLeakMonitor() {
		init__SQLLeakMonitor("");
	}

	public SQLLeakMonitor(String _mess) {
		init__SQLLeakMonitor(mess);
	}

	private void init__SQLLeakMonitor(String _mess) {
		this.mess = _mess;
		Pair<Integer, Integer> openedResources = currentOpenResources();
		stmtAtStart = openedResources.getLeft();
		rsAtStart = openedResources.getRight();
		int totalAtStart = stmtAtStart+rsAtStart;
		if (!silent && (stmtAtStart+rsAtStart) > 0) {
			System.out.println("SQLLeakMonitor["+mess+"]: There were already some leaked resource when we started this monitor: Statements="+stmtAtStart+", ResultSets="+rsAtStart);
		}
	}

	private Pair<Integer, Integer> currentOpenResources() {
		int totalStatements = ResourcesTracker.totalStatements(true);
		int totalRS = ResourcesTracker.totalResultSets(true);
		return Pair.of(totalStatements, totalRS);
	}

	public int leakedResources() {
		check();
		int leaked = stmtLeaked + rsLeaked;
		return leaked;
	}

	public int leakedStatements() {
		check();
		return stmtLeaked;
	}

	public int leakedResultSets() {
		check();
		return rsLeaked;
	}

	public void check() {
		boolean ok = true;
		stmtNow = ResourcesTracker.totalStatements(true);
		stmtLeaked = Math.max(0, stmtNow - stmtAtStart);

		rsNow = ResourcesTracker.totalResultSets(true);
		rsLeaked = Math.max(0, rsNow - rsAtStart);
		if (!silent && (stmtLeaked+rsLeaked) > 0) {
			System.out.println("SQLLeakMonitor["+mess+"]: Some resources were leaked since we started this monitor: Statements="+stmtLeaked+", ResultSets="+rsLeaked);
		}
		return;
	}

	public void assertNoLeaks() {
		int leakedSets = leakedResultSets();
		int leakedStatements = leakedStatements();
		int totalLeaked = leakedSets + leakedStatements;
		if (totalLeaked > 0) {
			String mess =
				"Some SQL resources were leaked!\n";
			if (leakedSets > 0) {
				mess += "  ResultSets : " + leakedSets + "\n";
			}
			if (leakedStatements > 0) {
				mess += "  Statements : " +leakedStatements+ "\n";
			}
			Assertions.fail(mess);
		}
	}
}
