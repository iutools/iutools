package org.iutools.sql;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

public class SQLLeakMonitorTest {

	//////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////

	@Test
	public void test__SQLLeakMonitor__Synopsis() throws Exception {
		// Use SQLLeakMonitor to monitor if some SQL resources (Statement, ResultSet)
		// were leaked between two time points
		//
		SQLLeakMonitor monitor = new SQLLeakMonitor();

		// Say we open some SQL resources
		Pair<Statement, ResultSet> resources = SQLTestHelpers.openManagedResources();

		// At this point, the monitor will tell you that some resources were leaked
		// since the time when it (the monitor) was created.
		int totalLeaked = monitor.leakedResources();
		Assertions.assertTrue(totalLeaked > 0);

		int leakedRS = monitor.leakedResultSets();
		Assertions.assertTrue(leakedRS > 0);

		int leakedStmts = monitor.leakedStatements();
		Assertions.assertTrue(leakedStmts > 0);

		// If we close the resouces, then the monitor will not report any leaks
		// anymore.
	}
}
