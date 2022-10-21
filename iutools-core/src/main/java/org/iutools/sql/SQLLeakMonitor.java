package org.iutools.sql;

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

	public SQLLeakMonitor() {
		init__SQLLeakMonitor("");
	}

	public SQLLeakMonitor(String _mess) {
		init__SQLLeakMonitor(mess);
	}

	private void init__SQLLeakMonitor(String _mess) {
		this.mess = _mess;
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
		return;
	}
}
