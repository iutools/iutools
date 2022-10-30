package org.iutools.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SQLLeakMonitorTest {

	//////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////

	@Test @Disabled
	public void test__SQLLeakMonitor__Synopsis() throws Exception {
		/*
		 * Use SQLLeakMonitor to monitor if some "managed" SQL resources (Statement, ResultSet)
		 * were leaked between two time points.
		 * By "managed", we mean resources that are created by classes in our SQL framework,
		 * more precisely:
		 *
		 * - QueryProcessor
		 * - ResultSetWrapper
		 * - ResultSetIterator
		 * - ResultSetColIterator
		 */

		//
		SQLLeakMonitor monitor = new SQLLeakMonitor();

		// Say we open a ResultSetWrapper (which in turn opens a Statement and
		// a ResultSet.
		ResultSetWrapper rsw = SQLTestHelpers.openResultSetWrapper();

		// At this point, the monitor will tell you that some resources were leaked
		// since the time when it (the monitor) was created.
		int totalLeaked = monitor.leakedResources();
		Assertions.assertTrue(totalLeaked > 0);

		int leakedRS = monitor.leakedResultSets();
		Assertions.assertTrue(leakedRS > 0);

		int leakedStmts = monitor.leakedStatements();
		Assertions.assertTrue(leakedStmts > 0);

		// If we close the wrapper (which in turn closes its Statement and
		// ResultSet), then the monitor will not report any leaks anymore.
		rsw.close();
		Assertions.assertTrue(monitor.leakedResources() > 0);
		Assertions.assertTrue(monitor.leakedResultSets() > 0);
		Assertions.assertTrue(monitor.leakedStatements() > 0);
	}
}
