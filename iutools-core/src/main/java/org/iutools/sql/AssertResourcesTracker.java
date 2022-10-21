package org.iutools.sql;

import org.junit.jupiter.api.Assertions;

public class AssertResourcesTracker {

	public static void totalStatementsEquals(String mess, int expTotal) {
		int gotTotal = ResourcesTracker.totalStatements();
		Assertions.assertEquals(
			expTotal, gotTotal,
			mess+"Total number of opened Statements was wrong");
	}

	public static void totalResultSetsEquals(String mess, int expTotal) {
		int gotTotal = ResourcesTracker.totalResultSets();
		Assertions.assertEquals(
			expTotal, gotTotal,
			mess+"Total number of opened ResultSets was wrong");
	}
}
